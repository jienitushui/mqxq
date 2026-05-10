/**********************************
 * @Author: Ronnie Zhang
 * @LastEditor: Ronnie Zhang
 * @LastEditTime: 2023/12/05 21:28:30
 * @Email: zclzone@outlook.com
 * Copyright © 2023 Ronnie Zhang(大脸怪) | https://isme.top
 **********************************/

import { request } from '@/utils'

export default {
  // 统一登录接口
  login: data => request.post('/api/auth/login', data, { needToken: false }),
  // 获取图形验证码 
  getCaptcha: () => request.get('/api/auth/captcha', { needToken: false }),
  // 获取当前用户信息
  getUserInfo: () => request.get('/api/auth/user-info'),
  // 用户登出
  logout: () => request.post('/api/auth/logout'),
  // 刷新Token
  refreshToken: () => request.post('/api/auth/refresh-token'),
  // 检查用户名是否可用
  checkUsername: username => request.get(`/api/auth/check-username?username=${username}`, { needToken: false }),
  // 检查邮箱是否可用
  checkEmail: email => request.get(`/api/auth/check-email?email=${email}`, { needToken: false }),
  // 发送邮箱验证码
  sendEmailCode: data => request.post('/api/auth/send-email-code', data, { needToken: false }),
  // 忘记密码
  forgotPassword: data => request.post('/api/auth/forgot-password', data, { needToken: false }),
  // 修改密码
  changePassword: data => request.put('/api/auth/change-password', data),
  // 更新用户资料
  updateProfile: data => request.put('/api/auth/update-profile', data),
  // 绑定邮箱
  bindEmail: data => request.post('/api/auth/bind-email', data),
}
