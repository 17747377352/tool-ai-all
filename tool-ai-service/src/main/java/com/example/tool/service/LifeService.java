package com.example.tool.service;

import com.example.tool.dto.ExpressQueryDTO;
import com.example.tool.vo.ExpressResultVO;
import com.example.tool.vo.ForexVO;
import com.example.tool.vo.LotteryVO;
import com.example.tool.vo.OilPriceVO;

/**
 * 生活查询服务接口
 * 提供快递查询、油价查询、汇率换算、彩票开奖等生活服务功能
 * 
 * @author tool-ai-service
 * @since 1.0
 */
public interface LifeService {

    /**
     * 查询快递物流信息
     * 
     * @param openid 用户openid
     * @param dto 快递查询请求参数，包含快递公司编码、运单号、手机号等
     * @return 快递查询结果，包含物流轨迹信息
     */
    ExpressResultVO queryExpress(String openid, ExpressQueryDTO dto);

    /**
     * 查询油价信息
     * 
     * @param province 省份名称
     * @return 油价信息，包含92号、95号、98号汽油和0号柴油价格
     */
    OilPriceVO queryOilPrice(String province);

    /**
     * 汇率换算
     * 
     * @param base 基础货币代码（如：USD, CNY）
     * @param target 目标货币代码（如：USD, CNY）
     * @param amount 原始金额
     * @return 汇率换算结果，包含汇率和换算后的金额
     */
    ForexVO queryForex(String base, String target, java.math.BigDecimal amount);

    /**
     * 查询彩票开奖信息
     * 
     * @param type 彩票类型
     * @return 彩票开奖信息，包含期号、开奖时间、开奖号码等
     */
    LotteryVO queryLottery(String type);
}








