package com.example.tool.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 汇率换算结果VO
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@Data
public class ForexVO {
    /**
     * 基础货币代码（如：USD, CNY）
     */
    private String base;
    
    /**
     * 目标货币代码（如：USD, CNY）
     */
    private String target;
    
    /**
     * 汇率
     */
    private BigDecimal rate;
    
    /**
     * 原始金额
     */
    private BigDecimal amount;
    
    /**
     * 换算后的金额
     */
    private BigDecimal converted;
    
    /**
     * 数据来源
     */
    private String source;
    
    /**
     * 查询时间
     */
    private String time;
}








