<template>
	<view class="container">
		<view class="header">
			<text class="title">生活查询</text>
			<text class="subtitle">便捷工具，点亮生活</text>
		</view>

		<!-- Tab切换 -->
		<view class="tabs">
			<view 
				v-for="(tab, index) in tabs" 
				:key="index" 
				class="tab-item" 
				:class="{ active: currentTab === index }"
				@click="switchTab(index)"
			>
				<text class="tab-icon">{{ tab.icon }}</text>
				<text class="tab-name">{{ tab.name }}</text>
			</view>
		</view>

		<!-- 内容区域 -->
		<view class="content">
			<!-- 快递查询 -->
			<view v-if="currentTab === 0" class="form-card">
				<view class="form-item">
					<text class="label required">快递单号</text>
					<input 
						v-model="express.trackingNo" 
						class="input" 
						placeholder="请输入快递单号"
						maxlength="50"
					/>
				</view>
				<view class="form-item">
					<text class="label required">手机号码</text>
					<input 
						v-model="express.phone" 
						class="input" 
						type="number"
						placeholder="请输入11位手机号码"
						maxlength="11"
					/>
				</view>
				<button class="submit-btn" @click="queryExpress">查询快递</button>
				<view v-if="expressResult" class="result-box">
					<view v-for="(item, idx) in expressResult" :key="idx" class="result-item">
						<text class="result-time">{{ item.time }}</text>
						<text class="result-desc">{{ item.context }}</text>
					</view>
				</view>
			</view>

			<!-- 今日油价 -->
			<view v-if="currentTab === 1" class="form-card">
				<view class="form-item">
					<text class="label">省份</text>
					<input 
						v-model="oil.province" 
						class="input" 
						placeholder="请输入省份名称，如：北京、上海"
					/>
				</view>
				<button class="submit-btn" @click="queryOilPrice">查询油价</button>
				<view v-if="oilResult" class="result-box">
					<view class="result-item">
						<text class="result-label">92号汽油：</text>
						<text class="result-value">{{ oilResult['92h'] }} 元/升</text>
					</view>
					<view class="result-item">
						<text class="result-label">95号汽油：</text>
						<text class="result-value">{{ oilResult['95h'] }} 元/升</text>
					</view>
					<view class="result-item">
						<text class="result-label">98号汽油：</text>
						<text class="result-value">{{ oilResult['98h'] }} 元/升</text>
					</view>
					<view class="result-item">
						<text class="result-label">0号柴油：</text>
						<text class="result-value">{{ oilResult['0h'] }} 元/升</text>
					</view>
				</view>
			</view>

			<!-- 汇率换算 -->
			<view v-if="currentTab === 2" class="form-card">
				<view class="form-item inline">
					<view class="form-item-inline">
						<text class="label">基础货币</text>
						<picker 
							mode="selector" 
							:range="currencyLabels" 
							:value="baseCurrencyIndex"
							@change="onBaseChange"
							class="picker-wrapper"
						>
							<view class="picker">
								<text class="picker-text">{{ getCurrencyLabel(baseCurrency) }}</text>
								<text class="picker-arrow">▼</text>
							</view>
						</picker>
					</view>
					<view class="form-item-inline">
						<text class="label">目标货币</text>
						<picker 
							mode="selector" 
							:range="currencyLabels" 
							:value="targetCurrencyIndex"
							@change="onTargetChange"
							class="picker-wrapper"
						>
							<view class="picker">
								<text class="picker-text">{{ getCurrencyLabel(targetCurrency) }}</text>
								<text class="picker-arrow">▼</text>
							</view>
						</picker>
					</view>
				</view>
				<view class="form-item">
					<text class="label">金额</text>
					<input 
						v-model="forex.amount" 
						class="input" 
						type="digit"
						placeholder="请输入金额"
					/>
				</view>
				<button class="submit-btn" @click="queryForex">查询汇率</button>
				<view v-if="forexResult" class="result-box">
					<view class="result-item">
						<text class="result-label">汇率：</text>
						<text class="result-value">1 {{ forexResult.base }} = {{ forexResult.rate }} {{ forexResult.target }}</text>
					</view>
					<view v-if="forexResult.amount" class="result-item">
						<text class="result-label">换算金额：</text>
						<text class="result-value">{{ forexResult.amount }} {{ forexResult.base }}</text>
					</view>
					<view v-if="forexResult.converted" class="result-item">
						<text class="result-label">换算结果：</text>
						<text class="result-value">{{ forexResult.converted }} {{ forexResult.target }}</text>
					</view>
				</view>
			</view>

			<!-- 彩票开奖 -->
			<view v-if="currentTab === 3" class="form-card">
				<view class="form-item">
					<text class="label">彩种类型</text>
					<picker 
						mode="selector" 
						:range="lotteryTypes" 
						:value="lotteryTypeIndex"
						@change="onLotteryTypeChange"
						class="picker-wrapper"
					>
						<view class="picker">
							<text class="picker-text">{{ lotteryTypes[lotteryTypeIndex] }}</text>
							<text class="picker-arrow">▼</text>
						</view>
					</picker>
				</view>
				<button class="submit-btn" @click="queryLottery">查询最新开奖</button>
				<view v-if="lotteryResult && lotteryResult.issue" class="result-box">
					<view class="result-item">
						<text class="result-label">彩种类型：</text>
						<text class="result-value">{{ getLotteryTypeName(lotteryResult.type) }}</text>
					</view>
					<view class="result-item">
						<text class="result-label">期号：</text>
						<text class="result-value">{{ lotteryResult.issue }}</text>
					</view>
					<view class="result-item">
						<text class="result-label">开奖时间：</text>
						<text class="result-value">{{ lotteryResult.openTime || '暂无' }}</text>
					</view>
					<view v-if="lotteryResult.numbers" class="result-item">
						<text class="result-label">开奖号码：</text>
						<view class="lottery-numbers">
							<text class="lottery-number red" v-for="(num, idx) in getRedNumbers(lotteryResult.numbers, lotteryResult.type)" :key="'red-' + idx">{{ num }}</text>
							<text v-if="getBlueNumbers(lotteryResult.numbers, lotteryResult.type).length > 0" class="lottery-separator">|</text>
							<text class="lottery-number blue" v-for="(num, idx) in getBlueNumbers(lotteryResult.numbers, lotteryResult.type)" :key="'blue-' + idx">{{ num }}</text>
						</view>
					</view>
					<view v-if="lotteryResult.detail" class="result-item">
						<text class="result-label">说明：</text>
						<text class="result-value">{{ lotteryResult.detail }}</text>
					</view>
				</view>
			</view>
		</view>

		<!-- Banner广告 -->
		<ad-video-banner />
	</view>
</template>

<script>
import api from '@/common/utils/api.js';
import AdVideoBanner from '@/common/components/ad-video-banner.vue';

export default {
	components: {
		AdVideoBanner
	},
	data() {
		return {
			currentTab: 0,
			tabs: [
				{ name: '快递查询', icon: '📦' },
				{ name: '今日油价', icon: '⛽' },
				{ name: '汇率换算', icon: '💱' },
				{ name: '彩票开奖', icon: '🎰' }
			],
			// 快递查询
			express: {
				trackingNo: '',
				phone: ''
			},
			expressResult: null,
			// 油价查询
			oil: {
				province: ''
			},
			oilResult: null,
			// 汇率查询
			forex: {
				base: 'CNY',
				target: 'USD',
				amount: '100'
			},
			forexResult: null,
			currencyList: ['CNY', 'USD', 'EUR', 'GBP', 'JPY', 'HKD', 'KRW', 'AUD', 'CAD', 'SGD', 'CHF', 'NZD', 'THB', 'MYR', 'INR'],
			currencyLabels: ['CNY(人民币)', 'USD(美元)', 'EUR(欧元)', 'GBP(英镑)', 'JPY(日元)', 'HKD(港币)', 'KRW(韩元)', 'AUD(澳元)', 'CAD(加元)', 'SGD(新加坡元)', 'CHF(瑞士法郎)', 'NZD(新西兰元)', 'THB(泰铢)', 'MYR(马来西亚林吉特)', 'INR(印度卢比)'],
			baseCurrency: 'CNY',
			targetCurrency: 'USD',
			baseCurrencyIndex: 0,
			targetCurrencyIndex: 1,
			// 彩票查询
			lotteryTypes: ['双色球', '大乐透'],
			lotteryTypeIndex: 0,
			lotteryType: 'ssq',
			lotteryResult: null
		};
	},
	methods: {
		switchTab(index) {
			this.currentTab = index;
			// 切换tab时清空结果
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
			// 转换彩种类型：双色球 -> ssq, 大乐透 -> dlt
			this.lotteryType = index === 0 ? 'ssq' : 'dlt';
		},
		getCurrencyLabel(code) {
			const labels = {
				'CNY': '人民币',
				'USD': '美元',
				'EUR': '欧元',
				'GBP': '英镑',
				'JPY': '日元',
				'HKD': '港币',
				'KRW': '韩元',
				'AUD': '澳元',
				'CAD': '加元',
				'SGD': '新加坡元',
				'CHF': '瑞士法郎',
				'NZD': '新西兰元',
				'THB': '泰铢',
				'MYR': '马来西亚林吉特',
				'INR': '印度卢比'
			};
			return labels[code] || code;
		},
		async queryExpress() {
			if (!this.express.trackingNo) {
				uni.showToast({
					title: '请输入快递单号',
					icon: 'none'
				});
				return;
			}
			if (!this.express.phone) {
				uni.showToast({
					title: '请输入手机号码',
					icon: 'none'
				});
				return;
			}
			if (!/^\d{11}$/.test(this.express.phone)) {
				uni.showToast({
					title: '手机号码必须为11位数字',
					icon: 'none'
				});
				return;
			}

			uni.showLoading({ title: '查询中...' });
			try {
				const res = await api.lifeExpress({
					trackingNo: this.express.trackingNo,
					phone: this.express.phone
				});
				if (res.code === 200) {
					this.expressResult = res.data || [];
					if (!this.expressResult || this.expressResult.length === 0) {
						uni.showToast({
							title: '暂无物流信息',
							icon: 'none'
						});
					}
				} else {
					uni.showToast({
						title: res.message || '查询失败',
						icon: 'none'
					});
				}
			} catch (error) {
				uni.showToast({
					title: '查询失败，请稍后重试',
					icon: 'none'
				});
			} finally {
				uni.hideLoading();
			}
		},
		async queryOilPrice() {
			uni.showLoading({ title: '查询中...' });
			try {
				const res = await api.lifeOilPrice({
					province: this.oil.province || '北京'
				});
				if (res.code === 200) {
					this.oilResult = res.data || {};
					if (!this.oilResult || Object.keys(this.oilResult).length === 0) {
						uni.showToast({
							title: '暂无油价信息',
							icon: 'none'
						});
					}
				} else {
					uni.showToast({
						title: res.message || '查询失败',
						icon: 'none'
					});
				}
			} catch (error) {
				uni.showToast({
					title: '查询失败，请稍后重试',
					icon: 'none'
				});
			} finally {
				uni.hideLoading();
			}
		},
		async queryForex() {
			if (!this.forex.amount || parseFloat(this.forex.amount) <= 0) {
				uni.showToast({
					title: '请输入有效金额',
					icon: 'none'
				});
				return;
			}

			uni.showLoading({ title: '查询中...' });
			try {
				const res = await api.lifeForex({
					base: this.forex.base,
					target: this.forex.target,
					amount: parseFloat(this.forex.amount)
				});
				if (res.code === 200) {
					this.forexResult = res.data || {};
				} else {
					uni.showToast({
						title: res.message || '查询失败',
						icon: 'none'
					});
				}
			} catch (error) {
				uni.showToast({
					title: '查询失败，请稍后重试',
					icon: 'none'
				});
			} finally {
				uni.hideLoading();
			}
		},
		getLotteryTypeName(type) {
			const typeMap = {
				'ssq': '双色球',
				'dlt': '大乐透',
				'双色球': '双色球',
				'大乐透': '大乐透'
			};
			return typeMap[type] || type || '未知';
		},
		getRedNumbers(numbers, type) {
			if (!numbers) return [];
			// 去除首尾空格，按空格或逗号分割
			const nums = numbers.trim().split(/[\s,]+/).filter(n => n && n.trim());
			if (type === 'ssq' || type === '双色球') {
				// 双色球：前6个是红球
				return nums.slice(0, 6);
			} else if (type === 'dlt' || type === '大乐透') {
				// 大乐透：前5个是红球
				return nums.slice(0, 5);
			}
			return nums;
		},
		getBlueNumbers(numbers, type) {
			if (!numbers) return [];
			// 去除首尾空格，按空格或逗号分割
			const nums = numbers.trim().split(/[\s,]+/).filter(n => n && n.trim());
			if (type === 'ssq' || type === '双色球') {
				// 双色球：最后1个是蓝球
				return nums.slice(6);
			} else if (type === 'dlt' || type === '大乐透') {
				// 大乐透：后2个是蓝球（从第6个开始，取2个）
				return nums.slice(5, 7);
			}
			return [];
		},
		async queryLottery() {
			uni.showLoading({ title: '查询中...' });
			try {
				const res = await api.lifeLottery({
					type: this.lotteryType
				});
				if (res.code === 200) {
					this.lotteryResult = res.data || {};
					if (!this.lotteryResult || !this.lotteryResult.issue) {
						uni.showToast({
							title: '暂无开奖信息',
							icon: 'none'
						});
					}
				} else {
					uni.showToast({
						title: res.message || '查询失败',
						icon: 'none'
					});
				}
			} catch (error) {
				uni.showToast({
					title: '查询失败，请稍后重试',
					icon: 'none'
				});
			} finally {
				uni.hideLoading();
			}
		}
	}
};
</script>

<style scoped>
.container {
	min-height: 100vh;
	background-color: #f8f9fa;
	padding: 40rpx 30rpx;
}

.header {
	margin-bottom: 40rpx;
	padding-left: 10rpx;
}

.title {
	font-size: 44rpx;
	font-weight: bold;
	color: #333;
	display: block;
}

.subtitle {
	font-size: 26rpx;
	color: #999;
	margin-top: 10rpx;
	display: block;
}

/* Tab切换 */
.tabs {
	display: flex;
	background: #fff;
	border-radius: 24rpx;
	padding: 20rpx;
	margin-bottom: 30rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
}

.tab-item {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 20rpx 10rpx;
	border-radius: 16rpx;
	transition: all 0.3s;
}

.tab-item.active {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.tab-icon {
	font-size: 40rpx;
	margin-bottom: 8rpx;
}

.tab-name {
	font-size: 24rpx;
	color: #666;
}

.tab-item.active .tab-name {
	color: #fff;
	font-weight: 500;
}

/* 内容区域 */
.content {
	margin-bottom: 40rpx;
}

.form-card {
	background: #fff;
	border-radius: 24rpx;
	padding: 40rpx 30rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
}

.form-item {
	margin-bottom: 40rpx;
}

.form-item.inline {
	display: flex;
	gap: 20rpx;
}

.form-item-inline {
	flex: 1;
	min-width: 0;
}

.label {
	font-size: 28rpx;
	color: #333;
	font-weight: 500;
	margin-bottom: 16rpx;
	display: block;
}

.label.required::before {
	content: '*';
	color: #ff4757;
	margin-right: 4rpx;
}

.input {
	width: 100%;
	padding: 24rpx 20rpx;
	background: #f8f9fa;
	border-radius: 16rpx;
	font-size: 28rpx;
	color: #333;
	min-height: 88rpx;
	line-height: 40rpx;
	box-sizing: border-box;
}

/* Picker样式 */
.picker-wrapper {
	width: 100%;
}

.picker {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 24rpx 20rpx;
	background: #f8f9fa;
	border-radius: 16rpx;
	min-height: 88rpx;
	box-sizing: border-box;
}

.picker-text {
	font-size: 28rpx;
	color: #333;
	flex: 1;
}

.picker-arrow {
	font-size: 24rpx;
	color: #999;
}

.submit-btn {
	width: 100%;
	padding: 28rpx;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: #fff;
	border-radius: 16rpx;
	font-size: 32rpx;
	font-weight: 500;
	margin-top: 20rpx;
}

.submit-btn:active {
	opacity: 0.8;
}

/* 结果展示 */
.result-box {
	margin-top: 40rpx;
	padding: 30rpx;
	background: #f8f9fa;
	border-radius: 16rpx;
}

.result-item {
	margin-bottom: 20rpx;
	padding-bottom: 20rpx;
	border-bottom: 1rpx solid #e0e0e0;
}

.result-item:last-child {
	margin-bottom: 0;
	padding-bottom: 0;
	border-bottom: none;
}

.result-time {
	font-size: 24rpx;
	color: #999;
	display: block;
	margin-bottom: 8rpx;
}

.result-desc {
	font-size: 28rpx;
	color: #333;
	line-height: 1.6;
}

.result-label {
	font-size: 28rpx;
	color: #666;
	margin-right: 10rpx;
}

.result-value {
	font-size: 28rpx;
	color: #333;
	font-weight: 500;
}

/* 彩票号码样式 */
.lottery-numbers {
	display: flex;
	flex-wrap: wrap;
	align-items: center;
	gap: 12rpx;
	margin-top: 8rpx;
}

.lottery-number {
	display: inline-block;
	width: 60rpx;
	height: 60rpx;
	line-height: 60rpx;
	text-align: center;
	border-radius: 50%;
	font-size: 24rpx;
	font-weight: 500;
	color: #fff;
}

.lottery-number.red {
	background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
}

.lottery-number.blue {
	background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.lottery-separator {
	font-size: 32rpx;
	color: #999;
	margin: 0 8rpx;
	font-weight: bold;
}
</style>
