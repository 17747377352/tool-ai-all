package com.example.simvoice.service;

/**
 * 火山引擎图片生成服务接口
 * 提供基于火山引擎文生图API的图片生成功能
 * 
 * @author ai-translation-service
 * @since 1.0
 */
public interface HuoshanImageService {
    /**
     * 字生图：根据文字提示词生成头像图片
     * 
     * @param prompt 生成提示词，描述想要生成的头像特征
     * @param style 风格：realistic（写实）, cartoon（卡通）, anime（动漫）, oil-painting（油画）等
     * @return 生成的图片URL
     */
    String generateAvatarFromText(String prompt, String style);
    
    /**
     * 图生图：基于上传的图片生成新头像
     * 
     * @param imageUrl 原始图片URL
     * @param prompt 生成提示词，描述想要生成的头像特征
     * @param style 风格：realistic（写实）, cartoon（卡通）, anime（动漫）, oil-painting（油画）等
     * @return 生成的图片URL
     */
    String generateAvatarFromImage(String imageUrl, String prompt, String style);
}

