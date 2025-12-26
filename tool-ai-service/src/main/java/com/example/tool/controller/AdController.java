package com.example.tool.controller;

import com.example.tool.result.Result;
import com.example.tool.service.AdWatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 广告相关控制器
 * 提供广告观看记录和状态检查功能
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@RestController
@RequestMapping("/ad")
@RequiredArgsConstructor
public class AdController {

    private final AdWatchService adWatchService;

    /**
     * 记录广告观看
     * 
     * @param adType 广告类型：1-去水印 2-老照片批量修复
     * @param request HTTP请求对象，用于获取用户openid
     * @return 统一返回结果
     */
    @PostMapping("/watch")
    public Result<Map<String, Object>> recordAdWatch(@RequestParam Integer adType, HttpServletRequest request) {
        String openid = (String) request.getAttribute("openid");
        boolean success = adWatchService.recordAdWatch(openid, adType);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        if (adType == 2) {
            // 老照片批量修复，返回剩余次数
            int remaining = adWatchService.getBatchRestoreRemainingCount(openid);
            result.put("remainingCount", remaining);
        }
        
        return Result.success(result);
    }

    /**
     * 检查去水印功能使用状态
     * 
     * @param request HTTP请求对象，用于获取用户openid
     * @return 统一返回结果，包含是否可以使用
     */
    @GetMapping("/check-remove-logo")
    public Result<Map<String, Object>> checkRemoveLogo(HttpServletRequest request) {
        String openid = (String) request.getAttribute("openid");
        boolean canUse = adWatchService.canUseRemoveLogo(openid);
        
        Map<String, Object> result = new HashMap<>();
        result.put("canUse", canUse);
        return Result.success(result);
    }

    /**
     * 检查老照片批量修复剩余额度
     * 返回观看广告获得的额度（不包含免费的第一张）
     * 
     * @param request HTTP请求对象，用于获取用户openid
     * @return 统一返回结果，包含剩余额度
     */
    @GetMapping("/check-batch-restore")
    public Result<Map<String, Object>> checkBatchRestore(HttpServletRequest request) {
        String openid = (String) request.getAttribute("openid");
        int remaining = adWatchService.getBatchRestoreRemainingCount(openid);
        
        Map<String, Object> result = new HashMap<>();
        result.put("remainingCount", remaining);
        return Result.success(result);
    }
}

