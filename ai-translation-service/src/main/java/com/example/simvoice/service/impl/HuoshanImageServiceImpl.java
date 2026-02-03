package com.example.simvoice.service.impl;

import com.example.simvoice.context.HuoshanProperties;
import com.example.simvoice.exception.BusinessException;
import com.example.simvoice.result.ResultCode;
import com.example.simvoice.service.HuoshanImageService;
import com.volcengine.ark.runtime.model.images.generation.GenerateImagesRequest;
import com.volcengine.ark.runtime.model.images.generation.ImagesResponse;
import com.volcengine.ark.runtime.model.images.generation.ResponseFormat;
import com.volcengine.ark.runtime.service.ArkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

/**
 * 火山引擎图片生成服务实现类
 * 使用火山引擎文生图API生成图片
 * 
 * @author ai-translation-service
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HuoshanImageServiceImpl implements HuoshanImageService {

    private final HuoshanProperties huoshanProperties;

    private ArkService arkService;

    /**
     * 初始化ArkService
     */
    @PostConstruct
    public void init() {
        ConnectionPool connectionPool = new ConnectionPool(5, 1, TimeUnit.SECONDS);
        Dispatcher dispatcher = new Dispatcher();
        arkService = ArkService.builder()
                .dispatcher(dispatcher)
                .connectionPool(connectionPool)
                .apiKey(huoshanProperties.getApiKey())
                .build();
        log.info("火山引擎ArkService初始化成功");
    }

    /**
     * 关闭ArkService
     */
    @PreDestroy
    public void destroy() {
        if (arkService != null) {
            arkService.shutdownExecutor();
            log.info("火山引擎ArkService已关闭");
        }
    }

    /**
     * 字生图：根据文字提示词生成头像图片
     * 
     * @param prompt 生成提示词，描述想要生成的头像特征
     * @param style  风格：realistic（写实）, cartoon（卡通）, anime（动漫）, oil-painting（油画）等
     * @return 生成的图片URL
     */
    @Override
    public String generateAvatarFromText(String prompt, String style) {
        try {
            // 根据风格优化prompt
            String enhancedPrompt = enhancePromptWithStyle(prompt, style);

            GenerateImagesRequest generateRequest = GenerateImagesRequest.builder()
                    .model("doubao-seedream-4-0-250828")
                    .prompt(enhancedPrompt)
                    .size("2K")
                    .sequentialImageGeneration("disabled")
                    .responseFormat(ResponseFormat.Url)
                    .stream(false)
                    .watermark(true)
                    .build();
            ImagesResponse imagesResponse = arkService.generateImages(generateRequest);

            if (imagesResponse == null || imagesResponse.getData() == null || imagesResponse.getData().isEmpty()) {
                throw new BusinessException(ResultCode.ERROR, "火山引擎图片生成失败：响应为空");
            }

            String imageUrl = imagesResponse.getData().get(0).getUrl();
            log.info("火山引擎字生图成功: prompt={}, style={}, url={}", prompt, style, imageUrl);

            // ⭐ 返回IMAGE_LIST格式，与其他功能保持一致
            String imageListResult = String.format("IMAGE_LIST:[\"%s\"]", imageUrl);
            log.info("返回IMAGE_LIST格式: prompt={}, result={}", prompt, imageListResult);

            return imageListResult;

        } catch (Exception e) {
            log.error("火山引擎字生图失败: prompt={}, style={}", prompt, style, e);
            if (e instanceof BusinessException) {
                throw e;
            }
            throw new BusinessException(ResultCode.ERROR, "火山引擎图片生成失败: " + e.getMessage());
        }
    }

    /**
     * 图生图：基于上传的图片生成新头像
     * 
     * @param imageUrl 原始图片URL
     * @param prompt   生成提示词，描述想要生成的头像特征
     * @param style    风格：realistic（写实）, cartoon（卡通）, anime（动漫）, oil-painting（油画）等
     * @return 生成的图片URL
     */
    @Override
    public String generateAvatarFromImage(String imageUrl, String prompt, String style) {
        try {
            // 根据风格优化prompt
            String enhancedPrompt = enhancePromptWithStyle(prompt, style);

            GenerateImagesRequest generateRequest = GenerateImagesRequest.builder()
                    .model("doubao-seedream-4-0-250828")
                    .prompt(enhancedPrompt)
                    .image(imageUrl) // 图生图：传入原始图片URL
                    .size("2K")
                    .sequentialImageGeneration("disabled")
                    .responseFormat(ResponseFormat.Url)
                    .stream(false)
                    .watermark(true)
                    .build();
            ImagesResponse imagesResponse = arkService.generateImages(generateRequest);

            if (imagesResponse == null || imagesResponse.getData() == null || imagesResponse.getData().isEmpty()) {
                throw new BusinessException(ResultCode.ERROR, "火山引擎图片生成失败：响应为空");
            }

            String resultUrl = imagesResponse.getData().get(0).getUrl();
            log.info("火山引擎图生图成功: imageUrl={}, prompt={}, style={}, resultUrl={}", imageUrl, prompt, style, resultUrl);

            // ⭐ 返回IMAGE_LIST格式，与其他功能保持一致
            String imageListResult = String.format("IMAGE_LIST:[\"%s\"]", resultUrl);
            log.info("返回IMAGE_LIST格式: imageUrl={}, result={}", imageUrl, imageListResult);

            return imageListResult;

        } catch (Exception e) {
            log.error("火山引擎图生图失败: imageUrl={}, prompt={}, style={}", imageUrl, prompt, style, e);
            if (e instanceof BusinessException) {
                throw e;
            }
            
            // 检查是否是账户余额不足的错误
            String errorMessage = e.getMessage();
            if (errorMessage != null) {
                if (errorMessage.contains("AccountOverdueError") || 
                    errorMessage.contains("overdue balance") ||
                    errorMessage.contains("余额不足")) {
                    throw new BusinessException(ResultCode.ERROR, "火山引擎账户余额不足，请联系管理员充值");
                }
                if (errorMessage.contains("403") || errorMessage.contains("Forbidden")) {
                    throw new BusinessException(ResultCode.ERROR, "火山引擎服务访问被拒绝，请检查账户状态");
                }
            }
            
            // 检查异常类型
            Throwable cause = e.getCause();
            while (cause != null) {
                String causeMessage = cause.getMessage();
                if (causeMessage != null && 
                    (causeMessage.contains("AccountOverdueError") || 
                     causeMessage.contains("overdue balance"))) {
                    throw new BusinessException(ResultCode.ERROR, "火山引擎账户余额不足，请联系管理员充值");
                }
                cause = cause.getCause();
            }
            
            throw new BusinessException(ResultCode.ERROR, "火山引擎图片生成失败: " + (errorMessage != null ? errorMessage : "未知错误"));
        }
    }

    /**
     * 根据风格优化prompt
     * 
     * @param prompt 原始提示词
     * @param style  风格
     * @return 优化后的提示词
     */
    private String enhancePromptWithStyle(String prompt, String style) {
        StringBuilder enhancedPrompt = new StringBuilder();

        // 添加风格描述
        switch (style) {
            case "realistic":
                enhancedPrompt.append("写实风格，");
                break;
            case "cartoon":
                enhancedPrompt.append("卡通风格，");
                break;
            case "anime":
                enhancedPrompt.append("动漫风格，日系二次元，");
                break;
            case "oil-painting":
                enhancedPrompt.append("油画风格，艺术感强，");
                break;
            default:
                enhancedPrompt.append("精美头像，");
                break;
        }

        enhancedPrompt.append(prompt);
        enhancedPrompt.append("，高质量，细节丰富，专业摄影");

        return enhancedPrompt.toString();
    }
}

