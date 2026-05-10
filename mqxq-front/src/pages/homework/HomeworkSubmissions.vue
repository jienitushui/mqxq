<template>
  <div class="homework-submissions-page">
    <!-- 导航栏 -->
    <Navbar />
    
    <!-- 页面头部 -->
    <div class="page-header bg-gradient-to-r from-blue-700 to-blue-900 text-white py-12">
      <div class="container mx-auto px-4">
        <div class="flex items-center gap-4 mb-4">
          <el-button 
            @click.stop="goBack" 
            type="primary" 
            plain 
            size="small"
            class="text-white border-white hover:bg-white hover:text-blue-700"
            style="z-index: 10; position: relative;"
          >
            <i class="fas fa-arrow-left mr-2"></i>返回
          </el-button>
          <div>
            <h1 class="text-3xl font-bold mb-2">提交记录</h1>
            <p class="text-blue-100 text-lg">查看我的作业提交历史</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 页面内容 -->
    <div class="page-content py-6">
      <div class="container mx-auto px-4">




        <!-- 提交记录列表 -->
        <div class="submissions-list">
          <div v-if="loading && submissions.length === 0" class="text-center py-12">
            <el-skeleton :rows="5" animated />
          </div>

          <div v-else-if="submissions.length === 0" class="empty-state text-center py-12">
            <i class="fas fa-file-alt text-6xl text-gray-300 mb-4"></i>
            <h3 class="text-xl font-medium text-gray-500 mb-2">暂无提交记录</h3>
            <p class="text-gray-400">您还没有提交过任何作业</p>
          </div>

          <div v-else class="space-y-4">
            <div 
              v-for="submission in submissions" 
              :key="submission.id"
              class="submission-card bg-white rounded-lg shadow-sm border hover:shadow-md transition-shadow"
            >
              <div class="p-6">
                <div class="flex items-start justify-between mb-4">
                  <div class="flex-1">
                    <!-- 作业标题和状态 -->
                    <div class="flex items-center gap-3 mb-3">
                      <h3 class="text-lg font-semibold text-gray-900">
                        {{ submission.homeworkTitle || `作业 #${submission.homeworkId}` }}
                      </h3>
                      <el-tag 
                        :type="getStatusTagType(submission.status)" 
                        size="small"
                      >
                        {{ getStatusText(submission.status) }}
                      </el-tag>
                    </div>

                    <!-- 提交信息 -->
                    <div class="flex flex-wrap gap-4 text-sm text-gray-500 mb-3">
                      <div class="flex items-center gap-1">
                        <i class="fas fa-clock"></i>
                        <span>提交时间：{{ formatDate(submission.submitTime) }}</span>
                      </div>
                      <div v-if="submission.gradeTime" class="flex items-center gap-1">
                        <i class="fas fa-check-circle"></i>
                        <span>批改时间：{{ formatDate(submission.gradeTime) }}</span>
                      </div>
                      <div class="flex items-center gap-1">
                        <i class="fas fa-user"></i>
                        <span>学生：{{ submission.studentName || '我' }}</span>
                      </div>
                    </div>

                    <!-- 提交内容预览 -->
                    <div class="content-preview bg-gray-50 rounded-lg p-4 mb-4">
                      <p class="text-gray-700 line-clamp-3 text-left">{{ submission.content }}</p>
                    </div>

                    <!-- 附件信息 -->
                    <div v-if="submission.attachmentUrl" class="attachment-info mb-3">
                      <div class="flex items-center gap-3">
                        <div class="flex items-center gap-2 text-blue-700">
                          <i class="fas fa-paperclip"></i>
                          <a :href="submission.attachmentUrl" target="_blank" class="hover:underline">
                            下载附件
                          </a>
                        </div>
                        <el-button 
                          type="primary" 
                          size="small" 
                          plain
                          @click="previewFile(submission.attachmentUrl)"
                        >
                          <i class="fas fa-eye mr-1"></i>预览
                        </el-button>
                      </div>
                    </div>
                  </div>

                  <!-- 分数和操作 -->
                  <div class="flex flex-col items-end gap-3 ml-4">
                    <!-- 分数显示 -->
                    <div class="text-right">
                      <div v-if="submission.score !== undefined" class="score-display">
                        <div :class="getScoreColor(submission.score, submission.maxScore)">
                          {{ submission.score === null ? `未评分` : `${submission.score}` }} / {{ submission.maxScore }}
                        </div>
                        <!-- <div class="text-sm text-gray-500">/ {{ submission.maxScore }}</div> -->
                      </div>
                      <div v-else class="text-sm text-gray-500">
                        待批改
                      </div>
                    </div>

                    <!-- 操作按钮 -->
                    <div class="flex flex-col gap-2">
                      <el-button 
                        type="primary" 
                        size="small"
                        @click="viewSubmissionDetail(submission)"
                      >
                        查看详情
                      </el-button>
                      
                      <el-button 
                        type="success" 
                        size="small"
                        @click="viewHomework(submission.homeworkId)"
                        style="margin-left: 0px;"
                      >
                        查看作业
                      </el-button>
                      
                      <el-button 
                        v-if="canWithdraw(submission)" 
                        type="danger" 
                        size="small"
                        @click="withdrawSubmission(submission)"
                        style="margin-left: 0px;"
                      >
                        撤回提交
                      </el-button>
                    </div>
                  </div>
                </div>

                <!-- 教师评语 -->
                <div v-if="submission.teacherComment" class="teacher-comment mt-4 pt-4 border-t">
                  <div class="bg-yellow-50 border-l-4 border-yellow-400 p-4 rounded-r-lg">
                    <div class="flex items-start gap-3">
                      <i class="fas fa-comment-alt text-yellow-600 mt-1"></i>
                      <div class="flex-1">
                        <h4 class="font-medium text-yellow-800 mb-2">教师评语</h4>
                        <p class="text-yellow-700 text-left">{{ submission.teacherComment }}</p>
                      </div>
                    </div>
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

    <!-- 提交详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="提交详情"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-if="selectedSubmission" class="submission-detail">
        <!-- 基本信息 -->
        <div class="detail-section mb-6">
          <h3 class="text-lg font-semibold text-gray-900 mb-3">基本信息</h3>
          <div class="grid grid-cols-2 gap-4 text-sm">
            <div>
              <span class="text-gray-600">作业标题：</span>
              <span class="font-medium">{{ selectedSubmission.homeworkTitle }}</span>
            </div>
            <div>
              <span class="text-gray-600">提交时间：</span>
              <span class="font-medium">{{ formatDate(selectedSubmission.submitTime) }}</span>
            </div>
            <div>
              <span class="text-gray-600">提交状态：</span>
              <el-tag :type="getStatusTagType(selectedSubmission.status)" size="small">
                {{ getStatusText(selectedSubmission.status) }}
              </el-tag>
            </div>
            <div v-if="selectedSubmission.score !== undefined">
              <span class="text-gray-600">得分：</span>
              <span class="font-medium" :class="getScoreColor(selectedSubmission.score, selectedSubmission.maxScore)">
                {{ selectedSubmission.score === null ? '未评分' : `${ selectedSubmission.score }`}}/{{ selectedSubmission.maxScore }}
              </span>
            </div>
          </div>
        </div>

        <!-- 提交内容 -->
        <div class="detail-section mb-6">
          <h3 class="text-lg font-semibold text-gray-900 mb-3">提交内容</h3>
          <div class="bg-gray-50 rounded-lg p-4">
            <p class="text-gray-700 whitespace-pre-wrap text-left">{{ selectedSubmission.content }}</p>
          </div>
        </div>

        <!-- 提交附件 -->
        <div v-if="selectedSubmission.attachmentUrl" class="detail-section mb-6">
          <h3 class="text-lg font-semibold text-gray-900 mb-3">提交附件</h3>
          <div class="bg-blue-100 rounded-lg p-4">
            <div class="flex items-center gap-3">
              <a 
                :href="selectedSubmission.attachmentUrl" 
                target="_blank"
                class="inline-flex items-center gap-2 text-blue-700 hover:text-blue-900 hover:underline"
              >
                <i class="fas fa-download"></i>
                下载附件
              </a>
              <el-button 
                type="primary" 
                size="small" 
                plain
                @click="previewFile(selectedSubmission.attachmentUrl)"
              >
                <i class="fas fa-eye mr-1"></i>预览
              </el-button>
            </div>
          </div>
        </div>

        <!-- 教师评语 -->
        <div v-if="selectedSubmission.teacherComment" class="detail-section">
          <h3 class="text-lg font-semibold text-gray-900 mb-3">教师评语</h3>
          <div class="bg-yellow-50 border-l-4 border-yellow-400 p-4">
            <p class="text-gray-700 text-left">{{ selectedSubmission.teacherComment }}</p>
            <p v-if="selectedSubmission.gradeTime" class="text-xs text-gray-500 mt-2 text-left">
              批改时间：{{ formatDate(selectedSubmission.gradeTime) }}
            </p>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 文件预览对话框 -->
    <el-dialog
      v-model="previewDialogVisible"
      title="文件预览"
      width="90%"
      :before-close="handlePreviewClose"
      class="file-preview-dialog"
    >
      <div class="preview-container">
        <!-- 图片预览 -->
        <img 
          v-if="isImageFile(previewFileUrl)" 
          :src="previewFileUrl" 
          alt="预览图片"
          class="preview-image"
        />
        
        <!-- PDF预览 -->
        <iframe 
          v-else-if="isPdfFile(previewFileUrl)"
          :src="previewFileUrl"
          class="preview-iframe"
          frameborder="0"
        ></iframe>
        
        <!-- 文本文件预览 -->
        <div v-else-if="isTextFile(previewFileUrl)" class="preview-text">
          <el-skeleton v-if="loadingPreview" :rows="10" animated />
          <pre v-else class="text-content">{{ previewTextContent }}</pre>
        </div>
        
        <!-- 不支持预览的文件类型 -->
        <div v-else class="preview-unsupported">
          <i class="fas fa-file text-6xl text-gray-400 mb-4"></i>
          <p class="text-gray-600 mb-4">该文件类型不支持在线预览</p>
          <el-button type="primary" @click="downloadFile(previewFileUrl)">
            <i class="fas fa-download mr-2"></i>下载文件
          </el-button>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="previewDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="downloadFile(previewFileUrl)">
            <i class="fas fa-download mr-2"></i>下载
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '@/api'
import type { HomeworkSubmission, HomeworkSubmissionSearchParams } from '@/api'
import Pagination from '@/components/Pagination.vue'
import Navbar from '@/components/Navbar.vue'

const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const submissions = ref<HomeworkSubmission[]>([])
const myCourses = ref<any[]>([])
const total = ref(0)
const detailDialogVisible = ref(false)
const selectedSubmission = ref<HomeworkSubmission | null>(null)

// 文件预览相关
const previewDialogVisible = ref(false)
const previewFileUrl = ref('')
const previewTextContent = ref('')
const loadingPreview = ref(false)

// 搜索参数
const searchParams = reactive<HomeworkSubmissionSearchParams>({
  page: 1,
  size: 10,
  courseId: undefined,
  status: undefined
})



// 加载提交记录
const loadSubmissions = async () => {
  try {
    loading.value = true
    
    // 如果URL中有homeworkId参数，添加到搜索条件中
    const homeworkId = route.query.homeworkId
    if (homeworkId) {
      // 这里可以根据homeworkId筛选，但API中没有这个参数
      // 可以在获取数据后进行前端筛选
    }
    
    const response = await userApi.getMySubmissions(searchParams)
    submissions.value = response.list || response.records || []
    total.value = response.total
    
    // 如果有homeworkId参数，进行前端筛选
    if (homeworkId) {
      submissions.value = submissions.value.filter(
        submission => submission.homeworkId === Number(homeworkId)
      )
      total.value = submissions.value.length
    }
  } catch (error) {
    ElMessage.error('加载提交记录失败')
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
  loadSubmissions()
  loadMyCourses()
}

// 分页处理
const handlePageChange = (page: number) => {
  searchParams.page = page
  loadSubmissions()
}

// 查看提交详情
const viewSubmissionDetail = (submission: HomeworkSubmission) => {
  selectedSubmission.value = submission
  detailDialogVisible.value = true
}

// 查看作业
const viewHomework = (homeworkId: number) => {
  router.push(`/homework/${homeworkId}`)
}

// 撤回提交
const withdrawSubmission = async (submission: HomeworkSubmission) => {
  try {
    await ElMessageBox.confirm(
      '确定要撤回这个提交吗？撤回后可以重新提交。',
      '确认撤回',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await userApi.deleteHomeworkSubmission(submission.id)
    ElMessage.success('撤回成功')
    
    // 从列表中移除
    const index = submissions.value.findIndex(s => s.id === submission.id)
    if (index > -1) {
      submissions.value.splice(index, 1)
      total.value--
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('撤回失败')
    }
  }
}

// 返回上一页
const goBack = () => {
  console.log('goBack 函数被调用')
  try {
    // 优先使用 router.back()，如果历史记录为空则跳转到作业列表
    if (window.history.length > 1) {
      console.log('使用 router.back()')
      router.back()
    } else {
      console.log('跳转到 /homework')
      router.push('/homework')
    }
  } catch (error) {
    console.error('导航错误:', error)
    ElMessage.error('导航失败，请重试')
    // 如果出错，直接跳转到作业列表
    router.push('/homework').catch(err => {
      console.error('跳转失败:', err)
    })
  }
}

// 工具函数
const getStatusText = (status: number) => {
  const statusMap = {
    0: '已提交待批改',
    1: '已批改'
  }
  return statusMap[status] || '未知状态'
}

const getStatusTagType = (status: number) => {
  const typeMap = {
    0: 'warning',  // 已提交待批改 - 橙色
    1: 'success'   // 已批改 - 绿色
  }
  return typeMap[status] || 'info'
}

const getScoreColor = (score: number, maxScore: number) => {
  const percentage = (score / maxScore) * 100
  if (percentage >= 90) return 'text-green-600'
  if (percentage >= 80) return 'text-blue-700'
  if (percentage >= 70) return 'text-yellow-600'
  if (percentage >= 60) return 'text-orange-600'
  return 'text-red-600'
}

const canWithdraw = (submission: HomeworkSubmission) => {
  // 只有已提交待批改的作业可以撤回
  return submission.status === 0
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleString('zh-CN')
}

// 文件预览相关函数
const isImageFile = (url: string) => {
  if (!url) return false
  const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp', '.svg']
  const lowerUrl = url.toLowerCase()
  return imageExtensions.some(ext => lowerUrl.includes(ext)) || 
         lowerUrl.includes('image/')
}

const isPdfFile = (url: string) => {
  if (!url) return false
  const lowerUrl = url.toLowerCase()
  return lowerUrl.includes('.pdf') || lowerUrl.includes('application/pdf')
}

const isTextFile = (url: string) => {
  if (!url) return false
  const textExtensions = ['.txt', '.md', '.json', '.xml', '.csv', '.log', '.code', '.js', '.ts', '.vue', '.html', '.css']
  const lowerUrl = url.toLowerCase()
  return textExtensions.some(ext => lowerUrl.includes(ext))
}

const previewFile = async (url: string) => {
  if (!url) {
    ElMessage.warning('文件地址无效')
    return
  }
  
  previewFileUrl.value = url
  previewDialogVisible.value = true
  
  // 如果是文本文件，尝试加载内容
  if (isTextFile(url)) {
    loadingPreview.value = true
    try {
      const response = await fetch(url)
      if (response.ok) {
        previewTextContent.value = await response.text()
      } else {
        ElMessage.warning('无法加载文件内容')
      }
    } catch (error) {
      console.error('加载文本文件失败:', error)
      ElMessage.warning('无法加载文件内容，请尝试下载')
    } finally {
      loadingPreview.value = false
    }
  }
}

const handlePreviewClose = () => {
  previewDialogVisible.value = false
  previewFileUrl.value = ''
  previewTextContent.value = ''
}

const downloadFile = (url: string) => {
  if (!url) return
  window.open(url, '_blank')
}

// 组件挂载
onMounted(() => {
  // 从URL参数中获取初始筛选条件
  const courseId = route.query.courseId
  const status = route.query.status
  
  if (courseId) {
    searchParams.courseId = Number(courseId)
  }
  if (status) {
    searchParams.status = Number(status)
  }
  
  loadSubmissions()
  loadMyCourses()
})
</script>

<style scoped>
.homework-submissions-page {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
}

.page-header {
  background: #2563eb;
  position: relative;
  overflow: hidden;
}

.page-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 20"><defs><radialGradient id="a" cx="50%" cy="0%" r="100%"><stop offset="0%" stop-color="%23ffffff" stop-opacity="0.1"/><stop offset="100%" stop-color="%23ffffff" stop-opacity="0"/></radialGradient></defs><rect width="100" height="20" fill="url(%23a)"/></svg>');
  opacity: 0.3;
}

.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.submission-card {
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid rgba(30, 64, 175, 0.1);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.submission-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 15px 35px rgba(30, 64, 175, 0.15);
  border-color: rgba(30, 64, 175, 0.2);
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
  background: linear-gradient(135deg, rgba(30, 64, 175, 0.05) 0%, rgba(59, 130, 246, 0.05) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.stat-card:hover::before {
  opacity: 1;
}

.stat-card:hover {
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 15px 35px rgba(30, 64, 175, 0.2);
}

.score-display {
  text-align: center;
}

.detail-section {
  border-bottom: 1px solid rgba(30, 64, 175, 0.1);
  padding-bottom: 1rem;
}

.detail-section:last-child {
  border-bottom: none;
  padding-bottom: 0;
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
  background: rgba(30, 64, 175, 0.3);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(30, 64, 175, 0.5);
}

/* 响应式优化 */
@media (max-width: 768px) {
  .submission-card:hover {
    transform: none;
    box-shadow: 0 4px 12px rgba(30, 64, 175, 0.15);
  }
  
  .stat-card:hover {
    transform: translateY(-2px);
  }
}

/* 文件预览对话框样式 */
.file-preview-dialog :deep(.el-dialog__body) {
  padding: 20px;
  max-height: 70vh;
  overflow: auto;
}

.preview-container {
  width: 100%;
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-image {
  max-width: 100%;
  max-height: 70vh;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.preview-iframe {
  width: 100%;
  min-height: 600px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.preview-text {
  width: 100%;
  max-height: 600px;
  overflow: auto;
  background: #f9fafb;
  border-radius: 8px;
  padding: 20px;
}

.preview-text .text-content {
  margin: 0;
  font-family: 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.6;
  color: #374151;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.preview-unsupported {
  text-align: center;
  padding: 60px 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>