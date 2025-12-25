package com.example.tool.vo;

import lombok.Data;

/**
 * 星座运势响应VO
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@Data
public class ConstellationFortuneVO {
    /**
     * 星座名称
     */
    private String constellation;
    
    /**
     * 日期
     */
    private String date;
    
    /**
     * 综合运势分数（0-100）
     */
    private Integer overallScore;
    
    /**
     * 爱情运势分数（0-100）
     */
    private Integer loveScore;
    
    /**
     * 事业运势分数（0-100）
     */
    private Integer careerScore;
    
    /**
     * 财运分数（0-100）
     */
    private Integer wealthScore;
    
    /**
     * 健康运势分数（0-100）
     */
    private Integer healthScore;
    
    /**
     * 幸运颜色
     */
    private String luckyColor;
    
    /**
     * 幸运数字
     */
    private String luckyNumber;
    
    /**
     * 速配星座
     */
    private String compatibleConstellation;
    
    /**
     * 宜（建议做的事情）
     */
    private String suitable;
    
    /**
     * 忌（避免做的事情）
     */
    private String avoid;
    
    /**
     * 综合运势详细介绍
     */
    private String overallDetail;
    
    /**
     * 爱情运势详细介绍
     */
    private String loveDetail;
    
    /**
     * 事业学业运势详细介绍（包含工作和学习）
     */
    private String careerDetail;
    
    /**
     * 财富运势详细介绍
     */
    private String wealthDetail;
    
    /**
     * 健康运势详细介绍
     */
    private String healthDetail;
}

