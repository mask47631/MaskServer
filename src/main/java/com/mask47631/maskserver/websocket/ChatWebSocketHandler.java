package com.mask47631.maskserver.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mask47631.maskserver.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 获取经过身份验证的用户信息
        User user = (User) session.getAttributes().get("user");
        sessions.put(session.getId(), session);
        log.info("WebSocket connection established for user: {} ({})", 
                user != null ? user.getUsername() : "unknown", session.getId());
        
        // 发送欢迎消息
        if (user != null) {
            sendMessage(session, "Welcome to the chat, " + user.getUsername() + "!");
        } else {
            sendMessage(session, "Welcome to the chat!");
        }
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        User user = (User) session.getAttributes().get("user");
        String username = user != null ? user.getUsername() : "Anonymous";
        String payload = message.getPayload();
        log.info("Received message: {} from user: {} session: {}", payload, username, session.getId());
        
        // 广播消息给所有连接的客户端
        broadcastMessage(username + ": " + payload);
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        User user = (User) session.getAttributes().get("user");
        String username = user != null ? user.getUsername() : "unknown";
        sessions.remove(session.getId());
        log.info("WebSocket connection closed for user: {} ({}) with status: {}", username, session.getId(), status);
    }
    
    private void sendMessage(WebSocketSession session, String message) throws Exception {
        session.sendMessage(new TextMessage(message));
    }
    
    private void broadcastMessage(String message) throws Exception {
        for (WebSocketSession session : sessions.values()) {
            if (session.isOpen()) {
                sendMessage(session, message);
            }
        }
    }
}