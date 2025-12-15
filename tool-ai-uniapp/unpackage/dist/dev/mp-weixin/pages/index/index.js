"use strict";
const common_vendor = require("../../common/vendor.js");
const common_utils_auth = require("../../common/utils/auth.js");
const AdVideoBanner = () => "../../common/components/ad-video-banner.js";
const _sfc_main = {
  components: {
    AdVideoBanner
  },
  onLoad() {
  },
  methods: {
    // 登录逻辑已移至App.vue，这里不再需要
    // async initLogin() {
    //     ...
    // },
    getWxCode() {
      return new Promise((resolve, reject) => {
        common_vendor.index.login({
          provider: "weixin",
          success: (res) => {
            resolve(res.code);
          },
          fail: reject
        });
      });
    },
    async navigateToTool(toolName) {
      try {
        await common_utils_auth.checkUserAuth();
      } catch (e) {
        common_vendor.index.showModal({
          title: "需要授权",
          content: "需要获取您的用户信息",
          showCancel: false,
          success: async (modalRes) => {
            if (modalRes.confirm) {
              try {
                await common_utils_auth.getUserInfoAndDecrypt();
                this.goToTool(toolName);
              } catch (err) {
                common_vendor.index.showToast({
                  title: "授权失败",
                  icon: "none"
                });
              }
            }
          }
        });
        return;
      }
      this.goToTool(toolName);
    },
    goToTool(toolName) {
      const pages = {
        "remove-logo": "/pages/remove-logo/remove-logo",
        "ai-avatar": "/pages/ai-avatar/ai-avatar",
        "name-sign": "/pages/name-sign/name-sign",
        "fortune": "/pages/fortune/fortune",
        "constellation": "/pages/constellation/constellation"
      };
      common_vendor.index.navigateTo({
        url: pages[toolName]
      });
    }
  }
};
if (!Array) {
  const _component_ad_video_banner = common_vendor.resolveComponent("ad-video-banner");
  _component_ad_video_banner();
}
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return {
    a: common_vendor.o(($event) => $options.navigateToTool("remove-logo")),
    b: common_vendor.o(($event) => $options.navigateToTool("ai-avatar")),
    c: common_vendor.o(($event) => $options.navigateToTool("name-sign")),
    d: common_vendor.o(($event) => $options.navigateToTool("fortune")),
    e: common_vendor.o(($event) => $options.navigateToTool("constellation"))
  };
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-1cf27b2a"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/index/index.js.map
