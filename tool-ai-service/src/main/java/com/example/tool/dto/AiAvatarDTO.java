package com.example.tool.dto;

import lombok.Data;

/**
 * AI头像生成请求DTO
 * 支持两种模式：
 * 1. 字生图：仅提供prompt，根据文字描述生成头像
 * 2. 图生图：提供imageUrl和prompt，基于上传的图片生成新头像
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@Data
public class AiAvatarDTO {
    /**
     * 生成提示词（必填）
     * 用于描述想要生成的头像特征
     */
    private String prompt;
    
    /**
     * 风格：realistic（写实）, cartoon（卡通）, anime（动漫）, oil-painting（油画）等
     */
    private String style;
    
    /**
     * 图片URL（可选，用于图生图模式）
     * 如果提供此字段，将基于此图片生成新头像（图生图）
     * 如果不提供，则仅根据prompt生成头像（字生图）
     */
    private String imageUrl;
}

