package com.example.simvoice.config;

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
            "/api/image/**", // 本地图片代理接口，允许匿名访问
            "/test/**", // 测试接口，允许匿名访问
            "/onlyoffice/**", // OnlyOffice 接口，允许匿名访问（OnlyOffice Document Server 需要访问）
            "/tool/asr/recognize-file/**",
            "/tool/asr/submit-task/**",
            "/api/test/**"
    );

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 开启 JWT 拦截器，除白名单外其余接口都要求携带有效的 Bearer Token，
        // 由 JwtInterceptor 解析出 openid 存入 request attribute。
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(WHITE_LIST);
    }
}

