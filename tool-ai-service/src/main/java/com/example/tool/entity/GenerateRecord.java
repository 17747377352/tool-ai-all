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
    private String openid;
    private Integer type; // 1-去水印 2-AI头像 3-姓氏签名 4-运势测试
    private String inputData; // 输入数据（JSON格式）
    private String resultUrl; // 结果图片URL
    private LocalDateTime createTime;
}




