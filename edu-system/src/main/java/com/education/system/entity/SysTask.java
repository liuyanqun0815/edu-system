package com.education.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

/**
 * 用户任务实体
 */
@Data
@TableName("sys_task")
public class SysTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID */
    private Long userId;

    /** 任务标题 */
    private String title;

    /** 任务详情 */
    private String content;

    /** 任务日期 */
    private LocalDate taskDate;

    /** 开始时间 */
    private LocalTime startTime;

    /** 结束时间 */
    private LocalTime endTime;

    /** 优先级：1低 2中 3高 */
    private Integer priority;

    /** 状态：0待完成 1已完成 */
    private Integer status;

    /** 任务类型：lesson/exam/custom */
    private String taskType;

    /** 关联ID（如排课ID、考试ID） */
    private Long refId;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;
}
