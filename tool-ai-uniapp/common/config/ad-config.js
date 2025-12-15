/**
 * 广告配置
 * 开发阶段可以关闭广告，方便调试
 */

// 是否开启广告功能
// true: 开启广告（生产环境）
// false: 关闭广告（开发环境）
export const ENABLE_AD = false;

// 广告位ID配置
export const AD_CONFIG = {
    // 激励视频广告位ID
    REWARDED_VIDEO_AD_UNIT_ID: 'adunit-xxx',
    
    // 插屏广告位ID
    INTERSTITIAL_AD_UNIT_ID: 'adunit-xxx',
    
    // Banner广告位ID
    BANNER_AD_UNIT_ID: 'adunit-xxx'
};

// 广告相关提示信息
export const AD_TIPS = {
    // 激励视频未看完的提示
    REWARDED_VIDEO_NOT_COMPLETED: '看完广告才能保存',
    
    // 激励视频加载失败的提示
    REWARDED_VIDEO_LOAD_FAILED: '广告加载失败，请重试'
};

