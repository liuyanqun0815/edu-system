package com.education.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.education.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 考试答题卡模板实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_answer_template")
public class ExamAnswerTemplate extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 试卷类型:1-单元测试 2-期中 3-期末 4-模拟考 5-真题
     */
    private Integer paperType;

    /**
     * 模板配置(JSON格式)
     */
    private String templateConfig;

    /**
     * 状态:0-禁用 1-启用
     */
    private Integer status;
}
