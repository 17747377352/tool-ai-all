package com.example.simvoice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 联想词表（latin 表）
 *
 * 示例：
 * latin = "baina"
 * suggestion = "A;CJ;qJ;5b;..."
 */
@Data
@TableName("latin")
public class ImeLatin {

    /**
     * 拉丁串本身，作为主键使用
     */
    private String latin;

    /**
     * 建议的词 ID 列表，使用分号 ; 分隔，例如：A;CJ;qJ;5b
     */
    private String suggestion;
}


