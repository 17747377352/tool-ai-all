"use strict";
const common_vendor = require("../vendor.js");
const common_utils_api = require("./api.js");
async function uploadToOss(filePath, fileName) {
  try {
    if (!fileName) {
      const timestamp = Date.now();
      const random = Math.random().toString(36).substring(2, 8);
      const date = /* @__PURE__ */ new Date();
      const dateStr = `${date.getFullYear()}${String(date.getMonth() + 1).padStart(2, "0")}${String(date.getDate()).padStart(2, "0")}`;
      const ext = filePath.substring(filePath.lastIndexOf("."));
      fileName = `upload/${dateStr}/${timestamp}_${random}${ext}`;
    }
    const res = await common_utils_api.api.getPostObjectSignature(fileName);
    if (res.code !== 200 || !res.data) {
      throw new Error("获取签名失败");
    }
    const signature = res.data;
    const host = signature.host;
    const key = signature.key || fileName;
    return new Promise((resolve, reject) => {
      common_vendor.index.uploadFile({
        url: `https://${host}`,
        filePath,
        name: "file",
        formData: {
          "key": key,
          "policy": signature.policy,
          "OSSAccessKeyId": signature.accessKeyId,
          "signature": signature.signature,
          "success_action_status": "200"
        },
        success: (uploadRes) => {
          if (uploadRes.statusCode === 200 || uploadRes.statusCode === 204) {
            const fileUrl = `https://${host}/${key}`;
            resolve(fileUrl);
          } else {
            reject(new Error("上传失败，状态码: " + uploadRes.statusCode));
          }
        },
        fail: (error) => {
          common_vendor.index.__f__("error", "at common/utils/oss-upload.js:59", "上传文件到OSS失败:", error);
          reject(new Error("上传失败: " + (error.errMsg || "未知错误")));
        }
      });
    });
  } catch (error) {
    common_vendor.index.__f__("error", "at common/utils/oss-upload.js:65", "上传文件到OSS失败:", error);
    throw new Error("上传失败: " + (error.message || "未知错误"));
  }
}
exports.uploadToOss = uploadToOss;
//# sourceMappingURL=../../../.sourcemap/mp-weixin/common/utils/oss-upload.js.map
