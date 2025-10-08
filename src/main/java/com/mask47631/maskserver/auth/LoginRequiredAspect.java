package com.mask47631.maskserver.auth;

import com.mask47631.maskserver.util.TokenUtil;
import com.mask47631.maskserver.entity.User;
import com.mask47631.maskserver.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.mask47631.maskserver.exception.UnauthenticatedException;

@Aspect
@Component
public class LoginRequiredAspect {
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private UserRepository userRepository;
    @Value("${token.secret}")
    private String tokenSecret;
    @Value("${token.expire-minutes:30}")
    private long tokenExpireMinutes;
    @Autowired
    private HttpServletResponse response;

    @Around("@within(com.mask47631.maskserver.auth.LoginRequired) || @annotation(com.mask47631.maskserver.auth.LoginRequired)")
    public Object checkLogin(ProceedingJoinPoint joinPoint) throws Throwable {
        String token = request.getHeader("Authorization");
        if (token == null) {
            throw new UnauthenticatedException("未登录或登录已失效");
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
        HttpSession session = request.getSession();
        Object sessionUser = session.getAttribute("user");
        if (sessionUser == null) {
            User user = userRepository.findById(Long.valueOf(userId)).orElse(null);
            if (user == null) {
                throw new UnauthenticatedException("用户不存在");
            }
            user.setPassword(null);
            session.setAttribute("user", user);
        }
        return joinPoint.proceed();
    }
}
