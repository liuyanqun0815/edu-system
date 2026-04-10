package com.education.system.dto;

import lombok.Data;

import java.util.Date;

/**
 * 训练结果VO
 */
@Data
public class TrainingResultVO {

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 总题数
     */
    private Integer questionCount;

    /**
     * 正确题数
     */
    private Integer correctCount;

    /**
     * 错误题数
     */
    private Integer wrongCount;

    /**
     * 跳答题数
     */
    private Integer skipCount;

    /**
     * 正确率(%)
     */
    private Double accuracy;

    /**
     * 总分
     */
    private Integer totalScore;

    /**
     * 实际用时(秒)
     */
    private Integer actualDuration;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 是否加入错题本
     */
    private Boolean addedToWrongBook;
}
