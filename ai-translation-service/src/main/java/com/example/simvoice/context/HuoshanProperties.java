package com.example.simvoice.context;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 火山引擎/豆包统一配置属性
 *
 * 一个配置类同时承载：
 * - Ark OpenAI-Compatible 接口（chat/completions、images/generations 等）
 * - 火山 OCR / 图像理解接口地址
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

    /**
     * Ark OpenAI-Compatible base url（chat/completions、images/generations 等）
     * 例如：https://ark.cn-beijing.volces.com/api/v3
     */
    private String baseUrl ;

    /**
     * 生图模型名（豆包生图 / Seedream 系列，按控制台中开通的模型填写）
     */
    private String imageModel;

    /**
     * 豆包视觉理解模型名（图像理解/多模态）
     */
    private String visionModel;


}

