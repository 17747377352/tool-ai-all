<template>
    <view class="container">
        <view v-if="loading" class="loading">
            <text>加载中...</text>
        </view>
        <view v-else-if="taskInfo">
            <!-- 任务详情模式 -->
            <view class="task-header">
                <text class="task-status" :class="getStatusClass(taskInfo.taskStatus)">
                    {{ getStatusText(taskInfo.taskStatus) }}
                </text>
            </view>
            <view v-if="taskInfo.taskStatus === 2 && taskInfo.resultUrl" class="result-container">
                <image 
                    :src="extractImageUrl(taskInfo.resultUrl)" 
                    class="result-image" 
                    mode="aspectFit"
                ></image>
            </view>
            <view v-else-if="taskInfo.taskStatus === 3" class="error-container">
                <text class="error-text">{{ taskInfo.errorMessage || '生成失败' }}</text>
            </view>
            <view v-else class="processing-container">
                <text class="processing-text">{{ taskInfo.taskStatus === 0 ? '任务排队中...' : '正在生成中...' }}</text>
            </view>
            <view class="action-buttons">
                <button 
                    v-if="taskInfo.taskStatus === 2 && taskInfo.resultUrl"
                    class="save-btn" 
                    @click="saveToAlbum"
                >
                    保存到相册
                </button>
                <button 
                    v-if="taskInfo.taskStatus === 2 && taskInfo.resultUrl"
                    class="download-btn" 
                    @click="downloadImage"
                >
                    下载图片
                </button>
            </view>
        </view>
        <view v-else>
            <!-- 普通结果模式 -->
            <view class="result-container">
                <image 
                    v-if="imageList.length > 0"
                    v-for="(imageUrl, index) in imageList" 
                    :key="index"
                    :src="imageUrl" 
                    class="result-image" 
                    mode="aspectFit"
                ></image>
                <image 
                    v-else-if="resultUrl"
                    :src="resultUrl" 
                    class="result-image" 
                    mode="aspectFit"
                ></image>
            </view>
            <view class="action-buttons">
                <button class="save-btn" @click="saveToAlbum">保存到相册</button>
            </view>
        </view>
    </view>
</template>

<script>
import api from '@/common/utils/api.js';

export default {
    data() {
        return {
            resultUrl: '',
            type: '',
            taskId: null,
            taskInfo: null,
            imageList: [],
            loading: false
        };
    },
    onLoad(options) {
        if (options.taskId) {
            // 任务详情模式
            this.taskId = options.taskId;
            this.loadTaskDetail();
        } else {
            // 普通结果模式
            this.type = options.type || '';
            let url = decodeURIComponent(options.resultUrl || '');
            
            if (url.startsWith('IMAGE_LIST:')) {
                try {
                    const imageListJson = url.substring('IMAGE_LIST:'.length);
                    this.imageList = JSON.parse(imageListJson);
                    if (this.imageList && this.imageList.length > 0) {
                        this.resultUrl = this.imageList[0];
                    }
                } catch (e) {
                    console.error('解析图片列表失败', e);
                    this.resultUrl = url;
                }
            } else {
                this.resultUrl = url;
            }
        }
    },
    onShow() {
        // 如果是任务模式，定时刷新状态
        if (this.taskId && this.taskInfo && this.taskInfo.taskStatus !== 2 && this.taskInfo.taskStatus !== 3) {
            this.refreshTaskStatus();
        }
    },
    methods: {
        async loadTaskDetail() {
            this.loading = true;
            try {
                const res = await api.getTaskDetail(this.taskId);
                if (res.code === 200) {
                    this.taskInfo = res.data;
                } else {
                    uni.showToast({
                        title: res.message || '加载失败',
                        icon: 'none'
                    });
                }
            } catch (e) {
                console.error('加载任务详情失败', e);
                uni.showToast({
                    title: '加载失败',
                    icon: 'none'
                });
            } finally {
                this.loading = false;
            }
        },
        async refreshTaskStatus() {
            try {
                const res = await api.getTaskDetail(this.taskId);
                if (res.code === 200) {
                    this.taskInfo = res.data;
                    // 如果已完成，停止刷新
                    if (this.taskInfo.taskStatus === 2 || this.taskInfo.taskStatus === 3) {
                        return;
                    }
                    // 继续刷新
                    setTimeout(() => {
                        this.refreshTaskStatus();
                    }, 3000);
                }
            } catch (e) {
                console.error('刷新任务状态失败', e);
            }
        },
        getStatusText(status) {
            const statusMap = {
                0: '排队中',
                1: '生成中',
                2: '已完成',
                3: '失败'
            };
            return statusMap[status] || '未知';
        },
        getStatusClass(status) {
            const classMap = {
                0: 'status-pending',
                1: 'status-processing',
                2: 'status-completed',
                3: 'status-failed'
            };
            return classMap[status] || '';
        },
        extractImageUrl(resultUrl) {
            if (resultUrl && resultUrl.startsWith('IMAGE_LIST:')) {
                try {
                    const jsonStr = resultUrl.replace('IMAGE_LIST:', '');
                    const urls = JSON.parse(jsonStr);
                    return urls[0] || '';
                } catch (e) {
                    return resultUrl;
                }
            }
            return resultUrl || '';
        },
        async saveToAlbum() {
            const imageUrl = this.taskInfo ? this.extractImageUrl(this.taskInfo.resultUrl) : (this.imageList[0] || this.resultUrl);
            if (!imageUrl) {
                return;
            }
            
            try {
                uni.showLoading({
                    title: '下载中...',
                    mask: true
                });
                
                uni.downloadFile({
                    url: imageUrl,
                    success: (res) => {
                        uni.hideLoading();
                        if (res.statusCode === 200) {
                            uni.saveImageToPhotosAlbum({
                                filePath: res.tempFilePath,
                                success: () => {
                                    uni.showToast({
                                        title: '保存成功',
                                        icon: 'success'
                                    });
                                },
                                fail: (err) => {
                                    console.error('保存图片失败', err);
                                    uni.showToast({
                                        title: '保存失败',
                                        icon: 'none'
                                    });
                                }
                            });
                        } else {
                            uni.showToast({
                                title: '下载失败',
                                icon: 'none'
                            });
                        }
                    },
                    fail: (err) => {
                        uni.hideLoading();
                        console.error('下载文件失败', err);
                        uni.showToast({
                            title: '下载失败',
                            icon: 'none'
                        });
                    }
                });
            } catch (e) {
                uni.hideLoading();
                console.error('保存失败', e);
                uni.showToast({
                    title: '保存失败',
                    icon: 'none'
                });
            }
        },
        async downloadImage() {
            const imageUrl = this.extractImageUrl(this.taskInfo.resultUrl);
            if (!imageUrl) {
                return;
            }
            
            try {
                const res = await api.downloadImage(this.taskId);
                if (res.code === 200) {
                    uni.showToast({
                        title: '下载链接已获取',
                        icon: 'success'
                    });
                }
            } catch (e) {
                console.error('下载失败', e);
                uni.showToast({
                    title: '下载失败',
                    icon: 'none'
                });
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

.loading {
    text-align: center;
    padding: 100rpx 0;
    color: #999;
}

.task-header {
    text-align: center;
    margin-bottom: 30rpx;
}

.task-status {
    font-size: 32rpx;
    font-weight: 500;
}

.status-pending {
    color: #999;
}

.status-processing {
    color: #667eea;
}

.status-completed {
    color: #52c41a;
}

.status-failed {
    color: #ff4d4f;
}

.result-container {
    background: #fff;
    border-radius: 20rpx;
    padding: 30rpx;
    margin-bottom: 30rpx;
    min-height: 500rpx;
    display: flex;
    align-items: center;
    justify-content: center;
}

.result-image {
    max-width: 100%;
    max-height: 600rpx;
    border-radius: 10rpx;
}

.processing-container, .error-container {
    background: #fff;
    border-radius: 20rpx;
    padding: 60rpx 30rpx;
    text-align: center;
    margin-bottom: 30rpx;
}

.processing-text {
    font-size: 28rpx;
    color: #667eea;
}

.error-text {
    font-size: 28rpx;
    color: #ff4d4f;
}

.action-buttons {
    display: flex;
    gap: 20rpx;
}

.save-btn, .download-btn {
    flex: 1;
    height: 88rpx;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    border-radius: 44rpx;
    font-size: 32rpx;
    border: none;
}

.download-btn {
    background: #52c41a;
}
</style>

