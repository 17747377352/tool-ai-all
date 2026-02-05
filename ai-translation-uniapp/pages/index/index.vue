<template>
	<view class="container">
		<view class="top-bg">
			<image class="top-bg-img" src="../../static/top-bg-1.png" mode=""></image>
		</view>
		<view class="tool-grid">
			<view class="tool-item" style="background-image: linear-gradient(120deg, #a1c4fd 0%, #c2e9fb 100%);"
				@click="navigateToTool('translate')">
				<image class="top-icon" src="../../static/ai.png" mode=""></image>
				<text class="tool-name">ᠤᠶᠤᠨᠲᠤ \nᠣᠷᠴᠢᠭᠤᠯᠭ᠎ᠠ</text>
			</view>
			<view class="tool-item" style="background-image: linear-gradient(135deg, #667eea 0%, #764ba2 100%);"
				@click="navigateToTool('old-photo')">
				<image class="top-icon" src="../../static/ai.png" mode=""></image>
				<text class="tool-name">ᠬᠠᠭᠤᠴᠢᠨ \nᠰᠡᠭᠦᠳᠡᠷ \nᠵᠠᠰᠠᠬᠤ</text>
			</view>
			<view class="tool-item" style="background-image: linear-gradient(to top, #a18cd1 0%, #fbc2eb 100%);"
				@click="navigateToTool('image-generate')">
				<image class="top-icon" src="../../static/ai.png" mode=""></image>
				<text class="tool-name">ᠵᠢᠷᠤᠭ \nᠬᠢᠬᠦ</text>
			</view>
			<view class="tool-item" style="background-image: linear-gradient(120deg, #f6d365 0%, #fda085 100%);"
				@click="navigateToTool('image-recognition')">
				<image class="top-icon" src="../../static/ai.png" mode=""></image>
				<text class="tool-name">ᠵᠢᠷᠤᠭ \nᠲᠠᠨᠢᠬᠤ</text>
			</view>
			<view class="tool-item" style="background-image: linear-gradient(120deg, #f093fb 0%, #f5576c 100%);"
				@click="navigateToTool('mongolian-chat')">
				<image class="top-icon" src="../../static/ai.png" mode=""></image>
				<text class="tool-name">ᠤᠶᠤᠨᠲᠤ \nᠮᠠᠰᠢᠨ \nᠬᠥᠮᠦᠨ</text>
			</view>
		</view>

		<!-- 反馈按钮（右下角浮动） -->
		<view class="feedback-btn" @click="navigateToFeedback">
			<text class="feedback-icon">💬</text>
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

<style scoped>
	.container {}

	.top-bg {
		width: 100%;
		height: 100vw;
	}

	.top-bg-img {
		width: 100%;
		height: 100%;
	}

	.tool-grid {
		display: grid;
		grid-template-columns: repeat(2, 1fr);
		background-color: #fff;
		border-radius: 20rpx;
		margin-top: -150rpx;
		position: relative;
		z-index: 2;
		background-color: #f5f5f5;
		padding: 25rpx 30rpx;
		gap: 20rpx;
	}

	.tool-item {
		height: 180rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		background: #fff;
		border-radius: 20rpx;
		padding: 40rpx 20rpx;
		background-color: #ffffff;
	}

	.tool-item:active {
		transform: scale(0.95);
	}

	.top-icon {
		width: 80rpx;
		height: 80rpx;
		margin-right: 20rpx;
	}

	.tool-name {
		display: block;
		font-size: 32rpx;
		color: #fff;
		font-weight: 500;
		writing-mode: vertical-lr;
		font-family: mgl;
		text-shadow: 0 0 3px #000;
	}

	/* 反馈按钮 */
	.feedback-btn {
		position: fixed;
		right: 30rpx;
		bottom: 120rpx;
		width: 120rpx;
		height: 120rpx;
		background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(255, 255, 255, 0.9) 100%);
		border-radius: 60rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.15);
		z-index: 999;
		transition: all 0.3s;
		backdrop-filter: blur(10rpx);
	}

	.feedback-btn:active {
		transform: scale(0.9);
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.2);
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