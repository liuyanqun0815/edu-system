package com.education.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 系统设置实体
 */
@Data
@TableName("sys_setting")
public class SysSetting {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 分组编码（basic/security/notify/learn）
     */
    private String groupCode;

    /**
     * 配置键
     */
    private String settingKey;

    /**
     * 配置值
     */
    private String settingValue;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
