package com.mask47631.maskserver.controller;

import com.mask47631.maskserver.entity.ChatMessage;
import com.mask47631.maskserver.entity.User;
import com.mask47631.maskserver.repository.ChatMessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Controller
@Slf4j
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;

    public WebSocketController(SimpMessagingTemplate messagingTemplate, 
                              ChatMessageRepository chatMessageRepository) {
        this.messagingTemplate = messagingTemplate;
        this.chatMessageRepository = chatMessageRepository;
    }

    @MessageMapping("/chat")
    public void chat(@Payload ChatMessage message, SimpMessageHeaderAccessor headerAccessor) {
        // 从会话属性中获取用户信息
        User user = (User) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("user");
        String username = user != null ? user.getUsername() : "Anonymous";
        Long userId = user != null ? user.getId() : null;
        String avatarUrl = user != null ? user.getAvatarUrl() : null;

        log.info("Received message: {} from user: {}", message, username);
        message.setFromId(userId);
        message.setFromName(username);
        message.setFromAvatar(avatarUrl);
        message.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        if(message.getMessageType() == null || message.getMessageType().isEmpty()) {
            message.setMessageType("chat");
        }
        if (!"webrtc".equals(message.getMessageType())) {
            // 保存消息到数据库
            chatMessageRepository.save(message);
        }
        // 广播消息
        messagingTemplate.convertAndSend("/topic/messages", message);
    }

    @MessageMapping("/private")
    @SendToUser("/queue/messages")
    public ChatMessage privateChat(@Payload ChatMessage message, SimpMessageHeaderAccessor headerAccessor) {
        // 从会话属性中获取用户信息
        User user = (User) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("user");
        String username = user != null ? user.getUsername() : "Anonymous";
        Long userId = user != null ? user.getId() : null;
        String avatarUrl = user != null ? user.getAvatarUrl() : null;

        log.info("Received private message: {} from user: {}", message, username);
        message.setFromId(userId);
        message.setFromName(username);
        message.setFromAvatar(avatarUrl);
        message.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // 保存消息到数据库
        chatMessageRepository.save(message);

        // 发送到特定用户的队列，使用用户ID作为标识
        if (message.getToId() != null) {
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(message.getToId()),  // 使用用户ID作为发送目标
                    "/queue/messages",
                    message
            );
        }
        return message;
    }
}