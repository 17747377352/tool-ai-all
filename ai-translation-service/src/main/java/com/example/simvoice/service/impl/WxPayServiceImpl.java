package com.example.simvoice.service.impl;

import com.example.simvoice.context.WechatPayProperties;
import com.example.simvoice.context.WechatProperties;
import com.example.simvoice.service.WxPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/**
 * 微信支付服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WxPayServiceImpl implements WxPayService {

    private final WechatProperties wechatProperties;
    private final WechatPayProperties wechatPayProperties;
    private final RestTemplate restTemplate;

    @Override
    public Map<String, Object> createMiniProgramPayment(String openid, String description, Integer amount, String outTradeNo) {
        try {
            // 生成商户订单号
            if (outTradeNo == null || outTradeNo.isEmpty()) {
                outTradeNo = "ORDER" + System.currentTimeMillis() + (int)(Math.random() * 1000);
            }
            // 构建统一下单参数
            Map<String, String> params = new HashMap<>();
            params.put("appid", wechatProperties.getAppid());
            params.put("mch_id", wechatPayProperties.getMchId());
            params.put("nonce_str", generateNonceStr());
            params.put("body", description != null ? description : "测试订单");
            params.put("out_trade_no", outTradeNo);
            params.put("total_fee", String.valueOf(amount));
            params.put("spbill_create_ip", "127.0.0.1");
            params.put("notify_url", wechatPayProperties.getNotifyUrl());
            params.put("trade_type", "JSAPI");
            params.put("openid", openid);
            
            // 生成签名
            String sign = generateSign(params);
            params.put("sign", sign);
            // 构建XML请求体
            String xmlBody = mapToXml(params);
            log.info("统一下单请求参数: {}", xmlBody);
            // 调用微信统一下单接口
            String url = wechatPayProperties.getBaseUrl() + "/pay/unifiedorder";
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Content-Type", "application/xml");
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(xmlBody, headers);
            String response = restTemplate.postForObject(url, entity, String.class);
            log.info("统一下单响应: {}", response);
            // 解析响应
            Map<String, String> responseMap = xmlToMap(response);
            if ("SUCCESS".equals(responseMap.get("return_code")) && "SUCCESS".equals(responseMap.get("result_code"))) {
                // 构建前端调起支付所需的参数
                Map<String, Object> paymentParams = new HashMap<>();
                paymentParams.put("appId", wechatProperties.getAppid());
                paymentParams.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
                paymentParams.put("nonceStr", generateNonceStr());
                paymentParams.put("package", "prepay_id=" + responseMap.get("prepay_id"));
                paymentParams.put("signType", "MD5");  // V2版本使用MD5签名
                
                // 生成支付签名（小程序支付V2版本使用MD5签名）
                String paySign = generatePaySign(paymentParams);
                paymentParams.put("paySign", paySign);
                return paymentParams;
            } else {
                log.error("统一下单失败: {}", responseMap.get("return_msg"));
                throw new RuntimeException("统一下单失败: " + responseMap.get("return_msg"));
            }
        } catch (Exception e) {
            log.error("创建支付订单异常", e);
            throw new RuntimeException("创建支付订单失败: " + e.getMessage());
        }
    }

    @Override
    public String handlePaymentNotify(String xmlData) {
        try {
            log.info("收到支付回调通知: {}", xmlData);
            
            Map<String, String> notifyMap = xmlToMap(xmlData);
            
            // 验证签名
            String sign = notifyMap.remove("sign");
            String calculatedSign = generateSign(notifyMap);
            
            if (!sign.equals(calculatedSign)) {
                log.error("支付回调签名验证失败");
                return buildNotifyResponse("FAIL", "签名验证失败");
            }
            
            // 处理支付结果
            String returnCode = notifyMap.get("return_code");
            String resultCode = notifyMap.get("result_code");
            String outTradeNo = notifyMap.get("out_trade_no");
            String transactionId = notifyMap.get("transaction_id");
            
            if ("SUCCESS".equals(returnCode) && "SUCCESS".equals(resultCode)) {
                log.info("支付成功 - 订单号: {}, 微信交易号: {}", outTradeNo, transactionId);
                
                // TODO: 这里处理业务逻辑，比如更新订单状态、增加用户额度等
                
                return buildNotifyResponse("SUCCESS", "OK");
            } else {
                log.warn("支付失败 - 订单号: {}, 错误信息: {}", outTradeNo, notifyMap.get("err_code_des"));
                return buildNotifyResponse("SUCCESS", "OK");
            }
        } catch (Exception e) {
            log.error("处理支付回调异常", e);
            return buildNotifyResponse("FAIL", "处理异常");
        }
    }

    @Override
    public Map<String, Object> queryOrder(String outTradeNo) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("appid", wechatProperties.getAppid());
            params.put("mch_id", wechatPayProperties.getMchId());
            params.put("out_trade_no", outTradeNo);
            params.put("nonce_str", generateNonceStr());
            
            String sign = generateSign(params);
            params.put("sign", sign);
            
            String xmlBody = mapToXml(params);
            String url = wechatPayProperties.getBaseUrl() + "/pay/orderquery";
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Content-Type", "application/xml");
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(xmlBody, headers);
            String response = restTemplate.postForObject(url, entity, String.class);
            
            log.info("查询订单响应: {}", response);
            
            Map<String, Object> result = new HashMap<>();
            Map<String, String> responseMap = xmlToMap(response);
            result.putAll(responseMap);
            return result;
        } catch (Exception e) {
            log.error("查询订单异常", e);
            throw new RuntimeException("查询订单失败: " + e.getMessage());
        }
    }

    /**
     * 生成随机字符串
     */
    private String generateNonceStr() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 32);
    }

    /**
     * 生成签名（MD5方式，适用于V2版本）
     */
    private String generateSign(Map<String, String> params) {
        // 移除空值和sign参数
        Map<String, String> filteredParams = new TreeMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty() && !"sign".equals(entry.getKey())) {
                filteredParams.put(entry.getKey(), entry.getValue());
            }
        }
        
        // 拼接字符串
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : filteredParams.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        sb.append("key=").append(wechatPayProperties.getApiKey());
        
        // MD5加密并转大写
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                result.append(String.format("%02x", b));
            }
            return result.toString().toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException("生成签名失败", e);
        }
    }

    /**
     * 生成支付签名（小程序支付V2版本使用MD5签名）
     * 注意：小程序支付V3版本使用RSA签名，V2版本使用MD5
     * 签名规则：按照参数名ASCII码从小到大排序，拼接成字符串，最后加上key，然后MD5加密转大写
     */
    private String generatePaySign(Map<String, Object> params) {
        // 按照参数名ASCII码从小到大排序
        Map<String, String> stringParams = new TreeMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            // 排除signType和paySign参数
            if (entry.getValue() != null && !"signType".equals(entry.getKey()) && !"paySign".equals(entry.getKey())) {
                stringParams.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        
        // 拼接字符串：key1=value1&key2=value2&key=API密钥
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : stringParams.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        sb.append("key=").append(wechatPayProperties.getApiKey());
        
        log.debug("支付签名原始字符串: {}", sb.toString());
        
        // MD5加密并转大写
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                result.append(String.format("%02x", b));
            }
            String sign = result.toString().toUpperCase();
            log.debug("支付签名结果: {}", sign);
            return sign;
        } catch (Exception e) {
            throw new RuntimeException("生成支付签名失败", e);
        }
    }

    /**
     * Map转XML
     */
    private String mapToXml(Map<String, String> params) {
        StringBuilder xml = new StringBuilder("<xml>");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            xml.append("<").append(entry.getKey()).append(">");
            xml.append("<![CDATA[").append(entry.getValue()).append("]]>");
            xml.append("</").append(entry.getKey()).append(">");
        }
        xml.append("</xml>");
        return xml.toString();
    }

    /**
     * XML转Map
     */
    private Map<String, String> xmlToMap(String xml) {
        Map<String, String> map = new HashMap<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new java.io.ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            
            Element root = doc.getDocumentElement();
            NodeList nodeList = root.getChildNodes();
            
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (nodeList.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element element = (Element) nodeList.item(i);
                    map.put(element.getNodeName(), element.getTextContent());
                }
            }
        } catch (Exception e) {
            log.error("解析XML失败", e);
        }
        return map;
    }

    /**
     * 构建回调响应
     */
    private String buildNotifyResponse(String returnCode, String returnMsg) {
        return "<xml><return_code><![CDATA[" + returnCode + "]]></return_code><return_msg><![CDATA[" + returnMsg + "]]></return_msg></xml>";
    }
}

