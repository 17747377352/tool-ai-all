package com.example.simvoice.service.impl;

import com.example.simvoice.config.QuotaConfig;
import com.example.simvoice.service.AdService;
import com.example.simvoice.service.UserQuotaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 广告服务实现类
 * 实现广告观看记录和奖励功能
 *
 * @author ai-translation-service
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdServiceImpl implements AdService {

    private final UserQuotaService userQuotaService;
    private final QuotaConfig quotaConfig;

    @Override
    public Map<String, Object> recordAdWatch(String openid, Integer type, Integer rewardCount) {
        if (type == null) {
            throw new IllegalArgumentException("功能类型不能为空");
        }

        if (rewardCount == null || rewardCount <= 0) {
            rewardCount = quotaConfig.getAdRewardQuota(); // 默认奖励额度（从配置文件读取）
        }

        // 增加额度
        int newQuota = userQuotaService.addQuota(openid, type, rewardCount);

        // 获取剩余额度
        int remainingQuota = userQuotaService.getRemainingQuota(openid, type);

        // 功能名称
        String typeName = getTypeName(type);

        Map<String, Object> result = new HashMap<>();
        result.put("rewardCount", rewardCount); // 奖励次数
        result.put("newQuota", newQuota); // 增加后的总额度
        result.put("remainingQuota", remainingQuota); // 剩余可用额度
        result.put("typeName", typeName); // 功能名称

        log.info("广告观看记录: openid={}, type={}({}), 奖励次数={}, 增加后额度={}, 剩余额度={}",
                openid, type, typeName, rewardCount, newQuota, remainingQuota);

        return result;
    }

    /**
     * 获取功能类型名称
     */
    private String getTypeName(Integer type) {
        switch (type) {
            case 1:
                return "去水印";
            case 2:
                return "生成图片";
            case 3:
                return "姓氏签名";
            case 4:
                return "运势测试";
            case 5:
                return "星座运势";
            case 6:
                return "老照片修复";
            default:
                return "未知功能";
        }
    }
}

