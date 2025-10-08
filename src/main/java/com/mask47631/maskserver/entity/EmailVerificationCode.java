package com.mask47631.maskserver.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "email_verification_code")
@Comment("邮箱验证码表")
@Schema(description = "邮箱验证码实体")
public class EmailVerificationCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "主键ID")
    private Long id;

    @Column(name = "user_id", nullable = false)
    @Comment("用户ID")
    @Schema(description = "用户ID")
    private Long userId;

    @Column(name = "code", length = 6, nullable = false)
    @Comment("6位数字验证码")
    @Schema(description = "6位数字验证码")
    private String code;

    @Column(name = "expire_at", nullable = false)
    @Comment("验证码有效期")
    @Schema(description = "验证码有效期，格式为yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expireAt;

    @Column(name = "used", nullable = false)
    @Comment("验证码是否已使用")
    @Schema(description = "验证码是否已使用")
    private Boolean used = false;
}
