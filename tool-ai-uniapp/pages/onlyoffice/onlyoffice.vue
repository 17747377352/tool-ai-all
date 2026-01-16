<template>
    <view class="container">
        <web-view 
            :src="editorUrl" 
            @message="handleMessage"
            @error="handleError"
        ></web-view>
    </view>
</template>

<script>
import apiConfig from '@/common/config/api-config.js';

export default {
    data() {
        return {
            fileName: 'demo.docx',
            documentServerUrl: 'http://127.0.0.1:29000', // OnlyOffice Document Server 地址
            editorUrl: ''
        };
    },
    onLoad(options) {
        // 从路由参数获取文件名
        if (options.fileName) {
            this.fileName = decodeURIComponent(options.fileName);
        }
        
        // 构建编辑器 URL
        const baseUrl = apiConfig.BASE_URL;
        const configUrl = `${baseUrl}/onlyoffice/config?fileName=${encodeURIComponent(this.fileName)}`;
        const editorPageUrl = `${baseUrl}/onlyoffice-editor.html`;
        
        // 构建完整的编辑器 URL，包含参数
        this.editorUrl = `${editorPageUrl}?fileName=${encodeURIComponent(this.fileName)}&configUrl=${encodeURIComponent(configUrl)}&documentServerUrl=${encodeURIComponent(this.documentServerUrl)}`;
        
        console.log('编辑器 URL:', this.editorUrl);
    },
    methods: {
        handleMessage(event) {
            console.log('收到 web-view 消息:', event.detail.data);
        },
        handleError(event) {
            console.error('web-view 加载错误:', event);
            uni.showToast({
                title: '编辑器加载失败',
                icon: 'none',
                duration: 3000
            });
        }
    }
};
</script>

<style scoped>
.container {
    width: 100%;
    height: 100vh;
    overflow: hidden;
}
</style>

