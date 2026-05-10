import { defineStore } from 'pinia'
import { nextTick } from 'vue'
import { authApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    accessToken: undefined as string | undefined,
    userInfo: undefined as any,
    expiresIn: undefined as number | undefined,
    tokenExpireTime: undefined as number | undefined,
  }),
  
  getters: {
    isAuthenticated: (state) => !!state.accessToken,
    isLoggedIn: (state) => !!state.accessToken,
  },
  
  actions: {
    setToken({ accessToken, expiresIn }: { accessToken: string, expiresIn?: number }) {
      this.accessToken = accessToken
      this.expiresIn = expiresIn || 86400 // 默认24小时
      // 计算过期时间
      this.tokenExpireTime = new Date().getTime() + (this.expiresIn * 1000)
    },
    
    setUserInfo(userInfo: any) {
      this.userInfo = userInfo
    },
    
    resetToken() {
      this.accessToken = undefined
      this.expiresIn = undefined
      this.tokenExpireTime = undefined
    },
    
    toLogin() {
      // 跳转到登录页面
      if (typeof window !== 'undefined') {
        const currentPath = window.location.pathname
        if (currentPath !== '/auth/login') {
          window.location.href = `/auth/login?redirect=${encodeURIComponent(currentPath)}`
        }
      }
    },
    
    async switchCurrentRole(data: { accessToken: string, expiresIn?: number }) {
      console.log('切换角色', data)
      this.resetLoginState()
      await nextTick()
      this.setToken(data)
    },
    
    resetLoginState() {
      // 重置token
      this.resetToken()
      // 重置用户信息
      this.userInfo = undefined
      // 清除本地存储
      if (typeof window !== 'undefined') {
        localStorage.removeItem('mqxq-auth')
        localStorage.removeItem('mqxqtoken')
        localStorage.removeItem('user')
      }
    },
    
    async logout() {
      try {
        if (this.accessToken) {
          await authApi.logout()
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
        const response = await authApi.refreshToken()
        if (response && 'token' in response) {
          this.setToken({
            accessToken: response.token,
            expiresIn: (response as any).expiresIn
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
        const userInfo = await authApi.getUserInfo()
        console.log('获取用户信息成功', userInfo)
        if (userInfo) {
          this.setUserInfo(userInfo)
          return userInfo
        }
  
        return null
      } catch (error) {
        console.error('获取用户信息失败', error)
        return null
      }
    },

    // 登录设置
    async login(loginData: { token: string, user: any, expiresIn?: number }) {
      this.setToken({
        accessToken: loginData.token,
        expiresIn: loginData.expiresIn
      })
      this.setUserInfo(loginData.user)
    },

    // 检查登录状态
    async checkLoginStatus() {
      try {
        const isLoggedIn = await authApi.checkLoginStatus()
        if (!isLoggedIn) {
          this.resetLoginState()
        }
        return isLoggedIn
      } catch (error) {
        console.error('检查登录状态失败', error)
        this.resetLoginState()
        return false
      }
    },

    // 初始化认证状态
    async initAuth() {
      try {
        // 从localStorage恢复token
        const token = localStorage.getItem('mqxqtoken')
        const user = localStorage.getItem('user')
        
        if (token && user) {
          this.accessToken = token
          this.userInfo = JSON.parse(user)
          
          // 验证token是否有效
          const isValid = await this.checkLoginStatus()
          if (!isValid) {
            this.resetLoginState()
          }
        }
      } catch (error) {
        console.error('初始化认证状态失败', error)
        this.resetLoginState()
      }
    },

    // 设置认证信息
    setAuth(authData: { token: string, user: any, expiresIn?: number }) {
      this.setToken({
        accessToken: authData.token,
        expiresIn: authData.expiresIn
      })
      this.setUserInfo(authData.user)
      
      // 保存到localStorage
      localStorage.setItem('mqxqtoken', authData.token)
      localStorage.setItem('user', JSON.stringify(authData.user))
      
      // 设置cookie（如果在浏览器环境中）
      if (typeof document !== 'undefined') {
        const expiresIn = authData.expiresIn || 86400 // 默认24小时
        const expiresDate = new Date(Date.now() + expiresIn * 1000)
        document.cookie = `mqxqtoken=${authData.token}; expires=${expiresDate.toUTCString()}; path=/`
      }
    },

    // 更新用户资料
    async updateProfile(profileData: any) {
      try {
        await authApi.updateProfile(profileData)
        // 更新成功后重新获取用户信息
        await this.getUserInfo()
        // 更新本地存储的用户信息
        if (this.userInfo) {
          localStorage.setItem('user', JSON.stringify(this.userInfo))
        }
        return true
      } catch (error) {
        console.error('更新用户资料失败', error)
        throw error
      }
    }
  },
  
  persist: {
    key: 'mqxq-auth',
  }
})