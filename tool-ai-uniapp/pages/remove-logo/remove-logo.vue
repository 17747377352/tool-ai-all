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
        </view>

        <button 
            class="generate-btn" 
            :disabled="!shareUrl || generating" 
            @click="generate"
        >
            {{ generating ? '处理中...' : '立即去水印' }}
        </button>

        <!-- Banner广告 -->
        <ad-video-banner />
    </view>
</template>

<script>
import api from '@/common/utils/api.js';
import AdVideoBanner from '@/common/components/ad-video-banner.vue';

export default {
    components: {
        AdVideoBanner
    },
    data() {
        return {
            shareUrl: '', // 分享链接
            generating: false
        };
    },
    methods: {
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
                    uni.showToast({
                        title: res.message || '去水印失败',
                        icon: 'none'
                    });
                }
            } catch (e) {
                console.error('去水印失败', e);
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
</style>

