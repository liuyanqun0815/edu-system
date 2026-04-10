package com.education.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.education.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 错题本实体类
 *
 * @author system
 * @since 2026-04-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_wrong_question")
public class ExamWrongQuestion extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 学员ID
     */
    private Long studentId;

    /**
     * 题目ID
     */
    private Long questionId;

    /**
     * 试卷ID
     */
    private Long paperId;

    /**
     * 错误次数
     */
    private Integer wrongCount;

    /**
     * 最后错误时间
     */
    private Date lastWrongTime;

    /**
     * 是否已掌握:0-否 1-是
     */
    private Integer isMastered;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
