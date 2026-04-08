package com.education.exam.service;

import com.education.common.result.RpcResult;
import com.education.exam.entity.Question;
import com.education.exam.entity.QuestionOption;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 试卷解析服务类
 * 
 * <p>提供Word试卷和文本试卷的解析功能，将题目批量导入题库。</p>
 * 
 * <h3>支持的试卷格式：</h3>
 * <pre>
 * 1. 题目格式：
 *    1. 题目内容
 *    1、题目内容
 *    1) 题目内容
 * 
 * 2. 选项格式：
 *    A. 选项内容
 *    A、选项内容
 *    A) 选项内容
 * 
 * 3. 答案格式：
 *    答案：A
 *    答案: ABC
 * </pre>
 * 
 * <h3>解析流程：</h3>
 * <pre>
 * 1. 读取Word/文本内容
 * 2. 逐行解析，识别题目、选项、答案
 * 3. 构建 Question 对象
 * 4. 批量保存到数据库
 * </pre>
 * 
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 解析Word试卷
 * MultipartFile file = ...;
 * JsonResult result = paperParseService.parseWordPaper(file, gradeId, subjectId);
 * 
 * // 解析文本试卷
 * String content = "1. 第一题\nA. 选项A\n答案：A";
 * JsonResult result = paperParseService.parseTextPaper(content, gradeId, subjectId);
 * }</pre>
 * 
 * <h3>注意事项：</h3>
 * <ul>
 *   <li>题目编号必须连续（1, 2, 3...）</li>
 *   <li>选项编号为 A-D（单选/多选题必须）</li>
 *   <li>答案行必须以"答案："或"答案:"开头</li>
 *   <li>解析完成后自动设置题型为单选、难度为中等、分值为10分</li>
 *   <li>解析后的题目可在题库管理中进一步编辑</li>
 * </ul>
 * 
 * @see Question 题目实体
 * @see QuestionService 题目服务
 * @author education-team
 */
@Slf4j
@Service
public class PaperParseService {

    @Autowired
    private QuestionService questionService;

    /**
     * 解析Word试卷
     */
    public RpcResult<List<Question>> parseWordPaper(MultipartFile file, Long gradeId, Long subjectId) {
        try {
            List<Question> questions = new ArrayList<>();
            
            InputStream is = file.getInputStream();
            XWPFDocument document = new XWPFDocument(is);
            
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            Question currentQuestion = null;
            List<QuestionOption> currentOptions = new ArrayList<>();
            
            for (XWPFParagraph para : paragraphs) {
                String text = para.getText().trim();
                if (text.isEmpty()) continue;
                
                // 匹配题目（支持多种格式）
                // 格式1: 1. 题目内容
                // 格式2: 1、题目内容  
                // 格式3: 1) 题目内容
                Pattern questionPattern = Pattern.compile("^(\\d+)[.、)\\s]+(.+)");
                Matcher questionMatcher = questionPattern.matcher(text);
                
                // 匹配选项 A. B. C. D.
                Pattern optionPattern = Pattern.compile("^([A-D])[.、)\\s]+(.+)");
                Matcher optionMatcher = optionPattern.matcher(text);
                
                // 匹配答案
                Pattern answerPattern = Pattern.compile("答案[:：]\\s*([A-D]+)", Pattern.CASE_INSENSITIVE);
                Matcher answerMatcher = answerPattern.matcher(text);
                
                if (questionMatcher.find()) {
                    // 保存上一题
                    if (currentQuestion != null) {
                        currentQuestion.setOptions(currentOptions);
                        questions.add(currentQuestion);
                    }
                    
                    // 创建新题目
                    currentQuestion = new Question();
                    currentQuestion.setTitle(questionMatcher.group(2));
                    currentQuestion.setGradeId(gradeId);
                    currentQuestion.setSubjectId(subjectId);
                    currentQuestion.setQuestionType(1); // 默认单选
                    currentQuestion.setDifficulty(2); // 默认中等
                    currentQuestion.setScore(10);
                    currentOptions = new ArrayList<>();
                    
                } else if (optionMatcher.find() && currentQuestion != null) {
                    // 添加选项
                    QuestionOption option = new QuestionOption();
                    option.setOptionLabel(optionMatcher.group(1));
                    option.setOptionContent(optionMatcher.group(2));
                    currentOptions.add(option);
                    
                } else if (answerMatcher.find() && currentQuestion != null) {
                    // 设置答案
                    String answer = answerMatcher.group(1);
                    currentQuestion.setAnswer(answer);
                    
                    // 标记正确选项
                    for (QuestionOption opt : currentOptions) {
                        if (answer.contains(opt.getOptionLabel())) {
                            opt.setIsCorrect(1);
                        }
                    }
                }
            }
            
            // 保存最后一题
            if (currentQuestion != null) {
                currentQuestion.setOptions(currentOptions);
                questions.add(currentQuestion);
            }
            
            document.close();
            is.close();
            
            // 批量保存题目
            for (Question q : questions) {
                questionService.saveQuestionWithOptions(q);
            }
            
            log.info("试卷解析成功，共解析{}道题目", questions.size());
            return RpcResult.success(questions);
            
        } catch (Exception e) {
            log.error("试卷解析失败：", e);
            return RpcResult.fail("试卷解析失败：" + e.getMessage());
        }
    }

    /**
     * 解析文本格式的试卷
     */
    public RpcResult<List<Question>> parseTextPaper(String content, Long gradeId, Long subjectId) {
        try {
            List<Question> questions = new ArrayList<>();
            String[] lines = content.split("\\n");
            
            Question currentQuestion = null;
            List<QuestionOption> currentOptions = new ArrayList<>();
            
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                // 匹配题目
                Pattern questionPattern = Pattern.compile("^(\\d+)[.、)\\s]+(.+)");
                Matcher questionMatcher = questionPattern.matcher(line);
                
                // 匹配选项
                Pattern optionPattern = Pattern.compile("^([A-D])[.、)\\s]+(.+)");
                Matcher optionMatcher = optionPattern.matcher(line);
                
                // 匹配答案
                Pattern answerPattern = Pattern.compile("答案[:：]\\s*([A-D]+)", Pattern.CASE_INSENSITIVE);
                Matcher answerMatcher = answerPattern.matcher(line);
                
                if (questionMatcher.find()) {
                    if (currentQuestion != null) {
                        currentQuestion.setOptions(currentOptions);
                        questions.add(currentQuestion);
                    }
                    
                    currentQuestion = new Question();
                    currentQuestion.setTitle(questionMatcher.group(2));
                    currentQuestion.setGradeId(gradeId);
                    currentQuestion.setSubjectId(subjectId);
                    currentQuestion.setQuestionType(1);
                    currentQuestion.setDifficulty(2);
                    currentQuestion.setScore(10);
                    currentOptions = new ArrayList<>();
                    
                } else if (optionMatcher.find() && currentQuestion != null) {
                    QuestionOption option = new QuestionOption();
                    option.setOptionLabel(optionMatcher.group(1));
                    option.setOptionContent(optionMatcher.group(2));
                    currentOptions.add(option);
                    
                } else if (answerMatcher.find() && currentQuestion != null) {
                    String answer = answerMatcher.group(1);
                    currentQuestion.setAnswer(answer);
                    
                    for (QuestionOption opt : currentOptions) {
                        if (answer.contains(opt.getOptionLabel())) {
                            opt.setIsCorrect(1);
                        }
                    }
                }
            }
            
            if (currentQuestion != null) {
                currentQuestion.setOptions(currentOptions);
                questions.add(currentQuestion);
            }
            
            // 批量保存
            for (Question q : questions) {
                questionService.saveQuestionWithOptions(q);
            }
            
            return RpcResult.success(questions);
            
        } catch (Exception e) {
            log.error("文本解析失败：", e);
            return RpcResult.fail("解析失败：" + e.getMessage());
        }
    }
}
