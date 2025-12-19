"use strict";
const common_vendor = require("../../common/vendor.js");
const common_utils_ad = require("../../common/utils/ad.js");
const AdVideoBanner = () => "../../common/components/ad-video-banner.js";
const _sfc_main = {
  components: {
    AdVideoBanner
  },
  data() {
    return {
      resultUrl: "",
      type: "",
      typeName: "",
      nickname: "用户",
      isVideo: false,
      isFortuneCard: false,
      // 是否显示黄历卡片
      fortuneData: {},
      // 黄历数据
      imageList: [],
      // 多图列表
      currentImageIndex: 0
      // 当前显示的图片索引
    };
  },
  onLoad(options) {
    common_vendor.index.__f__("log", "at pages/result/result.vue:144", "=== result页面加载 ===");
    common_vendor.index.__f__("log", "at pages/result/result.vue:145", "原始options:", options);
    common_vendor.index.__f__("log", "at pages/result/result.vue:146", "原始resultUrl:", options.resultUrl);
    let url = decodeURIComponent(options.resultUrl || "");
    common_vendor.index.__f__("log", "at pages/result/result.vue:149", "解码后的URL:", url);
    this.type = options.type || "";
    this.typeName = this.getTypeName(this.type);
    this.nickname = common_vendor.index.getStorageSync("nickname") || "用户";
    common_vendor.index.__f__("log", "at pages/result/result.vue:155", "result页面加载，type:", this.type, "url:", url);
    if (url.startsWith("IMAGE_LIST:")) {
      try {
        const imageListJson = url.substring("IMAGE_LIST:".length);
        this.imageList = JSON.parse(imageListJson);
        if (this.imageList && this.imageList.length > 0) {
          this.resultUrl = this.imageList[0];
          this.currentImageIndex = 0;
          common_vendor.index.__f__("log", "at pages/result/result.vue:165", "检测到多图内容，共", this.imageList.length, "张图片");
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/result/result.vue:168", "解析图片列表失败", e);
        this.resultUrl = url;
      }
    } else if (url.startsWith("FORTUNE_JSON:")) {
      try {
        const jsonStr = url.substring("FORTUNE_JSON:".length);
        this.fortuneData = JSON.parse(jsonStr);
        this.isFortuneCard = true;
        this.resultUrl = "";
        common_vendor.index.__f__("log", "at pages/result/result.vue:178", "检测到黄历JSON，开启卡片渲染模式", this.fortuneData);
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/result/result.vue:180", "解析黄历JSON失败", e);
        this.resultUrl = url;
      }
    } else {
      this.resultUrl = url;
      this.isVideo = this.type === "1" && this.isVideoUrl(this.resultUrl);
    }
    common_vendor.index.__f__("log", "at pages/result/result.vue:189", "=== 最终显示信息 ===");
    common_vendor.index.__f__("log", "at pages/result/result.vue:190", "最终resultUrl:", this.resultUrl);
    common_vendor.index.__f__("log", "at pages/result/result.vue:191", "isVideo:", this.isVideo);
    common_vendor.index.__f__("log", "at pages/result/result.vue:192", "==================");
  },
  methods: {
    getTypeName(type) {
      const names = {
        "1": "去水印",
        "2": "AI头像",
        "3": "姓氏签名",
        "4": "运势测试",
        "5": "星座运势",
        "6": "老照片修复"
      };
      return names[type] || "结果";
    },
    /**
     * 判断是否为视频URL
     */
    isVideoUrl(url) {
      if (!url)
        return false;
      const videoExtensions = [".mp4", ".m3u8", ".flv", ".mov", ".avi", ".webm"];
      const videoKeywords = ["video", "play", "stream", "m3u8"];
      const lowerUrl = url.toLowerCase();
      if (videoExtensions.some((ext) => lowerUrl.includes(ext))) {
        return true;
      }
      if (videoKeywords.some((keyword) => lowerUrl.includes(keyword))) {
        return true;
      }
      if (lowerUrl.includes("douyin") || lowerUrl.includes("xiaohongshu")) {
        if (lowerUrl.includes("play") || lowerUrl.includes("video")) {
          return true;
        }
      }
      return false;
    },
    onImageLoad() {
      common_vendor.index.__f__("log", "at pages/result/result.vue:233", "图片加载完成", this.resultUrl);
    },
    onImageError(e) {
      common_vendor.index.__f__("error", "at pages/result/result.vue:236", "图片加载失败", e, this.resultUrl);
      common_vendor.index.showModal({
        title: "图片加载失败",
        content: "图片可能无法访问，请检查：\n1. 网络连接\n2. 小程序域名白名单配置\n3. 图片链接是否有效\n\n图片地址：" + (this.resultUrl.length > 50 ? this.resultUrl.substring(0, 50) + "..." : this.resultUrl),
        showCancel: true,
        confirmText: "复制地址",
        cancelText: "关闭",
        success: (res) => {
          if (res.confirm) {
            common_vendor.index.setClipboardData({
              data: this.resultUrl,
              success: () => {
                common_vendor.index.showToast({
                  title: "地址已复制",
                  icon: "success"
                });
              }
            });
          }
        }
      });
    },
    onVideoPlay() {
      common_vendor.index.__f__("log", "at pages/result/result.vue:265", "视频开始播放");
    },
    onVideoError(e) {
      common_vendor.index.__f__("error", "at pages/result/result.vue:268", "视频播放错误", e);
      common_vendor.index.showToast({
        title: "视频播放失败，请检查网络",
        icon: "none"
      });
    },
    onSwiperChange(e) {
      this.currentImageIndex = e.detail.current;
    },
    async saveToAlbum() {
      try {
        await common_utils_ad.showRewardedVideo();
        if (this.isVideo) {
          await this.downloadAndSaveVideo();
        } else {
          await this.downloadAndSaveImage();
        }
        const saveCount = this.getSaveCount();
        const shouldShowInterstitial = [1, 3, 5].includes(saveCount);
        await common_utils_ad.showInterstitialAd(null, shouldShowInterstitial);
        common_vendor.index.showToast({
          title: "保存成功",
          icon: "success"
        });
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/result/result.vue:299", "保存失败", e);
        common_vendor.index.showToast({
          title: e.message || "保存失败，请重试",
          icon: "none"
        });
      }
    },
    /**
     * 下载并保存图片
     */
    downloadAndSaveImage() {
      return new Promise((resolve, reject) => {
        if (this.imageList.length > 1) {
          this.downloadAndSaveAllImages().then(resolve).catch(reject);
          return;
        }
        common_vendor.index.showLoading({
          title: "下载中...",
          mask: true
        });
        common_vendor.index.downloadFile({
          url: this.resultUrl,
          success: (res) => {
            common_vendor.index.hideLoading();
            if (res.statusCode === 200) {
              common_vendor.index.saveImageToPhotosAlbum({
                filePath: res.tempFilePath,
                success: () => {
                  this.incrementSaveCount();
                  resolve();
                },
                fail: (err) => {
                  common_vendor.index.__f__("error", "at pages/result/result.vue:335", "保存图片失败", err);
                  if (err.errMsg && err.errMsg.includes("auth deny")) {
                    common_vendor.index.showModal({
                      title: "需要相册权限",
                      content: "请在设置中开启相册权限",
                      showCancel: false
                    });
                  }
                  reject(err);
                }
              });
            } else {
              reject(new Error("下载失败"));
            }
          },
          fail: (err) => {
            common_vendor.index.hideLoading();
            common_vendor.index.__f__("error", "at pages/result/result.vue:353", "下载文件失败", err);
            reject(err);
          }
        });
      });
    },
    /**
     * 下载并保存所有图片
     */
    downloadAndSaveAllImages() {
      return new Promise((resolve, reject) => {
        common_vendor.index.showLoading({
          title: `下载图片 1/${this.imageList.length}...`,
          mask: true
        });
        let savedCount = 0;
        const total = this.imageList.length;
        const downloadNext = (index) => {
          if (index >= total) {
            common_vendor.index.hideLoading();
            if (savedCount > 0) {
              this.incrementSaveCount();
              common_vendor.index.showToast({
                title: `已保存${savedCount}张图片`,
                icon: "success"
              });
              resolve();
            } else {
              reject(new Error("所有图片保存失败"));
            }
            return;
          }
          common_vendor.index.showLoading({
            title: `下载图片 ${index + 1}/${total}...`,
            mask: true
          });
          common_vendor.index.downloadFile({
            url: this.imageList[index],
            success: (res) => {
              if (res.statusCode === 200) {
                common_vendor.index.saveImageToPhotosAlbum({
                  filePath: res.tempFilePath,
                  success: () => {
                    savedCount++;
                    downloadNext(index + 1);
                  },
                  fail: (err) => {
                    common_vendor.index.__f__("error", "at pages/result/result.vue:406", `保存第${index + 1}张图片失败`, err);
                    downloadNext(index + 1);
                  }
                });
              } else {
                downloadNext(index + 1);
              }
            },
            fail: (err) => {
              common_vendor.index.__f__("error", "at pages/result/result.vue:418", `下载第${index + 1}张图片失败`, err);
              downloadNext(index + 1);
            }
          });
        };
        downloadNext(0);
      });
    },
    /**
     * 下载并保存视频
     */
    downloadAndSaveVideo() {
      return new Promise((resolve, reject) => {
        common_vendor.index.showLoading({
          title: "下载视频中...",
          mask: true
        });
        common_vendor.index.downloadFile({
          url: this.resultUrl,
          success: (res) => {
            common_vendor.index.hideLoading();
            if (res.statusCode === 200) {
              common_vendor.index.saveVideoToPhotosAlbum({
                filePath: res.tempFilePath,
                success: () => {
                  this.incrementSaveCount();
                  resolve();
                },
                fail: (err) => {
                  common_vendor.index.__f__("error", "at pages/result/result.vue:451", "保存视频失败", err);
                  if (err.errMsg && err.errMsg.includes("auth deny")) {
                    common_vendor.index.showModal({
                      title: "需要相册权限",
                      content: "请在设置中开启相册权限",
                      showCancel: false
                    });
                  }
                  reject(err);
                }
              });
            } else {
              reject(new Error("下载失败"));
            }
          },
          fail: (err) => {
            common_vendor.index.hideLoading();
            common_vendor.index.__f__("error", "at pages/result/result.vue:469", "下载视频失败", err);
            if (err.errMsg && err.errMsg.includes("fail")) {
              common_vendor.index.showModal({
                title: "下载失败",
                content: "视频下载失败，可能是网络问题或视频链接已失效",
                showCancel: false
              });
            }
            reject(err);
          }
        });
      });
    },
    getSaveCount() {
      return common_vendor.index.getStorageSync("saveCount") || 0;
    },
    incrementSaveCount() {
      const count = this.getSaveCount() + 1;
      common_vendor.index.setStorageSync("saveCount", count);
    }
  }
};
if (!Array) {
  const _component_ad_video_banner = common_vendor.resolveComponent("ad-video-banner");
  _component_ad_video_banner();
}
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.t($data.nickname),
    b: common_vendor.t($data.typeName),
    c: $data.isFortuneCard
  }, $data.isFortuneCard ? common_vendor.e({
    d: common_vendor.t($data.fortuneData.date),
    e: common_vendor.t($data.fortuneData.weekday),
    f: common_vendor.t($data.fortuneData.lunar),
    g: common_vendor.t($data.fortuneData.lunarYear),
    h: common_vendor.t($data.fortuneData.desc || "吉"),
    i: common_vendor.t($data.fortuneData.suit || "诸事皆宜"),
    j: common_vendor.t($data.fortuneData.avoid || "诸事不忌"),
    k: $data.fortuneData.holiday || $data.fortuneData.animalsYear
  }, $data.fortuneData.holiday || $data.fortuneData.animalsYear ? {
    l: common_vendor.t($data.fortuneData.animalsYear),
    m: common_vendor.t($data.fortuneData.holiday ? " | " + $data.fortuneData.holiday : "")
  } : {}) : $data.isVideo ? {
    o: $data.resultUrl,
    p: common_vendor.o((...args) => $options.onVideoPlay && $options.onVideoPlay(...args)),
    q: common_vendor.o((...args) => $options.onVideoError && $options.onVideoError(...args))
  } : common_vendor.e({
    r: $data.imageList.length > 0
  }, $data.imageList.length > 0 ? {
    s: common_vendor.f($data.imageList, (imageUrl, index, i0) => {
      return {
        a: imageUrl,
        b: common_vendor.o((...args) => $options.onImageLoad && $options.onImageLoad(...args), index),
        c: common_vendor.o((...args) => $options.onImageError && $options.onImageError(...args), index),
        d: index
      };
    }),
    t: $data.currentImageIndex,
    v: common_vendor.o((...args) => $options.onSwiperChange && $options.onSwiperChange(...args))
  } : {
    w: $data.resultUrl,
    x: common_vendor.o((...args) => $options.onImageLoad && $options.onImageLoad(...args)),
    y: common_vendor.o((...args) => $options.onImageError && $options.onImageError(...args))
  }, {
    z: $data.imageList.length > 1
  }, $data.imageList.length > 1 ? {
    A: common_vendor.t($data.currentImageIndex + 1),
    B: common_vendor.t($data.imageList.length)
  } : {}), {
    n: $data.isVideo,
    C: !$data.isFortuneCard
  }, !$data.isFortuneCard ? {
    D: common_vendor.t($data.isVideo ? "保存视频到相册" : $data.imageList.length > 1 ? `保存全部${$data.imageList.length}张图片` : "保存到相册"),
    E: common_vendor.o((...args) => $options.saveToAlbum && $options.saveToAlbum(...args))
  } : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-b615976f"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/result/result.js.map
