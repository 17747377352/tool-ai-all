package com.example.tool.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.tool.dto.*;
import com.example.tool.entity.GenerateRecord;
import com.example.tool.mapper.GenerateRecordMapper;
import com.example.tool.entity.ConstellationFortune;
import com.example.tool.service.ConstellationFortuneService;
import com.example.tool.service.DailyLimitService;
import com.example.tool.service.DeepSeekService;
import com.example.tool.service.HuoshanImageService;
import com.example.tool.service.ToolService;
import com.example.tool.service.WatermarkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 工具服务实现类
 * 实现各种AI工具功能的核心业务逻辑
 * 包括：去水印、AI头像生成、姓氏签名生成、运势测试、星座运势等
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ToolServiceImpl implements ToolService {

    private final DailyLimitService dailyLimitService;
    private final GenerateRecordMapper generateRecordMapper;
    private final DeepSeekService deepSeekService;
    private final HuoshanImageService huoshanImageService;
    private final ConstellationFortuneService constellationFortuneService;
    private final WatermarkService watermarkService;

    /**
     * 短视频去水印
     * 支持抖音、小红书等平台的视频去水印
     * 
     * @param openid 用户openid
     * @param dto 去水印请求参数，包含分享链接或视频URL
     * @return 去水印后的视频URL
     */
    @Override
    public String removeLogo(String openid, RemoveLogoDTO dto) {
        // 1. 检查限流
        dailyLimitService.checkAndIncrement(openid, 1);

        // 2. 参数校验
        if (dto.getShareUrl() == null || dto.getShareUrl().trim().isEmpty()) {
            throw new com.example.tool.exception.BusinessException(
                com.example.tool.result.ResultCode.PARAM_ERROR, "分享链接不能为空");
        }

        // 3. 调用去水印服务
        log.info("开始去水印: openid={}, shareUrl={}", openid, dto.getShareUrl());
        String resultUrl = watermarkService.removeWatermark(dto.getShareUrl());
        log.info("去水印成功: openid={}, resultUrl={}", openid, resultUrl);
        
        // 4. 保存生成记录
        saveGenerateRecord(openid, 1, dto, resultUrl);

        return resultUrl;
    }

    /**
     * AI头像生成
     * 支持两种模式：
     * 1. 字生图：仅提供prompt，根据文字描述生成头像
     * 2. 图生图：提供imageUrl和prompt，基于上传的图片生成新头像
     * 
     * @param openid 用户openid
     * @param dto AI头像生成请求参数，包含prompt、style和可选的imageUrl
     * @return 生成的头像图片URL
     */
    @Override
    public String generateAiAvatar(String openid, AiAvatarDTO dto) {
        // 1. 检查限流
        dailyLimitService.checkAndIncrement(openid, 2);

        // 2. 参数校验
        if (dto.getPrompt() == null || dto.getPrompt().trim().isEmpty()) {
            throw new com.example.tool.exception.BusinessException(
                com.example.tool.result.ResultCode.PARAM_ERROR, "提示词不能为空");
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
                dto.getStyle() != null ? dto.getStyle() : "realistic"
            );
        } else {
            // 字生图模式：仅根据文字描述生成头像
            log.info("使用字生图模式生成头像: openid={}, prompt={}, style={}", 
                openid, dto.getPrompt(), dto.getStyle());
            resultUrl = huoshanImageService.generateAvatarFromText(
                dto.getPrompt(), 
                dto.getStyle() != null ? dto.getStyle() : "realistic"
            );
        }

        // 4. 保存生成记录
        saveGenerateRecord(openid, 2, dto, resultUrl);

        return resultUrl;
    }

    /**
     * 姓氏签名生成
     * 使用火山引擎生成签名图片，支持多种字体风格
     * 
     * @param openid 用户openid
     * @param dto 签名生成请求参数，包含姓氏和风格等信息
     * @return 生成的签名图片URL
     */
    @Override
    public String generateNameSign(String openid, NameSignDTO dto) {
        // 1. 检查限流
        dailyLimitService.checkAndIncrement(openid, 3);

        // 2. 参数校验
        if (dto.getSurname() == null || dto.getSurname().trim().isEmpty()) {
            throw new com.example.tool.exception.BusinessException(
                com.example.tool.result.ResultCode.PARAM_ERROR, "姓氏不能为空");
        }

        // 3. 使用火山引擎生成签名图片
        log.info("使用火山引擎生成签名: openid={}, surname={}, style={}", 
            openid, dto.getSurname(), dto.getStyle());
        String resultUrl = huoshanImageService.generateNameSignImage(
            dto.getSurname(), 
            dto.getStyle() != null ? dto.getStyle() : "classic"
        );

        // 4. 保存生成记录
        saveGenerateRecord(openid, 3, dto, resultUrl);

        return resultUrl;
    }

    /**
     * 运势测试生成
     * 调用DeepSeek AI生成运势文案，然后使用火山引擎生成图片
     * 
     * @param openid 用户openid
     * @param dto 运势测试请求参数，包含姓名和出生日期等信息
     * @return 生成的运势图片URL（火山引擎直接返回的URL）
     * @throws RuntimeException 如果运势生成失败
     */
    @Override
    public String generateFortune(String openid, FortuneDTO dto) {
        // 1. 检查限流
        dailyLimitService.checkAndIncrement(openid, 4);

        try {
            // 2. 调用DeepSeek生成运势文案
            String fortuneText = deepSeekService.generateFortuneText(dto.getName(), dto.getBirthDate());
            log.info("运势文案生成成功: name={}, birthDate={}, textLength={}", 
                    dto.getName(), dto.getBirthDate(), fortuneText.length());
            
            // 3. 使用火山引擎生成运势图片（直接返回URL，无需上传OSS）
            String resultUrl = huoshanImageService.generateFortuneImage(
                    fortuneText, dto.getName(), dto.getBirthDate());
            
            log.info("运势生成成功: openid={}, name={}, birthDate={}, url={}", 
                    openid, dto.getName(), dto.getBirthDate(), resultUrl);
            
            // 4. 保存生成记录
            saveGenerateRecord(openid, 4, dto, resultUrl);
            
            return resultUrl;
        } catch (Exception e) {
            log.error("运势生成失败", e);
            throw new RuntimeException("运势生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 星座运势生成
     * 优先从数据库查询今日运势（由定时任务每天凌晨生成），如果不存在则实时生成并保存
     * 
     * @param openid 用户openid
     * @param dto 星座运势请求参数，包含星座名称
     * @return 生成的星座运势图片URL
     * @throws RuntimeException 如果运势生成或上传失败
     */
    @Override
    public String generateConstellationFortune(String openid, ConstellationFortuneDTO dto) {
        // 1. 检查限流（使用独立的星座运势限流，类型5）
        dailyLimitService.checkAndIncrement(openid, 5);

        LocalDate today = LocalDate.now();
        
        // 2. 优先从数据库查询今日运势
        ConstellationFortune fortune = constellationFortuneService.getByConstellationAndDate(
                dto.getConstellation(), today);
        
        if (fortune != null) {
            // 数据库中有，直接返回
            log.info("从数据库获取星座运势: openid={}, constellation={}, url={}", 
                    openid, dto.getConstellation(), fortune.getResultUrl());
            saveGenerateRecord(openid, 5, dto, fortune.getResultUrl());
            return fortune.getResultUrl();
        }
        
        // 3. 数据库中没有，调用DeepSeek生成（兜底逻辑）
        log.warn("数据库中未找到今日星座运势，调用DeepSeek生成: constellation={}, date={}", 
                dto.getConstellation(), today);
        
        try {
            // 调用DeepSeek生成星座运势文案
            String fortuneText = deepSeekService.generateConstellationFortuneText(dto.getConstellation());
            log.info("星座运势文案生成成功: constellation={}, textLength={}", 
                    dto.getConstellation(), fortuneText.length());
            
            // 使用火山引擎生成星座运势图片（直接返回URL，无需上传OSS）
            String resultUrl = huoshanImageService.generateConstellationFortuneImage(
                    fortuneText, dto.getConstellation());
            
            // 保存到数据库（供后续用户查询）
            constellationFortuneService.saveOrUpdate(dto.getConstellation(), today, fortuneText, resultUrl);
            
            log.info("星座运势生成成功（兜底）: openid={}, constellation={}, url={}", 
                    openid, dto.getConstellation(), resultUrl);
            
            // 保存生成记录（类型5：星座运势）
            saveGenerateRecord(openid, 5, dto, resultUrl);
            
            return resultUrl;
        } catch (Exception e) {
            log.error("星座运势生成失败", e);
            throw new RuntimeException("星座运势生成失败: " + e.getMessage(), e);
        }
    }

    private void saveGenerateRecord(String openid, Integer type, Object inputData, String resultUrl) {
        GenerateRecord record = new GenerateRecord();
        record.setOpenid(openid);
        record.setType(type);
        record.setInputData(JSONObject.toJSONString(inputData));
        record.setResultUrl(resultUrl);
        record.setCreateTime(LocalDateTime.now());
        generateRecordMapper.insert(record);
    }
}

