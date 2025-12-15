/**
 * 广告工具
 */
import { ENABLE_AD, AD_CONFIG, AD_TIPS } from '@/common/config/ad-config.js';

// 激励视频广告
export const showRewardedVideo = (adUnitId) => {
    return new Promise((resolve, reject) => {
        // 如果未开启广告，直接resolve
        if (!ENABLE_AD) {
            console.log('广告功能已关闭（开发模式）');
            resolve(true);
            return;
        }

        // #ifdef MP-WEIXIN
        const videoAd = wx.createRewardedVideoAd({
            adUnitId: adUnitId || AD_CONFIG.REWARDED_VIDEO_AD_UNIT_ID
        });

        let startTime = Date.now();
        let isCompleted = false;

        videoAd.onLoad(() => {
            console.log('激励视频加载成功');
        });

        videoAd.onError((err) => {
            console.error('激励视频加载失败', err);
            reject(err);
        });

        videoAd.onClose((res) => {
            const duration = Date.now() - startTime;
            // 5秒内关闭视为无效
            if (res && res.isEnded && duration >= 5000) {
                isCompleted = true;
                resolve(true);
            } else {
                uni.showToast({
                    title: AD_TIPS.REWARDED_VIDEO_NOT_COMPLETED,
                    icon: 'none'
                });
                reject(new Error('广告未完整观看'));
            }
        });

        videoAd.load()
            .then(() => videoAd.show())
            .catch(err => {
                console.error('激励视频播放失败', err);
                reject(err);
            });
        // #endif

        // #ifndef MP-WEIXIN
        // 非微信小程序环境，直接resolve
        resolve(true);
        // #endif
    });
};

// 插屏广告
export const showInterstitialAd = (adUnitId, shouldShow) => {
    // 如果未开启广告，直接resolve
    if (!ENABLE_AD) {
        console.log('广告功能已关闭（开发模式）');
        return Promise.resolve();
    }

    if (!shouldShow) {
        return Promise.resolve();
    }

    return new Promise((resolve) => {
        // #ifdef MP-WEIXIN
        const interstitialAd = wx.createInterstitialAd({
            adUnitId: adUnitId || AD_CONFIG.INTERSTITIAL_AD_UNIT_ID
        });

        interstitialAd.onLoad(() => {
            console.log('插屏广告加载成功');
        });

        interstitialAd.onError((err) => {
            console.error('插屏广告加载失败', err);
            resolve(); // 失败也resolve，不影响主流程
        });

        interstitialAd.onClose(() => {
            resolve();
        });

        interstitialAd.load()
            .then(() => interstitialAd.show())
            .catch(err => {
                console.error('插屏广告播放失败', err);
                resolve();
            });
        // #endif

        // #ifndef MP-WEIXIN
        resolve();
        // #endif
    });
};

// Banner广告组件ID（已迁移到 ad-config.js）
export const BANNER_AD_UNIT_ID = AD_CONFIG.BANNER_AD_UNIT_ID;

