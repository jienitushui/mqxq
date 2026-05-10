<template>
  <div class="my-course-students h-full flex flex-col">
    <PageHeader class="flex-shrink-0">
      <template #title>我的学员</template>
      <template #extra>
        <n-button @click="handleExport" type="primary">
          <template #icon>
            <i class="i-carbon:download" />
          </template>
          导出学员列表
        </n-button>
      </template>
    </PageHeader>

    <!-- 统计卡片 -->
    <div class="flex-shrink-0 mb-4">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <n-card>
          <n-statistic label="总学员数" :value="statistics.totalStudents" />
        </n-card>
        <n-card>
          <n-statistic label="总课程数" :value="statistics.totalCourses" suffix="%" />
        </n-card>
        <n-card>
          <n-statistic label="课程评分" :value="statistics.averageRating" suffix="/5" />
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
          <n-form-item label="学员昵称">
            <n-input
              v-model:value="searchForm.studentName"
              placeholder="请输入学员昵称"
              clearable
              @keyup.enter="handleSearch"
            />
          </n-form-item>
          <n-form-item label="学习状态">
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

    <!-- 学员详情弹窗 -->
    <n-modal v-model:show="showDetailModal" :mask-closable="false">
      <n-card
        style="width: 800px"
        title="学员学习详情"
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
        
        <div v-if="selectedStudent">
          <n-descriptions :column="2" bordered>
            <n-descriptions-item label="学员昵称">
              {{ selectedStudent?.userNickname || '未知用户' }}
            </n-descriptions-item>
            <n-descriptions-item label="课程名称">
              {{ selectedStudent?.courseName || '未知课程' }}
            </n-descriptions-item>
            <n-descriptions-item label="学习状态">
              <n-tag :type="getStatusType(selectedStudent?.status)">
                {{ getStatusText(selectedStudent?.status) }}
              </n-tag>
            </n-descriptions-item>
            <n-descriptions-item label="用户ID">
              {{ selectedStudent?.userId || '无' }}
            </n-descriptions-item>
            <n-descriptions-item label="课程ID">
              {{ selectedStudent?.courseId || '无' }}
            </n-descriptions-item>
            <n-descriptions-item label="订单ID">
              {{ selectedStudent?.orderId || '无' }}
            </n-descriptions-item>
            <n-descriptions-item label="创建时间">
              {{ selectedStudent?.createTime ? new Date(selectedStudent.createTime).toLocaleString() : '无' }}
            </n-descriptions-item>
            <n-descriptions-item label="更新时间">
              {{ selectedStudent?.updateTime ? new Date(selectedStudent.updateTime).toLocaleString() : '无' }}
            </n-descriptions-item>
          </n-descriptions>


        </div>
      </n-card>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h, computed } from 'vue'
import { NButton, NTag, NProgress, useMessage } from 'naive-ui'
import { PageHeader } from '@/components'
import api from './api'

const message = useMessage()

// 响应式数据
const loading = ref(false)
const showDetailModal = ref(false)
const tableData = ref([])
const courseOptions = ref([])
const selectedStudent = ref(null)

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
  totalStudents: 0,
  totalCourses: 0,
  averageRating: 0
})

// 搜索表单
const searchForm = reactive({
  courseId: null,
  studentName: '',
  status: null
})

// 状态选项
const statusOptions = [
  // 后端约定：0-已加入，1-学习中，2-已完成
  { label: '已加入', value: 0 },
  { label: '学习中', value: 1 },
  { label: '已完成', value: 2 }
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
    title: '学员头像',
    key: 'userAvatar',
    width: 80,
    render(row) {
      return h('img', {
        src: row.userAvatar,
        alt: row.userNickname,
        style: {
          width: '40px',
          height: '40px',
          borderRadius: '50%',
          objectFit: 'cover'
        }
      })
    }
  },
  {
    title: '学员昵称',
    key: 'userNickname',
    ellipsis: {
      tooltip: true
    },
    render(row) {
      return row.userNickname || '未知用户'
    }
  },
  {
    title: '课程名称',
    key: 'courseName',
    ellipsis: {
      tooltip: true
    },
    render(row) {
      return row.courseName || '未知课程'
    }
  },

  {
    title: '学习状态',
    key: 'status',
    width: 100,
    render(row) {
      return h(NTag, {
        type: getStatusType(row.status)
      }, {
        default: () => getStatusText(row.status)
      })
    }
  },
  {
    title: '创建时间',
    key: 'createTime',
    width: 180,
    render(row) {
      return row.createTime ? new Date(row.createTime).toLocaleString() : ''
    }
  },
  {
    title: '更新时间',
    key: 'updateTime',
    width: 180,
    render(row) {
      return row.updateTime ? new Date(row.updateTime).toLocaleString() : ''
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    render(row) {
      return [
        h(NButton, {
          size: 'small',
          type: 'primary',
          style: { marginRight: '8px' },
          onClick: () => handleViewDetail(row)
        }, { default: () => '查看详情' }),
        h(NButton, {
          size: 'small',
          type: 'error',
          onClick: () => handleRemoveStudent(row.id)
        }, { default: () => '移除' })
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
    const response = await api.getStudentsList(params)
    
    console.log('API响应数据:', response) // 调试日志
    
    // 处理API返回的数据格式
    let studentsList = []
    let total = 0
    
    if (Array.isArray(response)) {
      // 如果API直接返回数组（如你提供的示例数据）
      studentsList = response
      total = response.length
    } else if (response && response.data) {
      // 如果返回分页包装对象
      if (Array.isArray(response.data)) {
        studentsList = response.data
        total = response.data.length
      } else {
        studentsList = response.data.list || response.data.records || []
        total = response.data.total || response.data.size || studentsList.length
      }
    } else {
      // 如果response本身就是数据
      studentsList = response || []
      total = studentsList.length
    }
    
    console.log('处理后的学员列表:', studentsList) // 调试日志
    
    // 如果数据中没有userNickname和courseName，通过ID查询补充
    const enrichedStudentsList = await Promise.all(
      studentsList.map(async (student) => {
        const enrichedStudent = { ...student }
        

        return enrichedStudent
      })
    )
    
    tableData.value = enrichedStudentsList
    pagination.itemCount = total
    
  } catch (error) {
    console.error('加载学员数据失败:', error)
    message.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const response = await api.getMyStatistics()
    // 根据API返回的数据格式 {averageRating: 5, totalStudents: 1, totalCourses: 1}
    // console.log(response.data)
    Object.assign(statistics, {
      totalStudents: response.data.totalStudents || 0,
      totalCourses: response.data.totalCourses || 0,
      averageRating: response.data.averageRating || 0
    })
  } catch (error) {
    message.error('加载统计数据失败')
    // 使用提供的默认数据作为fallback
    Object.assign(statistics, {
      totalStudents: 1,
      totalCourses: 1,
      averageRating: 5
    })
  }
}

const loadCourseOptions = async () => {
  try {
    const response = await api.getMyCourses()
    // API已经处理了分页格式，直接返回课程数组
    courseOptions.value = response.map(course => ({
      label: course.title,
      value: course.id
    }))
  } catch (error) {
    message.error('加载课程列表失败')
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
    courseList.value = response || []
    coursePagination.itemCount = response.length || 0
    
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
    studentName: '',
    status: null
  })
  pagination.page = 1
  loadData()
}

const handleViewDetail = async (row) => {
  try {
    console.log('查看详情 - 行数据:', row) // 调试日志
    
    let detailData = { ...row }
    
    // 尝试获取API详细信息
    try {
      const response = await api.getStudentDetail(row.id)
      // console.log('API详情响应:', response) // 调试日志
      detailData = {
        ...detailData,
        ...response.data
      }
    } catch (apiError) {
      // console.log('API调用失败，使用行数据:', apiError) // 调试日志
    }
    
    selectedStudent.value = detailData
    console.log('最终选中的学员数据:', selectedStudent.value) // 调试日志
    showDetailModal.value = true
  } catch (error) {
    console.error('处理学员详情失败:', error)
    message.error('加载学员详情失败')
  }
}

const handleRemoveStudent = async (id) => {
  try {
    await api.removeStudent(id)
    message.success('移除学员成功')
    loadData()
    loadStatistics()
  } catch (error) {
    message.error('移除学员失败')
  }
}

const handleExport = async () => {
  if (!searchForm.courseId) {
    message.warning('请先选择要导出的课程')
    return
  }
  try {
    const { courseId, ...params } = searchForm
    const blob = await api.exportStudents(courseId, params)
    
    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `学员列表_${courseId}_${Date.now()}.xlsx`
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

// 工具方法
const formatDuration = (seconds) => {
  if (!seconds) return '0分钟'
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  }
  return `${minutes}分钟`
}

const getStatusType = (status) => {
  const types = {
    0: 'default', // 已加入
    1: 'info',    // 学习中
    2: 'success'  // 已完成
  }
  return types[status] || 'default'
}

const getStatusText = (status) => {
  const texts = {
    0: '已加入',
    1: '学习中',
    2: '已完成'
  }
  return texts[status] || '未知状态'
}

// 生命周期
onMounted(() => {
  loadData()
  loadStatistics()
  loadCourseOptions()
})
</script>

<style scoped>
.my-course-students {
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