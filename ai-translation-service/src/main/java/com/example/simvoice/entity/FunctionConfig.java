package com.example.simvoice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 功能配置表
 * 用于配置前端展示的功能入口
 */
@Data
@TableName("t_function_config")
public class FunctionConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 功能类型：1-去水印 2-生成图片 3-老照片修复 4-AI识图+翻译 5-即时翻译 6-蒙古语AI对话
     */
    private Integer type;
    
    /**
     * 功能名称
     */
    private String name;
    
    /**
     * 功能路由路径（前端路由）
     */
    private String route;
    
    /**
     * 是否启用（1-启用 0-禁用）
     */
    private Boolean enabled;
    
    /**
     * 排序顺序
     */
    private Integer sortOrder;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

