package com.example.tool.context;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 火山引擎配置属性
 * 
 * @author tool-ai-service
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




