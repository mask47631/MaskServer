package com.mask47631.maskserver.auth;

import com.mask47631.maskserver.entity.User;
import com.mask47631.maskserver.exception.ForbiddenException;
import com.mask47631.maskserver.exception.UnauthenticatedException;
import com.mask47631.maskserver.repository.UserRepository;
import com.mask47631.maskserver.util.TokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.aspectj.lang.JoinPoint;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AdminRequiredAspect {

    @Value("${token.secret}")
    private String tokenSecret;
    @Value("${token.expire-minutes:30}")
    private long tokenExpireMinutes;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HttpServletResponse response;

    @Pointcut("@within(com.mask47631.maskserver.auth.AdminRequired) || @annotation(com.mask47631.maskserver.auth.AdminRequired)")
    public void adminRequiredPointcut() {}

    @Before("adminRequiredPointcut()")
    public void checkAdmin(JoinPoint joinPoint) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new ForbiddenException("无法获取请求上下文");
        }
        HttpServletRequest request = attrs.getRequest();
        HttpSession session = request.getSession(false);
        // token校验
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            throw new UnauthenticatedException("未登录，缺少token");
        }
        String userId;
        try {
            userId = TokenUtil.decrypt(token, tokenSecret);
            // 刷新token
            String newToken = TokenUtil.encrypt(userId, tokenSecret, tokenExpireMinutes);
            response.setHeader("X-Refreshed-Token", newToken);
        } catch (Exception e) {
            throw new UnauthenticatedException("无效的token");
        }
        User user = (User) session.getAttribute("user");
        if (user == null) {
            user = userRepository.findById(Long.valueOf(userId)).orElse(null);
            if (user == null) {
                throw new UnauthenticatedException("用户不存在");
            }
            user.setPassword(null);
            session.setAttribute("user", user);
        }
        // 校验session中user
        if (user == null || !"admin".equals(user.getRole())) {
            throw new ForbiddenException("无权访问服务器信息");
        }
    }
}
