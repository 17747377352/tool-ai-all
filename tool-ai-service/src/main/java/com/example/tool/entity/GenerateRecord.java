package com.example.tool.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 生成记录表
 */
@Data
@TableName("t_generate_record")
public class GenerateRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户openid
     */
    private String openid;
    
    /**
     * 功能类型：1-去水印 2-AI头像 3-姓氏签名 4-运势测试 5-星座运势 6-老照片修复
     */
    private Integer type;
    
    /**
     * 输入数据（JSON格式）
     */
    private String inputData;
    
    /**
     * 结果图片或视频URL
     */
    private String resultUrl;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}




