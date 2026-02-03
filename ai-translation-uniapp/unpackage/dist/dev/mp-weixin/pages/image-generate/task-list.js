"use strict";
const common_vendor = require("../../common/vendor.js");
const common_utils_api = require("../../common/utils/api.js");
const _sfc_main = {
  data() {
    return {
      tasks: [],
      statusFilter: null,
      loading: true
    };
  },
  watch: {
    statusFilter() {
      this.loadTasks();
    }
  },
  onLoad() {
    this.loadTasks();
  },
  onShow() {
    this.loadTasks();
  },
  methods: {
    async loadTasks() {
      this.loading = true;
      try {
        const res = await common_utils_api.api.getTasks(this.statusFilter);
        if (res.code === 200) {
          this.tasks = res.data;
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/image-generate/task-list.vue:96", "加载任务失败", e);
        common_vendor.index.showToast({
          title: "加载失败",
          icon: "none"
        });
      } finally {
        this.loading = false;
      }
    },
    viewTaskDetail(taskId) {
      common_vendor.index.navigateTo({
        url: `/pages/result/result?type=task&taskId=${taskId}`
      });
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
    formatTime(timeStr) {
      if (!timeStr)
        return "";
      const date = new Date(timeStr);
      const now = /* @__PURE__ */ new Date();
      const diff = now - date;
      const minutes = Math.floor(diff / 6e4);
      if (minutes < 1)
        return "刚刚";
      if (minutes < 60)
        return `${minutes}分钟前`;
      const hours = Math.floor(minutes / 60);
      if (hours < 24)
        return `${hours}小时前`;
      const days = Math.floor(hours / 24);
      if (days < 7)
        return `${days}天前`;
      return date.toLocaleDateString();
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
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: $data.statusFilter === null ? 1 : "",
    b: common_vendor.o(($event) => $data.statusFilter = null),
    c: $data.statusFilter === 0 ? 1 : "",
    d: common_vendor.o(($event) => $data.statusFilter = 0),
    e: $data.statusFilter === 1 ? 1 : "",
    f: common_vendor.o(($event) => $data.statusFilter = 1),
    g: $data.statusFilter === 2 ? 1 : "",
    h: common_vendor.o(($event) => $data.statusFilter = 2),
    i: $data.loading
  }, $data.loading ? {} : $data.tasks.length === 0 ? {} : {
    k: common_vendor.f($data.tasks, (task, k0, i0) => {
      return common_vendor.e({
        a: common_vendor.t($options.getStatusText(task.taskStatus)),
        b: common_vendor.n($options.getStatusClass(task.taskStatus)),
        c: common_vendor.t($options.formatTime(task.createTime)),
        d: common_vendor.t(task.prompt || "无提示词"),
        e: task.resultUrl && task.taskStatus === 2
      }, task.resultUrl && task.taskStatus === 2 ? {
        f: $options.extractImageUrl(task.resultUrl)
      } : {}, {
        g: task.id,
        h: common_vendor.o(($event) => $options.viewTaskDetail(task.id), task.id)
      });
    })
  }, {
    j: $data.tasks.length === 0
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-e9f06d79"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/image-generate/task-list.js.map
