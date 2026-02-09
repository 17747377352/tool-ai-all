package com.example.simvoice.dto;

import lombok.Data;

/**
 * 记录广告观看请求DTO
 * 
 * @author ai-translation-service
 * @since 1.0
 */
@Data
public class RecordAdWatchDTO {
    /**
     * 功能类型：1-去水印 2-生成图片 3-老照片修复 4-AI识图+翻译 5-即时翻译
     */
    private Integer type;
    
    /**
     * 奖励次数，可选，默认10次
     */
    private Integer rewardCount;
}

