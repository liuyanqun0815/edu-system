package com.education.system.dto;

import lombok.Data;

/**
 * 评价统计VO
 */
@Data
public class EvaluationStatsVO {
    
    /**
     * 总评价数
     */
    private Integer totalCount;
    
    /**
     * 平均评分
     */
    private Double avgRating;
    
    /**
     * 已回复数
     */
    private Integer repliedCount;
    
    /**
     * 未回复数
     */
    private Integer unrepliedCount;
    
    /**
     * 好评数（4-5星）
     */
    private Integer goodCount;
    
    /**
     * 中评数（3星）
     */
    private Integer mediumCount;
    
    /**
     * 差评数（1-2星）
     */
    private Integer badCount;
}
