package com.example.tool.service;

/**
 * DataAnalysis服务接口
 * 调用dataAnalysis-backend项目的API接口获取视频信息
 * 用于支持抖音、小红书等平台的视频去水印功能
 *
 * @author tool-ai-service
 * @since 1.0
 */
public interface DataAnalysisService {

    /**
     * 通过dataAnalysis-backend项目获取无水印视频URL
     * 
     * @param shareUrl 分享链接（抖音或小红书的分享链接）
     * @return 无水印视频URL
     */
    String getVideoUrl(String shareUrl);
}



