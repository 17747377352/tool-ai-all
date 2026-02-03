package com.example.simvoice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 图片生成任务表
 */
@Data
@TableName("t_image_generate_task")
public class ImageGenerateTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户openid
     */
    private String openid;
    
    /**
     * 模版ID（如果使用模版）
     */
    private Long templateId;
    
    /**
     * 提示词
     */
    private String prompt;
    
    /**
     * 参考图片URL（图生图模式）
     */
    private String imageUrl;
    
    /**
     * 风格
     */
    private String style;
    
    /**
     * 生成模式：1-字生图 2-图生图 3-模版同款 4-模版参考图
     */
    private Integer generateMode;
    
    /**
     * 任务状态：0-排队中 1-生成中 2-已完成 3-失败
     */
    private Integer taskStatus;
    
    /**
     * 生成结果图片URL
     */
    private String resultUrl;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 完成时间
     */
    private LocalDateTime finishTime;
}

