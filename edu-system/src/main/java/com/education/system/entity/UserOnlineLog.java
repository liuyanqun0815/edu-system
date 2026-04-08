package com.education.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_online_log")
public class UserOnlineLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private LocalDateTime loginTime;
    
    private LocalDateTime logoutTime;
    
    private String loginIp;
    
    private String userAgent;
    
    private Integer duration;
}
