package com.example.simvoice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.kimi")
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

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}

