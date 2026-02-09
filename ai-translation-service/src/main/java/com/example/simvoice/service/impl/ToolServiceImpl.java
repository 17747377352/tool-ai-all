package com.example.simvoice.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.simvoice.dto.*;
import com.example.simvoice.entity.ImageGenerateRecord;
import com.example.simvoice.entity.PhotoRestoreRecord;
import com.example.simvoice.entity.TranslateRecord;
import com.example.simvoice.exception.BusinessException;
import com.example.simvoice.mapper.ImageGenerateRecordMapper;
import com.example.simvoice.mapper.PhotoRestoreRecordMapper;
import com.example.simvoice.mapper.TranslateRecordMapper;
import com.example.simvoice.result.ResultCode;
import com.example.simvoice.entity.ImageGenerateTask;
import com.example.simvoice.entity.ImageTemplate;
import com.example.simvoice.service.HuoshanImageService;
import com.example.simvoice.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 工具服务实现类
 * 实现各种AI工具功能的核心业务逻辑
 * 
 * @author ai-translation-service
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ToolServiceImpl implements ToolService {

    private final UserQuotaService userQuotaService;
    private final ImageGenerateRecordMapper imageGenerateRecordMapper;
    private final PhotoRestoreRecordMapper photoRestoreRecordMapper;
    private final TranslateRecordMapper translateRecordMapper;
    private final HuoshanImageService huoshanImageService;
    private final NiuTransService niuTransService;
    private final ImageTemplateService imageTemplateService;
    private final ImageGenerateTaskService imageGenerateTaskService;

    /**
     * AI头像生成
     * 支持两种模式：
     * 1. 字生图：仅提供prompt，根据文字描述生成头像
     * 2. 图生图：提供imageUrl和prompt，基于上传的图片生成新头像
     *
     * @param openid 用户openid
     * @param dto    AI头像生成请求参数，包含prompt、style和可选的imageUrl
     * @return 生成的头像图片URL
     */
    @Override
    public String generateAiAvatar(String openid, AiAvatarDTO dto) {
        // 1. 检查并消费额度
        userQuotaService.checkAndConsume(openid, 2);

        // 2. 参数校验
        if (dto.getPrompt() == null || dto.getPrompt().trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "提示词不能为空");
        }

        String resultUrl;

        // 3. 根据是否有imageUrl决定使用图生图还是字生图
        if (dto.getImageUrl() != null && !dto.getImageUrl().trim().isEmpty()) {
            // 图生图模式：基于上传的图片生成新头像
            log.info("使用图生图模式生成头像: openid={}, imageUrl={}, prompt={}, style={}",
                    openid, dto.getImageUrl(), dto.getPrompt(), dto.getStyle());
            resultUrl = huoshanImageService.generateAvatarFromImage(
                    dto.getImageUrl(),
                    dto.getPrompt(),
                    dto.getStyle() != null ? dto.getStyle() : "realistic");
        } else {
            // 字生图模式：仅根据文字描述生成头像
            log.info("使用字生图模式生成头像: openid={}, prompt={}, style={}",
                    openid, dto.getPrompt(), dto.getStyle());
            resultUrl = huoshanImageService.generateAvatarFromText(
                    dto.getPrompt(),
                    dto.getStyle() != null ? dto.getStyle() : "realistic");
        }

        // 4. 保存生成记录到图片生成记录表
        ImageGenerateRecord record = new ImageGenerateRecord();
        record.setOpenid(openid);
        record.setInputData(JSONObject.toJSONString(dto));
        record.setResultUrl(resultUrl);
        record.setCreateTime(LocalDateTime.now());
        imageGenerateRecordMapper.insert(record);

        return resultUrl;
    }

    /**
     * 老照片修复（火山引擎）- 单张
     *
     * @param openid 用户openid
     * @param dto    修复请求参数
     * @return 修复后图片URL（IMAGE_LIST 单图格式）
     */
    @Override
    public String restoreOldPhoto(String openid, OldPhotoRestoreDTO dto) {
        // 检查并消费额度（功能类型3：老照片修复）
        userQuotaService.checkAndConsume(openid, 3);

        if (dto.getImageUrl() == null || dto.getImageUrl().trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "图片地址不能为空");
        }

        try {
            // 使用火山引擎对老照片进行修复/增强
            // 固定的中文提示语：请将照片修复清晰，提高分辨率，消除噪点，智能修复人物面部细节。
            String prompt = "请将照片修复清晰，提高分辨率，消除噪点，智能修复人物面部细节。";
            String enhancedUrl = huoshanImageService.enhancePhoto(dto.getImageUrl().trim(), prompt);

            // 返回 IMAGE_LIST 单图格式，便于前端统一展示
            String resultUrl = String.format("IMAGE_LIST:[\"%s\"]", enhancedUrl);

            // 记录生成记录到老照片修复记录表
            PhotoRestoreRecord record = new PhotoRestoreRecord();
            record.setOpenid(openid);
            record.setInputData(JSONObject.toJSONString(dto));
            record.setResultUrl(resultUrl);
            record.setCreateTime(LocalDateTime.now());
            photoRestoreRecordMapper.insert(record);
            return resultUrl;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("老照片修复失败", e);
            throw new RuntimeException("老照片修复失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 老照片批量修复（火山引擎）
     *
     * @param openid 用户openid
     * @param dto    批量修复请求参数，包含图片URL列表
     * @return 修复后的图片URL列表（IMAGE_LIST格式）
     */
    @Override
    public String batchRestoreOldPhoto(String openid, BatchRestoreOldPhotoDTO dto) {
        // 检查限流（每张图片都算一次）
        if (dto.getImageUrls() == null || dto.getImageUrls().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "图片列表不能为空");
        }
        
        if (dto.getImageUrls().size() > 10) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "最多只能批量处理10张图片");
        }
        
        int imageCount = dto.getImageUrls().size();
        
        // 检查并消费额度（批量修复每张都算一次使用，功能类型3：老照片修复）
        for (int i = 0; i < imageCount; i++) {
            userQuotaService.checkAndConsume(openid, 6);
        }
        
        try {
            List<String> resultUrls = new java.util.ArrayList<>();
            // 固定的中文提示语
            String prompt = "请将照片修复清晰，提高分辨率，消除噪点，智能修复人物面部细节。";
            
            // 批量处理图片
            for (String imageUrl : dto.getImageUrls()) {
                if (imageUrl == null || imageUrl.trim().isEmpty()) {
                    continue;
                }
                
                try {
                    // 调用火山引擎进行老照片修复/增强
                    String enhancedUrl = huoshanImageService.enhancePhoto(imageUrl.trim(), prompt);
                    resultUrls.add(enhancedUrl);
                } catch (Exception e) {
                    log.error("批量修复中单张图片处理失败: imageUrl={}", imageUrl, e);
                    // 继续处理其他图片，不中断整个流程
                }
            }
            
            if (resultUrls.isEmpty()) {
                throw new BusinessException(ResultCode.ERROR, "所有图片处理失败，请检查图片URL是否有效");
            }
            
            // 构建结果URL列表
            String resultUrlList = resultUrls.stream()
                    .map(url -> "\"" + url + "\"")
                    .collect(java.util.stream.Collectors.joining(","));
            String resultUrl = String.format("IMAGE_LIST:[%s]", resultUrlList);
            
            // 记录生成记录到老照片修复记录表
            PhotoRestoreRecord record = new PhotoRestoreRecord();
            record.setOpenid(openid);
            record.setInputData(JSONObject.toJSONString(dto));
            record.setResultUrl(resultUrl);
            record.setCreateTime(LocalDateTime.now());
            photoRestoreRecordMapper.insert(record);
            
            log.info("批量修复成功: openid={}, 处理数量={}, 成功数量={}", 
                    openid, dto.getImageUrls().size(), resultUrls.size());
            
            return resultUrl;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量修复失败", e);
            throw new RuntimeException("批量修复失败: " + e.getMessage(), e);
        }
    }

    /**
     * 即时翻译
     * 支持中译英、中译日、中译蒙、英译中、日译中、蒙译中
     *
     * @param openid 用户openid
     * @param dto 翻译请求参数
     * @return 翻译结果
     */
    @Override
    public String translate(String openid, TranslateDTO dto) {
        // 1. 检查限流（类型8：即时翻译，如果后续有新的类型编号，可以调整）
        // 根据实际需求，可以扩展UserQuota实体添加translateQuota字段
        // userQuotaService.checkAndConsume(openid, 8);

        // 2. 参数校验
        if (dto.getText() == null || dto.getText().trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "翻译文本不能为空");
        }
        if (dto.getFrom() == null || dto.getTo() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "源语言和目标语言不能为空");
        }

        // 3. 调用翻译服务
        String result = niuTransService.translate(dto.getText(), dto.getFrom(), dto.getTo());

        // 4. 保存生成记录到翻译记录表
        JSONObject recordData = new JSONObject();
        recordData.put("originalText", dto.getText());
        recordData.put("from", dto.getFrom());
        recordData.put("to", dto.getTo());
        recordData.put("translatedText", result);
        
        TranslateRecord record = new TranslateRecord();
        record.setOpenid(openid);
        record.setInputData(JSONObject.toJSONString(dto));
        record.setResultUrl(recordData.toJSONString());
        record.setCreateTime(LocalDateTime.now());
        translateRecordMapper.insert(record);

        return result;
    }

    /**
     * 创建图片生成任务（统一入口）
     * 支持模式：
     * 1-字生图 2-图生图 3-模版同款 4-模版参考图
     */
    @Override
    public Long createImageGenerateTask(String openid, ImageGenerateTaskCreateDTO dto) {
        // 1. 检查并消费额度（生成图片）
        userQuotaService.checkAndConsume(openid, 2);

        if (dto.getGenerateMode() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "生成模式不能为空");
        }

        Integer mode = dto.getGenerateMode();
        String style = dto.getStyle() != null ? dto.getStyle() : "realistic";

        ImageGenerateTask task = new ImageGenerateTask();
        task.setOpenid(openid);
        task.setGenerateMode(mode);
        task.setStyle(style);

        switch (mode) {
            case 1:
                // 1: 字生图（普通）
                if (dto.getPrompt() == null || dto.getPrompt().trim().isEmpty()) {
                    throw new BusinessException(ResultCode.PARAM_ERROR, "提示词不能为空");
                }
                task.setPrompt(dto.getPrompt().trim());
                task.setImageUrl(null);
                task.setTemplateId(null);
                break;
            case 2:
                // 2: 图生图（普通）
                if (dto.getImageUrl() == null || dto.getImageUrl().trim().isEmpty()) {
                    throw new BusinessException(ResultCode.PARAM_ERROR, "参考图片URL不能为空");
                }
                if (dto.getPrompt() == null || dto.getPrompt().trim().isEmpty()) {
                    throw new BusinessException(ResultCode.PARAM_ERROR, "提示词不能为空");
                }
                task.setPrompt(dto.getPrompt().trim());
                task.setImageUrl(dto.getImageUrl().trim());
                task.setTemplateId(null);
                break;
            case 3:
            case 4:
                // 3/4: 模版同款 / 模版参考图
                if (dto.getTemplateId() == null) {
                    throw new BusinessException(ResultCode.PARAM_ERROR, "模版ID不能为空");
                }
                ImageTemplate template = imageTemplateService.getTemplateById(dto.getTemplateId());
                task.setTemplateId(dto.getTemplateId());
                task.setStyle(template.getStyle());

                if (mode == 3) {
                    // 模版同款：仅使用模版提示词
                    task.setPrompt(template.getPrompt());
                    task.setImageUrl(null);
                } else {
                    // 模版参考图：图片来自模版，提示词可覆盖
                    task.setImageUrl(template.getImageUrl());
                    if (dto.getPrompt() != null && !dto.getPrompt().trim().isEmpty()) {
                        task.setPrompt(dto.getPrompt().trim());
                    } else {
                        task.setPrompt(template.getPrompt());
                    }
                }
                break;
            default:
                throw new BusinessException(ResultCode.PARAM_ERROR, "生成模式无效，应为1/2/3/4");
        }

        Long taskId = imageGenerateTaskService.createTask(task);

        log.info("创建图片生成任务: openid={}, taskId={}, mode={}, templateId={}",
                openid, taskId, mode, task.getTemplateId());

        return taskId;
    }
    
    /**
     * 执行图片生成任务
     * @param taskId 任务ID
     */
    public void executeImageGenerateTask(Long taskId) {
        try {
            // 1. 更新任务状态为生成中
            imageGenerateTaskService.updateTaskStatus(taskId, 1, null, null);
            
            // 2. 获取任务信息
            ImageGenerateTask task = imageGenerateTaskService.getTaskById(taskId);
            
            log.info("开始执行图片生成任务: taskId={}, openid={}, mode={}", 
                    taskId, task.getOpenid(), task.getGenerateMode());
            
            String resultUrl;

            // 3. 根据生成模式调用不同的生成方法：
            // 1 / 3：字生图（仅提示词）
            // 2 / 4：图生图（参考图 + 提示词）
            Integer mode = task.getGenerateMode();
            if (mode == null) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "生成模式不能为空");
            }

            String style = task.getStyle() != null ? task.getStyle() : "realistic";
            if (mode == 1 || mode == 3) {
                // 字生图
                resultUrl = huoshanImageService.generateAvatarFromText(task.getPrompt(), style);
            } else if (mode == 2 || mode == 4) {
                // 图生图
                resultUrl = huoshanImageService.generateAvatarFromImage(task.getImageUrl(), task.getPrompt(), style);
            } else {
                throw new BusinessException(ResultCode.PARAM_ERROR, "生成模式无效: " + mode);
            }
            
            // 4. 更新任务状态为已完成
            imageGenerateTaskService.updateTaskStatus(taskId, 2, resultUrl, null);
            
            // 5. 保存生成记录到图片生成记录表
            ImageGenerateRecord record = new ImageGenerateRecord();
            record.setOpenid(task.getOpenid());
            JSONObject inputData = new JSONObject();
            inputData.put("templateId", task.getTemplateId());
            inputData.put("prompt", task.getPrompt());
            inputData.put("imageUrl", task.getImageUrl());
            inputData.put("style", task.getStyle());
            inputData.put("generateMode", task.getGenerateMode());
            record.setInputData(inputData.toJSONString());
            record.setResultUrl(resultUrl);
            record.setCreateTime(LocalDateTime.now());
            imageGenerateRecordMapper.insert(record);
            
            log.info("图片生成任务完成: taskId={}, resultUrl={}", taskId, resultUrl);
            
        } catch (Exception e) {
            log.error("图片生成任务失败: taskId={}", taskId, e);
            // 更新任务状态为失败
            imageGenerateTaskService.updateTaskStatus(taskId, 3, null, 
                    e.getMessage() != null ? e.getMessage() : "生成失败");
        }
    }

}

