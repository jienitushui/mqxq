/**********************************
 * @FilePath: interceptors.js
 * @Author: Ronnie Zhang
 * @LastEditor: Ronnie Zhang
 * @LastEditTime: 2023/12/04 22:46:40
 * @Email: zclzone@outlook.com
 * Copyright © 2023 Ronnie Zhang(大脸怪) | https://isme.top
 **********************************/

import { useAuthStore } from '@/store'
import { resolveResError } from './helpers'

export function setupInterceptors(axiosInstance) {
  const SUCCESS_CODES = [0, 200, '0', '200']
  function resResolve(response) {
    const { config } = response
    
    // 如果是 blob 响应，直接返回 data（blob 对象），避免访问其他属性
    if (config?.responseType === 'blob') {
      return Promise.resolve(response.data)
    }
    
    const { data, status, statusText, headers } = response
    
    if (headers['content-type']?.includes('json')) {
      if (SUCCESS_CODES.includes(data?.code)) {
        return Promise.resolve(data)
      }
      const code = data?.code ?? status

      const needTip = config?.needTip !== false

      // 根据code处理对应的操作，并返回处理后的message
      const message = resolveResError(code, data?.msg ?? statusText, needTip)

      return Promise.reject({ code, message, error: data ?? response })
    }
    return Promise.resolve(data ?? response)
  }

  axiosInstance.interceptors.request.use(reqResolve, reqReject)
  axiosInstance.interceptors.response.use(resResolve, resReject)
}

// 是否正在刷新Token
let isRefreshing = false
// 等待刷新Token的请求队列
let requestsQueue = []

// 执行队列中的请求
function executeQueuedRequests(token) {
  requestsQueue.forEach(cb => cb(token))
  requestsQueue = []
}

async function reqResolve(config) {
  // 处理不需要token的请求
  if (config.needToken === false) {
    return config
  }

  const authStore = useAuthStore()
  const { accessToken, isTokenExpiringSoon } = authStore
  
  // 如果有token且即将过期，尝试刷新token
  if (accessToken && isTokenExpiringSoon() && !config.url.includes('/api/auth/refresh-token')) {
    if (!isRefreshing) {
      isRefreshing = true
      
      try {
        // 尝试刷新token
        const refreshSuccess = await authStore.refreshToken()
        
        if (refreshSuccess) {
          // 刷新成功，执行队列中的请求
          executeQueuedRequests(authStore.accessToken)
        } else {
          // 刷新失败，清空队列
          requestsQueue = []
          // 跳转到登录页
          authStore.logout()
          return Promise.reject({ message: 'Token刷新失败，请重新登录' })
        }
      } catch (error) {
        // 刷新出错，清空队列
        requestsQueue = []
        // 跳转到登录页
        authStore.logout()
        return Promise.reject({ message: 'Token刷新出错，请重新登录' })
      } finally {
        isRefreshing = false
      }
    }
    
    // 将请求加入队列
    return new Promise((resolve) => {
      requestsQueue.push((token) => {
        // 使用新的token继续请求
        config.headers.Authorization = `Bearer ${token}`
        resolve(config)
      })
    })
  }
  
  // 正常请求，添加token
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`
  }

  return config
}

function reqReject(error) {
  return Promise.reject(error)
}

async function resReject(error) {
  if (!error || !error.response) {
    const code = error?.code
    /** 根据code处理对应的操作，并返回处理后的message */
    const message = resolveResError(code, error.message)
    return Promise.reject({ code, message, error })
  }

  const { data, status, config } = error.response
  
  // 如果是 blob 响应类型的错误，直接返回错误
  if (config?.responseType === 'blob') {
    return Promise.reject(error)
  }
  
  const code = data?.code ?? status
  
  // 处理401错误（Token无效或已过期）
  if (code === 401 || code === 4004) {
    // 如果不是刷新Token的请求，尝试刷新Token
    if (!config.url.includes('/api/auth/refresh-token') && !isRefreshing) {
      isRefreshing = true
      
      try {
        const authStore = useAuthStore()
        // 尝试刷新token
        const refreshSuccess = await authStore.refreshToken()
        
        if (refreshSuccess) {
          // 刷新成功，重试原请求
          const newConfig = { ...config }
          newConfig.headers.Authorization = `Bearer ${authStore.accessToken}`
          
          // 执行队列中的请求
          executeQueuedRequests(authStore.accessToken)
          
          // 重试当前请求
          return axiosInstance(newConfig)
        } else {
          // 刷新失败，清空队列
          requestsQueue = []
          // 跳转到登录页
          authStore.logout()
        }
      } catch (error) {
        // 刷新出错，清空队列
        requestsQueue = []
        // 跳转到登录页
        const authStore = useAuthStore()
        authStore.logout()
      } finally {
        isRefreshing = false
      }
    }
  }

  const needTip = config?.needTip !== false
  const message = resolveResError(code, data?.msg ?? error.message, needTip)
  return Promise.reject({ code, message, error: error.response?.data || error.response })
}
