"use strict";
Object.defineProperty(exports, Symbol.toStringTag, { value: "Module" });
const common_vendor = require("./common/vendor.js");
const common_config_apiConfig = require("./common/config/api-config.js");
if (!Math) {
  "./pages/index/index.js";
  "./pages/translate/translate.js";
  "./pages/old-photo/old-photo.js";
  "./pages/image-to-image/image-to-image.js";
  "./pages/image-generate/image-generate.js";
  "./pages/image-generate/template-list.js";
  "./pages/image-generate/task-list.js";
  "./pages/image-recognition/image-recognition.js";
  "./pages/watermark-removal/watermark-removal.js";
  "./pages/mongolian-chat/mongolian-chat.js";
  "./pages/result/result.js";
  "./pages/user/user.js";
  "./pages/feedback/feedback.js";
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
    async function doLogin() {
      try {
        const code = await getWxCode();
        const res = await common_vendor.index.request({
          url: `${common_config_apiConfig.apiConfig.BASE_URL}/auth/wx-login`,
          method: "POST",
          data: { code }
        });
        if (res.data && res.data.code === 200) {
          common_vendor.index.setStorageSync("token", res.data.data.token);
          common_vendor.index.setStorageSync("openid", res.data.data.openid);
          common_vendor.index.__f__("log", "at App.vue:35", "登录成功，token已保存");
          return true;
        } else {
          common_vendor.index.__f__("warn", "at App.vue:38", "登录失败:", res.data);
          return false;
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at App.vue:42", "登录失败", e);
        return false;
      }
    }
    async function initLogin() {
      common_vendor.index.__f__("log", "at App.vue:50", "开始初始化登录...");
      await doLogin();
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
