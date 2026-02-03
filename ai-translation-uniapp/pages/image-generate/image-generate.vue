<template>
    <view class="container">
        <view class="mode-tabs">
            <view 
                class="mode-tab" 
                :class="{ active: mode === 'text' }"
                @click="mode = 'text'"
            >
                <text>字生图</text>
            </view>
            <view 
                class="mode-tab" 
                :class="{ active: mode === 'image' }"
                @click="mode = 'image'"
            >
                <text>图生图</text>
            </view>
            <view 
                class="mode-tab" 
                :class="{ active: mode === 'template' }"
                @click="mode = 'template'"
            >
                <text>模版生图</text>
            </view>
        </view>

        <!-- 字生图模式 -->
        <template v-if="mode === 'text'">
            <view class="form-card">
                <view class="form-item">
                    <text class="label">生成提示词</text>
                    <textarea
                        v-model="prompt"
                        class="textarea"
                        placeholder="例如：一个可爱的卡通头像，蓝色背景"
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
                    :disabled="!prompt || generating" 
                    @click="generateFromText"
                >
                    {{ generating ? '生成中...' : '立即生成' }}
                </button>
            </view>
        </template>

        <!-- 图生图模式 -->
        <template v-else-if="mode === 'image'">
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
                        v-model="imagePrompt"
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
                            :class="{ active: imageSelectedStyle === style.value }"
                            @click="imageSelectedStyle = style.value"
                        >
                            <text>{{ style.label }}</text>
                        </view>
                    </view>
                </view>

                <button 
                    class="generate-btn" 
                    :disabled="!imagePrompt || !imageUrl || generating" 
                    @click="generateFromImage"
                >
                    {{ generating ? '生成中...' : '立即生成' }}
                </button>
            </view>
        </template>

        <!-- 模版生图模式 -->
        <template v-else>
            <view class="form-card">
                <view class="template-section">
                    <view class="section-header">
                        <text class="section-title">选择模版</text>
                        <text class="section-link" @click="goToTemplateList">更多模版 ></text>
                    </view>
                    <view v-if="selectedTemplate" class="selected-template">
                        <image :src="selectedTemplate.imageUrl" class="template-image" mode="aspectFill"></image>
                        <view class="template-info">
                            <text class="template-name">{{ selectedTemplate.name }}</text>
                            <text class="template-desc">{{ selectedTemplate.description }}</text>
                        </view>
                    </view>
                    <view v-else class="select-template-btn" @click="goToTemplateList">
                        <text>点击选择模版</text>
                    </view>
                </view>

                <view v-if="selectedTemplate" class="form-item">
                    <text class="label">生成模式</text>
                    <view class="generate-mode-options">
                        <view
                            class="mode-option"
                            :class="{ active: generateMode === 3 }"
                            @click="generateMode = 3"
                        >
                            <text>模版同款</text>
                        </view>
                        <view
                            class="mode-option"
                            :class="{ active: generateMode === 4 }"
                            @click="generateMode = 4"
                        >
                            <text>模版参考图</text>
                        </view>
                    </view>
                </view>

                <view v-if="selectedTemplate && generateMode === 4" class="form-item">
                    <text class="label">自定义提示词（可选）</text>
                    <textarea
                        v-model="customPrompt"
                        class="textarea"
                        placeholder="可以修改或补充模版的提示词"
                        maxlength="200"
                    ></textarea>
                </view>

                <button 
                    v-if="selectedTemplate"
                    class="generate-btn" 
                    :disabled="generating" 
                    @click="generateFromTemplate"
                >
                    {{ generating ? '生成中...' : '立即生成' }}
                </button>
            </view>

            <view class="task-link" @click="goToTaskList">
                <text>查看我的任务 ></text>
            </view>
        </template>
    </view>
</template>

<script>
import api from '@/common/utils/api.js';
import { uploadToOss } from '@/common/utils/oss-upload.js';

export default {
    data() {
        return {
            mode: 'text', // 'text' 字生图, 'image' 图生图, 'template' 模版生图
            prompt: '',
            selectedStyle: 'realistic',
            styles: [
                { label: '写实', value: 'realistic' },
                { label: '卡通', value: 'cartoon' },
                { label: '动漫', value: 'anime' },
                { label: '油画', value: 'oil-painting' }
            ],
            imageUrl: '',
            imageTempPath: '',
            imagePrompt: '',
            imageSelectedStyle: 'realistic',
            selectedTemplate: null,
            generateMode: 3, // 3-模版同款 4-模版参考图
            customPrompt: '',
            generating: false
        };
    },
    onLoad(options) {
        // 如果从模版列表页面返回，获取选中的模版
        if (options.templateId) {
            this.loadTemplate(options.templateId);
        }
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

        async loadTemplate(templateId) {
            try {
                const res = await api.getTemplates();
                if (res.code === 200) {
                    const template = res.data.find(t => t.id === parseInt(templateId));
                    if (template) {
                        this.selectedTemplate = template;
                    }
                }
            } catch (e) {
                console.error('加载模版失败', e);
            }
        },
        goToTemplateList() {
            uni.navigateTo({
                url: '/pages/image-generate/template-list'
            });
        },
        goToTaskList() {
            uni.navigateTo({
                url: '/pages/image-generate/task-list'
            });
        },
        async generateFromText() {
            if (!this.prompt.trim()) {
                uni.showToast({
                    title: '请输入提示词',
                    icon: 'none'
                });
                return;
            }

            this.generating = true;
            try {
                const res = await api.createImageTask({
                    generateMode: 1,
                    prompt: this.prompt,
                    style: this.selectedStyle
                });

                if (res.code === 200) {
                    uni.showToast({
                        title: '任务已创建，请到任务列表查看',
                        icon: 'success'
                    });
                    setTimeout(() => {
                        this.goToTaskList();
                    }, 1500);
                } else {
                    uni.showToast({
                        title: res.message || '创建任务失败',
                        icon: 'none'
                    });
                }
            } catch (e) {
                console.error('创建任务失败', e);
                uni.showToast({
                    title: '创建任务失败，请重试',
                    icon: 'none'
                });
            } finally {
                this.generating = false;
            }
        },
        async generateFromImage() {
            if (!this.imagePrompt.trim()) {
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
                const res = await api.createImageTask({
                    generateMode: 2,
                    prompt: this.imagePrompt,
                    imageUrl: this.imageUrl,
                    style: this.imageSelectedStyle
                });

                if (res.code === 200) {
                    uni.showToast({
                        title: '任务已创建，请到任务列表查看',
                        icon: 'success'
                    });
                    setTimeout(() => {
                        this.goToTaskList();
                    }, 1500);
                } else {
                    uni.showToast({
                        title: res.message || '创建任务失败',
                        icon: 'none'
                    });
                }
            } catch (e) {
                console.error('创建任务失败', e);
                uni.showToast({
                    title: '创建任务失败，请重试',
                    icon: 'none'
                });
            } finally {
                this.generating = false;
            }
        },
        async generateFromTemplate() {
            if (!this.selectedTemplate) {
                uni.showToast({
                    title: '请选择模版',
                    icon: 'none'
                });
                return;
            }

            this.generating = true;
            try {
                const res = await api.createImageTask({
                    generateMode: this.generateMode,
                    templateId: this.selectedTemplate.id,
                    // 模版参考图时可以传自定义提示词
                    prompt: this.customPrompt || undefined
                });

                if (res.code === 200) {
                    uni.showToast({
                        title: '任务已创建，请到任务列表查看',
                        icon: 'success'
                    });
                    setTimeout(() => {
                        this.goToTaskList();
                    }, 1500);
                } else {
                    uni.showToast({
                        title: res.message || '创建任务失败',
                        icon: 'none'
                    });
                }
            } catch (e) {
                console.error('创建任务失败', e);
                uni.showToast({
                    title: '创建任务失败，请重试',
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

.mode-tabs {
    display: flex;
    gap: 20rpx;
    background: #fff;
    border-radius: 20rpx;
    padding: 10rpx;
    margin-bottom: 30rpx;
}

.mode-tab {
    flex: 1;
    text-align: center;
    padding: 20rpx;
    border-radius: 16rpx;
    transition: all 0.3s;
}

.mode-tab.active {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
}

.mode-tab:not(.active) {
    color: #666;
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

.template-section {
    margin-bottom: 30rpx;
}

.section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
}

.section-title {
    font-size: 28rpx;
    font-weight: 500;
    color: #333;
}

.section-link {
    font-size: 24rpx;
    color: #667eea;
}

.selected-template {
    display: flex;
    gap: 20rpx;
    background: #f5f5f5;
    border-radius: 12rpx;
    padding: 20rpx;
}

.template-image {
    width: 120rpx;
    height: 120rpx;
    border-radius: 8rpx;
}

.template-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
}

.template-name {
    font-size: 28rpx;
    font-weight: 500;
    color: #333;
    margin-bottom: 10rpx;
}

.template-desc {
    font-size: 24rpx;
    color: #999;
}

.select-template-btn {
    padding: 60rpx 20rpx;
    background: #f5f5f5;
    border-radius: 12rpx;
    text-align: center;
    color: #999;
    border: 2rpx dashed #ddd;
}

.generate-mode-options {
    display: flex;
    gap: 20rpx;
}

.mode-option {
    flex: 1;
    padding: 20rpx;
    background: #f5f5f5;
    border-radius: 10rpx;
    text-align: center;
    font-size: 26rpx;
    color: #666;
    border: 2rpx solid transparent;
}

.mode-option.active {
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

.task-link {
    margin-top: 30rpx;
    text-align: center;
    padding: 20rpx;
}

.task-link text {
    font-size: 28rpx;
    color: #667eea;
}
</style>

