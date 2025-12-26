<template>
    <view class="container">
        <!-- 输入分享链接 -->
        <view class="form-item">
            <text class="label">粘贴分享链接</text>
            <textarea
                v-model="shareUrl"
                class="textarea"
                placeholder="粘贴抖音或小红书的分享链接，例如：https://v.douyin.com/xxx/"
                maxlength="500"
                @input="onShareUrlInput"
            ></textarea>
            <view v-if="shareUrl" class="link-tips">
                <text class="link-tip-text">{{ getPlatformName() }}</text>
            </view>
        </view>

        <view class="tips">
            <text class="tips-text">支持抖音、小红书分享链接，自动去除水印</text>
            <view v-if="!canUse" class="ad-tip">
                <text class="ad-tip-text">⚠️ 需观看广告后使用，观看后24小时内有效</text>
            </view>
            <view v-else class="ad-tip success">
                <text class="ad-tip-text">✓ 已观看广告，24小时内有效</text>
            </view>
        </view>

        <button 
            class="generate-btn" 
            :disabled="!shareUrl || generating" 
            @click="generate"
        >
            {{ generating ? '处理中...' : (canUse ? '立即去水印' : '观看广告后使用') }}
        </button>

        <!-- Banner广告 -->
        <ad-video-banner />
    </view>
</template>

<script>
import api from '@/common/utils/api.js';
import { showRewardedVideo } from '@/common/utils/ad.js';
import AdVideoBanner from '@/common/components/ad-video-banner.vue';

export default {
    components: {
        AdVideoBanner
    },
    data() {
        return {
            shareUrl: '', // 分享链接
            generating: false,
            canUse: false, // 是否可以使用（24小时内观看过广告）
            checkingAd: false // 是否正在检查广告状态
        };
    },
    onLoad() {
        this.checkAdStatus();
    },
    methods: {
        /**
         * 检查广告观看状态
         */
        async checkAdStatus() {
            try {
                const res = await api.checkRemoveLogoAd();
                if (res.code === 200) {
                    this.canUse = res.data.canUse || false;
                }
            } catch (e) {
                console.error('检查广告状态失败', e);
            }
        },
        
        /**
         * 观看广告
         */
        async watchAd() {
            try {
                uni.showLoading({ title: '加载广告中...' });
                await showRewardedVideo();
                uni.hideLoading();
                
                // 记录广告观看
                const res = await api.recordAdWatch(1);
                if (res.code === 200) {
                    this.canUse = true;
                    uni.showToast({
                        title: '观看成功！24小时内可使用',
                        icon: 'success'
                    });
                } else {
                    uni.showToast({
                        title: '记录失败，请重试',
                        icon: 'none'
                    });
                }
            } catch (e) {
                uni.hideLoading();
                console.error('观看广告失败', e);
                if (e.message && !e.message.includes('广告未完整观看')) {
                    uni.showToast({
                        title: '观看广告失败，请重试',
                        icon: 'none'
                    });
                }
            }
        },
        
        /**
         * 分享链接输入处理
         */
        onShareUrlInput(e) {
            this.shareUrl = e.detail.value;
        },
        
        /**
         * 获取平台名称
         */
        getPlatformName() {
            if (!this.shareUrl) {
                return '';
            }
            if (this.shareUrl.includes('douyin.com') || this.shareUrl.includes('iesdouyin.com')) {
                return '✓ 已识别：抖音';
            } else if (this.shareUrl.includes('xiaohongshu.com')) {
                return '✓ 已识别：小红书';
            }
            return '';
        },
        
        /**
         * 去水印
         */
        async generate() {
            // 检查是否需要观看广告
            if (!this.canUse) {
                uni.showModal({
                    title: '提示',
                    content: '使用此功能需要观看广告，观看后24小时内有效',
                    confirmText: '观看广告',
                    cancelText: '取消',
                    success: (res) => {
                        if (res.confirm) {
                            this.watchAd();
                        }
                    }
                });
                return;
            }
            
            await this.generateFromShareUrl();
        },
        
        /**
         * 从分享链接去水印
         */
        async generateFromShareUrl() {
            if (!this.shareUrl.trim()) {
                uni.showToast({
                    title: '请输入分享链接',
                    icon: 'none'
                });
                return;
            }

            // 简单验证链接格式
            if (!this.shareUrl.includes('douyin.com') && 
                !this.shareUrl.includes('iesdouyin.com') && 
                !this.shareUrl.includes('xiaohongshu.com')) {
                uni.showToast({
                    title: '仅支持抖音和小红书链接',
                    icon: 'none'
                });
                return;
            }

            this.generating = true;
            try {
                const res = await api.removeLogo({
                    shareUrl: this.shareUrl.trim()
                });
                if (res.code === 200) {
                    uni.navigateTo({
                        url: `/pages/result/result?type=1&resultUrl=${encodeURIComponent(res.data.resultUrl)}`
                    });
                } else {
                    // 如果是广告相关错误，更新状态
                    if (res.message && res.message.includes('观看广告')) {
                        this.canUse = false;
                        await this.checkAdStatus();
                    }
                    uni.showToast({
                        title: res.message || '去水印失败',
                        icon: 'none'
                    });
                }
            } catch (e) {
                console.error('去水印失败', e);
                // 如果是广告相关错误，更新状态
                if (e.message && e.message.includes('观看广告')) {
                    this.canUse = false;
                    await this.checkAdStatus();
                }
                uni.showToast({
                    title: e.message || '去水印失败，请重试',
                    icon: 'none'
                });
            } finally {
                this.generating = false;
            }
        }
    }
};
</script>

<style scoped>
.container {
    min-height: 100vh;
    background: #f5f5f5;
    padding: 30rpx;
}

.tips {
    margin-bottom: 40rpx;
}

.tips-text {
    font-size: 24rpx;
    color: #999;
}

.generate-btn {
    width: 100%;
    height: 88rpx;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    border-radius: 44rpx;
    font-size: 32rpx;
    border: none;
}

.generate-btn[disabled] {
    background: #ccc;
}

.form-item {
    background: #fff;
    border-radius: 20rpx;
    padding: 30rpx;
    margin-bottom: 30rpx;
}

.label {
    display: block;
    font-size: 28rpx;
    color: #333;
    margin-bottom: 20rpx;
    font-weight: 500;
}

.textarea {
    width: 100%;
    min-height: 150rpx;
    background: #f5f5f5;
    border-radius: 10rpx;
    padding: 20rpx;
    font-size: 28rpx;
    box-sizing: border-box;
}

.link-tips {
    margin-top: 15rpx;
}

.link-tip-text {
    font-size: 24rpx;
    color: #667eea;
}

.ad-tip {
    margin-top: 15rpx;
    padding: 15rpx;
    background: #fff3cd;
    border-radius: 10rpx;
    border-left: 4rpx solid #ffc107;
}

.ad-tip.success {
    background: #d4edda;
    border-left-color: #28a745;
}

.ad-tip-text {
    font-size: 24rpx;
    color: #856404;
}

.ad-tip.success .ad-tip-text {
    color: #155724;
}
</style>

