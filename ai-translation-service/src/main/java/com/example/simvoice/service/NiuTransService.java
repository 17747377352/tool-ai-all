package com.example.simvoice.service;

/**
 * 小牛翻译服务接口
 * 提供文本翻译功能
 * 
 * @author ai-translation-service
 * @since 1.0
 */
public interface NiuTransService {
    /**
     * 文本翻译
     * 
     * @param text 待翻译文本
     * @param from 源语言代码（如：zh中文、en英文、ja日文、mn蒙古语）
     * @param to 目标语言代码（如：zh中文、en英文、ja日文、mn蒙古语）
     * @return 翻译结果
     */
    String translate(String text, String from, String to);
}

