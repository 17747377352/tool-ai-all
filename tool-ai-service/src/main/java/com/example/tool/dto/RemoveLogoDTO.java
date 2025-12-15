package com.example.tool.dto;

import lombok.Data;

/**
 * 去水印请求DTO
 * 支持两种方式：
 * 1. 分享链接：粘贴抖音或小红书的分享链接
 * 2. 视频URL：直接提供视频URL（保留原有功能）
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@Data
public class RemoveLogoDTO {
    /**
     * 分享链接或视频URL
     * - 抖音分享链接：https://v.douyin.com/xxx/ 或 https://www.douyin.com/video/xxx
     * - 小红书分享链接：https://www.xiaohongshu.com/explore/xxx
     * - 视频URL：直接提供视频文件URL
     */
    private String shareUrl; // 分享链接或视频URL
}

