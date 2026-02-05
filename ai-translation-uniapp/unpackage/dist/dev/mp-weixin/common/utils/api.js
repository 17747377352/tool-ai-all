"use strict";
const common_vendor = require("../vendor.js");
const common_utils_request = require("./request.js");
const common_config_apiConfig = require("../config/api-config.js");
const api = {
  // 认证
  wxLogin: (code) => common_utils_request.request({ url: "/auth/wx-login", method: "POST", data: { code } }),
  decryptUserInfo: (data) => common_utils_request.request({ url: "/auth/decrypt-userinfo", method: "POST", data }),
  // 工具
  // 即时翻译
  translate: (data) => common_utils_request.request({ url: "/tool/translate", method: "POST", data }),
  // 老照片修复
  restoreOldPhoto: (data) => common_utils_request.request({ url: "/tool/restore-old-photo", method: "POST", data }),
  batchRestoreOldPhoto: (data) => common_utils_request.request({ url: "/tool/batch-restore-old-photo", method: "POST", data }),
  // 图片生成（统一任务创建接口）
  createImageTask: (data) => common_utils_request.request({ url: "/tool/image-generate", method: "POST", data }),
  // OSS PostObject签名
  getPostObjectSignature: (fileName) => common_utils_request.request({
    url: "/tool/oss/post-signature" + (fileName ? "?fileName=" + encodeURIComponent(fileName) : ""),
    method: "GET"
  }),
  // OSS STS凭证（已废弃）
  getStsCredentials: () => common_utils_request.request({ url: "/tool/oss/sts-credentials", method: "GET" }),
  // 图片上传（已废弃，建议使用OSS直传）
  uploadImage: (filePath) => {
    return new Promise((resolve, reject) => {
      common_vendor.index.uploadFile({
        url: common_config_apiConfig.apiConfig.BASE_URL + "/tool/upload-image",
        filePath,
        name: "file",
        header: {
          "Authorization": `Bearer ${common_vendor.index.getStorageSync("token")}`
        },
        success: (res) => {
          try {
            const data = JSON.parse(res.data);
            if (data.code === 200) {
              resolve(data);
            } else {
              reject(data);
            }
          } catch (e) {
            reject({ message: "上传失败" });
          }
        },
        fail: reject
      });
    });
  },
  // 图片模版
  getTemplates: () => common_utils_request.request({ url: "/tool/templates", method: "GET" }),
  // templateGenerate: (data) => request({ url: '/tool/template-generate', method: 'POST', data }),
  // 任务相关
  getTasks: (status) => common_utils_request.request({
    url: `/tool/tasks${status !== void 0 && status !== null ? "?status=" + status : ""}`,
    method: "GET"
  }),
  getTaskDetail: (taskId) => common_utils_request.request({ url: `/tool/task/${taskId}`, method: "GET" }),
  downloadImage: (taskId) => common_utils_request.request({ url: `/tool/task/${taskId}/download`, method: "GET" }),
  // 蒙古语 AI 对话
  mongolianChat: (data) => common_utils_request.request({ url: "/api/ai/mongolian-chat", method: "POST", data }),
  // AI 识图
  imageRecognize: (data) => common_utils_request.request({ url: "/tool/image-recognize", method: "POST", data }),
  // 反馈
  submitFeedback: (data) => common_utils_request.request({ url: "/feedback/submit", method: "POST", data })
};
exports.api = api;
//# sourceMappingURL=../../../.sourcemap/mp-weixin/common/utils/api.js.map
