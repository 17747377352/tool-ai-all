"use strict";
const common_vendor = require("../vendor.js");
const common_config_apiConfig = require("../config/api-config.js");
const baseUrl = common_config_apiConfig.apiConfig.BASE_URL;
const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = common_vendor.index.getStorageSync("token");
    common_vendor.index.request({
      url: baseUrl + options.url,
      method: options.method || "GET",
      data: options.data || {},
      header: {
        "Content-Type": "application/json",
        "Authorization": token ? `Bearer ${token}` : ""
      },
      success: (res) => {
        if (res.statusCode === 200) {
          if (res.data.code === 200) {
            resolve(res.data);
          } else if (res.data.code === 401 || res.data.code === 40101 || res.data.code === 40102) {
            common_vendor.index.removeStorageSync("token");
            common_vendor.index.removeStorageSync("openid");
            common_vendor.index.reLaunch({
              url: "/pages/index/index"
            });
            reject(res.data);
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
