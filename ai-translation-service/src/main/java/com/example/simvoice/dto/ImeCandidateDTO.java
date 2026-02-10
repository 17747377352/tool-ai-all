package com.example.simvoice.dto;

import lombok.Data;

/**
 * 输入法候选词 DTO
 */
@Data
public class ImeCandidateDTO {
    private String id;
    private String word;
    private String shape;
    private Integer score;
    private String latin;
}


