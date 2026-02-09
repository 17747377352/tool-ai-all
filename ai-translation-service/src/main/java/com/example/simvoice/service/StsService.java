package com.example.simvoice.service;

import java.util.Map;

/**
 * STS临时凭证服务接口
 * 用于前端直传OSS时获取临时凭证或签名
 */
public interface StsService {
    /**
     * 获取OSS PostObject签名
     * 用于前端直接上传文件到OSS
     * 
     * @param fileName 文件名（包含路径）
     * @return 包含签名、policy、host等信息的Map
     */
    Map<String, Object> getPostObjectSignature(String fileName);
}
