package com.example.tool.controller;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 测试控制器
 * 提供测试用的接口
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {

    /**
     * 文件上传测试接口
     * 用于测试文件上传功能，不返回任何内容
     * 
     * @param file 上传的文件
     */
    @PostMapping("/upload")
    public void uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                log.warn("上传的文件为空");
                return;
            }
            
            String originalFilename = file.getOriginalFilename();
            long fileSize = file.getSize();
            String contentType = file.getContentType();
            
            log.info("收到文件上传: 文件名={}, 大小={} bytes, 类型={}", 
                    originalFilename, fileSize, contentType);
            
            // 这里可以添加文件处理逻辑，比如保存到本地或上传到OSS
            // 测试用，暂时只记录日志
            
        } catch (Exception e) {
            log.error("文件上传处理异常", e);
        }
    }
}

