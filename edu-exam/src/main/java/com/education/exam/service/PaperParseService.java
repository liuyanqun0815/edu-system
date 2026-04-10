package com.education.exam.service;

import com.education.common.constants.BusinessConstants;
import com.education.common.result.RpcResult;
import com.education.common.utils.QuestionImageUtil;
import com.education.exam.entity.ExamQuestion;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * 3. 构建 ExamQuestion 对象（选项存储为JSON）
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
 * @see ExamQuestion 题目实体
 * @see IExamQuestionService 题目服务
 * @author education-team
 */
@Slf4j
@Service
public class PaperParseService {

    @Autowired
    private IExamQuestionService examQuestionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 批量保存题目(带事务)
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveQuestionsWithTransaction(List<ExamQuestion> questions) {
        if (questions != null && !questions.isEmpty()) {
            examQuestionService.saveBatch(questions);
        }
    }

    /**
     * 解析Word试卷
     */
    @Transactional(rollbackFor = Exception.class)
    public RpcResult<List<ExamQuestion>> parseWordPaper(MultipartFile file, Long gradeId, Long subjectId) {
        try {
            List<ExamQuestion> questions = new ArrayList<>();
            
            InputStream is = file.getInputStream();
            XWPFDocument document = new XWPFDocument(is);
            
            // 提取文档中的所有图片及其位置信息
            List<XWPFPictureData> allPictures = document.getAllPictures();
            List<PicturePositionInfo> picturePositions = new ArrayList<>();
            
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            ExamQuestion currentQuestion = null;
            List<Map<String, String>> currentOptions = new ArrayList<>();
            List<String> currentImages = new ArrayList<>();
            int currentQuestionStartIndex = 0;
            int questionIndex = 0;
            
            for (int i = 0; i < paragraphs.size(); i++) {
                XWPFParagraph para = paragraphs.get(i);
                String text = para.getText().trim();
                if (text.isEmpty()) continue;
                
                // 匹配题目（支持多种格式）
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
                        currentQuestion.setOptions(objectMapper.writeValueAsString(currentOptions));
                        if (!currentImages.isEmpty()) {
                            currentQuestion.setImages(objectMapper.writeValueAsString(currentImages));
                            currentQuestion.setImageCount(currentImages.size());
                        }
                        questions.add(currentQuestion);
                    }
                    
                    // 创建新题目
                    currentQuestion = new ExamQuestion();
                    currentQuestion.setQuestionTitle(questionMatcher.group(2));
                    currentQuestion.setGrade(gradeId != null ? String.valueOf(gradeId) : null);
                    currentQuestion.setSubjectId(subjectId);
                    currentQuestion.setExamType(1); // 默认单元测试
                    currentQuestion.setQuestionType(1); // 默认单选
                    currentQuestion.setDifficulty(2); // 默认中等
                    currentQuestion.setScore(BusinessConstants.DEFAULT_QUESTION_SCORE);
                    currentQuestion.setStatus(1);
                    currentOptions = new ArrayList<>();
                    currentImages = new ArrayList<>();
                    currentQuestionStartIndex = i;
                    questionIndex++;
                    
                } else if (optionMatcher.find() && currentQuestion != null) {
                    // 添加选项
                    Map<String, String> option = new HashMap<>();
                    option.put("label", optionMatcher.group(1));
                    option.put("content", optionMatcher.group(2));
                    currentOptions.add(option);
                    
                } else if (answerMatcher.find() && currentQuestion != null) {
                    // 设置答案
                    String answer = answerMatcher.group(1);
                    currentQuestion.setCorrectAnswer(answer);
                }
            }
            
            // 保存最后一题
            if (currentQuestion != null) {
                currentQuestion.setOptions(objectMapper.writeValueAsString(currentOptions));
                if (!currentImages.isEmpty()) {
                    currentQuestion.setImages(objectMapper.writeValueAsString(currentImages));
                    currentQuestion.setImageCount(currentImages.size());
                }
                questions.add(currentQuestion);
            }
            
            // 智能匹配图片到题目
            if (!allPictures.isEmpty() && !questions.isEmpty()) {
                // 策略: 根据图片数量和题目数量进行分配
                List<String> imageUrls = QuestionImageUtil.extractAndSaveImages(
                    allPictures, 
                    questions.get(0).getId() != null ? questions.get(0).getId() : 0L
                );
                
                if (!imageUrls.isEmpty()) {
                    if (imageUrls.size() <= questions.size()) {
                        // 图片少于题目: 按顺序分配
                        for (int i = 0; i < imageUrls.size(); i++) {
                            ExamQuestion question = questions.get(i);
                            List<String> questionImages = new ArrayList<>();
                            questionImages.add(imageUrls.get(i));
                            question.setImages(objectMapper.writeValueAsString(questionImages));
                            question.setImageCount(1);
                        }
                    } else {
                        // 图片多于题目: 平均分配
                        int imagesPerQuestion = imageUrls.size() / questions.size();
                        int remainingImages = imageUrls.size() % questions.size();
                        int imageIndex = 0;
                        
                        for (int i = 0; i < questions.size(); i++) {
                            ExamQuestion question = questions.get(i);
                            List<String> questionImages = new ArrayList<>();
                            int count = imagesPerQuestion + (i < remainingImages ? 1 : 0);
                            
                            for (int j = 0; j < count && imageIndex < imageUrls.size(); j++) {
                                questionImages.add(imageUrls.get(imageIndex++));
                            }
                            
                            question.setImages(objectMapper.writeValueAsString(questionImages));
                            question.setImageCount(questionImages.size());
                        }
                    }
                    
                    log.info("智能匹配图片: {}张图片分配到{}道题目", imageUrls.size(), questions.size());
                }
            }
            
            document.close();
            is.close();
            
            // 批量保存题目到exam_question表
            saveQuestionsWithTransaction(questions);
            
            log.info("试卷解析成功，共解析{}道题目", questions.size());
            return RpcResult.success(questions);
            
        } catch (Exception e) {
            log.error("试卷解析失败：", e);
            return RpcResult.fail("试卷解析失败：" + e.getMessage());
        }
    }

    /**
     * 图片位置信息
     */
    private static class PicturePositionInfo {
        int paragraphIndex;
        XWPFPictureData pictureData;
        
        public PicturePositionInfo(int paragraphIndex, XWPFPictureData pictureData) {
            this.paragraphIndex = paragraphIndex;
            this.pictureData = pictureData;
        }
    }

    /**
     * 解析文本格式的试卷
     */
    @Transactional(rollbackFor = Exception.class)
    public RpcResult<List<ExamQuestion>> parseTextPaper(String content, Long gradeId, Long subjectId) {
        try {
            List<ExamQuestion> questions = new ArrayList<>();
            String[] lines = content.split("\\n");
            
            ExamQuestion currentQuestion = null;
            List<Map<String, String>> currentOptions = new ArrayList<>();
            
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
                        currentQuestion.setOptions(objectMapper.writeValueAsString(currentOptions));
                        questions.add(currentQuestion);
                    }
                    
                    currentQuestion = new ExamQuestion();
                    currentQuestion.setQuestionTitle(questionMatcher.group(2));
                    currentQuestion.setGrade(gradeId != null ? String.valueOf(gradeId) : null);
                    currentQuestion.setSubjectId(subjectId);
                    currentQuestion.setExamType(1); // 默认单元测试
                    currentQuestion.setQuestionType(1);
                    currentQuestion.setDifficulty(2);
                    currentQuestion.setScore(BusinessConstants.DEFAULT_QUESTION_SCORE);
                    currentQuestion.setStatus(1);
                    currentOptions = new ArrayList<>();
                    
                } else if (optionMatcher.find() && currentQuestion != null) {
                    Map<String, String> option = new HashMap<>();
                    option.put("label", optionMatcher.group(1));
                    option.put("content", optionMatcher.group(2));
                    currentOptions.add(option);
                    
                } else if (answerMatcher.find() && currentQuestion != null) {
                    String answer = answerMatcher.group(1);
                    currentQuestion.setCorrectAnswer(answer);
                }
            }
            
            if (currentQuestion != null) {
                currentQuestion.setOptions(objectMapper.writeValueAsString(currentOptions));
                questions.add(currentQuestion);
            }
            
            // 批量保存题目到exam_question表
            saveQuestionsWithTransaction(questions);
            
            log.info("文本解析成功，共解析{}道题目", questions.size());
            return RpcResult.success(questions);
            
        } catch (Exception e) {
            log.error("文本解析失败：", e);
            return RpcResult.fail("解析失败：" + e.getMessage());
        }
    }
}
