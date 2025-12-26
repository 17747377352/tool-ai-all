<template>
    <view class="container">
        <view class="header">
            <text class="title">AI工具箱</text>
            <text class="subtitle">免费工具，AI助力</text>
        </view>

        <view class="tool-grid">
            <view class="tool-item" @click="navigateToTool('remove-logo')">
                <view class="tool-icon-placeholder">💧</view>
                <text class="tool-name">去水印</text>
            </view>
            <view class="tool-item" @click="navigateToTool('ai-avatar')">
                <view class="tool-icon-placeholder">🎨</view>
                <text class="tool-name">AI头像</text>
            </view>
            <view class="tool-item" v-if="false" @click="navigateToTool('name-sign')">
                <view class="tool-icon-placeholder">✍️</view>
                <text class="tool-name">姓氏签名</text>
            </view>
            <view class="tool-item" @click="navigateToTool('fortune')">
                <view class="tool-icon-placeholder">🔮</view>
                <text class="tool-name">运势测试</text>
            </view>
            <view class="tool-item" @click="navigateToTool('constellation')">
                <view class="tool-icon-placeholder">⭐</view>
                <text class="tool-name">星座运势</text>
            </view>
            <view class="tool-item" @click="navigateToTool('old-photo')">
                <view class="tool-icon-placeholder">📸</view>
                <text class="tool-name">老照片修复</text>
            </view>
            <view class="tool-item" @click="navigateToTool('life')">
                <view class="tool-icon-placeholder">🧭</view>
                <text class="tool-name">生活查询</text>
            </view>
        </view>

        <!-- Banner广告 -->
        <ad-video-banner />

        <!-- 反馈按钮（右下角浮动） -->
        <view class="feedback-btn" @click="navigateToFeedback">
            <text class="feedback-icon">💬</text>
            <text class="feedback-text">反馈</text>
        </view>
    </view>
</template>

<script>
import { checkUserAuth, getUserInfoAndDecrypt } from '@/common/utils/auth.js';
import AdVideoBanner from '@/common/components/ad-video-banner.vue';

export default {
    components: {
        AdVideoBanner
    },
    onLoad() {
        // 登录已在App.vue中处理，这里不需要重复登录
        // this.initLogin();
    },
    methods: {
        // 登录逻辑已移至App.vue，这里不再需要
        // async initLogin() {
        //     ...
        // },
        getWxCode() {
            return new Promise((resolve, reject) => {
                uni.login({
                    provider: 'weixin',
                    success: (res) => {
                        resolve(res.code);
                    },
                    fail: reject
                });
            });
        },
        async navigateToTool(toolName) {
            try {
                // 检查用户授权
                await checkUserAuth();
            } catch (e) {
                // 需要授权，显示授权按钮
                uni.showModal({
                    title: '需要授权',
                    content: '需要获取您的用户信息',
                    showCancel: false,
                    success: async (modalRes) => {
                        if (modalRes.confirm) {
                            try {
                                await getUserInfoAndDecrypt();
                                this.goToTool(toolName);
                            } catch (err) {
                                uni.showToast({
                                    title: '授权失败',
                                    icon: 'none'
                                });
                            }
                        }
                    }
                });
                return;
            }
            this.goToTool(toolName);
        },
        goToTool(toolName) {
            const pages = {
                'remove-logo': '/pages/remove-logo/remove-logo',
                'ai-avatar': '/pages/ai-avatar/ai-avatar',
                'name-sign': '/pages/name-sign/name-sign',
                'fortune': '/pages/fortune/fortune',
                'constellation': '/pages/constellation/constellation',
                'old-photo': '/pages/old-photo/old-photo',
                'life': '/pages/life/life'
            };
            uni.navigateTo({
                url: pages[toolName]
            });
        },
        navigateToFeedback() {
            uni.navigateTo({
                url: '/pages/feedback/feedback'
            });
        }
    }
};
</script>

<style scoped>
.container {
    min-height: 100vh;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    padding: 40rpx 30rpx;
}

.header {
    text-align: center;
    margin-bottom: 60rpx;
}

.title {
    display: block;
    font-size: 48rpx;
    font-weight: bold;
    color: #fff;
    margin-bottom: 20rpx;
}

.subtitle {
    display: block;
    font-size: 28rpx;
    color: rgba(255, 255, 255, 0.8);
}

.tool-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20rpx;
    margin-bottom: 100rpx;
}

.tool-item {
    background: #fff;
    border-radius: 20rpx;
    padding: 40rpx 20rpx;
    text-align: center;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
    transition: transform 0.3s;
}

.tool-item:active {
    transform: scale(0.95);
}

.tool-icon-placeholder {
    width: 100rpx;
    height: 100rpx;
    margin: 0 auto 20rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 60rpx;
    line-height: 1;
}

.tool-name {
    display: block;
    font-size: 32rpx;
    color: #333;
    font-weight: 500;
}

/* 反馈按钮 */
.feedback-btn {
    position: fixed;
    right: 30rpx;
    bottom: 120rpx;
    width: 120rpx;
    height: 120rpx;
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(255, 255, 255, 0.9) 100%);
    border-radius: 60rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.15);
    z-index: 999;
    transition: all 0.3s;
    backdrop-filter: blur(10rpx);
}

.feedback-btn:active {
    transform: scale(0.9);
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.2);
}

.feedback-icon {
    font-size: 48rpx;
    line-height: 1;
    margin-bottom: 8rpx;
}

.feedback-text {
    font-size: 22rpx;
    color: #667eea;
    font-weight: 500;
}
</style>

