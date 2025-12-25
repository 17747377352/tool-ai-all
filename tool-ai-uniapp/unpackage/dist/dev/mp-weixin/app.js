"use strict";
Object.defineProperty(exports, Symbol.toStringTag, { value: "Module" });
const common_vendor = require("./common/vendor.js");
const common_config_apiConfig = require("./common/config/api-config.js");
if (!Math) {
  "./pages/index/index.js";
  "./pages/remove-logo/remove-logo.js";
  "./pages/ai-avatar/ai-avatar.js";
  "./pages/name-sign/name-sign.js";
  "./pages/fortune/fortune.js";
  "./pages/constellation/constellation.js";
  "./pages/constellation/constellation-detail.js";
  "./pages/result/result.js";
  "./pages/old-photo/old-photo.js";
  "./pages/life/life.js";
  "./pages/user/user.js";
}
const _sfc_main = {
  __name: "App",
  setup(__props) {
    common_vendor.onLaunch(() => {
      common_vendor.index.__f__("log", "at App.vue:11", "App Launch");
      initLogin();
    });
    common_vendor.onShow(() => {
      common_vendor.index.__f__("log", "at App.vue:17", "App Show");
    });
    common_vendor.onHide(() => {
      common_vendor.index.__f__("log", "at App.vue:21", "App Hide");
    });
    async function initLogin() {
      try {
        const existingToken = common_vendor.index.getStorageSync("token");
        if (existingToken) {
          common_vendor.index.__f__("log", "at App.vue:30", "已有token，跳过登录");
          return;
        }
        const code = await getWxCode();
        const res = await common_vendor.index.request({
          url: `${common_config_apiConfig.apiConfig.BASE_URL}/auth/wx-login`,
          method: "POST",
          data: { code }
        });
        if (res.data && res.data.code === 200) {
          common_vendor.index.setStorageSync("token", res.data.data.token);
          common_vendor.index.setStorageSync("openid", res.data.data.openid);
          common_vendor.index.__f__("log", "at App.vue:43", "登录成功，token已保存");
        } else {
          common_vendor.index.__f__("warn", "at App.vue:45", "登录失败:", res.data);
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at App.vue:48", "登录失败", e);
      }
    }
    function getWxCode() {
      return new Promise((resolve, reject) => {
        common_vendor.index.login({
          provider: "weixin",
          success: (res) => {
            resolve(res.code);
          },
          fail: reject
        });
      });
    }
    return (_ctx, _cache) => {
      return {};
    };
  }
};
function createApp() {
  const app = common_vendor.createSSRApp(_sfc_main);
  return {
    app
  };
}
createApp().app.mount("#app");
exports.createApp = createApp;
//# sourceMappingURL=../.sourcemap/mp-weixin/app.js.map
