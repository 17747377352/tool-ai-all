package com.example.simvoice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.doubao")
public class DoubaoProperties {
    /**
     * 火山引擎 Ark API Key（建议用环境变量 ARK_API_KEY 注入）
     */
    private String apiKey;

    /**
     * OpenAI 兼容模式 base url（Ark）
     * 常见示例： https://ark.cn-beijing.volces.com/api/v3
     */
    private String baseUrl = "https://ark.cn-beijing.volces.com/api/v3";

    /**
     * 生图模型名（豆包生图/Seedream 系列，按你控制台里开通的模型填写）
     */
    private String model;

    private Integer defaultN = 1;
    private String defaultSize = "1024x1024";

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

    public Integer getDefaultN() {
        return defaultN;
    }

    public void setDefaultN(Integer defaultN) {
        this.defaultN = defaultN;
    }

    public String getDefaultSize() {
        return defaultSize;
    }

    public void setDefaultSize(String defaultSize) {
        this.defaultSize = defaultSize;
    }
}



