package com.example.tool.service;

import java.awt.image.BufferedImage;

/**
 * 签名生成服务接口
 * 提供姓氏签名图片生成功能，支持多种字体风格
 * 
 * @author tool-ai-service
 * @since 1.0
 */
public interface NameSignService {
    /**
     * 生成签名图片
     * 
     * @param surname 姓氏
     * @param style 风格：classic（经典）, cursive（行书）, grass（草书）, artistic（艺术）
     * @return 签名图片的BufferedImage对象
     */
    BufferedImage generateSignImage(String surname, String style);
}

