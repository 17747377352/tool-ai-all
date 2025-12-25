package com.example.tool.service;

import com.example.tool.entity.ConstellationFortune;
import java.time.LocalDate;

/**
 * 星座运势服务接口
 * 提供星座运势数据管理功能，包括查询、保存和更新
 * 星座运势由定时任务每天凌晨生成，用户请求时从数据库查询
 * 
 * @author tool-ai-service
 * @since 1.0
 */
public interface ConstellationFortuneService {
    /**
     * 根据星座和日期查询运势
     * 
     * @param constellation 星座名称
     * @param date 日期
     * @return 星座运势实体，如果不存在返回null
     */
    ConstellationFortune getByConstellationAndDate(String constellation, LocalDate date);
    
    /**
     * 保存或更新星座运势
     * 
     * @param constellation 星座名称
     * @param date 日期
     * @param fortuneText 运势文案（已废弃，可为null）
     * @param resultData 结果数据（JSON格式字符串）
     */
    void saveOrUpdate(String constellation, LocalDate date, String fortuneText, String resultData);
    
    /**
     * 为所有12个星座生成今日运势
     * 通常由定时任务调用，每天凌晨执行
     */
    void generateTodayFortuneForAll();
}

