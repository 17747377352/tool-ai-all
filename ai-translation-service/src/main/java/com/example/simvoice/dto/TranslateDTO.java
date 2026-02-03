package com.example.simvoice.dto;

import lombok.Data;

/**
 * 翻译请求DTO
 * 
 * @author ai-translation-service
 * @since 1.0
 */
@Data
public class TranslateDTO {
    /**
     * 待翻译文本
     */
    private String text;
    
    /**
     * 源语言代码
     * 支持：zh（中文）、en（英文）、ja（日文）、mn（蒙古语）
     */
    private String from;
    
    /**
     * 目标语言代码
     * 支持：zh（中文）、en（英文）、ja（日文）、mn（蒙古语）
     */
    private String to;
}

