package com.example.tool.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提交语音转写任务响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitTaskResponse {
    private int code;
    private String message;
    private String taskId;
}

