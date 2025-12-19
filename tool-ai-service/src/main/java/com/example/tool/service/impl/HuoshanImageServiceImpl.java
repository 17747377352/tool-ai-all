package com.example.tool.service.impl;

import com.example.tool.context.HuoshanProperties;
import com.example.tool.exception.BusinessException;
import com.example.tool.result.ResultCode;
import com.example.tool.service.HuoshanImageService;
import com.example.tool.service.LocalFileService;
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
 * 使用火山引擎文生图API生成运势图片
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HuoshanImageServiceImpl implements HuoshanImageService {

    private final HuoshanProperties huoshanProperties;
    private final LocalFileService localFileService;

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
     * 根据运势文案生成运势图片
     * 
     * @param fortuneText 运势文案
     * @param name        姓名（用于生成prompt）
     * @param birthDate   出生日期（用于生成prompt，可选）
     * @return 生成的图片URL
     */
    @Override
    public String generateFortuneImage(String fortuneText, String name, String birthDate) {
        try {
            // 构建prompt，将运势信息转换为视觉描述，不再传递文字文案
            String prompt = buildFortunePrompt(name, birthDate);

            GenerateImagesRequest generateRequest = GenerateImagesRequest.builder()
                    .model("doubao-seedream-4-0-250828")
                    .prompt(prompt)
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

            String volcanoImageUrl = imagesResponse.getData().get(0).getUrl();
            log.info("火山引擎运势图片生成成功: name={}, birthDate={}, volcanoUrl={}", name, birthDate, volcanoImageUrl);

            // 下载火山引擎的图片
            cn.hutool.http.HttpResponse httpResponse = cn.hutool.http.HttpRequest.get(volcanoImageUrl)
                    .timeout(30000)
                    .execute();

            if (httpResponse.getStatus() != 200) {
                throw new BusinessException(ResultCode.ERROR, "下载火山引擎图片失败，状态码: " + httpResponse.getStatus());
            }

            byte[] imageBytes = httpResponse.bodyBytes();
            if (imageBytes == null || imageBytes.length == 0) {
                throw new BusinessException(ResultCode.ERROR, "下载的图片数据为空");
            }

            // 生成本地文件名
            String fileName = "fortune/" +
                    java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) +
                    "/" + java.util.UUID.randomUUID().toString() + ".jpeg";

            // 上传到本地存储
            String localUrl = localFileService.uploadFile(imageBytes, fileName, "image/jpeg");
            log.info("今日运势图片已保存到本地: name={}, localUrl={}", name, localUrl);

            // ⭐ 返回IMAGE_LIST格式，与星座运势和去水印保持一致
            String imageListResult = String.format("IMAGE_LIST:[\"%s\"]", localUrl);
            log.info("返回IMAGE_LIST格式: name={}, result={}", name, imageListResult);

            return imageListResult;

        } catch (Exception e) {
            log.error("火山引擎图片生成失败: name={}, birthDate={}", name, birthDate, e);
            if (e instanceof BusinessException) {
                throw e;
            }
            throw new BusinessException(ResultCode.ERROR, "火山引擎图片生成失败: " + e.getMessage());
        }
    }

    /**
     * 根据星座运势文案生成星座运势图片
     * 将火山引擎生成的图片下载后上传到OSS，通过代理返回确保浏览器可以直接预览
     * 
     * @param fortuneText   运势文案
     * @param constellation 星座名称（用于生成prompt）
     * @return 生成的图片代理URL（通过后端代理，可直接预览）
     */
    @Override
    public String generateConstellationFortuneImage(String fortuneText, String constellation) {
        try {
            // 构建prompt，将星座运势文案转换为图片描述
            String prompt = buildConstellationPrompt(fortuneText, constellation);

            GenerateImagesRequest generateRequest = GenerateImagesRequest.builder()
                    .model("doubao-seedream-4-0-250828")
                    .prompt(prompt)
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

            String volcanoImageUrl = imagesResponse.getData().get(0).getUrl();
            log.info("火山引擎星座运势图片生成成功: constellation={}, volcanoUrl={}", constellation, volcanoImageUrl);

            // 下载火山引擎的图片
            cn.hutool.http.HttpResponse httpResponse = cn.hutool.http.HttpRequest.get(volcanoImageUrl)
                    .timeout(30000)
                    .execute();

            if (httpResponse.getStatus() != 200) {
                throw new BusinessException(ResultCode.ERROR, "下载火山引擎图片失败，状态码: " + httpResponse.getStatus());
            }

            byte[] imageBytes = httpResponse.bodyBytes();
            if (imageBytes == null || imageBytes.length == 0) {
                throw new BusinessException(ResultCode.ERROR, "下载的图片数据为空");
            }

            // 生成本地文件名
            String fileName = "constellation-fortune/" +
                    java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) +
                    "/" + java.util.UUID.randomUUID().toString() + ".jpeg";

            // 上传到本地存储
            String localUrl = localFileService.uploadFile(imageBytes, fileName, "image/jpeg");
            log.info("星座运势图片已保存到本地: constellation={}, localUrl={}", constellation, localUrl);

            // ⭐ 返回IMAGE_LIST格式，与去水印保持一致
            // 这样前端可以复用相同的显示逻辑（swiper）
            String imageListResult = String.format("IMAGE_LIST:[\"%s\"]", localUrl);
            log.info("返回IMAGE_LIST格式: constellation={}, result={}", constellation, imageListResult);

            return imageListResult;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("火山引擎星座运势图片生成失败: constellation={}", constellation, e);
            throw new BusinessException(ResultCode.ERROR, "火山引擎图片生成失败: " + e.getMessage());
        }
    }

    /**
     * 将OSS URL转换为代理URL
     * 
     * 注意：此方法当前未使用，但保留以备生产环境使用。
     * 
     * 开发环境策略：直接返回IMAGE_LIST格式的原始URL
     * - 小程序HTTP协议限制：localhost:8080是HTTP，小程序不支持
     * - OSS URL是HTTPS，可以直接使用
     * 
     * 生产环境策略（未来启用）：返回HTTPS代理URL
     * - 需要后端部署到HTTPS域名
     * - 通过代理解决OSS强制下载问题
     * - 浏览器和小程序都能正常预览
     * 
     * @param ossUrl OSS直接URL
     * @return 代理URL
     */
    @SuppressWarnings("unused")
    private String convertToProxyUrl(String ossUrl) {
        try {
            // URL编码
            String encodedUrl = java.net.URLEncoder.encode(ossUrl, "UTF-8");
            // 返回代理URL格式：http://localhost:8080/proxy/image?url=encodedOssUrl
            return "http://localhost:8080/proxy/image?url=" + encodedUrl;
        } catch (Exception e) {
            log.error("URL编码失败: {}", ossUrl, e);
            return ossUrl; // 编码失败，返回原URL
        }
    }

    /**
     * 构建运势图片的prompt
     * 将运势文案转换为适合文生图的描述
     */
    private String buildFortunePrompt(String name, String birthDate) {
        // 构建“仿古黄历条”视觉风格的Prompt
        StringBuilder prompt = new StringBuilder();
        prompt.append("一张极致简约的仿古中式黄历条插画，竖幅长条比例（2:3或更窄），");
        prompt.append("背景主体：一张具有沉淀感、纹理清晰的朱砂红底红纸，");
        prompt.append("核心元素：画面中央垂直排列着墨色笔触的中文字体，呈现出传统木刻版画的活字印刷质感，");
        prompt.append("文字内容感：体现'").append(name).append("的今日运势'这一主题意象，");
        prompt.append("细节：纸张边缘有自然的手工撕裂感或宣纸毛边，黑色焦墨干枯笔触，");
        prompt.append("整体风格：极简主义，传统工艺美学，大量留白，营造禅意与平和感，");
        prompt.append("不含任何现代UI元素，不含公历农历等杂乱小字，只有大气的木刻文字意向，");
        prompt.append("画质：电影级细腻纹理，高对比度。");
        return prompt.toString();
    }

    /**
     * 构建星座运势图片的prompt
     * 基于专业设计模板：正反面双卡片设计，深空主题，精美视觉效果
     */
    private String buildConstellationPrompt(String fortuneText, String constellation) {
        // 过多的细节会触发火山生成失败，这里改为单面简洁卡片描述
        String shortText = fortuneText.length() > 80 ? fortuneText.substring(0, 80) + "..." : fortuneText;

        return new StringBuilder()
                .append("高清、简洁的单面星座运势卡片，2:3比例，")
                .append("深空蓝紫渐变背景，少量柔和星光，留白充足，")
                .append("中心有金色 ").append(constellation).append(" 星座符号与中文名称，")
                .append("下方一行小字标注今日日期，")
                .append("左下角四个轻量图标展示爱情/事业/财运/健康星级，")
                .append("右下角列出幸运数字和幸运颜色，")
                .append("整体现代、干净、清晰、无多余纹理，印刷级质量，300dpi，")
                .append("卡片底部一行简短运势概述：").append(shortText)
                .toString();
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
            throw new BusinessException(ResultCode.ERROR, "火山引擎图片生成失败: " + e.getMessage());
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

    /**
     * 生成姓氏签名图片
     * 根据姓氏和风格生成艺术签名图片
     * 
     * @param surname 姓氏
     * @param style   签名风格：classic（经典）, cursive（行书）, grass（草书）, artistic（艺术）
     * @return 生成的签名图片URL
     */
    @Override
    public String generateNameSignImage(String surname, String style) {
        try {
            // 构建签名prompt
            String prompt = buildNameSignPrompt(surname, style);

            GenerateImagesRequest generateRequest = GenerateImagesRequest.builder()
                    .model("doubao-seedream-4-0-250828")
                    .prompt(prompt)
                    .size("2K")
                    .sequentialImageGeneration("disabled")
                    .responseFormat(ResponseFormat.Url)
                    .stream(false)
                    .watermark(true)
                    .build();

            ImagesResponse imagesResponse = arkService.generateImages(generateRequest);

            if (imagesResponse == null || imagesResponse.getData() == null || imagesResponse.getData().isEmpty()) {
                throw new BusinessException(ResultCode.ERROR, "火山引擎签名生成失败：响应为空");
            }

            String imageUrl = imagesResponse.getData().get(0).getUrl();
            log.info("火山引擎签名生成成功: surname={}, style={}, url={}", surname, style, imageUrl);

            // ⭐ 返回IMAGE_LIST格式，与其他功能保持一致
            String imageListResult = String.format("IMAGE_LIST:[\"%s\"]", imageUrl);
            log.info("返回IMAGE_LIST格式: surname={}, result={}", surname, imageListResult);

            return imageListResult;

        } catch (Exception e) {
            log.error("火山引擎签名生成失败: surname={}, style={}", surname, style, e);
            if (e instanceof BusinessException) {
                throw e;
            }
            throw new BusinessException(ResultCode.ERROR, "火山引擎签名生成失败: " + e.getMessage());
        }
    }

    /**
     * 构建签名图片的prompt
     * 根据姓氏和风格生成适合的图片描述
     * 
     * @param surname 姓氏
     * @param style   签名风格
     * @return 优化后的prompt
     */
    private String buildNameSignPrompt(String surname, String style) {
        StringBuilder prompt = new StringBuilder();

        // 根据风格添加描述
        switch (style) {
            case "classic":
                prompt.append("经典书法风格，");
                prompt.append("工整端庄，笔画清晰，");
                prompt.append("传统楷书或隶书字体，");
                break;
            case "cursive":
                prompt.append("行书风格，");
                prompt.append("流畅自然，笔画连贯，");
                prompt.append("行云流水般的书写风格，");
                break;
            case "grass":
                prompt.append("草书风格，");
                prompt.append("狂放不羁，笔画飘逸，");
                prompt.append("艺术感强，富有动感，");
                break;
            case "artistic":
                prompt.append("艺术签名风格，");
                prompt.append("现代设计感，");
                prompt.append("创意独特，美观大方，");
                break;
            default:
                prompt.append("精美签名风格，");
                break;
        }

        prompt.append("姓氏：").append(surname).append("，");
        prompt.append("签名设计，");
        prompt.append("白色或浅色背景，");
        prompt.append("黑色或深色字体，");
        prompt.append("高质量，清晰，专业，");
        prompt.append("适合作为个人签名使用");

        return prompt.toString();
    }
}
