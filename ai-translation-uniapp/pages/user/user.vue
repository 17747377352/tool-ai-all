<template>
    <view class="container">
        <view class="user-header">
            <image v-if="userInfo.avatar" class="avatar" :src="userInfo.avatar" mode="aspectFill"></image>
            <view v-else class="avatar avatar-placeholder">👤</view>
            <text class="nickname">{{ userInfo.nickname || '未设置昵称' }}</text>
        </view>

        <view class="stats">
            <view class="stat-item">
                <text class="stat-value">{{ totalCount }}</text>
                <text class="stat-label">累计生成</text>
            </view>
            <view class="stat-item">
                <text class="stat-value">{{ todayCount }}</text>
                <text class="stat-label">今日生成</text>
            </view>
        </view>

        <view class="menu-list">
            <view class="menu-item" @click="viewHistory">
                <text class="menu-text">生成历史</text>
                <text class="menu-arrow">></text>
            </view>
            <view class="menu-item" @click="viewFavorites">
                <text class="menu-text">我的收藏</text>
                <text class="menu-arrow">></text>
            </view>
        </view>

        <!-- Banner广告 -->
        <ad-video-banner />
    </view>
</template>

<script>
import AdVideoBanner from '@/common/components/ad-video-banner.vue';

export default {
    components: {
        AdVideoBanner
    },
    data() {
        return {
            userInfo: {
                avatar: '',
                nickname: ''
            },
            totalCount: 0,
            todayCount: 0
        };
    },
    onLoad() {
        this.loadUserInfo();
        this.loadStats();
    },
    methods: {
        loadUserInfo() {
            const openid = uni.getStorageSync('openid');
            const nickname = uni.getStorageSync('nickname');
            const avatar = uni.getStorageSync('avatar');
            this.userInfo = {
                openid,
                nickname,
                avatar
            };
        },
        loadStats() {
            // 这里应该调用后端接口获取统计数据
            // 暂时使用模拟数据
            this.totalCount = 0;
            this.todayCount = 0;
        },
        viewHistory() {
            uni.showToast({
                title: '功能开发中',
                icon: 'none'
            });
        },
        viewFavorites() {
            uni.showToast({
                title: '功能开发中',
                icon: 'none'
            });
        }
    }
};
</script>

<style scoped>
.container {
    min-height: 100vh;
    background: #f5f5f5;
}

.user-header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    padding: 60rpx 30rpx 80rpx;
    text-align: center;
}

.avatar {
    width: 120rpx;
    height: 120rpx;
    border-radius: 60rpx;
    border: 4rpx solid rgba(255, 255, 255, 0.5);
    margin-bottom: 20rpx;
}

.avatar-placeholder {
    width: 120rpx;
    height: 120rpx;
    border-radius: 60rpx;
    border: 4rpx solid rgba(255, 255, 255, 0.5);
    margin-bottom: 20rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, 0.2);
    font-size: 60rpx;
    line-height: 1;
}

.nickname {
    display: block;
    font-size: 32rpx;
    color: #fff;
    font-weight: 500;
}

.stats {
    display: flex;
    background: #fff;
    margin: -40rpx 30rpx 30rpx;
    border-radius: 20rpx;
    padding: 40rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
}

.stat-item {
    flex: 1;
    text-align: center;
}

.stat-value {
    display: block;
    font-size: 48rpx;
    color: #333;
    font-weight: bold;
    margin-bottom: 10rpx;
}

.stat-label {
    display: block;
    font-size: 24rpx;
    color: #999;
}

.menu-list {
    background: #fff;
    margin: 0 30rpx;
    border-radius: 20rpx;
    overflow: hidden;
}

.menu-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 30rpx;
    border-bottom: 1rpx solid #f5f5f5;
}

.menu-item:last-child {
    border-bottom: none;
}

.menu-text {
    font-size: 28rpx;
    color: #333;
}

.menu-arrow {
    font-size: 28rpx;
    color: #999;
}
</style>

