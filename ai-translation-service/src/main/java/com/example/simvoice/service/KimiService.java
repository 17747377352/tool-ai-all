package com.example.simvoice.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.simvoice.config.KimiProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class KimiService {
    private final RestTemplate restTemplate;
    private final KimiProperties props;

    public KimiService(RestTemplate restTemplate, KimiProperties props) {
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
                                    Integer maxTokens,
                                    Boolean enableWebSearch,
                                    String searchMode) {
        if (!StringUtils.hasText(props.getApiKey())) {
            throw new IllegalStateException("未配置 Kimi API Key：请在 application.yml 配置 ai.kimi.api-key（或设置环境变量 MOONSHOT_API_KEY）");
        }

        String finalModel = StringUtils.hasText(model) ? model : props.getModel();
        if (!StringUtils.hasText(finalModel)) {
            throw new IllegalStateException("未配置 Kimi 模型：请在 application.yml 配置 ai.kimi.model");
        }
        if (!StringUtils.hasText(prompt)) {
            throw new IllegalArgumentException("prompt 不能为空");
        }

        List<Map<String, Object>> messages = new ArrayList<>();
        if (StringUtils.hasText(system)) {
            messages.add(msg("system", system));
        }
        messages.add(msg("user", prompt));

        return chatWithMessages(messages, finalModel, temperature, maxTokens, enableWebSearch, searchMode);
    }

    /**
     * 带完整 message 列表的对话，用于前端维护上下文的多轮对话场景
     */
    public Map<String, Object> chatWithMessages(List<Map<String, Object>> messages,
                                                String model,
                                                Double temperature,
                                                Integer maxTokens,
                                                Boolean enableWebSearch,
                                                String searchMode) {
        if (!StringUtils.hasText(props.getApiKey())) {
            throw new IllegalStateException("未配置 Kimi API Key：请在 application.yml 配置 ai.kimi.api-key（或设置环境变量 MOONSHOT_API_KEY）");
        }
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("messages 不能为空");
        }

        String finalModel = StringUtils.hasText(model) ? model : props.getModel();
        if (!StringUtils.hasText(finalModel)) {
            throw new IllegalStateException("未配置 Kimi 模型：请在 application.yml 配置 ai.kimi.model");
        }

        //默认开启联网搜索
        boolean useWebSearch = enableWebSearch == null ? true : Boolean.TRUE.equals(enableWebSearch);
        useWebSearch=true;
        // Moonshot 文档要求：每次请求都完整带上 tools 声明（builtin_function + function.name = "$web_search"）
        List<Map<String, Object>> tools = null;
        if (useWebSearch) {
            tools = new ArrayList<>();
            Map<String, Object> tool = new LinkedHashMap<>();
            tool.put("type", "builtin_function");
            Map<String, Object> function = new LinkedHashMap<>();
            function.put("name", "$web_search");
            tool.put("function", function);
            tools.add(tool);
        }

        // 注意：开启 $web_search 后，模型会先返回 finish_reason=tool_calls，需要我们把 tool_calls 回传（role=tool）后再请求一次
        List<Map<String, Object>> ctxMessages = new ArrayList<>(messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(props.getApiKey().trim());

        String url = joinUrl(props.getBaseUrl(), "/chat/completions");

        Map<String, Object> lastResp = null;
        for (int step = 0; step < 5; step++) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", finalModel);
            body.put("messages", ctxMessages);
            if (temperature != null) body.put("temperature", temperature);
            if (maxTokens != null) body.put("max_tokens", maxTokens);
            if (tools != null) body.put("tools", tools);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            lastResp = resp.getBody() == null ? Collections.emptyMap() : resp.getBody();

            ToolCallsParseResult parsed = parseToolCalls(lastResp);
            if (parsed == null) {
                break;
            }

            // 1) 追加 assistant 消息（包含 tool_calls），让模型理解当前要执行哪些工具
            ctxMessages.add(parsed.assistantMessage);

            // 2) 对于 $web_search：根据官方文档，我们无需真正执行搜索，只需把 arguments 原封不动回传给模型即可
            for (int i = 0; i < parsed.toolCalls.size(); i++) {
                JSONObject tc = parsed.toolCalls.getJSONObject(i);
                if (tc == null) continue;
                String toolCallId = tc.getString("id");
                JSONObject fn = tc.getJSONObject("function");
                String name = fn == null ? null : fn.getString("name");
                String arguments = fn == null ? null : fn.getString("arguments");

                Map<String, Object> toolMsg = new LinkedHashMap<>();
                toolMsg.put("role", "tool");
                toolMsg.put("tool_call_id", toolCallId);
                toolMsg.put("name", name);
                // 直接回传 arguments（字符串），等价于 Python 示例里的 json.dumps(arguments_obj)
                toolMsg.put("content", StrUtil.blankToDefault(arguments, "{}"));
                ctxMessages.add(toolMsg);
            }
        }

        return lastResp == null ? Collections.emptyMap() : lastResp;
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

    private static final class ToolCallsParseResult {
        final Map<String, Object> assistantMessage;
        final JSONArray toolCalls;

        private ToolCallsParseResult(Map<String, Object> assistantMessage, JSONArray toolCalls) {
            this.assistantMessage = assistantMessage;
            this.toolCalls = toolCalls;
        }
    }

    /**
     * 使用 Fastjson 解析 tool_calls，避免 Map 强转 + @SuppressWarnings。
     *
     * @return 如果本轮不是 tool_calls，则返回 null
     */
    private static ToolCallsParseResult parseToolCalls(Map<String, Object> resp) {
        if (resp == null || resp.isEmpty()) return null;

        JSONObject root = (JSONObject) JSON.toJSON(resp);
        JSONArray choices = root.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) return null;

        JSONObject choice0 = choices.getJSONObject(0);
        if (choice0 == null) return null;

        String finishReason = choice0.getString("finish_reason");
        if (!"tool_calls".equals(finishReason)) return null;

        JSONObject message = choice0.getJSONObject("message");
        if (message == null) return null;

        JSONArray toolCalls = message.getJSONArray("tool_calls");
        if (toolCalls == null || toolCalls.isEmpty()) return null;

        // 把 assistant 消息塞回上下文（包含 tool_calls）
        Map<String, Object> assistantMsg = new LinkedHashMap<>();
        assistantMsg.put("role", "assistant");
        if (message.containsKey("content")) assistantMsg.put("content", message.get("content"));
        assistantMsg.put("tool_calls", toolCalls);

        return new ToolCallsParseResult(assistantMsg, toolCalls);
    }
}

