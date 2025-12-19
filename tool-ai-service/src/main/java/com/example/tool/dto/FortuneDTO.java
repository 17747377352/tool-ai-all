package com.example.tool.dto;

import lombok.Data;

/**
 * 今日运势请求参数
 * 使用万年历接口，根据指定日期查询当天的黄历信息
 */
@Data
public class FortuneDTO {

    /**
     * 查询日期，格式为 yyyy-M-d 或 yyyy-MM-dd
     * 例如：2025-1-1 或 2025-01-01
     * 如果为空，则默认使用当天日期
     */
    private String date;
}

