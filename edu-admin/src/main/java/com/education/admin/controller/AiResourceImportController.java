package com.education.admin.controller;

import com.education.admin.service.AiResourceImportService;
import com.education.common.result.JsonResult;
import com.education.system.entity.AiResource;
import com.education.system.mapper.AiResourceMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * AI资源导入控制器
 */
@Slf4j
@Api(tags = "AI资源导入管理")
@RestController
@RequestMapping("/ai/import")
public class AiResourceImportController {

    @Autowired
    private AiResourceMapper resourceMapper;

    @Autowired
    private AiResourceImportService importService;

    @ApiOperation("手动导入资源")
    @PostMapping("/resource/{resourceId}")
    public JsonResult<String> importResource(@PathVariable Long resourceId) {
        AiResource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            return JsonResult.error("资源不存在");
        }

        if (resource.getStatus() != 1 && resource.getStatus() != 3) {
            return JsonResult.error("只能导入已审核或已入库的资源");
        }

        try {
            String result = importService.importResource(resource);
            
            // 更新状态为已入库
            resource.setStatus(3);
            resource.setReviewComment((resource.getReviewComment() != null ? resource.getReviewComment() + " | " : "") + "导入结果: " + result);
            resource.setUpdateTime(new Date());
            resourceMapper.updateById(resource);
            
            return JsonResult.success(result);
        } catch (Exception e) {
            log.error("资源导入失败: ID={}", resourceId, e);
            return JsonResult.error("导入失败: " + e.getMessage());
        }
    }

    @ApiOperation("批量导入资源")
    @PostMapping("/batch")
    public JsonResult<String> batchImport(@RequestBody java.util.List<Long> resourceIds) {
        int successCount = 0;
        int failCount = 0;
        StringBuilder errorMsg = new StringBuilder();

        for (Long resourceId : resourceIds) {
            try {
                AiResource resource = resourceMapper.selectById(resourceId);
                if (resource != null && (resource.getStatus() == 1 || resource.getStatus() == 3)) {
                    importService.importResource(resource);
                    resource.setStatus(3);
                    resource.setUpdateTime(new Date());
                    resourceMapper.updateById(resource);
                    successCount++;
                }
            } catch (Exception e) {
                log.error("资源导入失败: ID={}", resourceId, e);
                failCount++;
                errorMsg.append("ID=").append(resourceId).append(": ").append(e.getMessage()).append("; ");
            }
        }

        String result = String.format("成功: %d, 失败: %d", successCount, failCount);
        if (failCount > 0) {
            result += " | " + errorMsg.toString();
        }
        return JsonResult.success(result);
    }
}
