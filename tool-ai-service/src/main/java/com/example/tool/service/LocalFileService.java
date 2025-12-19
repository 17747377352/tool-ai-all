package com.example.tool.service;

import java.io.InputStream;

/**
 * 本地文件存储服务接口
 * 提供本地文件上传和访问功能
 * 
 * @author tool-ai-service
 * @since 1.0
 */
public interface LocalFileService {
    /**
     * 上传文件到本地存储
     * @param inputStream 文件输入流
     * @param fileName 文件名（包含路径，如：name-sign/20231209/xxx.jpg）
     * @param contentType 文件类型，如：image/jpeg
     * @return 文件访问URL（代理URL格式）
     */
    String uploadFile(InputStream inputStream, String fileName, String contentType);
    
    /**
     * 上传字节数组到本地存储
     * @param bytes 文件字节数组
     * @param fileName 文件名
     * @param contentType 文件类型
     * @return 文件访问URL（代理URL格式）
     */
    String uploadFile(byte[] bytes, String fileName, String contentType);
}

