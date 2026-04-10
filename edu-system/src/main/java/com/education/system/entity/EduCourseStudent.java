package com.education.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.education.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 课程学员关联实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_course_student")
public class EduCourseStudent extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 学员ID
     */
    private Long studentId;

    /**
     * 报名时间
     */
    private Date enrollTime;

    /**
     * 开始学习时间
     */
    private Date startTime;

    /**
     * 完成时间
     */
    private Date finishTime;

    /**
     * 学习进度(0-100)
     */
    private BigDecimal progress;

    /**
     * 状态:1-学习中 2-已完成 3-已过期
     */
    private Integer status;

    /**
     * 课程过期时间
     */
    private Date expireTime;
}
