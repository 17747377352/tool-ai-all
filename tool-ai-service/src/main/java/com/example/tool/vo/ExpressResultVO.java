package com.example.tool.vo;

import lombok.Data;

import java.util.List;

/**
 * 快递查询结果VO
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@Data
public class ExpressResultVO {
    /**
     * 快递公司名称
     */
    private String company;
    
    /**
     * 运单号
     */
    private String trackingNo;
    
    /**
     * 快递状态
     */
    private String status;
    
    /**
     * 物流轨迹列表
     */
    private List<ExpressTraceVO> traces;
}
