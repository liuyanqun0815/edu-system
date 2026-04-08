package com.education.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 邮件模板实体
 */
@Data
@TableName("sys_email_template")
public class SysEmailTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 模板编码（唯一标识，如 lesson_notify） */
    private String templateCode;

    /** 模板名称 */
    private String templateName;

    /** 邮件主题（支持变量 ${xxx}） */
    private String subject;

    /** 邮件正文（支持变量 ${xxx}，HTML格式） */
    private String body;

    /** 支持的变量说明，JSON格式 */
    private String variables;

    /** 适用角色：teacher/parent/student/all */
    private String roleType;

    /** 状态：0禁用 1启用 */
    private Integer status;

    /** 创建人ID */
    private Long createBy;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;
}
