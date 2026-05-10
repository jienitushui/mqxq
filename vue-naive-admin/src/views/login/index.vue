<!--------------------------------
 - @Author: Ronnie Zhang
 - @LastEditor: Ronnie Zhang
 - @LastEditTime: 2023/12/05 21:28:36
 - @Email: zclzone@outlook.com
 - Copyright © 2023 Ronnie Zhang(大脸怪) | https://isme.top
 --------------------------------->

<template>
  <div class="wh-full flex-col bg-[url(@/assets/images/login_bg.webp)] bg-cover">
    <div
      class="m-auto max-w-700 min-w-345 f-c-c rounded-12 auto-bg bg-opacity-20 bg-cover p-12 card-shadow transition-all hover:shadow-xl"
    >
      <!-- 左侧内容区域 -->
      <div class="hidden w-380 px-20 py-35 md:block rounded-l-12" style="background-color: #6A5BFF;">
        <h1 class="mb-16 text-28 text-white font-bold flex items-center">
          <i class="i-carbon:code mr-8"></i>码趣星球
        </h1>
        <ul class="text-white opacity-90 space-y-6 mt-8">
          <li class="flex items-center transform hover:translate-x-2 transition-transform">
            <i class="i-fe:check-circle mr-8 text-green-400 text-18" />
            <span class="text-16">专业教学平台</span>
          </li>
          <li class="flex items-center transform hover:translate-x-2 transition-transform">
            <i class="i-fe:check-circle mr-8 text-green-400 text-18" />
            <span class="text-16">学生进度追踪</span>
          </li>
          <li class="flex items-center transform hover:translate-x-2 transition-transform">
            <i class="i-fe:check-circle mr-8 text-green-400 text-18" />
            <span class="text-16">视频课程</span>
          </li>
        </ul>
        <img src="@/assets/images/login_banner.webp" class="mt-24 w-full rounded-8 shadow-md" alt="login_banner">
      </div>

      <!-- 右侧登录表单区域 -->
      <div class="w-320 flex-col px-20 py-28">
        <h2 class="f-c-c text-28 text-#6a6a6a font-medium">
          <img src="@/assets/images/logo.jpeg" class="mr-12 h-50 rounded-full shadow-sm">
          <span class="bg-gradient-to-r from-#6A5BFF to-#8A7BFF bg-clip-text text-transparent">{{ title }}</span>
        </h2>

        <!-- 身份选择标签 -->
        <div class="mb-24 mt-20 flex rounded-8 overflow-hidden bg-#f5f6f7 border border-#e5e6eb shadow-sm">
          <n-button
            class="flex-1 py-10 text-center transition-all text-16"
            :class="activeTab === '教师' ? 'bg-white text-#6A5BFF font-medium shadow-sm' : 'text-gray-600'"
            @click="activeTab = '教师'"
          >
            <i class="i-carbon:user-profile mr-4"></i>教师
          </n-button>
          <n-button
            class="flex-1 py-10 text-center transition-all text-16"
            :class="activeTab === '管理员' ? 'bg-white text-#6A5BFF font-medium shadow-sm' : 'text-gray-600'"
            @click="activeTab = '管理员'"
          >
            <i class="i-carbon:user-admin mr-4"></i>管理员
          </n-button>
        </div>

        <!-- 登录表单 -->
        <div v-if="currentView === 'login'">
          <!-- 用户名输入框 -->
          <n-input
            v-model:value="loginInfo.username"
            autofocus
            class="mt-32 h-40 items-center"
            placeholder="请输入用户名"
            :maxlength="20"
          >
            <template #prefix>
              <i class="i-fe:user mr-12 opacity-20" />
            </template>
          </n-input>

          <!-- 密码输入框 -->
          <n-input
            v-model:value="loginInfo.password"
            class="mt-20 h-40 items-center"
            type="password"
            show-password-on="mousedown"
            placeholder="请输入密码"
            :maxlength="20"
            @keydown.enter="handleLogin()"
          >
            <template #prefix>
              <i class="i-fe:lock mr-12 opacity-20" />
            </template>
          </n-input>

          <!-- 验证码输入框 -->
          <div class="mt-15 flex items-center">
            <n-input
              v-model:value="loginInfo.captcha"
              class="h-40 items-center"
              placeholder="请输入验证码"
              :maxlength="4"
              @keydown.enter="handleLogin()"
            >
              <template #prefix>
                <i class="i-fe:key mr-12 opacity-20" />
              </template>
            </n-input>
            <div class="ml-1 cursor-pointer rounded overflow-hidden flex items-center justify-center" style="width: 170px; height: 40px;" @click="getCaptchaImg">
              <img
                v-if="captchaImg"
                :src="captchaImg"
                alt="验证码"
                class="h-full object-cover"
              >
            </div>
          </div>

          <!-- 记住我选项 -->
          <div class="mt-20 flex justify-between">
            <n-checkbox
              :checked="isRemember"
              label="记住我"
              :on-update:checked="(val) => (isRemember = val)"
            />
            <!-- <n-button text type="primary" @click="currentView = 'forgotPassword'">
              忘记密码?
            </n-button> -->
          </div>

          <!-- 登录按钮 -->
          <div class="mt-20 flex items-center">
            <n-button
              class="h-48 flex-1 rounded-5 text-16"
              type="primary"
              style="background-color: #6A5BFF; color: white;"
              :loading="loading"
              @click="handleLogin()"
            >
              登录
            </n-button>
          </div>

          <!-- 底部说明 -->
          <div class="mt-16 text-center">
            <span class="text-gray-500">请联系管理员获取账号</span>
          </div>
        </div>



        <!-- 忘记密码表单 -->
        <div v-else-if="currentView === 'forgotPassword'">
          <h3 class="mb-16 text-center text-18 font-medium">重置密码</h3>
          
          <!-- 邮箱输入框 -->
          <n-input
            v-model:value="forgotPasswordInfo.email"
            class="mt-16 h-40 items-center"
            placeholder="请输入注册邮箱"
            :maxlength="50"
          >
            <template #prefix>
              <i class="i-fe:mail mr-12 opacity-20" />
            </template>
          </n-input>

          <!-- 邮箱验证码 -->
          <div class="mt-16 flex items-center">
            <n-input
              v-model:value="forgotPasswordInfo.emailCode"
              class="h-40 items-center"
              placeholder="请输入邮箱验证码"
              :maxlength="6"
            >
              <template #prefix>
                <i class="i-fe:key mr-12 opacity-20" />
              </template>
            </n-input>
            <n-button 
              class="ml-12 h-40 w-120" 
              :disabled="emailCodeCountdown > 0"
              @click="sendEmailCode()"
            >
              {{ emailCodeCountdown > 0 ? `${emailCodeCountdown}秒后重试` : '获取验证码' }}
            </n-button>
          </div>

          <!-- 新密码输入框 -->
          <n-input
            v-model:value="forgotPasswordInfo.newPassword"
            class="mt-16 h-40 items-center"
            type="password"
            show-password-on="mousedown"
            placeholder="请输入新密码"
            :maxlength="20"
          >
            <template #prefix>
              <i class="i-fe:lock mr-12 opacity-20" />
            </template>
          </n-input>

          <!-- 确认新密码输入框 -->
          <n-input
            v-model:value="forgotPasswordInfo.confirmPassword"
            class="mt-16 h-40 items-center"
            type="password"
            show-password-on="mousedown"
            placeholder="请确认新密码"
            :maxlength="20"
          >
            <template #prefix>
              <i class="i-fe:lock mr-12 opacity-20" />
            </template>
          </n-input>

          <!-- 重置密码按钮 -->
          <div class="mt-20 flex items-center">
            <n-button
              class="h-48 flex-1 rounded-5 text-16"
              type="primary"
              style="background-color: #6A5BFF; color: white;"
              :loading="loading"
              @click="handleForgotPassword()"
            >
              重置密码
            </n-button>
          </div>

          <!-- 返回登录 -->
          <div class="mt-16 text-center">
            <n-button text type="primary" @click="currentView = 'login'">
              返回登录
            </n-button>
          </div>
        </div>
      </div>
    </div>

    <TheFooter class="py-12" />
  </div>
</template>

<script setup>
import { useStorage } from '@vueuse/core'
import { useAuthStore, useUserStore } from '@/store'
import { lStorage } from '@/utils'
import api from './api'

const authStore = useAuthStore()
const userStore = useUserStore()
const router = useRouter()
const route = useRoute()
const title = import.meta.env.VITE_TITLE

// 当前视图（登录/忘记密码）
const currentView = ref('login')

// 当前选中的角色标签
const activeTab = ref('教师')

// 登录表单数据
const loginInfo = ref({
  username: 'teacher1',
  password: '123456',
  captcha: '',
  captchaKey: '',
})

// 监听角色切换，自动填充对应的默认账号密码
watch(activeTab, (newRole) => {
  if (newRole === '教师') {
    loginInfo.value.username = 'teacher1'
    loginInfo.value.password = '123456'
  } else if (newRole === '管理员') {
    loginInfo.value.username = 'admin'
    loginInfo.value.password = '123456'
  }
  // 清空验证码
  loginInfo.value.captcha = ''
})



// 忘记密码表单数据
const forgotPasswordInfo = ref({
  email: '',
  emailCode: '',
  newPassword: '',
  confirmPassword: '',
})

// 验证码图片
const captchaImg = ref('')

// 邮箱验证码倒计时
const emailCodeCountdown = ref(0)

// 记住我选项
const isRemember = useStorage('isRemember', true)

// 加载状态
const loading = ref(false)

// 获取图形验证码
async function getCaptchaImg() {
  try {
    const res = await api.getCaptcha()
    //console.log('验证码接口返回:', res)
    if (res?.code === '200') {
      // 现在拦截器已修改，应该可以直接从res.data获取数据
      const { captchaKey, captchaImage } = res.data || {}
      loginInfo.value.captchaKey = captchaKey || ''
      captchaImg.value = captchaImage || ''
    } else {
      $message.error(res?.message || '获取验证码失败')
    }
  } catch (e) {
    console.error('获取验证码出错:', e)
    $message.error('获取验证码失败')
  }
}

// 发送邮箱验证码
async function sendEmailCode() {
  const { email } = forgotPasswordInfo.value
  
  if (!email) {
    return $message.warning('请输入邮箱地址')
  }
  
  // 邮箱格式验证
  const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
  if (!emailRegex.test(email)) {
    return $message.warning('请输入正确的邮箱格式')
  }
  
  try {
    loading.value = true
    const res = await api.sendEmailCode({ email, type: 'RESET_PASSWORD' })
    if (res?.code === 200) {
      $message.success('验证码发送成功，请查收邮件')
      // 开始倒计时
      emailCodeCountdown.value = 60
      const timer = setInterval(() => {
        emailCodeCountdown.value--
        if (emailCodeCountdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    } else {
      $message.error(res?.message || '验证码发送失败')
    }
  } catch (error) {
    console.error(error)
    $message.error(error?.message || '验证码发送失败')
  } finally {
    loading.value = false
  }
}

// 处理登录
async function handleLogin() {
  const { username, password, captcha, captchaKey } = loginInfo.value
  
  if (!username || !password) {
    return $message.warning('请输入用户名和密码')
  }
  
  if (!captcha) {
    return $message.warning('请输入验证码')
  }
  
  try {
    loading.value = true
    $message.loading('正在验证，请稍后...', { key: 'login' })
    
    // 添加用户角色类型
    const role = activeTab.value
    
    const { data } = await api.login({ 
      username, 
      password: password.toString(), 
      captcha, 
      captchaKey,
      role // 添加用户角色类型
    })
    
    if (isRemember.value) {
      lStorage.set('loginInfo', { username, password, role })
    } else {
      lStorage.remove('loginInfo')
    }
    
    onLoginSuccess(data)
  } catch (error) {
    // 无论什么错误都刷新验证码
    getCaptchaImg()
    $message.destroy('login')
    // $message.error(error?.message || '登录失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}



// 处理忘记密码
async function handleForgotPassword() {
  const { 
    email, 
    emailCode, 
    newPassword, 
    confirmPassword 
  } = forgotPasswordInfo.value
  
  // 表单验证
  if (!email) return $message.warning('请输入邮箱')
  if (!emailCode) return $message.warning('请输入邮箱验证码')
  if (!newPassword) return $message.warning('请输入新密码')
  if (newPassword !== confirmPassword) return $message.warning('两次输入的密码不一致')
  
  try {
    loading.value = true
    $message.loading('正在重置密码，请稍后...', { key: 'forgotPassword' })
    
    await api.forgotPassword({ 
      email, 
      emailCode, 
      newPassword, 
      confirmPassword 
    })
    
    $message.success('密码重置成功，请登录', { key: 'forgotPassword' })
    currentView.value = 'login'
    
    // 清空忘记密码表单
    forgotPasswordInfo.value = {
      email: '',
      emailCode: '',
      newPassword: '',
      confirmPassword: '',
    }
  } catch (error) {
    $message.destroy('forgotPassword')
    $message.error(error?.message || '密码重置失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 登录成功处理
async function onLoginSuccess(data = {}) {
  const { token, expiresIn } = data
  
  // 存储token和用户信息
  authStore.setToken({ accessToken: token, expiresIn })
  userStore.setUser(data)
  //console.log('查看 userStore:', userStore.userInfo)
  //console.log('登录成功', data)
  $message.loading('登录中...', { key: 'login' })
  try {
    $message.success('登录成功', { key: 'login' })
    if (route.query.redirect) {
      const path = route.query.redirect
      delete route.query.redirect
      router.push({ path, query: route.query })
    } else {
      router.push('/')
    }
  } catch (error) {
    console.error(error)
    $message.destroy('login')
  }
}

// 初始化
onMounted(() => {
  // 获取验证码
  getCaptchaImg()
  
  // 如果有记住的登录信息，自动填充
  const localLoginInfo = lStorage.get('loginInfo')
  if (localLoginInfo) {
    loginInfo.value.username = localLoginInfo.username || ''
    loginInfo.value.password = localLoginInfo.password || ''
    if (localLoginInfo.role) {
      activeTab.value = localLoginInfo.role
    }
  }
})
</script>
