<template>
  <view class="page">
    <!-- 对话区域 -->
    <scroll-view class="chat-body" scroll-y :scroll-into-view="scrollIntoView" :scroll-with-animation="true">
      <view
        v-for="(item, index) in messages"
        :key="index"
        :id="'msg-' + index"
        class="msg-wrapper"
        :class="item.role === 'user' ? 'msg-right' : 'msg-left'"
      >
        <view class="msg-bubble">
          <!-- 原文 -->
          <view class="msg-text-original">
            <text class="msg-label">原文</text>
            <text>{{ item.original }}</text>
          </view>
          <!-- 蒙古语 -->
          <view v-if="item.mo" class="msg-text-mo">
            <text class="msg-label">蒙文</text>
            <text>{{ item.mo }}</text>
          </view>
          <view v-else class="msg-actions">
            <button
              class="translate-btn"
              size="mini"
              :loading="item.loadingMo"
              :disabled="item.loadingMo || !item.original"
              @tap="handleTranslate(item)"
            >
              {{ item.loadingMo ? '翻译中...' : '翻译' }}
            </button>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 输入区域 -->
    <view class="input-bar">
      <textarea
        class="input"
        v-model="inputText"
        :auto-height="true"
        :maxlength="1000"
        placeholder="请输入要咨询的问题（支持中文等，多轮对话会自动保留上下文）"
        confirm-type="send"
        @input="handleInputChange"
        @confirm="handleSend"
      />
      <!-- 候选词栏（拉丁转写输入法） -->
      <view v-if="ime.candidates.length > 0" class="ime-candidates">
        <view
          v-for="(c, idx) in ime.candidates"
          :key="c.id || idx"
          class="ime-candidate"
          @tap="selectCandidate(idx)"
        >
          <text class="ime-idx">{{ idx + 1 }}.</text>
          <text class="ime-word">{{ c.word }}</text>
        </view>
      </view>
      <button class="send-btn" :disabled="sending || !inputText.trim()" @tap="handleSend">
        {{ sending ? '发送中...' : '发送' }}
      </button>
      <button class="clear-btn" @tap="clearMessages" v-if="messages.length > 1">
        清空
      </button>
    </view>
  </view>
</template>

<script>
import api from '@/common/utils/api.js';

export default {
  data() {
    return {
      messages: [],
      inputText: '',
      sending: false,
      scrollIntoView: '',
      ime: {
        buffer: '',
        candidates: [],
        fetching: false
      }
    };
  },
  onLoad() {
    // 从本地存储恢复会话历史
    this.loadMessagesFromStorage();
    
    // 如果没有历史记录，显示默认欢迎语
    if (this.messages.length === 0) {
      const welcome = '嗨，我是你的ai蒙文小助手，需要我帮您做些什么？';
      const welcomeMsg = {
        role: 'assistant',
        original: welcome,
        mo: '',
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
    async handleInputChange(e) {
      // 取输入框最后一个 token 作为拉丁 buffer（用空格/换行分隔）
      const text = (this.inputText || '').toString();
      const tokens = text.split(/[\s\n]+/);
      const last = tokens.length > 0 ? tokens[tokens.length - 1] : '';
      const buffer = (last || '').trim();

      // 只在拉丁字符输入时触发（可按需扩展）
      if (!buffer || !/^[a-zA-Z]+$/.test(buffer)) {
        this.ime.buffer = '';
        this.ime.candidates = [];
        return;
      }

      this.ime.buffer = buffer.toLowerCase();
      if (this.ime.fetching) return;

      this.ime.fetching = true;
      try {
        const res = await api.imeCandidates(this.ime.buffer, 9);
        if (res.code === 200 && Array.isArray(res.data)) {
          this.ime.candidates = res.data;
        } else {
          this.ime.candidates = [];
        }
      } catch (err) {
        console.error('获取输入法候选失败', err);
        this.ime.candidates = [];
      } finally {
        this.ime.fetching = false;
      }
    },

    async selectCandidate(index) {
      const c = this.ime.candidates[index];
      if (!c) return;

      // 用候选词替换最后一个 token
      const text = (this.inputText || '').toString();
      const parts = text.split(/(\s+)/); // 保留空白分隔符
      // 找到最后一个非空白 token 的索引
      let lastTokenIdx = -1;
      for (let i = parts.length - 1; i >= 0; i--) {
        if (parts[i] && !/^\s+$/.test(parts[i])) {
          lastTokenIdx = i;
          break;
        }
      }
      if (lastTokenIdx >= 0) {
        parts[lastTokenIdx] = c.word;
        this.inputText = parts.join('');
        // 如果末尾不是空白，补一个空格方便继续输入
        if (!/\s$/.test(this.inputText)) {
          this.inputText += ' ';
        }
      }

      // 上报选择（词频学习）
      if (c.id) {
        try {
          await api.imeSelect(c.id);
        } catch (err) {
          console.error('上报输入法选择失败', err);
        }
      }

      // 清空候选
      this.ime.buffer = '';
      this.ime.candidates = [];
    },
    // 从本地存储加载会话历史
    loadMessagesFromStorage() {
      try {
        const stored = uni.getStorageSync('mongolian_chat_messages');
        if (stored && Array.isArray(stored) && stored.length > 0) {
          this.messages = stored;
          console.log('已恢复会话历史，共', stored.length, '条消息');
        }
      } catch (e) {
        console.error('加载会话历史失败', e);
      }
    },
    // 保存会话历史到本地存储
    saveMessagesToStorage() {
      try {
        uni.setStorageSync('mongolian_chat_messages', this.messages);
      } catch (e) {
        console.error('保存会话历史失败', e);
      }
    },
    // 清空会话历史
    clearMessages() {
      uni.showModal({
        title: '提示',
        content: '确定要清空所有对话记录吗？',
        success: (res) => {
          if (res.confirm) {
            this.messages = [];
            // 重新显示欢迎语
            const welcome = '嗨，我是你的ai蒙文小助手，需要我帮您做些什么？';
            const welcomeMsg = {
              role: 'assistant',
              original: welcome,
              mo: '',
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
      if (this.messages.length === 0) return;
      this.scrollIntoView = 'msg-' + (this.messages.length - 1);
    },
    handleTranslate(item) {
      if (!item || item.loadingMo || !item.original || !item.original.trim()) return;
      item.loadingMo = true;
      api
        .translate({
          text: item.original,
          from: 'zh', // 如需自动检测，可改为 'auto'
          to: 'mo'
        })
        .then((res) => {
          if (res && res.data && res.data.result) {
            item.mo = res.data.result;
            this.saveMessagesToStorage(); // 保存翻译结果
          } else {
            uni.showToast({
              title: '翻译失败',
              icon: 'none'
            });
          }
        })
        .catch((err) => {
          console.error('translate error', err);
          uni.showToast({
            title: '翻译失败',
            icon: 'none'
          });
        })
        .finally(() => {
          item.loadingMo = false;
        });
    },
    handleSend() {
      const content = this.inputText.trim();
      if (!content || this.sending) return;

      // 先把用户消息推入本地（不默认翻译，点按钮再翻译）
      const userMsg = {
        role: 'user',
        original: content,
        mo: '',
        loadingMo: false
      };
      this.messages.push(userMsg);
      this.saveMessagesToStorage(); // 保存会话历史
      this.inputText = '';
      this.$nextTick(() => {
        this.scrollToBottom();
      });

      this.sending = true;

      // 构造发给后端的 messages：仅携带 role / content（原文）
      const payloadMessages = this.messages.map((m) => ({
        role: m.role,
        content: m.original
      }));

      api
        .mongolianChat({
          messages: payloadMessages
        })
        .then((res) => {
          if (!res || res.code !== 200 || !res.data) {
            throw new Error(res && res.message ? res.message : '请求失败');
          }
          const { assistantText } = res.data;

          // 追加助手回复（不默认翻译）
          const assistantMsg = {
            role: 'assistant',
            original: assistantText || '',
            mo: '',
            loadingMo: false
          };
          this.messages.push(assistantMsg);
          this.saveMessagesToStorage(); // 保存会话历史

          this.$nextTick(() => {
            this.scrollToBottom();
          });
        })
        .catch((err) => {
          console.error('mongolianChat error', err);
          uni.showToast({
            title: '对话失败，请稍后重试',
            icon: 'none'
          });
        })
        .finally(() => {
          this.sending = false;
        });
    }
  }
};
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f5f5f5;
}

.chat-body {
  flex: 1;
  padding: 20rpx;
  box-sizing: border-box;
}

.msg-wrapper {
  display: flex;
  margin-bottom: 20rpx;
}

.msg-left {
  justify-content: flex-start;
}

.msg-right {
  justify-content: flex-end;
}

.msg-bubble {
  max-width: 80%;
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 16rpx 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
}

.msg-right .msg-bubble {
  background: linear-gradient(135deg, #3a7afe, #4f9dff);
  color: #ffffff;
}

.msg-text-original,
.msg-text-mo {
  margin-bottom: 8rpx;
}

.msg-text-mo:last-child {
  margin-bottom: 0;
}

.msg-label {
  font-size: 22rpx;
  color: #999;
  margin-right: 12rpx;
}

.msg-right .msg-label {
  color: #e0ecff;
}

.loading-text {
  font-size: 24rpx;
  color: #999;
}

.input-bar {
  padding: 12rpx 20rpx 24rpx;
  background-color: #ffffff;
  box-shadow: 0 -4rpx 12rpx rgba(0, 0, 0, 0.04);
  display: flex;
  align-items: flex-end;
  box-sizing: border-box;
  position: relative;
}

.ime-candidates {
  position: absolute;
  left: 20rpx;
  right: 20rpx;
  bottom: 96rpx;
  background: #ffffff;
  border-radius: 16rpx;
  padding: 16rpx;
  box-shadow: 0 6rpx 18rpx rgba(0, 0, 0, 0.12);
  max-height: 360rpx;
  overflow: scroll;
  z-index: 999;
}

.ime-candidate {
  display: flex;
  gap: 12rpx;
  padding: 12rpx 10rpx;
  border-radius: 12rpx;
}

.ime-candidate:active {
  background: #f5f7ff;
}

.ime-idx {
  color: #3a7afe;
  font-size: 24rpx;
}

.ime-word {
  color: #333;
  font-size: 28rpx;
}

.input {
  flex: 1;
  min-height: 72rpx;
  max-height: 220rpx;
  padding: 16rpx 20rpx;
  border-radius: 16rpx;
  background-color: #f5f5f5;
  font-size: 28rpx;
  line-height: 1.5;
}

.send-btn {
  margin-left: 16rpx;
  padding: 0 30rpx;
  height: 72rpx;
  line-height: 72rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #3a7afe, #4f9dff);
  color: #ffffff;
  font-size: 28rpx;
}

.send-btn[disabled] {
  opacity: 0.6;
}

.clear-btn {
  margin-left: 12rpx;
  padding: 0 24rpx;
  height: 72rpx;
  line-height: 72rpx;
  border-radius: 999rpx;
  background-color: #f5f5f5;
  color: #666;
  font-size: 26rpx;
  border: 1rpx solid #e0e0e0;
}
</style>

