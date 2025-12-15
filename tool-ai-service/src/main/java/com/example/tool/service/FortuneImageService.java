package com.example.tool.service;

import java.awt.image.BufferedImage;

/**
 * 运势图片生成服务接口
 * 提供运势和星座运势图片生成功能，使用Java Graphics2D绘制
 * 
 * @author tool-ai-service
 * @since 1.0
 */
public interface FortuneImageService {
    /**
     * 生成运势图片
     * @param name 姓名
     * @param birthDate 出生日期
     * @param fortuneText 运势文案
     * @return 运势图片
     */
    BufferedImage generateFortuneImage(String name, String birthDate, String fortuneText);
    
    /**
     * 生成星座运势图片
     * @param constellation 星座名称
     * @param fortuneText 运势文案
     * @return 运势图片
     */
    BufferedImage generateConstellationFortuneImage(String constellation, String fortuneText);
}

