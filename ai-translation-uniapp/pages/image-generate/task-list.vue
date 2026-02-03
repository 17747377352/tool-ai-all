<template>
    <view class="container">
        <view class="filter-tabs">
            <view 
                class="filter-tab"
                :class="{ active: statusFilter === null }"
                @click="statusFilter = null"
            >
                <text>全部</text>
            </view>
            <view 
                class="filter-tab"
                :class="{ active: statusFilter === 0 }"
                @click="statusFilter = 0"
            >
                <text>排队中</text>
            </view>
            <view 
                class="filter-tab"
                :class="{ active: statusFilter === 1 }"
                @click="statusFilter = 1"
            >
                <text>生成中</text>
            </view>
            <view 
                class="filter-tab"
                :class="{ active: statusFilter === 2 }"
                @click="statusFilter = 2"
            >
                <text>已完成</text>
            </view>
        </view>

        <view v-if="loading" class="loading">
            <text>加载中...</text>
        </view>
        <view v-else-if="tasks.length === 0" class="empty">
            <text>暂无任务</text>
        </view>
        <view v-else class="task-list">
            <view 
                v-for="task in tasks" 
                :key="task.id"
                class="task-item"
                @click="viewTaskDetail(task.id)"
            >
                <view class="task-header">
                    <text class="task-status" :class="getStatusClass(task.taskStatus)">
                        {{ getStatusText(task.taskStatus) }}
                    </text>
                    <text class="task-time">{{ formatTime(task.createTime) }}</text>
                </view>
                <view class="task-content">
                    <text class="task-prompt">{{ task.prompt || '无提示词' }}</text>
                </view>
                <view v-if="task.resultUrl && task.taskStatus === 2" class="task-result">
                    <image :src="extractImageUrl(task.resultUrl)" class="result-thumb" mode="aspectFill"></image>
                </view>
            </view>
        </view>
    </view>
</template>

<script>
import api from '@/common/utils/api.js';

export default {
    data() {
        return {
            tasks: [],
            statusFilter: null,
            loading: true
        };
    },
    watch: {
        statusFilter() {
            this.loadTasks();
        }
    },
    onLoad() {
        this.loadTasks();
    },
    onShow() {
        // 每次显示时刷新任务列表
        this.loadTasks();
    },
    methods: {
        async loadTasks() {
            this.loading = true;
            try {
                const res = await api.getTasks(this.statusFilter);
                if (res.code === 200) {
                    this.tasks = res.data;
                }
            } catch (e) {
                console.error('加载任务失败', e);
                uni.showToast({
                    title: '加载失败',
                    icon: 'none'
                });
            } finally {
                this.loading = false;
            }
        },
        viewTaskDetail(taskId) {
            uni.navigateTo({
                url: `/pages/result/result?type=task&taskId=${taskId}`
            });
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
        formatTime(timeStr) {
            if (!timeStr) return '';
            const date = new Date(timeStr);
            const now = new Date();
            const diff = now - date;
            const minutes = Math.floor(diff / 60000);
            if (minutes < 1) return '刚刚';
            if (minutes < 60) return `${minutes}分钟前`;
            const hours = Math.floor(minutes / 60);
            if (hours < 24) return `${hours}小时前`;
            const days = Math.floor(hours / 24);
            if (days < 7) return `${days}天前`;
            return date.toLocaleDateString();
        },
        extractImageUrl(resultUrl) {
            // 从 IMAGE_LIST:["url"] 格式中提取URL
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

.filter-tabs {
    display: flex;
    gap: 15rpx;
    margin-bottom: 30rpx;
    background: #fff;
    border-radius: 20rpx;
    padding: 10rpx;
}

.filter-tab {
    flex: 1;
    text-align: center;
    padding: 15rpx;
    border-radius: 10rpx;
    font-size: 26rpx;
    color: #666;
}

.filter-tab.active {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
}

.loading, .empty {
    text-align: center;
    padding: 100rpx 0;
    color: #999;
}

.task-list {
    display: flex;
    flex-direction: column;
    gap: 20rpx;
}

.task-item {
    background: #fff;
    border-radius: 16rpx;
    padding: 30rpx;
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.1);
}

.task-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15rpx;
}

.task-status {
    font-size: 26rpx;
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

.task-time {
    font-size: 24rpx;
    color: #999;
}

.task-content {
    margin-bottom: 15rpx;
}

.task-prompt {
    font-size: 28rpx;
    color: #333;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
}

.task-result {
    margin-top: 15rpx;
}

.result-thumb {
    width: 100%;
    height: 300rpx;
    border-radius: 12rpx;
}
</style>

