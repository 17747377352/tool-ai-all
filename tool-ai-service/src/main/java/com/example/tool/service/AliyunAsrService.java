package com.example.tool.service;

import com.example.tool.dto.SubmitTaskResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 阿里云智能语音识别服务
 */
public interface AliyunAsrService {

    /**
     * 提交语音转写任务（异步模式）
     * 提交任务后立即返回 taskId，后台异步轮询，结果通过回调接口返回
     *
     * @param file       音频文件（pcm/wav/opus）
     * @param format     音频格式，可为空，为空时使用默认配置
     * @param sampleRate 采样率，可为空，为空时使用默认配置
     * @return 提交任务响应（包含 code, message, taskId）
     */
    SubmitTaskResponse submitTask(MultipartFile file, String format, Integer sampleRate);
}


