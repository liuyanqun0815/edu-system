package com.education.system.dto;

import lombok.Data;

import java.util.Date;

/**
 * 训练会话VO
 */
@Data
public class TrainingSessionVO {

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 年级
     */
    private String grade;

    /**
     * 难度
     */
    private Integer difficulty;

    /**
     * 题目类型
     */
    private String questionType;

    /**
     * 题目总数
     */
    private Integer questionCount;

    /**
     * 当前题号
     */
    private Integer currentQuestionIndex;

    /**
     * 每题时间(秒)
     */
    private Integer perQuestionTime;

    /**
     * 会话状态:0-进行中 1-已完成 2-已放弃
     */
    private Integer status;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 已答题数
     */
    private Integer answeredCount;

    /**
     * 正确题数
     */
    private Integer correctCount;
}
