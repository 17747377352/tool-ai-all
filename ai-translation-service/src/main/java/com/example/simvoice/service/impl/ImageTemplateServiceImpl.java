package com.example.simvoice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.simvoice.entity.ImageTemplate;
import com.example.simvoice.exception.BusinessException;
import com.example.simvoice.mapper.ImageTemplateMapper;
import com.example.simvoice.result.ResultCode;
import com.example.simvoice.service.ImageTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 图片模版服务实现类
 */
@Service
@RequiredArgsConstructor
public class ImageTemplateServiceImpl implements ImageTemplateService {
    
    private final ImageTemplateMapper imageTemplateMapper;
    
    @Override
    public List<ImageTemplate> getAllEnabledTemplates() {
        LambdaQueryWrapper<ImageTemplate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ImageTemplate::getIsEnabled, true)
                .orderByAsc(ImageTemplate::getSortOrder)
                .orderByDesc(ImageTemplate::getCreateTime);
        return imageTemplateMapper.selectList(queryWrapper);
    }
    
    @Override
    public ImageTemplate getTemplateById(Long templateId) {
        ImageTemplate template = imageTemplateMapper.selectById(templateId);
        if (template == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "模版不存在");
        }
        if (!template.getIsEnabled()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "模版已禁用");
        }
        return template;
    }
}

