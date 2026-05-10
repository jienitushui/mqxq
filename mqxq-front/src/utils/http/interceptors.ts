import { useAuthStore } from '@/store/modules/auth'
import router from '@/router'
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios'

export function setupInterceptors(axiosInstance: AxiosInstance) {
  const SUCCESS_CODES = [0, 200, '0', '200', 'SUCCESS']
  
  // 请求拦截器
  axiosInstance.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
      // 不需要token的公开接口白名单
      const publicPaths = [
        '/api/auth/login',
        '/api/auth/register',
        '/api/auth/check-username',
        '/api/auth/send-email-code',
        '/api/file/upload',
        '/api/public/' // 所有公开接口
      ]
      
      // 需要token的公开接口（虽然路径包含public但需要身份验证）
      const publicButNeedTokenPaths = [
        '/api/public/files/avatar',
        '/api/public/course/'
      ]
      
      // 检查是否为需要token的公开接口
      const isPublicButNeedToken = publicButNeedTokenPaths.some(path => config.url?.includes(path))
      
      // 检查是否为完全公开的接口（不包括需要token的公开接口）
      const isCompletelyPublic = publicPaths.some(path => config.url?.includes(path)) && !isPublicButNeedToken
      
      // 处理不需要token的请求
      if ((config as any).needToken === false || isCompletelyPublic) {
        // console.log('跳过token添加:', config.url)
        return config
      }

      // console.log('准备添加token到:', config.url)

      // 优先从localStorage获取token
      let accessToken: string | null = localStorage.getItem('mqxqtoken')
      
      // 如果localStorage没有，再从store获取
      if (!accessToken) {
        try {
          const authStore = useAuthStore()
          accessToken = authStore.accessToken || null
        } catch (error) {
          // 如果store还未初始化，忽略错误
          console.warn('Auth store not initialized yet')
        }
      }
      
      // 添加token到请求头
      if (accessToken && config.headers) {
        config.headers['satoken'] = accessToken
        config.headers['token'] = accessToken
        config.headers['mqxqtoken'] = accessToken
        config.headers.Authorization = `Bearer ${accessToken}`
        // console.log('已添加token到请求头:', config.url, 'token:', accessToken.substring(0, 10) + '...')
      } else {
        console.log('未添加token:', config.url, 'accessToken:', !!accessToken, 'headers:', !!config.headers)
      }

      return config
    },
    (error: any) => {
      return Promise.reject(error)
    }
  )

  // 响应拦截器
  axiosInstance.interceptors.response.use(
    (response: AxiosResponse) => {
      const { data, status, config, statusText, headers } = response
      
      if (headers['content-type']?.includes('json')) {
        if (SUCCESS_CODES.includes(data?.code)) {
          return Promise.resolve(data.data || data)
        }
        
        const code = data?.code ?? status
        const needTip = (config as any)?.needTip !== false
        const message = resolveResError(code, data?.msg ?? statusText, needTip)
        
        return Promise.reject({ code, message, error: data ?? response })
      }
      
      return Promise.resolve(data ?? response)
    },
    async (error: any) => {
      if (!error || !error.response) {
        const code = error?.code
        const message = resolveResError(code, error.message)
        return Promise.reject({ code, message, error })
      }

      const { data, status, config } = error.response
      const code = data?.code ?? status
      
      // 处理401错误（Token无效或已过期）
      if (code === 401 || code === '401' || code === 4004) {
        try {
          const authStore = useAuthStore()
          // 清除登录状态
          authStore.resetLoginState()
          
          // 获取当前路径用于登录后重定向
          const currentPath = window.location.pathname + window.location.search
          
          // 使用路由跳转到登录页
          router.push({
            name: 'Login',
            query: { redirect: currentPath }
          })
        } catch (error) {
          console.warn('Auth store not available for logout')
          // 如果store不可用，使用window.location跳转
          window.location.href = '/auth/login'
        }
        
        // 返回一个特殊的错误，避免继续处理
        return Promise.reject({ 
          code: 401, 
          message: '请重新登录', 
          error: data,
          isAuthError: true 
        })
      }

      const needTip = (config as any)?.needTip !== false
      const message = resolveResError(code, data?.msg ?? error.message, needTip)
      return Promise.reject({ code, message, error: error.response?.data || error.response })
    }
  )
}

function resolveResError(code: any, message: string, needTip = true) {
  let errorMessage = message
  
  switch (code) {
    case 401:
      errorMessage = '未授权，请重新登录'
      break
    case 403:
      errorMessage = '拒绝访问'
      break
    case 404:
      errorMessage = '请求地址不存在'
      break
    case 500:
      errorMessage = '服务器内部错误'
      break
    default:
      errorMessage = message || '网络错误'
  }
  
  if (needTip) {
    console.error(errorMessage)
  }
  
  return errorMessage
}