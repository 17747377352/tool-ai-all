
package com.example.simvoice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分享记录表
 * 记录用户分享功能给其他用户的信息
 */
@Data
@TableName("t_share_record")
public class ShareRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 分享人openid（邀请人）
     */
    private String sharerOpenid;

    /**
     * 被邀请人openid
     */
    private String inviteeOpenid;

    /**
     * 功能类型：1-去水印 2-生成图片 3-老照片修复 4-AI识图+翻译 5-即时翻译
     */
    private Integer type;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}



