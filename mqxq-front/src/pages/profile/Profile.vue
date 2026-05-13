<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 导航栏 -->
    <Navbar />
    
    <!-- 页面头部 -->
    <div class="bg-white shadow-sm">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="py-6 flex items-center">
          <router-link
            to="/"
            class="text-gray-600 hover:text-gray-800 mr-4 flex items-center"
          >
            <i class="fas fa-arrow-left text-xl">
            </i>
          </router-link>
          <h1 class="text-2xl font-bold text-gray-900">个人设置</h1>
        </div>
      </div>
    </div>

    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- 左侧用户信息卡片 -->
        <div class="lg:col-span-1">
          <div class="bg-white rounded-lg shadow p-6">
            <div class="text-center">
              <!-- 头像 -->
              <div class="relative inline-block">
                <img
                  :src="userInfo?.avatar || '/default-avatar.png'"
                  :alt="userInfo?.nickname"
                  class="w-24 h-24 rounded-full object-cover border-4 border-white shadow-lg"
                />
                <button
                  @click="showAvatarUpload = true"
                  class="absolute bottom-0 right-0 bg-blue-500 text-white rounded-full p-2 hover:bg-blue-600 transition-colors"
                >
                  <i class="fas fa-camera text-sm"></i>
                </button>
              </div>

              <!-- 用户基本信息 -->
              <h2 class="mt-4 text-xl font-semibold text-gray-900">
                {{ userInfo?.nickname }}
              </h2>
              <p class="text-gray-500">@{{ userInfo?.username }}</p>
              <div class="mt-2">
                <span
                  class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                  :class="userTypeClass"
                >
                  {{ userTypeText }}
                </span>
              </div>

              <!-- 统计信息 -->
              <!-- <div class="mt-6 grid grid-cols-2 gap-4">
                <div class="text-center">
                  <div class="text-2xl font-bold text-blue-600">{{ statistics.courseCount || 0 }}</div>
                  <div class="text-sm text-gray-500">学习课程</div>
                </div>
                <div class="text-center">
                  <div class="text-2xl font-bold text-green-600">{{ statistics.studyHours || 0 }}</div>
                  <div class="text-sm text-gray-500">学习时长(小时)</div>
                </div>
              </div> -->

              <!-- 操作按钮 -->
              <div class="mt-6 space-y-3">
                <router-link
                  to="/profile/settings"
                  class="w-full bg-blue-500 text-white py-2 px-4 rounded-lg hover:bg-blue-600 transition-colors inline-block"
                >
                  编辑资料
                </router-link>
                <button
                  @click="handleLogout"
                  class="w-full bg-gray-500 text-white py-2 px-4 rounded-lg hover:bg-gray-600 transition-colors"
                >
                  退出登录
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧内容区域 -->
        <div class="lg:col-span-2 space-y-6">
          <!-- 学习统计 -->
          <div class="bg-white rounded-lg shadow p-6">
            <h3 class="text-lg font-semibold text-gray-900 mb-4">学习统计</h3>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div class="bg-blue-50 rounded-lg p-4">
                <div class="flex items-center">
                  <i class="fas fa-book text-blue-500 text-xl mr-3"></i>
                  <div>
                    <div class="text-2xl font-bold text-blue-600">
                      {{ statistics.totalCourses || 0 }}
                    </div>
                    <div class="text-sm text-gray-600">总课程数</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 快捷操作 -->
          <div class="bg-white rounded-lg shadow p-6">
            <h3 class="text-lg font-semibold text-gray-900 mb-4">快捷操作</h3>
            <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
              <router-link
                to="/learning/courses"
                class="flex flex-col items-center p-4 bg-blue-50 rounded-lg hover:bg-blue-100 transition-colors"
              >
                <i class="fas fa-book text-blue-500 text-2xl mb-2"></i>
                <span class="text-sm font-medium text-gray-700">我的课程</span>
              </router-link>
              
              <router-link
                to="/learning/history"
                class="flex flex-col items-center p-4 bg-green-50 rounded-lg hover:bg-green-100 transition-colors"
              >
                <i class="fas fa-history text-green-500 text-2xl mb-2"></i>
                <span class="text-sm font-medium text-gray-700">浏览记录</span>
              </router-link>
              
              <router-link
                to="/homework"
                class="flex flex-col items-center p-4 bg-yellow-50 rounded-lg hover:bg-yellow-100 transition-colors"
              >
                <i class="fas fa-tasks text-yellow-500 text-2xl mb-2"></i>
                <span class="text-sm font-medium text-gray-700">我的作业</span>
              </router-link>
              
              <router-link
                to="/orders"
                class="flex flex-col items-center p-4 bg-purple-50 rounded-lg hover:bg-purple-100 transition-colors"
              >
                <i class="fas fa-shopping-cart text-purple-500 text-2xl mb-2"></i>
                <span class="text-sm font-medium text-gray-700">我的订单</span>
              </router-link>
            </div>
          </div>

          <!-- 我的课程 -->
          <div class="bg-white rounded-lg shadow p-6">
            <div class="flex justify-between items-center mb-4">
              <h3 class="text-lg font-semibold text-gray-900">我的课程</h3>
              <router-link
                to="/learning/courses"
                class="text-blue-500 hover:text-blue-600 text-sm"
              >
                查看全部 →
              </router-link>
            </div>

            <div v-if="loading" class="text-center py-8">
              <i class="fas fa-spinner fa-spin text-gray-400 text-2xl"></i>
              <p class="text-gray-500 mt-2">加载中...</p>
            </div>

            <div v-else-if="myCourses.length === 0" class="text-center py-8">
              <i class="fas fa-book-open text-gray-300 text-4xl"></i>
              <p class="text-gray-500 mt-2">暂无课程</p>
              <router-link
                to="/courses"
                class="mt-4 inline-block bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-600 transition-colors"
              >
                去选课
              </router-link>
            </div>

            <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div
                v-for="course in myCourses"
                :key="course.id"
                class="border rounded-lg p-4 hover:shadow-md transition-shadow cursor-pointer"
                @click="goToCourse(course.id)"
              >
                <div class="flex items-start space-x-3">
                  <img
                    :src="course.cover || '/default-course.png'"
                    :alt="course.title"
                    class="w-16 h-16 rounded-lg object-cover"
                  />
                  <div class="flex-1 min-w-0">
                    <h4 class="font-medium text-gray-900 truncate">
                      {{ course.title || "课程标题" }}
                    </h4>
                    <p class="text-sm text-gray-500 mt-1">
                      {{ course.description || "暂无描述" }}
                    </p>
                    <div class="mt-2 flex items-center justify-between">
                      <!-- <span
                        class="text-xs px-2 py-1 rounded-full"
                        :class="
                          course.enrollStatus === 0
                            ? 'bg-green-100 text-green-800'
                            : 'bg-gray-100 text-gray-800'
                        "
                      >
                        {{ course.status === 1 ? "已加入" : "未知状态" }}
                      </span> -->
                      <span class="text-xs text-gray-400">
                        发布时间：{{ formatTime(course.publishTime) }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 头像上传弹窗 -->
    <el-dialog v-model="showAvatarUpload" title="更换头像" width="400px">
      <div class="text-center">
        <el-upload
          class="avatar-uploader"
          :action="uploadUrl"
          :show-file-list="false"
          :on-success="handleAvatarSuccess"
          :before-upload="beforeAvatarUpload"
          :headers="uploadHeaders"
        >
          <img
            v-if="newAvatar"
            :src="newAvatar"
            class="avatar w-32 h-32 rounded-full object-cover mx-auto"
          />
          <el-icon v-else class="avatar-uploader-icon text-6xl text-gray-400">
            <Plus />
          </el-icon>
        </el-upload>
        <p class="text-sm text-gray-500 mt-2">点击上传新头像</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAvatarUpload = false">取消</el-button>
          <el-button type="primary" @click="updateAvatar" :loading="uploading"
            >确定</el-button
          >
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { Plus } from "@element-plus/icons-vue";
import { useAuthStore } from "@/store";
import { authApi,userApi, fileApi } from "@/api";
import { publicApi } from '../../api/public'
import { getCourseDetail } from '../../utils/courseApi'
import type { UserInfo } from "@/api/types/auth";
import Navbar from '@/components/Navbar.vue';

const router = useRouter();
const authStore = useAuthStore();

// 响应式数据
const loading = ref(false);
const showAvatarUpload = ref(false);
const uploading = ref(false);
const newAvatar = ref("");
const statistics = ref({
  courseCount: 0,
  studyHours: 0,
  totalCourses: 0,
  completedCourses: 0,
  inProgressCourses: 0,
});
const myCourses = ref([]);
const recentViews = ref([]);

// 计算属性
const userInfo = computed(() => authStore.userInfo);

const userTypeClass = computed(() => {
  if (!userInfo.value || !userInfo.value.roles)
    return "bg-gray-100 text-gray-800";
  const roles = userInfo.value.roles;
  if (roles.includes("教师") || roles.includes("teacher")) {
    return "bg-green-100 text-green-800";
  }
  return "bg-blue-100 text-blue-800";
});

const userTypeText = computed(() => {
  if (!userInfo.value || !userInfo.value.roles) return "用户";
  const roles = userInfo.value.roles;
  if (roles.includes("教师") || roles.includes("teacher")) {
    return "教师";
  }
  if (roles.includes("学生") || roles.includes("student")) {
    return "学生";
  }
  return roles[0] || "用户";
});

const uploadUrl = computed(
  () => "http://localhost:9999/api/public/files/avatar"
  // () => "http://159.75.11.181:9999/api/public/files/avatar"
);
const uploadHeaders = computed(() => {
  const token = localStorage.getItem("mqxqtoken") || authStore.accessToken;
  return {
    Authorization: `Bearer ${token}`,
    satoken: token,
    token: token,
    mqxqtoken: token,
  };
});

// 根据课程ID列表获取课程详情
const getCourseDetailsByIds = async (userCourses: any[]) => {
  try {
    const courseDetails = [];

    // 为每个courseId获取课程详情
    for (const userCourse of userCourses) {
      // 使用智能接口获取课程详情
      const courseRes = await getCourseDetail(userCourse.courseId);
      console.log("课程详情:", courseRes);
      // 合并课程信息和用户课程关系信息
      courseDetails.push({
        ...courseRes,
        myCourses : userCourse,
      });
      console.log("合并后的课程详情:", courseDetails);
    }
    myCourses.value = courseDetails;
    console.log("我的课程详情:", myCourses.value);
  } catch (error) {
    console.error("获取课程详情失败:", error);
    myCourses.value = [];
  }
};

// 方法
const loadData = async () => {
  loading.value = true;
  try {
    // 并行加载数据
    const [statsRes, coursesRes, viewsRes] = await Promise.all([
      userApi.getStudyStatistics().catch(() => ({})),
      userApi
        .getMyCourseList({ page: 1, size: 4 })
        .catch(() => ({ records: [] })),
      userApi
        .getViewHistory({ page: 1, size: 5 })
        .catch(() => ({ records: [] })),
    ]);

    statistics.value = {
      courseCount: statsRes.courseCount || 0,
      studyHours: Math.floor((statsRes.totalStudyTime || 0) / 3600),
      totalCourses: statsRes.totalCourses || 0,
      completedCourses: statsRes.completedCourses || 0,
      inProgressCourses: statsRes.inProgressCourses || 0,
    };

    // 处理我的课程数据 - 根据courseId获取课程详情
    const userCourses = ('list' in coursesRes ? coursesRes.list : coursesRes.records) || [];
    console.log("用户课程关系:", userCourses);

    if (userCourses.length > 0) {
      await getCourseDetailsByIds(userCourses);
    } else {
      myCourses.value = [];
    }

    recentViews.value = ('list' in viewsRes ? viewsRes.list : viewsRes.records) || [];
  } catch (error) {
    console.error("加载数据失败:", error);
  } finally {
    loading.value = false;
  }
};

const goToCourse = (courseId: number) => {
  router.push(`/courses/${courseId}`);
};

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm("确定要退出登录吗？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });

    await authStore.logout();
    ElMessage.success("退出登录成功");
    router.push("/auth/login");
  } catch (error) {
    if (error !== "cancel") {
      console.error("退出登录失败:", error);
    }
  }
};

const beforeAvatarUpload = (file: File) => {
  const isImage = file.type.startsWith("image/");
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isImage) {
    ElMessage.error("只能上传图片文件!");
    return false;
  }
  if (!isLt2M) {
    ElMessage.error("图片大小不能超过 2MB!");
    return false;
  }
  return true;
};

const handleAvatarSuccess = (response: any) => {
  console.log("上传响应:", response); // 添加日志便于调试
  if (response.code === 200 || response.code === "200") {
    newAvatar.value = response.data;
    ElMessage.success("头像上传成功");
  } else {
    ElMessage.error("上传失败");
  }
};

const updateAvatar = async () => {
  if (!newAvatar.value) {
    ElMessage.warning("请先上传头像");
    return;
  }
  uploading.value = true;
  try {
    await authApi.updateProfile({ avatar: newAvatar.value })
    await authStore.updateProfile({ avatar: newAvatar.value });
    
    ElMessage.success("头像更新成功");
    showAvatarUpload.value = false;
    newAvatar.value = "";
  } catch (error) {
    console.error("更新头像失败:", error);
    ElMessage.error("更新头像失败");
  } finally {
    uploading.value = false;
  }
};

const formatTime = (time: string) => {
  const date = new Date(time);
  const now = new Date();
  const diff = now.getTime() - date.getTime();

  if (diff < 60000) return "刚刚";
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`;
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`;
  return `${Math.floor(diff / 86400000)}天前`;
};

// 生命周期
onMounted(() => {
  loadData();
});
</script>

<style scoped>
.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: 0.2s;
  width: 128px;
  height: 128px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-uploader .el-upload:hover {
  border-color: #409eff;
}
</style>