/**
 * API接口
 */
import request from './request.js';
import apiConfig from '@/common/config/api-config.js';

const api = {
    // 认证
    wxLogin: (code) => request({ url: '/auth/wx-login', method: 'POST', data: { code } }),
    decryptUserInfo: (data) => request({ url: '/auth/decrypt-userinfo', method: 'POST', data }),
    
    // 工具
    // 即时翻译
    translate: (data) => request({ url: '/tool/translate', method: 'POST', data }),
    
    // 去水印
    removeWatermark: (data) => request({ url: '/tool/remove-watermark', method: 'POST', data }),
    
    // 老照片修复
    restoreOldPhoto: (data) => request({ url: '/tool/restore-old-photo', method: 'POST', data }),
    batchRestoreOldPhoto: (data) => request({ url: '/tool/batch-restore-old-photo', method: 'POST', data }),
    
    // 图片生成（统一任务创建接口）
    createImageTask: (data) => request({ url: '/tool/image-generate', method: 'POST', data }),
    
    // OSS PostObject签名
    getPostObjectSignature: (fileName) => request({ 
        url: '/tool/oss/post-signature' + (fileName ? '?fileName=' + encodeURIComponent(fileName) : ''), 
        method: 'GET' 
    }),
    
    // 图片模版
    getTemplates: () => request({ url: '/tool/templates', method: 'GET' }),
    // templateGenerate: (data) => request({ url: '/tool/template-generate', method: 'POST', data }),
    
    // 任务相关
    getTasks: (status) => request({ 
        url: `/tool/tasks${status !== undefined && status !== null ? '?status=' + status : ''}`, 
        method: 'GET' 
    }),
    getTaskDetail: (taskId) => request({ url: `/tool/task/${taskId}`, method: 'GET' }),
    downloadImage: (taskId) => request({ url: `/tool/task/${taskId}/download`, method: 'GET' }),
    
    // 蒙古语 AI 对话
    mongolianChat: (data) => request({ url: '/api/ai/mongolian-chat', method: 'POST', data }),
    
    // AI 识图
    imageRecognize: (data) => request({ url: '/tool/image-recognize', method: 'POST', data }),
    
    // 反馈
    submitFeedback: (data) => request({ url: '/feedback/submit', method: 'POST', data }),
    
    // 广告
    recordAdWatch: (type, rewardCount) => request({ 
        url: '/api/ad/record-watch', 
        method: 'POST', 
        data: { 
            type: type, 
            rewardCount: rewardCount || 10 
        } 
    }),
    
    // 分享
    recordShare: (data) => request({ 
        url: '/api/share/record', 
        method: 'POST', 
        data: data 
    }),
    
    // 功能配置
    getFunctionList: () => request({ url: '/api/function/list', method: 'GET' })
};

export default api;

