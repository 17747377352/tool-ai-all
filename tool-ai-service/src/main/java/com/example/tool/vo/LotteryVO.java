package com.example.tool.vo;

import lombok.Data;

/**
 * 彩票开奖信息VO
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@Data
public class LotteryVO {
    /**
     * 彩票类型
     */
    private String type;
    
    /**
     * 期号
     */
    private String issue;
    
    /**
     * 开奖时间
     */
    private String openTime;
    
    /**
     * 开奖号码，例如：01 05 08 12 18 33 | 07
     */
    private String numbers;
    
    /**
     * 可选备注信息
     */
    private String detail;
}








