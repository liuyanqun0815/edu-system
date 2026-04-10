package com.education.course.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 课程统计 VO
 */
@Data
public class CourseStatsVO {
    
    /**
     * 总课程数
     */
    private Long total;
    
    /**
     * 已发布课程数
     */
    private Long published;
    
    /**
     * 草稿课程数
     */
    private Long draft;
    
    /**
     * 总浏览量
     */
    private Long totalViewCount;
    
    /**
     * 总学习人数
     */
    private Long totalLearnCount;
    
    /**
     * 平均评分
     */
    private BigDecimal avgRating;
}
