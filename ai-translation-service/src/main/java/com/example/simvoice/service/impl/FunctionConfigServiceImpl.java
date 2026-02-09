package com.example.simvoice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.simvoice.dto.FunctionConfigDTO;
import com.example.simvoice.entity.FunctionConfig;
import com.example.simvoice.mapper.FunctionConfigMapper;
import com.example.simvoice.service.FunctionConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 功能配置服务实现类
 * 实现功能入口配置相关服务
 * 从数据库读取功能配置
 * 
 * @author ai-translation-service
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FunctionConfigServiceImpl implements FunctionConfigService {

    private final FunctionConfigMapper functionConfigMapper;

    /**
     * 获取所有启用的功能配置列表
     * 从数据库读取配置，只返回启用的功能，按排序顺序返回
     */
    @Override
    public List<FunctionConfigDTO> getEnabledFunctions() {
        // 从数据库查询所有启用的功能配置
        List<FunctionConfig> configs = functionConfigMapper.selectList(
                new LambdaQueryWrapper<FunctionConfig>()
                        .eq(FunctionConfig::getEnabled, true)
                        .orderByAsc(FunctionConfig::getSortOrder)
        );
        
        // 转换为DTO
        return configs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 将实体转换为DTO
     */
    private FunctionConfigDTO convertToDTO(FunctionConfig config) {
        FunctionConfigDTO dto = new FunctionConfigDTO();
        dto.setType(config.getType());
        dto.setName(config.getName());
        dto.setRoute(config.getRoute());
        dto.setEnabled(config.getEnabled());
        dto.setSortOrder(config.getSortOrder());
        return dto;
    }
}

