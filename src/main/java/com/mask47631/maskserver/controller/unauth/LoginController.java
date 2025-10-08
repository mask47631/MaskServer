package com.mask47631.maskserver.controller.unauth;

import com.mask47631.maskserver.entity.User;
import com.mask47631.maskserver.repository.UserRepository;
import com.mask47631.maskserver.repository.ConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Value;
import com.mask47631.maskserver.util.TokenUtil;
import com.mask47631.maskserver.util.ApiResponse;

import jakarta.servlet.http.HttpSession;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "登录注册接口", description = "用户登录、注册相关接口")
@RestController
@RequestMapping("/unauth")
public class LoginController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConfigRepository configRepository;

    @Value("${token.secret}")
    private String tokenSecret;

    @Value("${token.expire-minutes:30}")
    private long tokenExpireMinutes;

    @Operation(summary = "用户登录", description = "管理员登录，返回token和有效期")
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(
            @Parameter(description = "用户名", required = true) @RequestParam String username,
            @Parameter(description = "密码", required = true) @RequestParam String password) {
        User user = userRepository.findFirstByRole("admin")
                .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                .orElse(null);
        Map<String, Object> result = new HashMap<>();
        if (user == null) {
            return ApiResponse.fail("用户名或密码错误");
        } else if (Boolean.TRUE.equals(user.getDisabled())) {
            return ApiResponse.fail("账号已停用");
        } else {
            // 使用用户id生成加密token，带有效期
            String token = TokenUtil.encrypt(user.getId().toString(), tokenSecret, tokenExpireMinutes);
            result.put("token", token);
            result.put("expireMinutes", tokenExpireMinutes);
            return ApiResponse.success(result);
        }
    }

    @Operation(summary = "用户注册", description = "注册新用户，支持邮箱唯一校验和用户名唯一校验，注册成功返回token")
    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(
            @Parameter(description = "用户名", required = true) @RequestParam String username,
            @Parameter(description = "密码", required = true) @RequestParam String password,
            @Parameter(description = "邮箱", required = true) @RequestParam String email,
            HttpSession session) {
        // 邮箱唯一校验
        if (userRepository.findByEmail(email).isPresent()) {
            return ApiResponse.fail("邮箱已被注册");
        }
        // 用户名唯一校验
        if (userRepository.findByUsername(username).isPresent()) {
            return ApiResponse.fail("用户名已被注册");
        }
        // 查询配置表，判断是否需要邮箱认证
        boolean needEmailVerify = configRepository.findById("need_email_verify")
            .map(cfg -> "true".equalsIgnoreCase(cfg.getValue()))
            .orElse(false);
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setDisabled(needEmailVerify); // 需要邮箱认证则初始为停用，否则启用
        user.setVerified(false);
        user.setRole("user");
        userRepository.save(user);
        session.setAttribute("user", user);
        // 注册成功后生成token
        String token = TokenUtil.encrypt(user.getId().toString(), tokenSecret, tokenExpireMinutes);
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("expireMinutes", tokenExpireMinutes);
        return ApiResponse.success("注册成功！", result);
    }
}
