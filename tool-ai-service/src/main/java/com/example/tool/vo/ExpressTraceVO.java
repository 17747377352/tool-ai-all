package com.example.tool.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 快递轨迹节点VO
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpressTraceVO {
    /**
     * 时间
     */
    private String time;
    
    /**
     * 物流状态描述
     */
    private String status;
}
