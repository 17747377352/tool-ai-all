"use strict";
const common_vendor = require("../vendor.js");
const common_config_apiConfig = require("../config/api-config.js");
const baseUrl = common_config_apiConfig.apiConfig.BASE_URL;
let isRelogging = false;
async function reLogin() {
  if (isRelogging) {
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
    common_vendor.index.removeStorageSync("token");
    common_vendor.index.removeStorageSync("openid");
    const code = await new Promise((resolve, reject) => {
      common_vendor.index.login({
        provider: "weixin",
        success: (res2) => {
          resolve(res2.code);
        },
        fail: reject
      });
    });
    const res = await common_vendor.index.request({
      url: `${baseUrl}/auth/wx-login`,
      method: "POST",
      data: { code }
    });
    if (res.data && res.data.code === 200) {
      common_vendor.index.setStorageSync("token", res.data.data.token);
      common_vendor.index.setStorageSync("openid", res.data.data.openid);
      common_vendor.index.__f__("log", "at common/utils/request.js:53", "重新登录成功，token已更新");
      return true;
    } else {
      common_vendor.index.__f__("warn", "at common/utils/request.js:56", "重新登录失败:", res.data);
      return false;
    }
  } catch (e) {
    common_vendor.index.__f__("error", "at common/utils/request.js:60", "重新登录失败", e);
    return false;
  } finally {
    isRelogging = false;
  }
}
const request = (options) => {
  return new Promise(async (resolve, reject) => {
    const isLoginRequest = options.url === "/auth/wx-login";
    const token = common_vendor.index.getStorageSync("token");
    common_vendor.index.request({
      url: baseUrl + options.url,
      method: options.method || "GET",
      data: options.data || {},
      header: {
        "Content-Type": "application/json",
        "Authorization": token && !isLoginRequest ? `Bearer ${token}` : ""
      },
      success: async (res) => {
        if (res.statusCode === 200) {
          if (res.data.code === 200) {
            resolve(res.data);
          } else if (res.data.code === 401 || res.data.code === 40101 || res.data.code === 40102) {
            common_vendor.index.__f__("log", "at common/utils/request.js:88", "Token失效，尝试重新登录...");
            if (isLoginRequest) {
              common_vendor.index.removeStorageSync("token");
              common_vendor.index.removeStorageSync("openid");
              reject(res.data);
              return;
            }
            const loginSuccess = await reLogin();
            if (loginSuccess) {
              common_vendor.index.__f__("log", "at common/utils/request.js:103", "重新登录成功，重试原请求...");
              try {
                const retryRes = await request(options);
                resolve(retryRes);
              } catch (retryErr) {
                reject(retryErr);
              }
            } else {
              common_vendor.index.showToast({
                title: "登录已过期，请重新打开小程序",
                icon: "none",
                duration: 2e3
              });
              setTimeout(() => {
                common_vendor.index.reLaunch({
                  url: "/pages/index/index"
                });
              }, 2e3);
              reject(res.data);
            }
          } else {
            const errorMessage = res.data.message || "请求失败";
            common_vendor.index.showToast({
              title: errorMessage,
              icon: "none",
              duration: 3e3
              // 显示3秒，确保用户能看到
            });
            reject(res.data);
          }
        } else {
          common_vendor.index.showToast({
            title: "网络错误",
            icon: "none"
          });
          reject(res);
        }
      },
      fail: (err) => {
        common_vendor.index.showToast({
          title: "网络错误",
          icon: "none"
        });
        reject(err);
      }
    });
  });
};
exports.request = request;
//# sourceMappingURL=../../../.sourcemap/mp-weixin/common/utils/request.js.map
