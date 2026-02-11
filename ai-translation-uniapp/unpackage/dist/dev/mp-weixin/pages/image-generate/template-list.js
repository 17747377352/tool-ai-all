"use strict";
const common_vendor = require("../../common/vendor.js");
const common_utils_api = require("../../common/utils/api.js");
const _sfc_main = {
  data() {
    return {
      templates: [],
      loading: true
    };
  },
  onLoad() {
    this.loadTemplates();
  },
  methods: {
    async loadTemplates() {
      try {
        const res = await common_utils_api.api.getTemplates();
        if (res.code === 200) {
          this.templates = res.data;
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/image-generate/template-list.vue:44", "加载模版失败", e);
        common_vendor.index.showToast({
          title: "加载失败",
          icon: "none"
        });
      } finally {
        this.loading = false;
      }
    },
    selectTemplate(template) {
      const app = getApp();
      if (app) {
        app.globalData = app.globalData || {};
        app.globalData.selectedTemplate = template;
      }
      common_vendor.index.setStorageSync("selectedTemplate", template);
      common_vendor.index.navigateBack();
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: $data.loading
  }, $data.loading ? {} : {
    b: common_vendor.f($data.templates, (template, k0, i0) => {
      return {
        a: template.imageUrl,
        b: common_vendor.t(template.name),
        c: common_vendor.t(template.description),
        d: template.id,
        e: common_vendor.o(($event) => $options.selectTemplate(template), template.id)
      };
    })
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-b4d15225"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/image-generate/template-list.js.map
