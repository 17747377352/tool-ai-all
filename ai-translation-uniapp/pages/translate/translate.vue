<template>
    <view class="container">
        <view class="form-card">
            <view class="form-item">
                <text class="label">源语言</text>
                <view class="language-options">
                    <view
                        v-for="lang in sourceLanguages"
                        :key="lang.value"
                        class="lang-item"
                        :class="{ active: from === lang.value }"
                        @click="from = lang.value"
                    >
                        <text>{{ lang.label }}</text>
                    </view>
                </view>
            </view>

            <view class="form-item">
                <text class="label">目标语言</text>
                <view class="language-options">
                    <view
                        v-for="lang in targetLanguages"
                        :key="lang.value"
                        class="lang-item"
                        :class="{ active: to === lang.value }"
                        @click="to = lang.value"
                    >
                        <text>{{ lang.label }}</text>
                    </view>
                </view>
            </view>

            <view class="form-item">
                <text class="label">输入文本</text>
                <textarea
                    v-model="text"
                    class="textarea"
                    placeholder="请输入要翻译的文本"
                    maxlength="500"
                ></textarea>
            </view>

            <button 
                class="translate-btn" 
                :disabled="!text || !from || !to || translating" 
                @click="translate"
            >
                {{ translating ? '翻译中...' : '立即翻译' }}
            </button>
        </view>

        <view v-if="result" class="result-card">
            <view class="result-header">
                <text class="result-title">翻译结果</text>
            </view>
            <view class="result-content">
                <text class="result-text">{{ result }}</text>
            </view>
            <view class="result-actions">
                <button class="copy-btn" @click="copyResult">复制结果</button>
            </view>
        </view>
    </view>
</template>

<script>
import api from '@/common/utils/api.js';

export default {
    data() {
        return {
            from: 'zh', // 源语言
            to: 'en', // 目标语言
            text: '',
            result: '',
            translating: false,
            sourceLanguages: [
                { label: '中文', value: 'zh' },
                { label: '英文', value: 'en' },
                { label: '日文', value: 'ja' },
                { label: '蒙文', value: 'mo' }
            ],
            targetLanguages: []
        };
    },
    watch: {
        from(newVal) {
            // 根据源语言更新目标语言选项
            this.updateTargetLanguages(newVal);
            // 如果当前目标语言与源语言相同，自动切换
            if (this.to === newVal) {
                const available = this.targetLanguages.filter(l => l.value !== newVal);
                if (available.length > 0) {
                    this.to = available[0].value;
                }
            }
        }
    },
    mounted() {
        this.updateTargetLanguages(this.from);
    },
    methods: {
        updateTargetLanguages(sourceLang) {
            const allLanguages = [
                { label: '中文', value: 'zh' },
                { label: '英文', value: 'en' },
                { label: '日文', value: 'ja' },
                { label: '蒙文', value: 'mo' }
            ];
            // 过滤掉源语言
            this.targetLanguages = allLanguages.filter(lang => lang.value !== sourceLang);
        },
        async translate() {
            if (!this.text.trim()) {
                uni.showToast({
                    title: '请输入要翻译的文本',
                    icon: 'none'
                });
                return;
            }

            this.translating = true;
            try {
                const res = await api.translate({
                    text: this.text,
                    from: this.from,
                    to: this.to
                });
                
                if (res.code === 200) {
                    this.result = res.data.result;
                } else {
                    uni.showToast({
                        title: res.message || '翻译失败',
                        icon: 'none'
                    });
                }
            } catch (e) {
                console.error('翻译失败', e);
                uni.showToast({
                    title: '翻译失败，请重试',
                    icon: 'none'
                });
            } finally {
                this.translating = false;
            }
        },
        copyResult() {
            if (!this.result) {
                return;
            }
            uni.setClipboardData({
                data: this.result,
                success: () => {
                    uni.showToast({
                        title: '已复制到剪贴板',
                        icon: 'success'
                    });
                }
            });
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

.form-card {
    background: #fff;
    border-radius: 20rpx;
    padding: 30rpx;
    margin-bottom: 30rpx;
}

.form-item {
    margin-bottom: 30rpx;
}

.form-item:last-child {
    margin-bottom: 0;
}

.label {
    display: block;
    font-size: 28rpx;
    color: #333;
    margin-bottom: 20rpx;
    font-weight: 500;
}

.language-options {
    display: flex;
    flex-wrap: wrap;
    gap: 15rpx;
}

.lang-item {
    padding: 15rpx 30rpx;
    background: #f5f5f5;
    border-radius: 30rpx;
    font-size: 26rpx;
    color: #666;
    border: 2rpx solid transparent;
}

.lang-item.active {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    border-color: #667eea;
}

.textarea {
    width: 100%;
    min-height: 200rpx;
    background: #f5f5f5;
    border-radius: 10rpx;
    padding: 20rpx;
    font-size: 28rpx;
    box-sizing: border-box;
}

.translate-btn {
    width: 100%;
    height: 88rpx;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    border-radius: 44rpx;
    font-size: 32rpx;
    border: none;
    margin-top: 20rpx;
}

.translate-btn[disabled] {
    background: #ccc;
}

.result-card {
    background: #fff;
    border-radius: 20rpx;
    padding: 30rpx;
}

.result-header {
    margin-bottom: 20rpx;
}

.result-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
}

.result-content {
    background: #f5f5f5;
    border-radius: 10rpx;
    padding: 20rpx;
    margin-bottom: 20rpx;
    min-height: 150rpx;
}

.result-text {
    font-size: 28rpx;
    color: #333;
    line-height: 1.6;
}

.result-actions {
    display: flex;
    justify-content: flex-end;
}

.copy-btn {
    padding: 15rpx 40rpx;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    border-radius: 30rpx;
    font-size: 26rpx;
    border: none;
}
</style>

