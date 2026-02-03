package com.example.simvoice.context;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 小牛翻译配置属性
 * 
 * @author ai-translation-service
 * @since 1.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "niutrans")
public class NiuTransProperties {
    /**
     * 小牛翻译AppID（应用唯一标识）
     */
    private String appId;
    
    /**
     * 小牛翻译APIKey
     */
    private String apiKey;
}

