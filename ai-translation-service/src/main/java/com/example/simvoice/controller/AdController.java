package com.example.simvoice.controller;

import com.example.simvoice.dto.RecordAdWatchDTO;
import com.example.simvoice.result.Result;
import com.example.simvoice.service.AdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 广告控制器
 * 提供广告观看记录相关接口
 * 
 * @author ai-translation-service
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/ad")
@RequiredArgsConstructor
public class AdController {

    private final AdService adService;

    /**
     * POST /api/ad/record-watch
     * 记录广告观看并增加使用次数
     * 请求体示例：
     * {
     *   "type": 1,  // 功能类型：1-去水印 2-生成图片 3-老照片修复 4-AI识图+翻译 5-即时翻译
     *   "rewardCount": 10  // 可选，奖励次数，默认10次
     * }
     * 
     * @param dto 广告观看记录请求参数
     * @param request HTTP请求对象，用于获取用户openid（从JWT拦截器注入）
     * @return 统一返回结果，包含实际减少的次数、剩余可用次数等信息
     */
    @PostMapping("/record-watch")
    public Result<Map<String, Object>> recordAdWatch(@RequestBody RecordAdWatchDTO dto, HttpServletRequest request) {
        String openid = (String) request.getAttribute("openid");
        if (openid == null || openid.isEmpty()) {
            return Result.unauthorized();
        }

        if (dto.getType() == null) {
            return Result.error(400, "功能类型不能为空");
        }

        Map<String, Object> result = adService.recordAdWatch(openid, dto.getType(), dto.getRewardCount());
        return Result.success("观看广告成功，已增加使用次数", result);
    }
}

