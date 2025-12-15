package com.example.tool.controller;

import com.example.tool.dto.DecryptUserInfoDTO;
import com.example.tool.dto.WxLoginDTO;
import com.example.tool.result.Result;
import com.example.tool.service.AuthService;
import com.example.tool.vo.WxLoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 认证控制器
 * 提供微信小程序登录和用户信息解密相关接口
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 微信小程序登录接口
     * 通过微信code换取openid和session_key，并生成JWT token
     * 此接口在白名单中，无需token即可访问
     * 
     * @param dto 微信登录请求参数，包含微信小程序返回的code
     * @return 统一返回结果，包含token、openid等信息
     */
    @PostMapping("/wx-login")
    public Result<WxLoginVO> wxLogin(@RequestBody WxLoginDTO dto) {
        return Result.success(authService.wxLogin(dto.getCode()));
    }

    /**
     * 解密用户信息接口
     * 解密微信小程序返回的加密用户信息，并保存到数据库
     * 需要JWT token认证
     * 
     * @param dto 解密请求参数，包含加密数据、初始向量和session_key
     * @param request HTTP请求对象，用于获取用户openid（从JWT拦截器注入）
     * @return 统一返回结果
     */
    @PostMapping("/decrypt-userinfo")
    public Result<Void> decryptUserInfo(@RequestBody DecryptUserInfoDTO dto, HttpServletRequest request) {
        String openid = (String) request.getAttribute("openid");
        authService.decryptUserInfo(openid, dto.getEncryptedData(), dto.getIv(), dto.getSessionKey());
        return Result.success();
    }
}

