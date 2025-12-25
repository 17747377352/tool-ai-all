package com.example.tool.dto;

import lombok.Data;

/**
 * 快递查询请求DTO
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@Data
public class ExpressQueryDTO {
    /**
     * 快递公司编码（可选）
     */
    private String company;

    /**
     * 运单号（必填）
     */
    private String trackingNo;

    /**
     * 手机号后4位（部分快递公司需要）
     */
    private String phone;
}





