package com.education.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 按钮配置实体
 */
@Data
@TableName("sys_button")
public class SysButton {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 按钮编码（唯一）
     */
    private String buttonCode;
    
    /**
     * 按钮名称
     */
    private String buttonName;
    
    /**
     * 权限标识
     */
    private String permission;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
}
