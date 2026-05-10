<template>
  <div
    class="bg-gray-50 min-h-screen flex items-center justify-center p-4 font-sans"
    style="margin: auto; min-width: 100vw"
  >
    <!-- 注册卡片容器 -->
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
            加入我们的编程世界
          </h2>
          <p class="text-white/90 mb-8 max-w-md">
            开启孩子的编程之旅，培养未来科技人才。通过专业的课程体系和趣味互动，让学习变得更有趣。
          </p>
          <div class="space-y-6">
            <div class="flex items-start">
              <div class="bg-white/20 p-3 rounded-full mr-4">
                <i class="fas fa-user-plus"></i>
              </div>
              <div class="text-left">
                <h3 class="font-semibold text-lg mb-1">快速注册</h3>
                <p class="text-white/80">
                  简单几步即可创建账户，立即开始您的编程学习之旅
                </p>
              </div>
            </div>
            <div class="flex items-start">
              <div class="bg-white/20 p-3 rounded-full mr-4">
                <i class="fas fa-shield-alt"></i>
              </div>
              <div class="text-left">
                <h3 class="font-semibold text-lg mb-1">安全保障</h3>
                <p class="text-white/80">
                  多重验证机制，确保您的账户和学习数据安全可靠
                </p>
              </div>
            </div>
            <div class="flex items-start">
              <div class="bg-white/20 p-3 rounded-full mr-4">
                <i class="fas fa-rocket"></i>
              </div>
              <div class="text-left">
                <h3 class="font-semibold text-lg mb-1">个性化学习</h3>
                <p class="text-white/80">
                  根据学习进度和兴趣，为每个孩子定制专属的学习路径
                </p>
              </div>
            </div>
          </div>
        </div>
        <div class="mt-12">
          <img
            alt="孩子们一起编程"
            class="w-full h-48 object-cover rounded-lg shadow-lg"
            src="https://design.gemcoder.com/staticResource/echoAiSystemImages/c642a05dacad5beff45ef5befa42d5e3.png"
          />
        </div>
      </div>

      <!-- 右侧注册表单区 -->
      <div class="md:w-1/2 p-8 md:p-12 flex flex-col justify-center">
        <div class="max-w-md mx-auto w-full">
          <div class="text-center mb-10">
            <h2 class="text-[clamp(1.5rem,3vw,2rem)] font-bold text-dark mb-2">
              创建账户
            </h2>
            <p class="text-gray-600">填写信息，开始您的编程学习之旅</p>
          </div>

          <!-- 注册表单 -->
          <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            class="space-y-6"
            @submit.prevent="handleRegister"
          >

            <!-- 邮箱 -->
            <div>
              <label
                class="block text-sm font-medium text-gray-700 mb-1 text-left"
                for="email"
              >
                邮箱
              </label>
              <div class="relative">
                <el-input
                  v-model="registerForm.username"
                  class="register-input"
                  placeholder="请输入邮箱"
                  size="large"
                  @blur="checkEmail"
                >
                  <template #prefix>
                    <i class="fas fa-envelope text-gray-400"></i>
                  </template>
                </el-input>
              </div>
            </div>



            <!-- 密码 -->
            <div>
              <label
                class="block text-sm font-medium text-gray-700 mb-1 text-left"
                for="password"
              >
                密码
              </label>
              <div class="relative">
                <el-input
                  v-model="registerForm.password"
                  type="password"
                  class="register-input"
                  placeholder="请输入密码"
                  size="large"
                  show-password
                >
                  <template #prefix>
                    <i class="fas fa-lock text-gray-400"></i>
                  </template>
                </el-input>
              </div>
            </div>

            <!-- 确认密码 -->
            <div>
              <label
                class="block text-sm font-medium text-gray-700 mb-1 text-left"
                for="confirmPassword"
              >
                确认密码
              </label>
              <div class="relative">
                <el-input
                  v-model="registerForm.confirmPassword"
                  type="password"
                  class="register-input"
                  placeholder="请确认密码"
                  size="large"
                  show-password
                >
                  <template #prefix>
                    <i class="fas fa-lock text-gray-400"></i>
                  </template>
                </el-input>
              </div>
            </div>

            <!-- 图形验证码 -->
            <div>
              <label
                class="block text-sm font-medium text-gray-700 mb-1 text-left"
                for="captcha"
              >
                图形验证码
              </label>
              <div class="flex space-x-4">
                <div class="relative flex-1">
                  <el-input
                    v-model="registerForm.captcha"
                    class="register-input"
                    placeholder="请输入验证码"
                    size="large"
                  >
                    <template #prefix>
                      <i class="fas fa-shield-alt text-gray-400"></i>
                    </template>
                  </el-input>
                </div>
                <div class="relative w-32 h-14 flex-shrink-0" style="height: 64px;width: 165px;">
                  <img
                    v-if="captchaImage"
                    :src="captchaImage"
                    alt="验证码"
                    class="w-full h-full object-cover rounded-lg border border-gray-300 cursor-pointer"
                    @click="refreshCaptcha"
                  />
                  <div
                    v-else
                    class="w-full h-full flex items-center justify-center bg-gray-100 rounded-lg border border-gray-300 cursor-pointer text-xs text-gray-500"
                    @click="refreshCaptcha"
                  >
                    点击获取验证码
                  </div>
                  <button
                    type="button"
                    class="absolute inset-0 bg-black/20 flex items-center justify-center opacity-0 hover:opacity-100 transition-custom"
                    @click="refreshCaptcha"
                  >
                    <i class="fas fa-sync-alt text-white"></i>
                  </button>
                </div>
              </div>
            </div>

            <!-- 邮箱验证码 -->
            <div>
              <label
                class="block text-sm font-medium text-gray-700 mb-1 text-left"
                for="code"
              >
                邮箱验证码
              </label>
              <div class="flex space-x-4">
                <div class="relative flex-1">
                  <el-input
                    v-model="registerForm.code"
                    class="register-input"
                    placeholder="请输入邮箱验证码"
                    size="large"
                  >
                    <template #prefix>
                      <i class="fas fa-envelope-open text-gray-400"></i>
                    </template>
                  </el-input>
                </div>
                <el-button
                  :disabled="emailCodeDisabled || !registerForm.username"
                  :loading="emailCodeLoading"
                  class="flex-shrink-0 email-code-button"
                  size="large"
                  @click="sendEmailCode"
                >
                  {{ emailCodeText }}
                </el-button>
              </div>
            </div>

            <!-- 注册按钮 -->
            <el-button
              :loading="registerLoading"
              type="primary"
              size="large"
              class="w-full !bg-primary-500 !border-primary-500 hover:!bg-primary-600 hover:!border-primary-600"
              @click="handleRegister"
            >
              <span>创建账户</span>
              <i class="fas fa-user-plus ml-2"></i>
            </el-button>
          </el-form>

          <div class="mt-8 text-center">
            <p class="text-gray-600">
              已有账户?
              <a
                href="javascript:void(0);"
                class="font-medium text-primary-500 hover:text-primary-600 transition-custom"
                @click="$router.push('/auth/login')"
              >
                立即登录
              </a>
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #4f46e5 0%, #6366f1 100%);
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.register-card {
  width: 100%;
  max-width: 450px;
  background: white;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  backdrop-filter: blur(10px);
  animation: slideUp 0.6s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h2 {
  margin: 0 0 8px 0;
  color: #1f2937;
  font-size: clamp(1.5rem, 3vw, 2rem);
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.register-header p {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.register-form {
  margin-top: 20px;
}

.captcha-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.captcha-image {
  width: 120px;
  height: 40px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background: #f9fafb;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.captcha-image:hover {
  border-color: #3b82f6;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.captcha-placeholder {
  font-size: 12px;
  color: #6b7280;
  text-align: center;
  font-weight: 500;
}

.email-code-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.login-link {
  text-align: center;
  margin-top: 25px;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
  color: #6b7280;
  font-size: 14px;
}

.link {
  color: #3b82f6;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.link:hover {
  color: #2563eb;
  text-decoration: underline;
}

/* Element Plus 组件样式覆盖 */
:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-input__wrapper) {
  padding: 12px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background-color: #f9fafb;
  box-shadow: none;
}

:deep(.el-input__wrapper:hover) {
  border-color: #d1d5db;
  background-color: white;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #3b82f6;
  background-color: white;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

:deep(.el-input__inner) {
  font-size: 16px;
  color: #374151;
}

:deep(.el-input__inner::placeholder) {
  color: #9ca3af;
}

:deep(.el-select .el-input__wrapper) {
  cursor: pointer;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border: none;
  border-radius: 12px;
  padding: 15px 24px;
  font-size: 16px;
  font-weight: 600;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

:deep(.el-button--primary:active) {
  transform: translateY(0);
}

:deep(.el-button--primary.is-loading) {
  background: #6b7280;
  transform: none;
  box-shadow: none;
}

:deep(.el-button--default) {
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  padding: 8px 16px;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.el-button--default:hover) {
  border-color: #3b82f6;
  color: #3b82f6;
  background-color: #eff6ff;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .register-container {
    padding: 10px;
  }
  
  .register-card {
    padding: 30px 20px;
    border-radius: 16px;
  }
  
  .register-header h2 {
    font-size: 1.5rem;
  }
  
  .captcha-container,
  .email-code-container {
    flex-direction: column;
    gap: 8px;
  }
  
  .captcha-image {
    width: 100%;
    height: 50px;
  }
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

:deep(.register-input .el-input__wrapper) {
  padding-left: 2.5rem;
  padding-right: 1rem;
  padding-top: 0.75rem;
  padding-bottom: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 0.5rem;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.register-input .el-input__wrapper:focus-within) {
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
}

:deep(.register-input .el-input__prefix) {
  position: absolute;
  left: 0.75rem;
  color: #9ca3af;
}

:deep(.register-input .el-input__suffix) {
  position: absolute;
  right: 0.75rem;
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

:deep(.el-select .el-input__wrapper) {
  cursor: pointer;
}

.primary-500 {
  color: #3b82f6;
}

.primary-600 {
  color: #2563eb;
}

/* 邮箱验证码按钮样式 */
:deep(.email-code-button) {
  height: 62px !important;
  min-height: 48px !important;
  padding: 0 16px !important;
  border-radius: 0.5rem !important;
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

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElForm } from 'element-plus'
import { authApi } from '@/api/auth'
import type { RegisterRequest } from '@/api/types/auth'

const router = useRouter()
const registerFormRef = ref<InstanceType<typeof ElForm>>()

// 表单数据
const registerForm = reactive<RegisterRequest>({
  username: '',
  password: '',
  confirmPassword: '',
  captcha: '',
  captchaKey: '',
  code: '',
  userType: 'USER'
})

// 验证码相关
const captchaImage = ref('')
const emailCodeDisabled = ref(false)
const emailCodeLoading = ref(false)
const emailCodeText = ref('发送验证码')
const emailCodeCountdown = ref(0)

// 加载状态
const registerLoading = ref(false)

// 表单验证规则
const registerRules = {
  username: [
       { required: true, message: '请输入邮箱', trigger: 'blur' },
       { type: 'email' as const, message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 10, message: '昵称长度在 2 到 10 个字符', trigger: 'blur' }
  ],
  userType: [
    { required: true, message: '请选择用户类型', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: Function) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  captcha: [
    { required: true, message: '请输入图形验证码', trigger: 'blur' },
    { len: 4, message: '验证码长度为4位', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入邮箱验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度为6位', trigger: 'blur' }
  ]
}

// 获取图形验证码
const getCaptcha = async () => {
  try {
    const response = await authApi.getCaptcha()
    captchaImage.value = response.captchaImage
    registerForm.captchaKey = response.captchaKey
  } catch (error) {
    ElMessage.error('获取验证码失败')
  }
}

// 刷新图形验证码
const refreshCaptcha = () => {
  registerForm.captcha = ''
  getCaptcha()
}

// 检查用户名可用性
const checkUsername = async () => {
  if (!registerForm.username) return
  
  try {
    const isAvailable = await authApi.checkUsername(registerForm.username)
    if (!isAvailable) {
      ElMessage.warning('用户名已存在')
    }
  } catch (error) {
    console.error('检查用户名失败:', error)
  }
}

// 检查邮箱可用性
const checkEmail = async () => {
  if (!registerForm.username) return
  
  try {
    const isAvailable = await authApi.checkEmail(registerForm.username)
    if (!isAvailable) {
      ElMessage.warning('邮箱已被注册')
    }
  } catch (error) {
    console.error('检查邮箱失败:', error)
  }
}

// 发送邮箱验证码
const sendEmailCode = async () => {
  if (!registerForm.username) {
    ElMessage.warning('请先输入邮箱')
    return
  }

  emailCodeLoading.value = true
  
  try {
    await authApi.sendEmailCode({
      username: registerForm.username,
      type: 'REGISTER'
    })
    
    ElMessage.success('验证码发送成功')
    startCountdown()
  } catch (error) {
    ElMessage.error('验证码发送失败')
  } finally {
    emailCodeLoading.value = false
  }
}

// 开始倒计时
const startCountdown = () => {
  emailCodeDisabled.value = true
  emailCodeCountdown.value = 60
  
  const timer = setInterval(() => {
    emailCodeCountdown.value--
    emailCodeText.value = `${emailCodeCountdown.value}秒后重发`
    
    if (emailCodeCountdown.value <= 0) {
      clearInterval(timer)
      emailCodeDisabled.value = false
      emailCodeText.value = '发送验证码'
    }
  }, 1000)
}

// 处理注册
const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  try {
    await registerFormRef.value.validate()
    registerLoading.value = true
    
    await authApi.register(registerForm)
    
    ElMessage.success('注册成功！请登录')
    router.push('/auth/login')
  } catch (error: any) {
    if (error.fields) {
      // 表单验证错误
      return
    }
    ElMessage.error(error.message || '注册失败')
  } finally {
    registerLoading.value = false
  }
}

// 组件挂载时获取验证码
onMounted(() => {
  getCaptcha()
})
</script>
