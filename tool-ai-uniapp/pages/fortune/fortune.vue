<template>
    <view class="container">
        <view class="form-item">
            <text class="label">请输入姓名</text>
            <input
                v-model="name"
                class="input"
                placeholder="例如：张三"
                maxlength="20"
            />
        </view>

        <view class="form-item">
            <text class="label">选择出生日期</text>
            <picker
                mode="date"
                :value="birthDate"
                @change="onDateChange"
            >
                <view class="picker">
                    <text :class="{ placeholder: !birthDate }">
                        {{ birthDate || '请选择出生日期' }}
                    </text>
                </view>
            </picker>
        </view>

        <button class="generate-btn" :disabled="!name || !birthDate || generating" @click="generate">
            {{ generating ? '生成中...' : '立即生成' }}
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
            name: '',
            birthDate: '',
            generating: false
        };
    },
    methods: {
        onDateChange(e) {
            this.birthDate = e.detail.value;
        },
        async generate() {
            if (!this.name.trim()) {
                uni.showToast({
                    title: '请输入姓名',
                    icon: 'none'
                });
                return;
            }
            if (!this.birthDate) {
                uni.showToast({
                    title: '请选择出生日期',
                    icon: 'none'
                });
                return;
            }

            this.generating = true;
            try {
                const res = await api.generateFortune({
                    name: this.name,
                    birthDate: this.birthDate
                });
                if (res.code === 200) {
                    uni.navigateTo({
                        url: `/pages/result/result?type=4&resultUrl=${encodeURIComponent(res.data.resultUrl)}`
                    });
                }
            } catch (e) {
                console.error('生成失败', e);
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




