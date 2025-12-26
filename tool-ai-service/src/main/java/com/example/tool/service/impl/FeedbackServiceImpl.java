package com.example.tool.service.impl;

import com.example.tool.dto.FeedbackDTO;
import com.example.tool.entity.Feedback;
import com.example.tool.mapper.FeedbackMapper;
import com.example.tool.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户反馈服务实现类
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackMapper feedbackMapper;

    @Override
    public boolean submitFeedback(String openid, FeedbackDTO dto) {
        try {
            Feedback feedback = new Feedback();
            feedback.setOpenid(openid);
            feedback.setFeedbackType(dto.getFeedbackType());
            feedback.setContent(dto.getContent());
            feedback.setContact(dto.getContact());
            feedback.setCreateTime(LocalDateTime.now());
            feedback.setUpdateTime(LocalDateTime.now());
            
            int result = feedbackMapper.insert(feedback);
            log.info("用户反馈提交成功: openid={}, feedbackType={}, id={}", 
                    openid, dto.getFeedbackType(), feedback.getId());
            return result > 0;
        } catch (Exception e) {
            log.error("用户反馈提交失败: openid={}, feedbackType={}", 
                    openid, dto.getFeedbackType(), e);
            return false;
        }
    }
}

