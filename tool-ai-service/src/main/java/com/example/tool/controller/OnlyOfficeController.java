package com.example.tool.controller;

import com.example.tool.context.LocalFileProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

/**
 * OnlyOffice 协同编辑控制器
 * 提供文件下载、配置获取、回调保存等功能
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/onlyoffice")
@CrossOrigin
@RequiredArgsConstructor
public class OnlyOfficeController {

    private final LocalFileProperties localFileProperties;

    /**
     * 创建包含默认内容的文件
     * 
     * @param file 目标文件
     * @param fileName 文件名
     */
    private void createDefaultFile(File file, String fileName) {
        try {
            if (fileName.endsWith(".docx")) {
                // 创建 Word 文档
                XWPFDocument document = new XWPFDocument();
                XWPFParagraph paragraph = document.createParagraph();
                XWPFRun run = paragraph.createRun();
                run.setText("你好");
                run.setFontSize(14);
                
                FileOutputStream out = new FileOutputStream(file);
                document.write(out);
                out.close();
                document.close();
                log.info("已创建 Word 文档: {}", file.getAbsolutePath());
                
            } else if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
                // 创建 Excel 文档
                XSSFWorkbook workbook = new XSSFWorkbook();
                XSSFSheet sheet = workbook.createSheet("Sheet1");
                
                // 创建第一行并设置内容
                XSSFRow row = sheet.createRow(0);
                XSSFCell cell = row.createCell(0);
                cell.setCellValue("你好");
                
                // 自动调整列宽（可选）
                sheet.autoSizeColumn(0);
                
                FileOutputStream out = new FileOutputStream(file);
                workbook.write(out);
                out.close();
                workbook.close();
                log.info("已创建 Excel 文档: {}, 文件大小: {} bytes", file.getAbsolutePath(), file.length());
                
            } else if (fileName.endsWith(".pptx") || fileName.endsWith(".ppt")) {
                // 创建 PowerPoint 文档
                XMLSlideShow ppt = new XMLSlideShow();
                XSLFSlide slide = ppt.createSlide();
                // 创建文本框并设置文本
                XSLFTextShape textShape = slide.createTextBox();
                textShape.setText("你好");
                
                FileOutputStream out = new FileOutputStream(file);
                ppt.write(out);
                out.close();
                ppt.close();
                log.info("已创建 PowerPoint 文档: {}", file.getAbsolutePath());
                
            } else {
                // 其他格式创建空文件
                file.createNewFile();
                log.info("已创建空文件: {}", file.getAbsolutePath());
            }
        } catch (Exception e) {
            log.error("创建默认文件失败: {}", file.getAbsolutePath(), e);
            try {
                // 如果创建失败，至少创建一个空文件
                file.createNewFile();
            } catch (IOException ioException) {
                log.error("创建空文件也失败", ioException);
            }
        }
    }

    /**
     * 生成文档唯一标识符（key）
     * OnlyOffice 使用 key 来标识文档，相同 key 的文档可以协同编辑
     * 注意：同一文件的所有用户必须使用相同的 key 才能实现协同编辑
     * 
     * @param fileName 文件名
     * @param userId 用户ID（用于日志，不影响 key 生成）
     * @return 文档 key
     */
    private String generateDocumentKey(String fileName, String userId) {
        try {
            // 只使用文件名生成 key，确保同一文件的所有用户使用相同的 key
            // 这样才能实现多人协同编辑
            String keyString = fileName;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(keyString.getBytes("UTF-8"));
            
            // 转换为十六进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            String key = sb.toString();
            log.debug("生成文档 key: fileName={}, key={}", fileName, key);
            return key;
        } catch (Exception e) {
            log.error("生成文档 key 失败", e);
            // 如果生成失败，使用简单的文件名哈希
            return fileName.replaceAll("[^a-zA-Z0-9]", "_").hashCode() + "";
        }
    }

    /**
     * 前端获取编辑器配置
     * 
     * @param fileName 文件名
     * @param request HTTP请求对象，用于获取用户信息
     * @return 编辑器配置
     */
    @GetMapping("/config")
    public Map<String, Object> config(String fileName, 
                                      @RequestParam(value = "userId", required = false) String userIdParam,
                                      @RequestParam(value = "userName", required = false) String userNameParam,
                                      HttpServletRequest request) {
        if (fileName == null || fileName.isEmpty()) {
            fileName = "demo.docx";
        }
        
        // 获取用户信息（优先使用请求参数，其次从JWT拦截器注入，最后使用默认值）
        String userId = userIdParam;
        String userName = userNameParam;
        
        if (userId == null) {
            userId = (String) request.getAttribute("openid");
        }
        if (userName == null) {
            userName = (String) request.getAttribute("userName");
        }
        
        if (userId == null) {
            userId = "1";
        }
        if (userName == null) {
            userName = "用户" + userId;
        }
        
        // 构建文件URL和回调URL
        // 注意：OnlyOffice 容器需要访问这些 URL，所以使用 onlyOfficeBaseUrl
        // 浏览器访问配置接口时使用 baseUrl，但返回给 OnlyOffice 的 URL 需要使用 onlyOfficeBaseUrl
        String onlyOfficeBaseUrl = localFileProperties.getOnlyOfficeBaseUrlForContainer();
        String fileUrl;
        String callbackUrl;
        try {
            fileUrl = onlyOfficeBaseUrl + "/onlyoffice/download?fn=" + java.net.URLEncoder.encode(fileName, "UTF-8");
            callbackUrl = onlyOfficeBaseUrl + "/onlyoffice/callback?fn=" + java.net.URLEncoder.encode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("URL编码失败", e);
            fileUrl = onlyOfficeBaseUrl + "/onlyoffice/download?fn=" + fileName;
            callbackUrl = onlyOfficeBaseUrl + "/onlyoffice/callback?fn=" + fileName;
        }
        log.debug("OnlyOffice URL配置: fileUrl={}, callbackUrl={}", fileUrl, callbackUrl);
        
        // 判断文档类型
        String documentType = "text";
        if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
            documentType = "spreadsheet";
        } else if (fileName.endsWith(".pptx") || fileName.endsWith(".ppt")) {
            documentType = "presentation";
        }
        
        // 获取文件扩展名
        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
        
        // 生成文档唯一标识符（key）
        // key 用于标识文档，相同 key 的文档可以协同编辑
        String documentKey = generateDocumentKey(fileName, userId);
        
        Map<String, Object> cfg = new HashMap<>();
        cfg.put("documentType", documentType);
        
        Map<String, Object> document = new HashMap<>();
        document.put("title", fileName);
        document.put("url", fileUrl);
        document.put("fileType", fileType);
        document.put("key", documentKey);  // 必须字段：文档唯一标识符
        cfg.put("document", document);
        
        Map<String, Object> editorConfig = new HashMap<>();
        editorConfig.put("callbackUrl", callbackUrl);
        editorConfig.put("mode", "edit");
        // 移动端支持：false 表示允许编辑，true 表示强制只读视图
        editorConfig.put("mobileForceView", false);
        
        Map<String, Object> user = new HashMap<>();
        user.put("id", userId);
        user.put("name", userName);
        editorConfig.put("user", user);
        
        cfg.put("editorConfig", editorConfig);
        
        log.info("OnlyOffice配置生成: fileName={}, userId={}, userName={}", fileName, userId, userName);
        
        return cfg;
    }

    /**
     * 给 OnlyOffice 下载文件
     * 
     * @param fn 文件名
     * @param response HTTP响应对象
     * @throws IOException IO异常
     */
    @GetMapping("/download")
    public void download(@RequestParam("fn") String fn, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String fileName = null;
        try {
            // 解码文件名
            fileName = java.net.URLDecoder.decode(fn, "UTF-8");
            log.info("OnlyOffice下载请求: fn={}, decodedFileName={}", fn, fileName);
            
            // 构建文件路径
            // base-path 已经包含了 onlyoffice 目录，直接使用
            File storageDir = new File(localFileProperties.getBasePath());
            if (!storageDir.exists()) {
                storageDir.mkdirs();
                log.info("创建存储目录: {}", storageDir.getAbsolutePath());
            }
            
            File file = new File(storageDir, fileName);
            log.info("文件路径: {}", file.getAbsolutePath());
            
            // 如果文件不存在，创建包含默认内容的文件
            if (!file.exists()) {
                log.warn("文件不存在，创建默认文件: {}", file.getAbsolutePath());
                createDefaultFile(file, fileName);
            }
            
            // 检查文件是否存在
            if (!file.exists()) {
                log.error("文件创建失败: {}", file.getAbsolutePath());
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在");
                return;
            }
            
            // 设置响应头
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", 
                    "attachment;filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
            response.setContentLengthLong(file.length());
            // 允许跨域访问（OnlyOffice 需要）
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "*");
            
            // 记录请求来源（用于调试）
            String referer = request.getHeader("Referer");
            String userAgent = request.getHeader("User-Agent");
            log.info("下载请求来源: referer={}, userAgent={}, fileSize={}", referer, userAgent, file.length());
            
            // 复制文件内容到响应流
            FileCopyUtils.copy(new FileInputStream(file), response.getOutputStream());
            
            log.info("文件下载成功: fileName={}, size={}", fileName, file.length());
            
        } catch (Exception e) {
            log.error("文件下载失败: fn={}, fileName={}", fn, fileName, e);
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "文件下载失败: " + e.getMessage());
            }
        }
    }

    /**
     * 处理 CORS 预检请求
     */
    @RequestMapping(value = "/callback", method = RequestMethod.OPTIONS)
    public void optionsCallback(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * OnlyOffice 回写文件
     * 
     * @param fn 文件名
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @return 回调响应
     */
    @PostMapping("/callback")
    public void callback(@RequestParam("fn") String fn, HttpServletRequest request, HttpServletResponse response) {
        try {
            // 设置 CORS 头（OnlyOffice 需要）
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "*");
            response.setContentType("application/json;charset=UTF-8");
            
            // 解码文件名
            String fileName = java.net.URLDecoder.decode(fn, "UTF-8");
            
            // 读取回调请求体
            String body = org.apache.commons.io.IOUtils.toString(request.getInputStream(), "UTF-8");
            log.info("OnlyOffice回调: fileName={}, body={}", fileName, body);
            
            // 解析JSON
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.readValue(body, ObjectNode.class);
            
            int status = node.get("status").asInt();
            log.info("回调状态: fileName={}, status={}", fileName, status);
            
            // status: 2=文档已保存，6=文档正在保存
            if (status == 2 || status == 6) {
                // 从回调URL下载文件
                String fileUrl = node.get("url").asText();
                log.info("开始保存文件: fileName={}, url={}", fileName, fileUrl);
                
                // 构建保存路径
                // base-path 已经包含了 onlyoffice 目录，直接使用
                File storageDir = new File(localFileProperties.getBasePath());
                if (!storageDir.exists()) {
                    storageDir.mkdirs();
                    log.info("创建存储目录: {}", storageDir.getAbsolutePath());
                }
                
                File targetFile = new File(storageDir, fileName);
                
                // 从URL下载文件并保存
                URL url = new URL(fileUrl);
                FileUtils.copyInputStreamToFile(url.openStream(), targetFile);
                
                log.info("文件保存成功: fileName={}, size={}", fileName, targetFile.length());
            } else {
                log.info("文件状态未变更，无需保存: fileName={}, status={}", fileName, status);
            }
            
            // 返回成功响应（OnlyOffice 要求的格式）
            response.getWriter().write("{\"error\":0}");
            response.getWriter().flush();
            
        } catch (Exception e) {
            log.error("OnlyOffice回调处理失败: fn={}", fn, e);
            try {
                response.getWriter().write("{\"error\":1,\"message\":\"" + e.getMessage().replace("\"", "\\\"") + "\"}");
                response.getWriter().flush();
            } catch (IOException ioException) {
                log.error("写入错误响应失败", ioException);
            }
        }
    }
}

