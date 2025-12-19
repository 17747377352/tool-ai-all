<template>
    <view class="container">
        <view class="form-item">
            <text class="label">选择日期（默认今日）</text>
            <picker
                mode="date"
                :value="date"
                @change="onDateChange"
            >
                <view class="picker">
                    <text :class="{ placeholder: !date }">
                        {{ date || '请选择日期' }}
                    </text>
                </view>
            </picker>
        </view>

        <button class="generate-btn" :disabled="loading" @click="generate">
            {{ loading ? '生成中...' : '生成今日运势卡片' }}
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
            date: '',
            loading: false,
            fortune: null
        };
    },
    onLoad() {
        const d = new Date();
        const y = d.getFullYear();
        const m = d.getMonth() + 1;
        const day = d.getDate();
        // 万年历接口支持 yyyy-M-d 或 yyyy-MM-dd，这里统一用 yyyy-M-d
        this.date = `${y}-${m}-${day}`;
    },
    methods: {
        onDateChange(e) {
            this.date = e.detail.value;
        },
        async generate() {
            if (!this.date) {
                uni.showToast({
                    title: '请选择日期',
                    icon: 'none'
                });
                return;
            }

            this.loading = true;
            try {
                const res = await api.generateFortune({
                    date: this.date
                });
                if (res.code === 200) {
                    const url = res.data && res.data.resultUrl;
                    if (url) {
                        uni.navigateTo({
                            url: `/pages/result/result?type=4&resultUrl=${encodeURIComponent(url)}`
                        });
                    } else {
                        uni.showToast({
                            title: '生成失败，请稍后重试',
                            icon: 'none'
                        });
                    }
                } else {
                    uni.showToast({
                        title: res.message || '查询失败',
                        icon: 'none'
                    });
                }
            } catch (e) {
                console.error('查询失败', e);
                uni.showToast({
                    title: '查询失败，请稍后重试',
                    icon: 'none'
                });
            } finally {
                this.loading = false;
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

.input {
    width: 100%;
    height: 80rpx;
    background: #f5f5f5;
    border-radius: 10rpx;
    padding: 0 20rpx;
    font-size: 28rpx;
    box-sizing: border-box;
}

.picker {
    width: 100%;
    height: 80rpx;
    background: #f5f5f5;
    border-radius: 10rpx;
    padding: 0 20rpx;
    display: flex;
    align-items: center;
    box-sizing: border-box;
}

.picker text {
    font-size: 28rpx;
    color: #333;
}

.picker text.placeholder {
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
    margin-top: 40rpx;
}

.generate-btn[disabled] {
    background: #ccc;
}
</style>




