package com.example.tool.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * Web配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    /**
     * 白名单路径
     */
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/auth/wx-login",
            "/error",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/favicon.ico",
            "/",
            "/actuator/**",
            "/health",
            "/proxy/**", // 外部图片代理接口（兼容旧版本）
            "/api/image/**" // 本地图片代理接口，允许匿名访问
    );

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(WHITE_LIST);
    }
}
