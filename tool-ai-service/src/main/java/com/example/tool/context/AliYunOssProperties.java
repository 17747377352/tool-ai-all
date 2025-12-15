package com.example.tool.context;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云OSS配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class AliYunOssProperties {
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private String domain; // OSS域名，用于拼接完整URL，如：https://your-bucket.oss-cn-hangzhou.aliyuncs.com
}




