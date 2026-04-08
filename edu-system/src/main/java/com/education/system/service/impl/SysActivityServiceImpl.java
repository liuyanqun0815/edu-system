package com.education.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.common.result.PageResult;
import com.education.system.entity.SysActivity;
import com.education.system.entity.SysActivityRole;
import com.education.system.mapper.SysActivityMapper;
import com.education.system.mapper.SysActivityRoleMapper;
import com.education.system.mapper.SysUserRoleMapper;
import com.education.system.query.ActivityQuery;
import com.education.system.service.ISysActivityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 活动管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysActivityServiceImpl extends ServiceImpl<SysActivityMapper, SysActivity> implements ISysActivityService {

    private final SysActivityRoleMapper activityRoleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final ObjectMapper objectMapper;

    // WebSocket推送器（通过反射拿到）
    private static volatile Object wsHandler = null;

    /**
     * 延迟加载 WebSocket Handler（避免循环依赖）
     */
    private static Object getWsHandler() {
        if (wsHandler == null) {
            try {
                Class<?> clazz = Class.forName("com.education.admin.websocket.ActivityWebSocketHandler");
                wsHandler = clazz;
            } catch (ClassNotFoundException e) {
                // 单模块运行时可能不存在
            }
        }
        return wsHandler;
    }

    @Override
    public PageResult<SysActivity> pageList(ActivityQuery query) {
        Page<SysActivity> page = new Page<>(query.getPageNum(), query.getPageSize());
        
        LambdaQueryWrapper<SysActivity> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(query.getTitle())) {
            wrapper.like(SysActivity::getTitle, query.getTitle());
        }
        if (query.getActivityType() != null) {
            wrapper.eq(SysActivity::getActivityType, query.getActivityType());
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysActivity::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(SysActivity::getIsTop)
               .orderByAsc(SysActivity::getSort)
               .orderByDesc(SysActivity::getCreateTime);
        
        Page<SysActivity> result = this.page(page, wrapper);
        return PageResult.of(result.getTotal(), result.getRecords(), result.getCurrent(), result.getSize());
    }

    @Override
    public List<SysActivity> getPublishedList() {
        return baseMapper.selectPublishedList();
    }

    @Override
    public List<SysActivity> getLatestList(int limit) {
        return baseMapper.selectLatestList(limit);
    }

    @Override
    public List<SysActivity> getTopList() {
        return baseMapper.selectTopList();
    }

    @Override
    public List<SysActivity> getByRoleId(Long roleId, int limit) {
        return baseMapper.selectByRoleId(roleId, limit);
    }

    @Override
    public List<SysActivity> getVisibleActivities(Long roleId, int limit) {
        return baseMapper.selectVisibleActivities(roleId, limit);
    }

    @Override
    public boolean publish(Long id) {
        SysActivity activity = getById(id);
        if (activity == null) return false;
        activity.setStatus(1);
        activity.setPublishTime(new Date());
        return updateById(activity);
    }

    @Override
    public boolean publishAndNotify(Long id) {
        SysActivity activity = getById(id);
        if (activity == null) return false;
        activity.setStatus(1);
        activity.setPublishTime(new Date());
        boolean ok = updateById(activity);
        if (ok) {
            pushActivityNotifyByRole(activity);
        }
        return ok;
    }

    @Override
    public boolean saveAndPublish(SysActivity activity) {
        activity.setStatus(1);
        activity.setPublishTime(new Date());
        return save(activity);
    }

    @Override
    public boolean saveAndPublishAndNotify(SysActivity activity) {
        activity.setStatus(1);
        activity.setPublishTime(new Date());
        boolean ok = save(activity);
        if (ok) {
            pushActivityNotifyByRole(activity);
        }
        return ok;
    }

    /**
     * 按角色精准推送活动通知（WebSocket）
     * 逻辑：
     *   1. 查询 sys_activity_role 获取该活动绑定的角色列表
     *   2. 若没有绑定角色 → 广播全员
     *   3. 若有绑定角色 → 查询 sys_user_role 获取对应用户ID → 定向推送
     */
    private void pushActivityNotifyByRole(SysActivity activity) {
        try {
            Map<String, Object> msg = new HashMap<>();
            msg.put("type", "activity");
            msg.put("id", activity.getId());
            msg.put("title", activity.getTitle());
            msg.put("content", activity.getContent());
            msg.put("icon", activity.getIcon());
            msg.put("color", activity.getColor());
            msg.put("activityType", activity.getActivityType());
            String json = objectMapper.writeValueAsString(msg);

            // 查询此活动绑定的角色
            List<Long> roleIds = activityRoleMapper.selectRoleIdsByActivityId(activity.getId());

            if (roleIds == null || roleIds.isEmpty()) {
                // 未限定角色，广播全员
                log.info("[WS推送] 活动ID={} 未绑定角色，广播全员", activity.getId());
                wsbroadcast(json);
            } else {
                // 查询拥有这些角色的用户
                List<Long> userIds = userRoleMapper.selectUserIdsByRoleIds(roleIds);
                if (userIds == null || userIds.isEmpty()) {
                    log.warn("[WS推送] 活动ID={} 绑定角色无对应用户", activity.getId());
                    return;
                }
                List<String> userIdStrs = userIds.stream()
                        .map(Object::toString).collect(Collectors.toList());
                log.info("[WS推送] 活动ID={} 定向推送 roleIds={} userIds={}",
                        activity.getId(), roleIds, userIdStrs);
                wspush(userIdStrs, json);
            }
        } catch (Exception e) {
            log.error("活动WebSocket推送失败", e);
        }
    }

    /**
     * 通过反射调用 ActivityWebSocketHandler.pushToUsers
     */
    private void wspush(List<String> userIds, String json) {
        try {
            Class<?> clazz = Class.forName("com.education.admin.websocket.ActivityWebSocketHandler");
            clazz.getMethod("pushToUsers", List.class, String.class).invoke(null, userIds, json);
        } catch (Exception e) {
            log.error("调用WS pushToUsers失败", e);
        }
    }

    /**
     * 通过反射调用 ActivityWebSocketHandler.broadcast
     */
    private void wsbroadcast(String json) {
        try {
            Class<?> clazz = Class.forName("com.education.admin.websocket.ActivityWebSocketHandler");
            clazz.getMethod("broadcast", String.class).invoke(null, json);
        } catch (Exception e) {
            log.error("调用WS broadcast失败", e);
        }
    }

    @Override
    public boolean end(Long id) {
        SysActivity activity = getById(id);
        if (activity == null) return false;
        activity.setStatus(2);
        return updateById(activity);
    }

    @Override
    public boolean toggleTop(Long id, Integer isTop) {
        SysActivity activity = getById(id);
        if (activity == null) return false;
        activity.setIsTop(isTop);
        return updateById(activity);
    }

    @Override
    public boolean updateSort(Long id, Integer sort) {
        SysActivity activity = getById(id);
        if (activity == null) return false;
        activity.setSort(sort);
        return updateById(activity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignActivityRoles(Long activityId, List<Long> roleIds) {
        activityRoleMapper.deleteByActivityId(activityId);
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                SysActivityRole activityRole = new SysActivityRole();
                activityRole.setActivityId(activityId);
                activityRole.setRoleId(roleId);
                activityRoleMapper.insert(activityRole);
            }
        }
        return true;
    }

    @Override
    public List<Long> getActivityRoleIds(Long activityId) {
        return activityRoleMapper.selectRoleIdsByActivityId(activityId);
    }
}
