<template>
    <view class="container">
        <view class="header">
            <text class="title">意见反馈</text>
            <text class="subtitle">您的建议对我们很重要</text>
        </view>

        <view class="form-card">
            <view class="form-item">
                <text class="label">反馈类型</text>
                <view class="type-options">
                    <view 
                        class="type-option" 
                        :class="{ active: feedbackType === 1 }"
                        @click="feedbackType = 1"
                    >
                        <text class="type-icon">💡</text>
                        <text class="type-text">功能建议</text>
                    </view>
                    <view 
                        class="type-option" 
                        :class="{ active: feedbackType === 2 }"
                        @click="feedbackType = 2"
                    >
                        <text class="type-icon">🐛</text>
                        <text class="type-text">问题反馈</text>
                    </view>
                    <view 
                        class="type-option" 
                        :class="{ active: feedbackType === 3 }"
                        @click="feedbackType = 3"
                    >
                        <text class="type-icon">💬</text>
                        <text class="type-text">其他</text>
                    </view>
                </view>
            </view>

            <view class="form-item">
                <text class="label">反馈内容 <text class="required">*</text></text>
                <textarea
                    class="textarea"
                    v-model="content"
                    placeholder="请详细描述您的反馈内容..."
                    :maxlength="1000"
                    :show-confirm-bar="false"
                ></textarea>
                <view class="char-count">{{ content.length }}/1000</view>
            </view>

            <view class="form-item">
                <text class="label">联系方式（可选）</text>
                <input
                    class="input"
                    v-model="contact"
                    placeholder="微信号、QQ号或其他联系方式"
                    :maxlength="100"
                />
            </view>
        </view>

        <button
            class="submit-btn"
            :disabled="!canSubmit || submitting"
            @click="submitFeedback"
        >
            {{ submitting ? '提交中...' : '提交反馈' }}
        </button>
    </view>
</template>

<script>
import api from '@/common/utils/api.js';

export default {
    data() {
        return {
            feedbackType: 1, // 1-功能建议 2-问题反馈 3-其他
            content: '',
            contact: '',
            submitting: false
        };
    },
    computed: {
        canSubmit() {
            return this.feedbackType > 0 && this.content.trim().length > 0 && !this.submitting;
        }
    },
    methods: {
        async submitFeedback() {
            if (!this.canSubmit) {
                uni.showToast({
                    title: '请填写反馈内容',
                    icon: 'none'
                });
                return;
            }

            this.submitting = true;
            try {
                const res = await api.submitFeedback({
                    feedbackType: this.feedbackType,
                    content: this.content.trim(),
                    contact: this.contact.trim() || null
                });

                if (res.code === 200) {
                    uni.showToast({
                        title: '反馈提交成功',
                        icon: 'success'
                    });
                    
                    // 延迟返回，让用户看到成功提示
                    setTimeout(() => {
                        uni.navigateBack();
                    }, 1500);
                } else {
                    uni.showToast({
                        title: res.message || '提交失败，请重试',
                        icon: 'none'
                    });
                }
            } catch (e) {
                console.error('提交反馈失败', e);
                uni.showToast({
                    title: '提交失败，请检查网络',
                    icon: 'none'
                });
            } finally {
                this.submitting = false;
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
    margin-bottom: 40rpx;
}

.title {
    display: block;
    font-size: 48rpx;
    font-weight: bold;
    color: #fff;
    margin-bottom: 16rpx;
}

.subtitle {
    display: block;
    font-size: 28rpx;
    color: rgba(255, 255, 255, 0.8);
}

.form-card {
    background: #fff;
    border-radius: 20rpx;
    padding: 40rpx 30rpx;
    margin-bottom: 40rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
}

.form-item {
    margin-bottom: 40rpx;
}

.form-item:last-child {
    margin-bottom: 0;
}

.label {
    display: block;
    font-size: 28rpx;
    color: #333;
    font-weight: 500;
    margin-bottom: 20rpx;
}

.required {
    color: #ff4757;
}

.type-options {
    display: flex;
    gap: 20rpx;
}

.type-option {
    flex: 1;
    background: #f5f5f5;
    border-radius: 16rpx;
    padding: 30rpx 20rpx;
    text-align: center;
    border: 2rpx solid transparent;
    transition: all 0.3s;
}

.type-option.active {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-color: #667eea;
}

.type-option.active .type-text {
    color: #fff;
}

.type-icon {
    display: block;
    font-size: 48rpx;
    margin-bottom: 12rpx;
}

.type-text {
    display: block;
    font-size: 26rpx;
    color: #666;
    font-weight: 500;
}

.textarea {
    width: 100%;
    min-height: 240rpx;
    background: #f5f5f5;
    border-radius: 12rpx;
    padding: 24rpx;
    font-size: 28rpx;
    color: #333;
    box-sizing: border-box;
}

.char-count {
    text-align: right;
    font-size: 24rpx;
    color: #999;
    margin-top: 12rpx;
}

.input {
    width: 100%;
    height: 88rpx;
    background: #f5f5f5;
    border-radius: 12rpx;
    padding: 0 24rpx;
    font-size: 28rpx;
    color: #333;
    box-sizing: border-box;
}

.submit-btn {
    width: 100%;
    height: 88rpx;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    border-radius: 44rpx;
    font-size: 32rpx;
    font-weight: 500;
    border: none;
    box-shadow: 0 4rpx 20rpx rgba(102, 126, 234, 0.3);
}

.submit-btn[disabled] {
    background: #ccc;
    box-shadow: none;
}
</style>

