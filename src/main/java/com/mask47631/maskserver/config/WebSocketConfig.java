package com.mask47631.maskserver.config;

import com.mask47631.maskserver.websocket.AuthHandshakeInterceptor;
import com.mask47631.maskserver.websocket.ChatWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final AuthHandshakeInterceptor authHandshakeInterceptor;

    public WebSocketConfig(AuthHandshakeInterceptor authHandshakeInterceptor) {
        this.authHandshakeInterceptor = authHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册聊天WebSocket处理器，并添加鉴权拦截器
        registry.addHandler(new ChatWebSocketHandler(), "/ws/chat")
                .addInterceptors(authHandshakeInterceptor)
                .setAllowedOrigins("*");
    }
}