package com.example.simvoice.controller;

import com.example.simvoice.dto.*;
import com.example.simvoice.entity.ImageGenerateTask;
import com.example.simvoice.entity.ImageTemplate;
import com.example.simvoice.result.Result;
import com.example.simvoice.service.HuoshanImageService;
import com.example.simvoice.service.ImageGenerateTaskService;
import com.example.simvoice.service.ImageTemplateService;
import com.example.simvoice.service.StsService;
import com.example.simvoice.service.ToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 工具控制器
 * 提供各种AI工具功能的HTTP接口
 * 
 * @author ai-translation-service
 * @since 1.0
 */
@RestController
@RequestMapping("/tool")
@RequiredArgsConstructor
public class ToolController {

    private final ToolService toolService;
    private final StsService stsService;
    private final ImageTemplateService imageTemplateService;
    private final ImageGenerateTaskService imageGenerateTaskService;
    private final HuoshanImageService huoshanImageService;

    /**
     * AI头像生成接口
     * 
     * @param dto AI头像生成请求参数，包含用户上传的图片等信息
     * @param request HTTP请求对象，用于获取用户openid（从JWT拦截器注入）
     * @return 统一返回结果，包含生成的头像图片URL
     */
    @PostMapping("/ai-avatar")
    public Result<Map<String, String>> generateAiAvatar(@RequestBody AiAvatarDTO dto, HttpServletRequest request) {
        String openid = (String) request.getAttribute("openid");
        if (openid == null || openid.isEmpty()) {
            return Result.unauthorized();
        }
        String resultUrl = toolService.generateAiAvatar(openid, dto);
        Map<String, String> result = new HashMap<>();
        result.put("resultUrl", resultUrl);
        return Result.success(result);
    }

    /**
     * 获取OSS PostObject签名接口
     * 用于前端直传OSS时获取签名，避免在前端暴露永久密钥
     * 
     * @param fileName 文件名（可选），不传则自动生成
     * @param request HTTP请求对象，用于获取用户openid（从JWT拦截器注入）
     * @return 统一返回结果，包含签名信息（accessKeyId、policy、signature、host等）
     */
    @GetMapping("/oss/post-signature")
    public Result<Map<String, Object>> getPostObjectSignature(@RequestParam(required = false) String fileName, HttpServletRequest request) {
        try {
            // 如果没有指定文件名，自动生成
            if (fileName == null || fileName.isEmpty()) {
                String timestamp = String.valueOf(System.currentTimeMillis());
                String random = UUID.randomUUID().toString().substring(0, 8);
                java.time.LocalDate date = java.time.LocalDate.now();
                String dateStr = date.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
                fileName = "upload/" + dateStr + "/" + timestamp + "_" + random;
            }
            Map<String, Object> signature = stsService.getPostObjectSignature(fileName);
            return Result.success(signature);
        } catch (Exception e) {
            return Result.error("获取签名失败: " + e.getMessage());
        }
    }

    /**
     * 老照片修复（火山引擎）- 单张
     *
     * @param dto 请求参数，包含图片URL
     * @param request HTTP请求对象，用于获取用户openid
     * @return 修复后的图片URL
     */
    @PostMapping("/restore-old-photo")
    public Result<Map<String, String>> restoreOldPhoto(@RequestBody OldPhotoRestoreDTO dto, HttpServletRequest request) {
        String openid = (String) request.getAttribute("openid");
        if (openid == null || openid.isEmpty()) {
            return Result.unauthorized();
        }
        String resultUrl = toolService.restoreOldPhoto(openid, dto);
        Map<String, String> result = new HashMap<>();
        result.put("resultUrl", resultUrl);
        return Result.success(result);
    }

    /**
     * 老照片批量修复（火山引擎）
     *
     * @param dto 批量修复请求参数，包含图片URL列表
     * @param request HTTP请求对象，用于获取用户openid
     * @return 修复后的图片URL列表
     */
    @PostMapping("/batch-restore-old-photo")
    public Result<Map<String, String>> batchRestoreOldPhoto(@RequestBody BatchRestoreOldPhotoDTO dto, HttpServletRequest request) {
        String openid = (String) request.getAttribute("openid");
        if (openid == null || openid.isEmpty()) {
            return Result.unauthorized();
        }
        String resultUrl = toolService.batchRestoreOldPhoto(openid, dto);
        Map<String, String> result = new HashMap<>();
        result.put("resultUrl", resultUrl);
        return Result.success(result);
    }

    /**
     * 即时翻译接口
     * 支持中译英、中译日、中译蒙、英译中、日译中、蒙译中
     *
     * @param dto 翻译请求参数，包含文本、源语言和目标语言
     * @param request HTTP请求对象，用于获取用户openid
     * @return 统一返回结果，包含翻译结果
     */
    @PostMapping("/translate")
    public Result<Map<String, String>> translate(@RequestBody TranslateDTO dto, HttpServletRequest request) {
        String openid = (String) request.getAttribute("openid");
        if(dto.getFrom().equals(dto.getTo())){
            return Result.error("源语言和目标语言不能相同");
        }
        if (openid == null || openid.isEmpty()) {
            return Result.unauthorized();
        }
        String result = toolService.translate(openid, dto);
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("result", result);
        resultMap.put("from", dto.getFrom());
        resultMap.put("to", dto.getTo());
        return Result.success(resultMap);
    }

    /**
     * 获取图片模版列表
     *
     * @return 统一返回结果，包含模版列表
     */
    @GetMapping("/templates")
    public Result<List<Map<String, Object>>> getTemplates() {
        List<ImageTemplate> templates = imageTemplateService.getAllEnabledTemplates();
        List<Map<String, Object>> result = templates.stream().map(template -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", template.getId());
            map.put("name", template.getName());
            map.put("imageUrl", template.getImageUrl());
            map.put("prompt", template.getPrompt());
            map.put("style", template.getStyle());
            map.put("description", template.getDescription());
            return map;
        }).collect(Collectors.toList());
        return Result.success(result);
    }

    /**
     * 创建图片生成任务（统一入口）
     * 支持模式：
     * 1-字生图 2-图生图 3-模版同款 4-模版参考图
     *
     * @param dto 请求参数
     * @param request HTTP请求对象，用于获取用户openid
     * @return 统一返回结果，包含任务ID
     */
    @PostMapping("/image-generate")
    public Result<Map<String, Object>> createImageGenerateTask(@RequestBody ImageGenerateTaskCreateDTO dto, HttpServletRequest request) {
        String openid = (String) request.getAttribute("openid");
        if (openid == null || openid.isEmpty()) {
            return Result.unauthorized();
        }
        Long taskId = toolService.createImageGenerateTask(openid, dto);
        Map<String, Object> result = new HashMap<>();
        result.put("taskId", taskId);
        return Result.success(result);
    }

    /**
     * 查询用户的任务列表
     *
     * @param status 任务状态（可选）：0-排队中 1-生成中 2-已完成 3-失败。不传则查询所有状态
     * @param request HTTP请求对象，用于获取用户openid
     * @return 统一返回结果，包含任务列表
     */
    @GetMapping("/tasks")
    public Result<List<Map<String, Object>>> getTasks(@RequestParam(required = false) Integer status, HttpServletRequest request) {
        String openid = (String) request.getAttribute("openid");
        if (openid == null || openid.isEmpty()) {
            return Result.unauthorized();
        }
        List<ImageGenerateTask> tasks = imageGenerateTaskService.getUserTasks(openid, status);
        List<Map<String, Object>> result = tasks.stream().map(task -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", task.getId());
            map.put("templateId", task.getTemplateId());
            map.put("prompt", task.getPrompt());
            map.put("imageUrl", task.getImageUrl());
            map.put("style", task.getStyle());
            map.put("generateMode", task.getGenerateMode());
            map.put("taskStatus", task.getTaskStatus());
            map.put("resultUrl", task.getResultUrl());
            map.put("errorMessage", task.getErrorMessage());
            map.put("createTime", task.getCreateTime());
            map.put("updateTime", task.getUpdateTime());
            map.put("finishTime", task.getFinishTime());
            return map;
        }).collect(Collectors.toList());
        return Result.success(result);
    }

    /**
     * 查询任务详情
     *
     * @param taskId 任务ID
     * @param request HTTP请求对象，用于获取用户openid
     * @return 统一返回结果，包含任务详情
     */
    @GetMapping("/task/{taskId}")
    public Result<Map<String, Object>> getTaskDetail(@PathVariable Long taskId, HttpServletRequest request) {
        String openid = (String) request.getAttribute("openid");
        if (openid == null || openid.isEmpty()) {
            return Result.unauthorized();
        }
        ImageGenerateTask task = imageGenerateTaskService.getTaskById(taskId);
        
        // 验证任务是否属于当前用户
        if (!task.getOpenid().equals(openid)) {
            return Result.error("无权访问该任务");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", task.getId());
        result.put("templateId", task.getTemplateId());
        result.put("prompt", task.getPrompt());
        result.put("imageUrl", task.getImageUrl());
        result.put("style", task.getStyle());
        result.put("generateMode", task.getGenerateMode());
        result.put("taskStatus", task.getTaskStatus());
        result.put("resultUrl", task.getResultUrl());
        result.put("errorMessage", task.getErrorMessage());
        result.put("createTime", task.getCreateTime());
        result.put("updateTime", task.getUpdateTime());
        result.put("finishTime", task.getFinishTime());
        return Result.success(result);
    }

    /**
     * 下载生成的图片
     * 注意：这里返回的是图片URL，实际下载由前端处理
     * 如果需要服务端下载，可以使用OSS的预签名URL或直接返回OSS URL
     *
     * @param taskId 任务ID
     * @param request HTTP请求对象，用于获取用户openid
     * @return 统一返回结果，包含图片下载URL
     */
    @GetMapping("/task/{taskId}/download")
    public Result<Map<String, String>> downloadImage(@PathVariable Long taskId, HttpServletRequest request) {
        String openid = (String) request.getAttribute("openid");
        if (openid == null || openid.isEmpty()) {
            return Result.unauthorized();
        }
        ImageGenerateTask task = imageGenerateTaskService.getTaskById(taskId);
        
        // 验证任务是否属于当前用户
        if (!task.getOpenid().equals(openid)) {
            return Result.error("无权访问该任务");
        }
        
        // 验证任务是否已完成
        if (task.getTaskStatus() != 2) {
            return Result.error("任务尚未完成，无法下载");
        }
        
        if (task.getResultUrl() == null || task.getResultUrl().isEmpty()) {
            return Result.error("图片URL不存在");
        }
        
        Map<String, String> result = new HashMap<>();
        result.put("downloadUrl", task.getResultUrl());
        result.put("taskId", String.valueOf(taskId));
        return Result.success(result);
    }

    /**
     * AI 识图接口：使用火山引擎识别图片中的文字并提炼为中文说明
     *
     * 请求体示例：
     * {
     *   "imageUrl": "https://xxx.oss-cn-beijing.aliyuncs.com/path/to/image.jpg"
     * }
     */
    @PostMapping("/image-recognize")
    public Result<Map<String, String>> recognizeImage(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String openid = (String) request.getAttribute("openid");
        if (openid == null || openid.isEmpty()) {
            return Result.unauthorized();
        }
        Object urlObj = body.get("imageUrl");
        String imageUrl = urlObj == null ? null : String.valueOf(urlObj).trim();
        if (imageUrl == null || imageUrl.isEmpty()) {
            return Result.error("图片地址不能为空");
        }
        String text = huoshanImageService.understandImage(imageUrl, "图片主要讲了什么？请用简洁的中文总结。");
        Map<String, String> result = new HashMap<>();
        result.put("text", text);
        return Result.success(result);
    }
}

