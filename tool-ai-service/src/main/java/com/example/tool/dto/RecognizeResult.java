package com.example.tool.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 语音识别结果（回调接收）
 */
@Data
public class RecognizeResult {
    @JsonProperty("code")
    private Integer code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("taskId")
    private  String taskId;
    @JsonProperty("payload")
    private PayloadDTO payload;

    @Data
    public static class PayloadDTO {
        @JsonProperty("audio_meta")
        private AudioMetaDTO audioMeta;

        @JsonProperty("code")
        private Integer code;

        @JsonProperty("message")
        private MessageDTO message;

        @JsonProperty("request_id")
        private String requestId;

        @JsonProperty("result")
        private ResultDTO result;
    }

    @Data
    public static class AudioMetaDTO {
        // 音频元数据字段（根据实际回调内容补充）
    }

    @Data
    public static class MessageDTO {
        // 消息字段（根据实际回调内容补充）
    }

    @Data
    public static class ResultDTO {
        @JsonProperty("Sentences")
        private SentenceDTO[] sentences;

        @JsonProperty("Text")
        private String text;

        // 其他结果字段（根据实际回调内容补充）
    }

    @Data
    public static class SentenceDTO {
        @JsonProperty("Text")
        private String text;

        @JsonProperty("BeginTime")
        private Long beginTime;

        @JsonProperty("EndTime")
        private Long endTime;

        // 其他句子字段（根据实际回调内容补充）
    }
}

