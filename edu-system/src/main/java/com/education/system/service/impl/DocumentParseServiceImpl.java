package com.education.system.service.impl;

import com.education.system.entity.SysParseTemplate;
import com.education.system.mapper.SysParseTemplateMapper;
import com.education.system.service.IDocumentParseService;
import com.education.system.vo.DocumentParseResultVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI文档解析服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentParseServiceImpl implements IDocumentParseService {

    private final SysParseTemplateMapper parseTemplateMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public DocumentParseResultVO parseDocument(MultipartFile file, String documentType, 
                                                String businessType, String extraParams) {
        try {
            String filename = file.getOriginalFilename();
            if (filename == null) {
                return buildErrorResult("文件名不能为空");
            }

            // 根据文件类型选择解析方式
            if (filename.toLowerCase().endsWith(".pdf")) {
                return parsePdf(file, documentType, extraParams);
            } else if (filename.toLowerCase().endsWith(".docx") || filename.toLowerCase().endsWith(".doc")) {
                return parseWord(file, documentType, extraParams);
            } else {
                return buildErrorResult("不支持的文件格式，仅支持PDF和Word文档");
            }
        } catch (Exception e) {
            log.error("文档解析失败", e);
            return buildErrorResult("文档解析失败: " + e.getMessage());
        }
    }

    @Override
    public DocumentParseResultVO parsePdf(MultipartFile file, String documentType, String extraParams) {
        try (InputStream is = file.getInputStream();
             PDDocument document = PDDocument.load(is)) {
            
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            log.info("PDF文本提取成功，内容长度: {}", text.length());
            
            return parseByText(text, documentType, extraParams);
        } catch (Exception e) {
            log.error("PDF解析失败", e);
            return buildErrorResult("PDF解析失败: " + e.getMessage());
        }
    }

    @Override
    public DocumentParseResultVO parseWord(MultipartFile file, String documentType, String extraParams) {
        try (InputStream is = file.getInputStream();
             XWPFDocument document = new XWPFDocument(is)) {
            
            StringBuilder textBuilder = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                textBuilder.append(paragraph.getText()).append("\n");
            }
            
            String text = textBuilder.toString();
            log.info("Word文本提取成功，内容长度: {}", text.length());
            
            return parseByText(text, documentType, extraParams);
        } catch (Exception e) {
            log.error("Word解析失败", e);
            return buildErrorResult("Word解析失败: " + e.getMessage());
        }
    }

    @Override
    public DocumentParseResultVO parseWordDocument(String text, String extraParams) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // 按行分割
        String[] lines = text.split("\n");
        
        // 单词解析模式: 匹配 "单词 音标 释义" 或 "单词 释义" 格式
        // 支持格式:
        // 1. abandon [ə'bændən] v.放弃
        // 2. ability [ə'bɪləti] n.能力；才能
        // 3. able ['eɪbl] adj.能够
        
        Pattern wordPattern = Pattern.compile(
            "^\\s*([a-zA-Z]+)\\s+" +           // 单词
            "(?:\\[([^\\]]+)\\]\\s+)?" +       // 音标(可选)
            "([\\u4e00-\\u9fa5a-zA-Z.、；，,;]+)" // 中文释义
        );

        int lineNum = 0;
        for (String line : lines) {
            lineNum++;
            line = line.trim();
            
            if (line.isEmpty() || line.startsWith("=") || line.startsWith("-")) {
                continue; // 跳过空行和分隔线
            }

            Matcher matcher = wordPattern.matcher(line);
            if (matcher.find()) {
                Map<String, Object> wordData = new HashMap<>();
                wordData.put("word", matcher.group(1).trim());
                
                if (matcher.group(2) != null) {
                    wordData.put("phonetic", matcher.group(2).trim());
                }
                
                wordData.put("translation", matcher.group(3).trim());
                wordData.put("grade", "九年级"); // 默认九年级,可从extraParams获取
                wordData.put("difficulty", 2);   // 默认中等难度
                
                dataList.add(wordData);
            } else {
                warnings.add("第" + lineNum + "行无法识别: " + line.substring(0, Math.min(50, line.length())));
            }
        }

        return buildSuccessResult(dataList, warnings);
    }

    @Override
    public DocumentParseResultVO parseQuestionDocument(String text, String extraParams) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // 题目解析模式
        // 支持格式:
        // 1. 单选题: 题目内容 A.xxx B.xxx C.xxx D.xxx 答案:A
        // 2. 填空题: 题目内容 答案:xxx
        
        String[] lines = text.split("\n");
        StringBuilder currentQuestion = new StringBuilder();
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            currentQuestion.append(line).append("\n");
            
            // 检测到答案标记,表示一道题结束
            if (line.contains("答案:") || line.contains("答案：")) {
                Map<String, Object> questionData = parseSingleQuestion(currentQuestion.toString());
                if (questionData != null) {
                    dataList.add(questionData);
                } else {
                    warnings.add("无法解析题目: " + currentQuestion.toString().substring(0, Math.min(50, currentQuestion.length())));
                }
                currentQuestion = new StringBuilder();
            }
        }

        return buildSuccessResult(dataList, warnings);
    }

    /**
     * 根据文本内容解析
     */
    private DocumentParseResultVO parseByText(String text, String documentType, String extraParams) {
        if (documentType == null) {
            return buildErrorResult("文档类型不能为空");
        }

        // 尝试使用模板解析
        SysParseTemplate template = findTemplateByType(documentType);
        if (template != null && template.getStatus() == 1) {
            log.info("使用模板解析: {}", template.getTemplateName());
            return parseByTemplate(text, template);
        }

        // 回退到硬编码解析
        log.info("未找到模板,使用硬编码解析: {}", documentType);
        switch (documentType.toLowerCase()) {
            case "word":
            case "vocabulary":
                return parseWordDocument(text, extraParams);
            case "question":
            case "exam":
                return parseQuestionDocument(text, extraParams);
            default:
                return buildErrorResult("不支持的文档类型: " + documentType);
        }
    }

    /**
     * 查找匹配的模板
     */
    private SysParseTemplate findTemplateByType(String documentType) {
        // 优先查找启用的模板
        List<SysParseTemplate> templates = parseTemplateMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysParseTemplate>()
                .eq(SysParseTemplate::getDocumentType, documentType)
                .eq(SysParseTemplate::getStatus, 1)
                .eq(SysParseTemplate::getDr, 0)
                .orderByAsc(SysParseTemplate::getSortOrder)
        );
        
        return templates.isEmpty() ? null : templates.get(0);
    }

    /**
     * 使用模板解析文档
     */
    private DocumentParseResultVO parseByTemplate(String text, SysParseTemplate template) {
        try {
            JsonNode rules = objectMapper.readTree(template.getParseRules());
            List<Map<String, Object>> dataList = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            String[] lines = text.split("\n");
            
            // 根据文档类型选择不同的解析策略
            if ("question".equals(template.getDocumentType())) {
                return parseQuestionByTemplate(text, rules, warnings);
            } else {
                return parseGeneralByTemplate(lines, rules, template, warnings);
            }
        } catch (Exception e) {
            log.error("模板解析失败", e);
            return buildErrorResult("模板解析失败: " + e.getMessage());
        }
    }

    /**
     * 通用模板解析(单词、课程、学生等)
     */
    private DocumentParseResultVO parseGeneralByTemplate(String[] lines, JsonNode rules, 
                                                          SysParseTemplate template, List<String> warnings) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        
        String patternStr = rules.has("pattern") ? rules.get("pattern").asText() : null;
        if (patternStr == null) {
            return buildErrorResult("模板缺少pattern配置");
        }

        Pattern pattern = Pattern.compile(patternStr);
        JsonNode fields = rules.get("fields");
        JsonNode defaults = rules.get("defaults");
        JsonNode skipLines = rules.get("skipLines");

        int lineNum = 0;
        for (String line : lines) {
            lineNum++;
            line = line.trim();
            
            // 检查是否需要跳过
            if (shouldSkipLine(line, skipLines)) {
                continue;
            }

            if (line.isEmpty()) {
                continue;
            }

            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                Map<String, Object> data = new HashMap<>();
                
                // 提取字段
                for (int i = 0; i < fields.size(); i++) {
                    String fieldName = fields.get(i).asText();
                    if (matcher.groupCount() > i && matcher.group(i + 1) != null) {
                        data.put(fieldName, matcher.group(i + 1).trim());
                    }
                }
                
                // 应用默认值
                if (defaults != null) {
                    defaults.fieldNames().forEachRemaining(field -> {
                        if (!data.containsKey(field)) {
                            JsonNode value = defaults.get(field);
                            if (value.isTextual()) {
                                data.put(field, value.asText());
                            } else if (value.isNumber()) {
                                data.put(field, value.numberValue());
                            } else if (value.isBoolean()) {
                                data.put(field, value.asBoolean());
                            }
                        }
                    });
                }
                
                dataList.add(data);
            } else {
                warnings.add("第" + lineNum + "行无法识别: " + line.substring(0, Math.min(50, line.length())));
            }
        }

        return buildSuccessResult(dataList, warnings);
    }

    /**
     * 题目模板解析
     */
    private DocumentParseResultVO parseQuestionByTemplate(String text, JsonNode rules, List<String> warnings) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        
        String questionPattern = rules.has("questionPattern") ? rules.get("questionPattern").asText() : null;
        String answerPattern = rules.has("answerPattern") ? rules.get("answerPattern").asText() : null;
        String optionPattern = rules.has("optionPattern") ? rules.get("optionPattern").asText() : null;
        
        if (answerPattern == null) {
            return buildErrorResult("题目模板缺少answerPattern配置");
        }

        // 按答案分割题目
        String[] parts = text.split(answerPattern);
        JsonNode defaults = rules.get("defaults");
        
        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;
            
            Map<String, Object> question = new HashMap<>();
            
            // 提取答案
            Pattern ansPattern = Pattern.compile(answerPattern);
            Matcher ansMatcher = ansPattern.matcher(part);
            if (ansMatcher.find()) {
                question.put("correctAnswer", ansMatcher.group(1).toUpperCase());
            }
            
            // 提取选项
            if (optionPattern != null) {
                Pattern optPattern = Pattern.compile(optionPattern);
                Matcher optMatcher = optPattern.matcher(part);
                List<Map<String, String>> options = new ArrayList<>();
                while (optMatcher.find()) {
                    Map<String, String> option = new HashMap<>();
                    option.put("label", optMatcher.group(1));
                    option.put("content", optMatcher.group(2).trim());
                    options.add(option);
                }
                
                if (!options.isEmpty()) {
                    question.put("questionType", 1); // 单选
                    question.put("options", options);
                }
            }
            
            // 题目内容
            String content = part.replaceAll(answerPattern, "").trim();
            question.put("questionTitle", content);
            
            // 应用默认值
            if (defaults != null) {
                defaults.fieldNames().forEachRemaining(field -> {
                    if (!question.containsKey(field)) {
                        JsonNode value = defaults.get(field);
                        if (value.isNumber()) {
                            question.put(field, value.numberValue());
                        }
                    }
                });
            }
            
            if (question.containsKey("correctAnswer")) {
                dataList.add(question);
            }
        }

        return buildSuccessResult(dataList, warnings);
    }

    /**
     * 判断是否应该跳过该行
     */
    private boolean shouldSkipLine(String line, JsonNode skipLines) {
        if (skipLines == null || !skipLines.isArray()) {
            return false;
        }
        
        for (JsonNode skipPattern : skipLines) {
            if (line.matches(skipPattern.asText())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解析单道题目
     */
    private Map<String, Object> parseSingleQuestion(String questionText) {
        Map<String, Object> question = new HashMap<>();
        
        // 提取答案
        Pattern answerPattern = Pattern.compile("答案[:：]\\s*([A-Da-d]+)");
        Matcher answerMatcher = answerPattern.matcher(questionText);
        if (answerMatcher.find()) {
            question.put("correctAnswer", answerMatcher.group(1).toUpperCase());
        } else {
            return null;
        }

        // 提取选项
        Pattern optionPattern = Pattern.compile("([A-D])[.．、]\\s*([^A-D答案\n]+)");
        Matcher optionMatcher = optionPattern.matcher(questionText);
        List<Map<String, String>> options = new ArrayList<>();
        while (optionMatcher.find()) {
            Map<String, String> option = new HashMap<>();
            option.put("label", optionMatcher.group(1));
            option.put("content", optionMatcher.group(2).trim());
            options.add(option);
        }

        if (!options.isEmpty()) {
            question.put("questionType", 1); // 单选题
            question.put("options", options);
        } else {
            question.put("questionType", 4); // 填空题
        }

        // 提取题目内容(去掉答案和选项部分)
        String questionContent = questionText
            .replaceAll("答案[:：].*", "")
            .replaceAll("[A-D][.．、][^A-D答案\n]+", "")
            .trim();
        question.put("questionTitle", questionContent);
        question.put("difficulty", 2);
        question.put("score", 2.0);

        return question;
    }

    /**
     * 构建成功结果
     */
    private DocumentParseResultVO buildSuccessResult(List<Map<String, Object>> dataList, List<String> warnings) {
        DocumentParseResultVO result = new DocumentParseResultVO();
        result.setSuccess(true);
        result.setMessage("解析成功，共解析 " + dataList.size() + " 条数据");
        result.setTotalCount(dataList.size());
        result.setDataList(dataList);
        result.setWarnings(warnings);
        return result;
    }

    /**
     * 构建错误结果
     */
    private DocumentParseResultVO buildErrorResult(String message) {
        DocumentParseResultVO result = new DocumentParseResultVO();
        result.setSuccess(false);
        result.setMessage(message);
        result.setTotalCount(0);
        result.setDataList(new ArrayList<>());
        result.setWarnings(new ArrayList<>());
        return result;
    }
}
