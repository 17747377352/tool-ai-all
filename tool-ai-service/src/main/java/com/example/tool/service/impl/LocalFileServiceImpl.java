package com.example.tool.service.impl;

import com.example.tool.context.LocalFileProperties;
import com.example.tool.exception.BusinessException;
import com.example.tool.result.ResultCode;
import com.example.tool.service.LocalFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 本地文件存储服务实现类
 * 实现本地文件上传功能
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocalFileServiceImpl implements LocalFileService {

    private final LocalFileProperties localFileProperties;

    /**
     * 上传文件到本地存储
     * 
     * @param inputStream 文件输入流
     * @param fileName    文件名（包含路径，如：name-sign/20231209/xxx.jpg）
     * @param contentType 文件类型，如：image/jpeg
     * @return 文件访问URL（代理URL格式）
     * @throws BusinessException 如果上传失败
     */
    @Override
    public String uploadFile(InputStream inputStream, String fileName, String contentType) {
        try {
            // 构建本地文件完整路径
            Path baseDir = Paths.get(localFileProperties.getBasePath());
            Path filePath = baseDir.resolve(fileName);
            
            // 确保父目录存在
            Files.createDirectories(filePath.getParent());
            
            // 写入文件
            try (FileOutputStream fos = new FileOutputStream(filePath.toFile());
                 BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }
                bos.flush();
            }
            
            // 构建代理URL（通过后端接口访问）
            // 使用URLEncoder对路径进行编码，确保特殊字符正确处理
            String encodedPath = URLEncoder.encode(fileName, "UTF-8");
            String proxyUrl = String.format("%s/api/image/local?path=%s", 
                    localFileProperties.getBaseUrl(), encodedPath);
            
            log.info("文件上传成功: 本地路径={}, 代理URL={}", filePath, proxyUrl);
            return proxyUrl;
            
        } catch (Exception e) {
            log.error("文件上传失败: {}", fileName, e);
            throw new BusinessException(ResultCode.ERROR, "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传字节数组到本地存储
     * 
     * @param bytes       文件字节数组
     * @param fileName    文件名（包含路径）
     * @param contentType 文件类型
     * @return 文件访问URL（代理URL格式）
     */
    @Override
    public String uploadFile(byte[] bytes, String fileName, String contentType) {
        return uploadFile(new ByteArrayInputStream(bytes), fileName, contentType);
    }
}

