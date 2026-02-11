package com.example.simvoice.service;

import com.example.simvoice.dto.FunctionConfigDTO;

import java.util.List;

/**
 * 功能配置服务接口
 * 提供功能入口配置相关服务
 * 
 * @author ai-translation-service
 * @since 1.0
 */
public interface FunctionConfigService {
    /**
     * 获取所有启用的功能配置列表
     * 按排序顺序返回
     * 
     * @return 功能配置列表
     */
    List<FunctionConfigDTO> getEnabledFunctions();
}

