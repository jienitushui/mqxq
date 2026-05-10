<template>
  <div class="teacher-homework-management h-full flex flex-col">
    <PageHeader class="flex-shrink-0">
      <template #title>作业管理</template>
      <template #extra>
        <n-space>
          <n-button @click="handleExport" type="primary">
            <template #icon>
              <i class="i-carbon:download" />
            </template>
            导出作业数据
          </n-button>
          <n-button type="primary" @click="handleAdd">
            <template #icon>
              <i class="i-carbon:add" />
            </template>
            发布作业</n-button>
        </n-space>
      </template>
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
              @click="openCourseSelector('search')"
              @clear="searchForm.courseId = null"
              style="width: 200px; cursor: pointer"
            >
              <template #suffix>
                <i class="i-carbon:search" />
              </template>
            </n-input>
          </n-form-item>
          <n-form-item label="作业标题">
            <n-input
              v-model:value="searchForm.title"
              placeholder="请输入作业标题"
              clearable
              @keyup.enter="handleSearch"
            />
          </n-form-item>
          <n-form-item label="状态">
            <n-select
              v-model:value="searchForm.status"
              placeholder="请选择状态"
              :options="statusOptions"
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
          :scroll-x="1600"
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

    <!-- 新增/编辑弹窗 -->
    <n-modal v-model:show="showModal" :mask-closable="false">
      <n-card
        style="width: 800px"
        :title="modalTitle"
        :bordered="false"
        size="huge"
        role="dialog"
        aria-modal="true"
      >
        <template #header-extra>
          <n-button quaternary circle @click="showModal = false">
            <template #icon>
              <i class="i-carbon:close" />
            </template>
          </n-button>
        </template>
        
        <n-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-placement="left"
          label-width="80px"
          require-mark-placement="right-hanging"
        >
          <n-form-item label="课程" path="courseId">
            <n-input
              :value="getCourseName(formData.courseId)"
              placeholder="请选择课程"
              readonly
              @click="openCourseSelector('form')"
              style="cursor: pointer"
            >
              <template #suffix>
                <i class="i-carbon:search" />
              </template>
            </n-input>
          </n-form-item>
          <n-form-item label="作业标题" path="title">
            <n-input v-model:value="formData.title" placeholder="请输入作业标题" />
          </n-form-item>
          <n-form-item label="作业内容" path="content">
            <n-input
              v-model:value="formData.content"
              type="textarea"
              placeholder="请输入作业内容"
              :rows="4"
            />
          </n-form-item>
          <n-form-item label="参考答案" path="answer">
            <n-input
              v-model:value="formData.answer"
              type="textarea"
              placeholder="请输入参考答案"
              :rows="3"
            />
          </n-form-item>
          <n-form-item label="总分值" path="score">
            <n-input-number
              v-model:value="formData.score"
              placeholder="请输入总分值"
              :min="1"
            />
          </n-form-item>
          <n-form-item label="开始时间" path="startTime">
            <n-date-picker
              v-model:value="formData.startTime"
              type="datetime"
              placeholder="请选择开始时间"
              clearable
            />
          </n-form-item>
          <n-form-item label="截止时间" path="endTime">
            <n-date-picker
              v-model:value="formData.endTime"
              type="datetime"
              placeholder="请选择截止时间"
              clearable
            />
          </n-form-item>
          <n-form-item label="状态" path="status">
            <n-select
              v-model:value="formData.status"
              placeholder="请选择状态"
              :options="statusOptions"
            />
          </n-form-item>
        </n-form>

        <template #footer>
          <div class="flex justify-end space-x-2">
            <n-button @click="showModal = false">取消</n-button>
            <n-button type="primary" :loading="submitLoading" @click="handleSubmit">
              确定</n-button>
          </div>
        </template>
      </n-card>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h, computed } from 'vue'
import { NButton, NTag, NPopconfirm, useMessage } from 'naive-ui'
import { useRouter } from 'vue-router'
import { PageHeader } from '@/components'
import api from './api'

const message = useMessage()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const showModal = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const formRef = ref(null)
const courseOptions = ref([])

// 课程选择弹窗相关
const showCourseModal = ref(false)
const courseLoading = ref(false)
const courseList = ref([])
const courseSelectorType = ref('search') // 'search' 或 'form'

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
  title: '',
  status: null
})

// 状态选项
const statusOptions = [
  { label: '未发布', value: 0 },
  { label: '已发布', value: 1 }
]

// 课程状态选项
const courseStatusOptions = [
  { label: '全部', value: null },
  { label: '未发布', value: 0 },
  { label: '已发布', value: 1 }
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

// 表单数据
const formData = reactive({
  id: null,
  courseId: null,
  title: '',
  content: '',
  answer: '',
  score: 100,
  startTime: null,
  endTime: null,
  status: 0
})

// 表单验证规则
const formRules = {
  courseId: [
    { required: true, message: '请选择课程', trigger: 'change', type: 'number' }
  ],
  title: [
    { required: true, message: '请输入作业标题', trigger: 'blur' },
    { min: 2, max: 200, message: '作业标题长度应在2-200个字符之间', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入作业内容', trigger: 'blur' }
  ],
  score: [
    { required: true, message: '请输入总分值', trigger: 'blur', type: 'number' },
    { type: 'number', min: 1, message: '总分值不能小于1', trigger: 'blur' }
  ],
  startTime: [
    { type: 'number', required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { type: 'number', required: true, message: '请选择截止时间', trigger: 'change' }
  ]
}

// 计算属性
const modalTitle = computed(() => formData.id ? '编辑作业' : '发布作业')

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
    width: 80,
    fixed: 'left'
  },
  {
    title: '课程ID',
    key: 'courseId',
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
    title: '作业内容',
    key: 'content',
    width: 200,
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '参考答案',
    key: 'answer',
    width: 150,
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '总分值',
    key: 'score',
    width: 80
  },
  {
    title: '开始时间',
    key: 'startTime',
    width: 150,
    render(row) {
      return formatDateTime(row.startTime)
    }
  },
  {
    title: '截止时间',
    key: 'endTime',
    width: 150,
    render(row) {
      return formatDateTime(row.endTime)
    }
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render(row) {
      return h(NTag, {
        type: row.status === 1 ? 'success' : 'warning',
        size: 'small'
      }, {
        default: () => row.status === 1 ? '已发布' : '未发布'
      })
    }
  },
  {
    title: '创建人ID',
    key: 'createUser',
    width: 100
  },
  {
    title: '创建时间',
    key: 'createTime',
    width: 150,
    render(row) {
      return formatDateTime(row.createTime)
    }
  },
  {
    title: '修改人ID',
    key: 'updateUser',
    width: 100
  },
  {
    title: '更新时间',
    key: 'updateTime',
    width: 150,
    render(row) {
      return formatDateTime(row.updateTime)
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    fixed: 'right',
    render(row) {
      return h('div', { class: 'flex space-x-2' }, [
        h(
          NButton,
          {
            size: 'small',
            type: 'primary',
            secondary: true,
            onClick: () => handleEdit(row)
          },
          { default: () => '编辑' }
        ),
        h(
          NButton,
          {
            size: 'small',
            type: 'info',
            secondary: true,
            onClick: () => handleViewSubmissions(row)
          },
          { default: () => '查看提交' }
        ),
        h(
          NPopconfirm,
          {
            onPositiveClick: () => handleDelete(row.id)
          },
          {
            default: () => '确定删除这个作业吗？',
            trigger: () => h(
              NButton,
              {
                size: 'small',
                type: 'error',
                secondary: true
              },
              { default: () => '删除' }
            )
          }
        )
      ])
    }
  }
]

// 方法
const formatDateTime = (timestamp) => {
  if (!timestamp) return '-'
  const date = new Date(timestamp)
  const Y = date.getFullYear()
  const M = (date.getMonth() + 1).toString().padStart(2, '0')
  const D = date.getDate().toString().padStart(2, '0')
  const h = date.getHours().toString().padStart(2, '0')
  const m = date.getMinutes().toString().padStart(2, '0')
  const s = date.getSeconds().toString().padStart(2, '0')
  return `${Y}-${M}-${D} ${h}:${m}:${s}`
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.pageSize,
      ...searchForm
    }
    const res = await api.getList(params)
    const data = res?.data || res || {}
    tableData.value = data.list || data.records || []
    pagination.itemCount = data.total || data.totalCount || 0
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
    // The API returns the course list directly in the `data` property
    courseOptions.value = (res.data || []).map(course => ({
      label: course.title,
      value: course.id,
    }))
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
    courseList.value = data.list || data.records || data || []
    coursePagination.itemCount = data.total || data.totalCount || courseList.value.length
    
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
const openCourseSelector = (type) => {
  courseSelectorType.value = type
  courseSearch.title = ''
  courseSearch.status = null
  coursePagination.page = 1
  showCourseModal.value = true
  loadCourseList()
}

// 选择课程
const handleSelectCourse = (course) => {
  if (courseSelectorType.value === 'search') {
    searchForm.courseId = course.id
  } else {
    formData.courseId = course.id
  }
  
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

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    courseId: null,
    title: '',
    status: null
  })
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

const handleAdd = () => {
  Object.assign(formData, {
    id: null,
    courseId: null,
    title: '',
    content: '',
    answer: '',
    score: 100,
    startTime: null,
    endTime: null,
    status: 0
  })
  showModal.value = true
}

const handleEdit = async (row) => {
  try {
    const res = await api.getDetail(row.id)
    const data = res.data
    // Convert date strings to timestamps for the date picker
    if (data.startTime) {
      data.startTime = new Date(data.startTime).getTime()
    }
    if (data.endTime) {
      data.endTime = new Date(data.endTime).getTime()
    }
    Object.assign(formData, data)
    showModal.value = true
  } catch (error) {
    message.error('获取作业详情失败')
    console.error(error)
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitLoading.value = true

    if (formData.id) {
      await api.update(formData.id, formData)
      message.success('更新成功')
    } else {
      await api.create(formData)
      message.success('发布成功')
    }
    
    showModal.value = false
    loadData()
  } catch (error) {
    message.error(error?.message || '操作失败')
    console.error(error)
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await api.delete(id)
    message.success('删除成功')
    loadData()
  } catch (error) {
    message.error('删除失败')
    console.error(error)
  }
}

const handleViewSubmissions = (row) => {
  // 跳转到作业提交管理页面，并传递作业ID
  router.push({
    path: '/teacherhome/submission-manage',
    query: {
      homeworkId: row.id
    }
  })
}

const handleExport = async () => {
  try {
    const params = {}
    if (searchForm.courseId) {
      params.courseId = searchForm.courseId
    }
    if (searchForm.status !== null && searchForm.status !== undefined) {
      params.status = searchForm.status
    }
    
    const blob = await api.exportHomework(params)
    
    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    const fileName = `作业数据_${searchForm.courseId || '全部'}_${Date.now()}.xlsx`
    link.download = fileName
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

// 生命周期
onMounted(() => {
  loadData()
  loadCourses()
})
</script>

<style scoped>
.teacher-homework-management {
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