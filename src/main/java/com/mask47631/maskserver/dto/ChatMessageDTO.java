package com.mask47631.maskserver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "聊天消息DTO")
public class ChatMessageDTO {
    /**
     * 消息ID
     */
    @Schema(description = "消息ID")
    private Long id;

    /**
     * 发送者用户ID
     */
    @Schema(description = "发送者用户ID")
    private Long fromId;

    /**
     * 发送者用户名
     */
    @Schema(description = "发送者用户名")
    private String fromName;

    /**
     * 发送者头像URL
     */
    @Schema(description = "发送者头像URL")
    private String fromAvatar;

    /**
     * 接收者用户ID（私聊时使用）
     */
    @Schema(description = "接收者用户ID")
    private Long toId;

    /**
     * 接收者用户名（私聊时使用）
     */
    @Schema(description = "接收者用户名")
    private String toName;

    /**
     * 消息内容
     */
    @Schema(description = "消息内容")
    private String content;

    /**
     * 时间戳
     */
    @Schema(description = "时间戳")
    private String timestamp;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}