package com.example.tool.context;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 本地文件存储配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "local.file")
public class LocalFileProperties {
    /**
     * 本地存储基础路径（如：/data/tool/uploads）
     */
    private String basePath = "./uploads";
    
    /**
     * 后端服务基础URL（用于生成代理URL，如：http://localhost:8080）
     */
    private String baseUrl = "http://localhost:8080";
}


