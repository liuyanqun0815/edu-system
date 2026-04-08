package com.education.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("subject")
public class Subject {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long gradeId;
    
    private String name;
    
    private String code;
    
    private String icon;
    
    private String color;
    
    private String description;
    
    private Integer questionCount;
    
    private Integer status;
    
    private Integer dr;
    
    private Date createTime;
    
    private Date updateTime;
}
