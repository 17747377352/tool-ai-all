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
}

