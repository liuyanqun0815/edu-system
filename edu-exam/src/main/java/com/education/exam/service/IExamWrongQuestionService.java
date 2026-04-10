package com.education.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.exam.entity.ExamWrongQuestion;

import java.util.List;

/**
 * 错题本Service接口
 *
 * @author system
 * @since 2026-04-09
 */
public interface IExamWrongQuestionService extends IService<ExamWrongQuestion> {

    /**
     * 添加错题
     *
     * @param studentId 学员ID
     * @param questionId 题目ID
     * @param paperId 试卷ID
     */
    void addWrongQuestion(Long studentId, Long questionId, Long paperId);

    /**
     * 标记为已掌握
     *
     * @param studentId 学员ID
     * @param questionId 题目ID
     */
    void markAsMastered(Long studentId, Long questionId);

    /**
     * 获取学员错题列表
     *
     * @param studentId 学员ID
     * @param isMastered 是否已掌握(null表示全部)
     * @return 错题列表
     */
    List<ExamWrongQuestion> getWrongQuestions(Long studentId, Integer isMastered);
}
