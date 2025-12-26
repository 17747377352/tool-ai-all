package com.example.tool.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 用户反馈请求DTO
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@Data
public class FeedbackDTO {
    /**
     * 反馈类型：1-功能建议 2-问题反馈 3-其他
     */
    @NotNull(message = "反馈类型不能为空")
    private Integer feedbackType;
    
    /**
     * 反馈内容
     */
    @NotBlank(message = "反馈内容不能为空")
    @Size(max = 1000, message = "反馈内容不能超过1000字")
    private String content;
    
    /**
     * 联系方式（可选）
     */
    @Size(max = 100, message = "联系方式不能超过100字")
    private String contact;
}

