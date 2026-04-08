package com.education.system.dto;

import lombok.Data;

/**
 * 仪表盘统计 VO
 */
@Data
public class DashboardStatsVO {
    
    /**
     * 今日学习人数
     */
    private Integer todayLearners;
    
    /**
     * 今日考试人数
     */
    private Integer todayExamUsers;
    
    /**
     * 总用户数
     */
    private Integer totalUsers;
    
    /**
     * 总课程数
     */
    private Integer totalCourses;
    
    /**
     * 总考试数
     */
    private Integer totalExams;
    
    /**
     * 在线用户数
     */
    private Integer onlineUsers;
}
