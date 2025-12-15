package com.example.tool.service.impl;

import cn.hutool.core.util.StrUtil;
import com.example.tool.exception.BusinessException;
import com.example.tool.result.ResultCode;
import com.example.tool.service.DataAnalysisService;
import com.example.tool.service.WatermarkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class WatermarkServiceImpl implements WatermarkService {

    private static final Pattern URL_PATTERN = Pattern.compile("(https?://\\S+)");
    /**
     * 是否优先使用dataAnalysis服务
     * 如果为true，优先调用dataAnalysis服务；如果失败，再使用stealer或本地实现
     */
    @Value("${data-analysis.enabled:true}")
    private boolean dataAnalysisEnabled;

    @Autowired(required = false)
    private DataAnalysisService dataAnalysisService;


    @Override
    public String removeWatermark(String shareUrl) {
        if (StrUtil.isBlank(shareUrl)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "分享链接不能为空");
        }

        // 提取URL（支持从文本中提取）
        String originalUrl = extractUrl(shareUrl);
        if (originalUrl == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "未识别到有效链接，请确保链接中包含 douyin.com");
        }

        // 判断平台并处理
        if (originalUrl.contains("douyin.com") || originalUrl.contains("iesdouyin.com") || originalUrl.contains("xiaohongshu.com")) {

            // 优先级1: 如果启用了dataAnalysis服务，优先使用dataAnalysis
            if (dataAnalysisEnabled && dataAnalysisService != null) {
                try {
                    log.info("使用dataAnalysis服务处理链接: {}", originalUrl);
                    String videoUrl = dataAnalysisService.getVideoUrl(originalUrl);
                    if (StrUtil.isNotBlank(videoUrl)) {
                        log.info("dataAnalysis服务成功获取视频URL");
                        return videoUrl;
                    }
                } catch (BusinessException e) {
                    log.warn("dataAnalysis服务调用失败: {}, 尝试其他方案", e.getMessage());
                    // 继续尝试其他方案
                } catch (Exception e) {
                    log.warn("dataAnalysis服务调用异常: {}, 尝试其他方案", e.getMessage(), e);
                    // 继续尝试其他方案
                }
            }
        }

        throw new BusinessException(ResultCode.PARAM_ERROR, "不支持的链接格式，仅支持抖音和小红书");
    }


    private String extractUrl(String text) {
        if (StrUtil.isBlank(text)) return null;

        // 清理文本
        text = text.trim();

        // 尝试提取URL
        Matcher matcher = URL_PATTERN.matcher(text);
        while (matcher.find()) {
            String url = matcher.group(1);
            // 清理URL末尾的标点符号
            url = url.replaceAll("[。，、；：！？\\.\\s]+$", "");
            // 只返回抖音或小红书链接
            if (url.contains("douyin.com") || url.contains("iesdouyin.com") || url.contains("xiaohongshu.com")) {
                return url;
            }
        }
        return null;
    }
}