package com.education.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.education.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学科实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_subject")
public class ExamSubject extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 学科名称
     */
    private String subjectName;

    /**
     * 学科编码
     */
    private String subjectCode;

    /**
     * 学科描述
     */
    private String description;

    /**
     * 所属年级，如：六年级、初一、高三
     */
    private String grade;

    /**
     * 学科图标（emoji）
     */
    private String icon;

    /**
     * 学科颜色（十六进制色值）
     */
    private String color;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;
}
