package com.example.simvoice.controller;

import com.example.simvoice.dto.MongolianChatDTO;
import com.example.simvoice.service.DoubaoImageService;
import com.example.simvoice.service.KimiService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final KimiService kimiService;
    private final DoubaoImageService doubaoImageService;

    public AiController(KimiService kimiService,
                        DoubaoImageService doubaoImageService) {
        this.kimiService = kimiService;
        this.doubaoImageService = doubaoImageService;
    }

    /**
     * Kimi 对话（Moonshot OpenAI compatible chat/completions）
     * body 示例：
     * {"prompt":"你好","system":"你是一个助手","model":"kimi-latest","temperature":0.7,"maxTokens":1024,"enableWebSearch":true,"searchMode":"accurate"}
     */
    @PostMapping("/kimi/chat")
    public Map<String, Object> kimiChat(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String prompt = str(body.get("prompt"));
        String system = str(body.get("system"));
        String model = str(body.get("model"));
        Double temperature = num(body.get("temperature"));
        Integer maxTokens = intNum(body.get("maxTokens"));
        Boolean enableWebSearch = body.get("enableWebSearch") instanceof Boolean ? (Boolean) body.get("enableWebSearch") : null;
        String searchMode = str(body.get("searchMode"));

        Map<String, Object> raw = kimiService.chat(prompt, system, model, temperature, maxTokens, enableWebSearch, searchMode);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", 200);
        result.put("message", "ok");
        String content = extractChatContent(raw);
        result.put("content", content);
        result.put("raw", raw);

        // 日志记录 Kimi 对话问答
        String openid = request != null ? (String) request.getAttribute("openid") : null;
        log.info("AI 对话[Kimi] openid={}, model={}, prompt={}, answer={}",
                openid,
                model != null ? model : "default",
                truncate(prompt, 500),
                truncate(content, 500));
        return result;
    }

    /**
     * 蒙古语 AI 对话（默认走 Kimi）
     *
     * - 前端传入完整对话 messages（role: system/user/assistant, content: 原始文本，通常为中文）
     * - 服务端在最前追加一个 system 提示，告诉千问「你是了解蒙古族生活方式及习惯的 AI 助手」
     * - 服务端将 messages 传给千问生成最新回复
     * - 蒙古语翻译由前端按需调用 /tool/translate 完成（每条消息点击“翻译”）
     */
    @PostMapping("/mongolian-chat")
    public Map<String, Object> mongolianChat(@RequestBody MongolianChatDTO dto, HttpServletRequest request) {
        if (dto.getMessages() == null || dto.getMessages().isEmpty()) {
            throw new IllegalArgumentException("messages 不能为空");
        }

        // 构造 Kimi messages 列表（只关心 role / content）
        List<Map<String, Object>> messages = new java.util.ArrayList<>();
        for (MongolianChatDTO.Message m : dto.getMessages()) {
            if (m == null) continue;
            String role = m.getRole();
            String content = m.getContent();
            if (content == null || content.trim().isEmpty()) {
                continue;
            }
            java.util.Map<String, Object> msg = new java.util.LinkedHashMap<>();
            msg.put("role", role == null || role.trim().isEmpty() ? "user" : role.trim());
            msg.put("content", content);
            messages.add(msg);
        }
        if (messages.isEmpty()) {
            throw new IllegalArgumentException("有效 messages 不能为空");
        }

        // 在最前面追加一个系统提示，固定告诉 Kimi：你是一名了解蒙古族人生活方式及习惯的 AI 助手
        Map<String, Object> systemMsg = new java.util.LinkedHashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", "你是一名非常了解蒙古族人生活方式和生活习惯的 AI 助手，请用简洁、友好的语气回答用户问题。");
        messages.add(0, systemMsg);

        // 调用 Kimi，保持上下文
        Map<String, Object> raw = kimiService.chatWithMessages(
                messages,
                dto.getModel(),
                dto.getTemperature(),
                dto.getMaxTokens(),
                dto.getEnableWebSearch(),
                dto.getSearchMode()
        );
        String assistantContent = extractChatContent(raw);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("assistantText", assistantContent);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", 200);
        result.put("message", "ok");
        result.put("data", data);
        result.put("raw", raw);

        // 从对话中取出用户最新一条 user 消息作为“提问”，记录蒙古语 AI 对话问答
        String userQuestion = null;
        for (int i = messages.size() - 1; i >= 0; i--) {
            Object roleObj = messages.get(i).get("role");
            if ("user".equals(roleObj)) {
                Object c = messages.get(i).get("content");
                userQuestion = c == null ? null : String.valueOf(c);
                break;
            }
        }
        String openid = request != null ? (String) request.getAttribute("openid") : null;
        log.info("AI 对话[蒙古语] openid={}, model={}, question={}, answer={}",
                openid,
                dto.getModel() != null ? dto.getModel() : "default",
                truncate(userQuestion, 500),
                truncate(assistantContent, 500));
        return result;
    }

    /**
     * 豆包生图（OpenAI compatible images/generations）
     * body 示例：
     * {"prompt":"一只穿宇航服的猫","n":1,"size":"1024x1024","model":"doubao-seedream-3"}
     */
    @PostMapping("/doubao/image")
    public Map<String, Object> doubaoImage(@RequestBody Map<String, Object> body) {
        String prompt = str(body.get("prompt"));
        Integer n = intNum(body.get("n"));
        String size = str(body.get("size"));
        String model = str(body.get("model"));

        Map<String, Object> raw = doubaoImageService.generate(prompt, n, size, model);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", 200);
        result.put("message", "ok");
        result.put("data", raw.get("data"));
        result.put("raw", raw);
        return result;
    }

    @SuppressWarnings("unchecked")
    private static String extractChatContent(Map<String, Object> raw) {
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

    private static String str(Object o) {
        if (o == null) return null;
        String s = String.valueOf(o);
        return s.trim().isEmpty() ? null : s.trim();
    }

    private static Double num(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).doubleValue();
        try {
            return Double.parseDouble(String.valueOf(o));
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Integer intNum(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).intValue();
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        if (s.length() <= max) return s;
        return s.substring(0, max) + "...(truncated)";
    }
}



