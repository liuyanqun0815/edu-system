package com.education.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_email_log")
public class SysEmailLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String fromEmail;
    
    private String toEmail;
    
    private String subject;
    
    private String content;
    
    private Integer sendStatus;
    
    private String errorMsg;
    
    private LocalDateTime sendTime;
}
