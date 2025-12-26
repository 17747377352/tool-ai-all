package com.example.tool.service;

import com.example.tool.dto.FeedbackDTO;

/**
 * 用户反馈服务接口
 * 
 * @author tool-ai-service
 * @since 1.0
 */
public interface FeedbackService {
    /**
     * 提交用户反馈
     * 
     * @param openid 用户openid
     * @param dto 反馈内容DTO
     * @return 是否提交成功
     */
    boolean submitFeedback(String openid, FeedbackDTO dto);
}

