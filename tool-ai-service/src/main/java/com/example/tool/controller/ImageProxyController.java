package com.example.tool.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.example.tool.context.LocalFileProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 图片代理控制器
 * 1. 代理外部图片（OSS、火山引擎等），解决强制下载问题
 * 2. 提供本地文件访问接口，用于预览本地存储的图片
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/image")
@RequiredArgsConstructor
public class ImageProxyController {

    private final LocalFileProperties localFileProperties;

    /**
     * 本地文件访问接口
     * 用于预览本地存储的图片文件
     * 
     * @param path 文件相对路径（如：name-sign/20231209/xxx.jpg）
     * @return 图片字节流
     */
    @GetMapping("/local")
    public ResponseEntity<byte[]> getLocalImage(@RequestParam("path") String path) {
        try {
            // URL解码路径参数
            String decodedPath = URLDecoder.decode(path, "UTF-8");
            log.info("本地图片访问请求: path={}, decodedPath={}", path, decodedPath);
            
            // 构建文件完整路径
            Path baseDir = Paths.get(localFileProperties.getBasePath());
            Path filePath = baseDir.resolve(decodedPath).normalize();
            
            // 安全检查：确保文件在基础目录内（防止路径遍历攻击）
            if (!filePath.startsWith(baseDir.normalize())) {
                log.warn("非法路径访问尝试: path={}, decodedPath={}", path, decodedPath);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }
            
            File file = filePath.toFile();
            if (!file.exists() || !file.isFile()) {
                log.warn("文件不存在: path={}, decodedPath={}", path, decodedPath);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            
            // 读取文件
            byte[] fileBytes = Files.readAllBytes(filePath);
            
            // 根据文件扩展名确定Content-Type
            String contentType = getContentType(file.getName());
            
            // 构建响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline");
            headers.setContentLength(fileBytes.length);
            
            log.info("本地图片访问成功: path={}, decodedPath={}, ContentType={}, Size={} bytes", path, decodedPath, contentType, fileBytes.length);
            return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("本地图片访问失败: path={}", path, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    /**
     * 根据文件扩展名获取Content-Type
     */
    private String getContentType(String fileName) {
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG_VALUE;
        } else if (lowerName.endsWith(".png")) {
            return MediaType.IMAGE_PNG_VALUE;
        } else if (lowerName.endsWith(".gif")) {
            return MediaType.IMAGE_GIF_VALUE;
        } else if (lowerName.endsWith(".webp")) {
            return "image/webp";
        } else {
            return MediaType.IMAGE_JPEG_VALUE; // 默认
        }
    }

    /**
     * 外部图片代理接口（保留兼容性）
     * 从OSS或其他外部源下载图片，然后以Content-Disposition: inline方式返回
     * 
     * @param imageUrl 图片URL（外部地址）
     * @return 图片字节流
     */
    @GetMapping("/proxy")
    public ResponseEntity<byte[]> proxyImage(@RequestParam("url") String imageUrl) {
        try {
            log.info("图片代理请求: {}", imageUrl);

            // 安全检查：只允许代理特定域名的图片
            if (!isAllowedDomain(imageUrl)) {
                log.warn("不允许代理该域名的图片: {}", imageUrl);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }

            // 从外部源下载图片
            HttpResponse response = HttpRequest.get(imageUrl)
                    .timeout(30000) // 30秒超时
                    .execute();

            if (response.getStatus() != 200) {
                log.error("下载图片失败，状态码: {}, URL: {}", response.getStatus(), imageUrl);
                return ResponseEntity.status(response.getStatus()).body(null);
            }
            byte[] imageBytes = response.bodyBytes();
            // 从原始响应中获取Content-Type
            String contentType = response.header("Content-Type");
            if (contentType == null || contentType.isEmpty()) {
                // 默认使用 image/jpeg
                contentType = MediaType.IMAGE_JPEG_VALUE;
            }
            // 构建响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            // ⭐ 关键：设置为inline，浏览器和小程序可以直接预览
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline");
            headers.setContentLength(imageBytes.length);
            log.info("图片代理成功: URL={}, ContentType={}, Size={} bytes", imageUrl, contentType, imageBytes.length);
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("图片代理失败: {}", imageUrl, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 检查是否允许代理该域名
     * 安全措施：防止被用作开放代理
     */
    private boolean isAllowedDomain(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }

        // 只允许代理以下域名的图片
        return url.contains("aliyuncs.com") || // OSS
                url.contains("volces.com") || // 火山引擎
                url.contains("douyinpic.com"); // 抖音
    }
}
