package com.education.admin.config;

import com.education.admin.websocket.ActivityWebSocketHandler;
import com.education.admin.websocket.ActivityWebSocketInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 配置
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Bean
    public ActivityWebSocketHandler activityWebSocketHandler() {
        return new ActivityWebSocketHandler();
    }

    @Bean
    public ActivityWebSocketInterceptor activityWebSocketInterceptor() {
        return new ActivityWebSocketInterceptor();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(activityWebSocketHandler(), "/ws/activity")
                .addInterceptors(activityWebSocketInterceptor())
                .setAllowedOrigins("*");
    }
}
