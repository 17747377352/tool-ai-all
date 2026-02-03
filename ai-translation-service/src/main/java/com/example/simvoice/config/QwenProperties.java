package com.example.simvoice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.qwen")
public class QwenProperties {
    /**
     * DashScope API Key（建议用环境变量 DASHSCOPE_API_KEY 注入）
     */
    private String apiKey;

    /**
     * OpenAI 兼容模式 base url
     * 默认：DashScope compatible-mode
     */
    private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";

    /**
     * 模型名（示例：qwen-plus / qwen-turbo 等）
     */
    private String model = "qwen-plus";

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



