package com.mask47631.maskserver.websocket;

import com.mask47631.maskserver.entity.User;
import com.mask47631.maskserver.exception.UnauthenticatedException;
import com.mask47631.maskserver.repository.UserRepository;
import com.mask47631.maskserver.util.TokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@Slf4j
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    private final UserRepository userRepository;

    @Value("${token.secret}")
    private String tokenSecret;

    @Value("${token.expire-minutes:30}")
    private long tokenExpireMinutes;

    public AuthHandshakeInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        log.debug("WebSocket handshake interceptor: beforeHandshake");

        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        // 从请求头中获取token
        String token = servletRequest.getServletRequest().getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            log.warn("WebSocket connection attempt without Authorization header");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        try {
            // 验证token
            String userId = TokenUtil.decrypt(token, tokenSecret);
            
            // 查找用户
            User user = userRepository.findById(Long.valueOf(userId)).orElse(null);
            if (user == null) {
                log.warn("User not found for userId: {}", userId);
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }
            
            // 将用户信息存储到attributes中，以便在handler中使用
            user.setPassword(null); // 清除密码信息
            attributes.put("user", user);
            
            log.info("WebSocket authentication successful for user: {}", user.getUsername());
            return true;
        } catch (Exception e) {
            log.warn("WebSocket authentication failed: {}", e.getMessage());
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        log.debug("WebSocket handshake interceptor: afterHandshake");
    }
}