"use strict";
const common_vendor = require("../../common/vendor.js");
const common_utils_api = require("../../common/utils/api.js");
const _sfc_main = {
  data() {
    return {
      resultUrl: "",
      type: "",
      taskId: null,
      taskInfo: null,
      imageList: [],
      loading: false
    };
  },
  onLoad(options) {
    if (options.taskId) {
      this.taskId = options.taskId;
      this.loadTaskDetail();
    } else {
      this.type = options.type || "";
      let url = decodeURIComponent(options.resultUrl || "");
      if (url.startsWith("IMAGE_LIST:")) {
        try {
          const imageListJson = url.substring("IMAGE_LIST:".length);
          this.imageList = JSON.parse(imageListJson);
          if (this.imageList && this.imageList.length > 0) {
            this.resultUrl = this.imageList[0];
          }
        } catch (e) {
          common_vendor.index.__f__("error", "at pages/result/result.vue:100", "解析图片列表失败", e);
          this.resultUrl = url;
        }
      } else {
        this.resultUrl = url;
      }
    }
  },
  onShow() {
    if (this.taskId && this.taskInfo && this.taskInfo.taskStatus !== 2 && this.taskInfo.taskStatus !== 3) {
      this.refreshTaskStatus();
    }
  },
  methods: {
    async loadTaskDetail() {
      this.loading = true;
      try {
        const res = await common_utils_api.api.getTaskDetail(this.taskId);
        if (res.code === 200) {
          this.taskInfo = res.data;
        } else {
          common_vendor.index.showToast({
            title: res.message || "加载失败",
            icon: "none"
          });
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/result/result.vue:128", "加载任务详情失败", e);
        common_vendor.index.showToast({
          title: "加载失败",
          icon: "none"
        });
      } finally {
        this.loading = false;
      }
    },
    async refreshTaskStatus() {
      try {
        const res = await common_utils_api.api.getTaskDetail(this.taskId);
        if (res.code === 200) {
          this.taskInfo = res.data;
          if (this.taskInfo.taskStatus === 2 || this.taskInfo.taskStatus === 3) {
            return;
          }
          setTimeout(() => {
            this.refreshTaskStatus();
          }, 3e3);
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/result/result.vue:152", "刷新任务状态失败", e);
      }
    },
    getStatusText(status) {
      const statusMap = {
        0: "排队中",
        1: "生成中",
        2: "已完成",
        3: "失败"
      };
      return statusMap[status] || "未知";
    },
    getStatusClass(status) {
      const classMap = {
        0: "status-pending",
        1: "status-processing",
        2: "status-completed",
        3: "status-failed"
      };
      return classMap[status] || "";
    },
    extractImageUrl(resultUrl) {
      if (resultUrl && resultUrl.startsWith("IMAGE_LIST:")) {
        try {
          const jsonStr = resultUrl.replace("IMAGE_LIST:", "");
          const urls = JSON.parse(jsonStr);
          return urls[0] || "";
        } catch (e) {
          return resultUrl;
        }
      }
      return resultUrl || "";
    },
    async saveToAlbum() {
      const imageUrl = this.taskInfo ? this.extractImageUrl(this.taskInfo.resultUrl) : this.imageList[0] || this.resultUrl;
      if (!imageUrl) {
        return;
      }
      try {
        common_vendor.index.showLoading({
          title: "下载中...",
          mask: true
        });
        common_vendor.index.downloadFile({
          url: imageUrl,
          success: (res) => {
            common_vendor.index.hideLoading();
            if (res.statusCode === 200) {
              common_vendor.index.saveImageToPhotosAlbum({
                filePath: res.tempFilePath,
                success: () => {
                  common_vendor.index.showToast({
                    title: "保存成功",
                    icon: "success"
                  });
                },
                fail: (err) => {
                  common_vendor.index.__f__("error", "at pages/result/result.vue:211", "保存图片失败", err);
                  common_vendor.index.showToast({
                    title: "保存失败",
                    icon: "none"
                  });
                }
              });
            } else {
              common_vendor.index.showToast({
                title: "下载失败",
                icon: "none"
              });
            }
          },
          fail: (err) => {
            common_vendor.index.hideLoading();
            common_vendor.index.__f__("error", "at pages/result/result.vue:227", "下载文件失败", err);
            common_vendor.index.showToast({
              title: "下载失败",
              icon: "none"
            });
          }
        });
      } catch (e) {
        common_vendor.index.hideLoading();
        common_vendor.index.__f__("error", "at pages/result/result.vue:236", "保存失败", e);
        common_vendor.index.showToast({
          title: "保存失败",
          icon: "none"
        });
      }
    },
    async downloadImage() {
      const imageUrl = this.extractImageUrl(this.taskInfo.resultUrl);
      if (!imageUrl) {
        return;
      }
      try {
        const res = await common_utils_api.api.downloadImage(this.taskId);
        if (res.code === 200) {
          common_vendor.index.showToast({
            title: "下载链接已获取",
            icon: "success"
          });
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/result/result.vue:258", "下载失败", e);
        common_vendor.index.showToast({
          title: "下载失败",
          icon: "none"
        });
      }
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: $data.loading
  }, $data.loading ? {} : $data.taskInfo ? common_vendor.e({
    c: common_vendor.t($options.getStatusText($data.taskInfo.taskStatus)),
    d: common_vendor.n($options.getStatusClass($data.taskInfo.taskStatus)),
    e: $data.taskInfo.taskStatus === 2 && $data.taskInfo.resultUrl
  }, $data.taskInfo.taskStatus === 2 && $data.taskInfo.resultUrl ? {
    f: $options.extractImageUrl($data.taskInfo.resultUrl)
  } : $data.taskInfo.taskStatus === 3 ? {
    h: common_vendor.t($data.taskInfo.errorMessage || "生成失败")
  } : {
    i: common_vendor.t($data.taskInfo.taskStatus === 0 ? "任务排队中..." : "正在生成中...")
  }, {
    g: $data.taskInfo.taskStatus === 3,
    j: $data.taskInfo.taskStatus === 2 && $data.taskInfo.resultUrl
  }, $data.taskInfo.taskStatus === 2 && $data.taskInfo.resultUrl ? {
    k: common_vendor.o((...args) => $options.saveToAlbum && $options.saveToAlbum(...args))
  } : {}, {
    l: $data.taskInfo.taskStatus === 2 && $data.taskInfo.resultUrl
  }, $data.taskInfo.taskStatus === 2 && $data.taskInfo.resultUrl ? {
    m: common_vendor.o((...args) => $options.downloadImage && $options.downloadImage(...args))
  } : {}) : common_vendor.e({
    n: $data.imageList.length > 0
  }, $data.imageList.length > 0 ? {
    o: common_vendor.f($data.imageList, (imageUrl, index, i0) => {
      return {
        a: index,
        b: imageUrl
      };
    })
  } : $data.resultUrl ? {
    q: $data.resultUrl
  } : {}, {
    p: $data.resultUrl,
    r: common_vendor.o((...args) => $options.saveToAlbum && $options.saveToAlbum(...args))
  }), {
    b: $data.taskInfo
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-b615976f"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/result/result.js.map
