<template>
    <view id="app">
    </view>
</template>

<script setup>
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'
import apiConfig from '@/common/config/api-config.js'

onLaunch(() => {
    console.log('App Launch')
    // 初始化登录
    initLogin()
})

onShow(() => {
    console.log('App Show')
})

onHide(() => {
    console.log('App Hide')
})

async function initLogin() {
    // 静默登录
    try {
        // 检查是否已有token
        const existingToken = uni.getStorageSync('token')
        if (existingToken) {
            console.log('已有token，跳过登录')
            return
        }

        const code = await getWxCode()
        const res = await uni.request({
            url: `${apiConfig.BASE_URL}/auth/wx-login`,
            method: 'POST',
            data: { code }
        })
        if (res.data && res.data.code === 200) {
            uni.setStorageSync('token', res.data.data.token)
            uni.setStorageSync('openid', res.data.data.openid)
            console.log('登录成功，token已保存')
        } else {
            console.warn('登录失败:', res.data)
        }
    } catch (e) {
        console.error('登录失败', e)
    }
}

function getWxCode() {
    return new Promise((resolve, reject) => {
        uni.login({
            provider: 'weixin',
            success: (res) => {
                resolve(res.code)
            },
            fail: reject
        })
    })
}
</script>

<style>
/*每个页面公共css */
page {
    background-color: #f5f5f5;
}
</style>

