package com.education.system.dto;

import com.education.system.entity.SysExcelTemplate;
import com.education.system.entity.SysExcelTemplateColumn;
import lombok.Data;

import java.util.List;

/**
 * Excel模板完整DTO (包含列配置)
 */
@Data
public class ExcelTemplateVO {

    /**
     * 模板信息
     */
    private SysExcelTemplate template;

    /**
     * 列配置列表
     */
    private List<SysExcelTemplateColumn> columns;
}
