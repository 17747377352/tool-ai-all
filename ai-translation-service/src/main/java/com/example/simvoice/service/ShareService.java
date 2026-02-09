package com.example.simvoice.service;

import java.util.Map;

/**
 * 分享服务接口
 * 提供分享功能相关服务
 * 类型说明：1-去水印 2-生成图片 3-老照片修复 4-AI识图+翻译 5-即时翻译
 * 
 * @author ai-translation-service
 * @since 1.0
 */
public interface ShareService {
    /**
     * 记录分享并给分享人增加额度
     * 
     * @param sharerOpenid 分享人openid（邀请人）
     * @param inviteeOpenid 被邀请人openid
     * @param type 功能类型：1-去水印 2-生成图片 3-老照片修复 4-AI识图+翻译 5-即时翻译
     * @param rewardCount 奖励次数，默认10次
     * @return 返回结果，包含奖励信息等
     */
    Map<String, Object> recordShare(String sharerOpenid, String inviteeOpenid, Integer type, Integer rewardCount);
}



