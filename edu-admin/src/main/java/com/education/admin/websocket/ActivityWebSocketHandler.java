package com.education.admin.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 活动 WebSocket Handler
 * 维护 userId -> WebSocketSession 映射，支持精准推送和广播
 */
@Slf4j
public class ActivityWebSocketHandler extends TextWebSocketHandler {

    /** userId -> 会话列表（同一用户可能多开标签页） */
    private static final Map<String, CopyOnWriteArrayList<WebSocketSession>> sessions =
            new ConcurrentHashMap<>();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = getUserId(session);
        sessions.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(session);
        log.info("WebSocket连接建立: userId={}, sessionId={}", userId, session.getId());

        // 发送欢迎心跳
        try {
            session.sendMessage(new TextMessage("{\"type\":\"connected\",\"msg\":\"连接成功\"}"));
        } catch (IOException e) {
            log.warn("发送连接确认失败", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userId = getUserId(session);
        CopyOnWriteArrayList<WebSocketSession> list = sessions.get(userId);
        if (list != null) {
            list.remove(session);
            if (list.isEmpty()) {
                sessions.remove(userId);
            }
        }
        log.info("WebSocket连接关闭: userId={}, status={}", userId, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.warn("WebSocket传输异常: sessionId={}", session.getId(), exception);
        try {
            if (session.isOpen()) session.close();
        } catch (IOException ignored) {}
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 处理客户端心跳 ping
        if ("ping".equals(message.getPayload())) {
            try {
                session.sendMessage(new TextMessage("pong"));
            } catch (IOException e) {
                log.warn("发送pong失败", e);
            }
        }
    }

    // ==================== 静态推送方法 ====================

    /**
     * 向指定用户列表精准推送
     */
    public static void pushToUsers(List<String> userIds, String jsonMessage) {
        for (String userId : userIds) {
            CopyOnWriteArrayList<WebSocketSession> list = sessions.get(userId);
            if (list == null) continue;
            for (WebSocketSession session : list) {
                sendSafe(session, jsonMessage);
            }
        }
    }

    /**
     * 广播给所有在线用户
     */
    public static void broadcast(String jsonMessage) {
        sessions.forEach((userId, list) -> {
            for (WebSocketSession session : list) {
                sendSafe(session, jsonMessage);
            }
        });
    }

    /**
     * 获取当前在线用户ID列表
     */
    public static Map<String, CopyOnWriteArrayList<WebSocketSession>> getOnlineSessions() {
        return sessions;
    }

    private static void sendSafe(WebSocketSession session, String message) {
        if (session == null || !session.isOpen()) return;
        try {
            session.sendMessage(new TextMessage(message));
        } catch (IOException e) {
            log.warn("WebSocket消息发送失败: sessionId={}", session.getId(), e);
        }
    }

    private String getUserId(WebSocketSession session) {
        Object uid = session.getAttributes().get("userId");
        return uid != null ? uid.toString() : "anonymous";
    }
}
