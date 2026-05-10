<template>
  <div
    class="bg-gray-50 min-h-screen flex items-center justify-center p-4 font-sans"
    style="margin: auto; min-width: 100vw"
  >
    <!-- 忘记密码卡片容器 -->
    <div
      class="w-full max-w-5xl bg-white rounded-2xl shadow-soft overflow-hidden flex flex-col md:flex-row transition-custom"
    >
      <!-- 左侧品牌展示区 -->
      <div
        class="md:w-1/2 bg-gradient-primary p-8 md:p-12 flex flex-col justify-between text-white"
      >
        <div>
          <div class="flex items-center mb-10">
            <i class="fas fa-code text-3xl mr-3"></i>
            <h1 class="text-[clamp(1.5rem,3vw,2.5rem)] font-bold text-shadow">
              码趣星球
            </h1>
          </div>
          <h2 class="text-[clamp(1.2rem,2vw,1.8rem)] font-semibold mb-4">
            找回您的学习账号
          </h2>
          <p class="text-white/90 mb-8 max-w-md">
            忘记密码不用担心，通过邮箱验证码即可快速重置密码，继续您的编程学习之旅。
          </p>
          <div class="space-y-6">
            <div class="flex items-start">
              <div class="bg-white/20 p-3 rounded-full mr-4">
                <i class="fas fa-envelope"></i>
              </div>
              <div class="text-left">
                <h3 class="font-semibold text-lg mb-1">邮箱验证</h3>
                <p class="text-white/80">
                  输入注册邮箱，我们将发送6位验证码到您的邮箱
                </p>
              </div>
            </div>
            <div class="flex items-start">
              <div class="bg-white/20 p-3 rounded-full mr-4">
                <i class="fas fa-shield-alt"></i>
              </div>
              <div class="text-left">
                <h3 class="font-semibold text-lg mb-1">安全验证</h3>
                <p class="text-white/80">
                  验证码5分钟内有效，确保账号安全重置
                </p>
              </div>
            </div>
            <div class="flex items-start">
              <div class="bg-white/20 p-3 rounded-full mr-4">
                <i class="fas fa-key"></i>
              </div>
              <div class="text-left">
                <h3 class="font-semibold text-lg mb-1">设置新密码</h3>
                <p class="text-white/80">
                  输入验证码后即可设置新密码，立即恢复学习
                </p>
              </div>
            </div>
          </div>
        </div>
        <div class="mt-12">
          <img
            alt="密码重置"
            class="w-full h-48 object-cover rounded-lg shadow-lg"
            src="https://design.gemcoder.com/staticResource/echoAiSystemImages/c642a05dacad5beff45ef5befa42d5e3.png"
          />
        </div>
      </div>

      <!-- 右侧表单区 -->
      <div class="md:w-1/2 p-8 md:p-12 flex flex-col justify-center">
        <div class="max-w-md mx-auto w-full">
          <div class="text-center mb-10">
            <h2 class="text-[clamp(1.5rem,3vw,2rem)] font-bold text-dark mb-2">
              忘记密码
            </h2>
            <p class="text-gray-600">请输入您的邮箱地址和验证码，设置新密码</p>
          </div>

          <!-- 忘记密码表单 -->
          <el-form
            ref="forgotPasswordFormRef"
            :model="forgotPasswordForm"
            :rules="forgotPasswordRules"
            class="space-y-6"
            @submit.prevent="handleForgotPassword"
          >
            <div>
              <label
                class="block text-sm font-medium text-gray-700 mb-1 text-left"
                for="username"
              >
                邮箱地址
              </label>
              <div class="relative">
                <el-input
                  v-model="forgotPasswordForm.username"
                  class="login-input"
                  placeholder="请输入注册时使用的邮箱地址"
                  size="large"
                >
                  <template #prefix>
                    <i class="fas fa-envelope text-gray-400"></i>
                  </template>
                </el-input>
              </div>
            </div>

            <div>
              <label
                class="block text-sm font-medium text-gray-700 mb-1 text-left"
                for="code"
              >
                邮箱验证码
              </label>
              <div class="flex gap-3">
                <el-input
                  v-model="forgotPasswordForm.code"
                  class="login-input flex-1"
                  placeholder="请输入6位验证码"
                  size="large"
                  maxlength="6"
                >
                  <template #prefix>
                    <i class="fas fa-shield-alt text-gray-400"></i>
                  </template>
                </el-input>
                <el-button
                  type="default"
                  size="large"
                  class="send-code-btn"
                  :loading="sendCodeLoading"
                  :disabled="countdown > 0"
                  @click="sendEmailCode"
                >
                  {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
                </el-button>
              </div>
            </div>

            <div>
              <label
                class="block text-sm font-medium text-gray-700 mb-1 text-left"
                for="newPassword"
              >
                新密码
              </label>
              <div class="relative">
                <el-input
                  v-model="forgotPasswordForm.newPassword"
                  :type="showPassword ? 'text' : 'password'"
                  class="login-input"
                  placeholder="请输入新密码"
                  size="large"
                >
                  <template #prefix>
                    <i class="fas fa-lock text-gray-400"></i>
                  </template>
                  <template #suffix>
                    <i
                      :class="showPassword ? 'fas fa-eye' : 'fas fa-eye-slash'"
                      class="text-gray-400 hover:text-gray-600 cursor-pointer transition-custom"
                      @click="togglePassword"
                    ></i>
                  </template>
                </el-input>
              </div>
            </div>

            <div>
              <label
                class="block text-sm font-medium text-gray-700 mb-1 text-left"
                for="confirmPassword"
              >
                确认新密码
              </label>
              <div class="relative">
                <el-input
                  v-model="forgotPasswordForm.confirmPassword"
                  :type="showConfirmPassword ? 'text' : 'password'"
                  class="login-input"
                  placeholder="请确认新密码"
                  size="large"
                >
                  <template #prefix>
                    <i class="fas fa-lock text-gray-400"></i>
                  </template>
                  <template #suffix>
                    <i
                      :class="showConfirmPassword ? 'fas fa-eye' : 'fas fa-eye-slash'"
                      class="text-gray-400 hover:text-gray-600 cursor-pointer transition-custom"
                      @click="toggleConfirmPassword"
                    ></i>
                  </template>
                </el-input>
              </div>
            </div>

            <el-button
              type="primary"
              size="large"
              class="w-full !bg-primary-500 !border-primary-500 hover:!bg-primary-600 hover:!border-primary-600"
              :loading="loading"
              @click="handleForgotPassword"
            >
              <span>重置密码</span>
              <i class="fas fa-key ml-2"></i>
            </el-button>
          </el-form>

          <!-- 成功提示 -->
          <div v-if="resetSuccess" class="text-center">
            <div
              class="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4"
            >
              <i class="fas fa-check text-2xl text-green-500"></i>
            </div>
            <h3 class="text-xl font-bold text-gray-900 mb-2">密码重置成功</h3>
            <p class="text-gray-600 mb-6">
              您的密码已成功重置，请使用新密码登录
            </p>
            <el-button
              type="primary"
              class="!bg-primary-500 !border-primary-500"
              @click="$router.push('/auth/login')"
            >
              前往登录
            </el-button>
          </div>

          <!-- 底部链接 -->
          <div v-if="!resetSuccess" class="mt-8 text-center">
            <p class="text-gray-600">
              想起密码了？
              <a
                href="javascript:void(0);"
                class="font-medium text-primary-500 hover:text-primary-600 transition-custom"
                @click="$router.push('/auth/login')"
              >
                返回登录
              </a>
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onUnmounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { authApi } from '@/api'

// 表单引用
const forgotPasswordFormRef = ref<FormInstance>()

// 加载状态
const loading = ref(false)
const sendCodeLoading = ref(false)
const resetSuccess = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)

// 验证码倒计时
const countdown = ref(0)
let countdownTimer: number | null = null

// 切换密码显示
const togglePassword = () => {
  showPassword.value = !showPassword.value
}

const toggleConfirmPassword = () => {
  showConfirmPassword.value = !showConfirmPassword.value
}

// 忘记密码表单数据
const forgotPasswordForm = reactive({
  username: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

// 自定义验证规则
const validateConfirmPassword = (_rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== forgotPasswordForm.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

// 表单验证规则
const forgotPasswordRules: FormRules = {
  username: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '请输入6位数字验证码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
    { pattern: /^(?=.*[a-zA-Z])(?=.*\d)/, message: '密码必须包含字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 开始倒计时
const startCountdown = () => {
  countdown.value = 60
  countdownTimer = window.setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(countdownTimer!)
      countdownTimer = null
    }
  }, 1000)
}

// 发送邮箱验证码
const sendEmailCode = async () => {
  if (!forgotPasswordForm.username) {
    ElMessage.error('请先输入邮箱地址')
    return
  }

  // 验证邮箱格式
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(forgotPasswordForm.username)) {
    ElMessage.error('请输入正确的邮箱地址')
    return
  }

  try {
    sendCodeLoading.value = true
    await authApi.sendEmailCode({
      username: forgotPasswordForm.username,
      type: 'RESET_PASSWORD'
    })
    ElMessage.success('验证码发送成功，请查收邮件')
    startCountdown()
  } catch (error: any) {
    console.error('发送验证码失败:', error)
    ElMessage.error(error.response?.data?.message || error.message || '发送验证码失败，请稍后重试')
  } finally {
    sendCodeLoading.value = false
  }
}

// 忘记密码处理
const handleForgotPassword = async () => {
  if (!forgotPasswordFormRef.value) return

  try {
    await forgotPasswordFormRef.value.validate()
    loading.value = true

    // 调用忘记密码API
    await authApi.forgotPassword({
      username: forgotPasswordForm.username,
      code: forgotPasswordForm.code,
      newPassword: forgotPasswordForm.newPassword,
      confirmPassword: forgotPasswordForm.confirmPassword
    })

    resetSuccess.value = true
    ElMessage.success('密码重置成功')
    
  } catch (error: any) {
    console.error('重置密码失败:', error)
    ElMessage.error(error.response?.data?.message || error.message || '重置密码失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 组件销毁时清理定时器
const cleanup = () => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

// 监听组件卸载
onUnmounted(cleanup)
</script>

<style scoped>
.transition-custom {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.text-shadow {
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.bg-gradient-primary {
  background: linear-gradient(135deg, #4f46e5 0%, #6366f1 100%);
}

.shadow-soft {
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
}

.text-dark {
  color: #1f2937;
}

:deep(.login-input .el-input__wrapper) {
  padding-left: 2.5rem;
  padding-right: 1rem;
  padding-top: 0.75rem;
  padding-bottom: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 0.5rem;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.login-input .el-input__wrapper:focus-within) {
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
}

:deep(.login-input .el-input__prefix) {
  position: absolute;
  left: 0.75rem;
  color: #9ca3af;
}

/* 发送验证码按钮样式 */
:deep(.send-code-btn) {
  height: 62px !important;
  padding: 0.75rem 1rem !important;
  border-radius: 0.5rem !important;
  border: 1px solid #d1d5db !important;
  background-color: #f9fafb !important;
  color: #374151 !important;
  font-weight: 500 !important;
  white-space: nowrap !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
}

:deep(.send-code-btn:hover:not(.is-disabled)) {
  background-color: #f3f4f6 !important;
  border-color: #9ca3af !important;
}

:deep(.send-code-btn.is-disabled) {
  background-color: #f3f4f6 !important;
  color: #9ca3af !important;
  cursor: not-allowed !important;
}

:deep(.el-button--primary) {
  background-color: #3b82f6;
  border-color: #3b82f6;
  font-weight: 500;
  padding: 0.75rem 1rem;
  border-radius: 0.5rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.el-button--primary:hover) {
  background-color: #2563eb;
  border-color: #2563eb;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
}

.primary-500 {
  color: #3b82f6;
}

.primary-600 {
  color: #2563eb;
}

/* 自定义主题色 */
:root {
  --el-color-primary: #3b82f6;
  --el-color-primary-light-3: #93c5fd;
  --el-color-primary-light-5: #dbeafe;
  --el-color-primary-light-7: #eff6ff;
  --el-color-primary-light-8: #f8fafc;
  --el-color-primary-light-9: #f1f5f9;
  --el-color-primary-dark-2: #2563eb;
}
</style>