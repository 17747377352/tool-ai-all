"use strict";
const common_utils_request = require("./request.js");
const api = {
  // 认证
  wxLogin: (code) => common_utils_request.request({ url: "/auth/wx-login", method: "POST", data: { code } }),
  decryptUserInfo: (data) => common_utils_request.request({ url: "/auth/decrypt-userinfo", method: "POST", data }),
  // 工具
  removeLogo: (data) => common_utils_request.request({ url: "/tool/remove-logo", method: "POST", data }),
  generateAiAvatar: (data) => common_utils_request.request({ url: "/tool/ai-avatar", method: "POST", data }),
  generateNameSign: (data) => common_utils_request.request({ url: "/tool/name-sign", method: "POST", data }),
  generateFortune: (data) => common_utils_request.request({ url: "/tool/fortune", method: "POST", data }),
  generateConstellationFortune: (data) => common_utils_request.request({ url: "/tool/constellation-fortune", method: "POST", data }),
  restoreOldPhoto: (data) => common_utils_request.request({ url: "/tool/restore-old-photo", method: "POST", data }),
  batchRestoreOldPhoto: (data) => common_utils_request.request({ url: "/tool/batch-restore-old-photo", method: "POST", data }),
  // 广告相关
  recordAdWatch: (adType) => common_utils_request.request({ url: "/ad/watch?adType=" + adType, method: "POST", data: {} }),
  checkRemoveLogoAd: () => common_utils_request.request({ url: "/ad/check-remove-logo", method: "GET" }),
  checkBatchRestoreAd: () => common_utils_request.request({ url: "/ad/check-batch-restore", method: "GET" }),
  // 生活查询
  lifeExpress: (data) => common_utils_request.request({ url: "/life/express", method: "POST", data }),
  lifeOilPrice: (params) => common_utils_request.request({ url: "/life/oil-price", method: "GET", data: params || {} }),
  lifeForex: (params) => common_utils_request.request({ url: "/life/forex", method: "GET", data: params || {} }),
  lifeLottery: (params) => common_utils_request.request({ url: "/life/lottery", method: "GET", data: params || {} })
};
exports.api = api;
//# sourceMappingURL=../../../.sourcemap/mp-weixin/common/utils/api.js.map
