package com.example.tool.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

/**
 * 每日限流表
 */
@Data
@TableName("t_daily_limit")
public class DailyLimit {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户openid
     */
    private String openid;
    
    /**
     * 日期
     */
    private LocalDate date;
    
    /**
     * 去水印使用次数
     */
    private Integer removeLogoCount;
    
    /**
     * AI头像生成使用次数
     */
    private Integer aiAvatarCount;
    
    /**
     * 姓氏签名生成使用次数
     */
    private Integer nameSignCount;
    
    /**
     * 运势测试使用次数
     */
    private Integer fortuneCount;
    
    /**
     * 星座运势查询使用次数
     */
    private Integer constellationCount;
    
    /**
     * 老照片修复使用次数
     */
    private Integer restorePhotoCount;
}

