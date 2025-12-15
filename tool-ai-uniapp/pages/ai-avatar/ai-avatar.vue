<template>
    <view class="container">
        <!-- 模式选择 -->
        <view class="form-item">
            <text class="label">生成模式</text>
            <view class="mode-options">
                <view
                    class="mode-item"
                    :class="{ active: mode === 'text' }"
                    @click="mode = 'text'"
                >
                    <text>字生图</text>
                </view>
                <view
                    class="mode-item"
                    :class="{ active: mode === 'image' }"
                    @click="mode = 'image'"
                >
                    <text>图生图</text>
                </view>
            </view>
        </view>

        <!-- 图生图模式：图片上传 -->
        <view v-if="mode === 'image'" class="form-item">
            <text class="label">上传图片</text>
            <view class="upload-area" @click="chooseImage">
                <image v-if="imageUrl" :src="imageUrl" class="preview-image" mode="aspectFit"></image>
                <view v-else class="upload-placeholder">
                    <text class="upload-icon">📷</text>
                    <text class="upload-text">点击选择图片</text>
                </view>
            </view>
        </view>

        <!-- 生成提示词 -->
        <view class="form-item">
            <text class="label">生成提示词</text>
            <textarea
                v-model="prompt"
                class="textarea"
                :placeholder="mode === 'image' ? '例如：将这个头像转换为卡通风格' : '例如：一个可爱的卡通头像，蓝色背景'"
                maxlength="200"
            ></textarea>
        </view>

        <!-- 选择风格 -->
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
            :disabled="!prompt || (mode === 'image' && !imageUrl) || generating" 
            @click="generate"
        >
            {{ generating ? '生成中...' : '立即生成' }}
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
            mode: 'text', // 'text' 字生图, 'image' 图生图
            imageUrl: '', // 上传的图片URL（图生图模式）
            imageTempPath: '', // 临时图片路径（用于预览）
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
        /**
         * 选择图片（图生图模式）
         */
        chooseImage() {
            uni.chooseImage({
                count: 1,
                sizeType: ['compressed'],
                sourceType: ['album', 'camera'],
                success: (res) => {
                    this.imageTempPath = res.tempFilePaths[0];
                    // 先显示本地预览
                    this.imageUrl = this.imageTempPath;
                    // 上传图片到服务器获取URL
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
         * 上传图片到服务器
         */
        async uploadImage() {
            if (!this.imageTempPath) {
                return;
            }
            
            uni.showLoading({
                title: '上传中...'
            });
            
            try {
                // 获取token
                const token = uni.getStorageSync('token');
                if (!token) {
                    throw new Error('未登录');
                }
                
                // 使用uni.uploadFile上传图片
                const uploadRes = await new Promise((resolve, reject) => {
                    uni.uploadFile({
                        url: `${apiConfig.BASE_URL}/tool/upload-image`,
                        filePath: this.imageTempPath,
                        name: 'file',
                        header: {
                            'Authorization': 'Bearer ' + token
                        },
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
                
                // 更新图片URL为服务器返回的URL
                if (uploadRes.data && uploadRes.data.imageUrl) {
                    this.imageUrl = uploadRes.data.imageUrl;
                }
                
                uni.hideLoading();
            } catch (e) {
                console.error('上传图片失败', e);
                uni.hideLoading();
                uni.showToast({
                    title: e.message || '上传图片失败',
                    icon: 'none'
                });
                // 上传失败时清空图片
                this.imageUrl = '';
                this.imageTempPath = '';
            }
        },
        
        /**
         * 生成头像
         */
        async generate() {
            if (!this.prompt.trim()) {
                uni.showToast({
                    title: '请输入提示词',
                    icon: 'none'
                });
                return;
            }
            
            if (this.mode === 'image' && !this.imageUrl) {
                uni.showToast({
                    title: '请先上传图片',
                    icon: 'none'
                });
                return;
            }

            this.generating = true;
            try {
                const requestData = {
                    prompt: this.prompt,
                    style: this.selectedStyle
                };
                
                // 图生图模式：添加图片URL
                if (this.mode === 'image' && this.imageUrl) {
                    // 如果是临时路径，需要先上传到服务器
                    // 这里暂时使用临时路径，实际应该使用服务器返回的URL
                    requestData.imageUrl = this.imageUrl;
                }
                
                const res = await api.generateAiAvatar(requestData);
                if (res.code === 200) {
                    uni.navigateTo({
                        url: `/pages/result/result?type=2&resultUrl=${encodeURIComponent(res.data.resultUrl)}`
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
    margin-top: 40rpx;
}

.generate-btn[disabled] {
    background: #ccc;
}

.mode-options {
    display: flex;
    gap: 20rpx;
}

.mode-item {
    flex: 1;
    padding: 20rpx;
    background: #f5f5f5;
    border-radius: 10rpx;
    text-align: center;
    font-size: 28rpx;
    color: #666;
    border: 2rpx solid transparent;
}

.mode-item.active {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    border-color: #667eea;
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
</style>


