package com.example.tool.config;

import com.example.tool.exception.BusinessException;
import com.example.tool.result.ResultCode;
import com.example.tool.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT拦截器
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 获取token
        String token = request.getHeader("Authorization");
        log.info("token:{}",token);
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

