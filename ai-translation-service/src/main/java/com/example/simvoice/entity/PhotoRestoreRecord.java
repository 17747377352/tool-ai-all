package com.example.simvoice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 老照片修复记录表（功能类型3：老照片修复）
 */
@Data
@TableName("t_photo_restore_record")
public class PhotoRestoreRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户openid
     */
    private String openid;
    
    /**
     * 输入数据（JSON格式）
     */
    private String inputData;
    
    /**
     * 修复后的图片URL
     */
    private String resultUrl;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

