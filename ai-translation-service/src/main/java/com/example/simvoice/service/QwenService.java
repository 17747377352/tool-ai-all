package com.example.simvoice.service;

import com.example.simvoice.config.QwenProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class QwenService {
    private final RestTemplate restTemplate;
    private final QwenProperties props;

    public QwenService(RestTemplate restTemplate, QwenProperties props) {
        this.restTemplate = restTemplate;
        this.props = props;
    }

    /**
     * 简单单轮对话：仅 system + 当前用户 prompt
     */
    public Map<String, Object> chat(String prompt,
                                    String system,
                                    String model,
                                    Double temperature,
                                    Integer maxTokens) {
        if (!StringUtils.hasText(props.getApiKey())) {
            throw new IllegalStateException("未配置千问 API Key：请在 application.yml 配置 ai.qwen.api-key（或设置环境变量 DASHSCOPE_API_KEY）");
        }

        String finalModel = StringUtils.hasText(model) ? model : props.getModel();
        if (!StringUtils.hasText(finalModel)) {
            throw new IllegalStateException("未配置千问模型：请在 application.yml 配置 ai.qwen.model");
        }
        if (!StringUtils.hasText(prompt)) {
            throw new IllegalArgumentException("prompt 不能为空");
        }

        List<Map<String, Object>> messages = new ArrayList<>();
        if (StringUtils.hasText(system)) {
            messages.add(msg("system", system));
        }
        messages.add(msg("user", prompt));

        return chatWithMessages(messages, finalModel, temperature, maxTokens);
    }

    /**
     * 带完整 message 列表的对话，用于前端维护上下文的多轮对话场景
     */
    public Map<String, Object> chatWithMessages(List<Map<String, Object>> messages,
                                                String model,
                                                Double temperature,
                                                Integer maxTokens) {
        if (!StringUtils.hasText(props.getApiKey())) {
            throw new IllegalStateException("未配置千问 API Key：请在 application.yml 配置 ai.qwen.api-key（或设置环境变量 DASHSCOPE_API_KEY）");
        }
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("messages 不能为空");
        }

        String finalModel = StringUtils.hasText(model) ? model : props.getModel();
        if (!StringUtils.hasText(finalModel)) {
            throw new IllegalStateException("未配置千问模型：请在 application.yml 配置 ai.qwen.model");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", finalModel);
        body.put("messages", messages);
        if (temperature != null) body.put("temperature", temperature);
        if (maxTokens != null) body.put("max_tokens", maxTokens);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(props.getApiKey().trim());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        String url = joinUrl(props.getBaseUrl(), "/chat/completions");
        ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        //noinspection unchecked
        return (Map<String, Object>) (resp.getBody() == null ? Collections.emptyMap() : resp.getBody());
    }

    private static Map<String, Object> msg(String role, String content) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("role", role);
        m.put("content", content);
        return m;
    }

    private static String joinUrl(String base, String path) {
        if (!StringUtils.hasText(base)) return path;
        String b = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        String p = path.startsWith("/") ? path : ("/" + path);
        return b + p;
    }
}



