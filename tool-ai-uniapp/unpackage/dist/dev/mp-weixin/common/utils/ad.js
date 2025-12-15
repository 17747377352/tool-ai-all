"use strict";
const common_vendor = require("../vendor.js");
const showRewardedVideo = (adUnitId) => {
  return new Promise((resolve, reject) => {
    {
      common_vendor.index.__f__("log", "at common/utils/ad.js:11", "广告功能已关闭（开发模式）");
      resolve(true);
      return;
    }
  });
};
const showInterstitialAd = (adUnitId, shouldShow) => {
  {
    common_vendor.index.__f__("log", "at common/utils/ad.js:67", "广告功能已关闭（开发模式）");
    return Promise.resolve();
  }
};
exports.showInterstitialAd = showInterstitialAd;
exports.showRewardedVideo = showRewardedVideo;
//# sourceMappingURL=../../../.sourcemap/mp-weixin/common/utils/ad.js.map
