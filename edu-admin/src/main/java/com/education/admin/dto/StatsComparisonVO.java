package com.education.admin.dto;

import lombok.Data;

/**
 * 统计对比 VO
 */
@Data
public class StatsComparisonVO {
    
    /**
     * 当前值
     */
    private Long current;
    
    /**
     * 上期值
     */
    private Long lastMonth;
}
