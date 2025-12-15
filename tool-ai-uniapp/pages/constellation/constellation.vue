<template>
    <view class="container">
        <view class="header">
            <text class="title">选择你的星座</text>
            <text class="subtitle">查看今日运势</text>
        </view>

        <view class="constellation-grid">
            <view
                v-for="item in constellations"
                :key="item.name"
                class="constellation-item"
                :class="{ generating: generatingConstellation === item.name }"
                @click="selectConstellation(item.name)"
            >
                <view class="constellation-icon">
                    <text class="icon-text">{{ item.icon }}</text>
                </view>
                <text class="constellation-name">{{ item.name }}</text>
                <text class="constellation-date">{{ item.date }}</text>
            </view>
        </view>

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
            generatingConstellation: '',
            constellations: [
                { name: '白羊座', icon: '♈', date: '3.21-4.19' },
                { name: '金牛座', icon: '♉', date: '4.20-5.20' },
                { name: '双子座', icon: '♊', date: '5.21-6.21' },
                { name: '巨蟹座', icon: '♋', date: '6.22-7.22' },
                { name: '狮子座', icon: '♌', date: '7.23-8.22' },
                { name: '处女座', icon: '♍', date: '8.23-9.22' },
                { name: '天秤座', icon: '♎', date: '9.23-10.23' },
                { name: '天蝎座', icon: '♏', date: '10.24-11.22' },
                { name: '射手座', icon: '♐', date: '11.23-12.21' },
                { name: '摩羯座', icon: '♑', date: '12.22-1.19' },
                { name: '水瓶座', icon: '♒', date: '1.20-2.18' },
                { name: '双鱼座', icon: '♓', date: '2.19-3.20' }
            ]
        };
    },
    methods: {
        async selectConstellation(constellation) {
            if (this.generatingConstellation) {
                return; // 正在生成中，不允许重复点击
            }

            this.generatingConstellation = constellation;
            
            try {
                uni.showLoading({
                    title: '生成中...',
                    mask: true
                });

                const res = await api.generateConstellationFortune({
                    constellation: constellation
                });

                uni.hideLoading();
                
                if (res.code === 200) {
                    uni.navigateTo({
                        url: `/pages/result/result?type=5&resultUrl=${encodeURIComponent(res.data.resultUrl)}&title=${encodeURIComponent(constellation + '今日运势')}`
                    });
                } else {
                    // 处理非200的响应（如429限流错误）
                    // 注意：request.js 已经显示了 toast，这里再次显示确保用户能看到
                    uni.showToast({
                        title: res.message || '生成失败，请稍后再试',
                        icon: 'none',
                        duration: 3000
                    });
                }
            } catch (e) {
                uni.hideLoading();
                console.error('生成失败', e);
                // 从错误对象中提取错误信息
                // request.js 已经显示了 toast，但这里再次显示确保用户能看到
                const errorMessage = (e.data && e.data.message) || e.message || '生成失败，请稍后再试';
                uni.showToast({
                    title: errorMessage,
                    icon: 'none',
                    duration: 3000
                });
            } finally {
                this.generatingConstellation = '';
            }
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
    margin-bottom: 50rpx;
}

.title {
    display: block;
    font-size: 40rpx;
    font-weight: bold;
    color: #fff;
    margin-bottom: 15rpx;
}

.subtitle {
    display: block;
    font-size: 26rpx;
    color: rgba(255, 255, 255, 0.8);
}

.constellation-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20rpx;
    margin-bottom: 100rpx;
}

.constellation-item {
    background: #fff;
    border-radius: 20rpx;
    padding: 30rpx 20rpx;
    text-align: center;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
    transition: transform 0.3s;
}

.constellation-item:active {
    transform: scale(0.95);
}

.constellation-item.generating {
    opacity: 0.6;
    pointer-events: none;
}

.constellation-icon {
    width: 80rpx;
    height: 80rpx;
    margin: 0 auto 15rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 50%;
    position: relative;
}

.icon-text {
    font-size: 40rpx;
    color: #fff;
    line-height: 1;
    text-align: center;
    display: inline-block;
    vertical-align: middle;
    margin: 0;
    padding: 0;
}

.constellation-name {
    display: block;
    font-size: 28rpx;
    color: #333;
    font-weight: 500;
    margin-bottom: 8rpx;
}

.constellation-date {
    display: block;
    font-size: 22rpx;
    color: #999;
}
</style>

