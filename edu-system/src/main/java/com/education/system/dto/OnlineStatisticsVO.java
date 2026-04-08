package com.education.system.dto;

import com.education.system.entity.SysUser;
import lombok.Data;

import java.util.List;

/**
 * 在线统计VO
 */
@Data
public class OnlineStatisticsVO {
    
    /**
     * 当前在线人数
     */
    private Integer onlineCount;
    
    /**
     * 今日登录人数
     */
    private Integer todayLoginCount;
    
    /**
     * 总用户数
     */
    private Integer totalUserCount;
    
    /**
     * 在线用户列表
     */
    private List<SysUser> onlineUsers;
}
