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
        @confirm="handleSend"
      />
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
      scrollIntoView: ''
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

