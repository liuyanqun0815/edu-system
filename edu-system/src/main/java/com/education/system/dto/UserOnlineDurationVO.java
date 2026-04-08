package com.education.system.dto;

import lombok.Data;

/**
 * 用户在线时长统计VO
 */
@Data
public class UserOnlineDurationVO {
    
    /**
     * 总在线时长（分钟）
     */
    private Integer totalDuration;
    
    /**
     * 今日在线时长（分钟）
     */
    private Integer todayDuration;
    
    /**
     * 登录次数
     */
    private Integer loginCount;
}
