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
    
    // OSS STS凭证（已废弃）
    getStsCredentials: () => request({ url: '/tool/oss/sts-credentials', method: 'GET' }),
    
    // 图片上传（已废弃，建议使用OSS直传）
    uploadImage: (filePath) => {
        return new Promise((resolve, reject) => {
            uni.uploadFile({
                url: apiConfig.BASE_URL + '/tool/upload-image',
                filePath: filePath,
                name: 'file',
                header: {
                    'Authorization': `Bearer ${uni.getStorageSync('token')}`
                },
                success: (res) => {
                    try {
                        const data = JSON.parse(res.data);
                        if (data.code === 200) {
                            resolve(data);
                        } else {
                            reject(data);
                        }
                    } catch (e) {
                        reject({ message: '上传失败' });
                    }
                },
                fail: reject
            });
        });
    },
    
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
    submitFeedback: (data) => request({ url: '/feedback/submit', method: 'POST', data })
};

export default api;

