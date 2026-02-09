package com.example.simvoice.dto;

import lombok.Data;

/**
 * 功能配置DTO
 * 用于返回前端展示的功能入口信息
 * 
 * @author ai-translation-service
 * @since 1.0
 */
@Data
public class FunctionConfigDTO {
    /**
     * 功能类型：1-去水印 2-生成图片 3-老照片修复 4-AI识图+翻译 5-即时翻译 6-蒙古语AI对话
     */
    private Integer type;
    
    /**
     * 功能名称
     */
    private String name;
    
    /**
     * 功能路由路径（前端路由）
     */
    private String route;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 排序顺序
     */
    private Integer sortOrder;
}

