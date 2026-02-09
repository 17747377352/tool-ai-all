package com.example.simvoice.service.impl;

import com.example.simvoice.entity.User;
import com.example.simvoice.service.AuthService;
import com.example.simvoice.service.UserService;
import com.example.simvoice.service.UserQuotaService;
import com.example.simvoice.service.WechatService;
import com.example.simvoice.utils.JwtUtil;
import com.example.simvoice.vo.WxLoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 认证服务实现类
 * 实现微信小程序登录和用户信息解密功能
 * 
 * @author ai-translation-service
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final WechatService wechatService;
    private final UserService userService;
    private final UserQuotaService userQuotaService;

    /**
     * 微信小程序登录
     * 
     * @param code 微信小程序返回的code
     * @return 登录结果，包含token、openid等信息
     */
    @Override
    public WxLoginVO wxLogin(String code) {
        // 1. 通过code获取openid和session_key
        Map<String, String> session = wechatService.code2Session(code);
        String openid = session.get("openid");

        // 2. 查询或创建用户
        User user = userService.getByOpenid(openid);
        boolean isNewUser = (user == null);
        if (isNewUser) {
            user = userService.createOrUpdate(openid, null, null);
            // 新用户初始化额度
            userQuotaService.getOrCreateQuota(openid);
        }

        // 3. 生成JWT token
        String token = JwtUtil.generateToken(openid);

        // 4. 返回结果
        WxLoginVO vo = new WxLoginVO();
        vo.setToken(token);
        vo.setOpenid(openid);
        vo.setAvatar(user.getAvatar());
        vo.setNickname(user.getNickname());
        return vo;
    }

    /**
     * 解密用户信息
     * 
     * @param openid 用户openid
     * @param encryptedData 加密的用户信息数据
     * @param iv 初始向量
     * @param sessionKey 会话密钥
     */
    @Override
    public void decryptUserInfo(String openid, String encryptedData, String iv, String sessionKey) {
        // 解密用户信息
        Map<String, Object> userInfo = wechatService.decryptUserInfo(encryptedData, iv, sessionKey);
        String nickname = (String) userInfo.get("nickName");
        String avatarUrl = (String) userInfo.get("avatarUrl");

        // 更新用户信息
        userService.createOrUpdate(openid, nickname, avatarUrl);
    }
}

