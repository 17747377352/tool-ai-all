<template>
    <view class="container">
        <view v-if="loading" class="loading">
            <text>加载中...</text>
        </view>
        <view v-else class="template-grid">
            <view 
                v-for="template in templates" 
                :key="template.id"
                class="template-item"
                @click="selectTemplate(template)"
            >
                <image :src="template.imageUrl" class="template-image" mode="aspectFill"></image>
                <view class="template-info">
                    <text class="template-name">{{ template.name }}</text>
                    <text class="template-desc">{{ template.description }}</text>
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
            templates: [],
            loading: true
        };
    },
    onLoad() {
        this.loadTemplates();
    },
    methods: {
        async loadTemplates() {
            try {
                const res = await api.getTemplates();
                if (res.code === 200) {
                    this.templates = res.data;
                }
            } catch (e) {
                console.error('加载模版失败', e);
                uni.showToast({
                    title: '加载失败',
                    icon: 'none'
                });
            } finally {
                this.loading = false;
            }
        },
        selectTemplate(template) {
            // 返回上一页并传递选中的模版ID
            const pages = getCurrentPages();
            const prevPage = pages[pages.length - 2];
            if (prevPage) {
                prevPage.selectedTemplate = template;
                prevPage.generateMode = 3; // 默认模版同款
            }
            uni.navigateBack();
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

.template-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20rpx;
}

.template-item {
    background: #fff;
    border-radius: 16rpx;
    overflow: hidden;
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.1);
}

.template-image {
    width: 100%;
    height: 300rpx;
}

.template-info {
    padding: 20rpx;
}

.template-name {
    display: block;
    font-size: 28rpx;
    font-weight: 500;
    color: #333;
    margin-bottom: 10rpx;
}

.template-desc {
    display: block;
    font-size: 24rpx;
    color: #999;
    line-height: 1.4;
}
</style>

