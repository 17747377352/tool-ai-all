
package com.example.simvoice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.simvoice.entity.ShareRecord;
import com.example.simvoice.exception.BusinessException;
import com.example.simvoice.mapper.ShareRecordMapper;
import com.example.simvoice.result.ResultCode;
import com.example.simvoice.service.ShareService;
import com.example.simvoice.service.UserQuotaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 分享服务实现类
 * 实现分享功能相关服务
 *
 * @author ai-translation-service
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShareServiceImpl implements ShareService {

    private final ShareRecordMapper shareRecordMapper;
    private final UserQuotaService userQuotaService;

    @Override
    public Map<String, Object> recordShare(String sharerOpenid, String inviteeOpenid, Integer type, Integer rewardCount) {
        if (sharerOpenid == null || sharerOpenid.trim().isEmpty()) {
            throw new IllegalArgumentException("分享人openid不能为空");
        }
        if (inviteeOpenid == null || inviteeOpenid.trim().isEmpty()) {
            throw new IllegalArgumentException("被邀请人openid不能为空");
        }
        if (type == null) {
            throw new IllegalArgumentException("功能类型不能为空");
        }
        if (sharerOpenid.equals(inviteeOpenid)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不能分享给自己");
        }

        if (rewardCount == null || rewardCount <= 0) {
            rewardCount = 10; // 默认奖励10次
        }

        // 检查是否已经分享过（防止重复分享）
        ShareRecord existingRecord = shareRecordMapper.selectOne(new LambdaQueryWrapper<ShareRecord>()
                .eq(ShareRecord::getSharerOpenid, sharerOpenid)
                .eq(ShareRecord::getInviteeOpenid, inviteeOpenid)
                .eq(ShareRecord::getType, type));

        if (existingRecord != null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "您已经分享过此功能给该用户");
        }

        // 记录分享
        ShareRecord record = new ShareRecord();
        record.setSharerOpenid(sharerOpenid);
        record.setInviteeOpenid(inviteeOpenid);
        record.setType(type);
        record.setCreateTime(LocalDateTime.now());
        shareRecordMapper.insert(record);

        // 给分享人增加额度
        int newQuota = userQuotaService.addQuota(sharerOpenid, type, rewardCount);

        // 获取分享人剩余额度
        int remainingQuota = userQuotaService.getRemainingQuota(sharerOpenid, type);

        // 功能名称
        String typeName = getTypeName(type);

        Map<String, Object> result = new HashMap<>();
        result.put("rewardCount", rewardCount); // 奖励次数
        result.put("newQuota", newQuota); // 增加后的总额度
        result.put("remainingQuota", remainingQuota); // 剩余可用额度
        result.put("typeName", typeName); // 功能名称
        result.put("shareRecordId", record.getId()); // 分享记录ID

        log.info("分享记录: sharerOpenid={}, inviteeOpenid={}, type={}({}), 奖励次数={}, 增加后额度={}, 剩余额度={}",
                sharerOpenid, inviteeOpenid, type, typeName, rewardCount, newQuota, remainingQuota);

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
                return "老照片修复";
            case 4:
                return "AI识图+翻译";
            case 5:
                return "即时翻译";
            default:
                return "未知功能";
        }
    }
}



