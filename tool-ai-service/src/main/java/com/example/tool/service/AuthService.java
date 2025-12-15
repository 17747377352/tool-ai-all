package com.example.tool.service;

import com.example.tool.vo.WxLoginVO;

/**
 * 认证服务接口
 * 提供微信小程序登录和用户信息解密功能
 * 
 * @author tool-ai-service
 * @since 1.0
 */
public interface AuthService {
    /**
     * 微信小程序登录
     * 通过code换取openid和session_key，并生成JWT token
     * 
     * @param code 微信小程序返回的code
     * @return 登录结果，包含token、openid等信息
     */
    WxLoginVO wxLogin(String code);
    
    /**
     * 解密用户信息
     * 解密微信小程序返回的加密用户信息，并保存到数据库
     * 
     * @param openid 用户openid
     * @param encryptedData 加密的用户信息数据
     * @param iv 初始向量
     * @param sessionKey 会话密钥
     */
    void decryptUserInfo(String openid, String encryptedData, String iv, String sessionKey);
}

