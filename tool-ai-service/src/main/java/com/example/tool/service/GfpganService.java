package com.example.tool.service;

/**
 * GFPGAN 老照片修复服务接口
 * 提供调用云端GFPGAN API进行老照片修复的功能
 * 
 * @author tool-ai-service
 * @since 1.0
 */
public interface GfpganService {

    /**
     * 调用云端GFPGAN修复图片
     * 
     * @param imageUrl 可公开访问的原图URL（必须是公网可访问地址）
     * @param strength 修复强度（0.0-1.0，可为空，用于映射scale）
     * @return 修复后的图片字节数组
     */
    byte[] restore(String imageUrl, Double strength);
}

