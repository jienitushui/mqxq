<template>
  <div class="homework-management h-full flex flex-col">
    <PageHeader class="flex-shrink-0">
      <template #title>作业管理</template>
      <template #extra>
        <n-button @click="handleBatchDelete" :disabled="!selectedRowKeys.length" type="error" style="margin-right: 8px">
          <template #icon>
            <i class="i-carbon:trash-can" />
          </template>
          批量删除
        </n-button>
        <n-button @click="handleBatchPublish" :disabled="!selectedRowKeys.length" type="primary" style="margin-right: 8px">
          <template #icon>
            <i class="i-carbon:checkmark" />
          </template>
          批量发布
        </n-button>
        <n-button @click="handleExport" type="primary">
          <template #icon>
            <i class="i-carbon:download" />
          </template>
          导出数据
        </n-button>
      </template>
    </PageHeader>

    <!-- 统计卡片 -->
    <div class="flex-shrink-0 mb-4">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <n-card>
          <n-statistic label="总作业数" :value="statistics.totalHomework || 0">
            <template #prefix>
              <n-icon color="#2080f0">
                <i class="i-carbon:task" />
              </n-icon>
            </template>
          </n-statistic>
        </n-card>
        <n-card>
          <n-statistic label="已发布作业" :value="statistics.publishedHomework || 0">
            <template #prefix>
              <n-icon color="#18a058">
                <i class="i-carbon:checkmark-outline" />
              </n-icon>
            </template>
          </n-statistic>
        </n-card>
        <n-card>
          <n-statistic label="今日作业" :value="statistics.todayHomework || 0">
            <template #prefix>
              <n-icon color="#18a058">
                <i class="i-carbon:calendar" />
              </n-icon>
            </template>
          </n-statistic>
        </n-card>
        <n-card>
          <n-statistic label="草稿作业" :value="statistics.draftHomework || 0">
            <template #prefix>
              <n-icon color="#909399">
                <i class="i-carbon:document" />
              </n-icon>
            </template>
          </n-statistic>
        </n-card>
      </div>
    </div>

    <n-card class="flex-1 flex flex-col overflow-hidden">
      <!-- 搜索区域 -->
      <div class="flex-shrink-0 mb-4">
        <n-form inline :model="searchForm" label-placement="left">
          <n-form-item label="课程">
            <n-input
              :value="searchForm.courseName"
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
          <n-form-item label="教师">
            <n-input
              :value="getTeacherName(searchForm.teacherId)"
              placeholder="请选择教师"
              readonly
              clearable
              @click="openTeacherSelector"
              @clear="searchForm.teacherId = null"
              style="width: 150px; cursor: pointer"
            >
              <template #suffix>
                <i class="i-carbon:search" />
              </template>
            </n-input>
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
          <n-form-item label="作业标题">
            <n-input
              v-model:value="searchForm.title"
              placeholder="请输入作业标题"
              clearable
              @keyup.enter="handleSearch"
            />
          </n-form-item>
          <n-form-item>
            <n-button type="primary" @click="handleSearch">搜索</n-button>
            <n-button @click="handleReset" style="margin-left: 8px">重置</n-button>
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
          v-model:checked-row-keys="selectedRowKeys"
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
        style="width: 900px"
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
        
        <!-- 课程搜索 -->
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
          :data="courseTableData"
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

    <!-- 教师选择弹窗 -->
    <n-modal v-model:show="showTeacherModal" :mask-closable="false">
      <n-card
        style="width: 800px"
        title="选择教师"
        :bordered="false"
        size="huge"
        role="dialog"
        aria-modal="true"
      >
        <template #header-extra>
          <n-button quaternary circle @click="showTeacherModal = false">
            <template #icon>
              <i class="i-carbon:close" />
            </template>
          </n-button>
        </template>
        
        <!-- 搜索区域 -->
        <div class="mb-4">
          <n-form inline>
            <n-form-item label="教师姓名">
              <n-input
                v-model:value="teacherSearch.keyword"
                placeholder="请输入教师姓名"
                clearable
                @keyup.enter="handleTeacherSearch"
                style="width: 200px"
              />
            </n-form-item>
            <n-form-item>
              <n-button type="primary" @click="handleTeacherSearch">
                <template #icon>
                  <i class="i-carbon:search" />
                </template>
                搜索
              </n-button>
              <n-button @click="handleTeacherReset" style="margin-left: 8px">
                <template #icon>
                  <i class="i-carbon:reset" />
                </template>
                重置
              </n-button>
            </n-form-item>
          </n-form>
        </div>

        <!-- 教师列表 -->
        <n-data-table
          :columns="teacherColumns"
          :data="teacherList"
          :loading="teacherLoading"
          :pagination="teacherPagination"
          :row-key="row => row.id"
          @update:page="handleTeacherPageChange"
          @update:page-size="handleTeacherPageSizeChange"
          remote
          :max-height="400"
          striped
          size="small"
        >
          <template #empty>
            <n-empty description="暂无教师数据" />
          </template>
        </n-data-table>
      </n-card>
    </n-modal>

    <!-- 作业详情弹窗 -->
    <n-modal v-model:show="showDetailModal" :mask-closable="false">
      <n-card
        style="width: 800px"
        title="作业详情"
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
        
        <div v-if="selectedHomework">
          <n-descriptions :column="2" bordered>
            <n-descriptions-item label="作业标题">
              {{ selectedHomework.title }}
            </n-descriptions-item>
            <!-- <n-descriptions-item label="课程名称">
              {{ selectedHomework.courseName || '未知课程' }}
            </n-descriptions-item>
            <n-descriptions-item label="教师姓名">
              {{ selectedHomework.teacherName || '未知教师' }}
            </n-descriptions-item> -->
            <n-descriptions-item label="总分">
              {{ selectedHomework.score }}
            </n-descriptions-item>
            <n-descriptions-item label="开始时间">
              {{ selectedHomework.startTime ? new Date(selectedHomework.startTime).toLocaleString('zh-CN') : '-' }}
            </n-descriptions-item>
            <n-descriptions-item label="截止时间">
              {{ selectedHomework.endTime ? new Date(selectedHomework.endTime).toLocaleString('zh-CN') : '-' }}
            </n-descriptions-item>
            <n-descriptions-item label="发布状态">
              <n-tag :type="selectedHomework.status === 1 ? 'success' : 'warning'">
                {{ selectedHomework.status === 1 ? '已发布' : '未发布' }}
              </n-tag>
            </n-descriptions-item>
            <!-- <n-descriptions-item label="提交数量">
              {{ selectedHomework.submissionCount || 0 }}
            </n-descriptions-item> -->
            <n-descriptions-item label="创建时间">
              {{ selectedHomework.createTime ? new Date(selectedHomework.createTime).toLocaleString('zh-CN') : '-' }}
            </n-descriptions-item>
            <n-descriptions-item label="更新时间">
              {{ selectedHomework.updateTime ? new Date(selectedHomework.updateTime).toLocaleString('zh-CN') : '-' }}
            </n-descriptions-item>
          </n-descriptions>

          <div class="mt-4">
            <h4 class="font-semibold mb-2">作业内容</h4>
            <n-card>
              <div class="whitespace-pre-wrap">{{ selectedHomework.content || '暂无内容' }}</div>
            </n-card>
          </div>

          <div v-if="selectedHomework.answer" class="mt-4">
            <h4 class="font-semibold mb-2">参考答案</h4>
            <n-card>
              <div class="whitespace-pre-wrap">{{ selectedHomework.answer }}</div>
            </n-card>
          </div>
        </div>
      </n-card>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h } from 'vue'
import { NButton, NTag, NPopconfirm, useMessage } from 'naive-ui'
import { PageHeader } from '@/components'
import api from './api'

const message = useMessage()

// 响应式数据
const loading = ref(false)
const showDetailModal = ref(false)
const tableData = ref([])
const courseOptions = ref([])
const teacherOptions = ref([]) // 用于缓存已选择的教师信息
const selectedRowKeys = ref([])
const selectedHomework = ref(null)

// 课程选择弹窗相关
const showCourseModal = ref(false)
const courseLoading = ref(false)
const courseTableData = ref([])

// 课程搜索表单
const courseSearch = reactive({
  title: '',
  status: 1
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

// 教师选择弹窗相关
const showTeacherModal = ref(false)
const teacherLoading = ref(false)
const teacherList = ref([])

// 教师搜索表单
const teacherSearch = reactive({
  keyword: ''
})

// 教师分页配置
const teacherPagination = reactive({
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
  todayHomework: 0,
  draftHomework: 0,
  totalHomework: 0,
  publishedHomework: 0
})

// 搜索表单
const searchForm = reactive({
  courseId: null,
  courseName: '',
  teacherId: null,
  status: null,
  title: ''
})

// 状态选项
const statusOptions = [
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
      return row.status === 1 ? '启用' : '禁用'
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

// 教师表格列配置
const teacherColumns = [
  {
    title: 'ID',
    key: 'id',
    width: 80
  },
  {
    title: '教师姓名',
    key: 'name',
    render(row) {
      return row.name || row.username || '-'
    }
  },
  {
    title: '用户名',
    key: 'username',
    ellipsis: {
      tooltip: true
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
          onClick: () => handleSelectTeacher(row)
        },
        { default: () => '选择' }
      )
    }
  }
]

// 表格列配置
const columns = [
  {
    type: 'selection'
  },
  {
    title: 'ID',
    key: 'id',
    width: 80
  },
  {
    title: '作业标题',
    key: 'title',
    width: 80,
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
    },
    render(row) {
      return row.courseName || '未知课程'
    }
  },
  {
    title: '教师姓名',
    key: 'teacherName',
    width: 120,
    render(row) {
      return row.teacherName || '未知教师'
    }
  },
  {
    title: '总分',
    key: 'score',
    width: 80
  },
  {
    title: '发布状态',
    key: 'status',
    width: 100,
    render(row) {
      return h(NTag, {
        type: row.status === 1 ? 'success' : 'warning'
      }, {
        default: () => row.status === 1 ? '已发布' : '未发布'
      })
    }
  },
  {
    title: '开始时间',
    key: 'startTime',
    width: 180,
    render(row) {
      return row.startTime ? new Date(row.startTime).toLocaleString('zh-CN') : '-'
    }
  },
  {
    title: '截止时间',
    key: 'endTime',
    width: 180,
    render(row) {
      return row.endTime ? new Date(row.endTime).toLocaleString('zh-CN') : '-'
    }
  },
  // {
  //   title: '提交数',
  //   key: 'submissionCount',
  //   width: 80,
  //   render(row) {
  //     return row.submissionCount || 0
  //   }
  // },
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
          type: row.status === 1 ? 'warning' : 'success',
          style: { marginRight: '8px' },
          onClick: () => handleToggleStatus(row)
        }, { default: () => row.status === 1 ? '取消发布' : '强制发布' }),
        h(NPopconfirm, {
          onPositiveClick: () => handleDelete(row.id)
        }, {
          default: () => '确定删除这个作业吗？',
          trigger: () => h(NButton, {
            size: 'small',
            type: 'error'
          }, { default: () => '删除' })
        })
      ]
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
    const response = await api.getHomeworkList(params)
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
    const response = await api.getHomeworkStatistics()
    Object.assign(statistics, response.data)
  } catch (error) {
    message.error('加载统计数据失败')
  }
}

const loadOptions = async () => {
  // 不再需要预加载课程选项，改为按需加载
}

// 加载课程列表（用于弹窗选择）
const loadCourseList = async () => {
  courseLoading.value = true
  try {
    const params = {
      page: coursePagination.page,
      size: coursePagination.pageSize,
      ...courseSearch
    }
    const res = await api.getCourseOptions(params)
    const data = res?.data || res || {}
    courseTableData.value = data.list || data.records || []
    coursePagination.itemCount = data.total || data.totalCount || 0
    
    // 同时更新 courseOptions 用于显示名称
    courseTableData.value.forEach(course => {
      const existingOption = courseOptions.value.find(opt => opt.value === course.id)
      if (!existingOption) {
        courseOptions.value.push({
          label: course.title,
          value: course.id
        })
      }
    })
  } catch (error) {
    console.error('加载课程列表失败', error)
    message.error('加载课程列表失败')
    courseTableData.value = []
    coursePagination.itemCount = 0
  } finally {
    courseLoading.value = false
  }
}

// 打开课程选择器
const openCourseSelector = () => {
  courseSearch.title = ''
  courseSearch.status = 1
  coursePagination.page = 1
  showCourseModal.value = true
  loadCourseList()
}

// 选择课程
const handleSelectCourse = (course) => {
  searchForm.courseId = course.id
  searchForm.courseName = course.title
  
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

// 清空课程选择
const handleClearCourse = () => {
  searchForm.courseId = null
  searchForm.courseName = ''
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
  courseSearch.status = 1
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

// 加载教师列表（用于弹窗选择）
const loadTeacherList = async () => {
  teacherLoading.value = true
  try {
    const params = {
      pageNum: teacherPagination.page,
      pageSize: teacherPagination.pageSize,
      keyword: teacherSearch.keyword || ''
    }
    const res = await api.getTeachers(params)
    const data = res?.data || res || {}
    teacherList.value = data.list || data.records || []
    teacherPagination.itemCount = data.total || data.totalCount || 0
    
    // 同时更新 teacherOptions 用于显示名称
    teacherList.value.forEach(teacher => {
      const existingOption = teacherOptions.value.find(opt => opt.value === teacher.id)
      if (!existingOption) {
        teacherOptions.value.push({
          label: teacher.name || teacher.username,
          value: teacher.id
        })
      }
    })
  } catch (error) {
    console.error('加载教师列表失败', error)
    message.error('加载教师列表失败')
    teacherList.value = []
    teacherPagination.itemCount = 0
  } finally {
    teacherLoading.value = false
  }
}

// 打开教师选择器
const openTeacherSelector = () => {
  teacherSearch.keyword = ''
  teacherPagination.page = 1
  showTeacherModal.value = true
  loadTeacherList()
}

// 选择教师
const handleSelectTeacher = (teacher) => {
  searchForm.teacherId = teacher.id
  
  // 更新 teacherOptions
  const existingOption = teacherOptions.value.find(opt => opt.value === teacher.id)
  if (!existingOption) {
    teacherOptions.value.push({
      label: teacher.name || teacher.username,
      value: teacher.id
    })
  }
  
  showTeacherModal.value = false
  message.success(`已选择教师：${teacher.name || teacher.username}`)
}

// 获取教师名称
const getTeacherName = (teacherId) => {
  if (!teacherId) return ''
  const teacher = teacherOptions.value.find(opt => opt.value === teacherId)
  return teacher ? teacher.label : ''
}

// 教师搜索
const handleTeacherSearch = () => {
  teacherPagination.page = 1
  loadTeacherList()
}

// 教师搜索重置
const handleTeacherReset = () => {
  teacherSearch.keyword = ''
  teacherPagination.page = 1
  loadTeacherList()
}

// 教师分页
const handleTeacherPageChange = (page) => {
  teacherPagination.page = page
  loadTeacherList()
}

const handleTeacherPageSizeChange = (pageSize) => {
  teacherPagination.pageSize = pageSize
  teacherPagination.page = 1
  loadTeacherList()
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    courseId: null,
    courseName: '',
    teacherId: null,
    status: null,
    title: ''
  })
  pagination.page = 1
  loadData()
}

const handleViewDetail = async (row) => {
  try {
    const response = await api.getHomeworkDetail(row.id)
    selectedHomework.value = response.data
    showDetailModal.value = true
  } catch (error) {
    message.error('加载作业详情失败')
  }
}

const handleToggleStatus = async (row) => {
  try {
    if (row.status === 1) {
      await api.forceUnpublish(row.id)
      message.success('取消发布成功')
    } else {
      await api.forcePublish(row.id)
      message.success('强制发布成功')
    }
    loadData()
    loadStatistics()
  } catch (error) {
    message.error('操作失败')
  }
}

const handleDelete = async (id) => {
  try {
    await api.deleteHomework(id)
    message.success('删除成功')
    loadData()
    loadStatistics()
  } catch (error) {
    message.error('删除失败')
  }
}

const handleBatchDelete = async () => {
  try {
    await api.batchDeleteHomeworks(selectedRowKeys.value)
    message.success('批量删除成功')
    selectedRowKeys.value = []
    loadData()
    loadStatistics()
  } catch (error) {
    message.error('批量删除失败')
  }
}

const handleBatchPublish = async () => {
  try {
    await api.batchPublishHomeworks(selectedRowKeys.value)
    message.success('批量发布成功')
    selectedRowKeys.value = []
    loadData()
    loadStatistics()
  } catch (error) {
    message.error('批量发布失败')
  }
}

const handleExport = async () => {
  try {
    const params = {}
    if (searchForm.courseId) {
      params.courseId = searchForm.courseId
    }
    if (searchForm.teacherId) {
      params.teacherId = searchForm.teacherId
    }
    if (searchForm.status !== null && searchForm.status !== undefined) {
      params.status = searchForm.status
    }
    
    const blob = await api.exportHomeworks(params)
    
    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `作业数据_${Date.now()}.xlsx`
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
  loadOptions()
})
</script>

<style scoped>
/* 页面容器 */
.homework-management {
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

/* 确保卡片内容区域布局正确 */
:deep(.n-card__content) {
  display: flex;
  flex-direction: column;
  padding: 20px;
}

/* 表格行悬停效果 */
:deep(.n-data-table-tr:hover) {
  background-color: var(--n-color-hover);
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

/* 表格加载状态 */
:deep(.n-data-table--loading) {
  min-height: 200px;
}

/* 空状态样式 */
:deep(.n-data-table-empty) {
  padding: 40px 0;
}
</style>