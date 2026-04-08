package com.education.system.controller;

import com.education.common.result.JsonResult;
import com.education.common.result.PageResult;
import com.education.system.dto.EvaluationStatsVO;
import com.education.system.entity.SysEvaluation;
import com.education.system.query.EvaluationQuery;
import com.education.system.service.ISysEvaluationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 评价管理控制器
 */
@RestController
@RequestMapping("/system/evaluation")
@Api(tags = "评价管理")
public class SysEvaluationController {
    
    @Autowired
    private ISysEvaluationService evaluationService;
    
    @ApiOperation("分页查询评价列表")
    @GetMapping("/page")
    public JsonResult<PageResult<SysEvaluation>> page(EvaluationQuery query) {
        PageResult<SysEvaluation> result = evaluationService.pageList(query);
        return JsonResult.success(result);
    }
    
    @ApiOperation("获取评价详情")
    @GetMapping("/{id}")
    public JsonResult<SysEvaluation> getById(@PathVariable Long id) {
        SysEvaluation evaluation = evaluationService.getById(id);
        return JsonResult.success(evaluation);
    }
    
    @ApiOperation("删除评价")
    @DeleteMapping("/{id}")
    public JsonResult<Void> delete(@PathVariable Long id) {
        evaluationService.removeById(id);
        return JsonResult.success("删除成功");
    }
    
    @ApiOperation("回复评价")
    @PostMapping("/{id}/reply")
    public JsonResult<Void> reply(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String reply = params.get("reply");
        if (reply == null || reply.trim().isEmpty()) {
            return JsonResult.error("回复内容不能为空");
        }
        evaluationService.reply(id, reply);
        return JsonResult.success("回复成功");
    }
    
    @ApiOperation("获取评价统计")
    @GetMapping("/stats")
    public JsonResult<EvaluationStatsVO> getStats() {
        EvaluationStatsVO stats = evaluationService.getStats();
        return JsonResult.success(stats);
    }
}
