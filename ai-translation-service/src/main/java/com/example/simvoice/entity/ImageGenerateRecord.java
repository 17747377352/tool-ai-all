package com.example.simvoice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 图片生成记录表（类型2：生成图片）
 */
@Data
@TableName("t_image_generate_record")
public class ImageGenerateRecord {
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
     * 结果图片URL
     */
    private String resultUrl;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

