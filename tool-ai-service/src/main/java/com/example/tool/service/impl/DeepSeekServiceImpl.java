package com.example.tool.service.impl;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.example.tool.context.DeepSeekProperties;
import com.example.tool.exception.BusinessException;
import com.example.tool.result.ResultCode;
import com.example.tool.service.DeepSeekService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek服务实现类
 * 实现调用DeepSeek AI API生成运势文案的功能
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeepSeekServiceImpl implements DeepSeekService {

    private final DeepSeekProperties deepSeekProperties;

    /**
     * 调用DeepSeek生成运势文案
     * 
     * @param name 姓名
     * @param birthDate 出生日期（格式：yyyy-MM-dd）
     * @return 运势文案
     * @throws BusinessException 如果API调用失败
     */
    @Override
    public String generateFortuneText(String name, String birthDate) {
        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek-chat");
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1000);
            
            // 构建提示词
            String prompt = buildFortunePrompt(name, birthDate);
            
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            messages.add(message);
            
            requestBody.put("messages", messages);

            // 发送请求
            String response = HttpRequest.post(deepSeekProperties.getBaseUrl())
                    .header("Authorization", "Bearer " + deepSeekProperties.getApiKey())
                    .header("Content-Type", "application/json")
                    .body(JSONObject.toJSONString(requestBody))
                    .execute()
                    .body();

            log.info("DeepSeek响应: {}", response);

            // 解析响应
            JSONObject json = JSONObject.parseObject(response);
            
            if (json.containsKey("error")) {
                JSONObject error = json.getJSONObject("error");
                String errorMsg = error.getString("message");
                log.error("DeepSeek API错误: {}", errorMsg);
                throw new BusinessException(ResultCode.ERROR, "DeepSeek API调用失败: " + errorMsg);
            }

            // 提取生成的文案
            // DeepSeek API响应格式: {"choices": [{"message": {"content": "..."}}]}
            String content = json.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            // 清理Markdown格式符号
            content = cleanMarkdown(content);
            
            log.info("运势文案生成成功: name={}, birthDate={}, textLength={}", 
                    name, birthDate, content.length());
            return content.trim();
        } catch (Exception e) {
            log.error("调用DeepSeek API失败", e);
            if (e instanceof BusinessException) {
                throw e;
            }
            throw new BusinessException(ResultCode.ERROR, "运势生成失败: " + e.getMessage());
        }
    }

    /**
     * 调用DeepSeek生成星座运势文案
     * 
     * @param constellation 星座名称
     * @return 运势文案
     * @throws BusinessException 如果API调用失败
     */
    @Override
    public String generateConstellationFortuneText(String constellation) {
        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek-chat");
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 800);
            
            // 构建提示词
            String prompt = buildConstellationPrompt(constellation);
            
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            messages.add(message);
            
            requestBody.put("messages", messages);

            // 发送请求
            String response = HttpRequest.post(deepSeekProperties.getBaseUrl())
                    .header("Authorization", "Bearer " + deepSeekProperties.getApiKey())
                    .header("Content-Type", "application/json")
                    .body(JSONObject.toJSONString(requestBody))
                    .execute()
                    .body();

            log.info("DeepSeek星座运势响应: {}", response);

            // 解析响应
            JSONObject json = JSONObject.parseObject(response);
            
            if (json.containsKey("error")) {
                JSONObject error = json.getJSONObject("error");
                String errorMsg = error.getString("message");
                log.error("DeepSeek API错误: {}", errorMsg);
                throw new BusinessException(ResultCode.ERROR, "DeepSeek API调用失败: " + errorMsg);
            }

            // 提取生成的文案
            String content = json.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            // 清理Markdown格式符号
            content = cleanMarkdown(content);
            
            log.info("星座运势文案生成成功: constellation={}, textLength={}", 
                    constellation, content.length());
            return content.trim();
        } catch (Exception e) {
            log.error("调用DeepSeek API失败", e);
            if (e instanceof BusinessException) {
                throw e;
            }
            throw new BusinessException(ResultCode.ERROR, "星座运势生成失败: " + e.getMessage());
        }
    }

    /**
     * 构建星座运势提示词
     */
    private String buildConstellationPrompt(String constellation) {
        return String.format(
            "请为%s生成今日星座运势报告。\n\n" +
            "重要要求：\n" +
            "1. 必须包含事业、爱情、健康、财运四个方面\n" +
            "2. 语言要温馨、积极、有指导性\n" +
            "3. 每个方面用2-3句话描述，每句话不超过30字\n" +
            "4. 总字数控制在300字以内\n" +
            "5. 严格禁止使用任何Markdown格式符号，包括：#、**、*、---、-、`、[]、()等\n" +
            "6. 不要使用emoji表情符号\n" +
            "7. 使用纯文本格式，用换行分隔不同部分\n\n" +
            "输出格式示例（严格按照此格式）：\n" +
            "%s今日运势\n" +
            "事业：今日思维清晰，适合处理细节工作。主动请教可能带来关键灵感。\n" +
            "爱情：沟通顺畅的一天，单身者可通过共同兴趣结识朋友。\n" +
            "健康：精力充沛，但需注意用眼疲劳。午后短暂散步能恢复最佳状态。\n" +
            "财运：可能有小额进账或收到礼物。避免冲动消费，做好记录。\n\n" +
            "请严格按照以上格式输出，不要添加任何额外的符号或格式。",
            constellation, constellation
        );
    }

    /**
     * 构建运势提示词
     */
    private String buildFortunePrompt(String name, String birthDate) {
        return String.format(
            "请为%s（出生日期：%s）生成一份详细的运势测试报告。\n\n" +
            "重要要求：\n" +
            "1. 必须包含今日运势、本周运势、本月运势三个部分\n" +
            "2. 每个运势部分必须包含事业、爱情、健康、财运四个方面\n" +
            "3. 语言要温馨、积极、有指导性\n" +
            "4. 每个方面用4-5句话描述，每句话不超过50字\n" +
            "5. 总字数控制在500字以内\n" +
            "6. 严格禁止使用任何Markdown格式符号，包括：#、**、*、---、-、`、[]、()等\n" +
            "7. 不要使用emoji表情符号\n" +
            "8. 使用纯文本格式，用换行分隔不同部分\n\n" +
            "输出格式示例（严格按照此格式）：\n" +
            "今日运势\n" +
            "事业：今日思维清晰，适合处理细节工作。主动请教可能带来关键灵感。\n" +
            "爱情：沟通顺畅的一天，单身者可通过共同兴趣结识朋友。\n" +
            "健康：精力充沛，但需注意用眼疲劳。午后短暂散步能恢复最佳状态。\n" +
            "财运：可能有小额进账或收到礼物。避免冲动消费，做好记录。\n\n" +
            "本周运势\n" +
            "事业：团队协作是成功关键，你的创意易被认可。\n" +
            "（以此类推，包含本月运势）\n\n" +
            "请严格按照以上格式输出，不要添加任何额外的符号或格式。",
            name, birthDate
        );
    }
    
    /**
     * 清理Markdown格式符号
     */
    private String cleanMarkdown(String text) {
        if (text == null) {
            return "";
        }
        // 移除Markdown格式符号
        text = text.replaceAll("#+\\s*", ""); // 移除标题符号
        text = text.replaceAll("\\*\\*([^*]+)\\*\\*", "$1"); // 移除加粗符号
        text = text.replaceAll("\\*([^*]+)\\*", "$1"); // 移除斜体符号
        text = text.replaceAll("-{3,}", ""); // 移除分割线
        text = text.replaceAll("^\\s*\\*\\s+", ""); // 移除列表符号
        text = text.replaceAll("^\\s*-\\s+", ""); // 移除列表符号
        text = text.replaceAll("^\\s*\\d+\\.\\s+", ""); // 移除有序列表
        text = text.replaceAll("`([^`]+)`", "$1"); // 移除代码符号
        text = text.replaceAll("\\[([^\\]]+)\\]\\([^\\)]+\\)", "$1"); // 移除链接
        text = text.replaceAll("~{2}([^~]+)~{2}", "$1"); // 移除删除线
        text = text.replaceAll(">{1,}\\s*", ""); // 移除引用符号
        text = text.replaceAll("\\|", ""); // 移除表格符号
        text = text.replaceAll(":.*?:", ""); // 移除emoji符号（如:star:）
        // 清理多余的空行
        text = text.replaceAll("\n{3,}", "\n\n");
        return text.trim();
    }
}

