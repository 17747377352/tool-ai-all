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
      surname: "",
      selectedStyle: "classic",
      styles: [
        { label: "经典", value: "classic" },
        { label: "行书", value: "cursive" },
        { label: "草书", value: "grass" },
        { label: "艺术", value: "artistic" }
      ],
      generating: false
    };
  },
  methods: {
    async generate() {
      if (!this.surname.trim()) {
        common_vendor.index.showToast({
          title: "请输入姓氏",
          icon: "none"
        });
        return;
      }
      this.generating = true;
      try {
        const res = await common_utils_api.api.generateNameSign({
          surname: this.surname,
          style: this.selectedStyle
        });
        if (res.code === 200) {
          common_vendor.index.navigateTo({
            url: `/pages/result/result?type=3&resultUrl=${encodeURIComponent(res.data.resultUrl)}`
          });
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/name-sign/name-sign.vue:80", "生成失败", e);
      } finally {
        this.generating = false;
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
    a: $data.surname,
    b: common_vendor.o(($event) => $data.surname = $event.detail.value),
    c: common_vendor.f($data.styles, (style, k0, i0) => {
      return {
        a: common_vendor.t(style.label),
        b: style.value,
        c: $data.selectedStyle === style.value ? 1 : "",
        d: common_vendor.o(($event) => $data.selectedStyle = style.value, style.value)
      };
    }),
    d: common_vendor.t($data.generating ? "生成中..." : "立即生成"),
    e: !$data.surname || $data.generating,
    f: common_vendor.o((...args) => $options.generate && $options.generate(...args))
  };
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-8f7744cd"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/name-sign/name-sign.js.map
