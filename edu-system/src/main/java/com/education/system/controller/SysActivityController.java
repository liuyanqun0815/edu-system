package com.education.system.controller;

import com.education.common.result.JsonResult;
import com.education.common.result.PageResult;
import com.education.system.entity.SysActivity;
import com.education.system.query.ActivityQuery;
import com.education.system.service.ISysActivityService;
import com.education.system.mapper.SysUserRoleMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 活动管理控制器
 */
@Slf4j
@Api(tags = "活动管理")
@RestController
@RequestMapping("/system/activity")
@RequiredArgsConstructor
public class SysActivityController {

    private final ISysActivityService activityService;
    private final SysUserRoleMapper userRoleMapper;

    // SSE连接池：用户ID -> 连接列表
    private static final Map<String, CopyOnWriteArrayList<SseEmitter>> sseEmitters = new ConcurrentHashMap<>();

    // ==================== SSE推送 ====================

    @ApiOperation("SSE订阅活动推送")
    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@RequestParam(defaultValue = "0") String userId) {
        SseEmitter emitter = new SseEmitter(0L); // 0表示永不超时
        sseEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(e -> removeEmitter(userId, emitter));

        // 发送心跳
        try {
            emitter.send(SseEmitter.event().name("connected").data("连接成功"));
        } catch (Exception e) {
            log.error("SSE心跳发送失败", e);
        }
        return emitter;
    }

    private void removeEmitter(String userId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> list = sseEmitters.get(userId);
        if (list != null) list.remove(emitter);
    }

    /**
     * 向指定用户列表推送活动消息
     */
    public static void pushActivity(List<String> userIds, String message) {
        for (String userId : userIds) {
            CopyOnWriteArrayList<SseEmitter> list = sseEmitters.get(userId);
            if (list == null) continue;
            for (SseEmitter emitter : list) {
                try {
                    emitter.send(SseEmitter.event().name("activity").data(message));
                } catch (Exception e) {
                    list.remove(emitter);
                }
            }
        }
    }

    /**
     * 向所有连接用户广播活动
     */
    public static void broadcastActivity(String message) {
        sseEmitters.forEach((userId, list) -> {
            for (SseEmitter emitter : list) {
                try {
                    emitter.send(SseEmitter.event().name("activity").data(message));
                } catch (Exception e) {
                    list.remove(emitter);
                }
            }
        });
    }

    // ==================== 活动查询 ====================

    @ApiOperation("分页查询活动列表")
    @GetMapping("/page")
    public JsonResult<PageResult<SysActivity>> page(ActivityQuery query) {
        PageResult<SysActivity> result = activityService.pageList(query);
        return JsonResult.success(result);
    }

    @ApiOperation("查询所有活动列表")
    @GetMapping("/list")
    public JsonResult<List<SysActivity>> list() {
        List<SysActivity> list = activityService.list();
        return JsonResult.success(list);
    }

    @ApiOperation("查询已发布的活动列表")
    @GetMapping("/published")
    public JsonResult<List<SysActivity>> publishedList() {
        List<SysActivity> list = activityService.getPublishedList();
        return JsonResult.success(list);
    }

    @ApiOperation("查询最新的N条活动")
    @GetMapping("/latest")
    public JsonResult<List<SysActivity>> latestList(@RequestParam(defaultValue = "5") int limit) {
        List<SysActivity> list = activityService.getLatestList(limit);
        return JsonResult.success(list);
    }

    @ApiOperation("查询置顶的活动列表")
    @GetMapping("/top")
    public JsonResult<List<SysActivity>> topList() {
        List<SysActivity> list = activityService.getTopList();
        return JsonResult.success(list);
    }

    @ApiOperation("根据角色ID查询活动列表")
    @GetMapping("/by-role/{roleId}")
    public JsonResult<List<SysActivity>> getByRoleId(@PathVariable Long roleId,
                                                      @RequestParam(defaultValue = "5") int limit) {
        List<SysActivity> list = activityService.getByRoleId(roleId, limit);
        return JsonResult.success(list);
    }

    @ApiOperation("按当前用户可见性查询活动（超管看全部，其他只看分配的）")
    @GetMapping("/visible-by-user")
    public JsonResult<List<SysActivity>> visibleByUser(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Boolean isSuperAdmin,
            @RequestParam(defaultValue = "9") int limit) {
        // 超级管理员看全部已发布的
        if (Boolean.TRUE.equals(isSuperAdmin) || userId == null) {
            return JsonResult.success(activityService.getLatestList(limit));
        }
        // 查询用户角色
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.education.system.entity.SysUserRole> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(com.education.system.entity.SysUserRole::getUserId, userId);
        List<com.education.system.entity.SysUserRole> userRoles = userRoleMapper.selectList(wrapper);
        if (userRoles.isEmpty()) {
            // 无角色，只看全员可见（未限定角色）的活动
            return JsonResult.success(activityService.getVisibleActivities(null, limit));
        }
        // 将多个角色的可见活动合并（取任意角色可见）
        java.util.Set<Long> activityIds = new java.util.LinkedHashSet<>();
        java.util.List<SysActivity> result = new java.util.ArrayList<>();
        for (com.education.system.entity.SysUserRole ur : userRoles) {
            List<SysActivity> roleActivities = activityService.getVisibleActivities(ur.getRoleId(), limit);
            for (SysActivity a : roleActivities) {
                if (activityIds.add(a.getId())) {
                    result.add(a);
                }
            }
            if (result.size() >= limit) break;
        }
        return JsonResult.success(result.size() > limit ? result.subList(0, limit) : result);
    }

    @ApiOperation("按当前用户角色查询可见活动（未选角色=全员可见，选了=就限定角色）")
    @GetMapping("/visible")
    public JsonResult<List<SysActivity>> visibleList(@RequestParam(required = false) Long roleId,
                                                      @RequestParam(defaultValue = "9") int limit) {
        List<SysActivity> list = activityService.getVisibleActivities(roleId, limit);
        return JsonResult.success(list);
    }

    @ApiOperation("根据ID查询活动")
    @GetMapping("/{id}")
    public JsonResult<SysActivity> getById(@PathVariable Long id) {
        SysActivity activity = activityService.getById(id);
        return JsonResult.success(activity);
    }

    @ApiOperation("新增活动")
    @PostMapping
    public JsonResult<Long> add(@RequestBody @Validated SysActivity activity) {
        // 如果状态为已发布，设置发布时间
        if (activity.getStatus() != null && activity.getStatus() == 1) {
            activity.setPublishTime(new java.util.Date());
        }
        activityService.save(activity);
        return JsonResult.success(activity.getId());
    }

    @ApiOperation("修改活动")
    @PutMapping
    public JsonResult<Void> update(@RequestBody @Validated SysActivity activity) {
        // 如果状态为已发布，检查是否需要设置发布时间
        if (activity.getStatus() != null && activity.getStatus() == 1) {
            SysActivity existing = activityService.getById(activity.getId());
            if (existing != null && existing.getPublishTime() == null) {
                activity.setPublishTime(new java.util.Date());
            }
        }
        activityService.updateById(activity);
        return JsonResult.success("修改成功");
    }

    @ApiOperation("删除活动")
    @DeleteMapping("/{id}")
    public JsonResult<Void> delete(@PathVariable Long id) {
        activityService.removeById(id);
        return JsonResult.success("删除成功");
    }

    @ApiOperation("发布活动")
    @PutMapping("/{id}/publish")
    public JsonResult<Void> publish(@PathVariable Long id) {
        activityService.publishAndNotify(id);
        return JsonResult.success("发布成功");
    }

    @ApiOperation("结束活动")
    @PutMapping("/{id}/end")
    public JsonResult<Void> end(@PathVariable Long id) {
        activityService.end(id);
        return JsonResult.success("结束成功");
    }

    @ApiOperation("置顶/取消置顶活动")
    @PutMapping("/{id}/top")
    public JsonResult<Void> toggleTop(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        Integer isTop = params.get("isTop");
        activityService.toggleTop(id, isTop);
        return JsonResult.success(isTop == 1 ? "置顶成功" : "取消置顶成功");
    }

    @ApiOperation("更新活动排序")
    @PutMapping("/{id}/sort")
    public JsonResult<Void> updateSort(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        Integer sort = params.get("sort");
        activityService.updateSort(id, sort);
        return JsonResult.success("排序更新成功");
    }

    @ApiOperation("保存并发布活动")
    @PostMapping("/publish")
    public JsonResult<Long> saveAndPublish(@RequestBody @Validated SysActivity activity) {
        activityService.saveAndPublishAndNotify(activity);
        return JsonResult.success(activity.getId());
    }

    @ApiOperation("获取活动的推送角色ID列表")
    @GetMapping("/{activityId}/roles")
    public JsonResult<List<Long>> getActivityRoles(@PathVariable Long activityId) {
        List<Long> roleIds = activityService.getActivityRoleIds(activityId);
        return JsonResult.success(roleIds);
    }

    @ApiOperation("分配活动推送角色")
    @PostMapping("/{activityId}/roles")
    public JsonResult<Void> assignActivityRoles(@PathVariable Long activityId, @RequestBody Map<String, List<Long>> params) {
        List<Long> roleIds = params.get("roleIds");
        activityService.assignActivityRoles(activityId, roleIds);
        return JsonResult.success("角色分配成功");
    }
}
