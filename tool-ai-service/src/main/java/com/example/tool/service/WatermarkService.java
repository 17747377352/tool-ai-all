package com.example.tool.service;

/**
 * 去水印服务接口
 * 支持抖音、小红书等平台的视频去水印
 * 
 * @author tool-ai-service
 * @since 1.0
 */
public interface WatermarkService {
    /**
     * 去除视频水印
     * 根据分享链接自动识别平台并去除水印
     * 
     * @param shareUrl 分享链接（抖音或小红书的分享链接）
     * @return 无水印视频URL
     */
    String removeWatermark(String shareUrl);

}



