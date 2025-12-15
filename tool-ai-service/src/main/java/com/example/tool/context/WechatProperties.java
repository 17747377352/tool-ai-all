package com.example.tool.context;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信小程序配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechat")
public class WechatProperties {
    private String appid;
    private String secret;
    private String baseUrl = "https://api.weixin.qq.com";
}




