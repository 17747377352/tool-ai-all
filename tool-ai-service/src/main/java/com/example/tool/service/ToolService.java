package com.example.tool.service;

import com.example.tool.dto.*;
import com.example.tool.vo.ConstellationFortuneVO;

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
     * 今日运势生成
     * 使用万年历接口获取当天黄历信息，并调用火山引擎生成运势图片
     *
     * @param openid 用户openid
     * @param dto    今日运势请求参数，包含日期
     * @return 生成的运势图片URL（IMAGE_LIST 单图格式）
     */
    String generateFortune(String openid, FortuneDTO dto);
    
    /**
     * 星座运势生成
     * 优先从数据库查询今日运势，如果不存在则实时生成
     * 
     * @param openid 用户openid
     * @param dto 星座运势请求参数
     * @return 星座运势数据VO对象
     */
    ConstellationFortuneVO generateConstellationFortune(String openid, ConstellationFortuneDTO dto);

    /**
     * 老照片修复（GFPGAN）- 单张
     *
     * @param openid 用户openid
     * @param dto 修复请求参数
     * @return 修复后图片URL（IMAGE_LIST 单图格式）
     */
    String restoreOldPhoto(String openid, OldPhotoRestoreDTO dto);
    
    /**
     * 老照片批量修复（GFPGAN）
     * 需要观看广告获得次数，每天首次免费10张
     *
     * @param openid 用户openid
     * @param dto 批量修复请求参数，包含图片URL数组
     * @return 修复后的图片URL列表（IMAGE_LIST格式）
     */
    String batchRestoreOldPhoto(String openid, BatchRestoreOldPhotoDTO dto);
}

