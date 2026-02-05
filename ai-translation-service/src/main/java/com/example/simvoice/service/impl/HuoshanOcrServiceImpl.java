package com.example.simvoice.service.impl;

import com.example.simvoice.context.HuoshanProperties;
import com.example.simvoice.exception.BusinessException;
import com.example.simvoice.result.ResultCode;
import com.example.simvoice.service.HuoshanOcrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 火山引擎 AI 识图实现
 *
 * 说明：
 * - 这里采用通用的 HTTP 调用方式，具体的 URL、请求体和返回结构需要根据你在火山引擎控制台选用的识图/图像理解接口来调整。
 * - 代码中假设：
 *   - 使用 huoshan.ocr-url 作为识图接口地址；
 *   - 使用 huoshan.api-key 作为鉴权（通过 X-Api-Key 头部），如与你的接口不同，请按文档修改。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HuoshanOcrServiceImpl implements HuoshanOcrService {

    private final RestTemplate restTemplate;
    private final HuoshanProperties huoshanProperties;

    @Override
    @SuppressWarnings("unchecked")
    public String recognizeAndSummarize(String imageUrl) {
        if (!StringUtils.hasText(huoshanProperties.getApiKey())) {
            throw new BusinessException(ResultCode.ERROR, "未配置火山引擎 API Key：请在 application.yml 配置 huoshan.api-key");
        }
        if (!StringUtils.hasText(huoshanProperties.getOcrUrl())) {
            throw new BusinessException(ResultCode.ERROR, "未配置火山引擎识图接口地址：请在 application.yml 配置 huoshan.ocr-url");
        }
        if (!StringUtils.hasText(imageUrl)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "图片地址不能为空");
        }

        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("image_url", imageUrl);
            // 如需更多参数（语言、是否返回坐标等），按火山引擎实际接口文档在此补充

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // 鉴权方式视你的接口而定：有的用 X-Api-Key，有的需要 AK/SK 签名，这里先用最简单形式
            headers.set("X-Api-Key", huoshanProperties.getApiKey().trim());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> resp = restTemplate.exchange(
                    huoshanProperties.getOcrUrl(),
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> respBody = resp.getBody() == null ? new LinkedHashMap<>() : (Map<String, Object>) resp.getBody();
            if (respBody == null) {
                log.error("火山引擎识图返回为空, imageUrl={}", imageUrl);
                throw new BusinessException(ResultCode.ERROR, "火山引擎识图失败：响应为空");
            }

            // ★ 根据你实际使用的识图接口结构，从返回值中取出识别文本。
            // 这里先尝试从 text 或 data.text 字段读取，并预留一个简单的提炼逻辑。
            String text = tryExtractText(respBody);
            if (!StringUtils.hasText(text)) {
                log.error("火山引擎识图未返回可用文本, body={}", respBody);
                throw new BusinessException(ResultCode.ERROR, "火山引擎识图失败：未返回文本内容");
            }

            // 这里直接返回识别出的文本；如果你在接口侧已经做了提炼，这里就不再二次处理
            return text.trim();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用火山引擎识图接口异常, imageUrl={}", imageUrl, e);
            throw new BusinessException(ResultCode.ERROR, "火山引擎识图失败: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private String tryExtractText(Map<String, Object> respBody) {
        // 1) 常见简单形式：{"text": "..."}
        Object direct = respBody.get("text");
        if (direct != null && StringUtils.hasText(String.valueOf(direct))) {
            return String.valueOf(direct);
        }

        // 2) 有些接口会嵌套在 data 里：{"data": {"text": "..."}}
        Object dataObj = respBody.get("data");
        if (dataObj instanceof Map) {
            Map<String, Object> data = (Map<String, Object>) dataObj;
            Object textObj = data.get("text");
            if (textObj != null && StringUtils.hasText(String.valueOf(textObj))) {
                return String.valueOf(textObj);
            }
        }

        // 3) 兜底：如果有 results[0].text 之类的结构，可在这里按需扩展
        Object resultsObj = respBody.get("results");
        if (resultsObj instanceof Iterable) {
            for (Object item : (Iterable<?>) resultsObj) {
                if (item instanceof Map) {
                    Object t = ((Map<?, ?>) item).get("text");
                    if (t != null && StringUtils.hasText(String.valueOf(t))) {
                        return String.valueOf(t);
                    }
                }
            }
        }

        return null;
    }
}


