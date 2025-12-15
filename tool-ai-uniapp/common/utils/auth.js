/**
 * 认证工具
 */
import apiConfig from '@/common/config/api-config.js';

/**
 * 检查并获取用户授权
 */
export const checkUserAuth = () => {
    return new Promise((resolve, reject) => {
        // #ifdef MP-WEIXIN
        uni.getSetting({
            success: (res) => {
                if (res.authSetting['scope.userInfo']) {
                    // 已授权
                    resolve(true);
                } else {
                    // 未授权，需要用户点击授权按钮
                    reject(new Error('需要授权'));
                }
            },
            fail: reject
        });
        // #endif

        // #ifndef MP-WEIXIN
        resolve(true);
        // #endif
    });
};

/**
 * 获取用户信息并解密
 */
export const getUserInfoAndDecrypt = () => {
    return new Promise((resolve, reject) => {
        // #ifdef MP-WEIXIN
        uni.getUserProfile({
            desc: '用于完善用户资料',
            success: async (res) => {
                try {
                    // 获取session_key（需要从后端获取，这里简化处理）
                    const code = await getWxCode();
                    const loginRes = await uni.request({
                        url: `${apiConfig.BASE_URL}/auth/wx-login`,
                        method: 'POST',
                        data: { code }
                    });

                    if (loginRes.data.code === 200) {
                        const sessionKey = loginRes.data.data.sessionKey; // 注意：实际不应该返回sessionKey给前端
                        // 调用解密接口
                        const decryptRes = await uni.request({
                            url: `${apiConfig.BASE_URL}/auth/decrypt-userinfo`,
                            method: 'POST',
                            header: {
                                'Authorization': `Bearer ${uni.getStorageSync('token')}`
                            },
                            data: {
                                encryptedData: res.encryptedData,
                                iv: res.iv,
                                sessionKey: sessionKey
                            }
                        });

                        if (decryptRes.data.code === 200) {
                            resolve(res.userInfo);
                        } else {
                            reject(new Error('解密失败'));
                        }
                    } else {
                        reject(new Error('登录失败'));
                    }
                } catch (e) {
                    reject(e);
                }
            },
            fail: reject
        });
        // #endif

        // #ifndef MP-WEIXIN
        resolve({});
        // #endif
    });
};

const getWxCode = () => {
    return new Promise((resolve, reject) => {
        uni.login({
            provider: 'weixin',
            success: (res) => {
                resolve(res.code);
            },
            fail: reject
        });
    });
};


