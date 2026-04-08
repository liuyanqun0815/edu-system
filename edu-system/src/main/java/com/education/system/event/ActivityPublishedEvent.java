package com.education.system.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 活动发布事件（观察者模式）
 * 
 * <p>当活动发布时触发，通知所有订阅者（WebSocket、邮件、站内信等）。</p>
 * 
 * <h3>使用示例：</h3>
 * <pre>
 * // 发布事件
 * ActivityPublishedEvent event = new ActivityPublishedEvent(this, activity);
 * applicationEventPublisher.publishEvent(event);
 * 
 * // 监听事件
 * @EventListener
 * public void onActivityPublished(ActivityPublishedEvent event) {
 *     // 处理通知逻辑
 * }
 * </pre>
 */
@Getter
public class ActivityPublishedEvent extends ApplicationEvent {

    /**
     * 活动ID
     */
    private final Long activityId;

    /**
     * 活动标题
     */
    private final String activityTitle;

    /**
     * 活动类型
     */
    private final Integer activityType;

    /**
     * 推送角色ID列表
     */
    private final java.util.List<Long> roleIds;

    /**
     * 构造函数
     * 
     * @param source 事件源
     * @param activityId 活动ID
     * @param activityTitle 活动标题
     * @param activityType 活动类型
     * @param roleIds 推送角色ID列表
     */
    public ActivityPublishedEvent(Object source, Long activityId, String activityTitle, 
                                   Integer activityType, java.util.List<Long> roleIds) {
        super(source);
        this.activityId = activityId;
        this.activityTitle = activityTitle;
        this.activityType = activityType;
        this.roleIds = roleIds;
    }
}
