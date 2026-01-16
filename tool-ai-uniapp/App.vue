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

async function doLogin() {
    try {
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
            return true
        } else {
            console.warn('登录失败:', res.data)
            return false
        }
    } catch (e) {
        console.error('登录失败', e)
        return false
    }
}

async function initLogin() {
    // 静默登录 - 每次启动都重新登录以确保token有效
    // 微信登录是静默的，不会影响用户体验
    console.log('开始初始化登录...')
    await doLogin()
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

