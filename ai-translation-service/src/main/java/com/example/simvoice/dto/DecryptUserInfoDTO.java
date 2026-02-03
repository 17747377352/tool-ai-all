package com.example.simvoice.dto;

import lombok.Data;

/**
 * 解密用户信息请求DTO
 * 
 * @author ai-translation-service
 * @since 1.0
 */
@Data
public class DecryptUserInfoDTO {
    /**
     * 加密的用户信息数据
     */
    private String encryptedData;
    
    /**
     * 初始向量
     */
    private String iv;
    
    /**
     * 会话密钥
     */
    private String sessionKey;
}

