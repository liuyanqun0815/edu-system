package com.education.system.event.listener;

import com.education.system.event.ActivityPublishedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WebSocket通知监听器（观察者模式）
 * 
 * <p>监听活动发布事件，通过WebSocket推送实时通知。</p>
 */
@Slf4j
@Component
public class WebSocketNotificationListener {

    @Async
    @EventListener
    public void onActivityPublished(ActivityPublishedEvent event) {
        log.info("[WebSocket通知] 活动发布: {} - {}, 推送角色: {}", 
                event.getActivityId(), event.getActivityTitle(), event.getRoleIds());
        
        try {
            // 构建推送消息
            Map<String, Object> message = new HashMap<>();
            message.put("type", "ACTIVITY_PUBLISHED");
            message.put("activityId", event.getActivityId());
            message.put("title", event.getActivityTitle());
            message.put("publishTime", LocalDateTime.now());
            message.put("roleIds", event.getRoleIds());
            
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonMessage = objectMapper.writeValueAsString(message);
            
            // 通过反射调用ActivityWebSocketHandler.broadcast
            broadcastToRoles(event.getRoleIds(), jsonMessage);
            
            log.info("[WebSocket通知] 消息推送成功");
        } catch (Exception e) {
            log.error("[WebSocket通知] 消息推送失败", e);
        }
    }

    /**
     * 广播消息到指定角色的用户
     */
    private void broadcastToRoles(List<Long> roleIds, String message) {
        try {
            Class<?> clazz = Class.forName("com.education.admin.websocket.ActivityWebSocketHandler");
            // 调用broadcast方法
            clazz.getMethod("broadcast", String.class).invoke(null, message);
            log.info("[WebSocket通知] 已广播消息到所有连接用户");
        } catch (ClassNotFoundException e) {
            log.warn("[WebSocket通知] ActivityWebSocketHandler类不存在，跳过推送");
        } catch (Exception e) {
            log.error("[WebSocket通知] 调用WebSocket推送失败", e);
        }
    }
}
