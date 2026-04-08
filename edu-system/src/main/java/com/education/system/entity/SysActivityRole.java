package com.education.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 活动角色推送关联实体
 */
@Data
@TableName("sys_activity_role")
public class SysActivityRole {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 创建时间
     */
    private Date createTime;
}
