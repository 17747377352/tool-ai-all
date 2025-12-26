"use strict";
const common_vendor = require("../../common/vendor.js");
const common_utils_api = require("../../common/utils/api.js");
const _sfc_main = {
  data() {
    return {
      feedbackType: 1,
      // 1-功能建议 2-问题反馈 3-其他
      content: "",
      contact: "",
      submitting: false
    };
  },
  computed: {
    canSubmit() {
      return this.feedbackType > 0 && this.content.trim().length > 0 && !this.submitting;
    }
  },
  methods: {
    async submitFeedback() {
      if (!this.canSubmit) {
        common_vendor.index.showToast({
          title: "请填写反馈内容",
          icon: "none"
        });
        return;
      }
      this.submitting = true;
      try {
        const res = await common_utils_api.api.submitFeedback({
          feedbackType: this.feedbackType,
          content: this.content.trim(),
          contact: this.contact.trim() || null
        });
        if (res.code === 200) {
          common_vendor.index.showToast({
            title: "反馈提交成功",
            icon: "success"
          });
          setTimeout(() => {
            common_vendor.index.navigateBack();
          }, 1500);
        } else {
          common_vendor.index.showToast({
            title: res.message || "提交失败，请重试",
            icon: "none"
          });
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/feedback/feedback.vue:124", "提交反馈失败", e);
        common_vendor.index.showToast({
          title: "提交失败，请检查网络",
          icon: "none"
        });
      } finally {
        this.submitting = false;
      }
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return {
    a: $data.feedbackType === 1 ? 1 : "",
    b: common_vendor.o(($event) => $data.feedbackType = 1),
    c: $data.feedbackType === 2 ? 1 : "",
    d: common_vendor.o(($event) => $data.feedbackType = 2),
    e: $data.feedbackType === 3 ? 1 : "",
    f: common_vendor.o(($event) => $data.feedbackType = 3),
    g: $data.content,
    h: common_vendor.o(($event) => $data.content = $event.detail.value),
    i: common_vendor.t($data.content.length),
    j: $data.contact,
    k: common_vendor.o(($event) => $data.contact = $event.detail.value),
    l: common_vendor.t($data.submitting ? "提交中..." : "提交反馈"),
    m: !$options.canSubmit || $data.submitting,
    n: common_vendor.o((...args) => $options.submitFeedback && $options.submitFeedback(...args))
  };
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-a24b82f2"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/feedback/feedback.js.map
