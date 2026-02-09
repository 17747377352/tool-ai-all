<template>
  <view class="container">
    <image class="top-bg-img"
      src="https://p26-dreamina-sign.byteimg.com/tos-cn-i-tb4s082cfz/444fe422131e47afb826f0122239fb52~tplv-tb4s082cfz-aigc_resize:1080:1080.webp?lk3s=43402efa&x-expires=1772928000&x-signature=TTZNURzA%2FtcGNRYkHfPWg8KqAKs%3D&format=.webp"
      mode="aspectFill"></image>
    <view class="linear"></view>
    <view class="tool-grid">
      <view class="tool-item large" style="" @click="navigateToTool('mongolian-chat')">
        <image class="tool-icon" src="/static/icon_agent.png" mode=""></image>
        <text class="tool-name">ᠴᠡᠴᠡᠨ \nᠲᠠᠢ \nᠵᠣᠭᠣᠭᠳᠠᠬᠤ</text>
      </view>
      <view class="tool-item large" style="" @click="navigateToTool('old-photo')">
        <image class="tool-icon" src="/static/icon_edit.png" mode=""></image>
        <text class="tool-name">ᠬᠠᠭᠤᠴᠢᠨ \nᠰᠡᠭᠦᠳᠡᠷ \nᠵᠠᠰᠠᠬᠤ</text>
      </view>
      <view class="tool-item" style="" @click="navigateToTool('image-generate')">
        <image class="tool-icon" src="/static/icon_make.png" mode=""></image>
        <text class="tool-name">ᠵᠢᠷᠤᠭ \nᠵᠣᠬᠢᠶᠠᠬᠤ</text>
      </view>
      <view class="tool-item" style="" @click="navigateToTool('image-recognition')">
        <image class="tool-icon" src="/static/icon_img.png" mode=""></image>
        <text class="tool-name">ᠵᠢᠷᠤᠭ \nᠲᠠᠨᠢᠬᠤ</text>
      </view>
      <view class="tool-item " style="" @click="navigateToTool('translate')">
        <image class="tool-icon" src="/static/icon_trans.png" mode=""></image>
        <text class="tool-name">ᠣᠷᠴᠢᠭᠤᠯᠬᠤ \nᠠᠵᠢᠯ</text>
      </view>
    </view>
    <view class="project-box">
      <view class="project-item">
        <image class="project-img"
          src="https://p3-dreamina-sign.byteimg.com/tos-cn-i-tb4s082cfz/2fbfdd088f43473f9dda2d74fd2cfe3e~tplv-tb4s082cfz-aigc_resize:480:480.webp?lk3s=4fa96020&x-expires=1802191787&x-signature=z9PdOqXZkTec9Kb55WL7hEdA4jk%3D"
          mode="aspectFill"></image>
      </view>
      <view class="project-item">
        <image class="project-img"
          src="https://p3-dreamina-sign.byteimg.com/tos-cn-i-tb4s082cfz/4894cc844fe44f12b7664835f22b1335~tplv-tb4s082cfz-aigc_resize:720:720.webp?lk3s=4fa96020&x-expires=1802192319&x-signature=s%2Fh5ISyAKsV1g4YW3NY%2F3O5AgLo%3D"
          mode="aspectFill"></image>
      </view>
      <view class="project-item">
        <image class="project-img"
          src="https://p26-dreamina-sign.byteimg.com/tos-cn-i-tb4s082cfz/39fb7bfb02db4be1b2fb1f3c252839ee~tplv-tb4s082cfz-aigc_resize:720:720.webp?lk3s=4fa96020&x-expires=1802192262&x-signature=0VOWH%2B%2FYFUKzwqAEPey2HALGswo%3D"
          mode="aspectFill"></image>
      </view>
      <view class="project-item">
        <image class="project-img"
          src="https://p26-dreamina-sign.byteimg.com/tos-cn-i-tb4s082cfz/75c6695476fe4b95b0a4db1f374ecbe0~tplv-tb4s082cfz-aigc_resize:2400:2400.webp?lk3s=43402efa&x-expires=1772928000&x-signature=%2B1m8hBjrItKq5c01kXxCaFgty3I%3D&format=.webp"
          mode="aspectFill"></image>
      </view>
    </view>
  </view>
</template>

<script>
  import {
    checkUserAuth,
    getUserInfoAndDecrypt
  } from '@/common/utils/auth.js';

  export default {
    onLoad() {
      // 登录已在App.vue中处理，这里不需要重复登录
    },
    methods: {
      async navigateToTool(toolName) {
        try {
          // 检查用户授权
          await checkUserAuth();
        } catch (e) {
          // 需要授权，显示授权按钮
          uni.showModal({
            title: '需要授权',
            content: '需要获取您的用户信息',
            showCancel: false,
            success: async (modalRes) => {
              if (modalRes.confirm) {
                try {
                  await getUserInfoAndDecrypt();
                  this.goToTool(toolName);
                } catch (err) {
                  uni.showToast({
                    title: '授权失败',
                    icon: 'none'
                  });
                }
              }
            }
          });
          return;
        }
        this.goToTool(toolName);
      },
      goToTool(toolName) {
        const pages = {
          'translate': '/pages/translate/translate',
          'old-photo': '/pages/old-photo/old-photo',
          'image-generate': '/pages/image-generate/image-generate',
          'image-recognition': '/pages/image-recognition/image-recognition',
          'mongolian-chat': '/pages/mongolian-chat/mongolian-chat'
        };
        uni.navigateTo({
          url: pages[toolName]
        });
      },
      navigateToFeedback() {
        uni.navigateTo({
          url: '/pages/feedback/feedback'
        });
      }
    }
  };
</script>

<style scoped lang="scss">
  .container {}

  .top-bg-img {
    width: 100%;
    height: 56.25vw;
    display: block;
  }

  .linear {
    width: 100%;
    height: 125rpx;
    margin-top: -125rpx;
    background-image: linear-gradient(to top, rgb(24, 24, 24) 0%, rgba(255, 255, 255, 0) 100%);
  }

  .tool-grid {
    display: grid;
    grid-template-columns: repeat(6, 1fr);
    border-radius: 20rpx;
    position: relative;
    z-index: 2;
    padding: 20rpx 30rpx;
    gap: 20rpx;
    margin-top: -50rpx;
  }

  .tool-item {
    grid-column: span 2;
    width: 100%;
    height: 180rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10rpx;
    background: #fff;
    border-radius: 15rpx;
    overflow: hidden;
    position: relative;
    background-color: rgb(187, 27, 42);

    &.large {
      grid-column: span 3;
    }
  }

  .tool-icon {
    width: 80rpx;
    height: 80rpx;
  }

  .tool-name {
    display: block;
    font-size: 36rpx;
    color: #fff;
    writing-mode: vertical-lr;
    font-family: mgl;
    text-align: center;
  }

  .project-box {
    display: grid;
    gap: 20rpx;
    padding: 0 20rpx;
    grid-template-columns: 1fr 1fr;
  }

  .project-item {
    width: 100%;
    aspect-ratio: 9 / 16;
    border-radius: 15rpx;
    overflow: hidden;
  }

  .project-img {
    width: 100%;
    height: 100%;
  }

  .feedback-icon {
    font-size: 48rpx;
    line-height: 1;
    margin-bottom: 8rpx;
  }

  .feedback-text {
    font-size: 22rpx;
    color: #667eea;
    font-weight: 500;
  }
</style>