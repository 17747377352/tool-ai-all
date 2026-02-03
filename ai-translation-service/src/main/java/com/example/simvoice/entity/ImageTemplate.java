package com.example.simvoice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 图片模版表
 */
@Data
@TableName("t_image_template")
public class ImageTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 模版名称
     */
    private String name;
    
    /**
     * 模版图片URL
     */
    private String imageUrl;
    
    /**
     * 提示词
     */
    private String prompt;
    
    /**
     * 风格（realistic/anime等）
     */
    private String style;
    
    /**
     * 模版描述
     */
    private String description;
    
    /**
     * 排序顺序
     */
    private Integer sortOrder;
    
    /**
     * 是否启用（1-启用 0-禁用）
     */
    private Boolean isEnabled;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

