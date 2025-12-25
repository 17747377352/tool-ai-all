package com.example.tool.dto;

import lombok.Data;

/**
 * 微信登录请求DTO
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@Data
public class WxLoginDTO {
    /**
     * 微信小程序返回的code，用于换取openid和session_key
     */
    private String code;
}




