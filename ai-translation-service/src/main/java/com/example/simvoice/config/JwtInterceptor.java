package com.example.simvoice.config;

import com.example.simvoice.exception.BusinessException;
import com.example.simvoice.result.ResultCode;
import com.example.simvoice.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * JWT拦截器
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    /**
     * 白名单路径（与 WebMvcConfig 保持一致，作为双重保险）
     */
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/auth/wx-login",
            "/error",
            "/swagger-ui",
            "/v3/api-docs",
            "/favicon.ico",
            "/",
            "/actuator",
            "/health",
            "/proxy",
            "/api/image",
            "/test",
            "/onlyoffice",
            "/onlyoffice-editor.html",
            "/.well-known"
    );

    /**
     * 检查路径是否在白名单中
     */
    private boolean isWhiteList(String path) {
        if (path == null) {
            return false;
        }
        for (String whitePath : WHITE_LIST) {
            if (path.equals(whitePath) || path.startsWith(whitePath + "/")) {
                return true;
            }
        }
        // 检查静态资源
        if (path.endsWith(".html") || path.endsWith(".js") || path.endsWith(".css") 
                || path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".ico")) {
            return true;
        }
        return false;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestURI = request.getRequestURI();
//        log.info("JWT拦截器: url={}, token={}", requestURI, request.getHeader("Authorization"));
        
        // 检查是否在白名单中
        if (isWhiteList(requestURI)) {
            log.debug("路径在白名单中，跳过JWT验证: {}", requestURI);
            return true;
        }
        
        // 获取token
        String token = request.getHeader("Authorization");
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            token = request.getParameter("token");
        }

        if (!StringUtils.hasText(token)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 验证token
        if (!JwtUtil.verifyToken(token)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        // 检查是否过期
        if (JwtUtil.isTokenExpired(token)) {
            throw new BusinessException(ResultCode.TOKEN_EXPIRED);
        }

        // 将openid存入request，方便后续使用
        String openid = JwtUtil.getOpenidFromToken(token);
        request.setAttribute("openid", openid);

        return true;
    }
}

