package com.mask47631.maskserver.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user")
@Schema(description = "用户实体")
@Comment("用户表")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Schema(description = "用户ID")
    private Long id;

    /**
     * 用户名
     */
    @Comment("用户名")
    @Column(name = "username", length = 255)
    @Schema(description = "用户名")
    private String username;

    /**
     * 密码
     */
    @Comment("密码")
    @Column(name = "password", length = 255)
    @Schema(description = "密码")
    private String password;

    /**
     * 邮箱
     */
    @Comment("邮箱")
    @Column(name = "email", length = 255)
    @Schema(description = "邮箱")
    private String email;

    /**
     * 停用（true=停用，false=启用）
     */
    @Comment("停用")
    @Column(name = "disabled")
    @Schema(description = "停用（true=停用，false=启用）")
    private Boolean disabled = true;

    /**
     * 验证（true=已验证，false=未验证）
     */
    @Comment("验证")
    @Column(name = "verified")
    @Schema(description = "验证（true=已验证，false=未验证）")
    private Boolean verified = false;

    /**
     * 权限（如：admin/user等）
     */
    @Comment("权限")
    @Column(name = "role", length = 50)
    @Schema(description = "权限（如：admin/user等）")
    private String role = "user";

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    /**
     * 头像URL
     */
    @Comment("头像URL")
    @Column(name = "avatar_url", length = 512)
    @Schema(description = "头像URL")
    private String avatarUrl;

    // getter/setter 省略
}
