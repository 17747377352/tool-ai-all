<template>
    <view class="container">
        <view class="upload-card" @click="chooseImage">
            <image v-if="imageUrl" :src="imageUrl" class="preview-image" mode="aspectFit"></image>
            <view v-else class="upload-placeholder">
                <text class="upload-icon">📸</text>
                <text class="upload-text">点击上传老照片</text>
                <text class="upload-subtext">支持JPG/PNG，建议人脸清晰无遮挡</text>
            </view>
        </view>

        <view class="form-card">
            <view class="form-row">
                <text class="label">修复强度</text>
                <text class="value-text">{{ (strength * 100).toFixed(0) }}%</text>
            </view>
            <slider
                :value="strength * 100"
                min="50"
                max="100"
                step="5"
                activeColor="#667eea"
                backgroundColor="#e0e0e0"
                block-size="18"
                @change="onStrengthChange"
            />
            <view class="tips">
                <text class="tips-text">默认70%，更高会更锐利但可能带来轻微失真</text>
            </view>
        </view>

        <button
            class="generate-btn"
            :disabled="!imageUrl || generating"
            @click="restore"
        >
            {{ generating ? '修复中...' : '开始修复' }}
        </button>

        <!-- Banner广告 -->
        <ad-video-banner />
    </view>
</template>

<script>
import api from '@/common/utils/api.js';
import apiConfig from '@/common/config/api-config.js';
import AdVideoBanner from '@/common/components/ad-video-banner.vue';

export default {
    components: {
        AdVideoBanner
    },
    data() {
        return {
            imageUrl: '', // OSS 地址
            imageTempPath: '', // 本地预览
            strength: 0.7,
            generating: false
        };
    },
    methods: {
        onStrengthChange(e) {
            this.strength = (e.detail.value || 70) / 100;
        },
        chooseImage() {
            uni.chooseImage({
                count: 1,
                sizeType: ['original', 'compressed'],
                sourceType: ['album', 'camera'],
                success: (res) => {
                    this.imageTempPath = res.tempFilePaths[0];
                    this.imageUrl = this.imageTempPath; // 先本地预览
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
            if (!this.imageTempPath) return;
            uni.showLoading({ title: '上传中...' });
            try {
                const token = uni.getStorageSync('token');
                if (!token) throw new Error('未登录');

                const uploadRes = await new Promise((resolve, reject) => {
                    uni.uploadFile({
                        url: `${apiConfig.BASE_URL}/tool/upload-image`,
                        filePath: this.imageTempPath,
                        name: 'file',
                        header: { 'Authorization': 'Bearer ' + token },
                        success: (res) => {
                            try {
                                const data = JSON.parse(res.data);
                                if (data.code === 200) {
                                    resolve(data);
                                } else {
                                    reject(new Error(data.message || '上传失败'));
                                }
                            } catch (e) {
                                reject(new Error('解析响应失败'));
                            }
                        },
                        fail: reject
                    });
                });

                if (uploadRes.data && uploadRes.data.imageUrl) {
                    this.imageUrl = uploadRes.data.imageUrl;
                }
                uni.hideLoading();
            } catch (e) {
                console.error('上传图片失败', e);
                uni.hideLoading();
                uni.showToast({
                    title: e.message || '上传失败',
                    icon: 'none'
                });
                this.imageUrl = '';
                this.imageTempPath = '';
            }
        },
        async restore() {
            if (!this.imageUrl) {
                uni.showToast({ title: '请先上传照片', icon: 'none' });
                return;
            }
            this.generating = true;
            try {
                const res = await api.restoreOldPhoto({
                    imageUrl: this.imageUrl,
                    strength: Number(this.strength.toFixed(2))
                });
                if (res.code === 200) {
                    uni.navigateTo({
                        url: `/pages/result/result?type=6&resultUrl=${encodeURIComponent(res.data.resultUrl)}`
                    });
                } else {
                    uni.showToast({
                        title: res.message || '修复失败',
                        icon: 'none'
                    });
                }
            } catch (e) {
                console.error('修复失败', e);
                uni.showToast({
                    title: e.message || '修复失败，请重试',
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

.upload-card {
    background: #fff;
    border-radius: 20rpx;
    padding: 40rpx 30rpx;
    margin-bottom: 30rpx;
    border: 2rpx dashed #ddd;
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 360rpx;
}

.upload-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12rpx;
    color: #777;
}

.upload-icon {
    font-size: 80rpx;
}

.upload-text {
    font-size: 32rpx;
    color: #333;
    font-weight: 500;
}

.upload-subtext {
    font-size: 24rpx;
    color: #999;
}

.preview-image {
    width: 100%;
    max-height: 500rpx;
    border-radius: 12rpx;
}

.form-card {
    background: #fff;
    border-radius: 20rpx;
    padding: 30rpx;
    margin-bottom: 30rpx;
}

.form-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
}

.label {
    font-size: 28rpx;
    color: #333;
    font-weight: 500;
}

.value-text {
    font-size: 28rpx;
    color: #667eea;
}

.tips {
    margin-top: 10rpx;
}

.tips-text {
    font-size: 24rpx;
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
    margin-top: 20rpx;
}

.generate-btn[disabled] {
    background: #ccc;
}
</style>

