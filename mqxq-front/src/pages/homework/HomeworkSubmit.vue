<template>
  <div class="homework-submit-page">
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
            <h1 class="text-3xl font-bold mb-2 text-left">{{ isEdit ? '修改提交' : '提交作业' }}</h1>
            <p class="text-blue-100 text-lg">{{ homework?.title || '作业提交' }}</p>
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
          <div class="lg:col-span-2">
            <!-- 作业信息 -->
            <div class="homework-info bg-white rounded-lg shadow-sm p-6 mb-6">
              <h2 class="text-xl font-bold text-gray-900 mb-4">作业要求</h2>
              <div class="bg-blue-50 rounded-lg p-4 mb-4">
                <div class="text-gray-700 markdown-content" v-html="renderedHomeworkContent"></div>
              </div>
              
              <div v-if="homework.attachmentUrl" class="flex items-center gap-3 text-blue-600">
                <i class="fas fa-paperclip"></i>
                <a :href="homework.attachmentUrl" target="_blank" class="hover:underline">
                  下载作业附件
                </a>
              </div>

              <!-- 参考答案（仅在已提交且已批改后显示） -->
              <div v-if="homework.answer && existingSubmission?.status === 1" class="mt-6">
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
            </div>

            <!-- 提交表单 -->
            <div class="submit-form bg-white rounded-lg shadow-sm p-6">
              <h2 class="text-xl font-bold text-gray-900 mb-6">
                {{ isEdit ? '修改提交内容' : '提交作业内容' }}
              </h2>

              <el-form 
                ref="formRef" 
                :model="submitForm" 
                :rules="formRules" 
                label-width="100px"
                size="large"
              >
                <!-- 提交内容 -->
                <el-form-item label="作业内容" prop="content" required>
                  <el-input
                    v-model="submitForm.content"
                    type="textarea"
                    :rows="12"
                    placeholder="请输入您的作业内容..."
                    show-word-limit
                    maxlength="5000"
                    resize="vertical"
                  />
                  <div class="text-sm text-gray-500 mt-2">
                    <i class="fas fa-info-circle mr-1"></i>
                    请详细描述您的解答过程和思路，最多5000字
                  </div>
                </el-form-item>

                <!-- 附件上传 -->
                <el-form-item label="上传附件">
                  <div class="w-full">
                    <el-upload
                      ref="uploadRef"
                      :auto-upload="true"
                      :before-upload="beforeUpload"
                      :http-request="customUpload"
                      :on-success="handleUploadSuccess"
                      :on-error="handleUploadError"
                      :on-remove="handleFileRemove"
                      :file-list="fileList"
                      :limit="1"
                      drag
                      class="w-full"
                      action=""
                    >
                      <div class="upload-area py-8">
                        <i class="fas fa-cloud-upload-alt text-4xl text-gray-400 mb-4"></i>
                        <div class="text-gray-600">
                          <p class="text-lg mb-2">点击或拖拽文件到此处上传</p>
                          <p class="text-sm text-gray-500">
                            支持 PDF、Word、图片等格式，单个文件不超过 10MB
                          </p>
                        </div>
                      </div>
                    </el-upload>

                    <!-- 上传进度 -->
                    <div v-if="uploadProgress > 0 && uploadProgress < 100" class="mt-4">
                      <el-progress :percentage="uploadProgress" :show-text="true" />
                    </div>

                    <!-- 已上传文件 -->
                    <div v-if="submitForm.attachmentUrl" class="mt-4 p-3 bg-green-50 rounded-lg">
                      <div class="flex items-center justify-between">
                        <div class="flex items-center gap-2 text-green-700">
                          <i class="fas fa-check-circle"></i>
                          <span>附件已上传</span>
                        </div>
                        <el-button 
                          type="danger" 
                          size="small" 
                          text
                          @click="removeAttachment"
                        >
                          删除
                        </el-button>
                      </div>
                    </div>
                  </div>
                </el-form-item>

                <!-- 提交按钮 -->
                <el-form-item>
                  <div class="flex gap-4">
                    <el-button 
                      type="primary" 
                      size="large"
                      @click="submitHomework"
                      :loading="submitting"
                      :disabled="!canSubmit"
                    >
                      <i class="fas fa-paper-plane mr-2"></i>
                      {{ isEdit ? '更新提交' : '提交作业' }}
                    </el-button>

                    <el-button 
                      size="large"
                      @click="goBack"
                    >
                      取消
                    </el-button>
                  </div>
                </el-form-item>
              </el-form>
            </div>
          </div>

          <!-- 侧边栏 -->
          <div class="space-y-6">
            <!-- 作业信息卡片 -->
            <div class="info-card bg-white rounded-lg shadow-sm p-6">
              <h3 class="text-lg font-semibold text-gray-900 mb-4">作业信息</h3>
              
              <div class="space-y-3">
                <div class="flex justify-between">
                  <span class="text-gray-600">截止时间</span>
                  <div class="text-right">
                    <div class="font-medium" :class="isOverdue ? 'text-red-600' : 'text-gray-900'">
                      {{ formatDate((homework as any).endTime || homework.deadline) }}
                    </div>
                    <div v-if="isOverdue" class="text-xs text-red-500">已过期</div>
                    <div v-else class="text-xs text-gray-500">{{ getTimeRemaining() }}</div>
                  </div>
                </div>

                <div class="flex justify-between">
                  <span class="text-gray-600">满分</span>
                  <span class="font-medium text-gray-900">{{ (homework as any).score || homework.maxScore }}分</span>
                </div>

                <div class="flex justify-between">
                  <span class="text-gray-600">提交状态</span>
                  <el-tag :type="getStatusTagType(homework.status)" size="small">
                    {{ getStatusText(homework.status) }}
                  </el-tag>
                </div>
              </div>
            </div>

            <!-- 提交提示 -->
            <div class="tips-card bg-yellow-50 border border-yellow-200 rounded-lg p-6">
              <h3 class="text-lg font-semibold text-yellow-800 mb-3">
                <i class="fas fa-lightbulb mr-2"></i>提交提示
              </h3>
              <ul class="text-sm text-yellow-700 space-y-2">
                <li>• 请仔细阅读作业要求，确保内容完整</li>
                <li>• 提交前请检查文字是否有错误</li>
                <li>• 如有附件，请确保文件格式正确</li>
                <li>• 提交后可以在截止时间前修改</li>
                <li>• 过期后将无法提交或修改</li>
              </ul>
            </div>

            <!-- 历史提交 -->
            <div v-if="existingSubmission" class="history-card bg-white rounded-lg shadow-sm p-6">
              <h3 class="text-lg font-semibold text-gray-900 mb-4">当前提交</h3>
              
              <div class="bg-gray-50 rounded-lg p-4">
                <div class="text-sm text-gray-600 mb-2">
                  提交时间: {{ formatDate(existingSubmission.submitTime) }}
                </div>
                <div class="text-sm text-gray-700 line-clamp-3 text-left">
                  {{ existingSubmission.content }}
                </div>
                <div v-if="existingSubmission.score !== undefined" class="mt-2">
                  <span class="text-sm font-medium text-green-600">
                    得分: {{ existingSubmission.score === null ? `0` : existingSubmission.score}}/{{ existingSubmission.maxScore }}
                  </span>
                </div>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules, UploadInstance } from 'element-plus'
import { userApi, fileApi } from '@/api'
import type { Homework, HomeworkSubmission } from '@/api/types/homework'
import Navbar from '@/components/Navbar.vue'
import { marked } from 'marked'

const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const uploadProgress = ref(0)
const homework = ref<Homework | null>(null)
const existingSubmission = ref<HomeworkSubmission | null>(null)

// 表单引用
const formRef = ref<FormInstance>()
const uploadRef = ref<UploadInstance>()

// 提交表单数据
const submitForm = reactive({
  homeworkId: 0,
  content: '',
  attachmentUrl: ''
})

// 文件列表
const fileList = ref<any[]>([])

// 表单验证规则
const formRules: FormRules = {
  content: [
    { required: true, message: '请输入作业内容', trigger: 'blur' },
    { min: 10, message: '作业内容至少10个字符', trigger: 'blur' }
  ]
}

// 计算属性
const isEdit = computed(() => !!existingSubmission.value)

const isOverdue = computed(() => {
  if (!homework.value) return false
  const deadline = (homework.value as any).endTime || homework.value.deadline
  return deadline ? new Date(deadline) < new Date() : false
})

const canSubmit = computed(() => {
  return submitForm.content.trim().length >= 10 && !isOverdue.value
})

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
  const content = (homework.value as any)?.content || homework.value?.description
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
  const answer = (homework.value as any)?.answer
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

// 加载数据
const loadData = async () => {
  try {
    loading.value = true
    const id = Number(route.params.id)
    submitForm.homeworkId = id
    
    // 先获取作业详情
    try {
      homework.value = await userApi.getHomeworkDetail(id)
    } catch (error) {
      console.warn('获取作业详情失败:', error)
    }
    
    // 使用作业提交接口获取提交信息
    try {
      const response: any = await userApi.getHomeworkSubmissionByHomework(id)
      
      // 处理两种可能的返回格式
      if (response && typeof response === 'object') {
        if ('homework' in response) {
          // 格式1: { homework: {...}, submission: {...} }
          if (response.homework) {
            homework.value = response.homework
          }
          if (response.submission) {
            existingSubmission.value = response.submission
            submitForm.content = response.submission.content || ''
            submitForm.attachmentUrl = response.submission.attachmentUrl || ''
            
            // 如果有附件，设置文件列表
            if (response.submission.attachmentUrl) {
              const fileName = response.submission.attachmentUrl.split('/').pop() || '附件'
              fileList.value = [{
                name: fileName,
                url: response.submission.attachmentUrl,
                status: 'success'
              }]
            }
          }
        } else if ('id' in response && 'homeworkId' in response) {
          // 格式2: 直接返回 HomeworkSubmission 对象
          existingSubmission.value = response as HomeworkSubmission
          submitForm.content = response.content || ''
          submitForm.attachmentUrl = response.attachmentUrl || ''
          
          // 如果有附件，设置文件列表
          if (response.attachmentUrl) {
            const fileName = response.attachmentUrl.split('/').pop() || '附件'
            fileList.value = [{
              name: fileName,
              url: response.attachmentUrl,
              status: 'success'
            }]
          }
        }
      }
    } catch (error: any) {
      // 如果没有提交记录，这是正常的（首次提交）
      // 404 或其他错误都视为没有提交记录
      console.log('未找到提交记录，这是首次提交:', error?.message || error)
      existingSubmission.value = null
      submitForm.content = ''
      submitForm.attachmentUrl = ''
      fileList.value = []
    }
    
    // 如果还是没有获取到作业信息，显示错误
    if (!homework.value) {
      ElMessage.error('作业不存在或无法访问')
    }
  } catch (error) {
    console.error('加载作业信息失败:', error)
    ElMessage.error('加载作业信息失败')
  } finally {
    loading.value = false
  }
}

// 文件上传处理
const beforeUpload = (file: File) => {
  const isValidType = [
    'application/pdf',
    'application/msword',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'image/jpeg',
    'image/png',
    'image/gif'
  ].includes(file.type)
  
  const isLt10M = file.size / 1024 / 1024 < 10
  
  if (!isValidType) {
    ElMessage.error('只能上传 PDF、Word 或图片格式的文件!')
    return false
  }
  
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!')
    return false
  }
  
  return true
}

// 自定义上传处理 - 文件选择后立即上传
const customUpload = async (options: any) => {
  if (!homework.value) {
    options.onError(new Error('作业信息未加载'))
    return
  }
  
  try {
    uploadProgress.value = 10
    
    // 如果已有旧文件，先删除旧文件
    if (submitForm.attachmentUrl) {
      try {
        await fileApi.deleteFileByParam(submitForm.attachmentUrl)
        console.log('旧文件已删除')
      } catch (error) {
        // 删除旧文件失败不影响新文件上传，只记录日志
        console.warn('删除旧文件失败，继续上传新文件:', error)
      }
    }
    
    // 调用 API 上传文件
    const response = await userApi.uploadHomeworkAttachment(homework.value.id, options.file)
    
    uploadProgress.value = 100
    
    // 根据实际API响应格式处理
    // API可能返回: { "msg": "url" } 或 { "data": "url" } 或 { "url": "url" } 或直接返回字符串
    let fileUrl = ''
    if (response && response.msg) {
      // 优先检查 msg 字段（当前后端返回格式）
      fileUrl = response.msg
    } else if (response && response.data) {
      fileUrl = response.data
    } else if (response && response.url) {
      fileUrl = response.url
    } else if (typeof response === 'string') {
      fileUrl = response
    }
    
    if (fileUrl) {
      // 更新附件URL
      submitForm.attachmentUrl = fileUrl
      
      // 更新文件列表以显示新上传的文件
      const fileName = options.file.name || fileUrl.split('/').pop() || '附件'
      fileList.value = [{
        name: fileName,
        url: fileUrl,
        status: 'success',
        uid: Date.now()
      }]
      
      ElMessage.success('文件上传成功')
      options.onSuccess(response)
    } else {
      throw new Error('上传响应格式错误')
    }
    
    setTimeout(() => {
      uploadProgress.value = 0
    }, 1000)
  } catch (error) {
    uploadProgress.value = 0
    ElMessage.error('文件上传失败')
    options.onError(error)
  }
}

const handleUploadSuccess = () => {
  // 上传成功处理已在 customUpload 中完成
}

const handleUploadError = () => {
  ElMessage.error('文件上传失败')
}

// 处理文件删除（通过上传组件的删除按钮）
const handleFileRemove = async () => {
  await removeAttachment()
}

const removeAttachment = async () => {
  if (!submitForm.attachmentUrl) return
  
  try {
    // 调用删除文件API
    await fileApi.deleteFileByParam(submitForm.attachmentUrl)
    
    // 清空本地状态
    submitForm.attachmentUrl = ''
    fileList.value = []
    uploadRef.value?.clearFiles()
    
    ElMessage.success('附件删除成功')
  } catch (error: any) {
    console.error('删除文件失败:', error)
    
    // 检查是否是服务器返回"文件删除成功"但被当作错误的情况
    if (error?.error === '文件删除成功' || error?.message?.includes('文件删除成功')) {
      // 实际上删除成功了，清空本地状态
      submitForm.attachmentUrl = ''
      fileList.value = []
      uploadRef.value?.clearFiles()
      ElMessage.success('附件删除成功')
    } else {
      ElMessage.error('删除文件失败，请重试')
    }
  }
}

// 提交作业
const submitHomework = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    await ElMessageBox.confirm(
      '确定要提交作业吗？提交后可以在截止时间前修改。',
      '确认提交',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    submitting.value = true
    
    // 文件已经在选择时上传，这里直接提交作业数据
    if (isEdit.value && existingSubmission.value) {
      const updateData: HomeworkSubmission = {
        ...existingSubmission.value,
        homeworkId: submitForm.homeworkId,
        content: submitForm.content,
        attachmentUrl: submitForm.attachmentUrl || undefined,
        maxScore: (homework.value as any)?.score || homework.value?.maxScore || 0,
      }
      await userApi.updateHomeworkSubmission(existingSubmission.value.id, updateData)
      ElMessage.success('修改提交成功')
    } else {
      const submitData: HomeworkSubmission = {
        id: 0,
        homeworkId: submitForm.homeworkId,
        studentId: 0, // 后端会自动填充
        content: submitForm.content,
        attachmentUrl: submitForm.attachmentUrl || undefined,
        maxScore: (homework.value as any)?.score || homework.value?.maxScore || 0,
        status: 1, // 已提交
        submitTime: new Date().toISOString(),
        createTime: new Date().toISOString(),
        updateTime: new Date().toISOString(),
        createUser: 0,
        updateUser: 0,
      }
      await userApi.submitHomework(submitData)
      ElMessage.success('提交作业成功')
    }
    
    // 返回详情页
    router.push(`/homework/${homework.value?.id}`)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(isEdit.value ? '修改提交失败' : '提交作业失败')
    }
  } finally {
    submitting.value = false
  }
}

// 导航方法
const goBack = () => {
  // 优先使用 router.back()，如果历史记录为空则跳转到作业列表
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/homework')
  }
}

// 工具函数
const getStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '未提交',
    1: '已提交',
    2: '已批改',
    3: '已过期'
  }
  return statusMap[status] || '未知'
}

const getStatusTagType = (status: number) => {
  const typeMap: Record<number, string> = {
    0: 'warning',
    1: 'success',
    2: 'primary',
    3: 'danger'
  }
  return typeMap[status] || 'info'
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleString('zh-CN')
}

const getTimeRemaining = () => {
  if (!homework.value) return ''
  
  const deadlineStr = (homework.value as any).endTime || homework.value.deadline
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

// 组件挂载
onMounted(async () => {
  await loadData()
})
</script>

<style scoped>
.homework-submit-page {
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  min-height: 100vh;
}

.page-header {
  background: #2563eb;
  position: relative;
  overflow: hidden;
}

.homework-info,
.submit-form,
.info-card,
.tips-card,
.history-card {
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid rgba(59, 130, 246, 0.1);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.homework-info:hover,
.submit-form:hover,
.info-card:hover,
.tips-card:hover,
.history-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(59, 130, 246, 0.1);
  border-color: rgba(59, 130, 246, 0.2);
}

.upload-area {
  border: 2px dashed rgba(59, 130, 246, 0.3);
  border-radius: 12px;
  transition: all 0.3s ease;
  background: rgba(59, 130, 246, 0.02);
}

.upload-area:hover {
  border-color: rgba(59, 130, 246, 0.6);
  background: rgba(59, 130, 246, 0.05);
  transform: translateY(-2px);
}

.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
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
  text-align: left;
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
  text-align: left;
}

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  margin: 15px 0;
  padding-left: 25px;
  text-align: left;
}

.markdown-content :deep(li) {
  margin-bottom: 8px;
  line-height: 1.6;
  color: #374151;
  text-align: left;
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
  text-align: left;
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
  text-align: left;
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
</style>