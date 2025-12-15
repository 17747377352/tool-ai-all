package com.example.tool.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 图片代理控制器
 * 用于解决OSS默认域名强制下载的问题
 * 
 * OSS限制：使用OSS默认域名访问文件会触发浏览器强制下载（Content-Disposition: attachment）
 * 解决方案：后端作为代理，下载OSS图片后以inline方式返回
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/proxy")
public class ImageProxyController {

    /**
     * 图片代理接口
     * 从OSS下载图片，然后以Content-Disposition: inline方式返回
     * 
     * @param imageUrl 图片URL（OSS地址）
     * @return 图片字节流
     */
    @GetMapping("/image")
    public ResponseEntity<byte[]> proxyImage(@RequestParam("url") String imageUrl) {
        try {
            log.info("图片代理请求: {}", imageUrl);

            // 安全检查：只允许代理特定域名的图片
            if (!isAllowedDomain(imageUrl)) {
                log.warn("不允许代理该域名的图片: {}", imageUrl);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }

            // 从OSS下载图片
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
