package com.example.simvoice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 输入法词条表（对应 word 表）
 */
@Data
@TableName("word")
public class ImeWord {
    /**
     * 唯一标识（原表为 LONGTEXT，这里用 String 承载）
     */
    private String id;

    /**
     * 字形/形状描述
     */
    private String shape;

    /**
     * 实际词
     */
    private String word;

    /**
     * 拉丁转写
     */
    private String latin;

    /**
     * 词频分数
     */
    private Integer score;
}


