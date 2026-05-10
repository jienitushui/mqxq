/**********************************
 * @Author: Ronnie Zhang
 * @LastEditor: Ronnie Zhang
 * @LastEditTime: 2023/12/05 21:25:39
 * @Email: zclzone@outlook.com
 * Copyright © 2023 Ronnie Zhang(大脸怪) | https://isme.top
 **********************************/

import { defineStore } from 'pinia'
import { usePermissionStore, useRouterStore, useTabStore, useUserStore } from '@/store'
import api from '@/views/login/api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    accessToken: undefined,
    userInfo: undefined,
    expiresIn: undefined,
    tokenExpireTime: undefined,
  }),
  actions: {
    setToken({ accessToken, expiresIn }) {
      this.accessToken = accessToken
      this.expiresIn = expiresIn || 86400 // 默认24小时
      // 计算过期时间
      this.tokenExpireTime = new Date().getTime() + (this.expiresIn * 1000)
    },
    setUserInfo(userInfo) {
      this.userInfo = userInfo
    },
    resetToken() {
      this.accessToken = undefined
      this.expiresIn = undefined
      this.tokenExpireTime = undefined
    },
    toLogin() {
      const { router, route } = useRouterStore()
      router.replace({
        path: '/login',
        query: route.query,
      })
    },
    async switchCurrentRole(data) {
      console.log('切换角色', data)
      this.resetLoginState()
      await nextTick()
      this.setToken(data)
    },
    resetLoginState() {
      const { resetUser } = useUserStore()
      const { resetRouter } = useRouterStore()
      const { resetPermission, accessRoutes } = usePermissionStore()
      const { resetTabs } = useTabStore()
      // 重置路由
      resetRouter(accessRoutes)
      // 重置用户
      resetUser()
      // 重置权限
      resetPermission()
      // 重置Tabs
      resetTabs()
      // 重置token
      this.resetToken()
      // 重置用户信息
      this.userInfo = undefined
    },
    async logout() {
      try {
        if (this.accessToken) {
          await api.logout()
        }
      } catch (error) {
        console.error('登出失败', error)
      } finally {
        this.resetLoginState()
        this.toLogin()
      }
    },
    // 检查token是否即将过期（30分钟内）
    isTokenExpiringSoon() {
      if (!this.tokenExpireTime) return false
      const now = new Date().getTime()
      const thirtyMinutes = 30 * 60 * 1000
      return this.tokenExpireTime - now < thirtyMinutes
    },
    // 刷新token
    async refreshToken() {
      try {
        const { data } = await api.refreshToken()
        if (data?.token) {
          this.setToken({
            accessToken: data.token,
            expiresIn: data.expiresIn
          })
          return true
        }
        return false
      } catch (error) {
        console.error('刷新token失败', error)
        return false
      }
    },
    // 获取用户信息
    async getUserInfo() {
      try {
        const { data } = await api.getUserInfo()
        if (data) {
          this.setUserInfo(data)
          return data
        }
        return null
      } catch (error) {
        console.error('获取用户信息失败', error)
        return null
      }
    }
  },
  persist: {
    key: 'vue-naivue-admin_auth',
  },
})
