package com.example.tool.service;

import com.example.tool.dto.*;

/**
 * 工具服务接口
 * 提供各种AI工具功能的核心业务逻辑
 * 
 * @author tool-ai-service
 * @since 1.0
 */
public interface ToolService {
    /**
     * 短视频去水印
     * 
     * @param openid 用户openid
     * @param dto 去水印请求参数
     * @return 去水印后的视频URL
     */
    String removeLogo(String openid, RemoveLogoDTO dto);
    
    /**
     * AI头像生成
     * 
     * @param openid 用户openid
     * @param dto AI头像生成请求参数
     * @return 生成的头像图片URL
     */
    String generateAiAvatar(String openid, AiAvatarDTO dto);
    
    /**
     * 姓氏签名生成
     * 
     * @param openid 用户openid
     * @param dto 签名生成请求参数
     * @return 生成的签名图片URL
     */
    String generateNameSign(String openid, NameSignDTO dto);
    
    /**
     * 运势测试生成
     * 
     * @param openid 用户openid
     * @param dto 运势测试请求参数
     * @return 生成的运势图片URL
     */
    String generateFortune(String openid, FortuneDTO dto);
    
    /**
     * 星座运势生成
     * 优先从数据库查询今日运势，如果不存在则实时生成
     * 
     * @param openid 用户openid
     * @param dto 星座运势请求参数
     * @return 生成的星座运势图片URL
     */
    String generateConstellationFortune(String openid, ConstellationFortuneDTO dto);
}

