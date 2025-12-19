"use strict";
const common_vendor = require("../../common/vendor.js");
const common_utils_api = require("../../common/utils/api.js");
const AdVideoBanner = () => "../../common/components/ad-video-banner.js";
const _sfc_main = {
  components: {
    AdVideoBanner
  },
  data() {
    return {
      currentTab: 0,
      tabs: [
        { name: "快递查询", icon: "📦" },
        { name: "今日油价", icon: "⛽" },
        { name: "汇率换算", icon: "💱" },
        { name: "彩票开奖", icon: "🎰" }
      ],
      // 快递查询
      express: {
        trackingNo: "",
        phone: ""
      },
      expressResult: null,
      // 油价查询
      oil: {
        province: ""
      },
      oilResult: null,
      // 汇率查询
      forex: {
        base: "CNY",
        target: "USD",
        amount: "100"
      },
      forexResult: null,
      currencyList: ["CNY", "USD", "EUR", "GBP", "JPY", "HKD", "KRW", "AUD", "CAD", "SGD", "CHF", "NZD", "THB", "MYR", "INR"],
      currencyLabels: ["CNY(人民币)", "USD(美元)", "EUR(欧元)", "GBP(英镑)", "JPY(日元)", "HKD(港币)", "KRW(韩元)", "AUD(澳元)", "CAD(加元)", "SGD(新加坡元)", "CHF(瑞士法郎)", "NZD(新西兰元)", "THB(泰铢)", "MYR(马来西亚林吉特)", "INR(印度卢比)"],
      baseCurrency: "CNY",
      targetCurrency: "USD",
      baseCurrencyIndex: 0,
      targetCurrencyIndex: 1,
      // 彩票查询
      lotteryTypes: ["双色球", "大乐透"],
      lotteryTypeIndex: 0,
      lotteryType: "ssq",
      lotteryResult: null
    };
  },
  methods: {
    switchTab(index) {
      this.currentTab = index;
      this.expressResult = null;
      this.oilResult = null;
      this.forexResult = null;
      this.lotteryResult = null;
    },
    onBaseChange(e) {
      const index = parseInt(e.detail.value);
      this.baseCurrencyIndex = index;
      this.baseCurrency = this.currencyList[index];
      this.forex.base = this.baseCurrency;
    },
    onTargetChange(e) {
      const index = parseInt(e.detail.value);
      this.targetCurrencyIndex = index;
      this.targetCurrency = this.currencyList[index];
      this.forex.target = this.targetCurrency;
    },
    onLotteryTypeChange(e) {
      const index = parseInt(e.detail.value);
      this.lotteryTypeIndex = index;
      this.lotteryType = index === 0 ? "ssq" : "dlt";
    },
    getCurrencyLabel(code) {
      const labels = {
        "CNY": "人民币",
        "USD": "美元",
        "EUR": "欧元",
        "GBP": "英镑",
        "JPY": "日元",
        "HKD": "港币",
        "KRW": "韩元",
        "AUD": "澳元",
        "CAD": "加元",
        "SGD": "新加坡元",
        "CHF": "瑞士法郎",
        "NZD": "新西兰元",
        "THB": "泰铢",
        "MYR": "马来西亚林吉特",
        "INR": "印度卢比"
      };
      return labels[code] || code;
    },
    async queryExpress() {
      if (!this.express.trackingNo) {
        common_vendor.index.showToast({
          title: "请输入快递单号",
          icon: "none"
        });
        return;
      }
      if (!this.express.phone) {
        common_vendor.index.showToast({
          title: "请输入手机号码",
          icon: "none"
        });
        return;
      }
      if (!/^\d{11}$/.test(this.express.phone)) {
        common_vendor.index.showToast({
          title: "手机号码必须为11位数字",
          icon: "none"
        });
        return;
      }
      common_vendor.index.showLoading({ title: "查询中..." });
      try {
        const res = await common_utils_api.api.lifeExpress({
          trackingNo: this.express.trackingNo,
          phone: this.express.phone
        });
        if (res.code === 200) {
          this.expressResult = res.data || [];
          if (!this.expressResult || this.expressResult.length === 0) {
            common_vendor.index.showToast({
              title: "暂无物流信息",
              icon: "none"
            });
          }
        } else {
          common_vendor.index.showToast({
            title: res.message || "查询失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.showToast({
          title: "查询失败，请稍后重试",
          icon: "none"
        });
      } finally {
        common_vendor.index.hideLoading();
      }
    },
    async queryOilPrice() {
      common_vendor.index.showLoading({ title: "查询中..." });
      try {
        const res = await common_utils_api.api.lifeOilPrice({
          province: this.oil.province || "北京"
        });
        if (res.code === 200) {
          this.oilResult = res.data || {};
          if (!this.oilResult || Object.keys(this.oilResult).length === 0) {
            common_vendor.index.showToast({
              title: "暂无油价信息",
              icon: "none"
            });
          }
        } else {
          common_vendor.index.showToast({
            title: res.message || "查询失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.showToast({
          title: "查询失败，请稍后重试",
          icon: "none"
        });
      } finally {
        common_vendor.index.hideLoading();
      }
    },
    async queryForex() {
      if (!this.forex.amount || parseFloat(this.forex.amount) <= 0) {
        common_vendor.index.showToast({
          title: "请输入有效金额",
          icon: "none"
        });
        return;
      }
      common_vendor.index.showLoading({ title: "查询中..." });
      try {
        const res = await common_utils_api.api.lifeForex({
          base: this.forex.base,
          target: this.forex.target,
          amount: parseFloat(this.forex.amount)
        });
        if (res.code === 200) {
          this.forexResult = res.data || {};
        } else {
          common_vendor.index.showToast({
            title: res.message || "查询失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.showToast({
          title: "查询失败，请稍后重试",
          icon: "none"
        });
      } finally {
        common_vendor.index.hideLoading();
      }
    },
    getLotteryTypeName(type) {
      const typeMap = {
        "ssq": "双色球",
        "dlt": "大乐透",
        "双色球": "双色球",
        "大乐透": "大乐透"
      };
      return typeMap[type] || type || "未知";
    },
    getRedNumbers(numbers, type) {
      if (!numbers)
        return [];
      const nums = numbers.trim().split(/[\s,]+/).filter((n) => n && n.trim());
      if (type === "ssq" || type === "双色球") {
        return nums.slice(0, 6);
      } else if (type === "dlt" || type === "大乐透") {
        return nums.slice(0, 5);
      }
      return nums;
    },
    getBlueNumbers(numbers, type) {
      if (!numbers)
        return [];
      const nums = numbers.trim().split(/[\s,]+/).filter((n) => n && n.trim());
      if (type === "ssq" || type === "双色球") {
        return nums.slice(6);
      } else if (type === "dlt" || type === "大乐透") {
        return nums.slice(5, 7);
      }
      return [];
    },
    async queryLottery() {
      common_vendor.index.showLoading({ title: "查询中..." });
      try {
        const res = await common_utils_api.api.lifeLottery({
          type: this.lotteryType
        });
        if (res.code === 200) {
          this.lotteryResult = res.data || {};
          if (!this.lotteryResult || !this.lotteryResult.issue) {
            common_vendor.index.showToast({
              title: "暂无开奖信息",
              icon: "none"
            });
          }
        } else {
          common_vendor.index.showToast({
            title: res.message || "查询失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.showToast({
          title: "查询失败，请稍后重试",
          icon: "none"
        });
      } finally {
        common_vendor.index.hideLoading();
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
    a: common_vendor.f($data.tabs, (tab, index, i0) => {
      return {
        a: common_vendor.t(tab.icon),
        b: common_vendor.t(tab.name),
        c: index,
        d: $data.currentTab === index ? 1 : "",
        e: common_vendor.o(($event) => $options.switchTab(index), index)
      };
    }),
    b: $data.currentTab === 0
  }, $data.currentTab === 0 ? common_vendor.e({
    c: $data.express.trackingNo,
    d: common_vendor.o(($event) => $data.express.trackingNo = $event.detail.value),
    e: $data.express.phone,
    f: common_vendor.o(($event) => $data.express.phone = $event.detail.value),
    g: common_vendor.o((...args) => $options.queryExpress && $options.queryExpress(...args)),
    h: $data.expressResult
  }, $data.expressResult ? {
    i: common_vendor.f($data.expressResult, (item, idx, i0) => {
      return {
        a: common_vendor.t(item.time),
        b: common_vendor.t(item.context),
        c: idx
      };
    })
  } : {}) : {}, {
    j: $data.currentTab === 1
  }, $data.currentTab === 1 ? common_vendor.e({
    k: $data.oil.province,
    l: common_vendor.o(($event) => $data.oil.province = $event.detail.value),
    m: common_vendor.o((...args) => $options.queryOilPrice && $options.queryOilPrice(...args)),
    n: $data.oilResult
  }, $data.oilResult ? {
    o: common_vendor.t($data.oilResult["92h"]),
    p: common_vendor.t($data.oilResult["95h"]),
    q: common_vendor.t($data.oilResult["98h"]),
    r: common_vendor.t($data.oilResult["0h"])
  } : {}) : {}, {
    s: $data.currentTab === 2
  }, $data.currentTab === 2 ? common_vendor.e({
    t: common_vendor.t($options.getCurrencyLabel($data.baseCurrency)),
    v: $data.currencyLabels,
    w: $data.baseCurrencyIndex,
    x: common_vendor.o((...args) => $options.onBaseChange && $options.onBaseChange(...args)),
    y: common_vendor.t($options.getCurrencyLabel($data.targetCurrency)),
    z: $data.currencyLabels,
    A: $data.targetCurrencyIndex,
    B: common_vendor.o((...args) => $options.onTargetChange && $options.onTargetChange(...args)),
    C: $data.forex.amount,
    D: common_vendor.o(($event) => $data.forex.amount = $event.detail.value),
    E: common_vendor.o((...args) => $options.queryForex && $options.queryForex(...args)),
    F: $data.forexResult
  }, $data.forexResult ? common_vendor.e({
    G: common_vendor.t($data.forexResult.base),
    H: common_vendor.t($data.forexResult.rate),
    I: common_vendor.t($data.forexResult.target),
    J: $data.forexResult.amount
  }, $data.forexResult.amount ? {
    K: common_vendor.t($data.forexResult.amount),
    L: common_vendor.t($data.forexResult.base)
  } : {}, {
    M: $data.forexResult.converted
  }, $data.forexResult.converted ? {
    N: common_vendor.t($data.forexResult.converted),
    O: common_vendor.t($data.forexResult.target)
  } : {}) : {}) : {}, {
    P: $data.currentTab === 3
  }, $data.currentTab === 3 ? common_vendor.e({
    Q: common_vendor.t($data.lotteryTypes[$data.lotteryTypeIndex]),
    R: $data.lotteryTypes,
    S: $data.lotteryTypeIndex,
    T: common_vendor.o((...args) => $options.onLotteryTypeChange && $options.onLotteryTypeChange(...args)),
    U: common_vendor.o((...args) => $options.queryLottery && $options.queryLottery(...args)),
    V: $data.lotteryResult && $data.lotteryResult.issue
  }, $data.lotteryResult && $data.lotteryResult.issue ? common_vendor.e({
    W: common_vendor.t($options.getLotteryTypeName($data.lotteryResult.type)),
    X: common_vendor.t($data.lotteryResult.issue),
    Y: common_vendor.t($data.lotteryResult.openTime || "暂无"),
    Z: $data.lotteryResult.numbers
  }, $data.lotteryResult.numbers ? common_vendor.e({
    aa: common_vendor.f($options.getRedNumbers($data.lotteryResult.numbers, $data.lotteryResult.type), (num, idx, i0) => {
      return {
        a: common_vendor.t(num),
        b: "red-" + idx
      };
    }),
    ab: $options.getBlueNumbers($data.lotteryResult.numbers, $data.lotteryResult.type).length > 0
  }, $options.getBlueNumbers($data.lotteryResult.numbers, $data.lotteryResult.type).length > 0 ? {} : {}, {
    ac: common_vendor.f($options.getBlueNumbers($data.lotteryResult.numbers, $data.lotteryResult.type), (num, idx, i0) => {
      return {
        a: common_vendor.t(num),
        b: "blue-" + idx
      };
    })
  }) : {}, {
    ad: $data.lotteryResult.detail
  }, $data.lotteryResult.detail ? {
    ae: common_vendor.t($data.lotteryResult.detail)
  } : {}) : {}) : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-c9b3d7a8"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/life/life.js.map
