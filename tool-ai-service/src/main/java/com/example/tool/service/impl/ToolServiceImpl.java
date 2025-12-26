package com.example.tool.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.tool.dto.*;
import com.example.tool.entity.GenerateRecord;
import com.example.tool.exception.BusinessException;
import com.example.tool.mapper.GenerateRecordMapper;
import com.example.tool.entity.ConstellationFortune;
import com.example.tool.result.ResultCode;
import com.example.tool.service.AdWatchService;
import com.example.tool.service.ConstellationFortuneService;
import com.example.tool.service.DailyLimitService;
import com.example.tool.service.DeepSeekService;
import com.example.tool.service.GfpganService;
import com.example.tool.service.HuoshanImageService;
import com.example.tool.service.LocalFileService;
import com.example.tool.service.OssService;
import com.example.tool.service.ToolService;
import com.example.tool.service.WatermarkService;
import com.example.tool.vo.ConstellationFortuneVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

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
    private final LocalFileService localFileService;
    private final OssService ossService;
    private final GfpganService gfpganService;
    private final AdWatchService adWatchService;

    /**
     * 聚合数据万年历（今日运势） API 密钥（Juhe）
     * 仅用于万年历今日运势查询接口
     */
    @Value("${juhe.fortune.key:}")
    private String juheFortuneKey;

    /**
     * 短视频去水印
     * 支持抖音、小红书等平台的视频去水印
     *
     * @param openid 用户openid
     * @param dto    去水印请求参数，包含分享链接或视频URL
     * @return 去水印后的视频URL
     */
    @Override
    public String removeLogo(String openid, RemoveLogoDTO dto) {
        // 1. 检查广告观看状态（24小时内有效）
        if (!adWatchService.canUseRemoveLogo(openid)) {
            throw new BusinessException(ResultCode.TOO_MANY_REQUESTS, 
                    "请先观看广告后使用，观看后24小时内有效");
        }
        
        // 2. 检查限流（广告观看后不再受每日限流限制）
        // dailyLimitService.checkAndIncrement(openid, 1); // 注释掉，使用广告控制

        // 3. 参数校验
        if (dto.getShareUrl() == null || dto.getShareUrl().trim().isEmpty()) {
            throw new com.example.tool.exception.BusinessException(
                    com.example.tool.result.ResultCode.PARAM_ERROR, "分享链接不能为空");
        }

        // 4. 调用去水印服务
        log.info("开始去水印: openid={}, shareUrl={}", openid, dto.getShareUrl());
        String resultUrl = watermarkService.removeWatermark(dto.getShareUrl());
        log.info("去水印成功: openid={}, resultUrl={}", openid, resultUrl);

        // 5. 保存生成记录
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
     * @param dto    AI头像生成请求参数，包含prompt、style和可选的imageUrl
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
                    dto.getStyle() != null ? dto.getStyle() : "realistic");
        } else {
            // 字生图模式：仅根据文字描述生成头像
            log.info("使用字生图模式生成头像: openid={}, prompt={}, style={}",
                    openid, dto.getPrompt(), dto.getStyle());
            resultUrl = huoshanImageService.generateAvatarFromText(
                    dto.getPrompt(),
                    dto.getStyle() != null ? dto.getStyle() : "realistic");
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
     * @param dto    签名生成请求参数，包含姓氏和风格等信息
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
                dto.getStyle() != null ? dto.getStyle() : "classic");

        // 4. 保存生成记录
        saveGenerateRecord(openid, 3, dto, resultUrl);

        return resultUrl;
    }

    /**
     * 今日运势生成
     * 使用万年历接口获取当天黄历信息，并调用火山引擎生成运势图片
     *
     * @param openid 用户openid
     * @param dto    今日运势请求参数，包含日期
     * @return 生成的运势图片URL（IMAGE_LIST 单图格式）
     */
    @Override
    public String generateFortune(String openid, FortuneDTO dto) {
        // 1. 检查限流（类型4：今日运势）
        dailyLimitService.checkAndIncrement(openid, 4);

        // 2. 参数处理：日期为空则默认今天
        String dateStr;
        if (dto != null && dto.getDate() != null && !dto.getDate().trim().isEmpty()) {
            dateStr = dto.getDate().trim();
        } else {
            dateStr = LocalDate.now().toString();
        }

        log.info("开始查询今日运势: openid={}, date={}", openid, dateStr);

        // 3. 优先从数据库查询缓存（当天生成的同日期运势）
        LambdaQueryWrapper<GenerateRecord> queryWrapper = new LambdaQueryWrapper<GenerateRecord>()
                .eq(GenerateRecord::getOpenid, openid)
                .eq(GenerateRecord::getType, 4)
                .ge(GenerateRecord::getCreateTime, LocalDate.now().atStartOfDay())
                .orderByDesc(GenerateRecord::getCreateTime);

        List<GenerateRecord> records = generateRecordMapper.selectList(queryWrapper);
        String cachedUrl = findMatchingRecord(records, dateStr);
        if (cachedUrl != null) {
            log.info("发现今日运势缓存记录: openid={}, date={}, url={}", openid, dateStr, cachedUrl);
            return cachedUrl;
        }

        try {
            if (juheFortuneKey == null || juheFortuneKey.trim().isEmpty()) {
                log.error("今日运势接口未配置 Juhe Key");
                throw new BusinessException(ResultCode.ERROR, "今日运势接口未配置，请联系管理员配置 Juhe Key");
            }

            // 4. 调用聚合数据万年历接口
            String url = "http://v.juhe.cn/calendar/day";
            Map<String, Object> params = new HashMap<>();
            params.put("key", juheFortuneKey);
            params.put("date", dateStr);

            HttpResponse resp = HttpRequest.get(url)
                    .form(params)
                    .timeout(10000)
                    .execute();

            int status = resp.getStatus();
            log.debug("万年历接口响应状态码: {}", status);

            if (status != 200) {
                log.error("万年历接口返回错误状态码: status={}, date={}", status, dateStr);
                throw new BusinessException(ResultCode.ERROR, "今日运势查询失败，状态码: " + status);
            }

            String body = resp.body();
            if (body == null || body.trim().isEmpty()) {
                log.error("万年历接口返回空响应: date={}", dateStr);
                throw new BusinessException(ResultCode.ERROR, "今日运势查询失败，返回空响应");
            }

            log.debug("万年历接口响应体: {}", body);

            JSONObject json = JSONObject.parseObject(body);
            if (json == null) {
                throw new BusinessException(ResultCode.ERROR, "今日运势查询失败，响应解析错误");
            }

            Integer errorCode = json.getInteger("error_code");
            if (errorCode == null || errorCode != 0) {
                String reason = json.getString("reason");
                log.warn("万年历接口返回错误: errorCode={}, reason={}", errorCode, reason);
                throw new BusinessException(ResultCode.ERROR,
                        (reason != null && !reason.isEmpty()) ? reason : "今日运势查询失败");
            }

            JSONObject result = json.getJSONObject("result");
            JSONObject data = result != null ? result.getJSONObject("data") : null;
            if (data == null) {
                log.error("万年历接口返回数据为空: date={}", dateStr);
                throw new BusinessException(ResultCode.ERROR, "今日运势查询失败，数据为空");
            }

            // 4. 组合黄历文案，作为火山引擎图片生成的文案
            StringBuilder text = new StringBuilder();
            String date = data.getString("date");
            String weekday = data.getString("weekday");
            String lunarYear = data.getString("lunarYear");
            String lunar = data.getString("lunar");
            String animalsYear = data.getString("animalsYear");
            String holiday = data.getString("holiday");
            String suit = data.getString("suit");
            String avoid = data.getString("avoid");
            String desc = data.getString("desc");

            text.append("公历：").append(date != null ? date : dateStr);
            if (weekday != null && !weekday.isEmpty()) {
                text.append("（").append(weekday).append("）");
            }
            text.append("；\n");
            if (lunarYear != null || lunar != null) {
                text.append("农历：");
                if (lunarYear != null) {
                    text.append(lunarYear).append(" ");
                }
                if (lunar != null) {
                    text.append(lunar);
                }
                text.append("；\n");
            }
            if (animalsYear != null && !animalsYear.isEmpty()) {
                text.append("生肖：").append(animalsYear).append("；\n");
            }
            if (holiday != null && !holiday.isEmpty()) {
                text.append("节日：").append(holiday).append("；\n");
            }
            if (suit != null && !suit.isEmpty()) {
                text.append("宜：").append(suit).append("；\n");
            }
            if (avoid != null && !avoid.isEmpty()) {
                text.append("忌：").append(avoid).append("；\n");
            }
            if (desc != null && !desc.isEmpty()) {
                text.append("说明：").append(desc);
            }

            String fortuneText = text.toString();
            log.info("今日运势文案生成完成: openid={}, date={}, textLength={}",
                    openid, date != null ? date : dateStr, fortuneText);

            // 5. 不再调用 AI 生图，改为直接返回 JSON 数据用于前端卡片渲染
            String resultUrl = "FORTUNE_JSON:" + data.toJSONString();
            log.info("今日运势改为 JSON 渲染模式: openid={}, date={}", openid, date != null ? date : dateStr);

            // 6. 保存生成记录（将请求和原始data一起存入 inputData，数据结果存 resultUrl）
            JSONObject recordInput = new JSONObject();
            recordInput.put("request", dto != null ? dto : new FortuneDTO());
            recordInput.put("data", data);
            saveGenerateRecord(openid, 4, recordInput, resultUrl);

            return resultUrl;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("今日运势查询或图片生成异常: openid={}, date={}", openid, dateStr, e);
            throw new BusinessException(ResultCode.ERROR, "今日运势生成失败: " + e.getMessage());
        }
    }

    /**
     * 星座运势生成
     * 优先从数据库查询今日运势（由定时任务每天凌晨生成），如果不存在则实时生成并保存
     *
     * @param openid 用户openid
     * @param dto    星座运势请求参数，包含星座名称
     * @return 星座运势数据VO对象
     * @throws RuntimeException 如果运势生成失败
     */
    @Override
    public ConstellationFortuneVO generateConstellationFortune(String openid, ConstellationFortuneDTO dto) {
        // 1. 检查限流（使用独立的星座运势限流，类型5）
        dailyLimitService.checkAndIncrement(openid, 5);

        LocalDate today = LocalDate.now();

        // 2. 优先从数据库查询今日运势
        ConstellationFortune fortune = constellationFortuneService.getByConstellationAndDate(dto.getConstellation(),
                today);

        String jsonData;
        if (fortune != null && fortune.getResultUrl() != null) {
            // 数据库中有，直接使用
            jsonData = fortune.getResultUrl();
            log.info("从数据库获取星座运势: openid={}, constellation={}",
                    openid, dto.getConstellation());
        } else {
            // 3. 数据库中没有，调用DeepSeek生成（兜底逻辑）
            log.warn("数据库中未找到今日星座运势，调用DeepSeek生成: constellation={}, date={}",
                    dto.getConstellation(), today);

            try {
                // 调用DeepSeek生成星座运势JSON数据
                jsonData = deepSeekService.generateConstellationFortuneText(dto.getConstellation());
                log.info("星座运势JSON生成成功: constellation={}, textLength={}",
                        dto.getConstellation(), jsonData.length());

                // 保存到数据库（供后续用户查询）
                constellationFortuneService.saveOrUpdate(dto.getConstellation(), today, null, jsonData);

                log.info("星座运势生成成功（兜底）: openid={}, constellation={}",
                        openid, dto.getConstellation());
            } catch (Exception e) {
                log.error("星座运势生成失败", e);
                throw new RuntimeException("星座运势生成失败: " + e.getMessage(), e);
            }
        }

        // 4. 解析JSON并转换为VO对象
        try {
            ConstellationFortuneVO vo = parseConstellationFortuneJson(jsonData, dto.getConstellation(), today);
            
            // 保存生成记录（类型5：星座运势）
            saveGenerateRecord(openid, 5, dto, jsonData);
            
            return vo;
        } catch (Exception e) {
            log.error("解析星座运势JSON失败: jsonData={}", jsonData, e);
            throw new RuntimeException("解析星座运势数据失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 解析星座运势JSON数据并转换为VO对象
     * 
     * @param jsonData JSON格式的运势数据
     * @param constellation 星座名称
     * @param date 日期
     * @return 星座运势VO对象
     */
    private ConstellationFortuneVO parseConstellationFortuneJson(String jsonData, String constellation, LocalDate date) {
        JSONObject json = JSONObject.parseObject(jsonData);
        
        ConstellationFortuneVO vo = new ConstellationFortuneVO();
        vo.setConstellation(constellation);
        vo.setDate(date.toString());
        vo.setOverallScore(json.getInteger("overallScore"));
        vo.setLoveScore(json.getInteger("loveScore"));
        vo.setCareerScore(json.getInteger("careerScore"));
        vo.setWealthScore(json.getInteger("wealthScore"));
        vo.setHealthScore(json.getInteger("healthScore"));
        vo.setLuckyColor(json.getString("luckyColor"));
        vo.setLuckyNumber(json.getString("luckyNumber"));
        vo.setCompatibleConstellation(json.getString("compatibleConstellation"));
        vo.setSuitable(json.getString("suitable"));
        vo.setAvoid(json.getString("avoid"));
        vo.setOverallDetail(json.getString("overallDetail"));
        vo.setLoveDetail(json.getString("loveDetail"));
        vo.setCareerDetail(json.getString("careerDetail"));
        vo.setWealthDetail(json.getString("wealthDetail"));
        vo.setHealthDetail(json.getString("healthDetail"));
        
        return vo;
    }

    /**
     * 老照片修复（GFPGAN）- 单张
     *
     * @param openid 用户openid
     * @param dto    修复请求参数
     * @return 修复后图片URL
     */
    @Override
    public String restoreOldPhoto(String openid, OldPhotoRestoreDTO dto) {
        // 检查是否可以使用免费的第一张
        if (!adWatchService.consumeFirstFreeRestore(openid)) {
            // 今天已经用过免费的第一张，需要观看广告才能使用
            throw new BusinessException(ResultCode.TOO_MANY_REQUESTS, 
                    "每天第一张修复免费，今天已使用。如需继续使用，请观看广告获得批量修复额度");
        }

        if (dto.getImageUrl() == null || dto.getImageUrl().trim().isEmpty()) {
            throw new BusinessException(
                    com.example.tool.result.ResultCode.PARAM_ERROR, "图片地址不能为空");
        }

        try {
            // 调用 GFPGAN 云端修复（基于可访问的原图URL）
            byte[] restoredBytes = gfpganService.restore(dto.getImageUrl(), dto.getStrength());

            // 上传到OSS（返回公网可访问的URL）
            String fileName = "old-photo-restore/" +
                    java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "/"
                    + java.util.UUID.randomUUID().toString() + ".png";
            String ossUrl = ossService.uploadFile(restoredBytes, fileName, "image/png");

            // 返回 IMAGE_LIST 单图格式，便于前端统一展示
            String resultUrl = String.format("IMAGE_LIST:[\"%s\"]", ossUrl);

            // 记录生成记录，类型6：老照片修复
            saveGenerateRecord(openid, 6, dto, resultUrl);
            return resultUrl;
        } catch (com.example.tool.exception.BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("老照片修复失败", e);
            throw new RuntimeException("老照片修复失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 老照片批量修复（GFPGAN）
     * 需要观看广告获得次数，每天首次免费10张
     *
     * @param openid 用户openid
     * @param dto    批量修复请求参数，包含图片URL列表
     * @return 修复后的图片URL列表（IMAGE_LIST格式）
     */
    @Override
    public String batchRestoreOldPhoto(String openid, BatchRestoreOldPhotoDTO dto) {
        if (dto.getImageUrls() == null || dto.getImageUrls().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "图片列表不能为空");
        }
        
        if (dto.getImageUrls().size() > 10) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "最多只能批量处理10张图片");
        }
        
        int imageCount = dto.getImageUrls().size();
        
        // 检查今天是否已经用过免费的第一张
        boolean canUseFirstFree = adWatchService.canUseFirstFreeRestore(openid);
        
        int needConsumeCount = imageCount;
        if (canUseFirstFree) {
            // 今天还没用过免费的第一张，第一张免费
            needConsumeCount = imageCount - 1;
            // 标记已使用免费的第一张
            adWatchService.consumeFirstFreeRestore(openid);
            log.info("批量修复使用免费第一张: openid={}, 总数量={}, 需要消费额度={}", openid, imageCount, needConsumeCount);
        }
        
        // 如果除了免费第一张外还有其他图片，需要消费批量修复额度
        if (needConsumeCount > 0) {
            int remaining = adWatchService.consumeBatchRestoreCount(openid, needConsumeCount);
            if (remaining < 0) {
                throw new BusinessException(ResultCode.TOO_MANY_REQUESTS, 
                        String.format("剩余次数不足（需要%d张额度），请观看广告获得更多次数（一次广告10张，每天第一张免费）", needConsumeCount));
            }
        }
        
        try {
            List<String> resultUrls = new java.util.ArrayList<>();
            Double strength = dto.getStrength() != null ? dto.getStrength() : 0.7;
            
            // 批量处理图片
            for (String imageUrl : dto.getImageUrls()) {
                if (imageUrl == null || imageUrl.trim().isEmpty()) {
                    continue;
                }
                
                try {
                    // 调用 GFPGAN 云端修复
                    byte[] restoredBytes = gfpganService.restore(imageUrl.trim(), strength);
                    
                    // 上传到OSS
                    String fileName = "old-photo-restore/" +
                            java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "/"
                            + java.util.UUID.randomUUID().toString() + ".png";
                    String ossUrl = ossService.uploadFile(restoredBytes, fileName, "image/png");
                    
                    resultUrls.add(ossUrl);
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
            
            // 记录生成记录，类型6：老照片修复
            saveGenerateRecord(openid, 6, dto, resultUrl);
            
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

    private String findMatchingRecord(List<GenerateRecord> records, String dateStr) {
        if (records == null)
            return null;
        for (GenerateRecord record : records) {
            JSONObject inputJson = JSONObject.parseObject(record.getInputData());
            String cachedDate = inputJson != null && inputJson.getJSONObject("request") != null
                    ? inputJson.getJSONObject("request").getString("date")
                    : null;
            if (dateStr.equals(cachedDate) || (cachedDate == null && dateStr.equals(LocalDate.now().toString()))) {
                return record.getResultUrl();
            }
        }
        return null;
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
