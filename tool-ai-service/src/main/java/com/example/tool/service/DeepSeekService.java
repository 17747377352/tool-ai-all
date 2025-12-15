package com.example.tool.service;

/**
 * DeepSeek服务接口
 * 提供调用DeepSeek AI API生成运势文案的功能
 * 
 * @author tool-ai-service
 * @since 1.0
 */
public interface DeepSeekService {
    /**
     * 调用DeepSeek生成运势文案
     * @param name 姓名
     * @param birthDate 出生日期（格式：yyyy-MM-dd）
     * @return 运势文案
     */
    String generateFortuneText(String name, String birthDate);
    
    /**
     * 调用DeepSeek生成星座运势文案
     * @param constellation 星座名称
     * @return 运势文案
     */
    String generateConstellationFortuneText(String constellation);
}

