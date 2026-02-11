package com.example.simvoice.service;

import com.example.simvoice.entity.UserQuota;

/**
 * 用户额度服务接口
 * 提供用户额度管理功能
 * 类型说明：1-去水印 2-生成图片 3-姓氏签名 4-运势测试 5-星座运势 6-老照片修复 7-AI识图+翻译
 * 
 * @author ai-translation-service
 * @since 1.0
 */
public interface UserQuotaService {
    /**
     * 获取或创建用户额度记录
     * 如果不存在则创建，并设置默认额度
     * 
     * @param openid 用户openid
     * @return 用户额度实体
     */
    UserQuota getOrCreateQuota(String openid);
    
    /**
     * 检查并消费额度
     * 如果额度不足则抛出异常
     * 
     * @param openid 用户openid
     * @param type 功能类型：1-去水印 2-生成图片 3-姓氏签名 4-运势测试 5-星座运势 6-老照片修复 7-AI识图+翻译
     * @return 是否成功
     * @throws com.example.simvoice.exception.BusinessException 如果额度不足则抛出异常
     */
    boolean checkAndConsume(String openid, Integer type);
    
    /**
     * 增加额度
     * 
     * @param openid 用户openid
     * @param type 功能类型：1-去水印 2-生成图片 3-姓氏签名 4-运势测试 5-星座运势 6-老照片修复 7-AI识图+翻译
     * @param amount 增加的额度数量
     * @return 增加后的额度
     */
    int addQuota(String openid, Integer type, Integer amount);
    
    /**
     * 获取剩余额度
     * 
     * @param openid 用户openid
     * @param type 功能类型：1-去水印 2-生成图片 3-姓氏签名 4-运势测试 5-星座运势 6-老照片修复 7-AI识图+翻译
     * @return 剩余额度
     */
    int getRemainingQuota(String openid, Integer type);
}

