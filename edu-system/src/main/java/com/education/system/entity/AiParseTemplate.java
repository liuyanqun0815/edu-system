package com.education.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 站点解析模板实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("ai_parse_template")
public class AiParseTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 模板ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 模板名称 */
    private String templateName;

    /** 站点名称 */
    private String siteName;

    /** 站点域名 */
    private String siteDomain;

    /** 列表页选择器 */
    private String listSelector;

    /** 详情页选择器 */
    private String detailSelector;

    /** 标题选择器 */
    private String titleSelector;

    /** 内容选择器 */
    private String contentSelector;

    /** 答案选择器 */
    private String answerSelector;

    /** 元信息选择器 */
    private String metaSelector;

    /** 分页规则 */
    private String paginationRule;

    /** 字符编码 */
    private String encoding;

    /** 是否启用:0-禁用 1-启用 */
    private Integer enabled;

    /** 优先级(数值越大优先级越高) */
    private Integer priority;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
