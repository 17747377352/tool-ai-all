"use strict";
const common_vendor = require("../../common/vendor.js");
const common_utils_auth = require("../../common/utils/auth.js");
const _sfc_main = {
  onLoad() {
  },
  methods: {
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
        "translate": "/pages/translate/translate",
        "old-photo": "/pages/old-photo/old-photo",
        "image-generate": "/pages/image-generate/image-generate",
        "image-recognition": "/pages/image-recognition/image-recognition",
        "mongolian-chat": "/pages/mongolian-chat/mongolian-chat"
      };
      common_vendor.index.navigateTo({
        url: pages[toolName]
      });
    },
    navigateToFeedback() {
      common_vendor.index.navigateTo({
        url: "/pages/feedback/feedback"
      });
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return {
    a: common_vendor.o(($event) => $options.navigateToTool("translate")),
    b: common_vendor.o(($event) => $options.navigateToTool("old-photo")),
    c: common_vendor.o(($event) => $options.navigateToTool("image-generate")),
    d: common_vendor.o(($event) => $options.navigateToTool("image-recognition")),
    e: common_vendor.o(($event) => $options.navigateToTool("mongolian-chat")),
    f: common_vendor.o((...args) => $options.navigateToFeedback && $options.navigateToFeedback(...args))
  };
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-1cf27b2a"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/index/index.js.map
