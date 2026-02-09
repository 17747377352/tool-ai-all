package com.example.simvoice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 翻译记录表（功能类型4：AI识图+翻译 功能类型5：即时翻译）
 */
@Data
@TableName("t_translate_record")
public class TranslateRecord {
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
     * 翻译结果（JSON格式存储）
     */
    private String resultUrl;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

