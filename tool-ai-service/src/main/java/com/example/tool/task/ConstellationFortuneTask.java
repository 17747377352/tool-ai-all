package com.example.tool.task;

import com.example.tool.entity.ConstellationFortune;
import com.example.tool.service.ConstellationFortuneService;
import com.example.tool.service.DeepSeekService;
import com.example.tool.service.impl.ConstellationFortuneServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 星座运势定时任务
 * 每天凌晨2点执行，为所有12个星座生成今日运势
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConstellationFortuneTask {

    private final ConstellationFortuneService constellationFortuneService;
    private final DeepSeekService deepSeekService;

    /**
     * 每天凌晨2点执行
     * cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void generateTodayFortuneForAll() {
        log.info("开始执行星座运势定时任务，为所有12个星座生成今日运势");
        
        LocalDate today = LocalDate.now();
        String[] constellations = ConstellationFortuneServiceImpl.getAllConstellations();
        
        int successCount = 0;
        int failCount = 0;
        
        for (String constellation : constellations) {
            try {
                // 检查是否已存在
                ConstellationFortune existing = constellationFortuneService.getByConstellationAndDate(constellation, today);
                if (existing != null) {
                    log.info("星座运势已存在，跳过: constellation={}, date={}", constellation, today);
                    successCount++;
                    continue;
                }
                
                // 1. 调用DeepSeek生成星座运势JSON数据
                String jsonData = deepSeekService.generateConstellationFortuneText(constellation);
                log.info("星座运势JSON生成成功: constellation={}, textLength={}", constellation, jsonData.length());
                
                // 2. 保存JSON数据到数据库（存储在resultUrl字段中）
                constellationFortuneService.saveOrUpdate(constellation, today, null, jsonData);
                
                log.info("星座运势生成成功: constellation={}", constellation);
                successCount++;
                
                // 避免API调用过快，每个星座间隔1秒
                Thread.sleep(1000);
                
            } catch (Exception e) {
                log.error("星座运势生成失败: constellation={}", constellation, e);
                failCount++;
            }
        }
        
        log.info("星座运势定时任务执行完成: 成功={}, 失败={}, 总计={}", 
                successCount, failCount, constellations.length);
    }
}

