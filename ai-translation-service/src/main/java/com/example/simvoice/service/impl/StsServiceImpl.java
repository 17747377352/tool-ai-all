package com.example.simvoice.service.impl;

import com.alibaba.fastjson.JSON;
import com.example.simvoice.context.AliYunOssProperties;
import com.example.simvoice.service.StsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.TimeZone;

import static java.util.Base64.getEncoder;

/**
 * STS临时凭证服务实现
 * 注意：此实现使用主账号AccessKey模拟STS，生产环境建议使用RAM角色和真正的STS AssumeRole
 */
@Slf4j
@Service
public class StsServiceImpl implements StsService {

    private final AliYunOssProperties ossProperties;
    
    @Value("${aliyun.oss.region:cn-beijing}")
    private String region;
    
    @Value("${aliyun.sts.duration-seconds:3600}")
    private Long durationSeconds;

    public StsServiceImpl(AliYunOssProperties ossProperties) {
        this.ossProperties = ossProperties;
    }

    @PostConstruct
    public void init() {
        try {
            // 从endpoint提取region，如：oss-cn-beijing.aliyuncs.com -> cn-beijing
            String endpoint = ossProperties.getEndpoint();
            if (endpoint.contains("oss-cn-")) {
                int start = endpoint.indexOf("oss-cn-") + 7;
                int end = endpoint.indexOf(".aliyuncs.com");
                if (end > start) {
                    region = endpoint.substring(start, end);
                }
            }
            
            log.info("STS服务初始化成功，region: {}", region);
        } catch (Exception e) {
            log.error("STS服务初始化失败", e);
        }
    }

    @Override
    public Map<String, Object> getPostObjectSignature(String fileName) {
        try {
            // 构建OSS host
            String endpoint = ossProperties.getEndpoint();
            String host = ossProperties.getBucketName() + "." + endpoint.replace("https://", "").replace("http://", "");
            
            // 生成过期时间（1小时后）
            long expireTime = System.currentTimeMillis() / 1000 + durationSeconds;
            
            // 构建Policy，使用UTC时间，格式：yyyy-MM-dd'T'HH:mm:ss'Z'
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date expiration = new Date(expireTime * 1000);
            String expirationStr = sdf.format(expiration);
            
            // 构建Policy
            Map<String, Object> policyMap = new HashMap<>();
            policyMap.put("expiration", expirationStr);
            
            List<Object> conditions = new ArrayList<>();
            // 限制文件大小（100MB）
            conditions.add(Arrays.asList("content-length-range", 0, 104857600));
            // 限制文件路径前缀
            if (fileName != null && !fileName.isEmpty()) {
                int lastSlashIndex = fileName.lastIndexOf('/');
                if (lastSlashIndex >= 0) {
                    conditions.add(Arrays.asList("starts-with", "$key", fileName.substring(0, lastSlashIndex + 1)));
                } else {
                    conditions.add(Arrays.asList("starts-with", "$key", "upload/"));
                }
            } else {
                conditions.add(Arrays.asList("starts-with", "$key", "upload/"));
            }
            policyMap.put("conditions", conditions);
            
            // 将Policy转为JSON并Base64编码
            String policyJson = JSON.toJSONString(policyMap);
            String policy = getEncoder().encodeToString(policyJson.getBytes(StandardCharsets.UTF_8));
            
            // 使用HMAC-SHA1签名
            Mac hmac = Mac.getInstance("HmacSHA1");
            hmac.init(new SecretKeySpec(ossProperties.getAccessKeySecret().getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            byte[] signatureBytes = hmac.doFinal(policy.getBytes(StandardCharsets.UTF_8));
            String signature = getEncoder().encodeToString(signatureBytes);
            
            // 返回签名信息
            Map<String, Object> result = new HashMap<>();
            result.put("accessKeyId", ossProperties.getAccessKeyId());
            result.put("policy", policy);
            result.put("signature", signature);
            result.put("host", host);
            result.put("bucket", ossProperties.getBucketName());
            result.put("key", fileName);
            result.put("expire", expireTime);
            
            return result;
            
        } catch (Exception e) {
            log.error("生成OSS PostObject签名失败", e);
            throw new RuntimeException("生成签名失败: " + e.getMessage());
        }
    }
}

