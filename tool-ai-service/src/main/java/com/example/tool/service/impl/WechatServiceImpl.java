package com.example.tool.service.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.example.tool.context.WechatProperties;
import com.example.tool.exception.BusinessException;
import com.example.tool.result.ResultCode;
import com.example.tool.service.WechatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信服务实现类
 * 实现微信小程序API调用相关功能，包括code2Session和用户信息解密
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatServiceImpl implements WechatService {

    private final WechatProperties wechatProperties;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 微信code2Session接口
     * 
     * @param code 微信小程序返回的code
     * @return 包含openid和session_key的Map
     * @throws BusinessException 如果微信API调用失败
     */
    @Override
    public Map<String, String> code2Session(String code) {
        String url = wechatProperties.getBaseUrl() + "/sns/jscode2session";
        Map<String, Object> params = new HashMap<>();
        params.put("appid", wechatProperties.getAppid());
        params.put("secret", wechatProperties.getSecret());
        params.put("js_code", code);
        params.put("grant_type", "authorization_code");

        try {
            String response = HttpUtil.get(url, params);
            log.info("微信登录响应: {}", response);
            JSONObject json = JSONObject.parseObject(response);

            if (json.containsKey("errcode") && json.getIntValue("errcode") != 0) {
                throw new BusinessException(ResultCode.WX_LOGIN_FAILED, json.getString("errmsg"));
            }

            Map<String, String> result = new HashMap<>();
            result.put("openid", json.getString("openid"));
            result.put("session_key", json.getString("session_key"));
            return result;
        } catch (Exception e) {
            log.error("微信登录失败", e);
            if (e instanceof BusinessException) {
                throw e;
            }
            throw new BusinessException(ResultCode.WX_LOGIN_FAILED, "微信登录失败");
        }
    }

    /**
     * 解密用户信息
     * 使用AES-128-CBC算法解密微信返回的加密用户信息
     * 
     * @param encryptedData 加密的用户信息数据
     * @param iv 初始向量
     * @param sessionKey 会话密钥
     * @return 解密后的用户信息Map，包含nickName、avatarUrl等
     * @throws BusinessException 如果解密失败
     */
    @Override
    public Map<String, Object> decryptUserInfo(String encryptedData, String iv, String sessionKey) {
        try {
            byte[] dataByte = Base64.getDecoder().decode(encryptedData);
            byte[] keyByte = Base64.getDecoder().decode(sessionKey);
            byte[] ivByte = Base64.getDecoder().decode(iv);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            SecretKeySpec spec = new SecretKeySpec(keyByte, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(ivByte);
            cipher.init(Cipher.DECRYPT_MODE, spec, ivSpec);

            byte[] decrypted = cipher.doFinal(dataByte);
            String result = new String(decrypted, StandardCharsets.UTF_8);
            JSONObject json = JSONObject.parseObject(result);

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("nickName", json.getString("nickName"));
            userInfo.put("avatarUrl", json.getString("avatarUrl"));
            return userInfo;
        } catch (Exception e) {
            log.error("解密用户信息失败", e);
            throw new BusinessException(ResultCode.PARAM_ERROR, "解密用户信息失败");
        }
    }
}

