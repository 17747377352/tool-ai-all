package com.example.tool.service;

import com.example.tool.entity.DailyLimit;

/**
 * 每日限流服务接口
 * 提供用户每日使用次数限制功能
 * 类型说明：1-去水印 2-AI头像 3-姓氏签名 4-运势测试 5-星座运势 6-老照片修复
 * 
 * @author tool-ai-service
 * @since 1.0
 */
public interface DailyLimitService {
    /**
     * 获取用户今日限流信息
     * 如果不存在则创建一条新记录
     * 
     * @param openid 用户openid
     * @return 今日限流实体
     */
    DailyLimit getTodayLimit(String openid);
    
    /**
     * 检查并增加使用次数
     * 如果超过限制则抛出异常
     * 
     * @param openid 用户openid
     * @param type 功能类型：1-去水印 2-AI头像 3-姓氏签名 4-运势测试 5-星座运势 6-老照片修复
     * @return 是否成功
     * @throws com.example.tool.exception.BusinessException 如果超过限制则抛出异常
     */
    boolean checkAndIncrement(String openid, Integer type);
}

