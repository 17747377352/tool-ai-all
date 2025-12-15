package com.example.tool.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 星座运势表
 */
@Data
@TableName("t_constellation_fortune")
public class ConstellationFortune {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String constellation; // 星座名称
    private LocalDate date; // 日期
    private String fortuneText; // 运势文案
    private String resultUrl; // 结果图片URL
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}




