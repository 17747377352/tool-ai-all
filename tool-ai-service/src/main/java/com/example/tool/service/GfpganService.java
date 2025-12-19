package com.example.tool.service;

/**
 * GFPGAN 老照片修复服务
 */
public interface GfpganService {

    /**
     * 调用云端 GFPGAN 修复图片
     *
     * @param imageUrl 可公开访问的原图URL
     * @param strength 修复强度（0-1，可为空，用于映射scale）
     * @return 修复后的图片字节
     */
    byte[] restore(String imageUrl, Double strength);
}

