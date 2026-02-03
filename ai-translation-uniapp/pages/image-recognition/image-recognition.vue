<template>
    <view class="container">
        <view class="form-card">
            <view class="form-item">
                <text class="label">上传图片</text>
                <view class="upload-area" @click="chooseImage">
                    <image v-if="imageUrl" :src="imageUrl" class="preview-image" mode="aspectFit"></image>
                    <view v-else class="upload-placeholder">
                        <text class="upload-icon">📷</text>
                        <text class="upload-text">点击选择图片</text>
                    </view>
                </view>
            </view>

            <view v-if="imageUrl" class="form-item">
                <text class="label">识别结果</text>
                <view v-if="recognizing" class="recognizing">
                    <text>识别中...</text>
                </view>
                <view v-else-if="recognitionResult" class="recognition-result">
                    <text class="result-text">{{ recognitionResult }}</text>
                </view>
            </view>

            <view v-if="recognitionResult" class="form-item">
                <text class="label">翻译</text>
                <view class="language-selector">
                    <picker mode="selector" :range="targetLanguages" range-key="label" @change="onTargetLangChange">
                        <view class="picker-view">
                            <text>{{ selectedTargetLang.label }}</text>
                            <text class="picker-arrow">></text>
                        </view>
                    </picker>
                </view>
                <view v-if="translating" class="translating">
                    <text>翻译中...</text>
                </view>
                <view v-else-if="translationResult" class="translation-result">
                    <text class="result-text">{{ translationResult }}</text>
                </view>
                <button 
                    v-if="recognitionResult && !translationResult"
                    class="translate-btn"
                    :disabled="translating"
                    @click="translate"
                >
                    {{ translating ? '翻译中...' : '立即翻译' }}
                </button>
            </view>

            <button 
                v-if="imageUrl && !recognitionResult"
                class="recognize-btn" 
                :disabled="recognizing" 
                @click="recognize"
            >
                {{ recognizing ? '识别中...' : '开始识别' }}
            </button>
        </view>
    </view>
</template>

<script>
import api from '@/common/utils/api.js';
import apiConfig from '@/common/config/api-config.js';
import { uploadToOss } from '@/common/utils/oss-upload.js';

export default {
    data() {
        return {
            imageUrl: '',
            imageTempPath: '',
            recognitionResult: '',
            translationResult: '',
            recognizing: false,
            translating: false,
            selectedTargetLang: { label: '英文', value: 'en' },
            targetLanguages: [
                { label: '英文', value: 'en' },
                { label: '日文', value: 'ja' },
                { label: '蒙文', value: 'mo' },
                { label: '中文', value: 'zh' }
            ]
        };
    },
    methods: {
        chooseImage() {
            uni.chooseImage({
                count: 1,
                sizeType: ['compressed'],
                sourceType: ['album', 'camera'],
                success: (res) => {
                    this.imageTempPath = res.tempFilePaths[0];
                    this.imageUrl = this.imageTempPath;
                    this.uploadImage();
                    // 重置结果
                    this.recognitionResult = '';
                    this.translationResult = '';
                },
                fail: (err) => {
                    console.error('选择图片失败', err);
                    uni.showToast({
                        title: '选择图片失败',
                        icon: 'none'
                    });
                }
            });
        },
        
        async uploadImage() {
            if (!this.imageTempPath) {
                return;
            }
            
            uni.showLoading({
                title: '上传中...'
            });
            
            try {
                // 使用OSS直传
                const imageUrl = await uploadToOss(this.imageTempPath);
                this.imageUrl = imageUrl;
                uni.hideLoading();
            } catch (e) {
                console.error('上传图片失败', e);
                uni.hideLoading();
                uni.showToast({
                    title: e.message || '上传图片失败',
                    icon: 'none'
                });
                this.imageUrl = '';
                this.imageTempPath = '';
            }
        },
        
        async recognize() {
            // TODO: 调用AI识图接口（后端还未实现）
            this.recognizing = true;
            try {
                // 模拟识别结果
                await new Promise(resolve => setTimeout(resolve, 2000));
                this.recognitionResult = '这是一张图片，包含文字内容...（AI识图功能后端待开发）';
            } catch (e) {
                console.error('识别失败', e);
                uni.showToast({
                    title: '识别失败，请重试',
                    icon: 'none'
                });
            } finally {
                this.recognizing = false;
            }
        },
        
        onTargetLangChange(e) {
            this.selectedTargetLang = this.targetLanguages[e.detail.value];
            this.translationResult = ''; // 重置翻译结果
        },
        
        async translate() {
            if (!this.recognitionResult) {
                return;
            }
            
            this.translating = true;
            try {
                const res = await api.translate({
                    text: this.recognitionResult,
                    from: 'zh', // 假设识别结果是中文
                    to: this.selectedTargetLang.value
                });
                
                if (res.code === 200) {
                    this.translationResult = res.data.result;
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
}

.form-item {
    margin-bottom: 30rpx;
}

.label {
    display: block;
    font-size: 28rpx;
    color: #333;
    margin-bottom: 20rpx;
    font-weight: 500;
}

.upload-area {
    width: 100%;
    min-height: 300rpx;
    background: #f5f5f5;
    border-radius: 10rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 2rpx dashed #ddd;
}

.preview-image {
    width: 100%;
    max-height: 500rpx;
    border-radius: 10rpx;
}

.upload-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40rpx;
}

.upload-icon {
    font-size: 80rpx;
    margin-bottom: 20rpx;
}

.upload-text {
    font-size: 28rpx;
    color: #999;
}

.recognizing, .translating {
    padding: 40rpx;
    text-align: center;
    color: #999;
}

.recognition-result, .translation-result {
    background: #f5f5f5;
    border-radius: 10rpx;
    padding: 20rpx;
    min-height: 150rpx;
}

.result-text {
    font-size: 28rpx;
    color: #333;
    line-height: 1.6;
}

.language-selector {
    margin-bottom: 20rpx;
}

.picker-view {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20rpx;
    background: #f5f5f5;
    border-radius: 10rpx;
}

.picker-arrow {
    color: #999;
}

.recognize-btn, .translate-btn {
    width: 100%;
    height: 88rpx;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    border-radius: 44rpx;
    font-size: 32rpx;
    border: none;
    margin-top: 20rpx;
}

.recognize-btn[disabled], .translate-btn[disabled] {
    background: #ccc;
}
</style>

