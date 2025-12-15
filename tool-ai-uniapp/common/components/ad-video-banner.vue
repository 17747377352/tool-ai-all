<template>
    <view class="ad-banner-container" v-if="showBanner">
        <!-- #ifdef MP-WEIXIN -->
        <ad
            :unit-id="bannerAdUnitId"
            ad-type="banner"
            ad-theme="white"
            @load="onBannerLoad"
            @error="onBannerError"
        ></ad>
        <!-- #endif -->
    </view>
</template>

<script>
import { ENABLE_AD, AD_CONFIG } from '@/common/config/ad-config.js';

export default {
    name: 'AdVideoBanner',
    props: {
        bannerAdUnitId: {
            type: String,
            default: AD_CONFIG.BANNER_AD_UNIT_ID
        }
    },
    data() {
        return {
            showBanner: ENABLE_AD // 根据配置决定是否显示
        };
    },
    methods: {
        onBannerLoad() {
            console.log('Banner广告加载成功');
        },
        onBannerError(err) {
            console.error('Banner广告加载失败', err);
            this.showBanner = false;
        }
    }
};
</script>

<style scoped>
.ad-banner-container {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    z-index: 999;
    background-color: #fff;
}
</style>

