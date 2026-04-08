package com.education.system.event.listener;

import com.education.system.event.ActivityPublishedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * WebSocket通知监听器（观察者模式）
 * 
 * <p>监听活动发布事件，通过WebSocket推送实时通知。</p>
 */
@Slf4j
@Component
public class WebSocketNotificationListener {

    @EventListener
    public void onActivityPublished(ActivityPublishedEvent event) {
        log.info("[WebSocket通知] 活动发布: {} - {}, 推送角色: {}", 
                event.getActivityId(), event.getActivityTitle(), event.getRoleIds());
        
        // TODO: 调用WebSocket推送逻辑
        // notificationWebSocket.sendToRoles(event.getRoleIds(), message);
    }
}
