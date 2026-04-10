package com.education.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.education.common.result.JsonResult;
import com.education.common.result.PageResult;
import com.education.system.entity.SysParseTemplate;
import com.education.system.service.ISysParseTemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文档解析模板管理Controller
 */
@Slf4j
@Api(tags = "文档解析模板管理")
@RestController
@RequestMapping("/system/parse-template")
@RequiredArgsConstructor
public class SysParseTemplateController {

    private final ISysParseTemplateService parseTemplateService;

    @ApiOperation("分页查询模板列表")
    @GetMapping("/page")
    public JsonResult<PageResult<SysParseTemplate>> page(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String documentType) {
        
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysParseTemplate> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(documentType != null, SysParseTemplate::getDocumentType, documentType)
               .eq(SysParseTemplate::getDr, 0)
               .orderByAsc(SysParseTemplate::getSortOrder);
        
        Page<SysParseTemplate> page = parseTemplateService.page(new Page<>(pageNum, pageSize), wrapper);
        return JsonResult.success(PageResult.of(page));
    }

    @ApiOperation("获取所有启用的模板")
    @GetMapping("/enabled")
    public JsonResult<List<SysParseTemplate>> listEnabled() {
        return JsonResult.success(parseTemplateService.listEnabled());
    }

    @ApiOperation("根据ID查询模板")
    @GetMapping("/{id}")
    public JsonResult<SysParseTemplate> getById(@PathVariable Long id) {
        return JsonResult.success(parseTemplateService.getById(id));
    }

    @ApiOperation("新增模板")
    @PostMapping
    public JsonResult<Void> add(@RequestBody SysParseTemplate template) {
        template.setCreateTime(new Date());
        template.setUpdateTime(new Date());
        template.setDr(0);
        if (template.getStatus() == null) {
            template.setStatus(1);
        }
        parseTemplateService.save(template);
        return JsonResult.success("新增成功");
    }

    @ApiOperation("修改模板")
    @PutMapping
    public JsonResult<Void> update(@RequestBody SysParseTemplate template) {
        template.setUpdateTime(new Date());
        parseTemplateService.updateById(template);
        return JsonResult.success("修改成功");
    }

    @ApiOperation("删除模板")
    @DeleteMapping("/{id}")
    public JsonResult<Void> delete(@PathVariable Long id) {
        SysParseTemplate template = new SysParseTemplate();
        template.setId(id);
        template.setDr(1);
        template.setUpdateTime(new Date());
        parseTemplateService.updateById(template);
        return JsonResult.success("删除成功");
    }

    @ApiOperation("测试模板解析")
    @PostMapping("/test/{id}")
    public JsonResult<?> testParse(@PathVariable Long id, @RequestBody String testContent) {
        SysParseTemplate template = parseTemplateService.getById(id);
        if (template == null) {
            return JsonResult.error("模板不存在");
        }
        
        try {
            // 注意: SysParseTemplate使用JSON格式的parseRules,与AiParseTemplate不同
            // 这里返回模板配置信息,实际解析需要专门的JSON规则解析器
            Map<String, Object> response = new HashMap<>();
            response.put("templateId", template.getId());
            response.put("templateName", template.getTemplateName());
            response.put("documentType", template.getDocumentType());
            response.put("parseRules", template.getParseRules());
            response.put("testContentLength", testContent != null ? testContent.length() : 0);
            response.put("message", "SysParseTemplate使用JSON规则解析,请使用专门的解析服务");
            
            log.info("模板解析测试: ID={}, 类型={}, 规则长度={}", 
                    id, template.getDocumentType(), 
                    template.getParseRules() != null ? template.getParseRules().length() : 0);
            
            return JsonResult.success(response);
            
        } catch (Exception e) {
            log.error("模板解析测试失败: ID={}", id, e);
            return JsonResult.error("解析失败: " + e.getMessage());
        }
    }
}
