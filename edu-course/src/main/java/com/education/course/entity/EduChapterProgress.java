package com.education.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.education.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 章节学习进度实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_chapter_progress")
public class EduChapterProgress extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 章节ID
     */
    private Long chapterId;

    /**
     * 学员ID
     */
    private Long studentId;

    /**
     * 已观看时长(秒)
     */
    private Integer watchDuration;

    /**
     * 总时长(秒)
     */
    private Integer totalDuration;

    /**
     * 是否完成:0-否 1-是
     */
    private Integer isCompleted;

    /**
     * 最后观看时间
     */
    private Date lastWatchTime;
}
