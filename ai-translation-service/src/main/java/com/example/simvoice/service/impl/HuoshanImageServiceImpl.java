package com.example.simvoice.service.impl;

import com.example.simvoice.context.HuoshanProperties;
import com.example.simvoice.exception.BusinessException;
import com.example.simvoice.result.ResultCode;
import com.example.simvoice.service.HuoshanImageService;
import com.volcengine.ark.runtime.model.images.generation.GenerateImagesRequest;
import com.volcengine.ark.runtime.model.images.generation.ImagesResponse;
import com.volcengine.ark.runtime.model.images.generation.ResponseFormat;
import com.volcengine.ark.runtime.service.ArkService;
import com.example.simvoice.utils.OpenAiCompatJsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

/**
 * 火山引擎图片生成服务实现类
 * 使用火山引擎文生图API生成图片/进行图片增强
 *
 * @author ai-translation-service
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HuoshanImageServiceImpl implements HuoshanImageService {

    private final HuoshanProperties huoshanProperties;
    private final RestTemplate restTemplate;

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
                    .model(huoshanProperties.getImageModel())
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

            // 返回IMAGE_LIST格式，与其他功能保持一致
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
                    .model(huoshanProperties.getImageModel())
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

            //  返回IMAGE_LIST格式，与其他功能保持一致
            String imageListResult = String.format("IMAGE_LIST:[\"%s\"]", resultUrl);
            log.info("返回IMAGE_LIST格式: imageUrl={}, result={}", imageUrl, imageListResult);
            return imageListResult;

        } catch (Exception e) {
            log.error("火山引擎图生图失败: imageUrl={}, prompt={}, style={}", imageUrl, prompt, style, e);
            e.printStackTrace();
            throw new BusinessException(ResultCode.ERROR, "火山引擎图片生成失败: "+ e.getMessage());
        }
    }

    /**
     * 老照片修复/增强：
     * 使用火山引擎对老照片进行清晰度增强、去噪和人脸细节智能修复。
     *
     * 注意：这里返回的是裸的图片URL，不再包装为 IMAGE_LIST，
     * 由调用方按需要决定是否包装。
     */
    @Override
    public String enhancePhoto(String imageUrl, String prompt) {
        try {
            // 如果调用方没有传入自定义提示词，使用默认的中文提示
            String finalPrompt = (prompt == null || prompt.trim().isEmpty())
                    ? "请将照片修复清晰，提高分辨率，消除噪点，智能修复人物面部细节。"
                    : prompt.trim();

            GenerateImagesRequest generateRequest = GenerateImagesRequest.builder()
                    .model(huoshanProperties.getImageModel())
                    .prompt(finalPrompt)
                    .image(imageUrl)
                    .size("2K")
                    .sequentialImageGeneration("disabled")
                    .responseFormat(ResponseFormat.Url)
                    .stream(false)
                    .watermark(false) // 老照片修复通常不希望带水印
                    .build();

            ImagesResponse imagesResponse = arkService.generateImages(generateRequest);

            if (imagesResponse == null || imagesResponse.getData() == null || imagesResponse.getData().isEmpty()) {
                throw new BusinessException(ResultCode.ERROR, "火山引擎老照片修复失败：响应为空");
            }

            String resultUrl = imagesResponse.getData().get(0).getUrl();
            log.info("火山引擎老照片修复成功: imageUrl={}, prompt={}, resultUrl={}", imageUrl, finalPrompt, resultUrl);

            return resultUrl;
        } catch (Exception e) {
            log.error("火山引擎老照片修复失败: imageUrl={}, prompt={}", imageUrl, prompt, e);
            if (e instanceof BusinessException) {
                throw e;
            }
            String errorMessage = e.getMessage();
            throw new BusinessException(ResultCode.ERROR, "火山引擎老照片修复失败: " + (errorMessage != null ? errorMessage : "未知错误"));
        }
    }

    /**
     * 图片理解 / 识图：
     * 使用火山 Ark 豆包视觉模型进行图片理解（/chat/completions）
     */
    @Override
    public String understandImage(String imageUrl, String question) {
        if (!StringUtils.hasText(huoshanProperties.getApiKey())) {
            throw new IllegalStateException("未配置火山 Ark API Key：请在 application.yml 配置 huoshan.api-key（或设置环境变量 ARK_API_KEY）");
        }
        if (!StringUtils.hasText(huoshanProperties.getBaseUrl())) {
            throw new IllegalStateException("未配置火山 Ark base-url：请在 application.yml 配置 huoshan.base-url");
        }
        if (!StringUtils.hasText(imageUrl)) {
            throw new IllegalArgumentException("图片地址不能为空");
        }

        String model = StringUtils.hasText(huoshanProperties.getVisionModel())
                ? huoshanProperties.getVisionModel()
                : "doubao-1-5-vision-pro-32k-250115";

        // content = [ {type:image_url,...}, {type:text,...} ]
        java.util.List<java.util.Map<String, Object>> contentList = new java.util.ArrayList<>();
        java.util.Map<String, Object> imagePart = new java.util.LinkedHashMap<>();
        imagePart.put("type", "image_url");
        java.util.Map<String, Object> imageUrlObj = new java.util.LinkedHashMap<>();
        imageUrlObj.put("url", imageUrl);
        imagePart.put("image_url", imageUrlObj);
        contentList.add(imagePart);

        java.util.Map<String, Object> textPart = new java.util.LinkedHashMap<>();
        textPart.put("type", "text");
        textPart.put("text", StringUtils.hasText(question) ? question : "图片主要讲了什么？请用简洁的中文总结。");
        contentList.add(textPart);

        java.util.List<java.util.Map<String, Object>> messages = new java.util.ArrayList<>();
        java.util.Map<String, Object> userMsg = new java.util.LinkedHashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", contentList);
        messages.add(userMsg);

        java.util.Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(huoshanProperties.getApiKey().trim());

        HttpEntity<java.util.Map<String, Object>> entity = new HttpEntity<>(body, headers);

        String url = joinUrl(huoshanProperties.getBaseUrl(), "/chat/completions");
        try {
            ResponseEntity<java.util.Map<String, Object>> resp = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    new org.springframework.core.ParameterizedTypeReference<java.util.Map<String, Object>>() {}
            );
            java.util.Map<String, Object> respBody = resp.getBody();
            String content = OpenAiCompatJsonUtils.firstChatContent(respBody);
            if (!StringUtils.hasText(content)) {
                throw new IllegalStateException("火山视觉模型未返回内容");
            }
            return content.trim();
        } catch (Exception e) {
            log.error("调用火山视觉模型失败, imageUrl={}", imageUrl, e);
            throw new IllegalStateException("火山视觉图片理解失败: " + e.getMessage());
        }
    }

    private static String joinUrl(String base, String path) {
        if (!StringUtils.hasText(base)) return path;
        String b = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        String p = path.startsWith("/") ? path : ("/" + path);
        return b + p;
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

