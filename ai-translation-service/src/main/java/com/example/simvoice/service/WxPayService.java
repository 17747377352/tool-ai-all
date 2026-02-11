package com.example.simvoice.service;

import java.util.Map;

/**
 * 微信支付服务接口
 */
public interface WxPayService {
    /**
     * 创建小程序支付订单
     * 
     * @param openid 用户openid
     * @param description 订单描述
     * @param amount 订单金额（分）
     * @param outTradeNo 商户订单号（可选）
     * @return 支付参数，用于前端调起支付
     */
    Map<String, Object> createMiniProgramPayment(String openid, String description, Integer amount, String outTradeNo);
    
    /**
     * 处理支付回调通知
     * 
     * @param xmlData 微信回调的XML数据
     * @return 处理结果
     */
    String handlePaymentNotify(String xmlData);
    
    /**
     * 查询订单状态
     * 
     * @param outTradeNo 商户订单号
     * @return 订单信息
     */
    Map<String, Object> queryOrder(String outTradeNo);
}

