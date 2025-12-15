<template>
    <view class="container">
        <view class="form-item">
            <text class="label">请输入姓氏</text>
            <input
                v-model="surname"
                class="input"
                placeholder="例如：张、李、王"
                maxlength="10"
            />
        </view>

        <view class="form-item">
            <text class="label">选择签名风格</text>
            <view class="style-options">
                <view
                    v-for="style in styles"
                    :key="style.value"
                    class="style-item"
                    :class="{ active: selectedStyle === style.value }"
                    @click="selectedStyle = style.value"
                >
                    <text>{{ style.label }}</text>
                </view>
            </view>
        </view>

        <button class="generate-btn" :disabled="!surname || generating" @click="generate">
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
            surname: '',
            selectedStyle: 'classic',
            styles: [
                { label: '经典', value: 'classic' },
                { label: '行书', value: 'cursive' },
                { label: '草书', value: 'grass' },
                { label: '艺术', value: 'artistic' }
            ],
            generating: false
        };
    },
    methods: {
        async generate() {
            if (!this.surname.trim()) {
                uni.showToast({
                    title: '请输入姓氏',
                    icon: 'none'
                });
                return;
            }

            this.generating = true;
            try {
                const res = await api.generateNameSign({
                    surname: this.surname,
                    style: this.selectedStyle
                });
                if (res.code === 200) {
                    uni.navigateTo({
                        url: `/pages/result/result?type=3&resultUrl=${encodeURIComponent(res.data.resultUrl)}`
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

.style-options {
    display: flex;
    flex-wrap: wrap;
    gap: 20rpx;
}

.style-item {
    padding: 15rpx 30rpx;
    background: #f5f5f5;
    border-radius: 30rpx;
    font-size: 26rpx;
    color: #666;
    border: 2rpx solid transparent;
}

.style-item.active {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    border-color: #667eea;
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




