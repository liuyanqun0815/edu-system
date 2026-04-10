package com.education.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.system.entity.SysParseTemplate;

import java.util.List;

/**
 * 文档解析模板服务接口
 */
public interface ISysParseTemplateService extends IService<SysParseTemplate> {

    /**
     * 根据文档类型获取模板
     */
    SysParseTemplate getByDocumentType(String documentType);

    /**
     * 获取所有启用的模板
     */
    List<SysParseTemplate> listEnabled();
}
