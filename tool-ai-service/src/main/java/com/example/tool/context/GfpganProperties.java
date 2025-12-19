package com.example.tool.context;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * GFPGAN 配置
 * 用于云端 GPU 修复服务的访问配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "gfpgan")
public class GfpganProperties {
    /**
     * 修复服务接口地址，如：https://gpu.example.com/gfpgan/restore
     */
    private String endpoint;

    /**
     * 可选的鉴权 Token（未配置则不加 Authorization 头）
     */
    private String apiKey;

    /**
     * 请求超时时间，毫秒
     */
    private Integer timeoutMillis = 120000;

    /**
     * Replicate 模型版本ID（完整哈希）
     */
    private String modelVersionId = "tencentarc/gfpgan:0fbacf7afc6c144e5be9767cff80f25aff23e52b0708f17e20f9879b2f21516c";

    /**
     * GFPGAN 内部版本参数，如 v1.4
     */
    private String modelVersion = "v1.4";

    /**
     * 放大倍数（1-4），默认3
     */
    private Double scale = 3.0;
}

