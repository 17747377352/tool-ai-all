<template>
    <view class="container">
        <view class="upload-card" @click="chooseImage">
            <image v-if="imageUrl" :src="imageUrl" class="preview-image" mode="aspectFit"></image>
            <view v-else class="upload-placeholder">
                <text class="upload-icon">💧</text>
                <text class="upload-text">点击上传需要去水印的图片</text>
                <text class="upload-subtext">支持JPG/PNG格式</text>
            </view>
        </view>

        <view class="form-card">
            <view class="tip-text">
                <text>💡 提示：上传图片后，系统将自动识别并去除水印</text>
            </view>
        </view>

        <button 
            class="remove-btn" 
            :disabled="!imageUrl || processing" 
            @click="removeWatermark"
        >
            {{ processing ? '处理中...' : '开始去水印' }}
        </button>
    </view>
</template>

<script>
import api from '@/common/utils/api.js';
import { FUNCTION_TYPE } from '@/common/config/function-type.js';
import { uploadToOss } from '@/common/utils/oss-upload.js';

export default {
    data() {
        return {
            imageUrl: '',
            imageTempPath: '',
            processing: false
        };
    },
    methods: {
        chooseImage() {
            uni.chooseImage({
                count: 1,
                sizeType: ['original', 'compressed'],
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
        
        async removeWatermark() {
            if (!this.imageUrl) {
                uni.showToast({
                    title: '请先上传图片',
                    icon: 'none'
                });
                return;
            }

            this.processing = true;
            try {
                const res = await api.removeWatermark({
                    imageUrl: this.imageUrl
                });
                
                if (res.code === 200) {
                    uni.navigateTo({
                        url: `/pages/result/result?type=${FUNCTION_TYPE.WATERMARK_REMOVAL}&resultUrl=${encodeURIComponent(res.data.resultUrl)}`
                    });
                } else {
                    uni.showToast({
                        title: res.message || '去水印失败',
                        icon: 'none'
                    });
                }
            } catch (e) {
                console.error('去水印失败', e);
                uni.showToast({
                    title: '去水印失败，请重试',
                    icon: 'none'
                });
            } finally {
                this.processing = false;
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

.upload-card {
    background: #fff;
    border-radius: 20rpx;
    padding: 30rpx;
    margin-bottom: 30rpx;
    min-height: 400rpx;
    display: flex;
    align-items: center;
    justify-content: center;
}

.preview-image {
    width: 100%;
    max-height: 600rpx;
    border-radius: 10rpx;
}

.upload-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 60rpx 40rpx;
}

.upload-icon {
    font-size: 100rpx;
    margin-bottom: 30rpx;
}

.upload-text {
    font-size: 32rpx;
    color: #333;
    margin-bottom: 15rpx;
    font-weight: 500;
}

.upload-subtext {
    font-size: 24rpx;
    color: #999;
}

.form-card {
    background: #fff;
    border-radius: 20rpx;
    padding: 30rpx;
    margin-bottom: 30rpx;
}

.tip-text {
    font-size: 26rpx;
    color: #666;
    line-height: 1.6;
}

.remove-btn {
    width: 100%;
    height: 88rpx;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    border-radius: 44rpx;
    font-size: 32rpx;
    border: none;
}

.remove-btn[disabled] {
    background: #ccc;
}
</style>

