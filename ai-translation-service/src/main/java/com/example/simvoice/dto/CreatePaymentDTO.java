package com.example.simvoice.dto;

import lombok.Data;

/**
 * 创建支付订单DTO
 */
@Data
public class CreatePaymentDTO {
    /**
     * 订单描述
     */
    private String description;
    
    /**
     * 订单金额（单位：分）
     */
    private Integer amount;
    
    /**
     * 用户openid（小程序支付必填）
     */
    private String openid;
    
    /**
     * 商户订单号（可选，不传则自动生成）
     */
    private String outTradeNo;
}

