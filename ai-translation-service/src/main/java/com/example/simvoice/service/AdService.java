package com.example.simvoice.service;

import java.util.Map;

/**
 * 广告服务接口
 * 提供广告观看记录和奖励功能
 * 
 * @author ai-translation-service
 * @since 1.0
 */
public interface AdService {
    /**
     * 记录广告观看并增加使用次数
     * 
     * @param openid 用户openid
     * @param type 功能类型：1-去水印 2-生成图片 3-老照片修复 4-AI识图+翻译 5-即时翻译
     * @param rewardCount 奖励次数，默认10次
     * @return 返回结果，包含实际减少的次数和剩余可用次数等信息
     */
    Map<String, Object> recordAdWatch(String openid, Integer type, Integer rewardCount);
}

