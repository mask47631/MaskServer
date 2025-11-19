package com.mask47631.maskserver.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "chat_message")
@Schema(description = "聊天消息实体")
@Comment("聊天消息表")
public class ChatMessage {
    /**
     * 消息ID，自增主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    @Schema(description = "消息ID")
    @Comment("消息ID，自增主键")
    private Long id;

    /**
     * 发送者用户ID
     */
    @Column(name = "sender_id")
    @Schema(description = "发送者用户ID")
    @Comment("发送者用户ID")
    private Long fromId;

    /**
     * 发送者用户名
     */
    @Column(name = "sender_name")
    @Schema(description = "发送者用户名")
    @Comment("发送者用户名")
    private String fromName;

    /**
     * 发送者头像URL
     */
    @Column(name = "sender_avatar")
    @Schema(description = "发送者头像URL")
    @Comment("发送者头像URL")
    private String fromAvatar;

    /**
     * 接收者用户ID（私聊时使用）
     */
    @Column(name = "receiver_id")
    @Schema(description = "接收者用户ID")
    @Comment("接收者用户ID（私聊时使用）")
    private Long toId;

    /**
     * 接收者用户名（私聊时使用）
     */
    @Column(name = "receiver_name")
    @Schema(description = "接收者用户名")
    @Comment("接收者用户名（私聊时使用）")
    private String toName;

    /**
     * 消息内容
     */
    @Column(name = "content", length = 10000)
    @Schema(description = "消息内容")
    @Comment("消息内容")
    private String content;

    /**
     * 时间戳
     */
    @Column(name = "timestamp")
    @Schema(description = "时间戳")
    @Comment("时间戳")
    private String timestamp;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at")
    @Schema(description = "创建时间")
    @Comment("创建时间")
    private LocalDateTime createdAt;

    /**
     * 消息类型
     */
    @Column(name = "message_type")
    @Schema(description = "消息类型")
    @Comment("消息类型")
    private String messageType;
}