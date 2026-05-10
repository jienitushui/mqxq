import request from '@/utils/request'
import type {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  ChangePasswordRequest,
  ForgotPasswordRequest,
  BindEmailRequest,
  SendEmailCodeRequest,
  UpdateProfileRequest,
  CaptchaResponse,
  UserInfo
} from './types/auth'

// 认证相关API
export const authApi = {
  // 获取验证码
  getCaptcha(): Promise<CaptchaResponse> {
    return request.get('/api/auth/captcha')
  },

  // 用户登录
  login(data: LoginRequest): Promise<LoginResponse> {
    return request.post('/api/auth/login', data)
  },

  // 用户注册
  register(data: RegisterRequest): Promise<void> {
    return request.post('/api/auth/register', data)
  },

  // 用户登出
  logout(): Promise<void> {
    return request.post('/api/auth/logout')
  },

  // 刷新令牌
  refreshToken(): Promise<LoginResponse> {
    return request.post('/api/auth/refresh-token')
  },

  // 获取当前用户信息
  getUserInfo(): Promise<UserInfo> {
    return request.get('/api/auth/userinfo')
  },

  // 检查登录状态
  checkLoginStatus(): Promise<boolean> {
    return request.get('/api/auth/check-login-status')
  },

  // 检查用户名可用性
  checkUsername(username: string): Promise<boolean> {
    return request.get('/api/auth/check-username', { params: { username } })
  },

  // 检查邮箱可用性
  checkEmail(username: string): Promise<boolean> {
    return request.get('/api/auth/check-username', { params: { username } })
  },

  // 发送邮箱验证码
  sendEmailCode(data: SendEmailCodeRequest): Promise<void> {
    return request.post('/api/auth/send-email-code', data)
  },

  // 修改密码
  changePassword(data: ChangePasswordRequest): Promise<void> {
    return request.post('/api/auth/change-password', data)
  },

  // 忘记密码
  forgotPassword(data: ForgotPasswordRequest): Promise<void> {
    return request.post('/api/auth/forgot-password', data)
  },

  // 绑定邮箱
  bindEmail(data: BindEmailRequest): Promise<void> {
    return request.post('/api/auth/bind-email', data)
  },

  // 更新用户资料
  updateProfile(data: UpdateProfileRequest): Promise<void> {
    return request.post('/api/auth/update-profile', data)
  },

  // 验证邮箱
  verifyEmail(token: string): Promise<void> {
    return request.post('/api/auth/verify-email', { token })
  },

  // 重新发送验证邮件
  resendVerificationEmail(email: string): Promise<void> {
    return request.post('/api/auth/resend-verification-email', { email })
  },

  // 检查邮箱验证状态
  checkEmailVerificationStatus(email: string): Promise<{ verified: boolean }> {
    return request.get('/api/auth/check-email-verification-status', { params: { email } })
  }
}
