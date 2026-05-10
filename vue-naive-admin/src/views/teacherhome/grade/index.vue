<template>
  <div class="teacher-grade-management h-full flex flex-col">
    <PageHeader class="flex-shrink-0">
      <template #title>作业批改</template>
    </PageHeader>

    <n-card class="flex-1 flex flex-col overflow-hidden">
      <!-- 搜索区域 -->
      <div class="flex-shrink-0 mb-4">
        <n-form inline :model="searchForm" label-placement="left">
          <n-form-item label="课程">
            <n-input
              :value="getCourseName(searchForm.courseId)"
              placeholder="请选择课程"
              readonly
              clearable
              @click="openCourseSelector"
              @clear="handleClearCourse"
              style="width: 200px; cursor: pointer"
            >
              <template #suffix>
                <i class="i-carbon:search" />
              </template>
            </n-input>
          </n-form-item>
          <n-form-item label="作业">
            <n-select
              v-model:value="searchForm.homeworkId"
              placeholder="请选择作业"
              :options="homeworkOptions"
              clearable
              style="width: 200px"
              @update:value="handleHomeworkChange"
            />
          </n-form-item>
          <n-form-item label="学生账号">
            <n-input
              v-model:value="searchForm.studentName"
              placeholder="请输入学生账号"
              clearable
              @keyup.enter="handleSearch"
            />
          </n-form-item>
          <n-form-item label="批改状态">
            <n-select
              v-model:value="searchForm.gradeStatus"
              placeholder="请选择状态"
              :options="gradeStatusOptions"
              clearable
              style="width: 120px"
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

      <!-- 表格 -->
      <div class="flex-1 min-h-0">
        <n-data-table
          :columns="columns"
          :data="tableData"
          :loading="loading"
          :pagination="pagination"
          :row-key="row => row.id"
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
          remote
          :scroll-x="1200"
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
    </n-card>

    <!-- 课程选择弹窗 -->
    <n-modal v-model:show="showCourseModal" :mask-closable="false">
      <n-card
        style="width: 800px"
        title="选择课程"
        :bordered="false"
        size="huge"
        role="dialog"
        aria-modal="true"
      >
        <template #header-extra>
          <n-button quaternary circle @click="showCourseModal = false">
            <template #icon>
              <i class="i-carbon:close" />
            </template>
          </n-button>
        </template>
        
        <!-- 搜索区域 -->
        <div class="mb-4">
          <n-form inline>
            <n-form-item label="课程名称">
              <n-input
                v-model:value="courseSearch.title"
                placeholder="请输入课程名称"
                clearable
                @keyup.enter="handleCourseSearch"
                style="width: 200px"
              />
            </n-form-item>
            <n-form-item label="状态">
              <n-select
                v-model:value="courseSearch.status"
                placeholder="请选择状态"
                :options="courseStatusOptions"
                clearable
                style="width: 120px"
              />
            </n-form-item>
            <n-form-item>
              <n-button type="primary" @click="handleCourseSearch">
                <template #icon>
                  <i class="i-carbon:search" />
                </template>
                搜索
              </n-button>
              <n-button @click="handleCourseReset" style="margin-left: 8px">
                <template #icon>
                  <i class="i-carbon:reset" />
                </template>
                重置
              </n-button>
            </n-form-item>
          </n-form>
        </div>

        <!-- 课程列表 -->
        <n-data-table
          :columns="courseColumns"
          :data="courseList"
          :loading="courseLoading"
          :pagination="coursePagination"
          :row-key="row => row.id"
          @update:page="handleCoursePageChange"
          @update:page-size="handleCoursePageSizeChange"
          remote
          :max-height="400"
          striped
          size="small"
        >
          <template #empty>
            <n-empty description="暂无课程数据" />
          </template>
        </n-data-table>
      </n-card>
    </n-modal>

    <!-- 批改弹窗 -->
    <n-modal v-model:show="showGradeModal" :mask-closable="false">
      <n-card
        style="width: 800px"
        title="作业批改"
        :bordered="false"
        size="huge"
        role="dialog"
        aria-modal="true"
      >
        <template #header-extra>
          <n-button quaternary circle @click="showGradeModal = false">
            <template #icon>
              <i class="i-carbon:close" />
            </template>
          </n-button>
        </template>
        
        <div v-if="currentSubmission">
          <!-- 作业信息 -->
          <n-descriptions :column="2" bordered>
            <n-descriptions-item label="作业标题">
              {{ currentSubmission.homeworkTitle }}
            </n-descriptions-item>
            <n-descriptions-item label="学生账号">
              {{ currentSubmission.studentName }}
            </n-descriptions-item>
            <n-descriptions-item label="提交时间">
              {{ formatDateTime(currentSubmission.submitTime) }}
            </n-descriptions-item>
            <n-descriptions-item label="总分">
              {{ currentSubmission.totalScore }}
            </n-descriptions-item>
          </n-descriptions>

          <!-- 学生答案 -->
          <n-divider title-placement="left">学生答案</n-divider>
          <n-card>
            <div v-html="currentSubmission.content"></div>
            <div v-if="currentSubmission.attachments && currentSubmission.attachments.length">
              <n-divider />
              <div class="mb-2">附件：</div>
              <n-space>
                <n-button
                  v-for="file in currentSubmission.attachments"
                  :key="file.id"
                  size="small"
                  @click="handleDownload(file)"
                >
                  {{ file.name }}
                </n-button>
              </n-space>
            </div>
          </n-card>

          <!-- 批改表单 -->
          <n-divider title-placement="left">批改</n-divider>
          <n-form
            ref="gradeFormRef"
            :model="gradeForm"
            :rules="gradeRules"
            label-placement="left"
            label-width="80px"
          >
            <n-form-item label="得分" path="score">
              <n-input-number
                v-model:value="gradeForm.score"
                :min="0"
                :max="currentSubmission.totalScore"
                placeholder="请输入得分"
              />
              <span class="ml-2">/ {{ currentSubmission.totalScore }}</span>
            </n-form-item>
            <n-form-item label="批改意见" path="teacherComment">
              <n-input
                v-model:value="gradeForm.teacherComment"
                type="textarea"
                placeholder="请输入批改意见"
                :rows="4"
              />
            </n-form-item>
          </n-form>
        </div>

        <template #footer>
          <div class="flex justify-end space-x-2">
            <n-button @click="showGradeModal = false">取消</n-button>
            <n-button type="primary" :loading="gradeLoading" @click="handleGradeSubmit">
              提交批改
            </n-button>
          </div>
        </template>
      </n-card>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h } from 'vue'
import { NButton, NTag, useMessage } from 'naive-ui'
import { PageHeader } from '@/components'
import api from './api'

// 时间格式化工具函数
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  
  try {
    const date = new Date(dateTime)
    if (isNaN(date.getTime())) return dateTime
    
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  } catch (error) {
    return dateTime
  }
}

const message = useMessage()

// 响应式数据
const loading = ref(false)
const showGradeModal = ref(false)
const gradeLoading = ref(false)
const tableData = ref([])
const gradeFormRef = ref(null)
const courseOptions = ref([])
const homeworkOptions = ref([])
const currentSubmission = ref(null)

// 课程选择弹窗相关
const showCourseModal = ref(false)
const courseLoading = ref(false)
const courseList = ref([])

// 课程搜索表单
const courseSearch = reactive({
  title: '',
  status: null
})

// 课程分页配置
const coursePagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  showQuickJumper: true,
  prefix: ({ itemCount }) => `共 ${itemCount} 条`
})

// 搜索表单
const searchForm = reactive({
  courseId: null,
  homeworkId: null,
  studentName: '',
  gradeStatus: null
})

// 批改状态选项
const gradeStatusOptions = [
  { label: '未批改', value: 0 },
  { label: '已批改', value: 1 }
]

// 分页配置
const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100],
  showQuickJumper: true,
  prefix: ({ itemCount }) => `共 ${itemCount} 条`,
  simple: false,
  disabled: false
})

// 批改表单
const gradeForm = reactive({
  score: 0,
  teacherComment: ''
})

// 批改表单验证规则
const gradeRules = {
  score: [
    { 
      required: true, 
      type: 'number',
      message: '请输入得分', 
      trigger: ['blur', 'change'] 
    }
  ]
}

// 课程状态选项
const courseStatusOptions = [
  { label: '全部', value: null },
  { label: '未发布', value: 0 },
  { label: '已发布', value: 1 }
]

// 课程表格列配置
const courseColumns = [
  {
    title: 'ID',
    key: 'id',
    width: 80
  },
  {
    title: '课程名称',
    key: 'title',
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render(row) {
      return row.status === 1 ? '已发布' : '未发布'
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    render(row) {
      return h(
        NButton,
        {
          size: 'small',
          type: 'primary',
          onClick: () => handleSelectCourse(row)
        },
        { default: () => '选择' }
      )
    }
  }
]

// 表格列配置
const columns = [
  {
    title: 'ID',
    key: 'id',
    width: 80
  },
  {
    title: '课程名称',
    key: 'courseName',
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '作业标题',
    key: 'homeworkTitle',
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '学生账号',
    key: 'studentName',
    width: 120
  },
  {
    title: '提交时间',
    key: 'submitTime',
    width: 180,
    render(row) {
      return formatDateTime(row.submitTime)
    }
  },
  {
    title: '得分',
    key: 'score',
    width: 100,
    render(row) {
      return row.score !== null ? `${row.score}/${row.totalScore}` : '-'
    }
  },
  {
    title: '批改状态',
    key: 'gradeStatus',
    width: 100,
    render(row) {
      return h(NTag, {
        type: row.gradeStatus === 1 ? 'success' : 'warning',
        size: 'small'
      }, {
        default: () => row.gradeStatus === 1 ? '已批改' : '未批改'
      })
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    render(row) {
      return h('div', { class: 'flex space-x-2' }, [
        h(
          NButton,
          {
            size: 'small',
            type: 'primary',
            secondary: true,
            onClick: () => handleGrade(row)
          },
          { default: () => row.gradeStatus === 1 ? '查看批改' : '批改' }
        )
      ])
    }
  }
]

// 方法
const loadData = async () => {
  // 如果没有选择作业，不发送请求
  if (!searchForm.homeworkId) {
    tableData.value = []
    pagination.itemCount = 0
    return
  }
  
  loading.value = true
  try {
    // 如果选择了特定作业，使用新的接口获取该作业的提交列表
    if (searchForm.homeworkId) {
      const submissions = await loadHomeworkSubmissions(searchForm.homeworkId)
      
      // 获取作业和课程信息
      const { homeworkTitle, courseName } = getHomeworkAndCourseInfo()
      
      // 数据格式转换，匹配表格显示需要的字段
      let mappedData = submissions.map(item => ({
        id: item.id,
        studentId: item.studentId,
        studentName: item.studentName,
        homeworkTitle: homeworkTitle,
        courseName: courseName,
        submitTime: item.submitTime,
        score: item.score,
        totalScore: item.maxScore,
        gradeStatus: item.status, // 0-未批改, 1-已批改
        teacherComment: item.teacherComment,
        content: item.content,
        attachmentUrl: item.attachmentUrl
      }))
      
      // 根据其他搜索条件过滤数据
      let filteredData = mappedData
      
      if (searchForm.studentName) {
        filteredData = filteredData.filter(item => 
          item.studentName && item.studentName.includes(searchForm.studentName)
        )
      }
      
      if (searchForm.gradeStatus !== null && searchForm.gradeStatus !== undefined) {
        filteredData = filteredData.filter(item => item.gradeStatus === searchForm.gradeStatus)
      }
      
      // 手动分页
      const startIndex = (pagination.page - 1) * pagination.pageSize
      const endIndex = startIndex + pagination.pageSize
      tableData.value = filteredData.slice(startIndex, endIndex)
      pagination.itemCount = filteredData.length
    } else {
      // 使用原有的通用接口
      const params = {
        page: pagination.page,
        size: pagination.pageSize,
        ...searchForm
      }
      const res = await api.getSubmissions(params)
      const data = res?.data || res || {}
      tableData.value = data.list || data.records || []
      pagination.itemCount = data.total || data.totalCount || 0
    }
  } catch (error) {
    message.error('加载数据失败')
    console.error(error)
    tableData.value = []
    pagination.itemCount = 0
  } finally {
    loading.value = false
  }
}

const loadCourses = async () => {
  try {
    const res = await api.getCourses()
    console.log('Grade页面课程数据:', res) // 调试日志
    
    let courses = []
    
    // 检查不同的数据结构
    if (res?.data) {
      if (Array.isArray(res.data)) {
        // 如果 res.data 是数组
        courses = res.data
      } else if (res.data.list || res.data.records) {
        // 如果是分页对象
        courses = res.data.list || res.data.records
      }
    } else if (Array.isArray(res)) {
      // 如果直接返回数组
      courses = res
    }
    
    courseOptions.value = courses.map(course => ({
      label: course.title,
      value: course.id
    }))
    
    console.log('Grade页面课程选项:', courseOptions.value) // 调试日志
  } catch (error) {
    console.error('加载课程列表失败', error)
    courseOptions.value = []
  }
}

// 加载课程列表（用于弹窗选择）
const loadCourseList = async () => {
  courseLoading.value = true
  try {
    const params = {
      page: coursePagination.page,
      size: coursePagination.pageSize,
      title: courseSearch.title || '',
      status: courseSearch.status ?? ''
    }
    const res = await api.getCourses(params)
    const data = res?.data || res || {}
    courseList.value = data.list || data.records || []
    coursePagination.itemCount = data.total || data.totalCount || 0
    
    // 同时更新 courseOptions 用于显示名称
    const newOptions = courseList.value.map(course => ({
      label: course.title,
      value: course.id
    }))
    
    // 合并选项，避免覆盖已选择的课程
    newOptions.forEach(newOpt => {
      const exists = courseOptions.value.find(opt => opt.value === newOpt.value)
      if (!exists) {
        courseOptions.value.push(newOpt)
      }
    })
  } catch (error) {
    console.error('加载课程列表失败', error)
    message.error('加载课程列表失败')
    courseList.value = []
    coursePagination.itemCount = 0
  } finally {
    courseLoading.value = false
  }
}

// 打开课程选择器
const openCourseSelector = () => {
  courseSearch.title = ''
  courseSearch.status = null
  coursePagination.page = 1
  showCourseModal.value = true
  loadCourseList()
}

// 选择课程
const handleSelectCourse = (course) => {
  searchForm.courseId = course.id
  handleCourseChange(course.id)
  
  // 更新 courseOptions
  const existingOption = courseOptions.value.find(opt => opt.value === course.id)
  if (!existingOption) {
    courseOptions.value.push({
      label: course.title,
      value: course.id
    })
  }
  
  showCourseModal.value = false
  message.success(`已选择课程：${course.title}`)
}

// 获取课程名称
const getCourseName = (courseId) => {
  if (!courseId) return ''
  const course = courseOptions.value.find(opt => opt.value === courseId)
  return course ? course.label : ''
}

// 课程搜索
const handleCourseSearch = () => {
  coursePagination.page = 1
  loadCourseList()
}

// 课程搜索重置
const handleCourseReset = () => {
  courseSearch.title = ''
  courseSearch.status = null
  coursePagination.page = 1
  loadCourseList()
}

// 课程分页
const handleCoursePageChange = (page) => {
  coursePagination.page = page
  loadCourseList()
}

const handleCoursePageSizeChange = (pageSize) => {
  coursePagination.pageSize = pageSize
  coursePagination.page = 1
  loadCourseList()
}

// 清除课程选择
const handleClearCourse = () => {
  searchForm.courseId = null
  searchForm.homeworkId = null
  homeworkOptions.value = []
}

const loadHomeworks = async (courseId) => {
  if (!courseId) {
    homeworkOptions.value = []
    return
  }
  
  try {
    const res = await api.getHomeworks(courseId, {
      page: 1,
      size: 100,
      status: 1 // 只获取已发布的作业
    })
    
    // 处理分页数据结构
    const data = res?.data || res
    let homeworks = []
    
    if (Array.isArray(data)) {
      homeworks = data
    } else if (data?.list || data?.records) {
      homeworks = data.list || data.records
    }
    
    homeworkOptions.value = homeworks.map(homework => ({
      label: homework.title,
      value: homework.id
    }))
  } catch (error) {
    console.error('加载作业列表失败', error)
    homeworkOptions.value = []
  }
}

// 获取指定作业的提交列表
const loadHomeworkSubmissions = async (homeworkId) => {
  if (!homeworkId) {
    return []
  }
  
  try {
    const res = await api.getHomeworkSubmissions(homeworkId)
    // 根据实际返回的数据结构处理
    const data = res?.data || res
    // 如果直接返回数组，则使用数组；否则尝试从对象中提取
    return Array.isArray(data) ? data : (data?.list || data?.records || [])
  } catch (error) {
    console.error('加载作业提交列表失败', error)
    message.error('加载作业提交列表失败')
    return []
  }
}

const handleCourseChange = (courseId) => {
  searchForm.homeworkId = null
  loadHomeworks(courseId)
}

const handleHomeworkChange = (homeworkId) => {
  // 当选择作业时，自动刷新数据
  if (homeworkId) {
    handleSearch()
  }
}

// 获取作业和课程信息用于显示
const getHomeworkAndCourseInfo = () => {
  const selectedHomework = homeworkOptions.value.find(h => h.value === searchForm.homeworkId)
  const selectedCourse = courseOptions.value.find(c => c.value === searchForm.courseId)
  
  return {
    homeworkTitle: selectedHomework?.label || '',
    courseName: selectedCourse?.label || ''
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    courseId: null,
    homeworkId: null,
    studentName: '',
    gradeStatus: null
  })
  homeworkOptions.value = []
  pagination.page = 1
  loadData()
}

const handlePageChange = (page) => {
  pagination.page = page
  loadData()
}

const handlePageSizeChange = (pageSize) => {
  pagination.pageSize = pageSize
  pagination.page = 1
  loadData()
}

const handleGrade = async (row) => {
  try {
    // 如果是通过新接口获取的数据，直接使用 row 数据
    if (searchForm.homeworkId && row.content) {
      // 构造完整的提交数据
      currentSubmission.value = {
        id: row.id,
        studentId: row.studentId,
        studentName: row.studentName,
        homeworkTitle: row.homeworkTitle,
        courseName: row.courseName,
        submitTime: row.submitTime,
        totalScore: row.totalScore,
        content: row.content,
        attachments: row.attachmentUrl ? [{ 
          id: 1, 
          name: '附件', 
          url: row.attachmentUrl 
        }] : []
      }
      
      // 初始化批改表单
      Object.assign(gradeForm, {
        score: row.score !== null && row.score !== undefined ? Number(row.score) : 0,
        teacherComment: row.teacherComment || ''
      })
      
      showGradeModal.value = true
    } else {
      // 使用原有的详情接口
      const res = await api.getSubmissionDetail(row.id)
      currentSubmission.value = res.data
      
      // 初始化批改表单
      Object.assign(gradeForm, {
        score: row.score !== null && row.score !== undefined ? Number(row.score) : 0,
        teacherComment: row.teacherComment || ''
      })
      
      showGradeModal.value = true
    }
  } catch (error) {
    message.error('获取提交详情失败')
    console.error(error)
  }
}

const handleGradeSubmit = async () => {
  try {
    await gradeFormRef.value?.validate()
    gradeLoading.value = true

    // 构造批改数据
    const gradeData = {
      score: gradeForm.score,
      teacherComment: gradeForm.teacherComment,
      gradeTime: new Date().toISOString()
    }

    await api.gradeSubmission(currentSubmission.value.id, gradeData)
    message.success('批改成功')
    
    showGradeModal.value = false
    loadData()
  } catch (error) {
    message.error(error?.message || '批改失败')
    console.error(error)
  } finally {
    gradeLoading.value = false
  }
}

const handleDownload = (file) => {
  // 下载附件
  window.open(file.url, '_blank')
}

// 生命周期
onMounted(() => {
  // 只加载课程列表，不自动加载提交数据
  loadCourses()
})
</script>

<style scoped>
.teacher-grade-management {
  padding: 16px;
}

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

/* 确保卡片内容区域布局正确 */
:deep(.n-card__content) {
  display: flex;
  flex-direction: column;
  padding: 20px;
}
</style>