package com.example.tool.service;

/**
 * 广告观看服务接口
 * 提供广告观看记录管理功能
 * 
 * @author tool-ai-service
 * @since 1.0
 */
public interface AdWatchService {
    /**
     * 记录广告观看
     * 
     * @param openid 用户openid
     * @param adType 广告类型：1-去水印 2-老照片批量修复
     * @return 是否成功
     */
    boolean recordAdWatch(String openid, Integer adType);
    
    /**
     * 检查去水印功能的广告观看状态
     * 返回true表示24小时内观看过广告，可以使用功能
     * 
     * @param openid 用户openid
     * @return 是否可以使用（24小时内观看过广告）
     */
    boolean canUseRemoveLogo(String openid);
    
    /**
     * 检查老照片批量修复的广告观看状态
     * 返回剩余可用次数（首次免费返回10，观看广告后返回剩余次数）
     * 
     * @param openid 用户openid
     * @return 剩余可用次数
     */
    int getBatchRestoreRemainingCount(String openid);
    
    /**
     * 消费批量修复次数
     * 
     * @param openid 用户openid
     * @param count 消费数量
     * @return 消费后的剩余次数，-1表示剩余次数不足
     */
    int consumeBatchRestoreCount(String openid, int count);
    
    /**
     * 检查单张修复是否可以使用免费的第一张
     * 每天第一张免费
     * 
     * @param openid 用户openid
     * @return true表示今天还没用过免费的第一张，可以使用
     */
    boolean canUseFirstFreeRestore(String openid);
    
    /**
     * 消费单张修复的首次免费
     * 如果今天还没用过免费的第一张，则标记为已使用并返回true
     * 
     * @param openid 用户openid
     * @return true表示成功使用免费额度，false表示今天已经用过免费额度
     */
    boolean consumeFirstFreeRestore(String openid);
}

