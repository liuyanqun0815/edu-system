package com.education.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.education.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Excel模板列配置实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_excel_template_column")
public class SysExcelTemplateColumn extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模板ID
     */
    private Long templateId;

    /**
     * 列索引 (从0开始)
     */
    private Integer columnIndex;

    /**
     * 列名 (字段名)
     */
    private String fieldName;

    /**
     * 列标题
     */
    private String columnTitle;

    /**
     * 列宽 (覆盖默认列宽)
     */
    private Integer columnWidth;

    /**
     * 数据类型: String, Integer, Long, Double, BigDecimal, Date
     */
    private String dataType;

    /**
     * 是否必填: 0-否 1-是
     */
    private Integer required;

    /**
     * 是否显示: 0-隐藏 1-显示
     */
    private Integer visible;

    /**
     * 对齐方式: left, center, right
     */
    private String alignment;

    /**
     * 示例数据
     */
    private String exampleValue;

    /**
     * 数据字典编码 (用于下拉选项)
     */
    private String dictCode;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 验证规则 (JSON格式)
     */
    private String validationRules;

    /**
     * 备注
     */
    private String remark;
}
