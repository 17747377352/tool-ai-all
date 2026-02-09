<template>
    <view class="container">
        <view class="form-card">
            <view class="form-item">
                <text class="label">上传参考图片</text>
                <view class="upload-area" @click="chooseImage">
                    <image v-if="imageUrl" :src="imageUrl" class="preview-image" mode="aspectFit"></image>
                    <view v-else class="upload-placeholder">
                        <text class="upload-icon">📷</text>
                        <text class="upload-text">点击选择图片</text>
                    </view>
                </view>
            </view>

            <view class="form-item">
                <text class="label">生成提示词</text>
                <textarea
                    v-model="prompt"
                    class="textarea"
                    placeholder="例如：将这个头像转换为卡通风格"
                    maxlength="200"
                ></textarea>
            </view>

            <view class="form-item">
                <text class="label">选择风格</text>
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

            <button 
                class="generate-btn" 
                :disabled="!prompt || !imageUrl || generating" 
                @click="generate"
            >
                {{ generating ? '生成中...' : '立即生成' }}
            </button>
        </view>
    </view>
</template>

<script>
import api from '@/common/utils/api.js';
import apiConfig from '@/common/config/api-config.js';
import { FUNCTION_TYPE } from '@/common/config/function-type.js';
import { uploadToOss } from '@/common/utils/oss-upload.js';

export default {
    data() {
        return {
            imageUrl: '',
            imageTempPath: '',
            prompt: '',
            selectedStyle: 'realistic',
            styles: [
                { label: '写实', value: 'realistic' },
                { label: '卡通', value: 'cartoon' },
                { label: '动漫', value: 'anime' },
                { label: '油画', value: 'oil-painting' }
            ],
            generating: false
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
        
        async generate() {
            if (!this.prompt.trim()) {
                uni.showToast({
                    title: '请输入提示词',
                    icon: 'none'
                });
                return;
            }
            
            if (!this.imageUrl) {
                uni.showToast({
                    title: '请先上传图片',
                    icon: 'none'
                });
                return;
            }

            this.generating = true;
            try {
                const res = await api.generateAiAvatar({
                    prompt: this.prompt,
                    imageUrl: this.imageUrl,
                    style: this.selectedStyle
                });
                
                if (res.code === 200) {
                    uni.navigateTo({
                        url: `/pages/result/result?type=${FUNCTION_TYPE.IMAGE_GENERATE}&resultUrl=${encodeURIComponent(res.data.resultUrl)}`
                    });
                } else {
                    uni.showToast({
                        title: res.message || '生成失败',
                        icon: 'none'
                    });
                }
            } catch (e) {
                console.error('生成失败', e);
                uni.showToast({
                    title: '生成失败，请重试',
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

.textarea {
    width: 100%;
    min-height: 200rpx;
    background: #f5f5f5;
    border-radius: 10rpx;
    padding: 20rpx;
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
    margin-top: 20rpx;
}

.generate-btn[disabled] {
    background: #ccc;
}
</style>

