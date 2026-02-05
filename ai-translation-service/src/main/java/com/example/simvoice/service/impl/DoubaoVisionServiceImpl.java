package com.example.simvoice.service.impl;

import com.example.simvoice.config.DoubaoProperties;
import com.example.simvoice.exception.BusinessException;
import com.example.simvoice.result.ResultCode;
import com.example.simvoice.service.DoubaoVisionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 使用火山 Ark 豆包视觉模型进行图片理解（/chat/completions）
 *
 * 等价于：
 * curl https://ark.cn-beijing.volces.com/api/v3/chat/completions \
 *   -H "Authorization: Bearer $ARK_API_KEY" \
 *   -H "Content-Type: application/json" \
 *   -d '{
 *     "model": "doubao-1-5-vision-pro-32k-250115",
 *     "messages": [{
 *       "role": "user",
 *       "content": [
 *         {"type": "image_url", "image_url": {"url": "https://..."}},
 *         {"type": "text", "text": "图片主要讲了什么?"}
 *       ]
 *     }]
 *   }'
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DoubaoVisionServiceImpl implements DoubaoVisionService {

    private final RestTemplate restTemplate;
    private final DoubaoProperties props;

    @Override
    @SuppressWarnings("unchecked")
    public String understandImage(String imageUrl, String question) {
        if (!StringUtils.hasText(props.getApiKey())) {
            throw new BusinessException(ResultCode.ERROR, "未配置豆包/火山 Ark API Key：请在 application.yml 配置 ai.doubao.api-key（或设置环境变量 ARK_API_KEY）");
        }
        if (!StringUtils.hasText(props.getBaseUrl())) {
            throw new BusinessException(ResultCode.ERROR, "未配置豆包 Ark base-url：请在 application.yml 配置 ai.doubao.base-url");
        }
        if (!StringUtils.hasText(imageUrl)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "图片地址不能为空");
        }

        String model = StringUtils.hasText(props.getVisionModel())
                ? props.getVisionModel()
                : "doubao-1-5-vision-pro-32k-250115";

        // content = [ {type:image_url,...}, {type:text,...} ]
        List<Map<String, Object>> contentList = new ArrayList<>();
        Map<String, Object> imagePart = new LinkedHashMap<>();
        imagePart.put("type", "image_url");
        Map<String, Object> imageUrlObj = new LinkedHashMap<>();
        imageUrlObj.put("url", imageUrl);
        imagePart.put("image_url", imageUrlObj);
        contentList.add(imagePart);

        Map<String, Object> textPart = new LinkedHashMap<>();
        textPart.put("type", "text");
        textPart.put("text", StringUtils.hasText(question) ? question : "图片主要讲了什么？请用简洁的中文总结。");
        contentList.add(textPart);

        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> userMsg = new LinkedHashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", contentList);
        messages.add(userMsg);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(props.getApiKey().trim());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        String url = joinUrl(props.getBaseUrl(), "/chat/completions");
        try {
            ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            Map<String, Object> respBody = resp.getBody();
            String content = extractChatContent(respBody);
            if (!StringUtils.hasText(content)) {
                throw new BusinessException(ResultCode.ERROR, "豆包视觉模型未返回内容");
            }
            return content.trim();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用豆包视觉模型失败, imageUrl={}", imageUrl, e);
            throw new BusinessException(ResultCode.ERROR, "豆包视觉图片理解失败: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private String extractChatContent(Map<String, Object> raw) {
        if (raw == null) return null;
        Object choicesObj = raw.get("choices");
        if (!(choicesObj instanceof List)) return null;
        List<Object> choices = (List<Object>) choicesObj;
        if (choices.isEmpty()) return null;
        Object c0 = choices.get(0);
        if (!(c0 instanceof Map)) return null;
        Map<String, Object> choice0 = (Map<String, Object>) c0;
        Object msgObj = choice0.get("message");
        if (!(msgObj instanceof Map)) return null;
        Map<String, Object> msg = (Map<String, Object>) msgObj;
        Object content = msg.get("content");
        return content == null ? null : String.valueOf(content);
    }

    private static String joinUrl(String base, String path) {
        if (!StringUtils.hasText(base)) return path;
        String b = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        String p = path.startsWith("/") ? path : ("/" + path);
        return b + p;
    }
}


