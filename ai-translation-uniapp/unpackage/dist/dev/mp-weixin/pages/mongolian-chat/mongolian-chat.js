"use strict";
const common_vendor = require("../../common/vendor.js");
const common_utils_api = require("../../common/utils/api.js");
const _sfc_main = {
  data() {
    return {
      messages: [],
      inputText: "",
      sending: false,
      scrollIntoView: ""
    };
  },
  onLoad() {
    this.loadMessagesFromStorage();
    if (this.messages.length === 0) {
      const welcome = "嗨，我是你的ai蒙文小助手，需要我帮您做些什么？";
      const welcomeMsg = {
        role: "assistant",
        original: welcome,
        mo: "",
        loadingMo: false
      };
      this.messages.push(welcomeMsg);
      this.saveMessagesToStorage();
    }
    this.$nextTick(() => {
      this.scrollToBottom();
    });
  },
  methods: {
    // 从本地存储加载会话历史
    loadMessagesFromStorage() {
      try {
        const stored = common_vendor.index.getStorageSync("mongolian_chat_messages");
        if (stored && Array.isArray(stored) && stored.length > 0) {
          this.messages = stored;
          common_vendor.index.__f__("log", "at pages/mongolian-chat/mongolian-chat.vue:99", "已恢复会话历史，共", stored.length, "条消息");
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/mongolian-chat/mongolian-chat.vue:102", "加载会话历史失败", e);
      }
    },
    // 保存会话历史到本地存储
    saveMessagesToStorage() {
      try {
        common_vendor.index.setStorageSync("mongolian_chat_messages", this.messages);
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/mongolian-chat/mongolian-chat.vue:110", "保存会话历史失败", e);
      }
    },
    // 清空会话历史
    clearMessages() {
      common_vendor.index.showModal({
        title: "提示",
        content: "确定要清空所有对话记录吗？",
        success: (res) => {
          if (res.confirm) {
            this.messages = [];
            const welcome = "嗨，我是你的ai蒙文小助手，需要我帮您做些什么？";
            const welcomeMsg = {
              role: "assistant",
              original: welcome,
              mo: "",
              loadingMo: false
            };
            this.messages.push(welcomeMsg);
            this.saveMessagesToStorage();
            this.$nextTick(() => {
              this.scrollToBottom();
            });
          }
        }
      });
    },
    scrollToBottom() {
      if (this.messages.length === 0)
        return;
      this.scrollIntoView = "msg-" + (this.messages.length - 1);
    },
    handleTranslate(item) {
      if (!item || item.loadingMo || !item.original || !item.original.trim())
        return;
      item.loadingMo = true;
      common_utils_api.api.translate({
        text: item.original,
        from: "zh",
        // 如需自动检测，可改为 'auto'
        to: "mo"
      }).then((res) => {
        if (res && res.data && res.data.result) {
          item.mo = res.data.result;
          this.saveMessagesToStorage();
        } else {
          common_vendor.index.showToast({
            title: "翻译失败",
            icon: "none"
          });
        }
      }).catch((err) => {
        common_vendor.index.__f__("error", "at pages/mongolian-chat/mongolian-chat.vue:163", "translate error", err);
        common_vendor.index.showToast({
          title: "翻译失败",
          icon: "none"
        });
      }).finally(() => {
        item.loadingMo = false;
      });
    },
    handleSend() {
      const content = this.inputText.trim();
      if (!content || this.sending)
        return;
      const userMsg = {
        role: "user",
        original: content,
        mo: "",
        loadingMo: false
      };
      this.messages.push(userMsg);
      this.saveMessagesToStorage();
      this.inputText = "";
      this.$nextTick(() => {
        this.scrollToBottom();
      });
      this.sending = true;
      const payloadMessages = this.messages.map((m) => ({
        role: m.role,
        content: m.original
      }));
      common_utils_api.api.mongolianChat({
        messages: payloadMessages
      }).then((res) => {
        if (!res || res.code !== 200 || !res.data) {
          throw new Error(res && res.message ? res.message : "请求失败");
        }
        const { assistantText } = res.data;
        const assistantMsg = {
          role: "assistant",
          original: assistantText || "",
          mo: "",
          loadingMo: false
        };
        this.messages.push(assistantMsg);
        this.saveMessagesToStorage();
        this.$nextTick(() => {
          this.scrollToBottom();
        });
      }).catch((err) => {
        common_vendor.index.__f__("error", "at pages/mongolian-chat/mongolian-chat.vue:224", "mongolianChat error", err);
        common_vendor.index.showToast({
          title: "对话失败，请稍后重试",
          icon: "none"
        });
      }).finally(() => {
        this.sending = false;
      });
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.f($data.messages, (item, index, i0) => {
      return common_vendor.e({
        a: common_vendor.t(item.original),
        b: item.mo
      }, item.mo ? {
        c: common_vendor.t(item.mo)
      } : {
        d: common_vendor.t(item.loadingMo ? "翻译中..." : "翻译"),
        e: item.loadingMo,
        f: item.loadingMo || !item.original,
        g: common_vendor.o(($event) => $options.handleTranslate(item), index)
      }, {
        h: index,
        i: "msg-" + index,
        j: common_vendor.n(item.role === "user" ? "msg-right" : "msg-left")
      });
    }),
    b: $data.scrollIntoView,
    c: common_vendor.o((...args) => $options.handleSend && $options.handleSend(...args)),
    d: $data.inputText,
    e: common_vendor.o(($event) => $data.inputText = $event.detail.value),
    f: common_vendor.t($data.sending ? "发送中..." : "发送"),
    g: $data.sending || !$data.inputText.trim(),
    h: common_vendor.o((...args) => $options.handleSend && $options.handleSend(...args)),
    i: $data.messages.length > 1
  }, $data.messages.length > 1 ? {
    j: common_vendor.o((...args) => $options.clearMessages && $options.clearMessages(...args))
  } : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-84491c9e"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/mongolian-chat/mongolian-chat.js.map
