"use strict";
const common_vendor = require("../../common/vendor.js");
const common_utils_api = require("../../common/utils/api.js");
const common_utils_ossUpload = require("../../common/utils/oss-upload.js");
const _sfc_main = {
  data() {
    return {
      imageUrl: "",
      imageTempPath: "",
      prompt: "",
      selectedStyle: "realistic",
      styles: [
        { label: "写实", value: "realistic" },
        { label: "卡通", value: "cartoon" },
        { label: "动漫", value: "anime" },
        { label: "油画", value: "oil-painting" }
      ],
      generating: false
    };
  },
  methods: {
    chooseImage() {
      common_vendor.index.chooseImage({
        count: 1,
        sizeType: ["compressed"],
        sourceType: ["album", "camera"],
        success: (res) => {
          this.imageTempPath = res.tempFilePaths[0];
          this.imageUrl = this.imageTempPath;
          this.uploadImage();
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/image-to-image/image-to-image.vue:84", "选择图片失败", err);
          common_vendor.index.showToast({
            title: "选择图片失败",
            icon: "none"
          });
        }
      });
    },
    async uploadImage() {
      if (!this.imageTempPath) {
        return;
      }
      common_vendor.index.showLoading({
        title: "上传中..."
      });
      try {
        const imageUrl = await common_utils_ossUpload.uploadToOss(this.imageTempPath);
        this.imageUrl = imageUrl;
        common_vendor.index.hideLoading();
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/image-to-image/image-to-image.vue:109", "上传图片失败", e);
        common_vendor.index.hideLoading();
        common_vendor.index.showToast({
          title: e.message || "上传图片失败",
          icon: "none"
        });
        this.imageUrl = "";
        this.imageTempPath = "";
      }
    },
    async generate() {
      if (!this.prompt.trim()) {
        common_vendor.index.showToast({
          title: "请输入提示词",
          icon: "none"
        });
        return;
      }
      if (!this.imageUrl) {
        common_vendor.index.showToast({
          title: "请先上传图片",
          icon: "none"
        });
        return;
      }
      this.generating = true;
      try {
        const res = await common_utils_api.api.generateAiAvatar({
          prompt: this.prompt,
          imageUrl: this.imageUrl,
          style: this.selectedStyle
        });
        if (res.code === 200) {
          common_vendor.index.navigateTo({
            url: `/pages/result/result?type=2&resultUrl=${encodeURIComponent(res.data.resultUrl)}`
          });
        } else {
          common_vendor.index.showToast({
            title: res.message || "生成失败",
            icon: "none"
          });
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/image-to-image/image-to-image.vue:156", "生成失败", e);
        common_vendor.index.showToast({
          title: "生成失败，请重试",
          icon: "none"
        });
      } finally {
        this.generating = false;
      }
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: $data.imageUrl
  }, $data.imageUrl ? {
    b: $data.imageUrl
  } : {}, {
    c: common_vendor.o((...args) => $options.chooseImage && $options.chooseImage(...args)),
    d: $data.prompt,
    e: common_vendor.o(($event) => $data.prompt = $event.detail.value),
    f: common_vendor.f($data.styles, (style, k0, i0) => {
      return {
        a: common_vendor.t(style.label),
        b: style.value,
        c: $data.selectedStyle === style.value ? 1 : "",
        d: common_vendor.o(($event) => $data.selectedStyle = style.value, style.value)
      };
    }),
    g: common_vendor.t($data.generating ? "生成中..." : "立即生成"),
    h: !$data.prompt || !$data.imageUrl || $data.generating,
    i: common_vendor.o((...args) => $options.generate && $options.generate(...args))
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-61b22b46"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/image-to-image/image-to-image.js.map
