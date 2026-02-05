"use strict";
const common_vendor = require("../../common/vendor.js");
const common_utils_api = require("../../common/utils/api.js");
const common_utils_ossUpload = require("../../common/utils/oss-upload.js");
const _sfc_main = {
  data() {
    return {
      imageUrl: "",
      imageTempPath: "",
      recognitionResult: "",
      translationResult: "",
      recognizing: false,
      translating: false,
      // 默认翻译目标语言改为蒙文
      selectedTargetLang: { label: "蒙文", value: "mo" },
      targetLanguages: [
        { label: "蒙文", value: "mo" },
        { label: "英文", value: "en" },
        { label: "日文", value: "ja" },
        { label: "中文", value: "zh" }
      ]
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
          this.recognitionResult = "";
          this.translationResult = "";
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/image-recognition/image-recognition.vue:102", "选择图片失败", err);
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
        common_vendor.index.__f__("error", "at pages/image-recognition/image-recognition.vue:126", "上传图片失败", e);
        common_vendor.index.hideLoading();
        common_vendor.index.showToast({
          title: e.message || "上传图片失败",
          icon: "none"
        });
        this.imageUrl = "";
        this.imageTempPath = "";
      }
    },
    async recognize() {
      if (!this.imageUrl) {
        common_vendor.index.showToast({
          title: "请先上传图片",
          icon: "none"
        });
        return;
      }
      this.recognizing = true;
      this.recognitionResult = "";
      try {
        const res = await common_utils_api.api.imageRecognize({
          imageUrl: this.imageUrl
        });
        if (res.code === 200 && res.data && res.data.text) {
          this.recognitionResult = res.data.text;
        } else {
          common_vendor.index.showToast({
            title: res.message || "识别失败，请重试",
            icon: "none"
          });
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/image-recognition/image-recognition.vue:160", "识别失败", e);
        common_vendor.index.showToast({
          title: "识别失败，请重试",
          icon: "none"
        });
      } finally {
        this.recognizing = false;
      }
    },
    onTargetLangChange(e) {
      this.selectedTargetLang = this.targetLanguages[e.detail.value];
      this.translationResult = "";
    },
    async translate() {
      if (!this.recognitionResult) {
        return;
      }
      this.translating = true;
      try {
        const res = await common_utils_api.api.translate({
          text: this.recognitionResult,
          from: "zh",
          // 假设识别结果是中文
          to: this.selectedTargetLang.value
        });
        if (res.code === 200) {
          this.translationResult = res.data.result;
        } else {
          common_vendor.index.showToast({
            title: res.message || "翻译失败",
            icon: "none"
          });
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/image-recognition/image-recognition.vue:197", "翻译失败", e);
        common_vendor.index.showToast({
          title: "翻译失败，请重试",
          icon: "none"
        });
      } finally {
        this.translating = false;
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
    d: $data.imageUrl
  }, $data.imageUrl ? common_vendor.e({
    e: $data.recognizing
  }, $data.recognizing ? {} : $data.recognitionResult ? {
    g: common_vendor.t($data.recognitionResult)
  } : {}, {
    f: $data.recognitionResult
  }) : {}, {
    h: $data.recognitionResult
  }, $data.recognitionResult ? common_vendor.e({
    i: common_vendor.t($data.selectedTargetLang.label),
    j: $data.targetLanguages,
    k: common_vendor.o((...args) => $options.onTargetLangChange && $options.onTargetLangChange(...args)),
    l: $data.translating
  }, $data.translating ? {} : $data.translationResult ? {
    n: common_vendor.t($data.translationResult)
  } : {}, {
    m: $data.translationResult,
    o: $data.recognitionResult && !$data.translationResult
  }, $data.recognitionResult && !$data.translationResult ? {
    p: common_vendor.t($data.translating ? "翻译中..." : "立即翻译"),
    q: $data.translating,
    r: common_vendor.o((...args) => $options.translate && $options.translate(...args))
  } : {}) : {}, {
    s: $data.imageUrl && !$data.recognitionResult
  }, $data.imageUrl && !$data.recognitionResult ? {
    t: common_vendor.t($data.recognizing ? "识别中..." : "开始识别"),
    v: $data.recognizing,
    w: common_vendor.o((...args) => $options.recognize && $options.recognize(...args))
  } : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-62b32c48"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/image-recognition/image-recognition.js.map
