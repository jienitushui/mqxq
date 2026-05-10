// 登录请求参数
export interface LoginRequest {
  username: string
  password: string
  captcha: string
  captchaKey: string
  role?: string
}

// 注册请求参数
export interface RegisterRequest {
  username: string
  password: string
  confirmPassword: string
  captcha: string
  captchaKey: string
  code: string
  userType: string
}

// 修改密码请求参数
export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

// 忘记密码请求参数
export interface ForgotPasswordRequest {
  username: string
  code: string
  newPassword: string
  confirmPassword: string
}

// 绑定邮箱请求参数
export interface BindEmailRequest {
  email: string
  emailCode: string
}

// 发送邮箱验证码请求参数
export interface SendEmailCodeRequest {
  username: string
  type: 'REGISTER' | 'RESET_PASSWORD' | 'BIND_EMAIL'
}

// 更新用户资料请求参数
export interface UpdateProfileRequest {
  name?: string
  phone?: string
  avatar?: string
}

// 验证码响应
export interface CaptchaResponse {
  captchaKey: string
  captchaImage: string
}

// 登录响应
export interface LoginResponse {
  token: string
  refreshToken: string
  userId: number
  username: string
  nickname: string
  phone?: string
  avatar?: string
  roles?: string[]
  expiresIn?: number
  userInfo?: UserInfo
}

// 用户信息
export interface UserInfo {
  id: number
  username: string
  nickname: string
  email: string
  phone?: string
  avatar?: string
  userType?: number
  roles?: string[]
  status: number
  createTime: string
}