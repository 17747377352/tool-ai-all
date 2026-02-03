"use strict";
const common_vendor = require("../vendor.js");
const common_config_apiConfig = require("../config/api-config.js");
const checkUserAuth = () => {
  return new Promise((resolve, reject) => {
    common_vendor.index.getSetting({
      success: (res) => {
        if (res.authSetting["scope.userInfo"]) {
          resolve(true);
        } else {
          reject(new Error("需要授权"));
        }
      },
      fail: reject
    });
  });
};
const getUserInfoAndDecrypt = () => {
  return new Promise((resolve, reject) => {
    common_vendor.index.getUserProfile({
      desc: "用于完善用户资料",
      success: async (res) => {
        try {
          const code = await getWxCode();
          const loginRes = await common_vendor.index.request({
            url: `${common_config_apiConfig.apiConfig.BASE_URL}/auth/wx-login`,
            method: "POST",
            data: { code }
          });
          if (loginRes.data.code === 200) {
            const sessionKey = loginRes.data.data.sessionKey;
            const decryptRes = await common_vendor.index.request({
              url: `${common_config_apiConfig.apiConfig.BASE_URL}/auth/decrypt-userinfo`,
              method: "POST",
              header: {
                "Authorization": `Bearer ${common_vendor.index.getStorageSync("token")}`
              },
              data: {
                encryptedData: res.encryptedData,
                iv: res.iv,
                sessionKey
              }
            });
            if (decryptRes.data.code === 200) {
              resolve(res.userInfo);
            } else {
              reject(new Error("解密失败"));
            }
          } else {
            reject(new Error("登录失败"));
          }
        } catch (e) {
          reject(e);
        }
      },
      fail: reject
    });
  });
};
const getWxCode = () => {
  return new Promise((resolve, reject) => {
    common_vendor.index.login({
      provider: "weixin",
      success: (res) => {
        resolve(res.code);
      },
      fail: reject
    });
  });
};
exports.checkUserAuth = checkUserAuth;
exports.getUserInfoAndDecrypt = getUserInfoAndDecrypt;
//# sourceMappingURL=../../../.sourcemap/mp-weixin/common/utils/auth.js.map
