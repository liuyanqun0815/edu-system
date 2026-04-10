package com.education.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.system.entity.SysParseTemplate;
import com.education.system.mapper.SysParseTemplateMapper;
import com.education.system.service.ISysParseTemplateService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 文档解析模板服务实现
 */
@Service
public class SysParseTemplateServiceImpl extends ServiceImpl<SysParseTemplateMapper, SysParseTemplate> 
        implements ISysParseTemplateService {

    @Override
    public SysParseTemplate getByDocumentType(String documentType) {
        LambdaQueryWrapper<SysParseTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysParseTemplate::getDocumentType, documentType)
               .eq(SysParseTemplate::getStatus, 1)
               .eq(SysParseTemplate::getDr, 0)
               .orderByAsc(SysParseTemplate::getSortOrder)
               .last("LIMIT 1");
        return getOne(wrapper);
    }

    @Override
    public List<SysParseTemplate> listEnabled() {
        LambdaQueryWrapper<SysParseTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysParseTemplate::getStatus, 1)
               .eq(SysParseTemplate::getDr, 0)
               .orderByAsc(SysParseTemplate::getSortOrder);
        return list(wrapper);
    }
}
