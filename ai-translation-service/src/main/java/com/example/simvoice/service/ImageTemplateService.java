package com.example.simvoice.service;

import com.example.simvoice.entity.ImageTemplate;

import java.util.List;

/**
 * 图片模版服务接口
 */
public interface ImageTemplateService {
    
    /**
     * 获取所有启用的模版列表
     * @return 模版列表
     */
    List<ImageTemplate> getAllEnabledTemplates();
    
    /**
     * 根据ID获取模版
     * @param templateId 模版ID
     * @return 模版信息
     */
    ImageTemplate getTemplateById(Long templateId);
}

