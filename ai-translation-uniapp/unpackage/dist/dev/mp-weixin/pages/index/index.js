"use strict";
const common_vendor = require("../../common/vendor.js");
const common_utils_api = require("../../common/utils/api.js");
const common_utils_auth = require("../../common/utils/auth.js");
const _sfc_main = {
  data() {
    return {
      functionList: [],
      loading: false
    };
  },
  onLoad() {
    this.loadFunctionList();
  },
  onShow() {
    this.loadFunctionList();
  },
  methods: {
    async loadFunctionList() {
      this.loading = true;
      try {
        const res = await common_utils_api.api.getFunctionList();
        if (res.code === 200 && res.data) {
          this.functionList = res.data;
        } else {
          common_vendor.index.__f__("error", "at pages/index/index.vue:59", "获取功能列表失败", res.message);
          this.functionList = this.getDefaultFunctionList();
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/index/index.vue:64", "获取功能列表异常", e);
        this.functionList = this.getDefaultFunctionList();
      } finally {
        this.loading = false;
      }
    },
    getDefaultFunctionList() {
      return [
        { type: 1, name: "去水印", route: "/pages/watermark-removal/watermark-removal" },
        { type: 2, name: "生成图片", route: "/pages/image-generate/image-generate" },
        { type: 3, name: "老照片修复", route: "/pages/old-photo/old-photo" },
        { type: 4, name: "AI识图+翻译", route: "/pages/image-recognition/image-recognition" },
        { type: 5, name: "即时翻译", route: "/pages/translate/translate" },
        { type: 6, name: "蒙古语AI对话", route: "/pages/mongolian-chat/mongolian-chat" }
      ];
    },
    getFunctionIcon(type) {
      const iconMap = {
        1: "💧",
        // 去水印
        2: "✨",
        // 生成图片
        3: "📸",
        // 老照片修复
        4: "👁️",
        // AI识图+翻译
        5: "🌐",
        // 即时翻译
        6: "💬"
        // 蒙古语AI对话
      };
      return iconMap[type] || "📱";
    },
    async navigateToFunction(func) {
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
                this.goToFunction(func);
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
      this.goToFunction(func);
    },
    goToFunction(func) {
      if (!func || !func.route) {
        common_vendor.index.showToast({
          title: "功能路由不存在",
          icon: "none"
        });
        return;
      }
      common_vendor.index.navigateTo({
        url: func.route
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
  return common_vendor.e({
    a: common_vendor.f($data.functionList, (func, index, i0) => {
      return {
        a: common_vendor.t($options.getFunctionIcon(func.type)),
        b: common_vendor.t(func.name),
        c: index,
        d: common_vendor.o(($event) => $options.navigateToFunction(func), index)
      };
    }),
    b: $data.loading
  }, $data.loading ? {} : {}, {
    c: common_vendor.o((...args) => $options.navigateToFeedback && $options.navigateToFeedback(...args))
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-1cf27b2a"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/index/index.js.map
