<template>
  <div
    class="bg-gray-50 min-h-screen flex items-center justify-center p-4 font-sans"
    style="margin: auto; min-width: 100vw"
  >
    <!-- 邮箱验证卡片容器 -->
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
            验证您的邮箱
          </h2>
          <p class="text-white/90 mb-8 max-w-md">
            为了确保账号安全和功能完整性，请验证您的邮箱地址，开启完整的学习体验。
          </p>
          <div class="space-y-6">
            <div class="flex items-start">
              <div class="bg-white/20 p-3 rounded-full mr-4">
                <i class="fas fa-envelope-open"></i>
              </div>
              <div class="text-left">
                <h3 class="font-semibold text-lg mb-1">邮件验证</h3>
                <p class="text-white/80">
                  检查您的邮箱，点击验证链接完成验证
                </p>
              </div>
            </div>
            <div class="flex items-start">
              <div class="bg-white/20 p-3 rounded-full mr-4">
                <i class="fas fa-bell"></i>
              </div>
              <div class="text-left">
                <h3 class="font-semibold text-lg mb-1">学习提醒</h3>
                <p class="text-white/80">
                  验证后可接收课程更新和学习进度提醒
                </p>
              </div>
            </div>
            <div class="flex items-start">
              <div class="bg-white/20 p-3 rounded-full mr-4">
                <i class="fas fa-unlock-alt"></i>
              </div>
              <div class="text-left">
                <h3 class="font-semibold text-lg mb-1">完整功能</h3>
                <p class="text-white/80">
                  验证后解锁所有学习功能和社区互动
                </p>
              </div>
            </div>
          </div>
        </div>
        <div class="mt-12">
          <img
            alt="邮箱验证"
            class="w-full h-48 object-cover rounded-lg shadow-lg"
            src="https://design.gemcoder.com/staticResource/echoAiSystemImages/c642a05dacad5beff45ef5befa42d5e3.png"
          />
        </div>
      </div>

      <!-- 右侧内容区 -->
      <div class="md:w-1/2 p-8 md:p-12 flex flex-col justify-center">
        <div class="max-w-md mx-auto w-full">
          <!-- 验证中状态 -->
          <div v-if="verifying" class="text-center">
            <div
              class="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4"
            >
              <i class="fas fa-spinner text-2xl text-blue-500 animate-spin"></i>
            </div>
            <h2 class="text-[clamp(1.5rem,3vw,2rem)] font-bold text-dark mb-2">正在验证邮箱</h2>
            <p class="text-gray-600">请稍候...</p>
          </div>

          <!-- 验证成功状态 -->
          <div v-else-if="verifySuccess" class="text-center">
            <div
              class="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4"
            >
              <i class="fas fa-check text-2xl text-green-500"></i>
            </div>
            <h2 class="text-[clamp(1.5rem,3vw,2rem)] font-bold text-dark mb-2">邮箱验证成功</h2>
            <p class="text-gray-600 mb-6">您的邮箱已成功验证，现在可以正常使用所有功能</p>
            <el-button
              type="primary"
              size="large"
              class="!bg-primary-500 !border-primary-500"
              @click="$router.push('/auth/login')"
            >
              前往登录
            </el-button>
          </div>

          <!-- 验证失败状态 -->
          <div v-else-if="verifyFailed" class="text-center">
            <div
              class="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4"
            >
              <i class="fas fa-times text-2xl text-red-500"></i>
            </div>
            <h2 class="text-[clamp(1.5rem,3vw,2rem)] font-bold text-dark mb-2">验证失败</h2>
            <p class="text-gray-600 mb-6">{{ errorMessage }}</p>
            <div class="space-y-3">
              <el-button
                type="primary"
                size="large"
                class="w-full !bg-primary-500 !border-primary-500"
                :loading="resendLoading"
                @click="resendVerificationEmail"
              >
                重新发送验证邮件
              </el-button>
              <el-button
                size="large"
                class="w-full"
                @click="$router.push('/auth/login')"
              >
                返回登录
              </el-button>
            </div>
          </div>

          <!-- 等待验证状态 -->
          <div v-else class="text-center">
            <div
              class="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4"
            >
              <i class="fas fa-envelope text-2xl text-blue-500"></i>
            </div>
            <h2 class="text-[clamp(1.5rem,3vw,2rem)] font-bold text-dark mb-2">验证您的邮箱</h2>
            <p class="text-gray-600 mb-4">
              我们已向 <strong>{{ email }}</strong> 发送了验证邮件
            </p>
            <p class="text-sm text-gray-500 mb-6">
              请检查您的邮箱（包括垃圾邮件文件夹），并点击验证链接完成邮箱验证
            </p>
            
            <div class="space-y-3 mb-6">
              <el-button
                type="primary"
                size="large"
                class="w-full !bg-primary-500 !border-primary-500"
                :loading="resendLoading"
                @click="resendVerificationEmail"
              >
                重新发送验证邮件
              </el-button>
              <el-button
                size="large"
                class="w-full"
                :loading="checkLoading"
                @click="checkVerificationStatus"
              >
                我已验证
              </el-button>
            </div>

            <div class="mt-8 text-center">
              <p class="text-gray-600">
                邮箱地址错误？
                <a
                  href="javascript:void(0);"
                  class="font-medium text-primary-500 hover:text-primary-600 transition-custom"
                  @click="$router.push('/auth/register')"
                >
                  重新注册
                </a>
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api'

const router = useRouter()
const route = useRoute()

// 状态管理
const verifying = ref(false)
const verifySuccess = ref(false)
const verifyFailed = ref(false)
const resendLoading = ref(false)
const checkLoading = ref(false)

// 数据
const email = ref('')
const errorMessage = ref('')

// 验证邮箱
const verifyEmail = async (token: string) => {
  verifying.value = true
  
  try {
    await authApi.verifyEmail(token)
    verifySuccess.value = true
    ElMessage.success('邮箱验证成功')
  } catch (error: any) {
    console.error('邮箱验证失败:', error)
    verifyFailed.value = true
    errorMessage.value = error.message || '验证链接已过期或无效'
  } finally {
    verifying.value = false
  }
}

// 重新发送验证邮件
const resendVerificationEmail = async () => {
  if (!email.value) {
    ElMessage.error('邮箱地址不能为空')
    return
  }

  resendLoading.value = true
  
  try {
    await authApi.resendVerificationEmail(email.value)
    ElMessage.success('验证邮件已重新发送')
  } catch (error: any) {
    console.error('重新发送验证邮件失败:', error)
    ElMessage.error(error.message || '发送失败，请稍后重试')
  } finally {
    resendLoading.value = false
  }
}

// 检查验证状态
const checkVerificationStatus = async () => {
  if (!email.value) {
    ElMessage.error('邮箱地址不能为空')
    return
  }

  checkLoading.value = true
  
  try {
    const result = await authApi.checkEmailVerificationStatus(email.value)
    if (result.verified) {
      verifySuccess.value = true
      ElMessage.success('邮箱验证成功')
    } else {
      ElMessage.warning('邮箱尚未验证，请检查邮件并点击验证链接')
    }
  } catch (error: any) {
    console.error('检查验证状态失败:', error)
    ElMessage.error(error.message || '检查失败，请稍后重试')
  } finally {
    checkLoading.value = false
  }
}

// 生命周期
onMounted(() => {
  const token = route.query.token as string
  email.value = (route.query.email as string) || ''

  if (token) {
    // 如果有验证令牌，直接进行验证
    verifyEmail(token)
  } else if (!email.value) {
    // 如果没有邮箱参数，跳转到注册页面
    ElMessage.error('缺少必要参数')
    router.push('/auth/register')
  }
})
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

.animate-spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
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