package com.mask47631.maskserver.controller;

import com.mask47631.maskserver.dto.ChatMessageDTO;
import com.mask47631.maskserver.entity.ChatMessage;
import com.mask47631.maskserver.entity.User;
import com.mask47631.maskserver.repository.ChatMessageRepository;
import com.mask47631.maskserver.repository.UserRepository;
import com.mask47631.maskserver.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Tag(name = "聊天消息接口", description = "聊天消息相关接口")
@RestController
@RequestMapping("/api/messages")
public class ChatMessageController {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    public ChatMessageController(ChatMessageRepository chatMessageRepository, UserRepository userRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
    }

    @Operation(summary = "获取比指定ID大的消息", description = "获取比指定ID大的消息，最多返回100条，按ID倒序排列")
    @GetMapping("/after")
    public ApiResponse<List<ChatMessageDTO>> getMessagesAfterId(
            @Parameter(description = "消息ID", required = true) 
            @RequestParam Long id) {
        
        // 直接查询比指定ID大的消息，按ID降序排列，最多获取100条记录
        Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "id"));
        
        // 查询比指定ID大的消息，按ID降序排列（最新的在前）
        List<ChatMessage> messages = chatMessageRepository.findByIdGreaterThanOrderByIdDesc(id, pageable);
        
        // 转换为DTO并补充发送者信息
        List<ChatMessageDTO> messageDTOs = convertToDTOs(messages);
        
        return ApiResponse.success(messageDTOs);
    }
    
    @Operation(summary = "获取比指定ID小的历史消息", description = "获取比指定ID小的历史消息，最多返回100条，按ID倒序排列")
    @GetMapping("/before")
    public ApiResponse<List<ChatMessageDTO>> getMessagesBeforeId(
            @Parameter(description = "消息ID", required = true) 
            @RequestParam Long id) {
        
        // 查询比指定ID小的消息，按ID降序排列（最新的在前），最多获取100条记录
        Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "id"));
        
        // 查询比指定ID小的消息，按ID降序排列
        List<ChatMessage> messages = chatMessageRepository.findByIdLessThanOrderByIdDesc(id, pageable);
        
        // 转换为DTO并补充发送者信息
        List<ChatMessageDTO> messageDTOs = convertToDTOs(messages);
        
        return ApiResponse.success(messageDTOs);
    }
    
    /**
     * 将ChatMessage实体列表转换为ChatMessageDTO列表，并补充发送者信息
     * @param messages 消息实体列表
     * @return 消息DTO列表
     */
    private List<ChatMessageDTO> convertToDTOs(List<ChatMessage> messages) {
        return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    /**
     * 将ChatMessage实体转换为ChatMessageDTO，并补充发送者信息
     * @param message 消息实体
     * @return 消息DTO
     */
    private ChatMessageDTO convertToDTO(ChatMessage message) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(message.getId());
        dto.setFromId(message.getFromId());
        dto.setFromName(message.getFromName());
        dto.setFromAvatar(message.getFromAvatar());
        dto.setToId(message.getToId());
        dto.setToName(message.getToName());
        dto.setContent(message.getContent());
        dto.setTimestamp(message.getTimestamp());
        dto.setCreatedAt(message.getCreatedAt());
        
        // 如果消息中没有用户名或头像，则从用户表中获取
        if (message.getFromId() != null) {
            Optional<User> userOptional = userRepository.findById(message.getFromId());
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                dto.setFromName(user.getUsername());
                dto.setFromAvatar(user.getAvatarUrl());
            }
        }
        
        return dto;
    }
}