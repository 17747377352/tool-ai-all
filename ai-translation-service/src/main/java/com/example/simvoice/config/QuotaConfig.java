package com.example.simvoice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 额度配置类
 * 统一管理用户额度相关的配置，方便后期调整
 * 
 * @author ai-translation-service
 * @since 1.0
 */
@Configuration
@ConfigurationProperties(prefix = "quota")
@Data
public class QuotaConfig {
    
    /**
     * 新用户默认初始化额度
     */
    private int defaultInitQuota = 3;
    
    /**
     * 分享增加额度（每次分享获得的额度）
     */
    private int shareRewardQuota = 3;
    
    /**
     * 广告观看奖励额度（每次观看广告获得的额度）
     */
    private int adRewardQuota = 3;

}

