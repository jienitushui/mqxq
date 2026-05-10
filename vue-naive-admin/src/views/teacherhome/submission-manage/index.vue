<template>
  <div class="h-full flex flex-col">
    <!-- 页面标题 -->
    <PageHeader class="flex-shrink-0">
      <template #title>作业提交管理</template>
      <template #extra>
        <n-space>
          <n-button @click="refreshData" :loading="loading">
            <template #icon>
              <i class="i-material-symbols:refresh" />
            </template>
            刷新
          </n-button>
          <n-button 
            @click="handleExport" 
            type="primary" 
            :disabled="!searchForm.homeworkId"
          >
            <template #icon>
              <i class="i-carbon:download" />
            </template>
            导出提交记录
          </n-button>
          <n-button type="primary" @click="batchGrade" :disabled="selectedSubmissions.length === 0">
            <template #icon>
              <i class="i-material-symbols:grade" />
            </template>
            批量批改 ({{ selectedSubmissions.length }})
          </n-button>
        </n-space>
      </template>
    </PageHeader>

    <!-- 主内容区域 -->
    <n-card class="flex-1 flex flex-col overflow-hidden">
      <!-- 筛选条件 -->
      <div class="flex-shrink-0 mb-4">
        <n-form
          ref="searchFormRef"
          :model="searchForm"
          label-placement="left"
          :label-width="80"
          inline
        >
          <n-form-item label="选择作业">
            <n-input
              :value="getHomeworkName(searchForm.homeworkId)"
              placeholder="请选择作业"
              readonly
              clearable
              @click="openHomeworkSelector"
              @clear="clearHomework"
              style="width: 250px; cursor: pointer"
            >
              <template #suffix>
                <i class="i-carbon:search" />
              </template>
            </n-input>
          </n-form-item>
          <n-form-item label="批改状态">
            <n-select
              v-model:value="searchForm.status"
              placeholder="批改状态"
              clearable
              style="width: 150px"
              :options="gradeStatusOptions"
              @update:value="handleSearch"
            />
          </n-form-item>
          <n-form-item label="学生姓名">
            <n-input
              v-model:value="searchForm.studentName"
              placeholder="学生姓名"
              clearable
              style="width: 150px"
              @keyup.enter="handleSearch"
            />
          </n-form-item>
          <n-form-item>
            <n-button type="primary" @click="handleSearch">
              <template #icon>
                <i class="i-carbon:search" />
              </template>
              搜索
            </n-button>
            <n-button @click="handleReset" style="margin-left: 8px">
              <template #icon>
                <i class="i-carbon:reset" />
              </template>
              重置
            </n-button>
          </n-form-item>
        </n-form>
      </div>

      <!-- 统计信息 -->
      <div v-if="searchForm.homeworkId" class="flex-shrink-0 mb-4">
        <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
          <n-card>
            <n-statistic label="总提交数" :value="statistics.totalSubmissions || 0" />
          </n-card>
          <n-card>
            <n-statistic label="已批改" :value="statistics.gradedCount || 0" />
          </n-card>
          <n-card>
            <n-statistic label="待批改" :value="statistics.ungradedCount || 0" />
          </n-card>
          <n-card>
            <n-statistic label="平均分" :value="statistics.avgScore || 0" :precision="1" />
          </n-card>
          <n-card>
            <n-statistic label="最高分" :value="statistics.maxScore || 0" />
          </n-card>
          <n-card>
            <n-statistic label="最低分" :value="statistics.minScore || 0" />
          </n-card>
          <n-card>
            <n-statistic label="及格率" :value="statistics.passRate || 0" :precision="1">
              <template #suffix>%</template>
            </n-statistic>
          </n-card>
          <n-card>
            <n-statistic label="优秀率" :value="statistics.excellentRate || 0" :precision="1">
              <template #suffix>%</template>
            </n-statistic>
          </n-card>
        </div>
      </div>

      <!-- 表格容器 -->
      <div class="flex-1 min-h-0">
        <n-data-table
          :columns="columns"
          :data="tableData"
          :loading="loading"
          :pagination="pagination"
          :row-key="row => row.id"
          v-model:checked-row-keys="selectedSubmissions"
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
          remote
          :scroll-x="1400"
          :max-height="800"
          virtual-scroll
          striped
          size="small"
          :bordered="false"
        >
          <template #empty>
            <n-empty description="暂无数据" />
          </template>
        </n-data-table>
      </div>

      <!-- 未提交学生列表 -->
      <div v-if="searchForm.homeworkId && unsubmittedStudents.length > 0" class="flex-shrink-0 mt-4">
        <n-card title="未提交学生">
          <div class="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-3">
            <n-tag
              v-for="student in unsubmittedStudents"
              :key="student.id"
              type="warning"
              size="large"
            >
              {{ student.name }}
            </n-tag>
          </div>
        </n-card>
      </div>
    </n-card>

  </div>

  <!-- 作业选择弹窗 -->
  <n-modal v-model:show="showHomeworkModal" :mask-closable="false">
    <n-card
      style="width: 900px"
      title="选择作业"
      :bordered="false"
      size="huge"
      role="dialog"
      aria-modal="true"
    >
      <template #header-extra>
        <n-button quaternary circle @click="showHomeworkModal = false">
          <template #icon>
            <i class="i-carbon:close" />
          </template>
        </n-button>
      </template>
      
      <!-- 搜索区域 -->
      <div class="mb-4">
        <n-form inline>
          <n-form-item label="作业标题">
            <n-input
              v-model:value="homeworkSearch.title"
              placeholder="请输入作业标题"
              clearable
              @keyup.enter="handleHomeworkSearch"
              style="width: 200px"
            />
          </n-form-item>
          <n-form-item label="课程">
            <n-input
              v-model:value="homeworkSearch.courseName"
              placeholder="请输入课程名称"
              clearable
              @keyup.enter="handleHomeworkSearch"
              style="width: 150px"
            />
          </n-form-item>
          <n-form-item label="状态">
            <n-select
              v-model:value="homeworkSearch.status"
              placeholder="请选择状态"
              :options="homeworkStatusOptions"
              clearable
              style="width: 120px"
            />
          </n-form-item>
          <n-form-item>
            <n-button type="primary" @click="handleHomeworkSearch">
              <template #icon>
                <i class="i-carbon:search" />
              </template>
              搜索
            </n-button>
            <n-button @click="handleHomeworkReset" style="margin-left: 8px">
              <template #icon>
                <i class="i-carbon:reset" />
              </template>
              重置
            </n-button>
          </n-form-item>
        </n-form>
      </div>

      <!-- 作业列表 -->
      <n-data-table
        :columns="homeworkColumns"
        :data="homeworkList"
        :loading="homeworkLoading"
        :pagination="homeworkPagination"
        :row-key="row => row.id"
        @update:page="handleHomeworkPageChange"
        @update:page-size="handleHomeworkPageSizeChange"
        remote
        :max-height="400"
        striped
        size="small"
      >
        <template #empty>
          <n-empty description="暂无作业数据" />
        </template>
      </n-data-table>
    </n-card>
  </n-modal>

  <!-- 批改弹窗 -->
  <n-modal v-model:show="showGradeModal" preset="card" :title="isViewMode ? '查看提交详情' : '批改作业'" style="width: 800px">
      <div v-if="currentSubmission">
        <div class="mb-16">
          <h3 class="font-semibold mb-8">学生信息</h3>
          <div class="grid grid-cols-2 gap-16">
            <div>
              <span class="text-gray-600">学生姓名：</span>
              <span>{{ currentSubmission.studentName || `学生ID: ${currentSubmission.studentId}` }}</span>
            </div>
            <div>
              <span class="text-gray-600">提交时间：</span>
              <span>{{ currentSubmission.submitTime }}</span>
            </div>
          </div>
        </div>

        <div class="mb-16">
          <h3 class="font-semibold mb-8">提交内容</h3>
          <div class="p-12 bg-gray-50 rounded-8 max-h-300 overflow-y-auto">
            {{ currentSubmission.content }}
          </div>
        </div>

        <div v-if="currentSubmission.attachmentUrl" class="mb-16">
          <h3 class="font-semibold mb-8">附件</h3>
          <n-button 
            type="primary" 
            ghost
            tag="a" 
            :href="currentSubmission.attachmentUrl" 
            target="_blank"
            download
          >
            <template #icon>
              <i class="i-carbon:download" />
            </template>
            下载附件
          </n-button>
        </div>

        <n-form :model="gradeForm" :rules="isViewMode ? {} : gradeRules" ref="gradeFormRef" label-placement="top">
          <n-form-item label="分数" path="score">
            <n-input-number
              v-model:value="gradeForm.score"
              :min="0"
              :max="currentSubmission.maxScore || 100"
              :disabled="isViewMode"
              placeholder="请输入分数"
              style="width: 200px"
            />
            <span v-if="currentSubmission.maxScore" class="ml-8 text-gray-500">
              满分：{{ currentSubmission.maxScore }}
            </span>
          </n-form-item>
          <n-form-item label="评语" path="comment">
            <n-input
              v-model:value="gradeForm.comment"
              type="textarea"
              :disabled="isViewMode"
              placeholder="请输入评语"
              :rows="4"
            />
          </n-form-item>
        </n-form>
      </div>

      <template #footer>
        <div class="flex justify-end gap-12">
          <n-button @click="showGradeModal = false">{{ isViewMode ? '关闭' : '取消' }}</n-button>
          <n-button v-if="!isViewMode" type="primary" @click="confirmGrade" :loading="grading">
            确认批改
          </n-button>
        </div>
      </template>
    </n-modal>

    <!-- 批量批改弹窗 -->
    <n-modal v-model:show="showBatchGradeModal" preset="card" title="批量批改" style="width: 500px">
      <n-form :model="batchGradeForm" :rules="batchGradeRules" ref="batchGradeFormRef" label-placement="top">
        <n-form-item label="统一分数" path="score">
          <n-input-number
            v-model:value="batchGradeForm.score"
            :min="0"
            :max="100"
            placeholder="请输入统一分数"
            style="width: 200px"
          />
        </n-form-item>
        <n-form-item label="统一评语" path="comment">
          <n-input
            v-model:value="batchGradeForm.comment"
            type="textarea"
            placeholder="请输入统一评语"
            :rows="4"
          />
        </n-form-item>
      </n-form>

      <template #footer>
        <div class="flex justify-end gap-12">
          <n-button @click="showBatchGradeModal = false">取消</n-button>
          <n-button type="primary" @click="confirmBatchGrade" :loading="batchGrading">
            批量批改 ({{ selectedSubmissions.length }})
          </n-button>
        </div>
      </template>
    </n-modal>
</template>

<script setup>
import { ref, reactive, onMounted, h, watch } from 'vue'
import { NButton, NTag, NSpace, useMessage } from 'naive-ui'
import { useRoute } from 'vue-router'
import { PageHeader } from '@/components'
import api from './api'

const message = useMessage()
const route = useRoute()

const loading = ref(false)
const grading = ref(false)
const batchGrading = ref(false)

// 选中的提交
const selectedSubmissions = ref([])

// 搜索表单
const searchFormRef = ref()
const searchForm = reactive({
  homeworkId: null,
  status: null, // 批改状态：0-待批改, 1-已批改
  studentName: ''
})

// 作业选项（用于显示名称）
const homeworkOptions = ref([])

// 作业选择弹窗相关
const showHomeworkModal = ref(false)
const homeworkLoading = ref(false)
const homeworkList = ref([])

// 作业搜索表单
const homeworkSearch = reactive({
  title: '',
  courseName: '',
  status: null
})

// 作业状态选项
const homeworkStatusOptions = [
  { label: '全部', value: null },
  { label: '未发布', value: 0 },
  { label: '已发布', value: 1 },
  { label: '已结束', value: 2 }
]

// 作业分页配置
const homeworkPagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  showQuickJumper: true,
  prefix: ({ itemCount }) => `共 ${itemCount} 条`
})

// 状态选项（批改状态：0-待批改, 1-已批改）
const gradeStatusOptions = [
  { label: '待批改', value: 0 },
  { label: '已批改', value: 1 }
]

// 统计数据
const statistics = reactive({
  totalSubmissions: 0,
  gradedCount: 0,
  ungradedCount: 0,
  avgScore: 0,
  maxScore: 0,
  minScore: 0,
  passRate: 0,
  excellentRate: 0
})

// 表格数据
const tableData = ref([])
const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  showQuickJumper: true,
  prefix: ({ itemCount }) => `共 ${itemCount} 条`,
  simple: false,
  disabled: false
})

// 未提交学生
const unsubmittedStudents = ref([])

// 批改相关
const showGradeModal = ref(false)
const isViewMode = ref(false) // 是否为查看模式
const currentSubmission = ref(null)
const gradeFormRef = ref()
const gradeForm = reactive({
  score: null,
  comment: ''
})
const gradeRules = {
  score: { 
    required: true, 
    type: 'number',
    message: '请输入分数', 
    trigger: ['blur', 'change'] 
  }
}

// 批量批改相关
const showBatchGradeModal = ref(false)
const batchGradeFormRef = ref()
const batchGradeForm = reactive({
  score: null,
  comment: ''
})
const batchGradeRules = {
  score: { 
    required: true, 
    type: 'number',
    message: '请输入分数', 
    trigger: ['blur', 'change'] 
  }
}

// 作业表格列定义
const homeworkColumns = [
  {
    title: 'ID',
    key: 'id',
    width: 80
  },
  {
    title: '作业标题',
    key: 'title',
    width: 200,
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '课程名称',
    key: 'courseName',
    width: 150,
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '截止时间',
    key: 'endTime',
    width: 180
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) => {
      const statusMap = {
        0: { type: 'warning', text: '未发布' },
        1: { type: 'success', text: '已发布' },
        2: { type: 'error', text: '已结束' }
      }
      const status = statusMap[row.status] || { type: 'default', text: '未知' }
      return h(NTag, { type: status.type }, { default: () => status.text })
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    render: (row) => h(
      NButton,
      {
        size: 'small',
        type: 'primary',
        onClick: () => handleSelectHomework(row)
      },
      { default: () => '选择' }
    )
  }
]

// 表格列定义
const columns = [
  { type: 'selection' },
  { title: '学生姓名', key: 'studentName', width: 150 },
  { title: '学生ID', key: 'studentId', width: 100 },
  { title: '作业标题', key: 'homeworkTitle', width: 150, ellipsis: { tooltip: true } },
  { title: '提交内容', key: 'content', width: 200, ellipsis: { tooltip: true } },
  {
    title: '批改状态',
    key: 'status',
    width: 100,
    render: (row) => {
      const statusMap = {
        0: { type: 'warning', text: '待批改' },
        1: { type: 'success', text: '已批改' }
      }
      const status = statusMap[row.status] || { type: 'default', text: '未知' }
      return h(NTag, { type: status.type }, { default: () => status.text })
    }
  },
  {
    title: '分数',
    key: 'score',
    width: 100,
    render: (row) => {
      if (row.score !== null && row.score !== undefined) {
        return `${row.score} / ${row.maxScore || 100}`
      }
      return '-'
    }
  },
  { title: '教师评语', key: 'teacherComment', width: 150, ellipsis: { tooltip: true } },
  { title: '提交时间', key: 'submitTime', width: 180 },
  { title: '批改时间', key: 'gradeTime', width: 180 },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    render: (row) => h(NSpace, { size: 'small' }, {
      default: () => [
        h(NButton, {
          size: 'small',
          type: 'primary',
          ghost: true,
          onClick: () => viewSubmission(row)
        }, { default: () => '查看' }),
        h(NButton, {
          size: 'small',
          onClick: () => gradeSubmission(row)
        }, { default: () => row.status === 1 ? '重新批改' : '批改' })
      ]
    })
  }
]

// 加载作业列表（用于弹窗选择）
const loadHomeworkList = async () => {
  homeworkLoading.value = true
  try {
    const params = {
      page: homeworkPagination.page,
      size: homeworkPagination.pageSize,
      title: homeworkSearch.title || '',
      courseName: homeworkSearch.courseName || '',
      status: homeworkSearch.status ?? ''
    }
    const res = await api.getHomeworkList(params)
    const data = res?.data || res || {}
    homeworkList.value = data.list || data.records || []
    homeworkPagination.itemCount = data.total || data.totalCount || 0
    
    // 同时更新 homeworkOptions 用于显示名称
    homeworkList.value.forEach(homework => {
      const existingOption = homeworkOptions.value.find(opt => opt.value === homework.id)
      if (!existingOption) {
        homeworkOptions.value.push({
          label: homework.title,
          value: homework.id
        })
      }
    })
  } catch (error) {
    console.error('加载作业列表失败', error)
    message.error('加载作业列表失败')
    homeworkList.value = []
    homeworkPagination.itemCount = 0
  } finally {
    homeworkLoading.value = false
  }
}

// 打开作业选择器
const openHomeworkSelector = () => {
  homeworkSearch.title = ''
  homeworkSearch.courseName = ''
  homeworkSearch.status = null
  homeworkPagination.page = 1
  showHomeworkModal.value = true
  loadHomeworkList()
}

// 选择作业
const handleSelectHomework = (homework) => {
  searchForm.homeworkId = homework.id
  
  // 更新 homeworkOptions
  const existingOption = homeworkOptions.value.find(opt => opt.value === homework.id)
  if (!existingOption) {
    homeworkOptions.value.push({
      label: homework.title,
      value: homework.id
    })
  }
  
  showHomeworkModal.value = false
  message.success(`已选择作业：${homework.title}`)
  handleHomeworkChange()
}

// 获取作业名称
const getHomeworkName = (homeworkId) => {
  if (!homeworkId) return ''
  const homework = homeworkOptions.value.find(opt => opt.value === homeworkId)
  return homework ? homework.label : ''
}

// 清除作业选择
const clearHomework = () => {
  searchForm.homeworkId = null
  handleHomeworkChange()
}

// 作业搜索
const handleHomeworkSearch = () => {
  homeworkPagination.page = 1
  loadHomeworkList()
}

// 作业搜索重置
const handleHomeworkReset = () => {
  homeworkSearch.title = ''
  homeworkSearch.courseName = ''
  homeworkSearch.status = null
  homeworkPagination.page = 1
  loadHomeworkList()
}

// 作业分页
const handleHomeworkPageChange = (page) => {
  homeworkPagination.page = page
  loadHomeworkList()
}

const handleHomeworkPageSizeChange = (pageSize) => {
  homeworkPagination.pageSize = pageSize
  homeworkPagination.page = 1
  loadHomeworkList()
}

// 加载提交列表
const loadSubmissions = async () => {
  if (!searchForm.homeworkId) {
    tableData.value = []
    return
  }

  try {
    loading.value = true
    const params = {
      page: pagination.page,
      size: pagination.pageSize,
      status: searchForm.status,
      studentName: searchForm.studentName
    }
    
    const res = await api.getSubmissionList(searchForm.homeworkId, params)
    const data = res?.data || {}
    
    tableData.value = data.list || []
    pagination.itemCount = data.total || 0
  } catch (error) {
    console.error('加载提交列表失败:', error)
    message.error('加载提交列表失败')
  } finally {
    loading.value = false
  }
}

// 加载统计数据
const loadStatistics = async () => {
  if (!searchForm.homeworkId) return

  try {
    const res = await api.getSubmissionStatistics(searchForm.homeworkId)
    Object.assign(statistics, res?.data || {})
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载未提交学生
const loadUnsubmittedStudents = async () => {
  if (!searchForm.homeworkId) return

  try {
    const res = await api.getUnsubmittedStudents(searchForm.homeworkId)
    unsubmittedStudents.value = res?.data || []
  } catch (error) {
    console.error('加载未提交学生失败:', error)
  }
}

// 作业变化处理
const handleHomeworkChange = () => {
  selectedSubmissions.value = []
  loadSubmissions()
  loadStatistics()
  loadUnsubmittedStudents()
}

// 查看提交详情
const viewSubmission = async (submission) => {
  try {
    const res = await api.getSubmissionDetail(searchForm.homeworkId, submission.studentId)
    const detailData = res?.data || {}
    // 合并数据，保留列表中的有用信息（如 studentName, homeworkTitle）
    currentSubmission.value = {
      ...submission,
      ...detailData,
      // 如果详情中没有这些字段，使用列表中的值
      studentName: detailData.studentName || submission.studentName || `学生ID: ${submission.studentId}`,
      homeworkTitle: detailData.homeworkTitle || submission.homeworkTitle || '未知作业'
    }
    // 初始化表单数据，回显分数和评语
    gradeForm.score = detailData.score || submission.score || null
    gradeForm.comment = detailData.teacherComment || submission.teacherComment || ''
    // 设置为查看模式
    isViewMode.value = true
    showGradeModal.value = true
  } catch (error) {
    console.error('获取提交详情失败:', error)
    // 如果获取详情失败，直接使用当前数据
    currentSubmission.value = submission
    // 初始化表单数据
    gradeForm.score = submission.score || null
    gradeForm.comment = submission.teacherComment || ''
    // 设置为查看模式
    isViewMode.value = true
    showGradeModal.value = true
  }
}

// 批改作业
const gradeSubmission = (submission) => {
  currentSubmission.value = submission
  gradeForm.score = submission.score || null
  gradeForm.comment = submission.teacherComment || ''
  // 设置为批改模式
  isViewMode.value = false
  showGradeModal.value = true
}

// 确认批改
const confirmGrade = async () => {
  try {
    await gradeFormRef.value?.validate()
    grading.value = true
    
    // 构造批改数据
    const gradeData = {
      score: gradeForm.score,
      teacherComment: gradeForm.comment,
      gradeTime: new Date().toISOString()
    }

    await api.gradeSubmission(currentSubmission.value.id, gradeData)
    
    message.success('批改成功')
    showGradeModal.value = false
    loadSubmissions()
    loadStatistics()
  } catch (error) {
    console.error('批改失败:', error)
    message.error('批改失败')
  } finally {
    grading.value = false
  }
}

// 批量批改
const batchGrade = () => {
  if (selectedSubmissions.value.length === 0) {
    message.warning('请选择要批改的提交')
    return
  }
  
  batchGradeForm.score = null
  batchGradeForm.comment = ''
  showBatchGradeModal.value = true
}

// 确认批量批改
const confirmBatchGrade = async () => {
  try {
    await batchGradeFormRef.value?.validate()
    batchGrading.value = true
    
    await api.batchGradeSubmissions(searchForm.homeworkId, {
      submissionIds: selectedSubmissions.value,
      score: batchGradeForm.score,
      comment: batchGradeForm.comment
    })
    
    message.success('批量批改成功')
    showBatchGradeModal.value = false
    selectedSubmissions.value = []
    loadSubmissions()
    loadStatistics()
  } catch (error) {
    console.error('批量批改失败:', error)
    message.error('批量批改失败')
  } finally {
    batchGrading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.page = 1
  loadSubmissions()
}

// 重置处理
const handleReset = () => {
  searchFormRef.value?.restoreValidation()
  searchForm.status = null
  searchForm.studentName = ''
  handleSearch()
}

// 分页处理
const handlePageChange = (page) => {
  pagination.page = page
  loadSubmissions()
}

const handlePageSizeChange = (pageSize) => {
  pagination.pageSize = pageSize
  pagination.page = 1
  loadSubmissions()
}

// 刷新数据
const refreshData = () => {
  loadSubmissions()
  loadStatistics()
  loadUnsubmittedStudents()
}

// 导出作业提交记录
const handleExport = async () => {
  if (!searchForm.homeworkId) {
    message.warning('请先选择作业')
    return
  }
  
  try {
    const blob = await api.exportSubmissions(searchForm.homeworkId)
    
    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `作业提交记录_${searchForm.homeworkId}_${Date.now()}.xlsx`
    document.body.appendChild(link)
    link.click()
    
    // 清理
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    message.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    message.error('导出失败')
  }
}

// 监听作业ID变化
watch(() => searchForm.homeworkId, handleHomeworkChange)

onMounted(() => {
  // 从路由查询参数中获取作业ID
  if (route.query.homeworkId) {
    const homeworkId = Number(route.query.homeworkId)
    if (homeworkId) {
      searchForm.homeworkId = homeworkId
      // 需要先加载作业信息以显示名称
      api.getHomeworkList({ page: 1, size: 1, homeworkId }).then(res => {
        const data = res?.data || res || {}
        const homeworks = data.list || data.records || []
        if (homeworks.length > 0) {
          homeworkOptions.value.push({
            label: homeworks[0].title,
            value: homeworks[0].id
          })
        }
        handleHomeworkChange()
      }).catch(error => {
        console.error('加载作业信息失败:', error)
        handleHomeworkChange()
      })
    }
  }
})
</script>

<style scoped>
/* 确保表格可以正确滚动 */
:deep(.n-data-table-wrapper) {
  display: flex;
  flex-direction: column;
  max-height: 100%;
}

:deep(.n-data-table) {
  display: flex;
  flex-direction: column;
}

:deep(.n-data-table-base-table-header) {
  flex-shrink: 0;
}

:deep(.n-data-table-base-table) {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

:deep(.n-data-table-base-table-body) {
  flex: 1;
  overflow-y: auto;
  min-height: 200px;
}

/* 分页样式优化 */
:deep(.n-pagination) {
  margin-top: 16px;
  justify-content: center;
  flex-shrink: 0;
  padding: 8px 0;
  border-top: 1px solid var(--n-border-color);
}

/* 表格滚动条样式 */
:deep(.n-scrollbar-rail) {
  right: 2px;
}

:deep(.n-scrollbar-rail--vertical) {
  width: 6px;
}

:deep(.n-scrollbar-rail--horizontal) {
  height: 6px;
}
</style>