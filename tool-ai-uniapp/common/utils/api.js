/**
 * API接口
 */
import request from './request.js';

const api = {
    // 认证
    wxLogin: (code) => request({ url: '/auth/wx-login', method: 'POST', data: { code } }),
    decryptUserInfo: (data) => request({ url: '/auth/decrypt-userinfo', method: 'POST', data }),
    
    // 工具
    removeLogo: (data) => request({ url: '/tool/remove-logo', method: 'POST', data }),
    generateAiAvatar: (data) => request({ url: '/tool/ai-avatar', method: 'POST', data }),
    generateNameSign: (data) => request({ url: '/tool/name-sign', method: 'POST', data }),
    generateFortune: (data) => request({ url: '/tool/fortune', method: 'POST', data }),
    generateConstellationFortune: (data) => request({ url: '/tool/constellation-fortune', method: 'POST', data }),
    restoreOldPhoto: (data) => request({ url: '/tool/restore-old-photo', method: 'POST', data }),
    batchRestoreOldPhoto: (data) => request({ url: '/tool/batch-restore-old-photo', method: 'POST', data }),
    
    // 广告相关
    recordAdWatch: (adType) => request({ url: '/ad/watch?adType=' + adType, method: 'POST', data: {} }),
    checkRemoveLogoAd: () => request({ url: '/ad/check-remove-logo', method: 'GET' }),
    checkBatchRestoreAd: () => request({ url: '/ad/check-batch-restore', method: 'GET' }),
    
    // 生活查询
    lifeExpress: (data) => request({ url: '/life/express', method: 'POST', data }),
    lifeOilPrice: (params) => request({ url: '/life/oil-price', method: 'GET', data: params || {} }),
    lifeForex: (params) => request({ url: '/life/forex', method: 'GET', data: params || {} }),
    lifeLottery: (params) => request({ url: '/life/lottery', method: 'GET', data: params || {} }),
    
    // 反馈
    submitFeedback: (data) => request({ url: '/feedback/submit', method: 'POST', data })
};

export default api;

