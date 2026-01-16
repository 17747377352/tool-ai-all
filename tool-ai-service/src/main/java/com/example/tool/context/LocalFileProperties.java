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
     * 这是浏览器访问的地址
     */
    private String baseUrl = "http://localhost:8080";
    
    /**
     * OnlyOffice 容器访问后端服务的 URL
     * 在 macOS/Windows 上，容器需要使用 host.docker.internal 访问宿主机
     * 在 Linux 上，可以使用宿主机 IP 或 host.docker.internal
     * 如果为空，则使用 baseUrl
     */
    private String onlyOfficeBaseUrl = "";
    
    /**
     * 获取 OnlyOffice 容器应该使用的 base URL
     * 如果 onlyOfficeBaseUrl 已配置，使用它；否则使用 baseUrl
     */
    public String getOnlyOfficeBaseUrlForContainer() {
        if (onlyOfficeBaseUrl != null && !onlyOfficeBaseUrl.isEmpty()) {
            return onlyOfficeBaseUrl;
        }
        return baseUrl;
    }
}







