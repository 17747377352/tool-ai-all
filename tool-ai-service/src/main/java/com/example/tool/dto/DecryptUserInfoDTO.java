package com.example.tool.dto;

import lombok.Data;

@Data
public class DecryptUserInfoDTO {
    private String encryptedData;
    private String iv;
    private String sessionKey;
}




