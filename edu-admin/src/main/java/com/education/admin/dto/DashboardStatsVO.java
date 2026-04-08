package com.education.admin.dto;

import lombok.Data;

/**
 * 仪表盘统计 VO
 */
@Data
public class DashboardStatsVO {
    
    /**
     * 课程统计
     */
    private StatsComparisonVO courses;
    
    /**
     * 学员统计
     */
    private StatsComparisonVO students;
    
    /**
     * 考试统计
     */
    private ExamSummaryVO exams;
    
    /**
     * 通过率统计
     */
    private StatsComparisonVO passRate;
    
    @Data
    public static class ExamSummaryVO {
        private Long yesterday;
    }
}
