package com.example.simvoice.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONPath;

/**
 * OpenAI compatible 接口返回体/入参的 JSON 提取工具（Fastjson + JSONPath）
 *
 * 目的：
 * - 避免 controller/service 中大量 Map 强转和 @SuppressWarnings("unchecked")
 * - 提升可读性与可维护性
 */
public final class OpenAiCompatJsonUtils {

    private OpenAiCompatJsonUtils() {
    }

    /**
     * 提取 choices[0].message.content（常见 chat/completions 返回内容）
     */
    public static String firstChatContent(Object raw) {
        Object v = eval(raw, "$.choices[0].message.content");
        return v == null ? null : String.valueOf(v);
    }

    /**
     * JSONPath 安全求值：raw 可以是 Map/JavaBean/JSONObject/String(JSON)
     */
    public static Object eval(Object raw, String jsonPath) {
        if (raw == null || jsonPath == null) return null;
        Object json = (raw instanceof String) ? JSON.parse(String.valueOf(raw)) : JSON.toJSON(raw);
        return JSONPath.eval(json, jsonPath);
    }
}


