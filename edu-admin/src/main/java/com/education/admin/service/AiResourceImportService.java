package com.education.admin.service;

import com.education.exam.entity.ExamQuestion;
import com.education.exam.mapper.ExamQuestionMapper;
import com.education.system.entity.AiResource;
import com.education.system.entity.SysWord;
import com.education.system.mapper.SysWordMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * AI资源导入服务
 * 负责将审核通过的资源导入到对应的业务表
 */
@Slf4j
@Service
public class AiResourceImportService {

    @Autowired
    private ExamQuestionMapper examQuestionMapper;

    @Autowired
    private SysWordMapper sysWordMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 导入资源到业务表
     */
    @Transactional(rollbackFor = Exception.class)
    public String importResource(AiResource resource) {
        String resourceType = resource.getResourceType();
        
        if (resourceType == null) {
            throw new IllegalArgumentException("资源类型不能为空");
        }

        log.info("开始导入资源: ID={}, 类型={}, 标题={}", 
                resource.getId(), resourceType, resource.getTitle());

        try {
            switch (resourceType) {
                case "exam_paper":
                case "exam_question":
                    return importQuestions(resource);
                case "word":
                case "vocabulary":
                    return importWords(resource);
                default:
                    return "不支持的资源类型: " + resourceType;
            }
        } catch (Exception e) {
            log.error("资源导入失败: ID={}", resource.getId(), e);
            throw new RuntimeException("资源导入失败: " + e.getMessage(), e);
        }
    }

    /**
     * 导入题目
     */
    private String importQuestions(AiResource resource) {
        List<ExamQuestion> questions = parseQuestions(resource.getContent());
        if (questions.isEmpty()) {
            return "未解析到题目数据";
        }

        int successCount = 0;
        for (ExamQuestion question : questions) {
            try {
                examQuestionMapper.insert(question);
                successCount++;
            } catch (Exception e) {
                log.error("题目导入失败: {}", question.getQuestionTitle(), e);
            }
        }

        return String.format("成功导入 %d/%d 道题目", successCount, questions.size());
    }

    /**
     * 导入单词
     */
    private String importWords(AiResource resource) {
        List<SysWord> words = parseWords(resource.getContent());
        if (words.isEmpty()) {
            return "未解析到单词数据";
        }

        int successCount = 0;
        for (SysWord word : words) {
            try {
                sysWordMapper.insert(word);
                successCount++;
            } catch (Exception e) {
                log.error("单词导入失败: {}", word.getWord(), e);
            }
        }

        return String.format("成功导入 %d/%d 个单词", successCount, words.size());
    }

    /**
     * 解析题目JSON
     */
    private List<ExamQuestion> parseQuestions(String content) {
        List<ExamQuestion> questions = new ArrayList<>();
        try {
            JsonNode rootNode = objectMapper.readTree(content);
            JsonNode questionsNode = rootNode.has("questions") ? rootNode.get("questions") : rootNode;
            
            if (!questionsNode.isArray()) {
                return questions;
            }

            for (JsonNode node : questionsNode) {
                questions.add(parseQuestion(node));
            }
        } catch (Exception e) {
            log.error("解析题目数据失败", e);
        }
        return questions;
    }

    /**
     * 解析单个题目
     */
    private ExamQuestion parseQuestion(JsonNode node) {
        ExamQuestion q = new ExamQuestion();
        q.setQuestionTitle(getText(node, "questionTitle", "title", "content"));
        q.setQuestionType(getInt(node, "questionType"));
        if (q.getQuestionType() == null) {
            q.setQuestionType(convertQuestionType(getText(node, "type")));
        }
        q.setCorrectAnswer(getText(node, "correctAnswer", "answer"));
        
        JsonNode optionsNode = node.get("options");
        if (optionsNode != null) q.setOptions(optionsNode.toString());
        
        q.setAnalysis(getText(node, "analysis", "explanation"));
        q.setDifficulty(getInt(node, "difficulty"));
        if (q.getDifficulty() == null) q.setDifficulty(2);
        
        q.setScore(getDecimal(node, "score"));
        if (q.getScore() == null) q.setScore(new BigDecimal("10"));
        
        JsonNode imagesNode = node.get("images");
        if (imagesNode != null) {
            q.setImages(imagesNode.toString());
            q.setImageCount(imagesNode.size());
        }
        
        q.setSourceType(4); // AI抓取
        q.setStatus(1);
        q.setUsageCount(0);
        q.setCreateTime(new Date());
        q.setUpdateTime(new Date());
        q.setDr(0);
        
        return q;
    }

    /**
     * 解析单词JSON
     */
    private List<SysWord> parseWords(String content) {
        List<SysWord> words = new ArrayList<>();
        try {
            JsonNode rootNode = objectMapper.readTree(content);
            JsonNode wordsNode = rootNode.has("words") ? rootNode.get("words") : rootNode;
            
            if (!wordsNode.isArray()) {
                return words;
            }

            for (JsonNode node : wordsNode) {
                words.add(parseWord(node));
            }
        } catch (Exception e) {
            log.error("解析单词数据失败", e);
        }
        return words;
    }

    /**
     * 解析单个单词
     */
    private SysWord parseWord(JsonNode node) {
        SysWord w = new SysWord();
        w.setWord(getText(node, "word", "term"));
        w.setPhonetic(getText(node, "phonetic", "pronunciation"));
        w.setTranslation(getText(node, "translation", "meaning", "definition"));
        w.setExample(getText(node, "example", "sentence"));
        w.setDifficulty(getInt(node, "difficulty"));
        if (w.getDifficulty() == null) w.setDifficulty(2);
        
        w.setQuestionType("spell,choice_select,choice_word,translate,sentence_fill");
        w.setAiRecommended(1);
        w.setWrongCount(0);
        w.setCreateTime(new Date());
        w.setUpdateTime(new Date());
        
        return w;
    }

    // 工具方法
    private String getText(JsonNode node, String... fields) {
        for (String field : fields) {
            JsonNode value = node.get(field);
            if (value != null && !value.isNull() && StringUtils.hasText(value.asText())) {
                return value.asText();
            }
        }
        return null;
    }

    private Integer getInt(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null && !value.isNull() ? value.asInt() : null;
    }

    private BigDecimal getDecimal(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null && !value.isNull() ? new BigDecimal(value.asText()) : null;
    }

    private Integer convertQuestionType(String typeStr) {
        if (!StringUtils.hasText(typeStr)) return null;
        switch (typeStr.toLowerCase()) {
            case "single": case "single_choice": case "单选": return 1;
            case "multiple": case "multiple_choice": case "多选": return 2;
            case "judge": case "judgment": case "判断": return 3;
            case "fill": case "blank": case "填空": return 4;
            case "short": case "essay": case "简答": return 5;
            default: return null;
        }
    }
}
