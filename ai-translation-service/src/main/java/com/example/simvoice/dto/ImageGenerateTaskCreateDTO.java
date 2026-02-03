package com.example.simvoice.dto;

import lombok.Data;

/**
 * 创建图片生成任务统一请求DTO
 * 支持三种模式：
 * 1-字生图 2-图生图 3-模版同款 4-模版参考图
 */
@Data
public class ImageGenerateTaskCreateDTO {

    /**
     * 生成模式：1-字生图 2-图生图 3-模版同款 4-模版参考图
     */
    private Integer generateMode;

    /**
     * 提示词（字生图 / 图生图必填；模版参考图可选覆盖模版默认提示词）
     */
    private String prompt;

    /**
     * 参考图片URL（图生图必填）
     */
    private String imageUrl;

    /**
     * 模版ID（模版同款 / 模版参考图必填）
     */
    private Long templateId;

    /**
     * 风格（可选，默认 realistic）
     */
    private String style;
}


