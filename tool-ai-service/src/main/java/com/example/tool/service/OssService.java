package com.example.tool.service;

import java.io.InputStream;

/**
 * OSS服务接口
 * 提供阿里云OSS文件上传功能
 * 
 * @author tool-ai-service
 * @since 1.0
 */
public interface OssService {
    /**
     * 上传文件到OSS
     * 
     * @param inputStream 文件输入流
     * @param fileName 文件名（包含路径，如：name-sign/20231209/xxx.jpg）
     * @param contentType 文件类型，如：image/jpeg
     * @return 文件访问URL（OSS公网可访问地址）
     */
    String uploadFile(InputStream inputStream, String fileName, String contentType);
    
    /**
     * 上传字节数组到OSS
     * 
     * @param bytes 文件字节数组
     * @param fileName 文件名（包含路径）
     * @param contentType 文件类型
     * @return 文件访问URL（OSS公网可访问地址）
     */
    String uploadFile(byte[] bytes, String fileName, String contentType);
}

