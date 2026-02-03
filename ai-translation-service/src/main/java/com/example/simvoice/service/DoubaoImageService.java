package com.example.simvoice.service;

import com.example.simvoice.config.DoubaoProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DoubaoImageService {
    private final RestTemplate restTemplate;
    private final DoubaoProperties props;

    public DoubaoImageService(RestTemplate restTemplate, DoubaoProperties props) {
        this.restTemplate = restTemplate;
        this.props = props;
    }

    public Map<String, Object> generate(String prompt, Integer n, String size, String model) {
        if (!StringUtils.hasText(props.getApiKey())) {
            throw new IllegalStateException("未配置豆包/火山 Ark API Key：请在 application.yml 配置 ai.doubao.api-key（或设置环境变量 ARK_API_KEY）");
        }
        String finalModel = StringUtils.hasText(model) ? model : props.getModel();
        if (!StringUtils.hasText(finalModel)) {
            throw new IllegalStateException("未配置豆包生图模型：请在 application.yml 配置 ai.doubao.model（填你控制台开通的模型名）");
        }
        if (!StringUtils.hasText(prompt)) {
            throw new IllegalArgumentException("prompt 不能为空");
        }

        int finalN = (n != null ? n : (props.getDefaultN() == null ? 1 : props.getDefaultN()));
        String finalSize = StringUtils.hasText(size) ? size : props.getDefaultSize();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", finalModel);
        body.put("prompt", prompt);
        body.put("n", finalN);
        if (StringUtils.hasText(finalSize)) body.put("size", finalSize);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(props.getApiKey().trim());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        String url = joinUrl(props.getBaseUrl(), "/images/generations");
        ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        //noinspection unchecked
        return (Map<String, Object>) (resp.getBody() == null ? Collections.emptyMap() : resp.getBody());
    }

    private static String joinUrl(String base, String path) {
        if (!StringUtils.hasText(base)) return path;
        String b = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        String p = path.startsWith("/") ? path : ("/" + path);
        return b + p;
    }
}



