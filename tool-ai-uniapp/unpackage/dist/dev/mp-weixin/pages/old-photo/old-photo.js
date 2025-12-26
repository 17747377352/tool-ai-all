"use strict";
const common_vendor = require("../../common/vendor.js");
const common_utils_api = require("../../common/utils/api.js");
const common_config_apiConfig = require("../../common/config/api-config.js");
const common_utils_ad = require("../../common/utils/ad.js");
const AdVideoBanner = () => "../../common/components/ad-video-banner.js";
const _sfc_main = {
  components: {
    AdVideoBanner
  },
  data() {
    return {
      mode: "single",
      // 'single' 单张模式, 'batch' 批量模式
      imageUrl: "",
      // OSS 地址（单张模式）
      imageTempPath: "",
      // 本地预览路径（单张模式）
      imageList: [],
      // 批量模式图片列表，格式: [{ url: '...', ossUrl: '...' }]
      strength: 0.7,
      generating: false,
      remainingCount: 0
      // 批量修复广告额度（不包含免费的第一张）
    };
  },
  onLoad() {
    this.checkBatchRestoreStatus();
  },
  methods: {
    /**
     * 检查批量修复剩余次数
     */
    async checkBatchRestoreStatus() {
      try {
        const res = await common_utils_api.api.checkBatchRestoreAd();
        if (res.code === 200) {
          this.remainingCount = res.data.remainingCount || 0;
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/old-photo/old-photo.vue:165", "检查批量修复状态失败", e);
      }
    },
    /**
     * 观看广告获得批量修复次数
     */
    async watchAdForBatch() {
      try {
        common_vendor.index.showLoading({ title: "加载广告中..." });
        await common_utils_ad.showRewardedVideo();
        common_vendor.index.hideLoading();
        const res = await common_utils_api.api.recordAdWatch(2);
        if (res.code === 200) {
          await this.checkBatchRestoreStatus();
          common_vendor.index.showToast({
            title: `观看成功！获得10张额度`,
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
        common_vendor.index.__f__("error", "at pages/old-photo/old-photo.vue:195", "观看广告失败", e);
        if (e.message && !e.message.includes("广告未完整观看")) {
          common_vendor.index.showToast({
            title: "观看广告失败，请重试",
            icon: "none"
          });
        }
      }
    },
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
          common_vendor.index.__f__("error", "at pages/old-photo/old-photo.vue:219", "选择图片失败", err);
          common_vendor.index.showToast({
            title: "选择图片失败",
            icon: "none"
          });
        }
      });
    },
    /**
     * 选择批量图片
     */
    chooseBatchImages() {
      const remaining = 10 - this.imageList.length;
      if (remaining <= 0) {
        common_vendor.index.showToast({
          title: "最多只能选择10张图片",
          icon: "none"
        });
        return;
      }
      common_vendor.index.chooseImage({
        count: remaining,
        sizeType: ["original", "compressed"],
        sourceType: ["album", "camera"],
        success: (res) => {
          res.tempFilePaths.forEach((tempPath) => {
            this.imageList.push({
              url: tempPath,
              ossUrl: "",
              uploading: true
            });
            this.uploadBatchImage(this.imageList.length - 1, tempPath);
          });
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/old-photo/old-photo.vue:257", "选择图片失败", err);
          common_vendor.index.showToast({
            title: "选择图片失败",
            icon: "none"
          });
        }
      });
    },
    /**
     * 上传批量图片中的一张
     */
    async uploadBatchImage(index, tempPath) {
      try {
        const token = common_vendor.index.getStorageSync("token");
        if (!token)
          throw new Error("未登录");
        const uploadRes = await new Promise((resolve, reject) => {
          common_vendor.index.uploadFile({
            url: `${common_config_apiConfig.apiConfig.BASE_URL}/tool/upload-image`,
            filePath: tempPath,
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
          this.imageList[index].ossUrl = uploadRes.data.imageUrl;
          this.imageList[index].uploading = false;
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/old-photo/old-photo.vue:301", "上传图片失败", e);
        this.imageList[index].uploading = false;
        common_vendor.index.showToast({
          title: "图片上传失败",
          icon: "none"
        });
      }
    },
    /**
     * 移除图片
     */
    removeImage(index) {
      this.imageList.splice(index, 1);
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
        common_vendor.index.__f__("error", "at pages/old-photo/old-photo.vue:350", "上传图片失败", e);
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
        common_vendor.index.__f__("error", "at pages/old-photo/old-photo.vue:382", "修复失败", e);
        common_vendor.index.showToast({
          title: e.message || "修复失败，请重试",
          icon: "none"
        });
      } finally {
        this.generating = false;
      }
    },
    /**
     * 批量修复
     */
    async batchRestore() {
      if (this.imageList.length === 0) {
        common_vendor.index.showToast({ title: "请先选择照片", icon: "none" });
        return;
      }
      const uploadingImages = this.imageList.filter((img) => img.uploading || !img.ossUrl);
      if (uploadingImages.length > 0) {
        common_vendor.index.showToast({ title: "请等待图片上传完成", icon: "none" });
        return;
      }
      const needCount = Math.max(0, this.imageList.length - 1);
      if (needCount > 0 && this.remainingCount < needCount) {
        common_vendor.index.showModal({
          title: "额度不足",
          content: `需要${needCount}张额度（第一张免费），当前剩余${this.remainingCount}张。观看广告可获得10张额度`,
          confirmText: "观看广告",
          cancelText: "取消",
          success: (res) => {
            if (res.confirm) {
              this.watchAdForBatch();
            }
          }
        });
        return;
      }
      this.generating = true;
      try {
        const imageUrls = this.imageList.map((img) => img.ossUrl).filter((url) => url);
        const res = await common_utils_api.api.batchRestoreOldPhoto({
          imageUrls,
          strength: Number(this.strength.toFixed(2))
        });
        if (res.code === 200) {
          await this.checkBatchRestoreStatus();
          common_vendor.index.navigateTo({
            url: `/pages/result/result?type=6&resultUrl=${encodeURIComponent(res.data.resultUrl)}`
          });
        } else {
          common_vendor.index.showToast({
            title: res.message || "批量修复失败",
            icon: "none"
          });
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/old-photo/old-photo.vue:449", "批量修复失败", e);
        if (e.message && e.message.includes("次数")) {
          await this.checkBatchRestoreStatus();
        }
        common_vendor.index.showToast({
          title: e.message || "批量修复失败，请重试",
          icon: "none"
        });
      } finally {
        this.generating = false;
      }
    }
  },
  watch: {
    mode(newVal) {
      if (newVal === "batch") {
        this.checkBatchRestoreStatus();
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
    a: $data.mode === "single" ? 1 : "",
    b: common_vendor.o(($event) => $data.mode = "single"),
    c: $data.mode === "batch" ? 1 : "",
    d: common_vendor.o(($event) => $data.mode = "batch"),
    e: $data.mode === "single"
  }, $data.mode === "single" ? common_vendor.e({
    f: $data.imageUrl
  }, $data.imageUrl ? {
    g: $data.imageUrl
  } : {}, {
    h: common_vendor.o((...args) => $options.chooseImage && $options.chooseImage(...args)),
    i: common_vendor.t(($data.strength * 100).toFixed(0)),
    j: $data.strength * 100,
    k: common_vendor.o((...args) => $options.onStrengthChange && $options.onStrengthChange(...args)),
    l: common_vendor.t($data.generating ? "修复中..." : "开始修复"),
    m: !$data.imageUrl || $data.generating,
    n: common_vendor.o((...args) => $options.restore && $options.restore(...args))
  }) : common_vendor.e({
    o: common_vendor.f($data.imageList, (item, index, i0) => {
      return {
        a: item.url,
        b: index,
        c: common_vendor.o(($event) => $options.removeImage(index), index)
      };
    }),
    p: $data.imageList.length < 10
  }, $data.imageList.length < 10 ? {
    q: common_vendor.t($data.imageList.length),
    r: common_vendor.o((...args) => $options.chooseBatchImages && $options.chooseBatchImages(...args))
  } : {}, {
    s: $data.remainingCount >= 0
  }, $data.remainingCount >= 0 ? {
    t: common_vendor.t($data.remainingCount)
  } : {}, {
    v: common_vendor.t(($data.strength * 100).toFixed(0)),
    w: $data.strength * 100,
    x: common_vendor.o((...args) => $options.onStrengthChange && $options.onStrengthChange(...args)),
    y: common_vendor.t($data.generating ? "批量修复中..." : `批量修复(${$data.imageList.length}张)`),
    z: $data.imageList.length === 0 || $data.generating,
    A: common_vendor.o((...args) => $options.batchRestore && $options.batchRestore(...args))
  }));
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-549edafb"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/old-photo/old-photo.js.map
