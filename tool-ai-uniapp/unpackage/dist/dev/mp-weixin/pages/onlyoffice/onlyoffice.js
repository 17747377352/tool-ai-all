"use strict";
const common_vendor = require("../../common/vendor.js");
const common_config_apiConfig = require("../../common/config/api-config.js");
const _sfc_main = {
  data() {
    return {
      fileName: "demo.docx",
      documentServerUrl: "http://127.0.0.1:29000",
      // OnlyOffice Document Server 地址
      editorUrl: ""
    };
  },
  onLoad(options) {
    if (options.fileName) {
      this.fileName = decodeURIComponent(options.fileName);
    }
    const baseUrl = common_config_apiConfig.apiConfig.BASE_URL;
    const configUrl = `${baseUrl}/onlyoffice/config?fileName=${encodeURIComponent(this.fileName)}`;
    const editorPageUrl = `${baseUrl}/onlyoffice-editor.html`;
    this.editorUrl = `${editorPageUrl}?fileName=${encodeURIComponent(this.fileName)}&configUrl=${encodeURIComponent(configUrl)}&documentServerUrl=${encodeURIComponent(this.documentServerUrl)}`;
    common_vendor.index.__f__("log", "at pages/onlyoffice/onlyoffice.vue:36", "编辑器 URL:", this.editorUrl);
  },
  methods: {
    handleMessage(event) {
      common_vendor.index.__f__("log", "at pages/onlyoffice/onlyoffice.vue:40", "收到 web-view 消息:", event.detail.data);
    },
    handleError(event) {
      common_vendor.index.__f__("error", "at pages/onlyoffice/onlyoffice.vue:43", "web-view 加载错误:", event);
      common_vendor.index.showToast({
        title: "编辑器加载失败",
        icon: "none",
        duration: 3e3
      });
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return {
    a: $data.editorUrl,
    b: common_vendor.o((...args) => $options.handleMessage && $options.handleMessage(...args)),
    c: common_vendor.o((...args) => $options.handleError && $options.handleError(...args))
  };
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-4cdab4b8"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/onlyoffice/onlyoffice.js.map
