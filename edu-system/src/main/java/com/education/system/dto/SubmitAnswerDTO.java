package com.education.system.dto;

import lombok.Data;

/**
 * 提交答案DTO
 */
@Data
public class SubmitAnswerDTO {

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 单词ID
     */
    private Long wordId;

    /**
     * 用户答案
     */
    private String userAnswer;

    /**
     * 是否跳过
     */
    private Boolean skipped = false;

    /**
     * 答题用时(秒)
     */
    private Integer answerTime;

    /**
     * 是否超时
     */
    private Boolean timeout = false;
}
