package com.education.admin.dto;

import lombok.Data;

/**
 * 用户统计 VO
 */
@Data
public class UserStatsVO {
    
    /**
     * 总用户数
     */
    private Long total;
    
    /**
     * 启用用户数
     */
    private Long enabled;
    
    /**
     * 禁用用户数
     */
    private Long disabled;
    
    /**
     * 在线用户数
     */
    private Long online;
}
