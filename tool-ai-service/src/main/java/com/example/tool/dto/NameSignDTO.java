package com.example.tool.dto;

import lombok.Data;

/**
 * 姓氏签名生成请求DTO
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@Data
public class NameSignDTO {
    /**
     * 姓氏
     */
    private String surname;
    
    /**
     * 签名风格：classic（经典）, cursive（行书）, grass（草书）, artistic（艺术）
     */
    private String style;
}

