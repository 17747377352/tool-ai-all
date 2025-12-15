"use strict";
const common_vendor = require("../../common/vendor.js");
const AdVideoBanner = () => "../../common/components/ad-video-banner.js";
const _sfc_main = {
  components: {
    AdVideoBanner
  },
  data() {
    return {
      userInfo: {
        avatar: "",
        nickname: ""
      },
      totalCount: 0,
      todayCount: 0
    };
  },
  onLoad() {
    this.loadUserInfo();
    this.loadStats();
  },
  methods: {
    loadUserInfo() {
      const openid = common_vendor.index.getStorageSync("openid");
      const nickname = common_vendor.index.getStorageSync("nickname");
      const avatar = common_vendor.index.getStorageSync("avatar");
      this.userInfo = {
        openid,
        nickname,
        avatar
      };
    },
    loadStats() {
      this.totalCount = 0;
      this.todayCount = 0;
    },
    viewHistory() {
      common_vendor.index.showToast({
        title: "功能开发中",
        icon: "none"
      });
    },
    viewFavorites() {
      common_vendor.index.showToast({
        title: "功能开发中",
        icon: "none"
      });
    }
  }
};
if (!Array) {
  const _component_ad_video_banner = common_vendor.resolveComponent("ad-video-banner");
  _component_ad_video_banner();
}
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: $data.userInfo.avatar
  }, $data.userInfo.avatar ? {
    b: $data.userInfo.avatar
  } : {}, {
    c: common_vendor.t($data.userInfo.nickname || "未设置昵称"),
    d: common_vendor.t($data.totalCount),
    e: common_vendor.t($data.todayCount),
    f: common_vendor.o((...args) => $options.viewHistory && $options.viewHistory(...args)),
    g: common_vendor.o((...args) => $options.viewFavorites && $options.viewFavorites(...args))
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-0f7520f0"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/user/user.js.map
