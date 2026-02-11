package com.example.simvoice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


@Data
@TableName("phrase")
public class ImePhrase {

    /**
     * 唯一标识
     */
    private String id;

    /**
     *  排序
     */
    private Integer score;
}


