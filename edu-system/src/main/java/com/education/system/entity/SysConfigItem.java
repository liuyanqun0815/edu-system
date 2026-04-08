package com.education.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 配置项实体（枚举值）
 */
@Data
@TableName("sys_config_item")
public class SysConfigItem {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 分类编码
     */
    private String categoryCode;
    
    /**
     * 配置键
     */
    private String itemKey;
    
    /**
     * 配置值
     */
    private String itemValue;
    
    /**
     * 显示标签
     */
    private String itemLabel;
    
    /**
     * 颜色
     */
    private String itemColor;
    
    /**
     * 描述
     */
    private String description;
    
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
