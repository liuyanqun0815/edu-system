package com.education.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 解析规则实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("ai_parse_rule")
public class AiParseRule implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 规则ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 关联模板ID */
    private Long templateId;

    /** 规则类型: title-标题, content-内容, answer-答案, question-题目, meta-元信息 */
    private String ruleType;

    /** 规则名称 */
    private String ruleName;

    /** 选择器类型: css- CSS选择器, regex-正则表达式 */
    private String selectorType;

    /** 选择器值 */
    private String selectorValue;

    /** 提取属性: text-文本, html-HTML, 或其他属性名 */
    private String extractAttr;

    /** 清洗规则 */
    private String cleanRule;

    /** 是否必填:0-非必填 1-必填 */
    private Integer required;

    /** 排序 */
    private Integer sortOrder;

    /** 是否启用:0-禁用 1-启用 */
    private Integer enabled;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
