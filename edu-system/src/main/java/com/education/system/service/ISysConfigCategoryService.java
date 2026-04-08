package com.education.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.system.entity.SysConfigCategory;

import java.util.List;

/**
 * 配置分类Service接口
 */
public interface ISysConfigCategoryService extends IService<SysConfigCategory> {
    
    /**
     * 查询所有启用的分类
     */
    List<SysConfigCategory> listActiveCategories();
}
