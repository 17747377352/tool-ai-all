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
      date: "",
      loading: false,
      fortune: null
    };
  },
  onLoad() {
    const d = /* @__PURE__ */ new Date();
    const y = d.getFullYear();
    const m = d.getMonth() + 1;
    const day = d.getDate();
    this.date = `${y}-${m}-${day}`;
  },
  methods: {
    onDateChange(e) {
      this.date = e.detail.value;
    },
    async generate() {
      if (!this.date) {
        common_vendor.index.showToast({
          title: "请选择日期",
          icon: "none"
        });
        return;
      }
      this.loading = true;
      try {
        const res = await common_utils_api.api.generateFortune({
          date: this.date
        });
        if (res.code === 200) {
          const url = res.data && res.data.resultUrl;
          if (url) {
            common_vendor.index.navigateTo({
              url: `/pages/result/result?type=4&resultUrl=${encodeURIComponent(url)}`
            });
          } else {
            common_vendor.index.showToast({
              title: "生成失败，请稍后重试",
              icon: "none"
            });
          }
        } else {
          common_vendor.index.showToast({
            title: res.message || "查询失败",
            icon: "none"
          });
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/fortune/fortune.vue:87", "查询失败", e);
        common_vendor.index.showToast({
          title: "查询失败，请稍后重试",
          icon: "none"
        });
      } finally {
        this.loading = false;
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
    a: common_vendor.t($data.date || "请选择日期"),
    b: !$data.date ? 1 : "",
    c: $data.date,
    d: common_vendor.o((...args) => $options.onDateChange && $options.onDateChange(...args)),
    e: common_vendor.t($data.loading ? "生成中..." : "生成今日运势卡片"),
    f: $data.loading,
    g: common_vendor.o((...args) => $options.generate && $options.generate(...args))
  };
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-6527d2e1"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/fortune/fortune.js.map
