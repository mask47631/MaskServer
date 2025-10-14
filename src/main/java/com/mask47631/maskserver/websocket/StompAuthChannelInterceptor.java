package com.mask47631.maskserver.websocket;

import com.mask47631.maskserver.entity.User;
import com.mask47631.maskserver.exception.UnauthenticatedException;
import com.mask47631.maskserver.repository.UserRepository;
import com.mask47631.maskserver.util.TokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final UserRepository userRepository;

    @Value("${token.secret}")
    private String tokenSecret;

    @Value("${token.expire-minutes:30}")
    private long tokenExpireMinutes;

    public StompAuthChannelInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 从STOMP头部获取token
            String token = accessor.getFirstNativeHeader("Authorization");
            
            if (token == null || token.isEmpty()) {
                log.warn("STOMP connection attempt without Authorization header");
                throw new UnauthenticatedException("未登录，缺少token");
            }
            
            try {
                // 验证token
                String userId = TokenUtil.decrypt(token, tokenSecret);
                
                // 查找用户
                User user = userRepository.findById(Long.valueOf(userId)).orElse(null);
                if (user == null) {
                    log.warn("User not found for userId: {}", userId);
                    throw new UnauthenticatedException("用户不存在");
                }
                
                // 将用户信息存储在会话属性中
                accessor.getSessionAttributes().put("user", user);
                log.info("STOMP authentication successful for user: {}", user.getUsername());
            } catch (Exception e) {
                log.warn("STOMP authentication failed: {}", e.getMessage());
                throw new UnauthenticatedException("无效的token: " + e.getMessage());
            }
        }
        
        return message;
    }
}