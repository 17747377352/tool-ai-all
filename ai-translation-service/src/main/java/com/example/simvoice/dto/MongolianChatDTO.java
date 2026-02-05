package com.example.simvoice.dto;

import lombok.Data;

import java.util.List;

/**
 * 蒙古语 AI 对话请求 DTO
 *
 * 前端维护完整对话上下文，将 messages 作为 Kimi 的 messages 传入，
 * 服务端在获得最新回复后，再调用小牛翻译将双方内容翻译成蒙古语。
 */
@Data
public class MongolianChatDTO {

    /**
     * 对话消息列表，顺序为从早到晚
     * role 取值：system / user / assistant
     */
    private List<Message> messages;

    /**
     * 使用的模型，可选；为空时使用默认配置的 Kimi 模型
     */
    private String model;

    /**
     * 温度，可选
     */
    private Double temperature;

    /**
     * 最大 tokens，可选
     */
    private Integer maxTokens;

    /**
     * 是否启用联网搜索
     */
    private Boolean enableWebSearch;

    /**
     * 搜索模式：default / accurate
     */
    private String searchMode;

    @Data
    public static class Message {
        /**
         * 角色：system / user / assistant
         */
        private String role;

        /**
         * 原始内容（通常为中文或其它非蒙古语）
         */
        private String content;
    }
}


