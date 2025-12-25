package com.example.tool.dto;

import lombok.Data;

/**
 * 老照片修复请求DTO
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@Data
public class OldPhotoRestoreDTO {
    /**
     * 已上传至OSS的图片URL（公网可访问地址）
     */
    private String imageUrl;

    /**
     * 修复强度（0.0-1.0，可选，默认0.7）
     * 值越大修复效果越强，但可能带来轻微失真
     */
    private Double strength;
}








