package com.education.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.system.dto.ConfigCategoryVO;
import com.education.system.entity.SysConfigItem;

import java.util.List;

/**
 * 配置项Service接口
 */
public interface ISysConfigItemService extends IService<SysConfigItem> {
    
    /**
     * 根据分类编码查询配置项
     */
    List<SysConfigItem> listByCategoryCode(String categoryCode);
    
    /**
     * 获取所有配置（按分类分组）
     */
    List<ConfigCategoryVO> listAllGroupByCategory();
}
