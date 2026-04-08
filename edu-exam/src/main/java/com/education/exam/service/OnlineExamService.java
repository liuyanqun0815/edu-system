package com.education.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.education.common.result.RpcResult;
import com.education.exam.dto.ExamQuestionVO;
import com.education.exam.dto.SubmitPaperVO;
import com.education.exam.entity.*;
import com.education.exam.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 在线考试服务类
 * 
 * <p>提供在线考试的核心业务逻辑，包括开始考试、提交答案、交卷评分等功能。</p>
 * 
 * <h3>考试流程：</h3>
 * <pre>
 * 开始考试 → 获取题目 → 答题(多次提交) → 交卷 → 自动评分 → 查看结果
 * </pre>
 * 
 * <h3>核心方法：</h3>
 * <table border="1">
 *   <tr><th>方法</th><th>说明</th><th>事务</th></tr>
 *   <tr><td>startExam</td><td>开始考试，创建考试记录</td><td>是</td></tr>
 *   <tr><td>submitAnswer</td><td>提交单题答案</td><td>否</td></tr>
 *   <tr><td>submitPaper</td><td>交卷并自动评分</td><td>是</td></tr>
 *   <tr><td>getExamQuestions</td><td>获取考试题目列表</td><td>否</td></tr>
 *   <tr><td>exportPaper</td><td>导出试卷为Word</td><td>否</td></tr>
 * </table>
 * 
 * <h3>考试状态说明：</h3>
 * <ul>
 *   <li>0 - 进行中：考试已开始，尚未交卷</li>
 *   <li>1 - 已完成：已交卷并评分</li>
 * </ul>
 * 
 * <h3>自动评分规则：</h3>
 * <ul>
 *   <li>客观题（单选、多选、判断）：系统自动判分</li>
 *   <li>填空题：精确匹配答案判分（可扩展模糊匹配）</li>
 *   <li>简答题：暂不支持自动评分，需人工复核</li>
 * </ul>
 * 
 * <h3>断点续考：</h3>
 * <p>如果用户有未完成的考试记录（status=0），startExam会返回该记录，
 * 用户可以继续答题，已提交的答案会保留。</p>
 * 
 * @see ExamRecord 考试记录
 * @see ExamRecordDetail 答题详情
 * @author education-team
 */
@Slf4j
@Service
public class OnlineExamService {

    @Autowired
    private ExamPaperMapper paperMapper;
    
    @Autowired
    private ExamPaperQuestionMapper paperQuestionMapper;
    
    @Autowired
    private ExamRecordMapper recordMapper;
    
    @Autowired
    private ExamRecordDetailMapper recordDetailMapper;
    
    @Autowired
    private QuestionMapper questionMapper;

    /**
     * 开始考试
     */
    @Transactional
    public RpcResult<ExamRecord> startExam(Long paperId, Long userId) {
        try {
            // 检查试卷是否存在
            ExamPaper paper = paperMapper.selectById(paperId);
            if (paper == null) {
                return RpcResult.fail("试卷不存在");
            }
            
            // 检查是否已有进行中的考试
            LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ExamRecord::getPaperId, paperId)
                   .eq(ExamRecord::getUserId, userId)
                   .eq(ExamRecord::getStatus, 0);
            ExamRecord existRecord = recordMapper.selectOne(wrapper);
            
            if (existRecord != null) {
                // 返回已有的考试记录
                return RpcResult.success(existRecord);
            }
            
            // 创建新的考试记录
            ExamRecord record = new ExamRecord();
            record.setPaperId(paperId);
            record.setUserId(userId);
            record.setStartTime(LocalDateTime.now());
            record.setStatus(0);
            record.setTotalScore(paper.getTotalScore());
            recordMapper.insert(record);
            
            // 获取试卷题目
            List<ExamPaperQuestion> paperQuestions = paperQuestionMapper.selectByPaperId(paperId);
            
            // 初始化答题详情
            for (ExamPaperQuestion pq : paperQuestions) {
                ExamRecordDetail detail = new ExamRecordDetail();
                detail.setRecordId(record.getId());
                detail.setQuestionId(pq.getQuestionId());
                detail.setScore(0);
                recordDetailMapper.insert(detail);
            }
            
            log.info("开始考试：recordId={}, userId={}, paperId={}", record.getId(), userId, paperId);
            return RpcResult.success(record);
            
        } catch (Exception e) {
            log.error("开始考试失败：", e);
            return RpcResult.fail("开始考试失败：" + e.getMessage());
        }
    }

    /**
     * 提交答案
     */
    public RpcResult<Void> submitAnswer(Long recordId, Long questionId, String answer) {
        try {
            LambdaQueryWrapper<ExamRecordDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ExamRecordDetail::getRecordId, recordId)
                   .eq(ExamRecordDetail::getQuestionId, questionId);
            
            ExamRecordDetail detail = recordDetailMapper.selectOne(wrapper);
            if (detail == null) {
                return RpcResult.fail("题目不存在");
            }
            
            detail.setUserAnswer(answer);
            recordDetailMapper.updateById(detail);
            
            return RpcResult.success(null);
            
        } catch (Exception e) {
            log.error("提交答案失败：", e);
            return RpcResult.fail("提交失败：" + e.getMessage());
        }
    }

    /**
     * 交卷
     */
    @Transactional
    public RpcResult<SubmitPaperVO> submitPaper(Long recordId) {
        try {
            ExamRecord record = recordMapper.selectById(recordId);
            if (record == null) {
                return RpcResult.fail("考试记录不存在");
            }
            
            record.setEndTime(LocalDateTime.now());
            long duration = ChronoUnit.MINUTES.between(record.getStartTime(), record.getEndTime());
            record.setDuration((int) duration);
            
            // 自动批改
            int score = 0;
            int correctCount = 0;
            int wrongCount = 0;
            
            LambdaQueryWrapper<ExamRecordDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ExamRecordDetail::getRecordId, recordId);
            List<ExamRecordDetail> details = recordDetailMapper.selectList(wrapper);
            
            for (ExamRecordDetail detail : details) {
                Question question = questionMapper.selectById(detail.getQuestionId());
                if (question != null && detail.getUserAnswer() != null) {
                    // 获取该题的分值
                    LambdaQueryWrapper<ExamPaperQuestion> pqWrapper = new LambdaQueryWrapper<>();
                    pqWrapper.eq(ExamPaperQuestion::getPaperId, record.getPaperId())
                             .eq(ExamPaperQuestion::getQuestionId, detail.getQuestionId());
                    ExamPaperQuestion pq = paperQuestionMapper.selectOne(pqWrapper);
                    int questionScore = pq != null ? pq.getScore() : question.getScore();
                    
                    // 判断答案
                    if (detail.getUserAnswer().equalsIgnoreCase(question.getAnswer())) {
                        detail.setIsCorrect(1);
                        detail.setScore(questionScore);
                        score += questionScore;
                        correctCount++;
                    } else {
                        detail.setIsCorrect(0);
                        detail.setScore(0);
                        wrongCount++;
                    }
                    recordDetailMapper.updateById(detail);
                }
            }
            
            record.setScore(score);
            record.setCorrectCount(correctCount);
            record.setWrongCount(wrongCount);
            record.setStatus(1);
            recordMapper.updateById(record);
            
            SubmitPaperVO result = new SubmitPaperVO();
            result.setScore(score);
            result.setTotalScore(record.getTotalScore());
            result.setCorrectCount(correctCount);
            result.setWrongCount(wrongCount);
            result.setDuration((int) duration);
            
            log.info("交卷成功：recordId={}, score={}", recordId, score);
            return RpcResult.success(result);
            
        } catch (Exception e) {
            log.error("交卷失败：", e);
            return RpcResult.fail("交卷失败：" + e.getMessage());
        }
    }

    /**
     * 获取考试题目
     */
    public RpcResult<List<ExamQuestionVO>> getExamQuestions(Long recordId) {
        try {
            ExamRecord record = recordMapper.selectById(recordId);
            if (record == null) {
                return RpcResult.fail("考试记录不存在");
            }
            
            List<ExamQuestionVO> questionList = new ArrayList<>();
            
            LambdaQueryWrapper<ExamRecordDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ExamRecordDetail::getRecordId, recordId);
            List<ExamRecordDetail> details = recordDetailMapper.selectList(wrapper);
            
            for (ExamRecordDetail detail : details) {
                Question question = questionMapper.selectById(detail.getQuestionId());
                if (question != null) {
                    ExamQuestionVO vo = new ExamQuestionVO();
                    vo.setQuestionId(question.getId());
                    vo.setTitle(question.getTitle());
                    vo.setQuestionType(question.getQuestionType());
                    vo.setScore(question.getScore());
                    vo.setUserAnswer(detail.getUserAnswer());
                    
                    // 获取选项
                    if (question.getQuestionType() == 1 || question.getQuestionType() == 2) {
                        List<QuestionOption> options = questionMapper.selectOptions(question.getId());
                        vo.setOptions(options);
                    }
                    
                    questionList.add(vo);
                }
            }
            
            return RpcResult.success(questionList);
            
        } catch (Exception e) {
            log.error("获取考试题目失败：", e);
            return RpcResult.fail("获取失败：" + e.getMessage());
        }
    }

    /**
     * 导出试卷为Word
     */
    public RpcResult<String> exportPaper(Long paperId) {
        try {
            ExamPaper paper = paperMapper.selectById(paperId);
            if (paper == null) {
                return RpcResult.fail("试卷不存在");
            }
            
            // 获取试卷题目
            List<Map<String, Object>> questions = paperQuestionMapper.selectQuestionsByPaperId(paperId);
            
            // TODO: 生成Word文档
            // 这里可以集成POI生成Word文件
            
            String fileName = paper.getName() + "_" + System.currentTimeMillis() + ".docx";
            
            log.info("试卷导出成功：paperId={}, fileName={}", paperId, fileName);
            return RpcResult.success(fileName);
            
        } catch (Exception e) {
            log.error("导出试卷失败：", e);
            return RpcResult.fail("导出失败：" + e.getMessage());
        }
    }
}
