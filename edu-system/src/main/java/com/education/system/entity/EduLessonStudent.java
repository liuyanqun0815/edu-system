package com.education.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 排课-学生关联实体类
 *
 * @author system
 * @since 2026-04-09
 */
@Data
@TableName("edu_lesson_student")
public class EduLessonStudent implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 排课ID
     */
    private Long lessonId;

    /**
     * 学生用户ID
     */
    private Long studentId;
}
