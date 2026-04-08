package com.education.admin.dto;

import lombok.Data;

import java.util.List;

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
}
