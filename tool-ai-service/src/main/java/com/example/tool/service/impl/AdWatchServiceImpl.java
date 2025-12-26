package com.example.tool.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.tool.entity.AdWatchRecord;
import com.example.tool.mapper.AdWatchRecordMapper;
import com.example.tool.service.AdWatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 广告观看服务实现类
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdWatchServiceImpl implements AdWatchService {

    private final AdWatchRecordMapper adWatchRecordMapper;
    
    /**
     * 去水印广告有效期（小时）
     */
    private static final int REMOVE_LOGO_AD_VALID_HOURS = 24;
    
    /**
     * 老照片批量修复每次广告获得的数量
     */
    private static final int BATCH_RESTORE_AD_COUNT = 10;
    
    /**
     * 老照片修复广告类型：3-单张修复首次免费
     */
    private static final int AD_TYPE_SINGLE_RESTORE = 3;

    @Override
    public boolean recordAdWatch(String openid, Integer adType) {
        try {
            AdWatchRecord existing = adWatchRecordMapper.selectOne(new LambdaQueryWrapper<AdWatchRecord>()
                    .eq(AdWatchRecord::getOpenid, openid)
                    .eq(AdWatchRecord::getAdType, adType)
                    .orderByDesc(AdWatchRecord::getWatchTime)
                    .last("LIMIT 1"));
            
            LocalDateTime now = LocalDateTime.now();
            
            if (adType == 1) {
                // 去水印：更新或创建记录，记录观看时间
                if (existing != null) {
                    existing.setWatchTime(now);
                    adWatchRecordMapper.updateById(existing);
                } else {
                    AdWatchRecord record = new AdWatchRecord();
                    record.setOpenid(openid);
                    record.setAdType(adType);
                    record.setWatchTime(now);
                    record.setRemainingCount(0);
                    record.setIsFirstUse(false);
                    adWatchRecordMapper.insert(record);
                }
                log.info("记录去水印广告观看: openid={}", openid);
            } else if (adType == 2) {
                // 老照片批量修复：观看广告后获得10张额度
                boolean isToday = existing != null && 
                                 existing.getWatchTime() != null &&
                                 existing.getWatchTime().toLocalDate().equals(now.toLocalDate());
                
                if (existing != null && isToday) {
                    // 今天已有记录，在现有剩余次数基础上增加10张
                    int currentCount = existing.getRemainingCount() != null ? existing.getRemainingCount() : 0;
                    existing.setRemainingCount(currentCount + BATCH_RESTORE_AD_COUNT);
                    existing.setWatchTime(now);
                    adWatchRecordMapper.updateById(existing);
                    log.info("观看广告增加老照片批量修复额度: openid={}, 原剩余={}, 增加后={}", 
                            openid, currentCount, currentCount + BATCH_RESTORE_AD_COUNT);
                } else {
                    // 没有记录或不是今天，创建新记录或重置为今天的记录
                    if (existing == null) {
                        AdWatchRecord record = new AdWatchRecord();
                        record.setOpenid(openid);
                        record.setAdType(adType);
                        record.setWatchTime(now);
                        record.setRemainingCount(BATCH_RESTORE_AD_COUNT);
                        record.setIsFirstUse(false); // 观看广告获得的，不是首次免费
                        adWatchRecordMapper.insert(record);
                        log.info("观看广告创建老照片批量修复记录: openid={}, 额度={}", openid, BATCH_RESTORE_AD_COUNT);
                    } else {
                        // 不是今天的记录，重置为今天的记录
                        existing.setWatchTime(now);
                        existing.setRemainingCount(BATCH_RESTORE_AD_COUNT);
                        existing.setIsFirstUse(false);
                        adWatchRecordMapper.updateById(existing);
                        log.info("观看广告重置老照片批量修复记录: openid={}, 额度={}", openid, BATCH_RESTORE_AD_COUNT);
                    }
                }
            }
            
            return true;
        } catch (Exception e) {
            log.error("记录广告观看失败: openid={}, adType={}", openid, adType, e);
            return false;
        }
    }

    @Override
    public boolean canUseRemoveLogo(String openid) {
        AdWatchRecord record = adWatchRecordMapper.selectOne(new LambdaQueryWrapper<AdWatchRecord>()
                .eq(AdWatchRecord::getOpenid, openid)
                .eq(AdWatchRecord::getAdType, 1)
                .orderByDesc(AdWatchRecord::getWatchTime)
                .last("LIMIT 1"));
        
        if (record == null || record.getWatchTime() == null) {
            return false;
        }
        
        // 检查是否在24小时内
        LocalDateTime now = LocalDateTime.now();
        long hours = ChronoUnit.HOURS.between(record.getWatchTime(), now);
        
        return hours < REMOVE_LOGO_AD_VALID_HOURS;
    }

    @Override
    public int getBatchRestoreRemainingCount(String openid) {
        AdWatchRecord record = adWatchRecordMapper.selectOne(new LambdaQueryWrapper<AdWatchRecord>()
                .eq(AdWatchRecord::getOpenid, openid)
                .eq(AdWatchRecord::getAdType, 2)
                .orderByDesc(AdWatchRecord::getWatchTime)
                .last("LIMIT 1"));
        
        // 批量修复的剩余次数（不包含免费的第一张）
        // 返回观看广告获得的额度
        if (record == null) {
            return 0; // 没有观看过广告，没有额度
        }
        
        LocalDateTime now = LocalDateTime.now();
        boolean isToday = record.getWatchTime() != null && 
                         record.getWatchTime().toLocalDate().equals(now.toLocalDate());
        
        if (!isToday) {
            // 不是今天，重置为0（新的一天需要重新观看广告）
            return 0;
        }
        
        // 今天已有记录，返回剩余次数（观看广告获得的额度）
        return record.getRemainingCount() != null ? record.getRemainingCount() : 0;
    }

    @Override
    public int consumeBatchRestoreCount(String openid, int count) {
        AdWatchRecord record = adWatchRecordMapper.selectOne(new LambdaQueryWrapper<AdWatchRecord>()
                .eq(AdWatchRecord::getOpenid, openid)
                .eq(AdWatchRecord::getAdType, 2)
                .orderByDesc(AdWatchRecord::getWatchTime)
                .last("LIMIT 1"));
        
        LocalDateTime now = LocalDateTime.now();
        
        // 检查是否是今天
        boolean isToday = record != null && 
                         record.getWatchTime() != null && 
                         record.getWatchTime().toLocalDate().equals(now.toLocalDate());
        
        if (!isToday || record == null) {
            // 不是今天或没有记录，说明没有观看过广告，没有额度
            return -1;
        }
        
        // 消费观看广告获得的额度
        int currentCount = record.getRemainingCount() != null ? record.getRemainingCount() : 0;
        if (currentCount < count) {
            return -1; // 剩余次数不足，需要观看广告
        }
        
        record.setRemainingCount(currentCount - count);
        record.setWatchTime(now); // 更新使用时间
        adWatchRecordMapper.updateById(record);
        log.info("消费老照片批量修复次数: openid={}, 使用={}, 剩余次数={}", openid, count, currentCount - count);
        return currentCount - count;
    }
    
    @Override
    public boolean canUseFirstFreeRestore(String openid) {
        AdWatchRecord record = adWatchRecordMapper.selectOne(new LambdaQueryWrapper<AdWatchRecord>()
                .eq(AdWatchRecord::getOpenid, openid)
                .eq(AdWatchRecord::getAdType, AD_TYPE_SINGLE_RESTORE)
                .orderByDesc(AdWatchRecord::getWatchTime)
                .last("LIMIT 1"));
        
        if (record == null) {
            // 没有记录，今天还没用过免费的第一张
            return true;
        }
        
        // 检查是否是今天
        LocalDateTime now = LocalDateTime.now();
        boolean isToday = record.getWatchTime() != null && 
                         record.getWatchTime().toLocalDate().equals(now.toLocalDate());
        
        // 如果今天已经用过，返回false
        return !isToday;
    }
    
    @Override
    public boolean consumeFirstFreeRestore(String openid) {
        AdWatchRecord record = adWatchRecordMapper.selectOne(new LambdaQueryWrapper<AdWatchRecord>()
                .eq(AdWatchRecord::getOpenid, openid)
                .eq(AdWatchRecord::getAdType, AD_TYPE_SINGLE_RESTORE)
                .orderByDesc(AdWatchRecord::getWatchTime)
                .last("LIMIT 1"));
        
        LocalDateTime now = LocalDateTime.now();
        
        // 检查是否是今天已经用过
        boolean isTodayUsed = record != null && 
                             record.getWatchTime() != null && 
                             record.getWatchTime().toLocalDate().equals(now.toLocalDate());
        
        if (isTodayUsed) {
            // 今天已经用过免费的第一张
            return false;
        }
        
        // 记录今天已使用免费的第一张
        if (record == null) {
            record = new AdWatchRecord();
            record.setOpenid(openid);
            record.setAdType(AD_TYPE_SINGLE_RESTORE);
            record.setWatchTime(now);
            record.setRemainingCount(1); // 标记已使用
            record.setIsFirstUse(true);
            adWatchRecordMapper.insert(record);
        } else {
            // 不是今天的记录，更新为今天
            record.setWatchTime(now);
            record.setRemainingCount(1);
            record.setIsFirstUse(true);
            adWatchRecordMapper.updateById(record);
        }
        
        log.info("消费单张修复首次免费: openid={}", openid);
        return true;
    }
}

