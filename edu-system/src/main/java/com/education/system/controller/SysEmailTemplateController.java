package com.education.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.education.common.result.JsonResult;
import com.education.system.entity.SysEmailTemplate;
import com.education.system.mapper.SysEmailTemplateMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 邮件模板Controller
 */
@Api(tags = "邮件模板管理")
@RestController
@RequestMapping("/system/email-template")
@RequiredArgsConstructor
public class SysEmailTemplateController {

    private final SysEmailTemplateMapper templateMapper;

    @ApiOperation("查询邮件模板列表")
    @GetMapping("/list")
    public JsonResult<List<SysEmailTemplate>> list(
            @RequestParam(required = false) String roleType,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<SysEmailTemplate> wrapper = new LambdaQueryWrapper<SysEmailTemplate>()
                .eq(roleType != null && !roleType.isEmpty(), SysEmailTemplate::getRoleType, roleType)
                .eq(status != null, SysEmailTemplate::getStatus, status)
                .orderByAsc(SysEmailTemplate::getId);
        return JsonResult.success(templateMapper.selectList(wrapper));
    }

    @ApiOperation("根据编码查询模板")
    @GetMapping("/code/{code}")
    public JsonResult<SysEmailTemplate> getByCode(@PathVariable String code) {
        LambdaQueryWrapper<SysEmailTemplate> wrapper = new LambdaQueryWrapper<SysEmailTemplate>()
                .eq(SysEmailTemplate::getTemplateCode, code)
                .eq(SysEmailTemplate::getStatus, 1);
        return JsonResult.success(templateMapper.selectOne(wrapper));
    }

    @ApiOperation("根据ID查询模板")
    @GetMapping("/{id}")
    public JsonResult<SysEmailTemplate> getById(@PathVariable Long id) {
        return JsonResult.success(templateMapper.selectById(id));
    }

    @ApiOperation("新增邮件模板")
    @PostMapping
    public JsonResult<Void> add(@RequestBody SysEmailTemplate template,
                                 @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        template.setCreateBy(userId);
        templateMapper.insert(template);
        return JsonResult.success("新增成功");
    }

    @ApiOperation("编辑邮件模板")
    @PutMapping
    public JsonResult<Void> update(@RequestBody SysEmailTemplate template) {
        templateMapper.updateById(template);
        return JsonResult.success("修改成功");
    }

    @ApiOperation("删除邮件模板")
    @DeleteMapping("/{id}")
    public JsonResult<Void> delete(@PathVariable Long id) {
        templateMapper.deleteById(id);
        return JsonResult.success("删除成功");
    }

    @ApiOperation("启用/禁用模板")
    @PutMapping("/{id}/status")
    public JsonResult<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        SysEmailTemplate template = new SysEmailTemplate();
        template.setId(id);
        template.setStatus(status);
        templateMapper.updateById(template);
        return JsonResult.success("更新成功");
    }
}
