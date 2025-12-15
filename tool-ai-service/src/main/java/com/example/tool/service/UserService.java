package com.example.tool.service;

import com.example.tool.entity.User;

/**
 * 用户服务接口
 * 提供用户信息管理相关功能
 * 
 * @author tool-ai-service
 * @since 1.0
 */
public interface UserService {
    /**
     * 根据openid查询用户
     * 
     * @param openid 微信openid
     * @return 用户实体，如果不存在返回null
     */
    User getByOpenid(String openid);
    
    /**
     * 创建或更新用户信息
     * 如果用户不存在则创建，存在则更新昵称和头像
     * 
     * @param openid 微信openid
     * @param nickname 用户昵称
     * @param avatar 用户头像URL
     * @return 用户实体
     */
    User createOrUpdate(String openid, String nickname, String avatar);
}

