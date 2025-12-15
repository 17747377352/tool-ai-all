package com.example.tool.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.tool.entity.DailyLimit;
import com.example.tool.exception.BusinessException;
import com.example.tool.mapper.DailyLimitMapper;
import com.example.tool.result.ResultCode;
import com.example.tool.service.DailyLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 每日限流服务实现类
 * 实现用户每日使用次数限制功能
 * 限制规则：去水印30次/天，其他功能各10次/天
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class DailyLimitServiceImpl implements DailyLimitService {

    private final DailyLimitMapper dailyLimitMapper;

    // 每日限制：去水印30次，其他各10次
    private static final int REMOVE_LOGO_LIMIT = 30;
    private static final int OTHER_LIMIT = 10;

    /**
     * 获取用户今日限流信息
     * 
     * @param openid 用户openid
     * @return 今日限流实体
     */
    @Override
    public DailyLimit getTodayLimit(String openid) {
        LocalDate today = LocalDate.now();
        DailyLimit limit = dailyLimitMapper.selectOne(new LambdaQueryWrapper<DailyLimit>()
                .eq(DailyLimit::getOpenid, openid)
                .eq(DailyLimit::getDate, today));

        if (limit == null) {
            limit = new DailyLimit();
            limit.setOpenid(openid);
            limit.setDate(today);
            limit.setRemoveLogoCount(0);
            limit.setAiAvatarCount(0);
            limit.setNameSignCount(0);
            limit.setFortuneCount(0);
            limit.setConstellationCount(0);
            dailyLimitMapper.insert(limit);
        }
        return limit;
    }

    /**
     * 检查并增加使用次数
     * 
     * @param openid 用户openid
     * @param type 功能类型：1-去水印 2-AI头像 3-姓氏签名 4-运势测试 5-星座运势
     * @return 是否成功
     * @throws BusinessException 如果超过限制则抛出异常
     */
    @Override
    public boolean checkAndIncrement(String openid, Integer type) {
        DailyLimit limit = getTodayLimit(openid);
        int currentCount;
        int maxLimit;

        switch (type) {
            case 1: // 去水印
                currentCount = limit.getRemoveLogoCount();
                maxLimit = REMOVE_LOGO_LIMIT;
                if (currentCount >= maxLimit) {
                    throw new BusinessException(ResultCode.TOO_MANY_REQUESTS, "今日去水印次数已达上限（30次）");
                }
                limit.setRemoveLogoCount(currentCount + 1);
                break;
            case 2: // AI头像
                currentCount = limit.getAiAvatarCount();
                maxLimit = OTHER_LIMIT;
                if (currentCount >= maxLimit) {
                    throw new BusinessException(ResultCode.TOO_MANY_REQUESTS, "今日AI头像生成次数已达上限（10次）");
                }
                limit.setAiAvatarCount(currentCount + 1);
                break;
            case 3: // 姓氏签名
                currentCount = limit.getNameSignCount();
                maxLimit = OTHER_LIMIT;
                if (currentCount >= maxLimit) {
                    throw new BusinessException(ResultCode.TOO_MANY_REQUESTS, "今日姓氏签名生成次数已达上限（10次）");
                }
                limit.setNameSignCount(currentCount + 1);
                break;
            case 4: // 运势测试
                currentCount = limit.getFortuneCount();
                maxLimit = OTHER_LIMIT;
                if (currentCount >= maxLimit) {
                    throw new BusinessException(ResultCode.TOO_MANY_REQUESTS, "今日运势测试次数已达上限（10次）");
                }
                limit.setFortuneCount(currentCount + 1);
                break;
            case 5: // 星座运势
                currentCount = limit.getConstellationCount();
                maxLimit = OTHER_LIMIT;
                if (currentCount >= maxLimit) {
                    throw new BusinessException(ResultCode.TOO_MANY_REQUESTS, "今日星座运势次数已达上限（10次）");
                }
                limit.setConstellationCount(currentCount + 1);
                break;
            default:
                throw new BusinessException(ResultCode.PARAM_ERROR, "无效的类型");
        }

        dailyLimitMapper.updateById(limit);
        return true;
    }
}

