package com.education.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.system.entity.SysConfigCategory;
import com.education.system.mapper.SysConfigCategoryMapper;
import com.education.system.service.ISysConfigCategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 配置分类Service实现
 */
@Service
public class SysConfigCategoryServiceImpl extends ServiceImpl<SysConfigCategoryMapper, SysConfigCategory> 
        implements ISysConfigCategoryService {

    @Override
    public List<SysConfigCategory> listActiveCategories() {
        LambdaQueryWrapper<SysConfigCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfigCategory::getStatus, 1)
               .orderByAsc(SysConfigCategory::getSort);
        return list(wrapper);
    }
}
