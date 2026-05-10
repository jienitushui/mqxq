<template>
  <div class="homework-detail-page">
    <!-- 导航栏 -->
    <Navbar />
    
    <!-- 页面头部 -->
    <div class="page-header bg-gradient-to-r from-blue-600 to-blue-800 text-white py-12">
      <div class="container mx-auto px-4">
        <div class="flex items-center gap-4 mb-4">
          <el-button 
            @click="goBack" 
            type="primary" 
            plain 
            size="small"
            class="text-white border-white hover:bg-white hover:text-blue-600"
          >
            <i class="fas fa-arrow-left mr-2"></i>返回
          </el-button>
          <div>
            <h1 class="text-3xl font-bold mb-2">{{ homework?.title || '作业详情' }}</h1>
            <p class="text-blue-100 text-lg text-left">查看作业要求和提交情况</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 页面内容 -->
    <div class="page-content py-6">
      <div class="container mx-auto px-4">
        <div v-if="loading" class="py-12">
          <el-skeleton :rows="8" animated />
        </div>

        <div v-else-if="homework" class="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <!-- 主要内容区域 -->
          <div class="lg:col-span-2 space-y-6">
            <!-- 作业信息卡片 -->
            <div class="homework-info bg-white rounded-lg shadow-sm p-6">
              <div class="flex items-start justify-between mb-6">
                <div>
                  <h2 class="text-2xl font-bold text-gray-900 mb-2">{{ homework.title }}</h2>
                  <div class="flex items-center gap-4 text-sm text-gray-500">
                    <span>作业ID: {{ homework.id }}</span>
                    <span>发布时间: {{ formatDate(homework.createTime) }}</span>
                  </div>
                </div>
                <el-tag :type="getStatusTagType(submission?.status)" size="large">
                  {{ getStatusText(submission?.status) }}
                </el-tag>
              </div>

              <!-- 作业描述 -->
              <div class="homework-description mb-6">
                <h3 class="text-lg font-semibold text-gray-900 mb-3">作业要求</h3>
                <div class="prose max-w-none text-left">
                  <div class="text-gray-700 leading-relaxed markdown-content" v-html="renderedHomeworkContent"></div>
                </div>
              </div>

              <!-- 作业附件 -->
              <div v-if="homework.attachmentUrl" class="homework-attachment mb-6">
                <h3 class="text-lg font-semibold text-gray-900 mb-3">作业附件</h3>
                <div class="bg-gray-50 rounded-lg p-4">
                  <div class="flex items-center gap-3">
                    <i class="fas fa-file-download text-blue-600 text-xl"></i>
                    <div class="flex-1">
                      <div class="flex items-center gap-3">
                        <a 
                          :href="homework.attachmentUrl" 
                          target="_blank"
                          class="text-blue-600 hover:text-blue-800 font-medium hover:underline"
                        >
                          下载作业附件
                        </a>
                        <el-button 
                          type="primary" 
                          size="small" 
                          plain
                          @click="previewFile(homework.attachmentUrl)"
                        >
                          <i class="fas fa-eye mr-1"></i>预览
                        </el-button>
                      </div>
                      <p class="text-sm text-gray-500">点击下载或预览查看详细要求</p>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 参考答案 -->
              <div v-if="homework.answer && submission?.status === 1" class="homework-answer mb-6">
                <h3 class="text-lg font-semibold text-gray-900 mb-3">
                  <i class="fas fa-lightbulb text-yellow-500 mr-2"></i>参考答案
                </h3>
                <div class="bg-gradient-to-r from-yellow-50 to-orange-50 border-l-4 border-yellow-400 rounded-lg p-4">
                  <div class="text-sm text-yellow-800 mb-2">
                    <i class="fas fa-info-circle mr-1"></i>
                    此答案仅在作业已批改后显示，供您参考学习
                  </div>
                  <div class="prose max-w-none text-left">
                    <div class="text-gray-700 leading-relaxed markdown-content" v-html="renderedAnswerContent"></div>
                  </div>
                </div>
              </div>

              <!-- 提交记录 -->
              <div v-if="submission" class="submission-info">
                <h3 class="text-lg font-semibold text-gray-900 mb-3">我的提交</h3>
                <div class="bg-blue-50 rounded-lg p-4">
                  <div class="flex items-start justify-between mb-3">
                    <div>
                      <p class="font-medium text-gray-900">提交时间: {{ formatDate(submission.submitTime) }}</p>
                      <p class="text-sm text-gray-600">提交ID: {{ submission.id }}</p>
                    </div>
                    <div class="text-right">
                      <div v-if="submission.score !== undefined" class="text-lg font-bold text-green-600">
                        {{ submission.score === null ? '未评分' : submission.score }}/{{ submission.maxScore }}分
                      </div>
                      <div v-else class="text-sm text-gray-500">待批改</div>
                    </div>
                  </div>

                  <!-- 提交内容 -->
                  <div class="mb-3">
                    <p class="text-sm font-medium text-gray-700 mb-2">提交内容:</p>
                    <p class="text-gray-600 bg-white rounded p-3 whitespace-pre-wrap text-left">{{ submission.content }}</p>
                  </div>

                  <!-- 提交附件 -->
                  <div v-if="submission.attachmentUrl" class="mb-3">
                    <p class="text-sm font-medium text-gray-700 mb-2">提交附件:</p>
                    <div class="flex items-center gap-3">
                      <a 
                        :href="submission.attachmentUrl" 
                        target="_blank"
                        class="inline-flex items-center gap-2 text-blue-600 hover:text-blue-800 hover:underline"
                      >
                        <i class="fas fa-paperclip"></i>
                        下载附件
                      </a>
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

                  <!-- 教师评语 -->
                  <div v-if="submission.teacherComment" class="teacher-comment">
                    <p class="text-sm font-medium text-gray-700 mb-2">教师评语:</p>
                    <div class="bg-yellow-50 border-l-4 border-yellow-400 p-3">
                      <p class="text-gray-700 text-left">{{ submission.teacherComment }}</p>
                      <p v-if="submission.gradeTime" class="text-xs text-gray-500 mt-2 text-left">
                        批改时间: {{ formatDate(submission.gradeTime) }}
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 侧边栏 -->
          <div class="space-y-6">
            <!-- 作业状态卡片 -->
            <div class="status-card bg-white rounded-lg shadow-sm p-6">
              <h3 class="text-lg font-semibold text-gray-900 mb-4">作业状态</h3>
              
              <div class="space-y-4">
                <!-- 截止时间 -->
                <div class="flex items-center justify-between">
                  <span class="text-gray-600">截止时间</span>
                  <div class="text-right">
                    <div class="font-medium" :class="isOverdue ? 'text-red-600' : 'text-gray-900'">
                      {{ formatDate(homework.endTime || homework.deadline) }}
                    </div>
                    <div v-if="isOverdue" class="text-xs text-red-500">已过期</div>
                    <div v-else class="text-xs text-gray-500">{{ getTimeRemaining() }}</div>
                  </div>
                </div>

                <!-- 满分 -->
                <div class="flex items-center justify-between">
                  <span class="text-gray-600">满分</span>
                  <span class="font-medium text-gray-900">{{ homework.score || homework.maxScore }}分</span>
                </div>

                <!-- 我的得分 -->
                <div v-if="submission?.score !== undefined" class="flex items-center justify-between">
                  <span class="text-gray-600">我的得分</span>
                  <span class="font-medium text-green-600">{{ submission.score === null ? '未评' : submission.score }}分</span>
                </div>

                <!-- 提交状态 -->
                <div class="flex items-center justify-between">
                  <span class="text-gray-600">提交状态</span>
                  <el-tag :type="getStatusTagType(submission?.status)" size="small">
                    {{ getStatusText(submission?.status) }}
                  </el-tag>
                </div>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="action-card bg-white rounded-lg shadow-sm p-6">
              <h3 class="text-lg font-semibold text-gray-900 mb-4">操作</h3>
              
              <div class="space-y-3">
                <!-- 提交作业 -->
                <el-button 
                  v-if="canSubmit"
                  type="primary" 
                  size="large" 
                  class="w-full"
                  @click="goToSubmit"
                >
                  <i class="fas fa-upload mr-2"></i>提交作业
                </el-button>

                <!-- 修改提交 -->
                <el-button 
                  v-if="canModify"
                  type="warning" 
                  size="large" 
                  class="w-full"
                  @click="goToSubmit"
                >
                  <i class="fas fa-edit mr-2"></i>修改提交
                </el-button>

                <!-- 撤回提交 -->
                <el-button 
                  v-if="canWithdraw"
                  type="danger" 
                  size="large" 
                  class="w-full"
                  @click="withdrawSubmission"
                  :loading="withdrawing"
                  style="margin-left: 0px;"
                >
                  <i class="fas fa-undo mr-2"></i>撤回提交
                </el-button>

                <!-- 查看所有提交记录 -->
                <el-button 
                  size="large" 
                  class="w-full"
                  @click="viewAllSubmissions"
                  style="margin-left: 0px;"
                >
                  <i class="fas fa-history mr-2"></i>提交记录
                </el-button>
              </div>
            </div>

            <!-- 课程信息 -->
            <div class="course-info bg-white rounded-lg shadow-sm p-6">
              <h3 class="text-lg font-semibold text-gray-900 mb-4">所属课程</h3>
              <div class="text-center">
                <i class="fas fa-book text-4xl text-blue-500 mb-3"></i>
                <p class="font-medium text-gray-900">课程ID: {{ homework.courseId }}</p>
                <el-button 
                  type="primary" 
                  plain 
                  size="small" 
                  class="mt-3"
                  @click="viewCourse"
                >
                  查看课程
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 错误状态 -->
        <div v-else class="error-state py-12">
          <i class="fas fa-exclamation-triangle text-6xl text-red-300 mb-4"></i>
          <h3 class="text-xl font-medium text-gray-500 mb-2">作业不存在</h3>
          <p class="text-gray-400 mb-4">该作业可能已被删除或您没有访问权限</p>
          <el-button type="primary" @click="goBack">返回列表</el-button>
        </div>
      </div>
    </div>

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
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '@/api'
import type { Homework, HomeworkSubmission } from '@/api'
import Navbar from '@/components/Navbar.vue'
import { marked } from 'marked'

const route = useRoute()
const router = useRouter()

// 导航方法 - 提前定义
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
  }
}

const goToSubmit = () => {
  router.push(`/homework/${homework.value?.id}/submit`)
}

const viewAllSubmissions = () => {
  router.push(`/homework/submissions?homeworkId=${homework.value?.id}`)
}

// 响应式数据
const loading = ref(false)
const withdrawing = ref(false)
const homework = ref<Homework | null>(null)
const submission = ref<HomeworkSubmission | null>(null)
const hasSubmitted = ref(false)
const canSubmitFromApi = ref(false)

// 文件预览相关
const previewDialogVisible = ref(false)
const previewFileUrl = ref('')
const previewTextContent = ref('')
const loadingPreview = ref(false)

// 计算属性
const isOverdue = computed(() => {
  if (!homework.value) return false
  const deadline = homework.value.endTime || homework.value.deadline
  return deadline ? new Date(deadline) < new Date() : false
})

const canSubmit = computed(() => {
  if (!homework.value) return false
  // 根据 API 响应，使用 hasSubmitted 和 canSubmit 字段
  return !hasSubmitted.value && canSubmitFromApi.value && !isOverdue.value
})

const canModify = computed(() => {
  if (!homework.value || !submission.value) return false
  return hasSubmitted.value && !isOverdue.value
})

const canWithdraw = computed(() => {
  if (!homework.value || !submission.value) return false
  return hasSubmitted.value && !isOverdue.value
})

// 工具函数
const getStatusText = (status?: number) => {
  // 如果没有提交记录，显示未提交或已过期
  if (!hasSubmitted.value) {
    return isOverdue.value ? '已过期' : '未提交'
  }
  
  // 根据提交状态显示
  const statusMap: Record<number, string> = {
    0: '已提交待批改',
    1: '已批改'
  }
  return statusMap[status!] || '未知状态'
}

const getStatusTagType = (status?: number) => {
  // 如果没有提交记录
  if (!hasSubmitted.value) {
    return isOverdue.value ? 'danger' : 'info'
  }
  
  // 根据提交状态返回标签类型
  const typeMap: Record<number, string> = {
    0: 'warning',  // 已提交待批改 - 橙色
    1: 'success'   // 已批改 - 绿色
  }
  return typeMap[status!] || 'info'
}

// 加载作业详情
const loadHomeworkDetail = async () => {
  try {
    loading.value = true
    const id = Number(route.params.id)
    
    // 先获取作业详情
    homework.value = await userApi.getHomeworkDetail(id)
    
    // 再尝试获取提交记录
    try {
      const submissionResponse = await userApi.getHomeworkSubmissionByHomework(id)
      
      // API返回的是 HomeworkSubmission 对象
      if (submissionResponse && submissionResponse.id) {
        submission.value = submissionResponse as any
        hasSubmitted.value = true
        // 如果已批改（status=1）则不能再提交，否则可以修改
        canSubmitFromApi.value = false
      } else {
        // 没有提交记录
        submission.value = null
        hasSubmitted.value = false
        canSubmitFromApi.value = true
      }
    } catch (submissionError) {
      // 获取提交记录失败，说明没有提交过
      console.log('未找到提交记录，用户尚未提交作业')
      submission.value = null
      hasSubmitted.value = false
      canSubmitFromApi.value = true
    }
  } catch (error) {
    console.error('加载作业详情失败:', error)
    ElMessage.error('加载作业详情失败')
    homework.value = null
  } finally {
    loading.value = false
  }
}

// 撤回提交
const withdrawSubmission = async () => {
  if (!submission.value) return
  
  try {
    await ElMessageBox.confirm(
      '确定要撤回提交吗？撤回后可以重新提交。',
      '确认撤回',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    withdrawing.value = true
    await userApi.deleteHomeworkSubmission(submission.value.id)
    
    ElMessage.success('撤回成功')
    submission.value = null
    if (homework.value) {
      homework.value.status = 0
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('撤回失败')
    }
  } finally {
    withdrawing.value = false
  }
}



const viewCourse = () => {
  router.push(`/courses/${homework.value?.courseId}`)
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleString('zh-CN')
}

const getTimeRemaining = () => {
  if (!homework.value) return ''
  
  const deadlineStr = homework.value.endTime || homework.value.deadline
  if (!deadlineStr) return ''
  
  const deadline = new Date(deadlineStr)
  const now = new Date()
  const diff = deadline.getTime() - now.getTime()
  
  if (diff <= 0) return '已过期'
  
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
  
  if (days > 0) return `还有${days}天${hours}小时`
  return `还有${hours}小时`
}

// 简单的Markdown渲染函数
const renderMarkdown = (content: string) => {
  if (!content) return ''
  
  return content
    // 标题
    .replace(/^### (.*$)/gim, '<h3>$1</h3>')
    .replace(/^## (.*$)/gim, '<h2>$1</h2>')
    .replace(/^# (.*$)/gim, '<h1>$1</h1>')
    // 粗体
    .replace(/\*\*(.*)\*\*/gim, '<strong>$1</strong>')
    // 斜体
    .replace(/\*(.*)\*/gim, '<em>$1</em>')
    // 代码块
    .replace(/```([\s\S]*?)```/gim, '<pre><code>$1</code></pre>')
    // 行内代码
    .replace(/`([^`]*)`/gim, '<code>$1</code>')
    // 链接
    .replace(/\[([^\]]*)\]\(([^\)]*)\)/gim, '<a href="$2" target="_blank">$1</a>')
    // 无序列表
    .replace(/^\- (.*$)/gim, '<li>$1</li>')
    .replace(/(<li>.*<\/li>)/s, '<ul>$1</ul>')
    // 有序列表
    .replace(/^\d+\. (.*$)/gim, '<li>$1</li>')
    // 引用
    .replace(/^> (.*$)/gim, '<blockquote>$1</blockquote>')
    // 换行
    .replace(/\n/gim, '<br>')
}

// 渲染作业内容的Markdown
const renderedHomeworkContent = computed(() => {
  const content = homework.value?.content || homework.value?.description
  if (!content) return ''
  
  try {
    // 首先尝试使用marked库
    const rendered = marked(content, {
      breaks: true,
      gfm: true
    })
    return rendered
  } catch (error) {
    console.error('Markdown渲染错误，使用简单渲染:', error)
    // 如果marked失败，使用简单的渲染函数
    return renderMarkdown(content)
  }
})

// 渲染参考答案的Markdown
const renderedAnswerContent = computed(() => {
  const answer = homework.value?.answer
  if (!answer) return ''
  
  try {
    const rendered = marked(answer, {
      breaks: true,
      gfm: true
    })
    return rendered
  } catch (error) {
    console.error('Markdown渲染错误，使用简单渲染:', error)
    return renderMarkdown(answer)
  }
})

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
  loadHomeworkDetail()
})
</script>

<style scoped>
.homework-detail-page {
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  min-height: 100vh;
}

.page-header {
  background: #2563eb;
  position: relative;
  overflow: hidden;
}

.homework-info,
.info-card,
.submission-card {
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid rgba(59, 130, 246, 0.1);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.homework-info:hover,
.info-card:hover,
.submission-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 15px 35px rgba(59, 130, 246, 0.15);
  border-color: rgba(59, 130, 246, 0.2);
}

.prose {
  line-height: 1.7;
  text-align: left;
}

.prose p {
  margin-bottom: 1rem;
  text-align: left;
}

.empty-state {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 1rem;
  padding: 3rem;
  margin: 2rem 0;
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

/* Markdown 内容样式 */
.markdown-content :deep(h1),
.markdown-content :deep(h2),
.markdown-content :deep(h3),
.markdown-content :deep(h4),
.markdown-content :deep(h5),
.markdown-content :deep(h6) {
  color: #333;
  margin: 20px 0 15px 0;
  font-weight: 600;
  line-height: 1.4;
}

.markdown-content :deep(h1) {
  font-size: 1.8rem;
  border-bottom: 2px solid #2563eb;
  padding-bottom: 10px;
}

.markdown-content :deep(h2) {
  font-size: 1.5rem;
  border-bottom: 1px solid #ddd;
  padding-bottom: 8px;
}

.markdown-content :deep(h3) {
  font-size: 1.3rem;
}

.markdown-content :deep(h4) {
  font-size: 1.1rem;
}

.markdown-content :deep(p) {
  margin-bottom: 15px;
  line-height: 1.7;
  color: #374151;
}

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  margin: 15px 0;
  padding-left: 25px;
}

.markdown-content :deep(li) {
  margin-bottom: 8px;
  line-height: 1.6;
  color: #374151;
}

.markdown-content :deep(ul li) {
  list-style-type: disc;
}

.markdown-content :deep(ol li) {
  list-style-type: decimal;
}

.markdown-content :deep(code) {
  background: #f1f3f4;
  color: #d73a49;
  padding: 3px 8px;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  border: 1px solid #e1e4e8;
}

.markdown-content :deep(pre) {
  background: #f6f8fa;
  color: #24292e;
  padding: 20px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 20px 0;
  border: 1px solid #e1e4e8;
}

.markdown-content :deep(pre code) {
  background: none;
  padding: 0;
  border: none;
  font-size: 14px;
  color: #24292e;
}

.markdown-content :deep(blockquote) {
  border-left: 4px solid #2563eb;
  padding: 15px 20px;
  margin: 20px 0;
  background: #f8f9fa;
  color: #666;
  font-style: italic;
  border-radius: 0 8px 8px 0;
}

.markdown-content :deep(blockquote p) {
  margin: 0;
}

.markdown-content :deep(a) {
  color: #2563eb;
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: border-color 0.3s ease;
}

.markdown-content :deep(a:hover) {
  border-bottom-color: #2563eb;
}

.markdown-content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 20px 0;
  background: #ffffff;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e1e4e8;
}

.markdown-content :deep(th),
.markdown-content :deep(td) {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #e1e4e8;
  color: #374151;
}

.markdown-content :deep(th) {
  background: #f6f8fa;
  color: #333;
  font-weight: 600;
}

.markdown-content :deep(tr:last-child td) {
  border-bottom: none;
}

.markdown-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 15px 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.markdown-content :deep(hr) {
  border: none;
  height: 2px;
  background: linear-gradient(to right, transparent, #2563eb, transparent);
  margin: 30px 0;
}

.markdown-content :deep(strong) {
  color: #333;
  font-weight: 600;
}

.markdown-content :deep(em) {
  color: #666;
  font-style: italic;
}
</style>