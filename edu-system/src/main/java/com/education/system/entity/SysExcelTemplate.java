package com.education.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.education.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Excel模板配置实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_excel_template")
public class SysExcelTemplate extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模板编码 (唯一标识)
     */
    private String templateCode;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板类型: 1-导入模板 2-导出模板
     */
    private Integer templateType;

    /**
     * 业务模块 (如: question, course, exam)
     */
    private String businessModule;

    /**
     * Sheet名称
     */
    private String sheetName;

    /**
     * 表头行高
     */
    private Integer headerRowHeight;

    /**
     * 内容行高
     */
    private Integer contentRowHeight;

    /**
     * 默认列宽
     */
    private Integer defaultColumnWidth;

    /**
     * 表头背景色 (如: #4472C4)
     */
    private String headerBgColor;

    /**
     * 表头字体颜色 (如: #FFFFFF)
     */
    private String headerFontColor;

    /**
     * 是否启用: 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 备注
     */
    private String remark;
}
