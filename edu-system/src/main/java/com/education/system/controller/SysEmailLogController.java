package com.education.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.education.common.result.JsonResult;
import com.education.common.result.PageResult;
import com.education.system.entity.SysEmailLog;
import com.education.system.mapper.SysEmailLogMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 邮件日志Controller
 */
@Api(tags = "邮件日志管理")
@RestController
@RequestMapping("/system/email-log")
@RequiredArgsConstructor
public class SysEmailLogController {

    private final SysEmailLogMapper emailLogMapper;

    @ApiOperation("分页查询邮件日志")
    @GetMapping("/page")
    public JsonResult<PageResult<SysEmailLog>> page(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String toEmail,
            @RequestParam(required = false) Integer sendStatus) {
        
        LambdaQueryWrapper<SysEmailLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(toEmail != null && !toEmail.isEmpty(), SysEmailLog::getToEmail, toEmail)
                .eq(sendStatus != null, SysEmailLog::getSendStatus, sendStatus)
                .orderByDesc(SysEmailLog::getSendTime);
        
        Page<SysEmailLog> page = emailLogMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return JsonResult.success(PageResult.of(page.getTotal(), page.getRecords(), pageNum, pageSize));
    }

    @ApiOperation("根据ID查询日志")
    @GetMapping("/{id}")
    public JsonResult<SysEmailLog> getById(@PathVariable Long id) {
        return JsonResult.success(emailLogMapper.selectById(id));
    }

    @ApiOperation("删除日志")
    @DeleteMapping("/{id}")
    public JsonResult<Void> delete(@PathVariable Long id) {
        emailLogMapper.deleteById(id);
        return JsonResult.success("删除成功");
    }
}
