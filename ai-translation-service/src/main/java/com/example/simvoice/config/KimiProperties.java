package com.example.simvoice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.kimi")
@Data
public class KimiProperties {
    /**
     * Moonshot API Key（建议用环境变量 MOONSHOT_API_KEY 注入）
     */
    private String apiKey;

    /**
     * OpenAI 兼容模式 base url
     * 默认：Moonshot API
     */
    private String baseUrl = "https://api.moonshot.cn/v1";

    /**
     * 模型名，示例：kimi-latest
     */
    private String model = "kimi-latest";

}

