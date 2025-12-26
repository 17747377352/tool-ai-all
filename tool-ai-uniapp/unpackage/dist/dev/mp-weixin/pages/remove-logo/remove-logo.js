"use strict";
const common_vendor = require("../../common/vendor.js");
const common_utils_api = require("../../common/utils/api.js");
const common_utils_ad = require("../../common/utils/ad.js");
const AdVideoBanner = () => "../../common/components/ad-video-banner.js";
const _sfc_main = {
  components: {
    AdVideoBanner
  },
  data() {
    return {
      shareUrl: "",
      // 分享链接
      generating: false,
      canUse: false,
      // 是否可以使用（24小时内观看过广告）
      checkingAd: false
      // 是否正在检查广告状态
    };
  },
  onLoad() {
    this.checkAdStatus();
  },
  methods: {
    /**
     * 检查广告观看状态
     */
    async checkAdStatus() {
      try {
        const res = await common_utils_api.api.checkRemoveLogoAd();
        if (res.code === 200) {
          this.canUse = res.data.canUse || false;
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/remove-logo/remove-logo.vue:72", "检查广告状态失败", e);
      }
    },
    /**
     * 观看广告
     */
    async watchAd() {
      try {
        common_vendor.index.showLoading({ title: "加载广告中..." });
        await common_utils_ad.showRewardedVideo();
        common_vendor.index.hideLoading();
        const res = await common_utils_api.api.recordAdWatch(1);
        if (res.code === 200) {
          this.canUse = true;
          common_vendor.index.showToast({
            title: "观看成功！24小时内可使用",
            icon: "success"
          });
        } else {
          common_vendor.index.showToast({
            title: "记录失败，请重试",
            icon: "none"
          });
        }
      } catch (e) {
        common_vendor.index.hideLoading();
        common_vendor.index.__f__("error", "at pages/remove-logo/remove-logo.vue:101", "观看广告失败", e);
        if (e.message && !e.message.includes("广告未完整观看")) {
          common_vendor.index.showToast({
            title: "观看广告失败，请重试",
            icon: "none"
          });
        }
      }
    },
    /**
     * 分享链接输入处理
     */
    onShareUrlInput(e) {
      this.shareUrl = e.detail.value;
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
     * 去水印
     */
    async generate() {
      if (!this.canUse) {
        common_vendor.index.showModal({
          title: "提示",
          content: "使用此功能需要观看广告，观看后24小时内有效",
          confirmText: "观看广告",
          cancelText: "取消",
          success: (res) => {
            if (res.confirm) {
              this.watchAd();
            }
          }
        });
        return;
      }
      await this.generateFromShareUrl();
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
          if (res.message && res.message.includes("观看广告")) {
            this.canUse = false;
            await this.checkAdStatus();
          }
          common_vendor.index.showToast({
            title: res.message || "去水印失败",
            icon: "none"
          });
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/remove-logo/remove-logo.vue:200", "去水印失败", e);
        if (e.message && e.message.includes("观看广告")) {
          this.canUse = false;
          await this.checkAdStatus();
        }
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
    e: !$data.canUse
  }, !$data.canUse ? {} : {}, {
    f: common_vendor.t($data.generating ? "处理中..." : $data.canUse ? "立即去水印" : "观看广告后使用"),
    g: !$data.shareUrl || $data.generating,
    h: common_vendor.o((...args) => $options.generate && $options.generate(...args))
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-101c0d2c"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/remove-logo/remove-logo.js.map
