package com.mask47631.maskserver.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "file_record")
@Schema(description = "文件记录实体")
@Comment("文件记录表")
public class FileRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "文件ID")
    private Long id;

    /**
     * 文件原始名称
     */
    @Comment("文件原始名称")
    @Column(name = "original_name", length = 255)
    @Schema(description = "文件原始名称")
    private String originalName;

    /**
     * 文件存储路径
     */
    @Comment("文件存储路径")
    @Column(name = "storage_path", length = 512)
    @Schema(description = "文件存储路径")
    private String storagePath;

    /**
     * 文件访问链接
     */
    @Comment("文件访问链接")
    @Column(name = "file_url", length = 512)
    @Schema(description = "文件访问链接")
    private String fileUrl;

    /**
     * 文件大小（字节）
     */
    @Comment("文件大小")
    @Column(name = "file_size")
    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    /**
     * 文件类型
     */
    @Comment("文件类型")
    @Column(name = "content_type", length = 100)
    @Schema(description = "文件类型")
    private String contentType;

    /**
     * 是否需要登录才能查看
     */
    @Comment("是否需要登录才能查看")
    @Column(name = "require_login")
    @Schema(description = "是否需要登录才能查看")
    private Boolean requireLogin = true;

    /**
     * 上传者用户ID
     */
    @Comment("上传者用户ID")
    @Column(name = "uploader_id")
    @Schema(description = "上传者用户ID")
    private Long uploaderId;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    // getter/setter 省略
}