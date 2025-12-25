package com.example.tool.vo;

import lombok.Data;

/**
 * 微信登录响应VO
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@Data
public class WxLoginVO {
    /**
     * JWT token，用于后续接口认证
     */
    private String token;
    
    /**
     * 微信openid
     */
    private String openid;
    
    /**
     * 用户头像URL
     */
    private String avatar;
    
    /**
     * 用户昵称
     */
    private String nickname;
}




