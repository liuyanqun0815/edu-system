package com.education.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.education.common.result.JsonResult;
import com.education.common.result.PageResult;
import com.education.system.entity.AiResource;
import com.education.system.entity.AiResourceTask;
import com.education.system.mapper.AiResourceMapper;
import com.education.system.mapper.AiResourceTaskMapper;
import com.education.system.service.AiCrawlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * AI资源管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/ai/resource")
public class AiResourceController {

    @Autowired
    private AiResourceMapper resourceMapper;

    @Autowired
    private AiResourceTaskMapper taskMapper;

    @Autowired
    private AiCrawlService crawlService;

    /**
     * 查询资源列表
     */
    @GetMapping("/page")
    public JsonResult<PageResult<AiResource>> getPage(
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        
        LambdaQueryWrapper<AiResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(resourceType != null, AiResource::getResourceType, resourceType)
               .eq(subject != null, AiResource::getSubject, subject)
               .eq(status != null, AiResource::getStatus, status)
               .eq(AiResource::getDr, 0)
               .orderByDesc(AiResource::getCreateTime);
        
        Page<AiResource> page = new Page<>(pageNum, pageSize);
        resourceMapper.selectPage(page, wrapper);
        
        return JsonResult.success(PageResult.of(page));
    }

    /**
     * 查询任务列表
     */
    @GetMapping("/task/page")
    public JsonResult<PageResult<AiResourceTask>> getTaskPage(
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) Integer crawlStatus,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        
        LambdaQueryWrapper<AiResourceTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(taskType != null, AiResourceTask::getTaskType, taskType)
               .eq(crawlStatus != null, AiResourceTask::getCrawlStatus, crawlStatus)
               .eq(AiResourceTask::getDr, 0)
               .orderByDesc(AiResourceTask::getCreateTime);
        
        Page<AiResourceTask> page = new Page<>(pageNum, pageSize);
        taskMapper.selectPage(page, wrapper);
        
        return JsonResult.success(PageResult.of(page));
    }

    /**
     * 手动提交抓取任务
     */
    @PostMapping("/task/submit")
    public JsonResult<Long> submitTask(@RequestBody AiResourceTask task) {
        task.setCreateTime(new Date());
        crawlService.submitCrawlTask(task);
        return JsonResult.success(task.getId());
    }

    /**
     * 审核资源
     */
    @PutMapping("/review")
    public JsonResult<Void> reviewResource(
            @RequestParam Long resourceId,
            @RequestParam Integer status,
            @RequestParam(required = false) String comment) {
        
        AiResource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            return JsonResult.error("资源不存在");
        }
        
        resource.setStatus(status);
        resource.setReviewComment(comment);
        resource.setUpdateTime(new Date());
        
        if (status == 3) { // 已入库
            log.info("资源待导入: ID={}, 类型={}", resourceId, resource.getResourceType());
            // 导入逻辑由 edu-admin 的 AiResourceImportService 处理
        }
        
        resourceMapper.updateById(resource);
        return JsonResult.success(null);
    }

    /**
     * 删除资源
     */
    @DeleteMapping("/{id}")
    public JsonResult<Void> deleteResource(@PathVariable Long id) {
        AiResource resource = new AiResource();
        resource.setId(id);
        resource.setDr(1);
        resource.setUpdateTime(new Date());
        resourceMapper.updateById(resource);
        return JsonResult.success(null);
    }
}
