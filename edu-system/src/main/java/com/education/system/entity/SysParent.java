package com.education.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 家长信息表
 */
@Data
@TableName("sys_parent")
public class SysParent implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 家长姓名 */
    private String name;

    /** 手机号 */
    private String phone;

    /** 微信号 */
    private String wechat;

    /** 邮箱 */
    private String email;

    /** 与学生关系：父亲/母亲/其他 */
    private String relation;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;
}
