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
     * 功能类型：1-去水印 2-生成图片 3-姓氏签名 4-运势测试 5-星座运势 6-老照片修复 7-AI识图+翻译
     */
    private Integer type;
    
    /**
     * 奖励次数，可选，默认10次
     */
    private Integer rewardCount;
}

