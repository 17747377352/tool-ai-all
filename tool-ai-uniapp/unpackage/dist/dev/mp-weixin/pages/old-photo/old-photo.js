"use strict";
const common_vendor = require("../../common/vendor.js");
const common_utils_api = require("../../common/utils/api.js");
const common_config_apiConfig = require("../../common/config/api-config.js");
const AdVideoBanner = () => "../../common/components/ad-video-banner.js";
const _sfc_main = {
  components: {
    AdVideoBanner
  },
  data() {
    return {
      imageUrl: "",
      // OSS 地址
      imageTempPath: "",
      // 本地预览
      strength: 0.7,
      generating: false
    };
  },
  methods: {
    onStrengthChange(e) {
      this.strength = (e.detail.value || 70) / 100;
    },
    chooseImage() {
      common_vendor.index.chooseImage({
        count: 1,
        sizeType: ["original", "compressed"],
        sourceType: ["album", "camera"],
        success: (res) => {
          this.imageTempPath = res.tempFilePaths[0];
          this.imageUrl = this.imageTempPath;
          this.uploadImage();
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/old-photo/old-photo.vue:77", "选择图片失败", err);
          common_vendor.index.showToast({
            title: "选择图片失败",
            icon: "none"
          });
        }
      });
    },
    async uploadImage() {
      if (!this.imageTempPath)
        return;
      common_vendor.index.showLoading({ title: "上传中..." });
      try {
        const token = common_vendor.index.getStorageSync("token");
        if (!token)
          throw new Error("未登录");
        const uploadRes = await new Promise((resolve, reject) => {
          common_vendor.index.uploadFile({
            url: `${common_config_apiConfig.apiConfig.BASE_URL}/tool/upload-image`,
            filePath: this.imageTempPath,
            name: "file",
            header: { "Authorization": "Bearer " + token },
            success: (res) => {
              try {
                const data = JSON.parse(res.data);
                if (data.code === 200) {
                  resolve(data);
                } else {
                  reject(new Error(data.message || "上传失败"));
                }
              } catch (e) {
                reject(new Error("解析响应失败"));
              }
            },
            fail: reject
          });
        });
        if (uploadRes.data && uploadRes.data.imageUrl) {
          this.imageUrl = uploadRes.data.imageUrl;
        }
        common_vendor.index.hideLoading();
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/old-photo/old-photo.vue:119", "上传图片失败", e);
        common_vendor.index.hideLoading();
        common_vendor.index.showToast({
          title: e.message || "上传失败",
          icon: "none"
        });
        this.imageUrl = "";
        this.imageTempPath = "";
      }
    },
    async restore() {
      if (!this.imageUrl) {
        common_vendor.index.showToast({ title: "请先上传照片", icon: "none" });
        return;
      }
      this.generating = true;
      try {
        const res = await common_utils_api.api.restoreOldPhoto({
          imageUrl: this.imageUrl,
          strength: Number(this.strength.toFixed(2))
        });
        if (res.code === 200) {
          common_vendor.index.navigateTo({
            url: `/pages/result/result?type=6&resultUrl=${encodeURIComponent(res.data.resultUrl)}`
          });
        } else {
          common_vendor.index.showToast({
            title: res.message || "修复失败",
            icon: "none"
          });
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/old-photo/old-photo.vue:151", "修复失败", e);
        common_vendor.index.showToast({
          title: e.message || "修复失败，请重试",
          icon: "none"
        });
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
  return common_vendor.e({
    a: $data.imageUrl
  }, $data.imageUrl ? {
    b: $data.imageUrl
  } : {}, {
    c: common_vendor.o((...args) => $options.chooseImage && $options.chooseImage(...args)),
    d: common_vendor.t(($data.strength * 100).toFixed(0)),
    e: $data.strength * 100,
    f: common_vendor.o((...args) => $options.onStrengthChange && $options.onStrengthChange(...args)),
    g: common_vendor.t($data.generating ? "修复中..." : "开始修复"),
    h: !$data.imageUrl || $data.generating,
    i: common_vendor.o((...args) => $options.restore && $options.restore(...args))
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-549edafb"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/old-photo/old-photo.js.map
