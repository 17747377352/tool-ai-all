package com.example.tool.vo;

import lombok.Data;

import java.util.List;

/**
 * 快递查询结果
 */
@Data
public class ExpressResultVO {
    private String company;
    private String trackingNo;
    private String status;
    private List<ExpressTraceVO> traces;
}






