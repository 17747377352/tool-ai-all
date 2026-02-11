package com.example.simvoice.controller;

import com.example.simvoice.dto.FunctionConfigDTO;
import com.example.simvoice.result.Result;
import com.example.simvoice.service.FunctionConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 功能配置控制器
 * 提供功能入口配置相关接口
 * 
 * @author ai-translation-service
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/function")
@RequiredArgsConstructor
public class FunctionConfigController {

    private final FunctionConfigService functionConfigService;

    /**
     * GET /api/function/list
     * 获取所有启用的功能入口列表
     * 用于前端首页展示功能入口
     */
    @GetMapping("/list")
    public Result<List<FunctionConfigDTO>> getFunctionList() {
        List<FunctionConfigDTO> functions = functionConfigService.getEnabledFunctions();
        return Result.success("获取成功", functions);
    }
}

