package com.example.simvoice.controller;

import cn.hutool.core.map.MapUtil;
import com.example.simvoice.entity.Feedback;
import com.example.simvoice.result.Result;
import com.example.simvoice.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 意见反馈接口
 */
@Slf4j
@RestController
@RequestMapping("/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    /**
     * 提交意见反馈
     *
     * 请求体示例：
     * {
     *   "feedbackType": 1,
     *   "content": "页面加载有点慢",
     *   "contact": "wechat:xxx"
     * }
     */
    @PostMapping("/submit")
    public Result<Void> submit(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String openid = (String) request.getAttribute("openid");
        if (openid == null || openid.isEmpty()) {
            return Result.unauthorized();
        }

        Integer feedbackType = MapUtil.getInt(body, "feedbackType");
        String content = MapUtil.getStr(body, "content");
        String contact = MapUtil.getStr(body, "contact");

        // 参数校验
        if (feedbackType == null || feedbackType <= 0 || feedbackType > 3) {
            return Result.error("反馈类型不合法");
        }
        if (content == null || content.isEmpty()) {
            return Result.error("反馈内容不能为空");
        }

        // 构建 Feedback 对象并保存
        Feedback feedback = new Feedback();
        feedback.setOpenid(openid);
        feedback.setFeedbackType(feedbackType);
        feedback.setContent(content);
        feedback.setContact(contact);

        feedbackService.save(feedback);
        log.info("收到用户反馈, openid={}, type={}, contact={}", openid, feedbackType, contact);
        return Result.success();
    }
}


