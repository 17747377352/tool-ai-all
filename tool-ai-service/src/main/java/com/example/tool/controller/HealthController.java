package com.example.tool.controller;

import com.example.tool.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 提供服务健康状态检查接口，用于监控和负载均衡器健康检查
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@RestController
public class HealthController {

    /**
     * 健康检查接口（根路径）
     * 
     * @return 统一返回结果，包含服务状态信息
     */
    @GetMapping("/")
    public Result<Map<String, String>> health() {
        Map<String, String> data = new HashMap<>();
        data.put("status", "ok");
        data.put("service", "tool-ai-service");
        return Result.success(data);
    }

    /**
     * 健康检查接口（/health路径）
     * 
     * @return 统一返回结果，包含服务状态信息
     */
    @GetMapping("/health")
    public Result<Map<String, String>> healthCheck() {
        Map<String, String> data = new HashMap<>();
        data.put("status", "ok");
        data.put("service", "tool-ai-service");
        return Result.success(data);
    }
}

