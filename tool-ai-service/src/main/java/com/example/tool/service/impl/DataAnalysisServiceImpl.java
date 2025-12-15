package com.example.tool.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.tool.exception.BusinessException;
import com.example.tool.result.ResultCode;
import com.example.tool.service.DataAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * DataAnalysis服务实现类
 * 调用dataAnalysis-backend项目的API接口获取视频信息
 *
 * @author tool-ai-service
 * @since 1.0
 */
@Slf4j
@Service
public class DataAnalysisServiceImpl implements DataAnalysisService {

    /**
     * DataAnalysis服务的基础URL
     * 默认: http://localhost:8000
     */
    @Value("${data-analysis.base-url:http://localhost:8000}")
    private String dataAnalysisBaseUrl;

    /**
     * 是否启用dataAnalysis服务
     * 默认: true
     */
    @Value("${data-analysis.enabled:true}")
    private boolean dataAnalysisEnabled;

    /**
     * 通过dataAnalysis-backend项目获取视频信息
     *
     * @param shareUrl 分享链接
     * @return 无水印视频URL
     */
    @Override
    public String getVideoUrl(String shareUrl) {
        if (StrUtil.isBlank(shareUrl)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "分享链接不能为空");
        }

        if (!dataAnalysisEnabled) {
            throw new BusinessException(ResultCode.ERROR, "dataAnalysis服务未启用");
        }

        try {
            // 判断平台类型
            String platform = detectPlatform(shareUrl);
            String apiUrl;
            
            if ("douyin".equals(platform)) {
                apiUrl = dataAnalysisBaseUrl + "/analyze/douyin";
            } else if ("xiaohongshu".equals(platform)) {
                apiUrl = dataAnalysisBaseUrl + "/analyze/xiaohongshu";
            } else {
                // 使用通用接口，自动识别平台
                apiUrl = dataAnalysisBaseUrl + "/analyze";
            }

            log.info("调用dataAnalysis API: {}, shareUrl: {}", apiUrl, shareUrl);

            // 构建请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("url", shareUrl);
            requestBody.put("type", "png");
            requestBody.put("format", "json");

            // 发送POST请求
            HttpResponse httpResponse = HttpRequest.post(apiUrl)
                    .header("Content-Type", "application/json")
                    .body(requestBody.toJSONString())
                    .timeout(30000) // 30秒超时
                    .execute();

            int statusCode = httpResponse.getStatus();
            String responseBody = httpResponse.body();

            if (statusCode != 200) {
                String errorMsg = "dataAnalysis服务返回错误状态码: " + statusCode;
                // 尝试解析错误信息
                try {
                    JSONObject errorJson = JSON.parseObject(responseBody);
                    if (errorJson != null) {
                        if (errorJson.containsKey("message")) {
                            errorMsg = errorJson.getString("message");
                        } else if (errorJson.containsKey("error")) {
                            errorMsg = errorJson.getString("error");
                        } else if (errorJson.containsKey("detail")) {
                            errorMsg = errorJson.getString("detail");
                        }
                    }
                } catch (Exception e) {
                    // 如果响应不是JSON格式，直接使用响应体作为错误信息
                    if (StrUtil.isNotBlank(responseBody)) {
                        errorMsg = responseBody.length() > 200 ? responseBody.substring(0, 200) : responseBody;
                    }
                }
                throw new BusinessException(ResultCode.ERROR, errorMsg);
            }

            if (StrUtil.isBlank(responseBody)) {
                throw new BusinessException(ResultCode.ERROR, "dataAnalysis服务返回空响应");
            }

            log.info("dataAnalysis API响应: {}",  responseBody);

            // 解析JSON响应
            JSONObject json = null;
            try {
                json = JSON.parseObject(responseBody);
            } catch (Exception e) {
                log.warn("dataAnalysis响应不是有效的JSON格式: {}", responseBody.length() > 200 ? responseBody.substring(0, 200) : responseBody);
                throw new BusinessException(ResultCode.ERROR, "dataAnalysis服务返回非JSON格式响应: " + responseBody);
            }

            if (json == null) {
                throw new BusinessException(ResultCode.ERROR, "无法解析dataAnalysis服务响应");
            }

            // 提取视频URL或图片URL（支持图文内容）
            // dataAnalysis-backend的响应格式可能不同，需要根据实际响应结构调整
            String mediaUrl = extractMediaUrlFromResponse(json);

            if (StrUtil.isBlank(mediaUrl)) {
                log.warn("无法从dataAnalysis响应中提取视频或图片URL，完整响应: {}", responseBody);
                throw new BusinessException(ResultCode.ERROR, "无法从dataAnalysis服务获取视频或图片URL");
            }

            log.info("从dataAnalysis获取到媒体URL: {}", mediaUrl);
            return mediaUrl;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用dataAnalysis服务失败: {}", shareUrl, e);
            throw new BusinessException(ResultCode.ERROR, "调用dataAnalysis服务失败: " + e.getMessage());
        }
    }

    /**
     * 检测平台类型
     */
    private String detectPlatform(String url) {
        if (url.contains("douyin.com") || url.contains("iesdouyin.com")) {
            return "douyin";
        } else if (url.contains("xiaohongshu.com")) {
            return "xiaohongshu";
        }
        return "unknown";
    }

    /**
     * 从响应中提取视频URL或图片URL（支持图文内容）
     * 根据dataAnalysis-backend的实际响应格式调整
     * 响应格式：{"code":200,"data":{"video":"https://...","image_list":[...],"url":"...",...},"message":"..."}
     */
    private String extractMediaUrlFromResponse(JSONObject json) {
        // 优先从 data.video 字段提取（这是 dataAnalysis-backend 的实际响应格式）
        if (json.containsKey("data")) {
            Object dataObj = json.get("data");
            if (dataObj instanceof JSONObject) {
                JSONObject dataJson = (JSONObject) dataObj;
                
                // 优先级1: data.video（最准确的视频URL）
                if (dataJson.containsKey("video")) {
                    String videoUrl = dataJson.getString("video");
                    // 检查是否为空字符串（图文内容时video可能是空字符串）
                    if (StrUtil.isNotBlank(videoUrl) && videoUrl.startsWith("http")) {
                        // 排除分享链接，只返回实际的视频URL
                        if (!videoUrl.contains("v.douyin.com") && !videoUrl.contains("v.xiaohongshu.com")) {
                            return videoUrl;
                        }
                    }
                }
                
                // 优先级1.5: data.image_list（图文内容，返回所有图片URL，用逗号分隔）
                if (dataJson.containsKey("image_list")) {
                    com.alibaba.fastjson.JSONArray imageList = dataJson.getJSONArray("image_list");
                    if (imageList != null && !imageList.isEmpty()) {
                        // 收集所有有效的图片URL
                        java.util.List<String> imageUrls = new java.util.ArrayList<>();
                        for (int i = 0; i < imageList.size(); i++) {
                            String imageUrl = imageList.getString(i);
                            if (StrUtil.isNotBlank(imageUrl) && imageUrl.startsWith("http")) {
                                imageUrls.add(imageUrl);
                            }
                        }
                        if (!imageUrls.isEmpty()) {
                            // 返回所有图片URL，用特殊分隔符连接（使用JSON格式，方便前端解析）
                            String allImages = com.alibaba.fastjson.JSON.toJSONString(imageUrls);
                            log.info("检测到图文内容，共{}张图片，返回所有图片URL", imageUrls.size());
                            // 使用特殊前缀标识这是多图内容，格式：IMAGE_LIST:["url1","url2",...]
                            return "IMAGE_LIST:" + allImages;
                        }
                    }
                }
                
                // 优先级2: data.video_url
                if (dataJson.containsKey("video_url")) {
                    String videoUrl = dataJson.getString("video_url");
                    if (StrUtil.isNotBlank(videoUrl) && videoUrl.startsWith("http")) {
                        if (!videoUrl.contains("v.douyin.com") && !videoUrl.contains("v.xiaohongshu.com")) {
                            return videoUrl;
                        }
                    }
                }
                
                // 优先级3: data.play_url
                if (dataJson.containsKey("play_url")) {
                    String videoUrl = dataJson.getString("play_url");
                    if (StrUtil.isNotBlank(videoUrl) && videoUrl.startsWith("http")) {
                        if (!videoUrl.contains("v.douyin.com") && !videoUrl.contains("v.xiaohongshu.com")) {
                            return videoUrl;
                        }
                    }
                }
                
                // 优先级4: data.final_url（如果存在）
                if (dataJson.containsKey("final_url")) {
                    String finalUrl = dataJson.getString("final_url");
                    if (StrUtil.isNotBlank(finalUrl) && finalUrl.startsWith("http")) {
                        if (!finalUrl.contains("v.douyin.com") && !finalUrl.contains("v.xiaohongshu.com")) {
                            return finalUrl;
                        }
                    }
                }
                
                // 优先级5: data.url（但需要排除分享链接）
                if (dataJson.containsKey("url")) {
                    String url = dataJson.getString("url");
                    if (StrUtil.isNotBlank(url) && url.startsWith("http")) {
                        // 排除分享链接，只返回实际的视频URL
                        if (!url.contains("v.douyin.com") && !url.contains("v.xiaohongshu.com")) {
                            return url;
                        }
                    }
                }
                
                // 优先级6: data.videos[0]（视频数组）
                if (dataJson.containsKey("videos")) {
                    com.alibaba.fastjson.JSONArray videos = dataJson.getJSONArray("videos");
                    if (videos != null && !videos.isEmpty()) {
                        String videoUrl = videos.getString(0);
                        if (StrUtil.isNotBlank(videoUrl) && videoUrl.startsWith("http")) {
                            if (!videoUrl.contains("v.douyin.com") && !videoUrl.contains("v.xiaohongshu.com")) {
                                return videoUrl;
                            }
                        }
                    }
                }
            }
        }
        
        // 如果以上路径都不匹配，尝试其他可能的路径
        String[] possiblePaths = {
            "video",                // 根级别的video字段
            "video_url",            // 根级别的video_url字段
            "play_url",             // 根级别的play_url字段
            "url"                   // 根级别的url字段（但需要排除分享链接）
        };

        for (String path : possiblePaths) {
            String videoUrl = getValueByPath(json, path);
            if (StrUtil.isNotBlank(videoUrl) && videoUrl.startsWith("http")) {
                // 排除分享链接
                if (!videoUrl.contains("v.douyin.com") && !videoUrl.contains("v.xiaohongshu.com")) {
                    return videoUrl;
                }
            }
        }

        // 如果以上路径都不匹配，尝试深度搜索
        return deepSearchVideoUrl(json);
    }

    /**
     * 根据路径获取JSON值
     */
    private String getValueByPath(JSONObject json, String path) {
        try {
            String[] parts = path.split("\\.");
            Object current = json;
            for (String part : parts) {
                if (part.contains("[")) {
                    String arrayName = part.substring(0, part.indexOf("["));
                    int index = Integer.parseInt(part.substring(part.indexOf("[") + 1, part.indexOf("]")));
                    if (current instanceof JSONObject) {
                        com.alibaba.fastjson.JSONArray array = ((JSONObject) current).getJSONArray(arrayName);
                        if (array != null && array.size() > index) {
                            current = array.get(index);
                        } else {
                            return null;
                        }
                    } else {
                        return null;
                    }
                } else {
                    if (current instanceof JSONObject) {
                        current = ((JSONObject) current).get(part);
                    } else {
                        return null;
                    }
                }
                if (current == null) return null;
            }
            return current instanceof String ? (String) current : String.valueOf(current);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 深度搜索视频URL
     */
    private String deepSearchVideoUrl(Object obj) {
        if (obj == null) return null;
        if (obj instanceof String) {
            String str = (String) obj;
            if (str.startsWith("http") && (str.contains("play") || str.contains(".mp4") || str.contains("video"))) {
                return str;
            }
            return null;
        }
        if (obj instanceof JSONObject) {
            JSONObject json = (JSONObject) obj;
            // 优先查找常见的视频URL字段
            String[] videoFields = {"video", "video_url", "url", "play_url", "download_url", "playApi", "playAddr", "play_addr"};
            for (String field : videoFields) {
                Object value = json.get(field);
                if (value != null) {
                    String result = deepSearchVideoUrl(value);
                    if (result != null) return result;
                }
            }
            // 递归搜索所有字段
            for (String key : json.keySet()) {
                String result = deepSearchVideoUrl(json.get(key));
                if (result != null) return result;
            }
        }
        if (obj instanceof com.alibaba.fastjson.JSONArray) {
            com.alibaba.fastjson.JSONArray array = (com.alibaba.fastjson.JSONArray) obj;
            for (int i = 0; i < array.size(); i++) {
                String result = deepSearchVideoUrl(array.get(i));
                if (result != null) return result;
            }
        }
        return null;
    }
}

