package com.example.simvoice.service;

/**
 * 火山引擎 AI 识图（OCR/图像理解）服务
 *
 * 实际调用的具体接口和返回字段需按火山引擎文档配置，这里只抽象成识别并提炼文本。
 */
public interface HuoshanOcrService {

    /**
     * 根据图片 URL 调用火山引擎接口，识别图片中的文字并用中文进行提炼总结。
     *
     * @param imageUrl 图片公网 URL（已上传到 OSS）
     * @return 提炼后的中文文本
     */
    String recognizeAndSummarize(String imageUrl);
}


