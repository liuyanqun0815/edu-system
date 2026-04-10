package com.education.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.common.result.PageResult;
import com.education.exam.entity.ExamQuestion;
import com.education.exam.mapper.ExamQuestionMapper;
import com.education.exam.query.QuestionQuery;
import com.education.exam.service.IExamQuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 题库Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExamQuestionServiceImpl extends ServiceImpl<ExamQuestionMapper, ExamQuestion> implements IExamQuestionService {

    @Override
    public PageResult<ExamQuestion> pageQuestions(QuestionQuery query) {
        Page<ExamQuestion> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<ExamQuestion> wrapper = new LambdaQueryWrapper<>();
        
        // 条件查询
        if (query.getSubjectId() != null) {
            wrapper.eq(ExamQuestion::getSubjectId, query.getSubjectId());
        }
        if (query.getQuestionType() != null) {
            wrapper.eq(ExamQuestion::getQuestionType, query.getQuestionType());
        }
        if (query.getExamType() != null) {
            wrapper.eq(ExamQuestion::getExamType, query.getExamType());
        }
        if (query.getDifficulty() != null) {
            wrapper.eq(ExamQuestion::getDifficulty, query.getDifficulty());
        }
        if (query.getSourceType() != null) {
            wrapper.eq(ExamQuestion::getSourceType, query.getSourceType());
        }
        if (query.getTags() != null && !query.getTags().isEmpty()) {
            wrapper.like(ExamQuestion::getTags, query.getTags());
        }
        
        wrapper.orderByDesc(ExamQuestion::getCreateTime);
        
        IPage<ExamQuestion> result = page(page, wrapper);
        return PageResult.of(result);
    }

    @Override
    public void autoSetSourceType(ExamQuestion question) {
        if (question.getSourceType() == null) {
            question.setSourceType(1); // 1-手动录入
            log.debug("题目来源自动设置为: 手动录入");
        }
    }
}
