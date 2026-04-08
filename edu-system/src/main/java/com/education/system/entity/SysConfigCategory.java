package com.education.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 配置分类实体
 */
@Data
@TableName("sys_config_category")
public class SysConfigCategory {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 分类编码
     */
    private String code;
    
    /**
     * 分类名称
     */
    private String name;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 图标
     */
    private String icon;
    
    /**
     * 排序
     */
    private Integer sort;
    
    /**
     * 状态 0-禁用 1-启用
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
