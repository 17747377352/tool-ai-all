package com.example.simvoice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试接口控制器
 */
@RestController
@RequestMapping("/api/test")
@Slf4j
public class TestController {

    /**
     * POST 测试接口
     * @param requestBody 请求体（可选）
     * @return 测试响应数据
     */
    @PostMapping("/hello")
    public Map<String, Object> testPost(@RequestParam("file") MultipartFile file, @RequestParam Map<String, Object> requestBody)  {
        String originalFilename = file.getOriginalFilename();
        log.info("文件名称: {}", originalFilename);
        log.info("requestBody: {}", requestBody);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "测试接口调用成功");
        response.put("data", "Hello, this is a test POST endpoint!");
        response.put("timestamp", System.currentTimeMillis());
        requestBody.put("file", originalFilename);
        if (requestBody != null && !requestBody.isEmpty()) {
            response.put("received", requestBody);
        }
        return response;
    }
}


