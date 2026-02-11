package com.example.simvoice.context;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信支付配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechat.pay")
public class WechatPayProperties {
    /**
     * 商户号
     */
    private String mchId;
    
    /**
     * API密钥（用于签名）
     */
    private String apiKey;
    
    /**
     * 支付回调通知地址
     */
    private String notifyUrl;
    
    /**
     * 微信支付API地址
     */
    private String baseUrl = "https://api.mch.weixin.qq.com";
}

