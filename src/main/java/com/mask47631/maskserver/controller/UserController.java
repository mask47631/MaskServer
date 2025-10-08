package com.mask47631.maskserver.controller;

import com.mask47631.maskserver.auth.LoginRequired;
import com.mask47631.maskserver.entity.EmailVerificationCode;
import com.mask47631.maskserver.entity.User;
import com.mask47631.maskserver.repository.EmailVerificationCodeRepository;
import com.mask47631.maskserver.repository.UserRepository;
import com.mask47631.maskserver.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Random;
import jakarta.mail.internet.MimeMessage;

@Tag(name = "用户相关接口", description = "用户信息、邮箱验证码等接口")
@RestController
@RequestMapping("/user")
@LoginRequired
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailVerificationCodeRepository emailVerificationCodeRepository;
    @Value("${mail.from:no-reply@maskserver.local}")
    private String mailFrom;
    @Autowired(required = false)
    private jakarta.mail.Session mailSession;

    /**
     * 获取当前登录用户的详细信息
     * 约定token格式为 username-token
     */
    @Operation(summary = "获取当前登录用户的详细信息")
    @GetMapping("/getSelf")
    public ApiResponse<User> getSelf(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.fail("未登录");
        }
        User dbUser = userRepository.findById(user.getId())
                .orElse(null);
        if (dbUser == null) {
            return ApiResponse.fail("用户不存在");
        }
        dbUser.setPassword(null);
        session.setAttribute("user", dbUser);
        return ApiResponse.success(dbUser);
    }

    /**
     * 向用户邮箱发送6位数字验证码
     */
    @Operation(summary = "向用户邮箱发送6位数字验证码")
    @PostMapping("/sendEmailCode")
    public ApiResponse<Void> sendEmailCode(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.fail("未登录");
        }
        String code = String.format("%06d", new Random().nextInt(1000000));
        EmailVerificationCode entity = new EmailVerificationCode();
        entity.setUserId(user.getId());
        entity.setCode(code);
        entity.setExpireAt(LocalDateTime.now().plusMinutes(10));
        entity.setUsed(false);
        emailVerificationCodeRepository.save(entity);
        // 发送邮件
        try {
            if (mailSession == null) throw new Exception("未配置邮件服务");
            MimeMessage message = new MimeMessage(mailSession);
            message.setFrom(new jakarta.mail.internet.InternetAddress(mailFrom));
            message.setRecipients(jakarta.mail.Message.RecipientType.TO, jakarta.mail.internet.InternetAddress.parse(user.getEmail()));
            message.setSubject("您的邮箱验证码");
            message.setText("您的验证码为：" + code + "，10分钟内有效。");
            jakarta.mail.Transport.send(message);
            return ApiResponse.success("验证码已发送", null);
        } catch (Exception e) {
            return ApiResponse.fail("邮件发送失败: " + e.getMessage());
        }
    }

    /**
     * 校验邮箱验证码是否正确，需校验用户邮箱和验证码邮箱是否一致
     */
    @Operation(summary = "校验邮箱验证码是否正确，需校验用户邮箱和验证码邮箱是否一致")
    @PostMapping("/verifyEmailCode")
    public ApiResponse<Void> verifyEmailCode(HttpSession session, @Parameter(description = "邮箱验证码", required = true) String code) {
        User user = (User) session.getAttribute("user");
        if (user.getVerified() != null && user.getVerified() && Boolean.TRUE.equals(user.getDisabled())) {
            return ApiResponse.fail("该用户已被停用");
        }
        EmailVerificationCode entity = emailVerificationCodeRepository.findAll().stream()
                .filter(e -> e.getUserId().equals(user.getId()) && !e.getUsed() && e.getCode().equals(code) && e.getExpireAt().isAfter(LocalDateTime.now()))
                .findFirst().orElse(null);
        if (entity == null) {
            return ApiResponse.fail("验证码无效或已过期");
        }
        if (!user.getEmail().equals(userRepository.findById(entity.getUserId()).map(User::getEmail).orElse(""))) {
            return ApiResponse.fail("用户邮箱与验证码邮箱不一致");
        }
        entity.setUsed(true);
        emailVerificationCodeRepository.save(entity);
        user.setDisabled(false);
        user.setVerified(true);
        userRepository.save(user);
        session.setAttribute("user", user);
        return ApiResponse.success("验证通过", null);
    }
}
