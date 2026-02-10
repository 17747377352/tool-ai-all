package com.example.simvoice.dto;

import lombok.Data;

/**
 * 输入法选择上报 DTO
 */
@Data
public class ImeSelectDTO {
    /**
     * 词条 id（对应 word 表 id）
     */
    private String wordId;
}


