"use strict";
const common_vendor = require("../../common/vendor.js");
const common_utils_api = require("../../common/utils/api.js");
const AdVideoBanner = () => "../../common/components/ad-video-banner.js";
const _sfc_main = {
  components: {
    AdVideoBanner
  },
  data() {
    return {
      generatingConstellation: "",
      constellations: [
        { name: "白羊座", icon: "♈", date: "3.21-4.19" },
        { name: "金牛座", icon: "♉", date: "4.20-5.20" },
        { name: "双子座", icon: "♊", date: "5.21-6.21" },
        { name: "巨蟹座", icon: "♋", date: "6.22-7.22" },
        { name: "狮子座", icon: "♌", date: "7.23-8.22" },
        { name: "处女座", icon: "♍", date: "8.23-9.22" },
        { name: "天秤座", icon: "♎", date: "9.23-10.23" },
        { name: "天蝎座", icon: "♏", date: "10.24-11.22" },
        { name: "射手座", icon: "♐", date: "11.23-12.21" },
        { name: "摩羯座", icon: "♑", date: "12.22-1.19" },
        { name: "水瓶座", icon: "♒", date: "1.20-2.18" },
        { name: "双鱼座", icon: "♓", date: "2.19-3.20" }
      ]
    };
  },
  methods: {
    async selectConstellation(constellation) {
      if (this.generatingConstellation) {
        return;
      }
      this.generatingConstellation = constellation;
      try {
        common_vendor.index.showLoading({
          title: "生成中...",
          mask: true
        });
        const res = await common_utils_api.api.generateConstellationFortune({
          constellation
        });
        common_vendor.index.hideLoading();
        if (res.code === 200) {
          common_vendor.index.navigateTo({
            url: `/pages/result/result?type=5&resultUrl=${encodeURIComponent(res.data.resultUrl)}&title=${encodeURIComponent(constellation + "今日运势")}`
          });
        } else {
          common_vendor.index.showToast({
            title: res.message || "生成失败，请稍后再试",
            icon: "none",
            duration: 3e3
          });
        }
      } catch (e) {
        common_vendor.index.hideLoading();
        common_vendor.index.__f__("error", "at pages/constellation/constellation.vue:91", "生成失败", e);
        const errorMessage = e.data && e.data.message || e.message || "生成失败，请稍后再试";
        common_vendor.index.showToast({
          title: errorMessage,
          icon: "none",
          duration: 3e3
        });
      } finally {
        this.generatingConstellation = "";
      }
    }
  }
};
if (!Array) {
  const _component_ad_video_banner = common_vendor.resolveComponent("ad-video-banner");
  _component_ad_video_banner();
}
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return {
    a: common_vendor.f($data.constellations, (item, k0, i0) => {
      return {
        a: common_vendor.t(item.icon),
        b: common_vendor.t(item.name),
        c: common_vendor.t(item.date),
        d: item.name,
        e: $data.generatingConstellation === item.name ? 1 : "",
        f: common_vendor.o(($event) => $options.selectConstellation(item.name), item.name)
      };
    })
  };
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-e8f14bc4"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/constellation/constellation.js.map
