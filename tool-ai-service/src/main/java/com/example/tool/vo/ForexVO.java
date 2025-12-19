package com.example.tool.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 汇率换算结果
 */
@Data
public class ForexVO {
    private String base;
    private String target;
    private BigDecimal rate;
    private BigDecimal amount;
    private BigDecimal converted;
    private String source;
    private String time;
}






