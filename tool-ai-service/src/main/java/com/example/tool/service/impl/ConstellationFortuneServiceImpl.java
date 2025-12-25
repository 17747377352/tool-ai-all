package com.example.tool.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.tool.entity.ConstellationFortune;
import com.example.tool.mapper.ConstellationFortuneMapper;
import com.example.tool.service.ConstellationFortuneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 星座运势服务实现类
 * 实现星座运势数据管理功能，包括查询、保存和更新
 * 星座运势由定时任务每天凌晨生成，用户请求时从数据库查询
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConstellationFortuneServiceImpl implements ConstellationFortuneService {

    private final ConstellationFortuneMapper constellationFortuneMapper;

    // 12个星座列表
    private static final String[] CONSTELLATIONS = {
            "白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座",
            "天秤座", "天蝎座", "射手座", "摩羯座", "水瓶座", "双鱼座"
    };

    /**
     * 根据星座和日期查询运势
     * 
     * @param constellation 星座名称
     * @param date 日期
     * @return 星座运势实体，如果不存在返回null
     */
    @Override
    public ConstellationFortune getByConstellationAndDate(String constellation, LocalDate date) {
        return constellationFortuneMapper.selectOne(new LambdaQueryWrapper<ConstellationFortune>()
                .eq(ConstellationFortune::getConstellation, constellation)
                .eq(ConstellationFortune::getDate, date));
    }

    /**
     * 保存或更新星座运势
     * 
     * @param constellation 星座名称
     * @param date 日期
     * @param fortuneText 运势文案（已废弃，可为null，将使用空字符串）
     * @param resultData 结果数据（JSON格式字符串，存储在resultUrl字段中）
     */
    @Override
    public void saveOrUpdate(String constellation, LocalDate date, String fortuneText, String resultData) {
        ConstellationFortune existing = getByConstellationAndDate(constellation, date);
        
        // fortuneText已废弃，如果为null则使用空字符串
        String textValue = (fortuneText != null) ? fortuneText : "";
        
        if (existing != null) {
            // 更新
            existing.setFortuneText(textValue);
            existing.setResultUrl(resultData);
            constellationFortuneMapper.updateById(existing);
            log.info("更新星座运势: constellation={}, date={}", constellation, date);
        } else {
            // 新增
            ConstellationFortune fortune = new ConstellationFortune();
            fortune.setConstellation(constellation);
            fortune.setDate(date);
            fortune.setFortuneText(textValue);
            fortune.setResultUrl(resultData);
            constellationFortuneMapper.insert(fortune);
            log.info("保存星座运势: constellation={}, date={}", constellation, date);
        }
    }

    @Override
    public void generateTodayFortuneForAll() {
        // 这个方法由定时任务调用，实际生成逻辑在定时任务中
        // 这里只是提供接口，实际实现需要依赖ToolService
    }

    /**
     * 获取所有星座列表
     */
    public static String[] getAllConstellations() {
        return CONSTELLATIONS.clone();
    }
}

