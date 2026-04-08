package com.education.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.common.result.PageResult;
import com.education.system.entity.SysActivity;
import com.education.system.query.ActivityQuery;

import java.util.List;

/**
 * 活动管理服务接口
 */
public interface ISysActivityService extends IService<SysActivity> {

    /**
     * 分页查询活动列表
     */
    PageResult<SysActivity> pageList(ActivityQuery query);

    /**
     * 查询已发布的活动列表
     */
    List<SysActivity> getPublishedList();

    /**
     * 查询最新的N条活动
     */
    List<SysActivity> getLatestList(int limit);

    /**
     * 查询置顶的活动列表
     */
    List<SysActivity> getTopList();

    /**
     * 根据角色ID查询活动列表
     */
    List<SysActivity> getByRoleId(Long roleId, int limit);

    /**
     * 发布活动
     */
    boolean publish(Long id);

    /**
     * 结束活动
     */
    boolean end(Long id);

    /**
     * 置顶/取消置顶活动
     */
    boolean toggleTop(Long id, Integer isTop);

    /**
     * 更新活动排序
     */
    boolean updateSort(Long id, Integer sort);

    /**
     * 保存并发布活动
     */
    boolean saveAndPublish(SysActivity activity);

    /**
     * 按角色可见性查询活动（roleId为null=全员可见的活动，否则查该角色可见的+全员可见的）
     */
    List<SysActivity> getVisibleActivities(Long roleId, int limit);

    /**
     * 发布活动并SSE推送通知
     */
    boolean publishAndNotify(Long id);

    /**
     * 保存并发布活动并SSE推送通知
     */
    boolean saveAndPublishAndNotify(SysActivity activity);

    /**
     * 分配活动推送角色
     */
    boolean assignActivityRoles(Long activityId, List<Long> roleIds);

    /**
     * 获取活动的推送角色ID列表
     */
    List<Long> getActivityRoleIds(Long activityId);
}
