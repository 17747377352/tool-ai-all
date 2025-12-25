<template>
    <view class="container">
        <!-- 顶部渐变背景 -->
        <view class="header-bg">
            <view class="header">
                <view class="constellation-icon-wrapper">
                    <text class="constellation-icon">{{ getConstellationIcon(constellationName) }}</text>
                </view>
                <text class="constellation-name">{{ constellationName }}今日运势</text>
                <text class="date">{{ formatDate(date) }}</text>
            </view>
        </view>

        <!-- 综合运势分数条形栏 -->
        <view class="scores-section">
            <view class="section-title">
                <text class="title-icon">📊</text>
                <text class="title-text">运势评分</text>
            </view>
            <view class="score-item">
                <view class="score-label-wrapper">
                    <text class="score-icon">⭐</text>
                    <text class="score-label">综合</text>
                </view>
                <view class="score-bar-container">
                    <view class="score-bar" :style="{ width: overallScore + '%' }">
                        <view class="score-bar-glow"></view>
                    </view>
                </view>
                <text class="score-value">{{ overallScore }}</text>
            </view>
            <view class="score-item">
                <view class="score-label-wrapper">
                    <text class="score-icon">💕</text>
                    <text class="score-label">爱情</text>
                </view>
                <view class="score-bar-container">
                    <view class="score-bar love" :style="{ width: loveScore + '%' }">
                        <view class="score-bar-glow"></view>
                    </view>
                </view>
                <text class="score-value">{{ loveScore }}</text>
            </view>
            <view class="score-item">
                <view class="score-label-wrapper">
                    <text class="score-icon">💼</text>
                    <text class="score-label">事业</text>
                </view>
                <view class="score-bar-container">
                    <view class="score-bar career" :style="{ width: careerScore + '%' }">
                        <view class="score-bar-glow"></view>
                    </view>
                </view>
                <text class="score-value">{{ careerScore }}</text>
            </view>
            <view class="score-item">
                <view class="score-label-wrapper">
                    <text class="score-icon">💰</text>
                    <text class="score-label">财运</text>
                </view>
                <view class="score-bar-container">
                    <view class="score-bar wealth" :style="{ width: wealthScore + '%' }">
                        <view class="score-bar-glow"></view>
                    </view>
                </view>
                <text class="score-value">{{ wealthScore }}</text>
            </view>
            <view class="score-item">
                <view class="score-label-wrapper">
                    <text class="score-icon">🏃</text>
                    <text class="score-label">健康</text>
                </view>
                <view class="score-bar-container">
                    <view class="score-bar health" :style="{ width: healthScore + '%' }">
                        <view class="score-bar-glow"></view>
                    </view>
                </view>
                <text class="score-value">{{ healthScore }}</text>
            </view>
        </view>

        <!-- 幸运信息 -->
        <view class="lucky-section">
            <view class="section-title">
                <text class="title-icon">🍀</text>
                <text class="title-text">幸运元素</text>
            </view>
            <view class="lucky-grid">
                <view class="lucky-card">
                    <view class="lucky-icon-wrapper" style="background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);">
                        <text class="lucky-icon">🎨</text>
                    </view>
                    <text class="lucky-label">幸运颜色</text>
                    <view class="lucky-color-box" :style="{ backgroundColor: getColorValue(luckyColor) }"></view>
                    <text class="lucky-value">{{ luckyColor }}</text>
                </view>
                <view class="lucky-card">
                    <view class="lucky-icon-wrapper" style="background: linear-gradient(135deg, #feca57 0%, #ff9ff3 100%);">
                        <text class="lucky-icon">🔢</text>
                    </view>
                    <text class="lucky-label">幸运数字</text>
                    <text class="lucky-number">{{ luckyNumber }}</text>
                </view>
                <view class="lucky-card">
                    <view class="lucky-icon-wrapper" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
                        <text class="lucky-icon">💫</text>
                    </view>
                    <text class="lucky-label">速配星座</text>
                    <text class="lucky-value">{{ compatibleConstellation }}</text>
                </view>
            </view>
        </view>

        <!-- 宜忌 -->
        <view class="suitable-avoid-section">
            <view class="suitable-card">
                <view class="suitable-header">
                    <view class="suitable-icon-wrapper">
                        <text class="suitable-icon">✅</text>
                    </view>
                    <text class="suitable-title">宜</text>
                </view>
                <text class="suitable-text">{{ suitable }}</text>
            </view>
            <view class="avoid-card">
                <view class="avoid-header">
                    <view class="avoid-icon-wrapper">
                        <text class="avoid-icon">❌</text>
                    </view>
                    <text class="avoid-title">忌</text>
                </view>
                <text class="avoid-text">{{ avoid }}</text>
            </view>
        </view>

        <!-- 详细运势 -->
        <view class="details-section">
            <view class="section-title">
                <text class="title-icon">📖</text>
                <text class="title-text">详细运势</text>
            </view>
            <view class="detail-card overall">
                <view class="detail-header">
                    <view class="detail-icon-wrapper" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
                        <text class="detail-icon">⭐</text>
                    </view>
                    <text class="detail-title">综合运势</text>
                </view>
                <text class="detail-content">{{ overallDetail }}</text>
            </view>
            <view class="detail-card love">
                <view class="detail-header">
                    <view class="detail-icon-wrapper" style="background: linear-gradient(135deg, #ff6b9d 0%, #ff8fab 100%);">
                        <text class="detail-icon">💕</text>
                    </view>
                    <text class="detail-title">爱情运势</text>
                </view>
                <text class="detail-content">{{ loveDetail }}</text>
            </view>
            <view class="detail-card career">
                <view class="detail-header">
                    <view class="detail-icon-wrapper" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
                        <text class="detail-icon">💼</text>
                    </view>
                    <text class="detail-title">事业学业</text>
                </view>
                <text class="detail-content">{{ careerDetail }}</text>
            </view>
            <view class="detail-card wealth">
                <view class="detail-header">
                    <view class="detail-icon-wrapper" style="background: linear-gradient(135deg, #feca57 0%, #ff9ff3 100%);">
                        <text class="detail-icon">💰</text>
                    </view>
                    <text class="detail-title">财富运势</text>
                </view>
                <text class="detail-content">{{ wealthDetail }}</text>
            </view>
            <view class="detail-card health">
                <view class="detail-header">
                    <view class="detail-icon-wrapper" style="background: linear-gradient(135deg, #48d1cc 0%, #20b2aa 100%);">
                        <text class="detail-icon">🏃</text>
                    </view>
                    <text class="detail-title">健康运势</text>
                </view>
                <text class="detail-content">{{ healthDetail }}</text>
            </view>
        </view>

        <!-- Banner广告 -->
        <ad-video-banner />
    </view>
</template>

<script>
import AdVideoBanner from '@/common/components/ad-video-banner.vue';

export default {
    components: {
        AdVideoBanner
    },
    data() {
        return {
            constellationName: '',
            date: '',
            overallScore: 0,
            loveScore: 0,
            careerScore: 0,
            wealthScore: 0,
            healthScore: 0,
            luckyColor: '',
            luckyNumber: '',
            compatibleConstellation: '',
            suitable: '',
            avoid: '',
            overallDetail: '',
            loveDetail: '',
            careerDetail: '',
            wealthDetail: '',
            healthDetail: ''
        };
    },
    onLoad(options) {
        // 从页面参数中获取数据
        if (options.data) {
            try {
                const data = JSON.parse(decodeURIComponent(options.data));
                this.constellationName = data.constellation || '';
                this.date = data.date || '';
                this.overallScore = data.overallScore || 0;
                this.loveScore = data.loveScore || 0;
                this.careerScore = data.careerScore || 0;
                this.wealthScore = data.wealthScore || 0;
                this.healthScore = data.healthScore || 0;
                this.luckyColor = data.luckyColor || '';
                this.luckyNumber = data.luckyNumber || '';
                this.compatibleConstellation = data.compatibleConstellation || '';
                this.suitable = data.suitable || '';
                this.avoid = data.avoid || '';
                this.overallDetail = data.overallDetail || '';
                this.loveDetail = data.loveDetail || '';
                this.careerDetail = data.careerDetail || '';
                this.wealthDetail = data.wealthDetail || '';
                this.healthDetail = data.healthDetail || '';
            } catch (e) {
                console.error('解析数据失败', e);
                uni.showToast({
                    title: '数据解析失败',
                    icon: 'none'
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
                '白羊座': '♈',
                '金牛座': '♉',
                '双子座': '♊',
                '巨蟹座': '♋',
                '狮子座': '♌',
                '处女座': '♍',
                '天秤座': '♎',
                '天蝎座': '♏',
                '射手座': '♐',
                '摩羯座': '♑',
                '水瓶座': '♒',
                '双鱼座': '♓'
            };
            return icons[name] || '⭐';
        },
        /**
         * 格式化日期
         */
        formatDate(dateStr) {
            if (!dateStr) return '';
            try {
                const date = new Date(dateStr);
                const month = date.getMonth() + 1;
                const day = date.getDate();
                const weekdays = ['日', '一', '二', '三', '四', '五', '六'];
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
                '红色': '#ff6b9d',
                '蓝色': '#4facfe',
                '绿色': '#48d1cc',
                '黄色': '#feca57',
                '紫色': '#667eea',
                '橙色': '#ff9a56',
                '粉色': '#ff8fab',
                '青色': '#20b2aa',
                '白色': '#f5f5f5',
                '黑色': '#333333',
                '金色': '#ffd700',
                '银色': '#c0c0c0'
            };
            return colorMap[colorName] || '#667eea';
        }
    }
};
</script>

<style scoped>
.container {
    min-height: 100vh;
    background: linear-gradient(180deg, #f8f9ff 0%, #f5f5f5 100%);
    padding-bottom: 30rpx;
}

/* 顶部渐变背景 */
.header-bg {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    padding: 60rpx 30rpx 80rpx 30rpx;
    position: relative;
    margin-bottom: -40rpx;
    border-radius: 0 0 40rpx 40rpx;
    box-shadow: 0 10rpx 30rpx rgba(102, 126, 234, 0.3);
}

.header-bg::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1440 320"><path fill="rgba(255,255,255,0.1)" d="M0,96L48,112C96,128,192,160,288,160C384,160,480,128,576,122.7C672,117,768,139,864,154.7C960,171,1056,181,1152,165.3C1248,149,1344,107,1392,85.3L1440,64L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z"></path></svg>') no-repeat bottom;
    background-size: cover;
    opacity: 0.3;
}

.header {
    text-align: center;
    position: relative;
    z-index: 1;
}

.constellation-icon-wrapper {
    width: 120rpx;
    height: 120rpx;
    margin: 0 auto 20rpx;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    backdrop-filter: blur(10rpx);
    border: 3rpx solid rgba(255, 255, 255, 0.3);
    box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.2);
}

.constellation-icon {
    font-size: 60rpx;
    line-height: 1;
}

.constellation-name {
    display: block;
    font-size: 42rpx;
    font-weight: bold;
    color: #fff;
    margin-bottom: 15rpx;
    text-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.2);
}

.date {
    display: block;
    font-size: 26rpx;
    color: rgba(255, 255, 255, 0.95);
    font-weight: 300;
}

/* 通用标题样式 */
.section-title {
    display: flex;
    align-items: center;
    margin-bottom: 30rpx;
    padding-left: 10rpx;
}

.title-icon {
    font-size: 32rpx;
    margin-right: 15rpx;
}

.title-text {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
}

/* 分数条形栏 */
.scores-section {
    background: #fff;
    border-radius: 24rpx;
    padding: 40rpx 30rpx;
    margin: 0 30rpx 30rpx 30rpx;
    box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.08);
}

.score-item {
    display: flex;
    align-items: center;
    margin-bottom: 30rpx;
}

.score-item:last-child {
    margin-bottom: 0;
}

.score-label-wrapper {
    display: flex;
    align-items: center;
    width: 120rpx;
}

.score-icon {
    font-size: 28rpx;
    margin-right: 10rpx;
}

.score-label {
    font-size: 28rpx;
    color: #333;
    font-weight: 500;
}

.score-bar-container {
    flex: 1;
    height: 24rpx;
    background: #f0f0f0;
    border-radius: 12rpx;
    margin: 0 25rpx;
    overflow: hidden;
    position: relative;
}

.score-bar {
    height: 100%;
    background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
    border-radius: 12rpx;
    position: relative;
    transition: width 0.6s ease-out;
    box-shadow: 0 2rpx 8rpx rgba(102, 126, 234, 0.4);
}

.score-bar-glow {
    position: absolute;
    top: 0;
    right: 0;
    width: 40rpx;
    height: 100%;
    background: linear-gradient(90deg, transparent 0%, rgba(255, 255, 255, 0.5) 100%);
    border-radius: 0 12rpx 12rpx 0;
}

.score-bar.love {
    background: linear-gradient(90deg, #ff6b9d 0%, #ff8fab 100%);
    box-shadow: 0 2rpx 8rpx rgba(255, 107, 157, 0.4);
}

.score-bar.career {
    background: linear-gradient(90deg, #4facfe 0%, #00f2fe 100%);
    box-shadow: 0 2rpx 8rpx rgba(79, 172, 254, 0.4);
}

.score-bar.wealth {
    background: linear-gradient(90deg, #feca57 0%, #ff9ff3 100%);
    box-shadow: 0 2rpx 8rpx rgba(254, 202, 87, 0.4);
}

.score-bar.health {
    background: linear-gradient(90deg, #48d1cc 0%, #20b2aa 100%);
    box-shadow: 0 2rpx 8rpx rgba(72, 209, 204, 0.4);
}

.score-value {
    width: 60rpx;
    text-align: right;
    font-size: 28rpx;
    color: #667eea;
    font-weight: 600;
}

/* 幸运信息 */
.lucky-section {
    background: #fff;
    border-radius: 24rpx;
    padding: 40rpx 30rpx;
    margin: 0 30rpx 30rpx 30rpx;
    box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.08);
}

.lucky-grid {
    display: flex;
    justify-content: space-between;
    gap: 20rpx;
}

.lucky-card {
    flex: 1;
    text-align: center;
    padding: 20rpx 10rpx;
    background: linear-gradient(180deg, #fafafa 0%, #fff 100%);
    border-radius: 16rpx;
    transition: transform 0.2s;
}

.lucky-card:active {
    transform: scale(0.95);
}

.lucky-icon-wrapper {
    width: 80rpx;
    height: 80rpx;
    margin: 0 auto 15rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);
}

.lucky-icon {
    font-size: 36rpx;
}

.lucky-label {
    display: block;
    font-size: 24rpx;
    color: #999;
    margin-bottom: 15rpx;
}

.lucky-color-box {
    width: 50rpx;
    height: 50rpx;
    margin: 0 auto 10rpx;
    border-radius: 12rpx;
    box-shadow: 0 4rpx 8rpx rgba(0, 0, 0, 0.1);
    border: 2rpx solid #f0f0f0;
}

.lucky-value {
    display: block;
    font-size: 28rpx;
    color: #333;
    font-weight: 600;
}

.lucky-number {
    display: block;
    font-size: 36rpx;
    color: #667eea;
    font-weight: 700;
    font-family: 'Arial', sans-serif;
}

/* 宜忌 */
.suitable-avoid-section {
    margin: 0 30rpx 30rpx 30rpx;
    display: flex;
    gap: 20rpx;
}

.suitable-card {
    flex: 1;
    background: linear-gradient(135deg, #48d1cc 0%, #20b2aa 100%);
    border-radius: 20rpx;
    padding: 30rpx;
    box-shadow: 0 8rpx 20rpx rgba(72, 209, 204, 0.3);
}

.avoid-card {
    flex: 1;
    background: linear-gradient(135deg, #ff6b9d 0%, #ff8fab 100%);
    border-radius: 20rpx;
    padding: 30rpx;
    box-shadow: 0 8rpx 20rpx rgba(255, 107, 157, 0.3);
}

.suitable-header,
.avoid-header {
    display: flex;
    align-items: center;
    margin-bottom: 20rpx;
}

.suitable-icon-wrapper,
.avoid-icon-wrapper {
    width: 50rpx;
    height: 50rpx;
    background: rgba(255, 255, 255, 0.3);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 15rpx;
}

.suitable-icon,
.avoid-icon {
    font-size: 28rpx;
}

.suitable-title,
.avoid-title {
    font-size: 32rpx;
    color: #fff;
    font-weight: 600;
}

.suitable-text,
.avoid-text {
    font-size: 26rpx;
    color: rgba(255, 255, 255, 0.95);
    line-height: 1.8;
}

/* 详细运势 */
.details-section {
    margin: 0 30rpx 30rpx 30rpx;
}

.detail-card {
    background: #fff;
    border-radius: 24rpx;
    padding: 35rpx 30rpx;
    margin-bottom: 25rpx;
    box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.08);
    border-left: 6rpx solid #667eea;
    transition: transform 0.2s, box-shadow 0.2s;
}

.detail-card:active {
    transform: translateY(-2rpx);
    box-shadow: 0 12rpx 32rpx rgba(0, 0, 0, 0.12);
}

.detail-card.love {
    border-left-color: #ff6b9d;
}

.detail-card.career {
    border-left-color: #4facfe;
}

.detail-card.wealth {
    border-left-color: #feca57;
}

.detail-card.health {
    border-left-color: #48d1cc;
}

.detail-header {
    display: flex;
    align-items: center;
    margin-bottom: 25rpx;
    padding-bottom: 20rpx;
    border-bottom: 2rpx solid #f5f5f5;
}

.detail-icon-wrapper {
    width: 56rpx;
    height: 56rpx;
    border-radius: 14rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 20rpx;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);
}

.detail-icon {
    font-size: 32rpx;
}

.detail-title {
    font-size: 34rpx;
    color: #333;
    font-weight: 600;
}

.detail-content {
    display: block;
    font-size: 28rpx;
    color: #666;
    line-height: 2;
    text-align: justify;
}
</style>

