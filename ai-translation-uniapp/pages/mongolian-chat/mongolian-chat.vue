<template>
  <view class="page">
    <!-- 对话区域 -->
    <scroll-view class="chat-body" scroll-x :scroll-into-view="scrollIntoView" :scroll-with-animation="true">
      <view class="list-box">
        <view v-for="(item, index) in messages" :key="index" :id="'msg-' + index" class="msg-wrapper"
          :class="item.role === 'user' ? 'msg-bottom' : 'msg-top'">
          <view class="user-img">
            {{item.role === 'user' ? 'ᠪᠢ' : 'ᠴᠡᠴᠡᠨ'}}
          </view>
          <view class="msg-bubble">
            <text>{{ item.mo }}</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 输入区域 -->
    <view class="input-bar">
      <textarea class="input" v-model="inputText" :auto-height="true" :maxlength="1000"
        placeholder="请输入要咨询的问题（支持中文等，多轮对话会自动保留上下文）" confirm-type="send" @confirm="handleSend" />
      <button class="send-btn" :disabled="sending || !inputText.trim()" @tap="handleSend">
        {{ sending ? '发送中...' : '发送' }}
      </button>
    </view>
  </view>
</template>

<script>
  import api from '@/common/utils/api.js';

  export default {
    data() {
      return {
        // messages: [{
        //   role: 'assistant',
        //   original: '我是你的蒙文助手，需要我做些什么？',
        //   mo: 'ᠪᠢ ᠪᠣᠯ ᠴᠢᠨᠦ ᠮᠣᠩᠭᠣᠯ ᠬᠡᠯᠡᠨ ᠦ ᠬᠠᠪᠰᠤᠷᠤᠭᠴᠢ ᠂ ᠨᠠᠳᠠ ᠪᠠᠷ ᠶᠠᠭᠤ ᠬᠢᠯᠭᠡᠬᠦ ᠴᠢᠬᠤᠯᠠ ᠲᠠᠶ ᠪᠤᠢ?',
        //   loadingMo: false
        // }, {
        //   role: 'user',
        //   original: '肉',
        //   mo: 'ᠮᠢᠬ᠎ᠠ',
        //   loadingMo: false
        // }, {
        //   role: 'assistant',
        //   original: '我是你的蒙文助手，需要我做些什么？',
        //   mo: 'ᠮᠣᠩᠭᠣᠯ ᠦᠨᠳᠦᠰᠦᠲᠡᠨ ᠦ ᠢᠳᠡᠭᠡᠨ ᠤᠮᠳᠠᠭᠠᠨ ᠤ ᠰᠣᠶᠣᠯ ᠳᠤ ᠂ ᠮᠢᠬ᠎ᠠ ᠪᠣᠯ ᠲᠤᠶᠢᠯ ᠤᠨ ᠴᠢᠬᠤᠯᠠ ᠢᠳᠡᠭᠡᠨ ᠪᠣᠯᠬᠤ ᠪᠥᠭᠡᠳ ᠢᠯᠠᠩᠭᠤᠶ᠎ᠠ ᠬᠣᠨᠢᠨ ᠮᠢᠬ᠎ᠠ ᠪᠠ ᠦᠬᠡᠷ ᠦᠨ ᠮᠢᠬ᠎ᠠ ᠪᠣᠯᠤᠨ᠎ᠠ ᠃ ᠮᠣᠩᠭᠣᠯᠴᠤᠳ ᠤᠨ ᠮᠢᠬ᠎ᠠ ᠢᠳᠡᠬᠦ ᠠᠷᠭ᠎ᠠ ᠮᠠᠶ᠋ᠢᠭ ᠨᠢ ᠣᠯᠠᠨ ᠶᠠᠩᠵᠤ ᠶᠢᠨ ᠪᠠᠶᠢᠳᠠᠭ ᠂ ᠵᠢᠱᠢᠶᠡᠯᠡᠪᠡᠯ ᠰᠢᠷᠠᠭᠰᠠᠨ ᠪᠦᠬᠦᠯᠢ ᠬᠣᠨᠢ ᠂ ᠪᠠᠷᠢᠭᠤᠯ ᠤᠨ ᠮᠢᠬ᠎ᠠ ᠂ ᠬᠠᠲᠠᠭᠠᠭᠰᠠᠨ ᠮᠢᠬ᠎ᠠ ᠵᠡᠷᠭᠡ ᠪᠠᠶᠢᠳᠠᠭ ᠂ ᠡᠲᠡᠭᠡᠷ ᠠᠮᠲᠠᠲᠤ ᠢᠳᠡᠭᠡᠨ ᠨᠢ ᠠᠮᠲᠠ ᠰᠢᠮᠲᠡ ᠪᠡᠷ ᠪᠠᠷᠠᠬᠤ ᠥᠬᠡᠶ ᠪᠠᠰᠠ ᠰᠢᠮ᠎ᠡ ᠲᠡᠵᠢᠭᠡᠯ ᠡᠯᠪᠡᠭ ᠪᠠᠶᠢᠳᠠᠭ ᠃ ᠬᠡᠷᠪᠡ ᠴᠢ ᠮᠣᠩᠭᠣᠯ ᠦᠨᠳᠦᠰᠦᠲᠡᠨ ᠦ ᠮᠢᠬᠠᠨ ᠢᠳᠡᠭᠡᠨ ᠦ ᠰᠣᠶᠣᠯ ᠳᠤ ᠰᠣᠨᠢᠷᠬᠠᠯ ᠲᠠᠶ ᠪᠣᠯ ᠂ ᠪᠢ ᠴᠢᠮ᠎ᠠ ᠳᠤ ᠨᠠᠩ ᠣᠯᠠᠨ ᠨᠠᠷᠢᠨ ᠵᠦᠶᠯᠡᠰ ᠢ ᠶᠠᠷᠢᠵᠤ ᠥᠭᠭᠦᠶ᠎ᠡ ︕',
        //   loadingMo: false
        // }],
        messages: [],
        inputText: '',
        sending: false,
        scrollIntoView: ''
      };
    },
    onLoad() {
      const welcomeMsg = {
        role: 'assistant',
        original: '我是你的蒙文助手，需要我做些什么？',
        mo: 'ᠪᠢ ᠪᠣᠯ ᠴᠢᠨᠦ ᠮᠣᠩᠭᠣᠯ ᠬᠡᠯᠡᠨ ᠦ ᠬᠠᠪᠰᠤᠷᠤᠭᠴᠢ ᠂ ᠨᠠᠳᠠ ᠪᠠᠷ ᠶᠠᠭᠤ ᠬᠢᠯᠭᠡᠬᠦ ᠴᠢᠬᠤᠯᠠ ᠲᠠᠶ ᠪᠤᠢ?',
        loadingMo: false
      };
      this.messages.push(welcomeMsg);
      this.$nextTick(() => {
        this.scrollToBottom();
      });
    },
    methods: {
      scrollToBottom() {
        if (this.messages.length === 0) return;
        this.scrollIntoView = 'msg-' + (this.messages.length - 1);
      },
      handleTranslate(item, type) {
        item.loadingMo = true;
        return api
          .translate({
            text: type == 'z2m' ? item.original : item.mo,
            from: type == 'z2m' ? 'zh' : 'mo',
            to: type == 'z2m' ? 'mo' : 'zh'
          })
          .then((res) => {
            if (res && res.data && res.data.result) {
              if (type == 'z2m') {
                item.mo = res.data.result;
                return item;
              }
              if (type == 'm2z') {
                return res.data.result;
              }
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
      async handleSend() {
        const content = this.inputText.trim();
        if (!content || this.sending) return;

        const userMsg = {
          role: 'user',
          original: '',
          mo: content,
          loadingMo: false
        };
        this.messages.push(userMsg);
        this.inputText = '';
        this.$nextTick(() => {
          this.scrollToBottom();
        });

        const userMsgOriginal = await this.handleTranslate(userMsg, 'm2z');

        this.messages[this.messages.length - 1] = {
          ...userMsg,
          original: userMsgOriginal
        }

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
          .then(async (res) => {
            if (!res || res.code !== 200 || !res.data) {
              throw new Error(res && res.message ? res.message : '请求失败');
            }
            const {
              assistantText
            } = res.data;

            const assistantMsg = {
              role: 'assistant',
              original: assistantText || '',
              mo: '',
              loadingMo: false
            };

            await this.handleTranslate(assistantMsg, 'z2m');

            this.messages.push(assistantMsg);

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

<style scoped lang="scss">
  .page {
    height: 100vh;
    background-color: #f5f5f5;
  }

  .chat-body {
    height: 100%;
    box-sizing: border-box;
  }

  .list-box {
    display: flex;
    width: max-content;
    height: 100%;
    padding: 30rpx;
    padding-bottom: 100px;
  }

  .msg-wrapper {
    height: max-content;
    display: flex;
    flex-direction: column;
    gap: 25rpx;
    margin-right: 30rpx;
  }

  .user-img {
    width: 90rpx;
    height: 90rpx;
    border-radius: 15rpx;
    background-color: #a854ff;
    color: #fff;
    writing-mode: vertical-lr;
    font-family: mgl;
    font-size: 32rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .msg-top {
    align-self: flex-start;
  }

  .msg-bottom {
    align-self: flex-end;
    flex-direction: column-reverse;
  }

  .msg-bubble {
    width: max-content;
    height: max-content;
    max-height: 70vh;
    background-color: #ffffff;
    border-radius: 16rpx;
    padding: 30rpx 20rpx;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
    writing-mode: vertical-lr;
    font-family: mgl;
    font-size: 40rpx;
    line-height: 50rpx;
    position: relative;

    &::after {
      content: '';
      position: absolute;
      width: 20rpx;
      height: 20rpx;
      left: 35rpx;
      top: -10rpx;
      background-color: #fff;
      transform: rotate(45deg);
    }
  }

  .msg-bottom {
    .user-img {
      background-color: #4f6bff;
    }

    .msg-bubble {
      background: rgb(149, 236, 105);
      color: rgb(15, 23, 10);

      &::after {
        content: '';
        position: absolute;
        width: 20rpx;
        height: 20rpx;
        left: 35rpx;
        top: initial;
        bottom: -10rpx;
        background-color: rgb(149, 236, 105);
        transform: rotate(45deg);
      }
    }
  }

  .loading-text {
    font-size: 24rpx;
    color: #999;
  }

  .input-bar {
    position: fixed;
    bottom: 0;
    left: 0;
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
</style>