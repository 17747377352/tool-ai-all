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
      mode: "text",
      // 'text' 字生图, 'image' 图生图
      imageUrl: "",
      // 上传的图片URL（图生图模式）
      imageTempPath: "",
      // 临时图片路径（用于预览）
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
    /**
     * 选择图片（图生图模式）
     */
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
          common_vendor.index.__f__("error", "at pages/ai-avatar/ai-avatar.vue:118", "选择图片失败", err);
          common_vendor.index.showToast({
            title: "选择图片失败",
            icon: "none"
          });
        }
      });
    },
    /**
     * 上传图片到服务器
     */
    async uploadImage() {
      if (!this.imageTempPath) {
        return;
      }
      common_vendor.index.showLoading({
        title: "上传中..."
      });
      try {
        const token = common_vendor.index.getStorageSync("token");
        if (!token) {
          throw new Error("未登录");
        }
        const uploadRes = await new Promise((resolve, reject) => {
          common_vendor.index.uploadFile({
            url: `${common_config_apiConfig.apiConfig.BASE_URL}/tool/upload-image`,
            filePath: this.imageTempPath,
            name: "file",
            header: {
              "Authorization": "Bearer " + token
            },
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
        common_vendor.index.__f__("error", "at pages/ai-avatar/ai-avatar.vue:178", "上传图片失败", e);
        common_vendor.index.hideLoading();
        common_vendor.index.showToast({
          title: e.message || "上传图片失败",
          icon: "none"
        });
        this.imageUrl = "";
        this.imageTempPath = "";
      }
    },
    /**
     * 生成头像
     */
    async generate() {
      if (!this.prompt.trim()) {
        common_vendor.index.showToast({
          title: "请输入提示词",
          icon: "none"
        });
        return;
      }
      if (this.mode === "image" && !this.imageUrl) {
        common_vendor.index.showToast({
          title: "请先上传图片",
          icon: "none"
        });
        return;
      }
      this.generating = true;
      try {
        const requestData = {
          prompt: this.prompt,
          style: this.selectedStyle
        };
        if (this.mode === "image" && this.imageUrl) {
          requestData.imageUrl = this.imageUrl;
        }
        const res = await common_utils_api.api.generateAiAvatar(requestData);
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
        common_vendor.index.__f__("error", "at pages/ai-avatar/ai-avatar.vue:236", "生成失败", e);
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
if (!Array) {
  const _component_ad_video_banner = common_vendor.resolveComponent("ad-video-banner");
  _component_ad_video_banner();
}
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: $data.mode === "text" ? 1 : "",
    b: common_vendor.o(($event) => $data.mode = "text"),
    c: $data.mode === "image" ? 1 : "",
    d: common_vendor.o(($event) => $data.mode = "image"),
    e: $data.mode === "image"
  }, $data.mode === "image" ? common_vendor.e({
    f: $data.imageUrl
  }, $data.imageUrl ? {
    g: $data.imageUrl
  } : {}, {
    h: common_vendor.o((...args) => $options.chooseImage && $options.chooseImage(...args))
  }) : {}, {
    i: $data.mode === "image" ? "例如：将这个头像转换为卡通风格" : "例如：一个可爱的卡通头像，蓝色背景",
    j: $data.prompt,
    k: common_vendor.o(($event) => $data.prompt = $event.detail.value),
    l: common_vendor.f($data.styles, (style, k0, i0) => {
      return {
        a: common_vendor.t(style.label),
        b: style.value,
        c: $data.selectedStyle === style.value ? 1 : "",
        d: common_vendor.o(($event) => $data.selectedStyle = style.value, style.value)
      };
    }),
    m: common_vendor.t($data.generating ? "生成中..." : "立即生成"),
    n: !$data.prompt || $data.mode === "image" && !$data.imageUrl || $data.generating,
    o: common_vendor.o((...args) => $options.generate && $options.generate(...args))
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-0f6d85d2"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/ai-avatar/ai-avatar.js.map
