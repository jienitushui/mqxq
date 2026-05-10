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
      class="m-auto max-w-500 min-w-345 rounded-8 auto-bg bg-opacity-20 bg-cover p-12 card-shadow"
    >
      <div class="w-full flex-col px-20 py-32">
        <h2 class="text-center text-28 text-#6a6a6a font-normal mb-24">
          重置密码
        </h2>

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
            @click="sendEmailCode"
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
            @click="handleForgotPassword"
          >
            重置密码
          </n-button>
        </div>

        <!-- 返回登录 -->
        <div class="mt-16 text-center">
          <n-button text type="primary" @click="router.push('/login')">
            返回登录
          </n-button>
        </div>
      </div>
    </div>

    <TheFooter class="py-12" />
  </div>
</template>

<script setup>
import api from './api'

const router = useRouter()

// 忘记密码表单数据
const forgotPasswordInfo = ref({
  email: '',
  emailCode: '',
  newPassword: '',
  confirmPassword: '',
})

// 邮箱验证码倒计时
const emailCodeCountdown = ref(0)

// 加载状态
const loading = ref(false)

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
    router.push('/login')
  } catch (error) {
    $message.destroy('forgotPassword')
    $message.error(error?.message || '密码重置失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>