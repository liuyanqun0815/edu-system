package com.education.system.event.listener;

import com.education.system.event.ActivityPublishedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 邮件通知监听器（观察者模式）
 * 
 * <p>监听活动发布事件，发送邮件通知给相关用户。</p>
 */
@Slf4j
@Component
public class EmailNotificationListener {

    @EventListener
    public void onActivityPublished(ActivityPublishedEvent event) {
        log.info("[邮件通知] 活动发布: {} - {}, 推送角色: {}", 
                event.getActivityId(), event.getActivityTitle(), event.getRoleIds());
        
        // TODO: 查询角色对应的用户邮箱，发送邮件通知
        // List<String> emails = userService.getEmailsByRoleIds(event.getRoleIds());
        // emails.forEach(email -> mailService.send(email, subject, content));
    }
}
