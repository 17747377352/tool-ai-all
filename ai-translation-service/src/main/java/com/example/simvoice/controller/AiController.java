package com.example.simvoice.controller;

import com.example.simvoice.dto.MongolianChatDTO;
import com.example.simvoice.service.HuoshanImageService;
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
    private final HuoshanImageService huoshanImageService;

    public AiController(HuoshanImageService huoshanImageService) {
        this.huoshanImageService = huoshanImageService;
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

        // 这里保留参数组织逻辑，将来需要切换到某个大模型时可以直接复用。
        // 当前版本仅作为占位，实现交给前端或后续服务，暂时返回空回答。
        String assistantContent = "";

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("assistantText", assistantContent);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", 200);
        result.put("message", "ok");
        result.put("data", data);
        result.put("raw", null);
        return result;
    }

    /**
     * 豆包生图（OpenAI compatible images/generations）
     * body 示例：
     * {"prompt":"一只穿宇航服的猫","n":1,"size":"1024x1024","model":"doubao-seedream-3"}
     */
    @PostMapping("/doubao/image")
    public Map<String, Object> doubaoImage(@RequestBody Map<String, Object> body) {
        throw new UnsupportedOperationException("该接口已废弃，请使用小程序内置的生图功能。");
    }

}



