package com.education.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.education.common.result.RpcResult;
import com.education.common.utils.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.education.system.dto.*;
import com.education.system.entity.SysWord;
import com.education.system.entity.WordTrainingRecord;
import com.education.system.entity.WordTrainingSession;
import com.education.system.entity.WordWrongBook;
import com.education.system.mapper.SysWordMapper;
import com.education.system.mapper.WordTrainingRecordMapper;
import com.education.system.mapper.WordTrainingSessionMapper;
import com.education.system.mapper.WordWrongBookMapper;
import com.education.system.service.IWordTrainingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 单词训练服务实现
 */
@Slf4j
@Service
public class WordTrainingServiceImpl implements IWordTrainingService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WordTrainingSessionMapper sessionMapper;

    @Autowired
    private WordTrainingRecordMapper recordMapper;

    @Autowired
    private SysWordMapper wordMapper;

    @Autowired
    private WordWrongBookMapper wrongBookMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RpcResult<TrainingSessionVO> startTraining(TrainingConfigDTO config) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            
            // 创建训练会话
            WordTrainingSession session = new WordTrainingSession();
            session.setUserId(userId);
            session.setGrade(config.getGrade());
            session.setDifficulty(config.getDifficulty());
            session.setQuestionType(config.getQuestionType());
            session.setQuestionCount(config.getQuestionCount());
            session.setPerQuestionTime(config.getPerQuestionTime());
            session.setStatus(0); // 进行中
            session.setStartTime(new Date());
            session.setTotalScore(0);
            session.setCorrectCount(0);
            session.setWrongCount(0);
            session.setSkipCount(0);
            session.setDr(0);
            sessionMapper.insert(session);

            log.info("用户{}开始单词训练,会话ID:{}, 年级:{}, 难度:{}, 类型:{}, 数量:{}", 
                    userId, session.getId(), config.getGrade(), config.getDifficulty(), 
                    config.getQuestionType(), config.getQuestionCount());

            // 构建返回VO
            TrainingSessionVO vo = new TrainingSessionVO();
            vo.setSessionId(session.getId());
            vo.setGrade(session.getGrade());
            vo.setDifficulty(session.getDifficulty());
            vo.setQuestionType(session.getQuestionType());
            vo.setQuestionCount(session.getQuestionCount());
            vo.setCurrentQuestionIndex(0);
            vo.setPerQuestionTime(session.getPerQuestionTime());
            vo.setStatus(session.getStatus());
            vo.setStartTime(session.getStartTime());
            vo.setAnsweredCount(0);
            vo.setCorrectCount(0);

            return RpcResult.success(vo);
        } catch (Exception e) {
            log.error("开始训练失败:", e);
            return RpcResult.fail("开始训练失败:" + e.getMessage());
        }
    }

    @Override
    public RpcResult<WordQuestionVO> getNextQuestion(Long sessionId) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            
            // 查询会话
            WordTrainingSession session = sessionMapper.selectById(sessionId);
            if (session == null || session.getDr() == 1) {
                return RpcResult.fail("会话不存在");
            }
            
            if (!session.getUserId().equals(userId)) {
                return RpcResult.fail("无权访问该会话");
            }
            
            if (session.getStatus() != 0) {
                return RpcResult.fail("会话已结束");
            }

            // 查询已答题数
            LambdaQueryWrapper<WordTrainingRecord> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.eq(WordTrainingRecord::getSessionId, sessionId);
            Integer answeredCount = recordMapper.selectCount(countWrapper).intValue();

            // 检查是否已完成所有题目
            if (answeredCount >= session.getQuestionCount()) {
                return RpcResult.fail("已完成所有题目,请提交训练");
            }

            // AI智能推荐题目
            WordQuestionVO question = recommendQuestion(session, answeredCount.intValue());
            if (question == null) {
                return RpcResult.fail("暂无可推荐的题目");
            }

            question.setQuestionIndex(answeredCount.intValue() + 1);
            question.setTimeLimit(session.getPerQuestionTime());

            log.info("会话{}获取第{}题,单词ID:{}", sessionId, question.getQuestionIndex(), question.getWordId());
            return RpcResult.success(question);
        } catch (Exception e) {
            log.error("获取下一题失败:", e);
            return RpcResult.fail("获取题目失败:" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RpcResult<Boolean> submitAnswer(SubmitAnswerDTO answerDTO) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            
            // 验证会话
            WordTrainingSession session = sessionMapper.selectById(answerDTO.getSessionId());
            if (session == null || session.getDr() == 1) {
                return RpcResult.fail("会话不存在");
            }

            if (!session.getUserId().equals(userId)) {
                return RpcResult.fail("无权操作该会话");
            }

            // 查询单词
            SysWord word = wordMapper.selectById(answerDTO.getWordId());
            if (word == null) {
                return RpcResult.fail("单词不存在");
            }

            // 判断答案是否正确
            boolean isCorrect = false;
            if (!answerDTO.getSkipped()) {
                isCorrect = checkAnswer(session.getQuestionType(), word, answerDTO.getUserAnswer());
            }

            // 保存答题记录
            WordTrainingRecord record = new WordTrainingRecord();
            record.setSessionId(answerDTO.getSessionId());
            record.setUserId(userId);
            record.setWordId(answerDTO.getWordId());
            record.setQuestionType(session.getQuestionType());
            record.setCorrectAnswer(getCorrectAnswer(session.getQuestionType(), word));
            record.setUserAnswer(answerDTO.getSkipped() ? null : answerDTO.getUserAnswer());
            record.setIsCorrect(answerDTO.getSkipped() ? null : (isCorrect ? 1 : 0));
            record.setIsSkipped(answerDTO.getSkipped() ? 1 : 0);
            record.setAnswerTime(answerDTO.getAnswerTime());
            record.setTimeout(answerDTO.getTimeout() ? 1 : 0);
            record.setSortOrder(answerDTO.getAnswerTime()); // 临时使用,实际应该单独计算
            recordMapper.insert(record);

            // 如果答错,加入错题本
            if (!isCorrect && !answerDTO.getSkipped()) {
                addToWrongBook(userId, word.getId(), session.getQuestionType());
                
                // 更新单词错误次数
                word.setWrongCount(word.getWrongCount() == null ? 1 : word.getWrongCount() + 1);
                wordMapper.updateById(word);
            }

            log.info("用户{}提交答案,单词:{}, 是否正确:{}", userId, word.getWord(), isCorrect);
            return RpcResult.success(isCorrect);
        } catch (Exception e) {
            log.error("提交答案失败:", e);
            return RpcResult.fail("提交答案失败:" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RpcResult<TrainingResultVO> submitTraining(Long sessionId) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            
            WordTrainingSession session = sessionMapper.selectById(sessionId);
            if (session == null || session.getDr() == 1) {
                return RpcResult.fail("会话不存在");
            }

            if (!session.getUserId().equals(userId)) {
                return RpcResult.fail("无权操作该会话");
            }

            if (session.getStatus() != 0) {
                return RpcResult.fail("会话已结束");
            }

            // 查询答题记录
            LambdaQueryWrapper<WordTrainingRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WordTrainingRecord::getSessionId, sessionId);
            List<WordTrainingRecord> records = recordMapper.selectList(wrapper);

            // 统计成绩
            int correctCount = 0;
            int wrongCount = 0;
            int skipCount = 0;
            
            for (WordTrainingRecord record : records) {
                if (record.getIsSkipped() == 1) {
                    skipCount++;
                } else if (record.getIsCorrect() != null && record.getIsCorrect() == 1) {
                    correctCount++;
                } else {
                    wrongCount++;
                }
            }

            // 更新会话
            session.setStatus(1); // 已完成
            session.setEndTime(new Date());
            session.setCorrectCount(correctCount);
            session.setWrongCount(wrongCount);
            session.setSkipCount(skipCount);
            session.setTotalScore(correctCount * 10); // 每题10分
            session.setActualDuration((int) ((session.getEndTime().getTime() - session.getStartTime().getTime()) / 1000));
            sessionMapper.updateById(session);

            // 构建结果VO
            TrainingResultVO resultVO = new TrainingResultVO();
            resultVO.setSessionId(sessionId);
            resultVO.setQuestionCount(session.getQuestionCount());
            resultVO.setCorrectCount(correctCount);
            resultVO.setWrongCount(wrongCount);
            resultVO.setSkipCount(skipCount);
            resultVO.setAccuracy(session.getQuestionCount() > 0 ? 
                    (correctCount * 100.0 / session.getQuestionCount()) : 0.0);
            resultVO.setTotalScore(session.getTotalScore());
            resultVO.setActualDuration(session.getActualDuration());
            resultVO.setEndTime(session.getEndTime());
            resultVO.setAddedToWrongBook(wrongCount > 0);

            log.info("用户{}提交训练,会话ID:{}, 正确:{}, 错误:{}, 得分:{}", 
                    userId, sessionId, correctCount, wrongCount, session.getTotalScore());
            return RpcResult.success(resultVO);
        } catch (Exception e) {
            log.error("提交训练失败:", e);
            return RpcResult.fail("提交训练失败:" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RpcResult<Void> abandonTraining(Long sessionId) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            
            WordTrainingSession session = sessionMapper.selectById(sessionId);
            if (session == null || session.getDr() == 1) {
                return RpcResult.fail("会话不存在");
            }

            if (!session.getUserId().equals(userId)) {
                return RpcResult.fail("无权操作该会话");
            }

            session.setStatus(2); // 已放弃
            session.setEndTime(new Date());
            sessionMapper.updateById(session);

            log.info("用户{}放弃训练,会话ID:{}", userId, sessionId);
            return RpcResult.success(null);
        } catch (Exception e) {
            log.error("放弃训练失败:", e);
            return RpcResult.fail("放弃训练失败:" + e.getMessage());
        }
    }

    @Override
    public RpcResult<TrainingSessionVO> getSessionDetail(Long sessionId) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            
            WordTrainingSession session = sessionMapper.selectById(sessionId);
            if (session == null || session.getDr() == 1) {
                return RpcResult.fail("会话不存在");
            }

            if (!session.getUserId().equals(userId)) {
                return RpcResult.fail("无权访问该会话");
            }

            // 查询已答题数
            LambdaQueryWrapper<WordTrainingRecord> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.eq(WordTrainingRecord::getSessionId, sessionId);
            Integer answeredCount = recordMapper.selectCount(countWrapper).intValue();

            TrainingSessionVO vo = new TrainingSessionVO();
            vo.setSessionId(session.getId());
            vo.setGrade(session.getGrade());
            vo.setDifficulty(session.getDifficulty());
            vo.setQuestionType(session.getQuestionType());
            vo.setQuestionCount(session.getQuestionCount());
            vo.setCurrentQuestionIndex(answeredCount.intValue());
            vo.setPerQuestionTime(session.getPerQuestionTime());
            vo.setStatus(session.getStatus());
            vo.setStartTime(session.getStartTime());
            vo.setAnsweredCount(answeredCount.intValue());
            vo.setCorrectCount(session.getCorrectCount());

            return RpcResult.success(vo);
        } catch (Exception e) {
            log.error("获取会话详情失败:", e);
            return RpcResult.fail("获取会话详情失败:" + e.getMessage());
        }
    }

    /**
     * AI智能推荐题目
     */
    private WordQuestionVO recommendQuestion(WordTrainingSession session, int currentIndex) {
        // 1. 基础查询条件: 年级 + 难度
        LambdaQueryWrapper<SysWord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysWord::getGrade, session.getGrade())
               .eq(SysWord::getDifficulty, session.getDifficulty());

        // 2. 如果指定了题目类型,过滤
        if (session.getQuestionType() != null && !session.getQuestionType().isEmpty()) {
            wrapper.apply("FIND_IN_SET({0}, question_type) > 0", session.getQuestionType());
        }

        // 3. 查询所有符合条件的单词
        List<SysWord> words = wordMapper.selectList(wrapper);
        if (words.isEmpty()) {
            return null;
        }

        // 4. 错题优先策略: 按错误次数降序
        words.sort((w1, w2) -> {
            int count1 = w1.getWrongCount() == null ? 0 : w1.getWrongCount();
            int count2 = w2.getWrongCount() == null ? 0 : w2.getWrongCount();
            return count2 - count1; // 降序
        });

        // 5. 选择当前题目(循环使用)
        SysWord word = words.get(currentIndex % words.size());

        // 6. 构建题目VO
        WordQuestionVO questionVO = buildQuestionVO(session.getQuestionType(), word, currentIndex + 1, words);
        return questionVO;
    }

    /**
     * 构建题目VO
     */
    private WordQuestionVO buildQuestionVO(String questionType, SysWord word, int index, List<SysWord> allWords) {
        WordQuestionVO vo = new WordQuestionVO();
        vo.setWordId(word.getId());
        vo.setQuestionType(questionType);
        vo.setWord(word.getWord());
        vo.setPhonetic(word.getPhonetic());
        vo.setTranslation(word.getTranslation());
        vo.setExample(word.getExample());

        switch (questionType) {
            case "spell":
                // 拼写题: 给出中文,填写英文
                vo.setQuestionContent("请根据中文释义拼写单词: " + word.getTranslation());
                vo.setCorrectAnswer(word.getWord());
                break;

            case "choice_select":
                // 看词选义: 给出英文,选择中文
                vo.setQuestionContent("请选择单词 \"" + word.getWord() + "\" 的中文释义:");
                vo.setOptions(generateOptions(word, allWords, "translation"));
                vo.setCorrectAnswer(word.getTranslation());
                break;

            case "choice_word":
                // 看义选词: 给出中文,选择英文
                vo.setQuestionContent("请选择中文 \"" + word.getTranslation() + "\" 对应的英文单词:");
                vo.setOptions(generateOptions(word, allWords, "word"));
                vo.setCorrectAnswer(word.getWord());
                break;

            case "translate":
                // 翻译题: 给出英文,填写中文
                vo.setQuestionContent("请翻译单词: " + word.getWord());
                vo.setCorrectAnswer(word.getTranslation());
                break;

            case "sentence_fill":
                // 例句填空: 给出例句,填写单词
                if (word.getExample() != null && !word.getExample().isEmpty()) {
                    String question = word.getExample().replaceAll("(?i)\\b" + word.getWord() + "\\b", "______");
                    vo.setQuestionContent("请填写例句中的空缺单词: " + question);
                } else {
                    vo.setQuestionContent("请根据中文释义拼写单词: " + word.getTranslation());
                }
                vo.setCorrectAnswer(word.getWord());
                break;

            default:
                vo.setQuestionContent(word.getWord());
                vo.setCorrectAnswer(word.getWord());
        }

        return vo;
    }

    /**
     * 生成选择题选项
     */
    private String generateOptions(SysWord correctWord, List<SysWord> allWords, String field) {
        List<Map<String, String>> options = new ArrayList<>();
        List<String> labels = Arrays.asList("A", "B", "C", "D");
        
        // 添加正确答案
        Map<String, String> correctOption = new HashMap<>();
        correctOption.put("label", labels.get(0));
        correctOption.put("content", getFieldValue(correctWord, field));
        options.add(correctOption);

        // 随机添加3个干扰项
        List<SysWord> shuffled = new ArrayList<>(allWords);
        Collections.shuffle(shuffled);
        
        int optionIndex = 1;
        for (SysWord word : shuffled) {
            if (optionIndex >= 4) break;
            if (word.getId().equals(correctWord.getId())) continue;
            
            Map<String, String> option = new HashMap<>();
            option.put("label", labels.get(optionIndex));
            option.put("content", getFieldValue(word, field));
            options.add(option);
            optionIndex++;
        }

        // 打乱选项顺序
        Collections.shuffle(options);
        try {
            return objectMapper.writeValueAsString(options);
        } catch (Exception e) {
            log.error("序列化选项失败", e);
            return "[]";
        }
    }

    /**
     * 获取字段值
     */
    private String getFieldValue(SysWord word, String field) {
        switch (field) {
            case "word":
                return word.getWord();
            case "translation":
                return word.getTranslation();
            default:
                return "";
        }
    }

    /**
     * 检查答案
     */
    private boolean checkAnswer(String questionType, SysWord word, String userAnswer) {
        if (userAnswer == null || userAnswer.trim().isEmpty()) {
            return false;
        }

        String correctAnswer = getCorrectAnswer(questionType, word);
        
        // 忽略大小写和前后空格
        return userAnswer.trim().equalsIgnoreCase(correctAnswer.trim());
    }

    /**
     * 获取正确答案
     */
    private String getCorrectAnswer(String questionType, SysWord word) {
        switch (questionType) {
            case "spell":
            case "sentence_fill":
                return word.getWord();
            case "choice_select":
            case "translate":
                return word.getTranslation();
            case "choice_word":
                return word.getWord();
            default:
                return word.getWord();
        }
    }

    /**
     * 加入错题本
     */
    private void addToWrongBook(Long userId, Long wordId, String questionType) {
        LambdaQueryWrapper<WordWrongBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WordWrongBook::getUserId, userId)
               .eq(WordWrongBook::getWordId, wordId)
               .eq(WordWrongBook::getQuestionType, questionType)
               .eq(WordWrongBook::getDr, 0);
        
        WordWrongBook wrongBook = wrongBookMapper.selectOne(wrapper);
        
        if (wrongBook == null) {
            // 新增错题
            wrongBook = new WordWrongBook();
            wrongBook.setUserId(userId);
            wrongBook.setWordId(wordId);
            wrongBook.setQuestionType(questionType);
            wrongBook.setWrongCount(1);
            wrongBook.setLastWrongTime(new Date());
            wrongBook.setReviewStatus(0);
            wrongBook.setMasteryLevel(0);
            wrongBook.setDr(0);
            wrongBookMapper.insert(wrongBook);
        } else {
            // 更新错题
            wrongBook.setWrongCount(wrongBook.getWrongCount() + 1);
            wrongBook.setLastWrongTime(new Date());
            wrongBook.setReviewStatus(0);
            wrongBookMapper.updateById(wrongBook);
        }
    }
}
