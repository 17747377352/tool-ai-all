package com.example.simvoice.dto;

import lombok.Data;
import java.util.List;

/**
 * 老照片批量修复请求DTO
 * 
 * @author ai-translation-service
 * @since 1.0
 */
@Data
public class BatchRestoreOldPhotoDTO {
    /**
     * 图片URL列表（最多10张）
     */
    private List<String> imageUrls;
    
    /**
     * 修复强度（0.0-1.0，可选，默认0.7）
     */
    private Double strength;
}

