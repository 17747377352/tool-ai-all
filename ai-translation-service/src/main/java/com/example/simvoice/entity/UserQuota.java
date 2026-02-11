package com.example.simvoice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户额度表
 * 存储用户各功能的使用额度，支持通过看广告、分享等方式增加额度
 */
@Data
@TableName("t_user_quota")
public class UserQuota {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户openid
     */
    private String openid;
    
    /**
     * 去水印额度
     */
    private Integer removeLogoQuota;
    
    /**
     * AI头像生成额度
     */
    private Integer aiAvatarQuota;
    
    /**
     * 姓氏签名生成额度
     */
    private Integer nameSignQuota;
    
    /**
     * 运势测试额度
     */
    private Integer fortuneQuota;
    
    /**
     * 星座运势查询额度
     */
    private Integer constellationQuota;
    
    /**
     * 老照片修复额度
     */
    private Integer restorePhotoQuota;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

