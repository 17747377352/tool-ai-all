package com.example.tool.service;

/**
 * DataAnalysis服务接口
 * 调用dataAnalysis-backend项目的API接口获取视频信息
 *
 * @author tool-ai-service
 * @since 1.0
 */
public interface DataAnalysisService {

    /**
     * 通过dataAnalysis-backend项目获取视频信息
     *
     * @param shareUrl 分享链接
     * @return 无水印视频URL
     */
    String getVideoUrl(String shareUrl);
}



