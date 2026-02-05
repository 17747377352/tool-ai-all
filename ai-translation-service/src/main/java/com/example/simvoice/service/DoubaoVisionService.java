package com.example.simvoice.service;

/**
 * 豆包视觉理解服务：使用 doubao-vision 模型进行图片理解，输出中文摘要。
 */
public interface DoubaoVisionService {

    /**
     * 使用豆包视觉模型理解图片内容，返回中文提炼结果。
     *
     * @param imageUrl 已上传的图片公网 URL
     * @param question 向模型提问的文本，例如“图片主要讲了什么？”
     * @return 模型返回的中文内容
     */
    String understandImage(String imageUrl, String question);
}


