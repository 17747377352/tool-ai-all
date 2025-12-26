package com.example.tool.controller;

import com.example.tool.dto.FeedbackDTO;
import com.example.tool.result.Result;
import com.example.tool.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户反馈控制器
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@RestController
@RequestMapping("/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    /**
     * 提交用户反馈
     * 
     * @param dto 反馈内容DTO
     * @param request HTTP请求对象，用于获取用户openid
     * @return 统一返回结果
     */
    @PostMapping("/submit")
    public Result<Map<String, Object>> submitFeedback(
            @RequestBody @Validated FeedbackDTO dto, 
            HttpServletRequest request) {
        String openid = (String) request.getAttribute("openid");
        
        boolean success = feedbackService.submitFeedback(openid, dto);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        
        if (success) {
            return Result.success("反馈提交成功，感谢您的宝贵意见！", result);
        } else {
            return Result.error("反馈提交失败，请稍后重试");
        }
    }
}

