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
     * 唯一标识
     */
    private String id;

    /**
     *  词形
     */
    private String shape;

    /**
     * 词
     */
    private String word;

    /**
     *  拉丁转写
     */
    private String latin;

    /**
     *  排序
     */
    private Integer score;
}


