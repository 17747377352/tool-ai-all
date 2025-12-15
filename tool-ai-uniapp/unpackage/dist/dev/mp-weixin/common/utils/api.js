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
  generateConstellationFortune: (data) => common_utils_request.request({ url: "/tool/constellation-fortune", method: "POST", data })
};
exports.api = api;
//# sourceMappingURL=../../../.sourcemap/mp-weixin/common/utils/api.js.map
