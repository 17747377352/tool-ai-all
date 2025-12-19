package com.example.tool.controller;

import com.example.tool.dto.*;
import com.example.tool.result.Result;
import com.example.tool.service.LocalFileService;
import com.example.tool.service.ToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 工具控制器
 * 提供各种AI工具功能的HTTP接口
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@RestController
@RequestMapping("/tool")
@RequiredArgsConstructor
public class ToolController {

    private final ToolService toolService;
    private final LocalFileService localFileService;

    /**
     * 短视频去水印接口
     * 
     * @param dto 去水印请求参数，包含视频URL等信息
     * @param request HTTP请求对象，用于获取用户openid（从JWT拦截器注入）
     * @return 统一返回结果，包含去水印后的视频URL
     */
    @PostMapping("/remove-logo")
    public Result<Map<String, String>> removeLogo(@RequestBody RemoveLogoDTO dto, HttpServletRequest request) {
        String openid = (String) request.getAttribute("openid");
        String resultUrl = toolService.removeLogo(openid, dto);
        Map<String, String> result = new HashMap<>();
        result.put("resultUrl", resultUrl);
        return Result.success(result);
    }

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
        String resultUrl = toolService.generateAiAvatar(openid, dto);
        Map<String, String> result = new HashMap<>();
        result.put("resultUrl", resultUrl);
        return Result.success(result);
    }

    /**
     * 姓氏签名生成接口
     * 
     * @param dto 签名生成请求参数，包含姓氏和风格等信息
     * @param request HTTP请求对象，用于获取用户openid（从JWT拦截器注入）
     * @return 统一返回结果，包含生成的签名图片URL
     */
    @PostMapping("/name-sign")
    public Result<Map<String, String>> generateNameSign(@RequestBody NameSignDTO dto, HttpServletRequest request) {
        String openid = (String) request.getAttribute("openid");
        String resultUrl = toolService.generateNameSign(openid, dto);
        Map<String, String> result = new HashMap<>();
        result.put("resultUrl", resultUrl);
        return Result.success(result);
    }

    /**
     * 今日运势生成接口（万年历 + 火山图片）
     * 
     * @param dto 今日运势请求参数，包含查询日期
     * @param request HTTP请求对象，用于获取用户openid（从JWT拦截器注入）
     * @return 统一返回结果，包含生成的运势图片URL
     */
    @PostMapping("/fortune")
    public Result<Map<String, String>> generateFortune(@RequestBody FortuneDTO dto, HttpServletRequest request) {
        String openid = (String) request.getAttribute("openid");
        String resultUrl = toolService.generateFortune(openid, dto);
        Map<String, String> result = new HashMap<>();
        result.put("resultUrl", resultUrl);
        return Result.success(result);
    }

    /**
     * 星座运势生成接口
     * 优先从数据库查询今日运势（由定时任务每天凌晨生成），如果不存在则实时生成
     * 
     * @param dto 星座运势请求参数，包含星座名称
     * @param request HTTP请求对象，用于获取用户openid（从JWT拦截器注入）
     * @return 统一返回结果，包含生成的星座运势图片URL
     */
    @PostMapping("/constellation-fortune")
    public Result<Map<String, String>> generateConstellationFortune(@RequestBody ConstellationFortuneDTO dto, HttpServletRequest request) {
        String openid = (String) request.getAttribute("openid");
        String resultUrl = toolService.generateConstellationFortune(openid, dto);
        Map<String, String> result = new HashMap<>();
        result.put("resultUrl", resultUrl);
        return Result.success(result);
    }

    /**
     * 图片上传接口
     * 用于上传图片到本地存储，返回可访问的URL（用于图生图功能）
     * 
     * @param file 上传的图片文件
     * @param request HTTP请求对象，用于获取用户openid（从JWT拦截器注入）
     * @return 统一返回结果，包含上传后的图片URL
     */
    @PostMapping("/upload-image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            if (file == null || file.isEmpty()) {
                return Result.error("文件不能为空");
            }
            
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = "ai-avatar/" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) 
                    + "/" + UUID.randomUUID().toString() + extension;
            
            // 上传到本地存储
            String imageUrl = localFileService.uploadFile(file.getInputStream(), fileName, file.getContentType());
            
            Map<String, String> result = new HashMap<>();
            result.put("imageUrl", imageUrl);
            return Result.success(result);
            
        } catch (IOException e) {
            return Result.error("图片上传失败: " + e.getMessage());
        } catch (Exception e) {
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 老照片修复（GFPGAN）
     *
     * @param dto 请求参数，包含图片URL
     * @param request HTTP请求对象，用于获取用户openid
     * @return 修复后的图片URL
     */
    @PostMapping("/restore-old-photo")
    public Result<Map<String, String>> restoreOldPhoto(@RequestBody OldPhotoRestoreDTO dto, HttpServletRequest request) {
        String openid = (String) request.getAttribute("openid");
        String resultUrl = toolService.restoreOldPhoto(openid, dto);
        Map<String, String> result = new HashMap<>();
        result.put("resultUrl", resultUrl);
        return Result.success(result);
    }
}

