package com.example.tool.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户反馈表
 * 用于收集用户对产品的反馈意见
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@Data
@TableName("t_feedback")
public class Feedback {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户openid
     */
    private String openid;
    
    /**
     * 反馈类型：1-功能建议 2-问题反馈 3-其他
     */
    private Integer feedbackType;
    
    /**
     * 反馈内容
     */
    private String content;
    
    /**
     * 联系方式（可选）
     */
    private String contact;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

