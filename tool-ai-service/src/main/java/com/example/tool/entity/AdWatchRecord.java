package com.example.tool.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 广告观看记录表
 * 用于记录用户观看广告的情况，控制功能使用权限
 */
@Data
@TableName("t_ad_watch_record")
public class AdWatchRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户openid
     */
    private String openid;
    
    /**
     * 广告类型：1-去水印 2-老照片批量修复
     */
    private Integer adType;
    
    /**
     * 观看时间
     */
    private LocalDateTime watchTime;
    
    /**
     * 剩余次数（仅用于老照片批量修复，默认10）
     */
    private Integer remainingCount;
    
    /**
     * 是否首次使用（仅用于老照片批量修复，首次免费）
     */
    private Boolean isFirstUse;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

