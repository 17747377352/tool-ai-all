package com.example.simvoice.service;

import com.example.simvoice.dto.*;

/**
 * 工具服务接口
 * 提供各种AI工具功能的核心业务逻辑
 * 
 * @author ai-translation-service
 * @since 1.0
 */
public interface ToolService {
    /**
     * AI头像生成
     * 
     * @param openid 用户openid
     * @param dto AI头像生成请求参数
     * @return 生成的头像图片URL
     */
    String generateAiAvatar(String openid, AiAvatarDTO dto);

    /**
     * 老照片修复（火山引擎）- 单张
     *
     * @param openid 用户openid
     * @param dto 修复请求参数
     * @return 修复后图片URL（IMAGE_LIST 单图格式）
     */
    String restoreOldPhoto(String openid, OldPhotoRestoreDTO dto);
    
    /**
     * 老照片批量修复（火山引擎）
     *
     * @param openid 用户openid
     * @param dto 批量修复请求参数，包含图片URL数组
     * @return 修复后的图片URL列表（IMAGE_LIST格式）
     */
    String batchRestoreOldPhoto(String openid, BatchRestoreOldPhotoDTO dto);
    
    /**
     * 即时翻译
     * 支持中译英、中译日、中译蒙、英译中、日译中、蒙译中
     *
     * @param openid 用户openid
     * @param dto 翻译请求参数，包含文本、源语言和目标语言
     * @return 翻译结果
     */
    String translate(String openid, TranslateDTO dto);
    
    /**
     * 创建图片生成任务（统一入口）
     * 支持三种模式：
     * 1-字生图 2-图生图 3-模版同款 4-模版参考图
     *
     * @param openid 用户openid
     * @param dto 任务创建请求参数
     * @return 任务ID
     */
    Long createImageGenerateTask(String openid, ImageGenerateTaskCreateDTO dto);
}

