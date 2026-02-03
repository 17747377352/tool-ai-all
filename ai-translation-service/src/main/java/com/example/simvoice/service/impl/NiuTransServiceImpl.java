package com.example.simvoice.service.impl;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.example.simvoice.context.NiuTransProperties;
import com.example.simvoice.exception.BusinessException;
import com.example.simvoice.result.ResultCode;
import com.example.simvoice.service.NiuTransService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.TreeMap;

/**
 * 小牛翻译服务实现类
 * 实现文本翻译功能
 * 
 * @author ai-translation-service
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NiuTransServiceImpl implements NiuTransService {

    private final NiuTransProperties niuTransProperties;

    private static final String TRANSLATE_URL = "https://api.niutrans.com/v2/text/translate";

    @Override
    public String translate(String text, String from, String to) {
        if (!StringUtils.hasText(text)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "翻译文本不能为空");
        }
        if (text.length() > 5000) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "翻译文本长度不能超过5000字符");
        }
        if (!StringUtils.hasText(from) || !StringUtils.hasText(to)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "源语言和目标语言不能为空");
        }

        try {
            // 构建请求参数（使用TreeMap自动按ASCII码排序）
            TreeMap<String, Object> requestParamsMap = new TreeMap<>();
            requestParamsMap.put("from", from);
            requestParamsMap.put("to", to);
            requestParamsMap.put("srcText", text);
            requestParamsMap.put("appId", niuTransProperties.getAppId());
            requestParamsMap.put("apikey", niuTransProperties.getApiKey());
            requestParamsMap.put("timestamp", String.valueOf(System.currentTimeMillis()));

            // 生成权限字符串（authStr）
            String authStr = generateAuthStr(requestParamsMap);
            requestParamsMap.put("authStr", authStr);

            log.info("小牛翻译请求: from={}, to={}, textLength={}", from, to, text.length());

            // 发送POST请求
            String response = HttpUtil.post(TRANSLATE_URL, requestParamsMap);
            log.info("小牛翻译响应: {}", response);

            // 解析响应
            JSONObject responseJson = JSONObject.parseObject(response);
            if (responseJson == null) {
                throw new BusinessException(ResultCode.ERROR, "小牛翻译响应解析失败");
            }

            // 检查错误码
            String errorCode = responseJson.getString("errorCode");
            if (StringUtils.hasText(errorCode) && !"0".equals(errorCode)) {
                String errorMsg = responseJson.getString("errorMsg");
                throw new BusinessException(ResultCode.ERROR, "小牛翻译失败: " + errorMsg + " (错误码: " + errorCode + ")");
            }

            // 提取翻译结果
            String result = responseJson.getString("tgtText");
            if (!StringUtils.hasText(result)) {
                throw new BusinessException(ResultCode.ERROR, "翻译结果为空");
            }

            log.info("小牛翻译成功: from={}, to={}, result={}", from, to, result);
            return result;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("小牛翻译异常: from={}, to={}", from, to, e);
            throw new BusinessException(ResultCode.ERROR, "小牛翻译异常: " + e.getMessage());
        }
    }

    /**
     * 生成权限字符串（authStr）
     * 步骤：
     * 1. 将所有参数（包括apikey）按ASCII码排序，拼接成键值对字符串
     * 2. 使用MD5加密
     * 3. authStr本身不参与签名生成
     */
    private String generateAuthStr(TreeMap<String, Object> requestParamsMap) {
        // 创建副本，避免修改原Map
        TreeMap<String, Object> paramsForSign = new TreeMap<>(requestParamsMap);
        
        // 构建参数字符串：apikey=xxx&appId=xxx&from=xxx&srcText=xxx&timestamp=xxx&to=xxx
        StringBuilder paramStr = new StringBuilder();
        Set<String> keys = paramsForSign.keySet();
        for (String key : keys) {
            if (paramStr.length() > 0) {
                paramStr.append("&");
            }
            paramStr.append(key).append("=").append(paramsForSign.get(key));
        }
        
        // 使用MD5加密
        String authStr = SecureUtil.md5(paramStr.toString());
        log.debug("权限字符串生成: paramStr={}, authStr={}", paramStr.toString(), authStr);
        
        return authStr;
    }
}
