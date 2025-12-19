package com.example.tool.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.tool.context.GfpganProperties;
import com.example.tool.exception.BusinessException;
import com.example.tool.result.ResultCode;
import com.example.tool.service.GfpganService;
import cn.hutool.http.ssl.SSLSocketFactoryBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * GFPGAN 云端修复实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GfpganServiceImpl implements GfpganService {

    private final GfpganProperties gfpganProperties;

    @Override
    public byte[] restore(String imageUrl, Double strength) {
        if (!StringUtils.hasText(imageUrl)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "图片URL不能为空");
        }
        if (!StringUtils.hasText(gfpganProperties.getEndpoint())) {
            throw new BusinessException(ResultCode.ERROR, "GFPGAN 接口未配置");
        }

        try {
            double scale = gfpganProperties.getScale() != null ? gfpganProperties.getScale() : 3.0;
            if (strength != null) {
                double s = Math.max(0.0, Math.min(1.0, strength));
                // 将 0-1 映射到 1~4，更高锐度
                scale = 1.0 + s * 3.0;
            }

            JSONObject input = new JSONObject();
            input.put("img", imageUrl);
            input.put("scale", scale);
            input.put("version", gfpganProperties.getModelVersion());

            JSONObject payload = new JSONObject();
            payload.put("version", gfpganProperties.getModelVersionId());
            payload.put("input", input);

            cn.hutool.http.HttpRequest request = cn.hutool.http.HttpRequest
                    .post(gfpganProperties.getEndpoint())
                    .timeout(gfpganProperties.getTimeoutMillis())
                    .header("Content-Type", "application/json")
                    .header("Prefer", "wait")
                    .body(payload.toJSONString());
            // 明确使用 TLSv1.2，避免握手被远端拒绝
            request.setSSLSocketFactory(
                    SSLSocketFactoryBuilder.create().setProtocol("TLSv1.2").build()
            );

            if (StringUtils.hasText(gfpganProperties.getApiKey())) {
                request.header("Authorization", "Bearer " + gfpganProperties.getApiKey());
            }

            cn.hutool.http.HttpResponse response = request.execute();
            int httpStatus = response.getStatus();
            // Replicate 同步模式会返回 201（已创建且包含 output），也视为成功
            if (httpStatus != 200 && httpStatus != 201) {
                log.error("GFPGAN (Replicate) 返回非 200/201，status={}, body={}", httpStatus, response.body());
                throw new BusinessException(ResultCode.ERROR, "GFPGAN 修复失败，状态码：" + httpStatus);
            }

            String body = response.body();
            JSONObject json = JSON.parseObject(body);
            if (json == null) {
                throw new BusinessException(ResultCode.ERROR, "GFPGAN 响应为空");
            }

            if (json.containsKey("error") && StringUtils.hasText(json.getString("error"))) {
                throw new BusinessException(ResultCode.ERROR, "GFPGAN 修复失败：" + json.getString("error"));
            }

            // 成功输出：兼容数组或单个字符串
            if (json.containsKey("output")) {
                try {
                    Object outputObj = json.get("output");
                    String resultUrl = null;
                    if (outputObj instanceof com.alibaba.fastjson.JSONArray) {
                        java.util.List<String> outputs = json.getJSONArray("output").toJavaList(String.class);
                        if (outputs != null && !outputs.isEmpty()) {
                            resultUrl = outputs.get(0);
                        }
                    } else if (outputObj instanceof String) {
                        resultUrl = (String) outputObj;
                    }

                    if (StringUtils.hasText(resultUrl)) {
                        cn.hutool.http.HttpResponse downloadResp = cn.hutool.http.HttpRequest.get(resultUrl)
                                .timeout(gfpganProperties.getTimeoutMillis())
                                .execute();
                        if (downloadResp.getStatus() == 200) {
                            return downloadResp.bodyBytes();
                        }
                        throw new BusinessException(ResultCode.ERROR, "下载修复结果失败，状态码：" + downloadResp.getStatus());
                    }
                } catch (Exception ignore) {
                    // 继续尝试 Base64 解析
                }
            }

            // 兼容 Base64 输出
            String base64 = json.getString("image_base64");
            if (!StringUtils.hasText(base64) && json.containsKey("data")) {
                base64 = json.getJSONObject("data").getString("image_base64");
            }
            if (StringUtils.hasText(base64)) {
                return Base64.getDecoder().decode(base64.getBytes(StandardCharsets.UTF_8));
            }

            log.error("GFPGAN 响应无法解析，body={}", body);
            throw new BusinessException(ResultCode.ERROR, "GFPGAN 修复失败，未获取到图片数据");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("GFPGAN 修复异常", e);
            throw new BusinessException(ResultCode.ERROR, "GFPGAN 修复异常：" + e.getMessage());
        }
    }
}

