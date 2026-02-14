package com.example.simvoice.controller;

import com.example.simvoice.dto.CreatePaymentDTO;
import com.example.simvoice.result.Result;
import com.example.simvoice.service.WxPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付控制器
 * 提供微信支付相关接口
 * 
 * @author ai-translation-service
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
public class WxPayController {

    private final WxPayService wxPayService;

    /**
     * POST /api/pay/create
     * 创建支付订单（小程序支付）
     * 
     * 请求体示例：
     * {
     *   "description": "测试订单",
     *   "amount": 100,  // 金额（分）
     *   "openid": "用户openid",
     *   "outTradeNo": "订单号（可选）"
     * }
     * 
     * @param dto 创建支付订单请求参数
     * @param request HTTP请求对象，用于获取用户openid（从JWT拦截器注入）
     * @return 统一返回结果，包含支付参数（用于前端调起支付）
     */
    @PostMapping("/create")
    public Result<Map<String, Object>> createPayment(@RequestBody CreatePaymentDTO dto, HttpServletRequest request) {
        try {
            // 从请求中获取openid（如果JWT拦截器已注入）
            String openid = (String) request.getAttribute("openid");
            
            // 如果请求体中没有openid，使用JWT中的openid
            if (dto.getOpenid() == null || dto.getOpenid().isEmpty()) {
                if (openid == null || openid.isEmpty()) {
                    return Result.error(400, "openid不能为空");
                }
                dto.setOpenid(openid);
            }
            
            // 参数校验
            if (dto.getAmount() == null || dto.getAmount() <= 0) {
                return Result.error(400, "订单金额必须大于0");
            }
            
            if (dto.getDescription() == null || dto.getDescription().isEmpty()) {
                dto.setDescription("测试订单");
            }
            
            // 创建支付订单
            Map<String, Object> paymentParams = wxPayService.createMiniProgramPayment(
                dto.getOpenid(),
                dto.getDescription(),
                dto.getAmount(),
                dto.getOutTradeNo()
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("paymentParams", paymentParams);
            result.put("outTradeNo", dto.getOutTradeNo() != null ? dto.getOutTradeNo() : "自动生成");
            
            return Result.success("创建支付订单成功", result);
        } catch (Exception e) {
            log.error("创建支付订单异常", e);
            return Result.error("创建支付订单失败: " + e.getMessage());
        }
    }

    /**
     * POST /api/pay/notify
     * 微信支付回调通知接口
     * 
     * @param request HTTP请求对象
     * @return 微信要求的XML响应
     */
    @PostMapping("/notify")
    public String paymentNotify(HttpServletRequest request) {
        try {
            // 读取请求体XML数据
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8)
            );
            StringBuilder xmlData = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                xmlData.append(line);
            }
            
            // 处理支付回调
            String response = wxPayService.handlePaymentNotify(xmlData.toString());
            return response;
        } catch (Exception e) {
            log.error("处理支付回调异常", e);
            return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[处理异常]]></return_msg></xml>";
        }
    }

    /**
     * GET /api/pay/query
     * 查询订单状态
     * 
     * @param outTradeNo 商户订单号
     * @return 统一返回结果，包含订单信息
     */
    @GetMapping("/query")
    public Result<Map<String, Object>> queryOrder(@RequestParam String outTradeNo) {
        try {
            if (outTradeNo == null || outTradeNo.isEmpty()) {
                return Result.error(400, "订单号不能为空");
            }
            
            Map<String, Object> orderInfo = wxPayService.queryOrder(outTradeNo);
            return Result.success("查询成功", orderInfo);
        } catch (Exception e) {
            log.error("查询订单异常", e);
            return Result.error("查询订单失败: " + e.getMessage());
        }
    }

    /**
     * GET /api/pay/test
     * 支付测试接口（用于测试配置是否正确）
     * 
     * @return 统一返回结果
     */
    @GetMapping("/test")
    public Result<Map<String, Object>> testPayment() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "微信支付接口测试成功");
        result.put("timestamp", System.currentTimeMillis());
        return Result.success("测试成功", result);
    }
}



