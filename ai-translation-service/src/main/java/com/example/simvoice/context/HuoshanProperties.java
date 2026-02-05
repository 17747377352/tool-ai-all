package com.example.simvoice.context;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 火山引擎配置属性
 * 
 * @author ai-translation-service
 * @since 1.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "huoshan")
public class HuoshanProperties {
    /**
     * 火山引擎API Key
     */
    private String apiKey;

    /**
     * 火山引擎识图/图像理解接口地址
     * 例如： https://xxx.volcengineapi.com/... （请根据实际文档配置）
     */
    private String ocrUrl;
}

