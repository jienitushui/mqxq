/**********************************
 * @FilePath: helpers.js
 * @Author: Ronnie Zhang
 * @LastEditor: Ronnie Zhang
 * @LastEditTime: 2023/12/04 22:46:22
 * @Email: zclzone@outlook.com
 * Copyright © 2023 Ronnie Zhang(大脸怪) | https://isme.top
 **********************************/

import { useAuthStore } from '@/store'

let isConfirming = false

/**
 * 处理401错误的通用函数
 * @param {Object} error - 错误对象
 * @param {Object} message - 消息实例
 * @returns {Promise<boolean>} - 返回true表示已处理401错误，false表示不是401错误
 */
export async function handle401Error(error, message) {
  // 检查是否是401错误
  const is401 = error?.code === 401 || 
                error?.response?.status === 401 || 
                error?.status === 401 ||
                (error?.response?.data?.code === 401)
  
  if (!is401) {
    return false
  }
  
  // 如果已经在确认中，直接返回
  if (isConfirming) {
    return true
  }
  
  isConfirming = true
  
  // 显示确认对话框
  $dialog.confirm({
    title: '提示',
    type: 'info',
    content: '登录已过期，是否重新登录？',
    confirm() {
      useAuthStore().logout()
      window.$message?.success('已退出登录')
      isConfirming = false
    },
    cancel() {
      isConfirming = false
    },
  })
  
  return true
}
export function resolveResError(code, message, needTip = true) {
  switch (code) {
    case 401:
      if (isConfirming || !needTip)
        return
      isConfirming = true
      $dialog.confirm({
        title: '提示',
        type: 'info',
        content: '登录已过期，是否重新登录？',
        confirm() {
          useAuthStore().logout()
          window.$message?.success('已退出登录')
          isConfirming = false
        },
        cancel() {
          isConfirming = false
        },
      })
      return false
    
    // 账户锁定
    case 4003: // 账户已被锁定
      if (isConfirming || !needTip)
        return
      isConfirming = true
      $dialog.confirm({
        title: '账户锁定',
        type: 'warning',
        content: '您的账户已被锁定，请稍后再试或联系管理员',
        confirm() {
          useAuthStore().logout()
          window.$message?.success('已退出登录')
          isConfirming = false
        },
        cancel() {
          isConfirming = false
        },
      })
      return false
    
    // 权限不足
    case 4005: // 权限不足
      message = message || '您没有权限执行此操作'
      break
    
    // 用户名或邮箱已存在
    case 4006: // 用户名已存在
    case 4007: // 邮箱已存在
      message = message || '用户名或邮箱已被使用'
      break
    
    // 验证码错误
    case 4002: // 验证码错误或已过期
    case 4008: // 邮箱验证码错误
      message = message || '验证码错误或已过期'
      break
    
    // 用户名或密码错误
    case 4001: // 用户名或密码错误
      message = message || '用户名或密码错误'
      break
    
    // 业务错误
    case 5001: // 邮件发送失败
    case 5002: // 验证码生成失败
    case 5003: // 密码格式不符合要求
    case 5004: // 原密码错误
    case 5005: // 用户不存在
      message = message || '操作失败，请稍后再试'
      break
    
    // HTTP状态码
    case 403:
      message = message || '请求被拒绝'
      break
    case 404:
      message = message || '请求资源或接口不存在'
      break
    case 500:
      message = message || '服务器发生异常'
      break
    default:
      message = message ?? `【${code}】: 未知异常!`
      break
  }
  needTip && window.$message?.error(message)
  return message
}
