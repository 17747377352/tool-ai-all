package com.example.tool.dto;

import lombok.Data;

/**
 * 老照片修复请求
 */
@Data
public class OldPhotoRestoreDTO {
    /**
     * 已上传至 OSS 的图片 URL
     */
    private String imageUrl;

    /**
     * 修复强度（0-1，可选）
     */
    private Double strength;
}






