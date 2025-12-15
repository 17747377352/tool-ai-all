"use strict";
const common_vendor = require("../vendor.js");
const common_config_adConfig = require("../config/ad-config.js");
const _sfc_main = {
  name: "AdVideoBanner",
  props: {
    bannerAdUnitId: {
      type: String,
      default: common_config_adConfig.AD_CONFIG.BANNER_AD_UNIT_ID
    }
  },
  data() {
    return {
      showBanner: common_config_adConfig.ENABLE_AD
      // 根据配置决定是否显示
    };
  },
  methods: {
    onBannerLoad() {
      common_vendor.index.__f__("log", "at common/components/ad-video-banner.vue:33", "Banner广告加载成功");
    },
    onBannerError(err) {
      common_vendor.index.__f__("error", "at common/components/ad-video-banner.vue:36", "Banner广告加载失败", err);
      this.showBanner = false;
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: $data.showBanner
  }, $data.showBanner ? {
    b: $props.bannerAdUnitId,
    c: common_vendor.o((...args) => $options.onBannerLoad && $options.onBannerLoad(...args)),
    d: common_vendor.o((...args) => $options.onBannerError && $options.onBannerError(...args))
  } : {});
}
const Component = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-ea9002ef"]]);
wx.createComponent(Component);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/common/components/ad-video-banner.js.map
