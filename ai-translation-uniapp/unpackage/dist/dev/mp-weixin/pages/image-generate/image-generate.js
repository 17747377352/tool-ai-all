"use strict";
const common_vendor = require("../../common/vendor.js");
const common_utils_api = require("../../common/utils/api.js");
const common_utils_ossUpload = require("../../common/utils/oss-upload.js");
const _sfc_main = {
  data() {
    return {
      mode: "text",
      // 'text' 字生图, 'image' 图生图, 'template' 模版生图
      prompt: "",
      selectedStyle: "realistic",
      styles: [
        { label: "写实", value: "realistic" },
        { label: "卡通", value: "cartoon" },
        { label: "动漫", value: "anime" },
        { label: "油画", value: "oil-painting" }
      ],
      imageUrl: "",
      imageTempPath: "",
      imagePrompt: "",
      imageSelectedStyle: "realistic",
      selectedTemplate: null,
      generateMode: 3,
      // 3-模版同款 4-模版参考图
      customPrompt: "",
      generating: false
    };
  },
  onLoad(options) {
    if (options.templateId) {
      this.loadTemplate(options.templateId);
    }
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
          common_vendor.index.__f__("error", "at pages/image-generate/image-generate.vue:225", "选择图片失败", err);
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
        common_vendor.index.__f__("error", "at pages/image-generate/image-generate.vue:249", "上传图片失败", e);
        common_vendor.index.hideLoading();
        common_vendor.index.showToast({
          title: e.message || "上传图片失败",
          icon: "none"
        });
        this.imageUrl = "";
        this.imageTempPath = "";
      }
    },
    async loadTemplate(templateId) {
      try {
        const res = await common_utils_api.api.getTemplates();
        if (res.code === 200) {
          const template = res.data.find((t) => t.id === parseInt(templateId));
          if (template) {
            this.selectedTemplate = template;
          }
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/image-generate/image-generate.vue:270", "加载模版失败", e);
      }
    },
    goToTemplateList() {
      common_vendor.index.navigateTo({
        url: "/pages/image-generate/template-list"
      });
    },
    goToTaskList() {
      common_vendor.index.navigateTo({
        url: "/pages/image-generate/task-list"
      });
    },
    async generateFromText() {
      if (!this.prompt.trim()) {
        common_vendor.index.showToast({
          title: "请输入提示词",
          icon: "none"
        });
        return;
      }
      this.generating = true;
      try {
        const res = await common_utils_api.api.createImageTask({
          generateMode: 1,
          prompt: this.prompt,
          style: this.selectedStyle
        });
        if (res.code === 200) {
          common_vendor.index.showToast({
            title: "任务已创建，请到任务列表查看",
            icon: "success"
          });
          setTimeout(() => {
            this.goToTaskList();
          }, 1500);
        } else {
          common_vendor.index.showToast({
            title: res.message || "创建任务失败",
            icon: "none"
          });
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/image-generate/image-generate.vue:315", "创建任务失败", e);
        common_vendor.index.showToast({
          title: "创建任务失败，请重试",
          icon: "none"
        });
      } finally {
        this.generating = false;
      }
    },
    async generateFromImage() {
      if (!this.imagePrompt.trim()) {
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
        const res = await common_utils_api.api.createImageTask({
          generateMode: 2,
          prompt: this.imagePrompt,
          imageUrl: this.imageUrl,
          style: this.imageSelectedStyle
        });
        if (res.code === 200) {
          common_vendor.index.showToast({
            title: "任务已创建，请到任务列表查看",
            icon: "success"
          });
          setTimeout(() => {
            this.goToTaskList();
          }, 1500);
        } else {
          common_vendor.index.showToast({
            title: res.message || "创建任务失败",
            icon: "none"
          });
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/image-generate/image-generate.vue:365", "创建任务失败", e);
        common_vendor.index.showToast({
          title: "创建任务失败，请重试",
          icon: "none"
        });
      } finally {
        this.generating = false;
      }
    },
    async generateFromTemplate() {
      if (!this.selectedTemplate) {
        common_vendor.index.showToast({
          title: "请选择模版",
          icon: "none"
        });
        return;
      }
      this.generating = true;
      try {
        const res = await common_utils_api.api.createImageTask({
          generateMode: this.generateMode,
          templateId: this.selectedTemplate.id,
          // 模版参考图时可以传自定义提示词
          prompt: this.customPrompt || void 0
        });
        if (res.code === 200) {
          common_vendor.index.showToast({
            title: "任务已创建，请到任务列表查看",
            icon: "success"
          });
          setTimeout(() => {
            this.goToTaskList();
          }, 1500);
        } else {
          common_vendor.index.showToast({
            title: res.message || "创建任务失败",
            icon: "none"
          });
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/image-generate/image-generate.vue:407", "创建任务失败", e);
        common_vendor.index.showToast({
          title: "创建任务失败，请重试",
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
    a: $data.mode === "text" ? 1 : "",
    b: common_vendor.o(($event) => $data.mode = "text"),
    c: $data.mode === "image" ? 1 : "",
    d: common_vendor.o(($event) => $data.mode = "image"),
    e: $data.mode === "template" ? 1 : "",
    f: common_vendor.o(($event) => $data.mode = "template"),
    g: $data.mode === "text"
  }, $data.mode === "text" ? {
    h: $data.prompt,
    i: common_vendor.o(($event) => $data.prompt = $event.detail.value),
    j: common_vendor.f($data.styles, (style, k0, i0) => {
      return {
        a: common_vendor.t(style.label),
        b: style.value,
        c: $data.selectedStyle === style.value ? 1 : "",
        d: common_vendor.o(($event) => $data.selectedStyle = style.value, style.value)
      };
    }),
    k: common_vendor.t($data.generating ? "生成中..." : "立即生成"),
    l: !$data.prompt || $data.generating,
    m: common_vendor.o((...args) => $options.generateFromText && $options.generateFromText(...args))
  } : $data.mode === "image" ? common_vendor.e({
    o: $data.imageUrl
  }, $data.imageUrl ? {
    p: $data.imageUrl
  } : {}, {
    q: common_vendor.o((...args) => $options.chooseImage && $options.chooseImage(...args)),
    r: $data.imagePrompt,
    s: common_vendor.o(($event) => $data.imagePrompt = $event.detail.value),
    t: common_vendor.f($data.styles, (style, k0, i0) => {
      return {
        a: common_vendor.t(style.label),
        b: style.value,
        c: $data.imageSelectedStyle === style.value ? 1 : "",
        d: common_vendor.o(($event) => $data.imageSelectedStyle = style.value, style.value)
      };
    }),
    v: common_vendor.t($data.generating ? "生成中..." : "立即生成"),
    w: !$data.imagePrompt || !$data.imageUrl || $data.generating,
    x: common_vendor.o((...args) => $options.generateFromImage && $options.generateFromImage(...args))
  }) : common_vendor.e({
    y: common_vendor.o((...args) => $options.goToTemplateList && $options.goToTemplateList(...args)),
    z: $data.selectedTemplate
  }, $data.selectedTemplate ? {
    A: $data.selectedTemplate.imageUrl,
    B: common_vendor.t($data.selectedTemplate.name),
    C: common_vendor.t($data.selectedTemplate.description)
  } : {
    D: common_vendor.o((...args) => $options.goToTemplateList && $options.goToTemplateList(...args))
  }, {
    E: $data.selectedTemplate
  }, $data.selectedTemplate ? {
    F: $data.generateMode === 3 ? 1 : "",
    G: common_vendor.o(($event) => $data.generateMode = 3),
    H: $data.generateMode === 4 ? 1 : "",
    I: common_vendor.o(($event) => $data.generateMode = 4)
  } : {}, {
    J: $data.selectedTemplate && $data.generateMode === 4
  }, $data.selectedTemplate && $data.generateMode === 4 ? {
    K: $data.customPrompt,
    L: common_vendor.o(($event) => $data.customPrompt = $event.detail.value)
  } : {}, {
    M: $data.selectedTemplate
  }, $data.selectedTemplate ? {
    N: common_vendor.t($data.generating ? "生成中..." : "立即生成"),
    O: $data.generating,
    P: common_vendor.o((...args) => $options.generateFromTemplate && $options.generateFromTemplate(...args))
  } : {}, {
    Q: common_vendor.o((...args) => $options.goToTaskList && $options.goToTaskList(...args))
  }), {
    n: $data.mode === "image"
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-0b9d8811"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/image-generate/image-generate.js.map
