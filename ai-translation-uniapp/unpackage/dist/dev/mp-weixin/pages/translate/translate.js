"use strict";
const common_vendor = require("../../common/vendor.js");
const common_utils_api = require("../../common/utils/api.js");
const _sfc_main = {
  data() {
    return {
      from: "zh",
      // 源语言
      to: "en",
      // 目标语言
      text: "",
      result: "",
      translating: false,
      sourceLanguages: [
        { label: "中文", value: "zh" },
        { label: "英文", value: "en" },
        { label: "日文", value: "ja" },
        { label: "蒙文", value: "mo" }
      ],
      targetLanguages: []
    };
  },
  watch: {
    from(newVal) {
      this.updateTargetLanguages(newVal);
      if (this.to === newVal) {
        const available = this.targetLanguages.filter((l) => l.value !== newVal);
        if (available.length > 0) {
          this.to = available[0].value;
        }
      }
    }
  },
  mounted() {
    this.updateTargetLanguages(this.from);
  },
  methods: {
    updateTargetLanguages(sourceLang) {
      const allLanguages = [
        { label: "中文", value: "zh" },
        { label: "英文", value: "en" },
        { label: "日文", value: "ja" },
        { label: "蒙文", value: "mo" }
      ];
      this.targetLanguages = allLanguages.filter((lang) => lang.value !== sourceLang);
    },
    async translate() {
      if (!this.text.trim()) {
        common_vendor.index.showToast({
          title: "请输入要翻译的文本",
          icon: "none"
        });
        return;
      }
      this.translating = true;
      try {
        const res = await common_utils_api.api.translate({
          text: this.text,
          from: this.from,
          to: this.to
        });
        if (res.code === 200) {
          this.result = res.data.result;
        } else {
          common_vendor.index.showToast({
            title: res.message || "翻译失败",
            icon: "none"
          });
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/translate/translate.vue:140", "翻译失败", e);
        common_vendor.index.showToast({
          title: "翻译失败，请重试",
          icon: "none"
        });
      } finally {
        this.translating = false;
      }
    },
    copyResult() {
      if (!this.result) {
        return;
      }
      common_vendor.index.setClipboardData({
        data: this.result,
        success: () => {
          common_vendor.index.showToast({
            title: "已复制到剪贴板",
            icon: "success"
          });
        }
      });
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.f($data.sourceLanguages, (lang, k0, i0) => {
      return {
        a: common_vendor.t(lang.label),
        b: lang.value,
        c: $data.from === lang.value ? 1 : "",
        d: common_vendor.o(($event) => $data.from = lang.value, lang.value)
      };
    }),
    b: common_vendor.f($data.targetLanguages, (lang, k0, i0) => {
      return {
        a: common_vendor.t(lang.label),
        b: lang.value,
        c: $data.to === lang.value ? 1 : "",
        d: common_vendor.o(($event) => $data.to = lang.value, lang.value)
      };
    }),
    c: $data.text,
    d: common_vendor.o(($event) => $data.text = $event.detail.value),
    e: common_vendor.t($data.translating ? "翻译中..." : "立即翻译"),
    f: !$data.text || !$data.from || !$data.to || $data.translating,
    g: common_vendor.o((...args) => $options.translate && $options.translate(...args)),
    h: $data.result
  }, $data.result ? {
    i: common_vendor.t($data.result),
    j: common_vendor.o((...args) => $options.copyResult && $options.copyResult(...args))
  } : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-b2af8d61"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/translate/translate.js.map
