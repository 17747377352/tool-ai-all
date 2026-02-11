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

    /**
     * 老照片修复/增强：
     * 使用火山引擎对老照片进行清晰度增强、去噪和人脸细节智能修复。
     *
     * @param imageUrl 原始老照片URL
     * @param prompt   修复提示语，例如：
     *                 “请将照片修复清晰，提高分辨率，消除噪点，智能修复人物面部细节。”
     * @return 修复后图片的URL（直接为图片地址，不带 IMAGE_LIST 前缀）
     */
    String enhancePhoto(String imageUrl, String prompt);

    /**
     * 图片理解 / 识图：
     * 使用火山 Ark 豆包视觉模型理解图片内容，输出中文摘要。
     *
     * @param imageUrl 已上传的图片公网 URL
     * @param question 向模型提问的文本，例如“图片主要讲了什么？”
     * @return 模型返回的中文内容
     */
    String understandImage(String imageUrl, String question);
}

