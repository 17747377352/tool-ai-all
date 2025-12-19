<template>
    <view class="container">
        <view class="result-header">
            <text class="result-title">@{{ nickname }}专属{{ typeName }}</text>
        </view>

        <!-- 根据类型显示图片、视频或黄历卡片 -->
        <view class="result-container">
            <!-- 今日黄历卡片 (Type 4) -->
            <view v-if="isFortuneCard" class="fortune-card">
                <view class="fortune-inner">
                    <view class="fortune-header-box">
                        <view class="header-decoration left">☯</view>
                        <text class="fortune-header-title">今日黄历</text>
                        <view class="header-decoration right">☯</view>
                    </view>
                    
                    <view class="fortune-main-table">
                        <!-- 第一行：日期 -->
                        <view class="fortune-row">
                            <view class="fortune-label">公历</view>
                            <view class="fortune-value">{{ fortuneData.date }} {{ fortuneData.weekday }}</view>
                            <view class="fortune-label">农历</view>
                            <view class="fortune-value">{{ fortuneData.lunar }}</view>
                        </view>
                        
                        <!-- 第二行：干支/吉日 -->
                        <view class="fortune-row">
                            <view class="fortune-label">干支</view>
                            <view class="fortune-value">{{ fortuneData.lunarYear }}</view>
                            <view class="fortune-label">黄道吉日</view>
                            <view class="fortune-value">{{ fortuneData.desc || '吉' }}</view>
                        </view>
                        
                        <!-- 宜 -->
                        <view class="fortune-info-row">
                            <view class="fortune-label item-label">
                                <view class="badge yi">宜</view>
                            </view>
                            <view class="fortune-info-content yi-text">{{ fortuneData.suit || '诸事皆宜' }}</view>
                        </view>
                        
                        <!-- 忌 -->
                        <view class="fortune-info-row">
                            <view class="fortune-label item-label">
                                <view class="badge ji">忌</view>
                            </view>
                            <view class="fortune-info-content ji-text">{{ fortuneData.avoid || '诸事不忌' }}</view>
                        </view>
                        
                        <!-- 冲/节日 (如果有) -->
                        <view v-if="fortuneData.holiday || fortuneData.animalsYear" class="fortune-info-row">
                            <view class="fortune-label item-label">
                                <view class="badge chong">岁</view>
                            </view>
                            <view class="fortune-info-content chong-text">
                                {{ fortuneData.animalsYear }}年 {{ fortuneData.holiday ? ' | ' + fortuneData.holiday : '' }}
                            </view>
                        </view>
                    </view>
                </view>
            </view>

            <!-- 视频类型 -->
            <view v-else-if="isVideo" class="result-video-container">
                <video 
                    :src="resultUrl" 
                    class="result-video" 
                    controls
                    :show-center-play-btn="true"
                    :enable-play-gesture="true"
                    :show-fullscreen-btn="true"
                    @play="onVideoPlay"
                    @error="onVideoError"
                ></video>
            </view>
            
            <!-- 图片类型 -->
            <view v-else class="result-image-container">
                <!-- 图片轮播（IMAGE_LIST格式，包括单图和多图） -->
                <swiper v-if="imageList.length > 0" class="image-swiper" :current="currentImageIndex" @change="onSwiperChange" :indicator-dots="true" :autoplay="false">
                    <swiper-item v-for="(imageUrl, index) in imageList" :key="index">
                        <image 
                            :src="imageUrl" 
                            class="result-image" 
                            mode="aspectFit" 
                            @load="onImageLoad"
                            @error="onImageError"
                            :lazy-load="false"
                        ></image>
                    </swiper-item>
                </swiper>
                <!-- 单图显示（非IMAGE_LIST格式，如星座运势） -->
                <image 
                    v-else 
                    :src="resultUrl" 
                    class="result-image" 
                    mode="aspectFit" 
                    @load="onImageLoad"
                    @error="onImageError"
                    :lazy-load="false"
                    :show-menu-by-longpress="true"
                ></image>
                <!-- 图片指示器（多图时显示） -->
                <view v-if="imageList.length > 1" class="image-indicator">
                    <text class="indicator-text">{{ currentImageIndex + 1 }} / {{ imageList.length }}</text>
                </view>
            </view>
        </view>

        <view v-if="!isFortuneCard" class="action-buttons">
            <button class="save-btn" @click="saveToAlbum">
                {{ isVideo ? '保存视频到相册' : (imageList.length > 1 ? `保存全部${imageList.length}张图片` : '保存到相册') }}
            </button>
        </view>

        <!-- Banner广告 -->
        <ad-video-banner />
    </view>
</template>

<script>
import { showRewardedVideo, showInterstitialAd } from '@/common/utils/ad.js';
import AdVideoBanner from '@/common/components/ad-video-banner.vue';

export default {
    components: {
        AdVideoBanner
    },
    data() {
        return {
            resultUrl: '',
            type: '',
            typeName: '',
            nickname: '用户',
            isVideo: false,
            isFortuneCard: false, // 是否显示黄历卡片
            fortuneData: {},      // 黄历数据
            imageList: [], // 多图列表
            currentImageIndex: 0 // 当前显示的图片索引
        };
    },
    onLoad(options) {
        console.log('=== result页面加载 ===');
        console.log('原始options:', options);
        console.log('原始resultUrl:', options.resultUrl);
        
        let url = decodeURIComponent(options.resultUrl || '');
        console.log('解码后的URL:', url);
        
        this.type = options.type || '';
        this.typeName = this.getTypeName(this.type);
        this.nickname = uni.getStorageSync('nickname') || '用户';
        
        console.log('result页面加载，type:', this.type, 'url:', url);
        
        // 检查是否是多图内容（格式：IMAGE_LIST:["url1","url2",...]）
        if (url.startsWith('IMAGE_LIST:')) {
            try {
                const imageListJson = url.substring('IMAGE_LIST:'.length);
                this.imageList = JSON.parse(imageListJson);
                if (this.imageList && this.imageList.length > 0) {
                    this.resultUrl = this.imageList[0]; // 默认显示第一张
                    this.currentImageIndex = 0;
                    console.log('检测到多图内容，共', this.imageList.length, '张图片');
                }
            } catch (e) {
                console.error('解析图片列表失败', e);
                this.resultUrl = url;
            }
        } else if (url.startsWith('FORTUNE_JSON:')) {
            // 今日黄历卡片渲染模式
            try {
                const jsonStr = url.substring('FORTUNE_JSON:'.length);
                this.fortuneData = JSON.parse(jsonStr);
                this.isFortuneCard = true;
                this.resultUrl = ''; // 不显示图片
                console.log('检测到黄历JSON，开启卡片渲染模式', this.fortuneData);
            } catch (e) {
                console.error('解析黄历JSON失败', e);
                this.resultUrl = url;
            }
        } else {
            this.resultUrl = url;
            // 判断是否为视频（去水印类型且URL包含视频特征）
            this.isVideo = this.type === '1' && this.isVideoUrl(this.resultUrl);
        }
        
        console.log('=== 最终显示信息 ===');
        console.log('最终resultUrl:', this.resultUrl);
        console.log('isVideo:', this.isVideo);
        console.log('==================');
    },
    methods: {
        getTypeName(type) {
            const names = {
                '1': '去水印',
                '2': 'AI头像',
                '3': '姓氏签名',
                '4': '运势测试',
                '5': '星座运势',
                '6': '老照片修复'
            };
            return names[type] || '结果';
        },
        /**
         * 判断是否为视频URL
         */
        isVideoUrl(url) {
            if (!url) return false;
            // 检查URL是否包含视频特征
            const videoExtensions = ['.mp4', '.m3u8', '.flv', '.mov', '.avi', '.webm'];
            const videoKeywords = ['video', 'play', 'stream', 'm3u8'];
            
            const lowerUrl = url.toLowerCase();
            // 检查文件扩展名
            if (videoExtensions.some(ext => lowerUrl.includes(ext))) {
                return true;
            }
            // 检查关键词
            if (videoKeywords.some(keyword => lowerUrl.includes(keyword))) {
                return true;
            }
            // 检查是否是抖音/小红书视频链接（通常包含 play 或 video）
            if (lowerUrl.includes('douyin') || lowerUrl.includes('xiaohongshu')) {
                if (lowerUrl.includes('play') || lowerUrl.includes('video')) {
                    return true;
                }
            }
            return false;
        },
        onImageLoad() {
            console.log('图片加载完成', this.resultUrl);
        },
        onImageError(e) {
            console.error('图片加载失败', e, this.resultUrl);
            
            // 检查是否是Content-Disposition: attachment导致的显示问题
            // 小程序中，即使Content-Disposition是attachment，image标签通常也能显示
            // 但如果无法显示，可能是域名白名单或网络问题
            
            uni.showModal({
                title: '图片加载失败',
                content: '图片可能无法访问，请检查：\n1. 网络连接\n2. 小程序域名白名单配置\n3. 图片链接是否有效\n\n图片地址：' + (this.resultUrl.length > 50 ? this.resultUrl.substring(0, 50) + '...' : this.resultUrl),
                showCancel: true,
                confirmText: '复制地址',
                cancelText: '关闭',
                success: (res) => {
                    if (res.confirm) {
                        // 复制图片地址到剪贴板
                        uni.setClipboardData({
                            data: this.resultUrl,
                            success: () => {
                                uni.showToast({
                                    title: '地址已复制',
                                    icon: 'success'
                                });
                            }
                        });
                    }
                }
            });
        },
        onVideoPlay() {
            console.log('视频开始播放');
        },
        onVideoError(e) {
            console.error('视频播放错误', e);
            uni.showToast({
                title: '视频播放失败，请检查网络',
                icon: 'none'
            });
        },
        onSwiperChange(e) {
            this.currentImageIndex = e.detail.current;
        },
        async saveToAlbum() {
            try {
                // 1. 显示激励视频广告
                await showRewardedVideo();
                
                // 2. 保存到相册（图片或视频）
                if (this.isVideo) {
                    await this.downloadAndSaveVideo();
                } else {
                    await this.downloadAndSaveImage();
                }
                
                // 3. 显示插屏广告（第1、3、5次）
                const saveCount = this.getSaveCount();
                const shouldShowInterstitial = [1, 3, 5].includes(saveCount);
                await showInterstitialAd(null, shouldShowInterstitial);
                
                uni.showToast({
                    title: '保存成功',
                    icon: 'success'
                });
            } catch (e) {
                console.error('保存失败', e);
                uni.showToast({
                    title: e.message || '保存失败，请重试',
                    icon: 'none'
                });
            }
        },
        /**
         * 下载并保存图片
         */
        downloadAndSaveImage() {
            return new Promise((resolve, reject) => {
                // 如果是多图，保存所有图片
                if (this.imageList.length > 1) {
                    this.downloadAndSaveAllImages().then(resolve).catch(reject);
                    return;
                }
                
                // 单图保存
                uni.showLoading({
                    title: '下载中...',
                    mask: true
                });
                
                uni.downloadFile({
                    url: this.resultUrl,
                    success: (res) => {
                        uni.hideLoading();
                        if (res.statusCode === 200) {
                            uni.saveImageToPhotosAlbum({
                                filePath: res.tempFilePath,
                                success: () => {
                                    this.incrementSaveCount();
                                    resolve();
                                },
                                fail: (err) => {
                                    console.error('保存图片失败', err);
                                    // 检查是否缺少权限
                                    if (err.errMsg && err.errMsg.includes('auth deny')) {
                                        uni.showModal({
                                            title: '需要相册权限',
                                            content: '请在设置中开启相册权限',
                                            showCancel: false
                                        });
                                    }
                                    reject(err);
                                }
                            });
                        } else {
                            reject(new Error('下载失败'));
                        }
                    },
                    fail: (err) => {
                        uni.hideLoading();
                        console.error('下载文件失败', err);
                        reject(err);
                    }
                });
            });
        },
        /**
         * 下载并保存所有图片
         */
        downloadAndSaveAllImages() {
            return new Promise((resolve, reject) => {
                uni.showLoading({
                    title: `下载图片 1/${this.imageList.length}...`,
                    mask: true
                });
                
                let savedCount = 0;
                let failedCount = 0;
                const total = this.imageList.length;
                
                // 逐个下载并保存
                const downloadNext = (index) => {
                    if (index >= total) {
                        uni.hideLoading();
                        if (savedCount > 0) {
                            this.incrementSaveCount();
                            uni.showToast({
                                title: `已保存${savedCount}张图片`,
                                icon: 'success'
                            });
                            resolve();
                        } else {
                            reject(new Error('所有图片保存失败'));
                        }
                        return;
                    }
                    
                    uni.showLoading({
                        title: `下载图片 ${index + 1}/${total}...`,
                        mask: true
                    });
                    
                    uni.downloadFile({
                        url: this.imageList[index],
                        success: (res) => {
                            if (res.statusCode === 200) {
                                uni.saveImageToPhotosAlbum({
                                    filePath: res.tempFilePath,
                                    success: () => {
                                        savedCount++;
                                        downloadNext(index + 1);
                                    },
                                    fail: (err) => {
                                        console.error(`保存第${index + 1}张图片失败`, err);
                                        failedCount++;
                                        // 继续保存下一张
                                        downloadNext(index + 1);
                                    }
                                });
                            } else {
                                failedCount++;
                                downloadNext(index + 1);
                            }
                        },
                        fail: (err) => {
                            console.error(`下载第${index + 1}张图片失败`, err);
                            failedCount++;
                            downloadNext(index + 1);
                        }
                    });
                };
                
                downloadNext(0);
            });
        },
        /**
         * 下载并保存视频
         */
        downloadAndSaveVideo() {
            return new Promise((resolve, reject) => {
                uni.showLoading({
                    title: '下载视频中...',
                    mask: true
                });
                
                uni.downloadFile({
                    url: this.resultUrl,
                    success: (res) => {
                        uni.hideLoading();
                        if (res.statusCode === 200) {
                            // 保存视频到相册
                            uni.saveVideoToPhotosAlbum({
                                filePath: res.tempFilePath,
                                success: () => {
                                    this.incrementSaveCount();
                                    resolve();
                                },
                                fail: (err) => {
                                    console.error('保存视频失败', err);
                                    // 检查是否缺少权限
                                    if (err.errMsg && err.errMsg.includes('auth deny')) {
                                        uni.showModal({
                                            title: '需要相册权限',
                                            content: '请在设置中开启相册权限',
                                            showCancel: false
                                        });
                                    }
                                    reject(err);
                                }
                            });
                        } else {
                            reject(new Error('下载失败'));
                        }
                    },
                    fail: (err) => {
                        uni.hideLoading();
                        console.error('下载视频失败', err);
                        // 如果是网络错误，提供更友好的提示
                        if (err.errMsg && err.errMsg.includes('fail')) {
                            uni.showModal({
                                title: '下载失败',
                                content: '视频下载失败，可能是网络问题或视频链接已失效',
                                showCancel: false
                            });
                        }
                        reject(err);
                    }
                });
            });
        },
        getSaveCount() {
            return uni.getStorageSync('saveCount') || 0;
        },
        incrementSaveCount() {
            const count = this.getSaveCount() + 1;
            uni.setStorageSync('saveCount', count);
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

.result-header {
    text-align: center;
    margin-bottom: 40rpx;
}

.result-title {
    font-size: 32rpx;
    color: #333;
    font-weight: 500;
}

.result-container {
    margin-bottom: 40rpx;
}

.result-image-container {
    background: #fff;
    border-radius: 20rpx;
    padding: 30rpx;
    min-height: 500rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
}

.image-swiper {
    width: 100%;
    height: 600rpx;
}

.result-image {
    max-width: 100%;
    max-height: 600rpx;
    border-radius: 10rpx;
    width: 100%;
    height: 100%;
}

.image-indicator {
    position: absolute;
    top: 50rpx;
    right: 50rpx;
    background: rgba(0, 0, 0, 0.5);
    color: #fff;
    padding: 10rpx 20rpx;
    border-radius: 20rpx;
    font-size: 24rpx;
}

.indicator-text {
    color: #fff;
}

.result-video-container {
    background: #000;
    border-radius: 20rpx;
    overflow: hidden;
    margin-bottom: 40rpx;
}

.result-video {
    width: 100%;
    min-height: 500rpx;
    background: #000;
}

.action-buttons {
    margin-bottom: 100rpx;
}

.save-btn {
    width: 100%;
    height: 88rpx;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    border-radius: 44rpx;
    font-size: 32rpx;
    border: none;
}

/* 今日黄历卡片样式 */
.fortune-card {
    background-color: #fdf6e3;
    border: 1px solid #dcd3b2;
    border-radius: 12rpx;
    padding: 20rpx;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.fortune-inner {
    border: 1px solid #e8e0c5;
    background-color: #fffaf0;
    padding: 20rpx;
}

.fortune-header-box {
    display: flex;
    justify-content: center;
    align-items: center;
    border-bottom: 2rpx solid #efead3;
    padding-bottom: 20rpx;
    margin-bottom: 20rpx;
}

.fortune-header-title {
    font-size: 36rpx;
    color: #8b4513;
    font-weight: bold;
    margin: 0 30rpx;
}

.header-decoration {
    color: #c9a063;
    font-size: 32rpx;
}

.fortune-main-table {
    display: flex;
    flex-direction: column;
}

.fortune-row {
    display: flex;
    border: 1rpx solid #efead3;
    margin-bottom: -1rpx;
}

.fortune-label {
    flex: 1;
    background-color: #faf7eb;
    color: #666;
    font-size: 24rpx;
    padding: 16rpx 10rpx;
    text-align: center;
    border-right: 1rpx solid #efead3;
    display: flex;
    align-items: center;
    justify-content: center;
}

.fortune-value {
    flex: 2;
    background-color: #fff;
    color: #333;
    font-size: 26rpx;
    padding: 16rpx 20rpx;
    border-right: 1rpx solid #efead3;
    display: flex;
    align-items: center;
}

.fortune-value:last-child {
    border-right: none;
}

.fortune-info-row {
    display: flex;
    border: 1rpx solid #efead3;
    margin-top: -1rpx;
    min-height: 100rpx;
}

.item-label {
    flex: 0 0 120rpx;
    background-color: #faf7eb;
    display: flex;
    align-items: center;
    justify-content: center;
}

.fortune-info-content {
    flex: 1;
    background-color: #fff;
    padding: 20rpx;
    font-size: 26rpx;
    line-height: 1.6;
}

.badge {
    width: 60rpx;
    height: 60rpx;
    border-radius: 50%;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28rpx;
    font-weight: bold;
}

.badge.yi { background-color: #e64340; } /* 红色 宜 */
.badge.ji { background-color: #09bb07; } /* 绿色 忌 */
.badge.chong { background-color: #576b95; } /* 蓝色 冲/岁 */

.yi-text { color: #d32f2f; }
.ji-text { color: #388e3c; }
.chong-text { color: #1976d2; }
</style>

