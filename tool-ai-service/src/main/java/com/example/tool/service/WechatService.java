package com.example.tool.service;

import java.util.Map;

/**
 * 微信服务接口
 * 提供微信小程序API调用相关功能
 * 
 * @author tool-ai-service
 * @since 1.0
 */
public interface WechatService {
    /**
     * 微信code2Session接口
     * 通过code换取openid和session_key
     * 
     * @param code 微信小程序返回的code
     * @return 包含openid和session_key的Map
     */
    Map<String, String> code2Session(String code);
    
    /**
     * 解密用户信息
     * 使用AES-128-CBC算法解密微信返回的加密用户信息
     * 
     * @param encryptedData 加密的用户信息数据
     * @param iv 初始向量
     * @param sessionKey 会话密钥
     * @return 解密后的用户信息Map，包含nickName、avatarUrl等
     */
    Map<String, Object> decryptUserInfo(String encryptedData, String iv, String sessionKey);
}

