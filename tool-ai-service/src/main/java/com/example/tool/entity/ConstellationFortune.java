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
    
    /**
     * 星座名称
     */
    private String constellation;
    
    /**
     * 日期
     */
    private LocalDate date;
    
    /**
     * 运势文案（已废弃，现使用resultUrl存储JSON数据）
     */
    private String fortuneText;
    
    /**
     * 结果数据（JSON格式字符串，包含运势分数、详情等信息）
     */
    private String resultUrl;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}




