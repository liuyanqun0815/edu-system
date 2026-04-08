package com.education.exam.dto;

import lombok.Data;

/**
 * 交卷结果VO
 */
@Data
public class SubmitPaperVO {
    
    /**
     * 得分
     */
    private Integer score;
    
    /**
     * 总分
     */
    private Integer totalScore;
    
    /**
     * 正确题数
     */
    private Integer correctCount;
    
    /**
     * 错误题数
     */
    private Integer wrongCount;
    
    /**
     * 用时（分钟）
     */
    private Integer duration;
}
