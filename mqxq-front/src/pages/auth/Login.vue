<template>
  <div
    class="bg-gray-50 min-h-screen flex items-center justify-center p-4 font-sans"
    style="margin: auto; min-width: 100vw"
  >
    <!-- 登录卡片容器 -->
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
            让孩子在数字时代领先一步
          </h2>
          <p class="text-white/90 mb-8 max-w-md">
            培养未来科技人才，从编程启蒙开始。通过趣味互动课程，激发孩子的创造力和逻辑思维。
          </p>
          <div class="space-y-6">
            <div class="flex items-start">
              <div class="bg-white/20 p-3 rounded-full mr-4">
                <i class="fas fa-graduation-cap"></i>
              </div>
              <div class="text-left">
                <h3 class="font-semibold text-lg mb-1">专业课程体系</h3>
                <p class="text-white/80">
                  从Scratch图形化编程到Python代码编程，循序渐进培养编程思维
                </p>
              </div>
            </div>
            <div class="flex items-start">
              <div class="bg-white/20 p-3 rounded-full mr-4">
                <i class="fas fa-gamepad"></i>
              </div>
              <div class="text-left">
                <h3 class="font-semibold text-lg mb-1">趣味互动学习</h3>
                <p class="text-white/80">
                  通过制作小游戏、动画故事等项目，在玩中学会编程技能
                </p>
              </div>
            </div>
            <div class="flex items-start">
              <div class="bg-white/20 p-3 rounded-full mr-4">
                <i class="fas fa-trophy"></i>
              </div>
              <div class="text-left">
                <h3 class="font-semibold text-lg mb-1">智能学习跟踪</h3>
                <p class="text-white/80">
                  AI智能评估学习进度，个性化推荐内容，让每个孩子都能找到适合的节奏
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

      <!-- 右侧登录表单区 -->
      <div class="md:w-1/2 p-8 md:p-12 flex flex-col justify-center">
        <div class="max-w-md mx-auto w-full">
          <div class="text-center mb-10">
            <h2 class="text-[clamp(1.5rem,3vw,2rem)] font-bold text-dark mb-2">
              欢迎回来
            </h2>
            <p class="text-gray-600">请登录你的学生账号继续学习</p>
          </div>

          <!-- 登录表单 -->
          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            class="space-y-6"
            @submit.prevent="handleLogin"
          >
            <div>
              <label
                class="block text-sm font-medium text-gray-700 mb-1 text-left"
                for="username"
              >
                学生用户名
              </label>
              <div class="relative">
                <el-input
                  v-model="loginForm.username"
                  class="login-input"
                  placeholder="请输入学生ID"
                  size="large"
                >
                  <template #prefix>
                    <i class="fas fa-id-card text-gray-400"></i>
                  </template>
                </el-input>
              </div>
            </div>

            <div>
              <label
                class="block text-sm font-medium text-gray-700 mb-1 text-left"
                for="password"
              >
                密码
              </label>
              <div class="relative">
                <el-input
                  v-model="loginForm.password"
                  :type="showPassword ? 'text' : 'password'"
                  class="login-input"
                  placeholder="请输入密码"
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

            <!-- 验证码 -->
            <div>
              <label
                class="block text-sm font-medium text-gray-700 mb-1 text-left"
                for="captcha"
              >
                验证码
              </label>
              <div class="flex space-x-4">
                <div class="relative flex-1">
                  <el-input
                    v-model="loginForm.captcha"
                    class="login-input"
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
                    :src="captchaUrl"
                    alt="验证码"
                    class="w-full h-full object-cover rounded-lg border border-gray-300 cursor-pointer"
                    @click="refreshCaptcha"
                  />
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

            <div class="flex items-center">
              <el-checkbox
                v-model="loginForm.rememberMe"
                class="text-sm text-gray-700"
              >
                记住我
              </el-checkbox>
              <a
                href="javascript:void(0);"
                class="ml-auto text-sm text-primary-500 hover:text-primary-600 transition-custom"
                @click="$router.push('/auth/forgot-password')"
              >
                忘记密码?
              </a>
            </div>

            <el-button
              :loading="loading"
              type="primary"
              size="large"
              class="w-full !bg-primary-500 !border-primary-500 hover:!bg-primary-600 hover:!border-primary-600"
              @click="handleLogin"
            >
              <span>登录</span>
              <i class="fas fa-arrow-right ml-2"></i>
            </el-button>
          </el-form>

          <div class="mt-8 text-center">
            <p class="text-gray-600">
              还没有账号?
              <a
                href="javascript:void(0);"
                class="font-medium text-primary-500 hover:text-primary-600 transition-custom"
                @click="$router.push('/auth/register')"
              >
                注册
              </a>
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- 登录成功提示弹窗 -->
    <el-dialog
      v-model="showSuccessModal"
      :show-close="false"
      align-center
      width="400px"
    >
      <div class="text-center">
        <div
          class="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4"
        >
          <i class="fas fa-check text-2xl text-green-500"></i>
        </div>
        <h3 class="text-xl font-bold text-gray-900 mb-2">登录成功!</h3>
        <p class="text-gray-600 mb-6">
          欢迎回来，准备好开始今天的编程冒险了吗？
        </p>
        <el-button
          type="primary"
          class="!bg-primary-500 !border-primary-500"
          @click="closeSuccessModal"
        >
          进入学习平台
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import type { FormInstance, FormRules } from "element-plus";
import { useAuthStore } from "@/store";
import { authApi } from "@/api";

const router = useRouter();
const authStore = useAuthStore();

// 表单引用
const loginFormRef = ref<FormInstance>();

// 表单数据
const loginForm = reactive({
  username: "",
  password: "",
  captcha: "",
  captchaKey: "",
  rememberMe: false,
});

// 表单验证规则
const loginRules: FormRules = {
  username: [{ required: true, message: "请输入学生ID", trigger: "blur" }],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, message: "密码长度不能少于6位", trigger: "blur" },
  ],
  captcha: [{ required: true, message: "请输入验证码", trigger: "blur" }],
};

// 状态管理
const loading = ref(false);
const showSuccessModal = ref(false);
const captchaUrl = ref("");
const showPassword = ref(false);

// 切换密码显示
const togglePassword = () => {
  showPassword.value = !showPassword.value;
};

// 刷新验证码
const refreshCaptcha = async () => {
  try {
    const response = await authApi.getCaptcha();
    captchaUrl.value = response.captchaImage;
    loginForm.captchaKey = response.captchaKey;
    // 清空之前输入的验证码
    loginForm.captcha = "";
    console.log("验证码已更新:", captchaUrl.value.substring(0, 50) + "...");
  } catch (error) {
    console.error("获取验证码失败:", error);
    ElMessage.error("获取验证码失败，请重试");
  }
};

// 登录处理
const handleLogin = async () => {
  if (!loginFormRef.value) return;

  const valid = await loginFormRef.value.validate().catch(() => false);
  if (!valid) return;

  loading.value = true;

  try {
    const loginData = {
      username: loginForm.username,
      password: loginForm.password,
      captcha: loginForm.captcha,
      captchaKey: loginForm.captchaKey,
      role: "用户",
    };

    console.log("登录表单数据:", loginForm);
    console.log("发送的登录数据:", loginData);

    const response = await authApi.login(loginData);

    // 检查响应并保存用户信息到 store
    if (response.token) {
      const userInfo = {
        userId: response.userId,
        username: response.username,
        nickname: response.nickname,
        phone: response.phone,
        avatar: response.avatar,
        roles: response.roles
      };
      authStore.setAuth({
        token: response.token,
        user: userInfo,
        expiresIn: response.expiresIn
      });
      
      // 记住我功能
      if (loginForm.rememberMe) {
        localStorage.setItem("rememberedUsername", loginForm.username);
      } else {
        localStorage.removeItem("rememberedUsername");
      }

      ElMessage.success("登录成功！");
      showSuccessModal.value = true;
    } else {
      ElMessage.error("登录失败：未获取到有效令牌");
      loginForm.captcha = "";
      refreshCaptcha();
    }
  } catch (error: any) {
    console.error("登录失败:", error);
    ElMessage.error(
      error.response?.data?.message || error.message || "登录失败，请重试"
    );
    loginForm.captcha = "";
    refreshCaptcha();
  } finally {
    loading.value = false;
  }
};

// 关闭成功弹窗并跳转
const closeSuccessModal = () => {
  showSuccessModal.value = false;
  router.push("/");
};

// 页面初始化
onMounted(() => {
  // 获取验证码
  refreshCaptcha();

  // 检查是否有记住的用户名
  const rememberedUsername = localStorage.getItem("rememberedUsername");
  if (rememberedUsername) {
    loginForm.username = rememberedUsername;
    loginForm.rememberMe = true;
  }
});
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

:deep(.login-input .el-input__suffix) {
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

:deep(.el-checkbox__label) {
  color: #374151;
  font-size: 0.875rem;
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