
package com.example.simvoice.controller;

import com.example.simvoice.dto.ShareDTO;
import com.example.simvoice.result.Result;
import com.example.simvoice.service.ShareService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 分享控制器
 * 提供分享功能相关接口
 *
 * @author ai-translation-service
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/share")
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    /**
     * POST请求 /api/share/record
     * 记录分享并给分享人增加额度
     *
     * 请求体示例：
     * {
     *   "type": 1,  // 功能类型：1-去水印 2-生成图片 3-老照片修复 4-AI识图+翻译 5-即时翻译
     *   "inviteeOpenid": "被邀请人openid",
     *   "rewardCount": 10  // 可选，奖励次数，默认10次
     * }
     *
     * @param dto 分享请求参数
     * @param request HTTP请求对象，用于获取分享人openid（从JWT拦截器注入）
     * @return 统一返回结果，包含奖励信息等
     */
    @PostMapping("/record")
    public Result<Map<String, Object>> recordShare(@RequestBody ShareDTO dto, HttpServletRequest request) {
        String sharerOpenid = (String) request.getAttribute("openid");
        if (sharerOpenid == null || sharerOpenid.isEmpty()) {
            return Result.unauthorized();
        }

        if (dto.getType() == null) {
            return Result.error(400, "功能类型不能为空");
        }
        if (dto.getInviteeOpenid() == null || dto.getInviteeOpenid().trim().isEmpty()) {
            return Result.error(400, "被邀请人openid不能为空");
        }

        Map<String, Object> result = shareService.recordShare(
                sharerOpenid,
                dto.getInviteeOpenid(),
                dto.getType(),
                dto.getRewardCount());
        return Result.success("分享成功，已增加使用次数", result);
    }
}



