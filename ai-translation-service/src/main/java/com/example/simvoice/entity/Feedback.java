package com.example.simvoice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户反馈实体
 */
@Data
@TableName("t_feedback")
public class Feedback {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 微信 openid
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

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}


