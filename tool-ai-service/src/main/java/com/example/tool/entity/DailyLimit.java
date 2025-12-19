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
    private String openid;
    private LocalDate date;
    private Integer removeLogoCount; // 去水印次数
    private Integer aiAvatarCount; // AI头像次数
    private Integer nameSignCount; // 姓氏签名次数
    private Integer fortuneCount; // 运势测试次数
    private Integer constellationCount; // 星座运势次数
    private Integer restorePhotoCount; // 老照片修复次数
}

