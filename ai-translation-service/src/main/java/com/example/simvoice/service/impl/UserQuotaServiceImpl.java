package com.example.simvoice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.simvoice.config.QuotaConfig;
import com.example.simvoice.entity.UserQuota;
import com.example.simvoice.exception.BusinessException;
import com.example.simvoice.mapper.UserQuotaMapper;
import com.example.simvoice.result.ResultCode;
import com.example.simvoice.service.UserQuotaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户额度服务实现类
 * 实现用户额度管理功能
 * 
 * @author ai-translation-service
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class UserQuotaServiceImpl implements UserQuotaService {

    private final UserQuotaMapper userQuotaMapper;
    private final QuotaConfig quotaConfig;

    @Override
    public UserQuota getOrCreateQuota(String openid) {
        UserQuota quota = userQuotaMapper.selectOne(new LambdaQueryWrapper<UserQuota>()
                .eq(UserQuota::getOpenid, openid));

        if (quota == null) {
            // 创建新用户额度记录，设置默认额度（从配置文件读取）
            int defaultQuota = quotaConfig.getDefaultInitQuota();
            quota = new UserQuota();
            quota.setOpenid(openid);
            quota.setRemoveLogoQuota(defaultQuota);
            quota.setAiAvatarQuota(defaultQuota);
            quota.setNameSignQuota(defaultQuota);
            quota.setFortuneQuota(defaultQuota);
            quota.setConstellationQuota(defaultQuota);
            quota.setRestorePhotoQuota(defaultQuota);
            quota.setCreateTime(LocalDateTime.now());
            quota.setUpdateTime(LocalDateTime.now());
            userQuotaMapper.insert(quota);
        }

        return quota;
    }

    @Override
    public boolean checkAndConsume(String openid, Integer type) {
        // TODO: 临时关闭限流
        if (true) {
            return true;
        }

        UserQuota quota = getOrCreateQuota(openid);
        int currentQuota;
        String typeName;

        switch (type) {
            case 1: // 去水印
                currentQuota = quota.getRemoveLogoQuota();
                typeName = "去水印";
                if (currentQuota <= 0) {
                    throw new BusinessException(ResultCode.DAILY_LIMIT_EXCEEDED, "去水印额度不足，请观看广告或分享获取更多额度");
                }
                quota.setRemoveLogoQuota(currentQuota - 1);
                break;
            case 2: // 生成图片
                currentQuota = quota.getAiAvatarQuota();
                typeName = "生成图片";
                if (currentQuota <= 0) {
                    throw new BusinessException(ResultCode.DAILY_LIMIT_EXCEEDED, "生成图片额度不足，请观看广告或分享获取更多额度");
                }
                quota.setAiAvatarQuota(currentQuota - 1);
                break;
            case 3: // 姓氏签名
                currentQuota = quota.getNameSignQuota();
                typeName = "姓氏签名";
                if (currentQuota <= 0) {
                    throw new BusinessException(ResultCode.DAILY_LIMIT_EXCEEDED, "姓氏签名额度不足，请观看广告或分享获取更多额度");
                }
                quota.setNameSignQuota(currentQuota - 1);
                break;
            case 4: // 运势测试
                currentQuota = quota.getFortuneQuota();
                typeName = "运势测试";
                if (currentQuota <= 0) {
                    throw new BusinessException(ResultCode.DAILY_LIMIT_EXCEEDED, "运势测试额度不足，请观看广告或分享获取更多额度");
                }
                quota.setFortuneQuota(currentQuota - 1);
                break;
            case 5: // 星座运势
                currentQuota = quota.getConstellationQuota();
                typeName = "星座运势";
                if (currentQuota <= 0) {
                    throw new BusinessException(ResultCode.DAILY_LIMIT_EXCEEDED, "星座运势额度不足，请观看广告或分享获取更多额度");
                }
                quota.setConstellationQuota(currentQuota - 1);
                break;
            case 6: // 老照片修复
                currentQuota = quota.getRestorePhotoQuota();
                typeName = "老照片修复";
                if (currentQuota <= 0) {
                    throw new BusinessException(ResultCode.DAILY_LIMIT_EXCEEDED, "老照片修复额度不足，请观看广告或分享获取更多额度");
                }
                quota.setRestorePhotoQuota(currentQuota - 1);
                break;
            default:
                throw new BusinessException(ResultCode.PARAM_ERROR, "无效的功能类型");
        }

        quota.setUpdateTime(LocalDateTime.now());
        userQuotaMapper.updateById(quota);
        return true;
    }

    @Override
    public int addQuota(String openid, Integer type, Integer amount) {
        if (amount == null || amount <= 0) {
            amount = quotaConfig.getShareRewardQuota(); // 默认增加额度（从配置文件读取）
        }

        UserQuota quota = getOrCreateQuota(openid);
        int currentQuota;
        int newQuota;

        switch (type) {
            case 1: // 去水印
                currentQuota = quota.getRemoveLogoQuota();
                newQuota = currentQuota + amount;
                quota.setRemoveLogoQuota(newQuota);
                break;
            case 2: // 生成图片
                currentQuota = quota.getAiAvatarQuota();
                newQuota = currentQuota + amount;
                quota.setAiAvatarQuota(newQuota);
                break;
            case 3: // 姓氏签名
                currentQuota = quota.getNameSignQuota();
                newQuota = currentQuota + amount;
                quota.setNameSignQuota(newQuota);
                break;
            case 4: // 运势测试
                currentQuota = quota.getFortuneQuota();
                newQuota = currentQuota + amount;
                quota.setFortuneQuota(newQuota);
                break;
            case 5: // 星座运势
                currentQuota = quota.getConstellationQuota();
                newQuota = currentQuota + amount;
                quota.setConstellationQuota(newQuota);
                break;
            case 6: // 老照片修复
                currentQuota = quota.getRestorePhotoQuota();
                newQuota = currentQuota + amount;
                quota.setRestorePhotoQuota(newQuota);
                break;
            default:
                throw new BusinessException(ResultCode.PARAM_ERROR, "无效的功能类型");
        }

        quota.setUpdateTime(LocalDateTime.now());
        userQuotaMapper.updateById(quota);
        return newQuota;
    }

    @Override
    public int getRemainingQuota(String openid, Integer type) {
        UserQuota quota = getOrCreateQuota(openid);
        
        switch (type) {
            case 1: // 去水印
                return quota.getRemoveLogoQuota();
            case 2: // 生成图片
                return quota.getAiAvatarQuota();
            case 3: // 姓氏签名
                return quota.getNameSignQuota();
            case 4: // 运势测试
                return quota.getFortuneQuota();
            case 5: // 星座运势
                return quota.getConstellationQuota();
            case 6: // 老照片修复
                return quota.getRestorePhotoQuota();
            default:
                throw new BusinessException(ResultCode.PARAM_ERROR, "无效的功能类型");
        }
    }
}

