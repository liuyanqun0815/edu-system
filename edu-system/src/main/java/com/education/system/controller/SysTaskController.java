package com.education.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.education.common.result.JsonResult;
import com.education.system.entity.SysTask;
import com.education.system.mapper.SysTaskMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户任务Controller
 */
@Api(tags = "用户任务")
@RestController
@RequestMapping("/system/task")
@RequiredArgsConstructor
public class SysTaskController {

    private final SysTaskMapper taskMapper;

    @ApiOperation("查询当前用户某日任务列表")
    @GetMapping("/list")
    public JsonResult<List<SysTask>> list(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(required = false) String date) {
        if (userId == null) return JsonResult.success(java.util.Collections.emptyList());
        LocalDate queryDate = (date != null && !date.isEmpty()) ? LocalDate.parse(date) : LocalDate.now();
        return JsonResult.success(taskMapper.selectByUserAndDate(userId, queryDate));
    }

    @ApiOperation("新增任务")
    @PostMapping
    public JsonResult<Void> add(@RequestBody SysTask task,
                                 @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (task.getUserId() == null) task.setUserId(userId);
        taskMapper.insert(task);
        return JsonResult.success("新增成功");
    }

    @ApiOperation("修改任务")
    @PutMapping
    public JsonResult<Void> update(@RequestBody SysTask task) {
        taskMapper.updateById(task);
        return JsonResult.success("修改成功");
    }

    @ApiOperation("完成/取消任务")
    @PutMapping("/{id}/status")
    public JsonResult<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        SysTask task = new SysTask();
        task.setId(id);
        task.setStatus(status);
        taskMapper.updateById(task);
        return JsonResult.success("更新成功");
    }

    @ApiOperation("删除任务")
    @DeleteMapping("/{id}")
    public JsonResult<Void> delete(@PathVariable Long id) {
        taskMapper.deleteById(id);
        return JsonResult.success("删除成功");
    }
}
