/**
 * 请求工具
 */
import apiConfig from '@/common/config/api-config.js';

const baseUrl = apiConfig.BASE_URL;

const request = (options) => {
    return new Promise((resolve, reject) => {
        const token = uni.getStorageSync('token');
        
        uni.request({
            url: baseUrl + options.url,
            method: options.method || 'GET',
            data: options.data || {},
            header: {
                'Content-Type': 'application/json',
                'Authorization': token ? `Bearer ${token}` : ''
            },
            success: (res) => {
                if (res.statusCode === 200) {
                    if (res.data.code === 200) {
                        resolve(res.data);
                    } else if (res.data.code === 401 || res.data.code === 40101 || res.data.code === 40102) {
                        // Token失效，重新登录
                        uni.removeStorageSync('token');
                        uni.removeStorageSync('openid');
                        uni.reLaunch({
                            url: '/pages/index/index'
                        });
                        reject(res.data);
                    } else {
                        // 对于429等错误，显示更明确的提示
                        const errorMessage = res.data.message || '请求失败';
                        uni.showToast({
                            title: errorMessage,
                            icon: 'none',
                            duration: 3000  // 显示3秒，确保用户能看到
                        });
                        reject(res.data);
                    }
                } else {
                    uni.showToast({
                        title: '网络错误',
                        icon: 'none'
                    });
                    reject(res);
                }
            },
            fail: (err) => {
                uni.showToast({
                    title: '网络错误',
                    icon: 'none'
                });
                reject(err);
            }
        });
    });
};

export default request;


