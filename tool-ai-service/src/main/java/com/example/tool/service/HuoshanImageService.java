package com.example.tool.service;

/**
 * 火山引擎图片生成服务接口
 * 提供基于火山引擎文生图API的图片生成功能
 * 
 * @author tool-ai-service
 * @since 1.0
 */
public interface HuoshanImageService {
    /**
     * 根据运势文案生成运势图片
     * 
     * @param fortuneText 运势文案
     * @param name 姓名（用于生成prompt）
     * @param birthDate 出生日期（用于生成prompt，可选）
     * @return 生成的图片URL
     */
    String generateFortuneImage(String fortuneText, String name, String birthDate);
    
    /**
     * 根据星座运势文案生成星座运势图片
     * 
     * @param fortuneText 运势文案
     * @param constellation 星座名称（用于生成prompt）
     * @return 生成的图片URL
     */
    String generateConstellationFortuneImage(String fortuneText, String constellation);
    
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
     * 生成姓氏签名图片
     * 根据姓氏和风格生成艺术签名图片
     * 
     * @param surname 姓氏
     * @param style 签名风格：classic（经典）, cursive（行书）, grass（草书）, artistic（艺术）
     * @return 生成的签名图片URL
     */
    String generateNameSignImage(String surname, String style);
}


