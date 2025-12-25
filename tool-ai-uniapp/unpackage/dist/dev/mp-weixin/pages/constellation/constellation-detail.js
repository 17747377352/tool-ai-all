"use strict";
const common_vendor = require("../../common/vendor.js");
const AdVideoBanner = () => "../../common/components/ad-video-banner.js";
const _sfc_main = {
  components: {
    AdVideoBanner
  },
  data() {
    return {
      constellationName: "",
      date: "",
      overallScore: 0,
      loveScore: 0,
      careerScore: 0,
      wealthScore: 0,
      healthScore: 0,
      luckyColor: "",
      luckyNumber: "",
      compatibleConstellation: "",
      suitable: "",
      avoid: "",
      overallDetail: "",
      loveDetail: "",
      careerDetail: "",
      wealthDetail: "",
      healthDetail: ""
    };
  },
  onLoad(options) {
    if (options.data) {
      try {
        const data = JSON.parse(decodeURIComponent(options.data));
        this.constellationName = data.constellation || "";
        this.date = data.date || "";
        this.overallScore = data.overallScore || 0;
        this.loveScore = data.loveScore || 0;
        this.careerScore = data.careerScore || 0;
        this.wealthScore = data.wealthScore || 0;
        this.healthScore = data.healthScore || 0;
        this.luckyColor = data.luckyColor || "";
        this.luckyNumber = data.luckyNumber || "";
        this.compatibleConstellation = data.compatibleConstellation || "";
        this.suitable = data.suitable || "";
        this.avoid = data.avoid || "";
        this.overallDetail = data.overallDetail || "";
        this.loveDetail = data.loveDetail || "";
        this.careerDetail = data.careerDetail || "";
        this.wealthDetail = data.wealthDetail || "";
        this.healthDetail = data.healthDetail || "";
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/constellation/constellation-detail.vue:245", "解析数据失败", e);
        common_vendor.index.showToast({
          title: "数据解析失败",
          icon: "none"
        });
      }
    }
  },
  methods: {
    /**
     * 获取星座图标
     */
    getConstellationIcon(name) {
      const icons = {
        "白羊座": "♈",
        "金牛座": "♉",
        "双子座": "♊",
        "巨蟹座": "♋",
        "狮子座": "♌",
        "处女座": "♍",
        "天秤座": "♎",
        "天蝎座": "♏",
        "射手座": "♐",
        "摩羯座": "♑",
        "水瓶座": "♒",
        "双鱼座": "♓"
      };
      return icons[name] || "⭐";
    },
    /**
     * 格式化日期
     */
    formatDate(dateStr) {
      if (!dateStr)
        return "";
      try {
        const date = new Date(dateStr);
        const month = date.getMonth() + 1;
        const day = date.getDate();
        const weekdays = ["日", "一", "二", "三", "四", "五", "六"];
        const weekday = weekdays[date.getDay()];
        return `${month}月${day}日 星期${weekday}`;
      } catch (e) {
        return dateStr;
      }
    },
    /**
     * 获取颜色值（简单映射）
     */
    getColorValue(colorName) {
      const colorMap = {
        "红色": "#ff6b9d",
        "蓝色": "#4facfe",
        "绿色": "#48d1cc",
        "黄色": "#feca57",
        "紫色": "#667eea",
        "橙色": "#ff9a56",
        "粉色": "#ff8fab",
        "青色": "#20b2aa",
        "白色": "#f5f5f5",
        "黑色": "#333333",
        "金色": "#ffd700",
        "银色": "#c0c0c0"
      };
      return colorMap[colorName] || "#667eea";
    }
  }
};
if (!Array) {
  const _component_ad_video_banner = common_vendor.resolveComponent("ad-video-banner");
  _component_ad_video_banner();
}
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return {
    a: common_vendor.t($options.getConstellationIcon($data.constellationName)),
    b: common_vendor.t($data.constellationName),
    c: common_vendor.t($options.formatDate($data.date)),
    d: $data.overallScore + "%",
    e: common_vendor.t($data.overallScore),
    f: $data.loveScore + "%",
    g: common_vendor.t($data.loveScore),
    h: $data.careerScore + "%",
    i: common_vendor.t($data.careerScore),
    j: $data.wealthScore + "%",
    k: common_vendor.t($data.wealthScore),
    l: $data.healthScore + "%",
    m: common_vendor.t($data.healthScore),
    n: $options.getColorValue($data.luckyColor),
    o: common_vendor.t($data.luckyColor),
    p: common_vendor.t($data.luckyNumber),
    q: common_vendor.t($data.compatibleConstellation),
    r: common_vendor.t($data.suitable),
    s: common_vendor.t($data.avoid),
    t: common_vendor.t($data.overallDetail),
    v: common_vendor.t($data.loveDetail),
    w: common_vendor.t($data.careerDetail),
    x: common_vendor.t($data.wealthDetail),
    y: common_vendor.t($data.healthDetail)
  };
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-18ee21d9"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/constellation/constellation-detail.js.map
