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
      shareUrl: "",
      // 分享链接
      videoPath: "",
      // 本地视频路径
      generating: false
    };
  },
  methods: {
    /**
     * 分享链接输入处理
     */
    onShareUrlInput(e) {
      this.shareUrl = e.detail.value;
      if (this.shareUrl) {
        this.videoPath = "";
      }
    },
    /**
     * 获取平台名称
     */
    getPlatformName() {
      if (!this.shareUrl) {
        return "";
      }
      if (this.shareUrl.includes("douyin.com") || this.shareUrl.includes("iesdouyin.com")) {
        return "✓ 已识别：抖音";
      } else if (this.shareUrl.includes("xiaohongshu.com")) {
        return "✓ 已识别：小红书";
      }
      return "";
    },
    /**
     * 选择本地视频
     */
    chooseVideo() {
      common_vendor.index.chooseVideo({
        count: 1,
        sizeType: ["compressed"],
        sourceType: ["album", "camera"],
        maxDuration: 60,
        camera: "back",
        success: (res) => {
          this.videoPath = res.tempFilePath;
          this.shareUrl = "";
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/remove-logo/remove-logo.vue:104", "选择视频失败", err);
          common_vendor.index.showToast({
            title: "选择视频失败",
            icon: "none"
          });
        }
      });
    },
    /**
     * 去水印
     */
    async generate() {
      if (this.shareUrl) {
        await this.generateFromShareUrl();
      } else if (this.videoPath) {
        common_vendor.index.showToast({
          title: "请使用分享链接功能",
          icon: "none"
        });
      } else {
        common_vendor.index.showToast({
          title: "请粘贴分享链接或选择视频",
          icon: "none"
        });
      }
    },
    /**
     * 从分享链接去水印
     */
    async generateFromShareUrl() {
      if (!this.shareUrl.trim()) {
        common_vendor.index.showToast({
          title: "请输入分享链接",
          icon: "none"
        });
        return;
      }
      if (!this.shareUrl.includes("douyin.com") && !this.shareUrl.includes("iesdouyin.com") && !this.shareUrl.includes("xiaohongshu.com")) {
        common_vendor.index.showToast({
          title: "仅支持抖音和小红书链接",
          icon: "none"
        });
        return;
      }
      this.generating = true;
      try {
        const res = await common_utils_api.api.removeLogo({
          shareUrl: this.shareUrl.trim()
        });
        if (res.code === 200) {
          common_vendor.index.navigateTo({
            url: `/pages/result/result?type=1&resultUrl=${encodeURIComponent(res.data.resultUrl)}`
          });
        } else {
          common_vendor.index.showToast({
            title: res.message || "去水印失败",
            icon: "none"
          });
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/remove-logo/remove-logo.vue:173", "去水印失败", e);
        common_vendor.index.showToast({
          title: e.message || "去水印失败，请重试",
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
    a: common_vendor.o([($event) => $data.shareUrl = $event.detail.value, (...args) => $options.onShareUrlInput && $options.onShareUrlInput(...args)]),
    b: $data.shareUrl,
    c: $data.shareUrl
  }, $data.shareUrl ? {
    d: common_vendor.t($options.getPlatformName())
  } : {}, {
    e: !$data.videoPath
  }, !$data.videoPath ? {} : {
    f: $data.videoPath
  }, {
    g: !$data.videoPath
  }, !$data.videoPath ? {} : {}, {
    h: common_vendor.o((...args) => $options.chooseVideo && $options.chooseVideo(...args)),
    i: common_vendor.t($data.generating ? "处理中..." : "立即去水印"),
    j: !$data.shareUrl && !$data.videoPath || $data.generating,
    k: common_vendor.o((...args) => $options.generate && $options.generate(...args))
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-101c0d2c"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/remove-logo/remove-logo.js.map
