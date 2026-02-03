/**
 * 请求工具
 */
import apiConfig from '@/common/config/api-config.js';

const baseUrl = apiConfig.BASE_URL;

// 是否正在重新登录，防止重复登录
let isRelogging = false;

/**
 * 重新登录
 */
async function reLogin() {
    if (isRelogging) {
        // 如果正在重新登录，等待完成
        return new Promise((resolve) => {
            const checkInterval = setInterval(() => {
                if (!isRelogging) {
                    clearInterval(checkInterval);
                    resolve();
                }
            }, 100);
        });
    }
    
    isRelogging = true;
    try {
        // 清除旧的token
        uni.removeStorageSync('token');
        uni.removeStorageSync('openid');
        
        // 获取新的code并登录
        const code = await new Promise((resolve, reject) => {
            uni.login({
                provider: 'weixin',
                success: (res) => {
                    resolve(res.code);
                },
                fail: reject
            });
        });
        
        const res = await uni.request({
            url: `${baseUrl}/auth/wx-login`,
            method: 'POST',
            data: { code }
        });
        
        if (res.data && res.data.code === 200) {
            uni.setStorageSync('token', res.data.data.token);
            uni.setStorageSync('openid', res.data.data.openid);
            console.log('重新登录成功，token已更新');
            return true;
        } else {
            console.warn('重新登录失败:', res.data);
            return false;
        }
    } catch (e) {
        console.error('重新登录失败', e);
        return false;
    } finally {
        isRelogging = false;
    }
}

const request = (options) => {
    return new Promise(async (resolve, reject) => {
        // 如果是登录接口，不需要token
        const isLoginRequest = options.url === '/auth/wx-login';
        
        const token = uni.getStorageSync('token');
        
        uni.request({
            url: baseUrl + options.url,
            method: options.method || 'GET',
            data: options.data || {},
            header: {
                'Content-Type': 'application/json',
                'Authorization': (token && !isLoginRequest) ? `Bearer ${token}` : ''
            },
            success: async (res) => {
                if (res.statusCode === 200) {
                    if (res.data.code === 200) {
                        resolve(res.data);
                    } else if (res.data.code === 401 || res.data.code === 40101 || res.data.code === 40102) {
                        // Token失效，尝试重新登录
                        console.log('Token失效，尝试重新登录...');
                        
                        // 如果是登录接口本身返回401，直接拒绝
                        if (isLoginRequest) {
                            uni.removeStorageSync('token');
                            uni.removeStorageSync('openid');
                            reject(res.data);
                            return;
                        }
                        
                        // 尝试重新登录
                        const loginSuccess = await reLogin();
                        
                        if (loginSuccess) {
                            // 重新登录成功，重试原请求
                            console.log('重新登录成功，重试原请求...');
                            try {
                                const retryRes = await request(options);
                                resolve(retryRes);
                            } catch (retryErr) {
                                reject(retryErr);
                            }
                        } else {
                            // 重新登录失败，跳转到首页
                            uni.showToast({
                                title: '登录已过期，请重新打开小程序',
                                icon: 'none',
                                duration: 2000
                            });
                            setTimeout(() => {
                                uni.reLaunch({
                                    url: '/pages/index/index'
                                });
                            }, 2000);
                            reject(res.data);
                        }
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

