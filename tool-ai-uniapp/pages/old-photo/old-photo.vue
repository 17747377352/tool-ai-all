<template>
    <view class="container">
        <!-- 模式选择 -->
        <view class="mode-section">
            <view class="mode-tabs">
                <view 
                    class="mode-tab" 
                    :class="{ active: mode === 'single' }"
                    @click="mode = 'single'"
                >
                    <text>单张修复</text>
                </view>
                <view 
                    class="mode-tab" 
                    :class="{ active: mode === 'batch' }"
                    @click="mode = 'batch'"
                >
                    <text>批量修复</text>
                    <text class="batch-tag">看广告可用</text>
                </view>
            </view>
        </view>

        <!-- 单张模式 -->
        <template v-if="mode === 'single'">
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
        </template>

        <!-- 批量模式 -->
        <template v-else>
            <view class="batch-upload-section">
                <view class="upload-grid">
                    <view 
                        v-for="(item, index) in imageList" 
                        :key="index"
                        class="image-item"
                        @click="removeImage(index)"
                    >
                        <image :src="item.url" class="batch-image" mode="aspectFill"></image>
                        <view class="remove-icon">×</view>
                    </view>
                    <view 
                        v-if="imageList.length < 10"
                        class="upload-item" 
                        @click="chooseBatchImages"
                    >
                        <text class="upload-plus">+</text>
                        <text class="upload-hint">添加照片</text>
                        <text class="upload-count">{{ imageList.length }}/10</text>
                    </view>
                </view>
                
                <view class="batch-tips">
                    <text class="batch-tips-text">批量修复：最多10张，每天第一张免费，看广告后可获得10张额度</text>
                    <view v-if="remainingCount >= 0" class="count-info">
                        <text class="count-text">广告额度：{{ remainingCount }}张</text>
                    </view>
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
                :disabled="imageList.length === 0 || generating"
                @click="batchRestore"
            >
                {{ generating ? '批量修复中...' : `批量修复(${imageList.length}张)` }}
            </button>
        </template>

        <!-- Banner广告 -->
        <ad-video-banner />
    </view>
</template>

<script>
import api from '@/common/utils/api.js';
import apiConfig from '@/common/config/api-config.js';
import { showRewardedVideo } from '@/common/utils/ad.js';
import AdVideoBanner from '@/common/components/ad-video-banner.vue';

export default {
    components: {
        AdVideoBanner
    },
    data() {
        return {
            mode: 'single', // 'single' 单张模式, 'batch' 批量模式
            imageUrl: '', // OSS 地址（单张模式）
            imageTempPath: '', // 本地预览路径（单张模式）
            imageList: [], // 批量模式图片列表，格式: [{ url: '...', ossUrl: '...' }]
            strength: 0.7,
            generating: false,
            remainingCount: 0 // 批量修复广告额度（不包含免费的第一张）
        };
    },
    onLoad() {
        this.checkBatchRestoreStatus();
    },
    methods: {
        /**
         * 检查批量修复剩余次数
         */
        async checkBatchRestoreStatus() {
            try {
                const res = await api.checkBatchRestoreAd();
                if (res.code === 200) {
                    this.remainingCount = res.data.remainingCount || 0;
                }
            } catch (e) {
                console.error('检查批量修复状态失败', e);
            }
        },
        
        /**
         * 观看广告获得批量修复次数
         */
        async watchAdForBatch() {
            try {
                uni.showLoading({ title: '加载广告中...' });
                await showRewardedVideo();
                uni.hideLoading();
                
                // 记录广告观看
                const res = await api.recordAdWatch(2);
                if (res.code === 200) {
                    // 更新剩余额度
                    await this.checkBatchRestoreStatus();
                    uni.showToast({
                        title: `观看成功！获得10张额度`,
                        icon: 'success'
                    });
                } else {
                    uni.showToast({
                        title: '记录失败，请重试',
                        icon: 'none'
                    });
                }
            } catch (e) {
                uni.hideLoading();
                console.error('观看广告失败', e);
                if (e.message && !e.message.includes('广告未完整观看')) {
                    uni.showToast({
                        title: '观看广告失败，请重试',
                        icon: 'none'
                    });
                }
            }
        },
        
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
        
        /**
         * 选择批量图片
         */
        chooseBatchImages() {
            const remaining = 10 - this.imageList.length;
            if (remaining <= 0) {
                uni.showToast({
                    title: '最多只能选择10张图片',
                    icon: 'none'
                });
                return;
            }
            
            uni.chooseImage({
                count: remaining,
                sizeType: ['original', 'compressed'],
                sourceType: ['album', 'camera'],
                success: (res) => {
                    // 添加到列表并上传
                    res.tempFilePaths.forEach(tempPath => {
                        this.imageList.push({
                            url: tempPath,
                            ossUrl: '',
                            uploading: true
                        });
                        this.uploadBatchImage(this.imageList.length - 1, tempPath);
                    });
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
        
        /**
         * 上传批量图片中的一张
         */
        async uploadBatchImage(index, tempPath) {
            try {
                const token = uni.getStorageSync('token');
                if (!token) throw new Error('未登录');

                const uploadRes = await new Promise((resolve, reject) => {
                    uni.uploadFile({
                        url: `${apiConfig.BASE_URL}/tool/upload-image`,
                        filePath: tempPath,
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
                    this.imageList[index].ossUrl = uploadRes.data.imageUrl;
                    this.imageList[index].uploading = false;
                }
            } catch (e) {
                console.error('上传图片失败', e);
                this.imageList[index].uploading = false;
                uni.showToast({
                    title: '图片上传失败',
                    icon: 'none'
                });
            }
        },
        
        /**
         * 移除图片
         */
        removeImage(index) {
            this.imageList.splice(index, 1);
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
        },
        
        /**
         * 批量修复
         */
        async batchRestore() {
            if (this.imageList.length === 0) {
                uni.showToast({ title: '请先选择照片', icon: 'none' });
                return;
            }
            
            // 检查是否有未上传完成的图片
            const uploadingImages = this.imageList.filter(img => img.uploading || !img.ossUrl);
            if (uploadingImages.length > 0) {
                uni.showToast({ title: '请等待图片上传完成', icon: 'none' });
                return;
            }
            
            // 检查剩余额度（第一张免费，所以只需要检查剩余的额度）
            const needCount = Math.max(0, this.imageList.length - 1); // 除了第一张外需要的额度
            if (needCount > 0 && this.remainingCount < needCount) {
                uni.showModal({
                    title: '额度不足',
                    content: `需要${needCount}张额度（第一张免费），当前剩余${this.remainingCount}张。观看广告可获得10张额度`,
                    confirmText: '观看广告',
                    cancelText: '取消',
                    success: (res) => {
                        if (res.confirm) {
                            this.watchAdForBatch();
                        }
                    }
                });
                return;
            }
            
            this.generating = true;
            try {
                // 提取所有OSS URL
                const imageUrls = this.imageList.map(img => img.ossUrl).filter(url => url);
                
                const res = await api.batchRestoreOldPhoto({
                    imageUrls: imageUrls,
                    strength: Number(this.strength.toFixed(2))
                });
                
                if (res.code === 200) {
                    // 更新剩余次数
                    await this.checkBatchRestoreStatus();
                    
                    uni.navigateTo({
                        url: `/pages/result/result?type=6&resultUrl=${encodeURIComponent(res.data.resultUrl)}`
                    });
                } else {
                    uni.showToast({
                        title: res.message || '批量修复失败',
                        icon: 'none'
                    });
                }
            } catch (e) {
                console.error('批量修复失败', e);
                // 如果是次数不足错误，更新状态
                if (e.message && e.message.includes('次数')) {
                    await this.checkBatchRestoreStatus();
                }
                uni.showToast({
                    title: e.message || '批量修复失败，请重试',
                    icon: 'none'
                });
            } finally {
                this.generating = false;
            }
        }
    },
    watch: {
        mode(newVal) {
            // 切换模式时检查状态
            if (newVal === 'batch') {
                this.checkBatchRestoreStatus();
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

/* 模式选择 */
.mode-section {
    margin-bottom: 30rpx;
}

.mode-tabs {
    display: flex;
    gap: 20rpx;
    background: #fff;
    border-radius: 20rpx;
    padding: 10rpx;
}

.mode-tab {
    flex: 1;
    text-align: center;
    padding: 20rpx;
    border-radius: 16rpx;
    position: relative;
    transition: all 0.3s;
}

.mode-tab.active {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
}

.mode-tab:not(.active) {
    color: #666;
}

.batch-tag {
    display: block;
    font-size: 20rpx;
    margin-top: 5rpx;
    opacity: 0.8;
}

.mode-tab.active .batch-tag {
    color: rgba(255, 255, 255, 0.9);
}

/* 批量上传 */
.batch-upload-section {
    margin-bottom: 30rpx;
}

.upload-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 15rpx;
    margin-bottom: 20rpx;
}

.image-item {
    position: relative;
    aspect-ratio: 1;
    border-radius: 12rpx;
    overflow: hidden;
}

.batch-image {
    width: 100%;
    height: 100%;
}

.remove-icon {
    position: absolute;
    top: 5rpx;
    right: 5rpx;
    width: 40rpx;
    height: 40rpx;
    background: rgba(0, 0, 0, 0.6);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 32rpx;
    line-height: 1;
}

.upload-item {
    aspect-ratio: 1;
    background: #f5f5f5;
    border: 2rpx dashed #ddd;
    border-radius: 12rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    position: relative;
}

.upload-plus {
    font-size: 60rpx;
    color: #999;
    line-height: 1;
    margin-bottom: 10rpx;
}

.upload-hint {
    font-size: 24rpx;
    color: #999;
}

.upload-count {
    position: absolute;
    bottom: 10rpx;
    font-size: 22rpx;
    color: #667eea;
}

.batch-tips {
    background: #fff;
    border-radius: 12rpx;
    padding: 20rpx;
}

.batch-tips-text {
    display: block;
    font-size: 24rpx;
    color: #666;
    line-height: 1.6;
    margin-bottom: 10rpx;
}

.count-info {
    margin-top: 10rpx;
    padding-top: 10rpx;
    border-top: 1rpx solid #f0f0f0;
}

.count-text {
    font-size: 26rpx;
    color: #667eea;
    font-weight: 500;
}
</style>

