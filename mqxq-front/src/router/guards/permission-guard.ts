import { useAuthStore } from '@/store/modules/auth'
import type { Router } from 'vue-router'

const WHITE_LIST = ['/auth/login', '/auth/register', '/404', '/']

export function createPermissionGuard(router: Router) {
  router.beforeEach(async (to) => {
    const authStore = useAuthStore()
    const token = authStore.accessToken

    /** 没有token */
    if (!token) {
      if (WHITE_LIST.includes(to.path)) {
        return true
      }
      return { 
        path: '/auth/login', 
        query: { ...to.query, redirect: to.path } 
      }
    }

    // 有token的情况
    if (to.path === '/auth/login') {
      return { path: '/' }
    }
    
    if (WHITE_LIST.includes(to.path)) {
      return true
    }

    // 检查用户信息
    if (!authStore.userInfo) {
      try {
        await authStore.getUserInfo()
      } catch (error) {
        console.error('获取用户信息失败:', error)
        authStore.logout()
        return { 
          path: '/auth/login', 
          query: { ...to.query, redirect: to.path } 
        }
      }
    }

    return true
  })
}