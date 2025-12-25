package com.example.tool.vo;

import lombok.Data;

/**
 * 油价信息VO
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@Data
public class OilPriceVO {
    /**
     * 省份名称
     */
    private String province;
    
    /**
     * 日期
     */
    private String date;
    
    /**
     * 92号汽油价格（元/升）
     */
    private String price92;
    
    /**
     * 95号汽油价格（元/升）
     */
    private String price95;
    
    /**
     * 98号汽油价格（元/升）
     */
    private String price98;
    
    /**
     * 0号柴油价格（元/升）
     */
    private String price0;
}








