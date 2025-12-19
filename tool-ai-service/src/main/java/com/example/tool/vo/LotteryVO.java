package com.example.tool.vo;

import lombok.Data;

/**
 * 彩票开奖信息
 */
@Data
public class LotteryVO {
    private String type;
    private String issue;
    private String openTime;
    private String numbers; // 例如：01 05 08 12 18 33 | 07
    private String detail;  // 可选备注
}






