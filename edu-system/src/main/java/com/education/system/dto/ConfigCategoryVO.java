package com.education.system.dto;

import com.education.system.entity.SysConfigItem;
import lombok.Data;

import java.util.List;

/**
 * 配置分类 VO
 */
@Data
public class ConfigCategoryVO {
    
    /**
     * 分类编码
     */
    private String categoryCode;
    
    /**
     * 分类名称
     */
    private String categoryName;
    
    /**
     * 配置项列表
     */
    private List<SysConfigItem> items;
}
