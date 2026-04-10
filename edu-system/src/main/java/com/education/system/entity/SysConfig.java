package com.education.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 系统配置实体类
 *
 * @author system
 * @since 2026-04-09
 */
@Data
@TableName("sys_config")
public class SysConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 配置分类编码
     */
    private String categoryCode;

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 配置值
     */
    private String configValue;

    /**
     * 配置类型:1文本 2数字 3布尔 4JSON
     */
    private Integer configType;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否系统配置:0否 1是(系统配置不可删除)
     */
    private Integer isSystem;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
