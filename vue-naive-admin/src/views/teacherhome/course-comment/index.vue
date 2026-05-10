<template>
  <div class="teacher-course-comment h-full flex flex-col">
    <PageHeader class="flex-shrink-0">
      <template #title>课程评论管理</template>
      <template #extra>
        <n-button @click="handleExport" type="primary">
          <template #icon>
            <i class="i-carbon:download" />
          </template>
          导出评论数据
        </n-button>
      </template>
    </PageHeader>

    <!-- 统计卡片 -->
    <div class="flex-shrink-0 mb-4">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <n-card>
          <n-statistic label="总评论数" :value="statistics.totalComments" />
        </n-card>
        <n-card>
          <n-statistic label="平均评分" :value="statistics.averageRating" :precision="1" />
        </n-card>
        <n-card>
          <n-statistic label="总课程数" :value="statistics.totalCourses" />
        </n-card>
        <n-card>
          <n-statistic label="最近评论" :value="statistics.recentComments" />
        </n-card>
      </div>
    </div>

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
              @clear="searchForm.courseId = null"
              style="width: 200px; cursor: pointer"
            >
              <template #suffix>
                <i class="i-carbon:search" />
              </template>
            </n-input>
          </n-form-item>
          <n-form-item label="评分">
            <n-select
              v-model:value="searchForm.score"
              placeholder="请选择评分"
              :options="scoreOptions"
              clearable
              style="width: 120px"
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
          <n-form-item label="关键词">
            <n-input
              v-model:value="searchForm.keyword"
              placeholder="请输入关键词"
              clearable
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

    <!-- 评论详情弹窗 -->
    <n-modal v-model:show="showDetailModal" :mask-closable="false">
      <n-card
        style="width: 700px"
        title="评论详情"
        :bordered="false"
        size="huge"
        role="dialog"
        aria-modal="true"
      >
        <template #header-extra>
          <n-button quaternary circle @click="showDetailModal = false">
            <template #icon>
              <i class="i-carbon:close" />
            </template>
          </n-button>
        </template>
        
        <div v-if="selectedComment">
          <n-descriptions :column="2" bordered>
            <n-descriptions-item label="评论用户">
              {{ selectedComment.nickname }}
            </n-descriptions-item>
            <n-descriptions-item label="课程名称">
              {{ selectedComment.courseName || '未知课程' }}
            </n-descriptions-item>
            <n-descriptions-item label="评分">
              <n-rate :value="selectedComment.score" readonly />
            </n-descriptions-item>
            <n-descriptions-item label="评论时间">
              {{ new Date(selectedComment.createTime).toLocaleString('zh-CN') }}
            </n-descriptions-item>
            <n-descriptions-item label="状态">
              <n-tag :type="selectedComment.status === 1 ? 'success' : 'error'">
                {{ selectedComment.status === 1 ? '显示' : '隐藏' }}
              </n-tag>
            </n-descriptions-item>

          </n-descriptions>

          <div class="mt-4">
            <h4 class="font-semibold mb-2">评论内容</h4>
            <n-card>
              <p class="whitespace-pre-wrap">{{ selectedComment.content }}</p>
            </n-card>
          </div>


        </div>
      </n-card>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h } from 'vue'
import { NButton, NTag, NRate, useMessage } from 'naive-ui'
import { PageHeader } from '@/components'
import api from './api'

const message = useMessage()

// 响应式数据
const loading = ref(false)
const showDetailModal = ref(false)
const tableData = ref([])
const courseOptions = ref([])
const selectedComment = ref(null)

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

// 统计数据
const statistics = reactive({
  totalComments: 0,
  averageRating: 0,
  totalCourses: 0,
  recentComments: 0,
  fiveStarComments: 0,
  fourStarComments: 0,
  badComments: 0
})

// 搜索表单
const searchForm = reactive({
  courseId: null,
  score: null,
  status: null,
  keyword: ''
})



// 选项配置
const scoreOptions = [
  { label: '5星', value: 5 },
  { label: '4星', value: 4 },
  { label: '3星', value: 3 },
  { label: '2星', value: 2 },
  { label: '1星', value: 1 }
]

const statusOptions = [
  { label: '显示', value: 1 },
  { label: '隐藏', value: 0 }
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

// 表格列配置
const columns = [
  {
    title: 'ID',
    key: 'id',
    width: 80
  },
  {
    title: '评论用户',
    key: 'nickname',
    width: 120
  },
  {
    title: '课程名称',
    key: 'courseName',
    width: 150,
    ellipsis: {
      tooltip: true
    },
    render(row) {
      return row.courseName || '未知课程'
    }
  },
  {
    title: '评分',
    key: 'score',
    width: 120,
    render(row) {
      return h(NRate, {
        value: row.score,
        readonly: true,
        size: 'small'
      })
    }
  },
  {
    title: '评论内容',
    key: 'content',
    width: 200,
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render(row) {
      return h(NTag, {
        type: row.status === 1 ? 'success' : 'error'
      }, {
        default: () => row.status === 1 ? '显示' : '隐藏'
      })
    }
  },

  {
    title: '评论时间',
    key: 'createTime',
    width: 180,
    render(row) {
      return new Date(row.createTime).toLocaleString('zh-CN')
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    render(row) {
      return [
        h(NButton, {
          size: 'small',
          type: 'primary',
          onClick: () => handleViewDetail(row)
        }, { default: () => '查看详情' })
      ]
    }
  }
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

// 方法
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.pageSize,
      ...searchForm
    }
    const response = await api.getMyComments(params)
    tableData.value = response.data.list
    pagination.itemCount = response.data.total
  } catch (error) {
    message.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const response = await api.getMyRatingStatistics()
    Object.assign(statistics, response.data)
  } catch (error) {
    message.error('加载统计数据失败')
  }
}

const loadCourseOptions = async () => {
  try {
    const response = await api.getMyCourses()
    console.log('课程数据:', response) // 调试日志
    
    // 根据实际返回的数据结构处理
    let courses = []
    
    // 检查不同的数据结构
    if (response?.data) {
      if (Array.isArray(response.data)) {
        // 如果 response.data 是数组
        courses = response.data
      } else if (response.data.list || response.data.records) {
        // 如果是分页对象
        courses = response.data.list || response.data.records
      }
    } else if (Array.isArray(response)) {
      // 如果直接返回数组
      courses = response
    }

    console.log('课程数据:', courses) // 调试日志
    
    courseOptions.value = courses.map(course => ({
      label: course.title,
      value: course.id
    }))
    
    console.log('课程选项:', courseOptions.value) // 调试日志
  } catch (error) {
    console.error('加载课程列表失败', error)
    message.error('加载课程列表失败')
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
    const response = await api.getMyCourses(params)
    const data = response?.data || response || {}
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
    score: null,
    status: null,
    keyword: ''
  })
  pagination.page = 1
  loadData()
}

const handleViewDetail = async (row) => {
  try {
    const response = await api.getCommentDetail(row.id)
    selectedComment.value = {
      ...response.data,
      courseName: response.data.courseName || row.courseName
    }
    showDetailModal.value = true
  } catch (error) {
    message.error('加载评论详情失败')
  }
}

const handleExport = async () => {
  try {
    // 必须先选择课程
    if (!searchForm.courseId) {
      message.warning('请先选择课程')
      return
    }

    // 调用后端接口：GET /api/teacher/comment/export/{courseId}
    const courseId = searchForm.courseId
    const blob = await api.exportComments(courseId)

    // 创建并触发下载
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `课程评论_${courseId}_${Date.now()}.xlsx`
    document.body.appendChild(link)
    link.click()

    // 清理
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    message.success('导出成功')
  } catch (error) {
    message.error('导出失败')
  }
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

// 生命周期
onMounted(() => {
  loadData()
  loadStatistics()
  loadCourseOptions()
})
</script>

<style scoped>
.teacher-course-comment {
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