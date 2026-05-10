<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 导航�?-->
    <Navbar />
    
    <!-- 页面头部 -->
    <div class="bg-white shadow-sm">
      <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="py-6 flex items-center">
          <button 
            @click="$router.back()"
            class="mr-4 text-gray-500 hover:text-gray-700"
          >
            <i class="fas fa-arrow-left text-xl"></i>
          </button>
          <h1 class="text-2xl font-bold text-gray-900">个人设置</h1>
        </div>
      </div>
    </div>

    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="bg-white rounded-lg shadow">
        <!-- 标签页导�?-->
        <div class="border-b border-gray-200">
          <nav class="-mb-px flex space-x-8 px-6">
            <button
              v-for="tab in tabs"
              :key="tab.key"
              @click="activeTab = tab.key"
              :class="[
                'py-4 px-1 border-b-2 font-medium text-sm',
                activeTab === tab.key
                  ? 'border-blue-500 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              ]"
            >
              <i :class="tab.icon + ' mr-2'"></i>
              {{ tab.label }}
            </button>
          </nav>
        </div>

        <!-- 标签页内�?-->
        <div class="p-6">
          <!-- 基本信息 -->
          <div v-if="activeTab === 'profile'" class="space-y-6">
            <div class="flex items-center space-x-6">
              <div class="relative">
                <img 
                  :src="form.avatar || '/default-avatar.png'" 
                  alt="头像"
                  class="w-20 h-20 rounded-full object-cover border-4 border-gray-200"
                >
                <el-upload
                  :action="uploadUrl"
                  :show-file-list="false"
                  :on-success="handleAvatarSuccess"
                  :before-upload="beforeAvatarUpload"
                  :headers="uploadHeaders"
                  class="absolute inset-0 flex items-center justify-center bg-black bg-opacity-50 rounded-full opacity-0 hover:opacity-100 transition-opacity cursor-pointer"
                >
                  <i class="fas fa-camera text-white"></i>
                </el-upload>
              </div>
              <div>
                <h3 class="text-lg font-medium text-gray-900">头像</h3>
                <p class="text-sm text-gray-500">点击头像可以更换</p>
              </div>
            </div>

            <el-form :model="form" :rules="rules" ref="profileFormRef" label-width="100px">
              <el-form-item label="用户名">
                <el-input v-model="form.username" disabled />
                <div class="text-xs text-gray-500 mt-1">用户名不可修改</div>
              </el-form-item>

              <el-form-item label="昵称" prop="nickname">
                <el-input 
                  v-model="form.nickname" 
                  placeholder="请输入昵称"
                  maxlength="20"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item label="手机号" prop="phone">
                <el-input 
                  v-model="form.phone" 
                  placeholder="请输入手机号"
                  maxlength="11"
                />
              </el-form-item>

              <el-form-item label="用户类型">
                <el-tag :type="userTypeTagType">{{ userTypeText }}</el-tag>
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="updateProfile" :loading="updating">
                  保存修改
                </el-button>
                <el-button @click="resetForm">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 安全设置 -->
          <div v-else-if="activeTab === 'security'" class="space-y-6">
            <div class="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
              <div class="flex">
                <i class="fas fa-exclamation-triangle text-yellow-400 mr-3 mt-0.5"></i>
                <div>
                  <h3 class="text-sm font-medium text-yellow-800">安全提示</h3>
                  <p class="text-sm text-yellow-700 mt-1">
                    为了您的账户安全，请定期更换密码，并确保密码强度足够。
                  </p>
                </div>
              </div>
            </div>

            <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="120px">
              <el-form-item label="当前密码" prop="oldPassword">
                <el-input 
                  v-model="passwordForm.oldPassword" 
                  type="password" 
                  placeholder="请输入当前密码"
                  show-password
                />
              </el-form-item>

              <el-form-item label="新密码" prop="newPassword">
                <el-input 
                  v-model="passwordForm.newPassword" 
                  type="password" 
                  placeholder="请输入新密码"
                  show-password
                />
                <div class="text-xs text-gray-500 mt-1">
                  密码长度至少6位，建议包含字母、数字和特殊字符
                </div>
              </el-form-item>

              <el-form-item label="确认新密码" prop="confirmPassword">
                <el-input 
                  v-model="passwordForm.confirmPassword" 
                  type="password" 
                  placeholder="请再次输入新密码"
                  show-password
                />
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="changePassword" :loading="changingPassword">
                  修改密码
                </el-button>
                <el-button @click="resetPasswordForm">重置</el-button>
              </el-form-item>
            </el-form>
          </div>


        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useAuthStore } from '@/store'
import { authApi, userApi, fileApi } from '@/api'
import type { UpdateProfileRequest, ChangePasswordRequest } from '@/api/types/auth'
import Navbar from '@/components/Navbar.vue'

const authStore = useAuthStore()

// 响应式数�?
const activeTab = ref('profile')
const updating = ref(false)
const changingPassword = ref(false)

const tabs = [
  { key: 'profile', label: '基本信息', icon: 'fas fa-user' },
  { key: 'security', label: '安全设置', icon: 'fas fa-shield-alt' }
]

// 表单数据
const form = reactive({
  username: '',
  nickname: '',
  phone: '',
  avatar: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单引用
const profileFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()

// 计算属�?
const userInfo = computed(() => authStore.userInfo)

const userTypeText = computed(() => {
  if (!userInfo.value || !userInfo.value.roles) return '用户'
  const roles = userInfo.value.roles
  if (roles.includes('教师') || roles.includes('teacher')) {
    return '教师'
  }
  if (roles.includes('学生') || roles.includes('student')) {
    return '学生'
  }
  return roles[0] || '用户'
})

const userTypeTagType = computed(() => {
  if (!userInfo.value || !userInfo.value.roles) return ''
  const roles = userInfo.value.roles
  if (roles.includes('教师') || roles.includes('teacher')) {
    return 'success'
  }
  return 'primary'
})

const uploadUrl = computed(() => 'http://localhost:9999/api/public/files/avatar')
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('mqxqtoken') || authStore.accessToken
  return {
    'Authorization': `Bearer ${token}`,
    'satoken': token,
    'token': token,
    'mqxqtoken': token
  }
})

// 表单验证规则
const rules: FormRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度为2到20个字符', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 方法
const initForm = () => {
  if (userInfo.value) {
    form.username = userInfo.value.username
    form.nickname = userInfo.value.nickname
    form.phone = userInfo.value.phone || ''
    form.avatar = userInfo.value.avatar || ''
  }
}

const resetForm = () => {
  initForm()
}

const resetPasswordForm = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.clearValidate()
}

const beforeAvatarUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handleAvatarSuccess = (response: any) => {
  console.log('上传响应:', response) // 添加日志便于调试
  if (response.code === 200 || response.code === '200') {
    form.avatar = response.data
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error('头像上传失败')
  }
}

const updateProfile = async () => {
  if (!profileFormRef.value) return

  try {
    await profileFormRef.value.validate()
  } catch {
    return
  }

  updating.value = true
  try {
    const updateData: UpdateProfileRequest = {
      name: form.nickname,
      phone: form.phone,
      avatar: form.avatar
    }

    await authApi.updateProfile(updateData)
    
    // 更新store中的用户信息
    await authStore.getUserInfo()

    
    ElMessage.success('个人信息更新成功')
  } catch (error) {
    console.error('更新个人信息失败:', error)
    ElMessage.error('更新个人信息失败')
  } finally {
    updating.value = false
  }
}

const changePassword = async () => {
  if (!passwordFormRef.value) return

  try {
    await passwordFormRef.value.validate()
  } catch {
    return
  }

  changingPassword.value = true
  try {
    const changeData: ChangePasswordRequest = {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
      confirmPassword: passwordForm.confirmPassword
    }

    await authApi.changePassword(changeData)
    ElMessage.success('密码修改成功，请重新登录')
    
    // 密码修改成功后需要重新登�?
    setTimeout(() => {
      authStore.logout()
    }, 1500)
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error('修改密码失败')
  } finally {
    changingPassword.value = false
  }
}



// 生命周期
onMounted(() => {
  initForm()
})
</script>

<style scoped>
.el-upload {
  border-radius: 50%;
}
</style>