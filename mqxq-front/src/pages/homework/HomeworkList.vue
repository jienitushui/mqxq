<template>
  <div class="homework-list-page">
    <!-- 导航栏 -->
    <Navbar />
    
    <!-- 页面头部 -->
    <div class="page-header bg-gradient-to-r from-blue-600 to-blue-800 text-white py-12">
      <div class="container mx-auto px-4">
        <h1 class="text-3xl font-bold mb-2">我的作业</h1>
        <p class="text-blue-100 text-lg">管理和查看您的课程作业</p>
      </div>
    </div>

    <!-- 页面内容 -->
    <div class="page-content py-6">
      <div class="container mx-auto px-4">




        <!-- 作业列表 -->
        <div class="homework-list">
          <div v-if="loading && homeworks.length === 0" class="py-12">
            <el-skeleton :rows="5" animated />
          </div>

          <div v-else-if="homeworks.length === 0" class="empty-state text-center py-12">
            <i class="fas fa-clipboard-list text-6xl text-gray-300 mb-4"></i>
            <h3 class="text-xl font-medium text-gray-500 mb-2">暂无作业</h3>
            <p class="text-gray-400">您还没有任何作业，请先加入课程</p>
          </div>

          <div v-else class="space-y-4">
            <div 
              v-for="homework in homeworks" 
              :key="homework.id"
              class="homework-card bg-white rounded-xl shadow-lg hover:shadow-xl transition-all duration-300 border border-blue-100 hover:border-blue-300"
            >
              <div class="p-6">
                <div class="flex items-start justify-between">
                  <div class="flex-1">
                    <!-- 作业标题和状态 -->
                    <div class="flex items-center gap-3 mb-3">
                      <h3 class="text-lg font-semibold text-gray-900">{{ homework.title }}</h3>
                      <el-tag 
                        :type="getStatusTagType(homework.status)" 
                        size="small"
                      >
                        {{ getStatusText(homework.status) }}
                      </el-tag>
                      <el-tag 
                        v-if="isOverdue(homework.deadline)" 
                        type="danger" 
                        size="small"
                      >
                        已过期
                      </el-tag>
                    </div>

                    <!-- 作业描述 -->
                    <!-- <p class="text-gray-600 mb-4 line-clamp-2 text-left">{{ homework.content || homework.description }}</p> -->

                    <!-- 作业信息 -->
                    <div class="flex flex-wrap gap-4 text-sm text-gray-500">
                      <div class="flex items-center gap-1">
                        <i class="fas fa-calendar-alt"></i>
                        <span>截止时间：{{ formatDate(homework.endTime || homework.deadline) }}</span>
                      </div>
                      <div class="flex items-center gap-1">
                        <i class="fas fa-star"></i>
                        <span>满分：{{ homework.score || homework.maxScore }}分</span>
                      </div>
                      <div class="flex items-center gap-1">
                        <i class="fas fa-clock"></i>
                        <span>发布时间：{{ formatDate(homework.createTime) }}</span>
                      </div>
                    </div>
                  </div>

                  <!-- 操作按钮 -->
                  <div class="flex flex-col gap-3 ml-6">
                    <el-button 
                      type="primary" 
                      size="default"
                      class="bg-blue-600 hover:bg-blue-700 border-blue-600 hover:border-blue-700"
                      @click="viewHomework(homework.id)"
                    >
                      <i class="fas fa-eye mr-2"></i>查看详情
                    </el-button>
                    
                    <!-- <el-button 
                      v-if="canSubmit(homework)" 
                      type="success" 
                      size="default"
                      class="bg-blue-500 hover:bg-blue-600 border-blue-500 hover:border-blue-600"
                      @click="submitHomework(homework.id)"
                    >
                      <i class="fas fa-upload mr-2"></i>提交作业
                    </el-button> -->
                    
                    <el-button 
                      v-if="hasSubmission(homework)" 
                      type="info" 
                      size="default"
                      class="bg-blue-400 hover:bg-blue-500 border-blue-400 hover:border-blue-500 text-white"
                      @click="viewSubmission(homework.id)"
                    >
                      <i class="fas fa-history mr-2"></i>查看提交
                    </el-button>
                  </div>
                </div>

                <!-- 附件信息 -->
                <div v-if="homework.attachmentUrl" class="mt-4 pt-4 border-t">
                  <div class="flex items-center gap-2 text-sm text-blue-600">
                    <i class="fas fa-paperclip"></i>
                    <a :href="homework.attachmentUrl" target="_blank" class="hover:underline">
                      作业附件
                    </a>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="total > 0" class="pagination-section mt-8">
          <Pagination
            :current="searchParams.page || 1"
            :total="total"
            :page-size="searchParams.size || 10"
            @change="handlePageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi } from '@/api'
import type { Homework, HomeworkSearchParams } from '@/api'
import Pagination from '@/components/Pagination.vue'
import Navbar from '@/components/Navbar.vue'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const homeworks = ref<Homework[]>([])
const myCourses = ref<any[]>([])
const total = ref(0)

// 搜索参数
const searchParams = reactive<HomeworkSearchParams>({
  page: 1,
  size: 10,
  status: undefined,
  courseId: undefined
})



// 加载作业列表
const loadHomeworks = async () => {
  try {
    loading.value = true
    const response = await userApi.getMyHomeworks(searchParams)
    homeworks.value = response.list || response.records || []
    total.value = response.total
  } catch (error) {
    ElMessage.error('加载作业列表失败')
  } finally {
    loading.value = false
  }
}

// 加载我的课程列表
const loadMyCourses = async () => {
  try {
    const response = await userApi.getMyCourseList({ page: 1, size: 100 })
    myCourses.value = response.list || response.records || []
  } catch (error) {
    console.error('加载课程列表失败:', error)
  }
}

// 刷新数据
const refreshData = () => {
  loadHomeworks()
  loadMyCourses()
}

// 分页处理
const handlePageChange = (page: number) => {
  searchParams.page = page
  loadHomeworks()
}

// 查看作业详情
const viewHomework = (id: number) => {
  router.push(`/homework/${id}`)
}

// 提交作业
// const submitHomework = (id: number) => {
//   router.push(`/homework/${id}/submit`)
// }

// 查看提交记录
const viewSubmission = (id: number) => {
  router.push(`/homework/submissions?homeworkId=${id}`)
}

// 工具函数
const getStatusText = (status: number) => {
  // 这里的status是作业的发布状态，不是提交状态
  // 作业状态：1-已发布
  const statusMap = {
    1: '已发布'
  }
  return statusMap[status] || '未发布'
}

const getStatusTagType = (status: number) => {
  // 作业发布状态的标签类型
  const typeMap = {
    1: 'success'  // 已发布 - 绿色
  }
  return typeMap[status] || 'info'
}

const isOverdue = (deadline: string) => {
  if (!deadline) return false
  return new Date(deadline) < new Date()
}

const canSubmit = (homework: Homework) => {
  // 作业状态为已发布且未过期时可以提交
  return homework.status === 1 && !isOverdue(homework.endTime || homework.deadline)
}

const hasSubmission = (homework: Homework) => {
  // 这里需要通过其他方式判断是否有提交，比如调用检查接口
  // 暂时返回 false，让用户点击查看详情来获取准确信息
  return false
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleString('zh-CN')
}

// 组件挂载
onMounted(() => {
  loadHomeworks()
  loadMyCourses()
})
</script>

<style scoped>
.homework-list-page {
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  min-height: 100vh;
}

.page-header {
  background: #2563eb;
  position: relative;
  overflow: hidden;
}

.page-content {
  background: transparent;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.homework-card {
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.homework-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 20px 40px rgba(59, 130, 246, 0.15);
}

.stat-card {
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.05) 0%, rgba(147, 197, 253, 0.05) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.stat-card:hover::before {
  opacity: 1;
}

.stat-card:hover {
  transform: translateY(-6px) scale(1.05);
  box-shadow: 0 15px 35px rgba(59, 130, 246, 0.2);
}

.empty-state {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 1rem;
  padding: 3rem;
  margin: 2rem 0;
}

.pagination-section {
  display: flex;
  justify-content: center;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-radius: 1rem;
  padding: 1.5rem;
}

/* 自定义滚动条 */
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: rgba(59, 130, 246, 0.3);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(59, 130, 246, 0.5);
}

/* 响应式优化 */
@media (max-width: 768px) {
  .homework-card:hover {
    transform: none;
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
  }
  
  .stat-card:hover {
    transform: translateY(-2px);
  }
}
</style>