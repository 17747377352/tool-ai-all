/**
 * 额度配置
 * 统一管理用户额度相关的配置，方便后期调整
 */

// 新用户默认初始化额度
export const DEFAULT_INIT_QUOTA = 3;

// 分享增加额度（每次分享获得的额度）
export const SHARE_REWARD_QUOTA = 3;

// 广告观看奖励额度（每次观看广告获得的额度）
export const AD_REWARD_QUOTA = 3;

export default {
    DEFAULT_INIT_QUOTA,
    SHARE_REWARD_QUOTA,
    AD_REWARD_QUOTA
};

