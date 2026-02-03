package com.example.simvoice.dto;

import lombok.Data;

/**
 * 模版生成图片请求DTO
 */
@Data
public class TemplateGenerateDTO {
    /**
     * 模版ID
     */
    private Long templateId;
    
    /**
     * 生成模式：3-模版同款 4-模版参考图
     */
    private Integer generateMode;
    
    /**
     * 自定义提示词（可选，用于模版参考图模式时补充或修改提示词）
     */
    private String customPrompt;
}

