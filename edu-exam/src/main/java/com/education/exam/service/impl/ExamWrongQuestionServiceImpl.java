package com.education.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.exam.entity.ExamWrongQuestion;
import com.education.exam.mapper.ExamWrongQuestionMapper;
import com.education.exam.service.IExamWrongQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 错题本Service实现类
 *
 * @author system
 * @since 2026-04-09
 */
@Service
@RequiredArgsConstructor
public class ExamWrongQuestionServiceImpl extends ServiceImpl<ExamWrongQuestionMapper, ExamWrongQuestion>
        implements IExamWrongQuestionService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addWrongQuestion(Long studentId, Long questionId, Long paperId) {
        LambdaQueryWrapper<ExamWrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamWrongQuestion::getStudentId, studentId)
               .eq(ExamWrongQuestion::getQuestionId, questionId);

        ExamWrongQuestion wrongQuestion = getOne(wrapper);

        if (wrongQuestion == null) {
            // 首次答错,创建错题记录
            wrongQuestion = new ExamWrongQuestion();
            wrongQuestion.setStudentId(studentId);
            wrongQuestion.setQuestionId(questionId);
            wrongQuestion.setPaperId(paperId);
            wrongQuestion.setWrongCount(1);
            wrongQuestion.setLastWrongTime(new Date());
            wrongQuestion.setIsMastered(0);
            save(wrongQuestion);
        } else {
            // 重复答错,增加错误次数
            wrongQuestion.setWrongCount(wrongQuestion.getWrongCount() + 1);
            wrongQuestion.setLastWrongTime(new Date());
            wrongQuestion.setPaperId(paperId);
            updateById(wrongQuestion);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsMastered(Long studentId, Long questionId) {
        LambdaQueryWrapper<ExamWrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamWrongQuestion::getStudentId, studentId)
               .eq(ExamWrongQuestion::getQuestionId, questionId);

        ExamWrongQuestion wrongQuestion = getOne(wrapper);
        if (wrongQuestion != null) {
            wrongQuestion.setIsMastered(1);
            updateById(wrongQuestion);
        }
    }

    @Override
    public List<ExamWrongQuestion> getWrongQuestions(Long studentId, Integer isMastered) {
        LambdaQueryWrapper<ExamWrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamWrongQuestion::getStudentId, studentId);

        if (isMastered != null) {
            wrapper.eq(ExamWrongQuestion::getIsMastered, isMastered);
        }

        wrapper.orderByDesc(ExamWrongQuestion::getLastWrongTime);
        return list(wrapper);
    }
}
