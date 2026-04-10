package com.education.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.exam.entity.ExamQuestion;
import com.education.exam.query.QuestionQuery;
import com.education.common.result.PageResult;

/**
 * 题库Service接口
 */
public interface IExamQuestionService extends IService<ExamQuestion> {

    /**
     * 分页查询题库
     */
    PageResult<ExamQuestion> pageQuestions(QuestionQuery query);

    /**
     * 自动设置题目来源
     */
    void autoSetSourceType(ExamQuestion question);
}
