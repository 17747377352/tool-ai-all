package com.example.tool.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.example.tool.context.AliYunOssProperties;
import com.example.tool.exception.BusinessException;
import com.example.tool.result.ResultCode;
import com.example.tool.service.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * OSS服务实现类
 * 实现阿里云OSS文件上传功能
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OssServiceImpl implements OssService {

    private final AliYunOssProperties ossProperties;

    /**
     * 上传文件到OSS
     * 
     * @param inputStream 文件输入流
     * @param fileName    文件名（包含路径，如：name-sign/20231209/xxx.jpg）
     * @param contentType 文件类型，如：image/jpeg
     * @return 文件访问URL
     * @throws BusinessException 如果上传失败
     */
    @Override
    public String uploadFile(InputStream inputStream, String fileName, String contentType) {
        OSS ossClient = null;
        try {
            // 处理endpoint（移除https://前缀，OSS SDK不需要）
            String endpoint = ossProperties.getEndpoint();
            if (endpoint.startsWith("https://")) {
                endpoint = endpoint.substring(8);
            } else if (endpoint.startsWith("http://")) {
                endpoint = endpoint.substring(7);
            }

            // 创建OSS客户端
            ossClient = new OSSClientBuilder().build(
                    endpoint,
                    ossProperties.getAccessKeyId(),
                    ossProperties.getAccessKeySecret());

            // ⚠️ 重要：先创建并设置metadata，再创建PutObjectRequest
            com.aliyun.oss.model.ObjectMetadata metadata = new com.aliyun.oss.model.ObjectMetadata();

            // 设置ContentType
            if (contentType != null && !contentType.isEmpty()) {
                metadata.setContentType(contentType);
            }

            // ⭐ 关键修复：对所有图片文件设置Content-Disposition为inline，确保浏览器直接预览而不是下载
            if (contentType != null && contentType.startsWith("image/")) {
                metadata.setContentDisposition("inline");
                log.info("设置图片Content-Disposition为inline: fileName={}, contentType={}", fileName, contentType);
            }

            // 创建PutObjectRequest对象，并应用metadata
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    ossProperties.getBucketName(),
                    fileName,
                    inputStream,
                    metadata // ⭐ 在构造函数中直接传入metadata
            );

            // 上传文件
            ossClient.putObject(putObjectRequest);

            // 构建文件访问URL
            String url;
            if (ossProperties.getDomain() != null && !ossProperties.getDomain().isEmpty()) {
                // 使用自定义域名
                url = ossProperties.getDomain();
                if (!url.endsWith("/")) {
                    url += "/";
                }
                url += fileName;
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://" + url;
                }
            } else {
                // 使用OSS默认域名（使用处理后的endpoint）
                url = "https://" + ossProperties.getBucketName() + "." + endpoint + "/" + fileName;
            }

            log.info("文件上传成功: {}", url);
            return url;
        } catch (Exception e) {
            log.error("文件上传失败: {}", fileName, e);
            throw new BusinessException(ResultCode.ERROR, "文件上传失败: " + e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 上传字节数组到OSS
     * 
     * @param bytes       文件字节数组
     * @param fileName    文件名（包含路径）
     * @param contentType 文件类型
     * @return 文件访问URL
     */
    @Override
    public String uploadFile(byte[] bytes, String fileName, String contentType) {
        return uploadFile(new ByteArrayInputStream(bytes), fileName, contentType);
    }
}
