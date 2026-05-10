<template>
  <div class="course-comment-container h-full flex flex-col">
    <PageHeader class="flex-shrink-0">
      <template #title>课程评论管理</template>
      <template #extra>
        <n-button
          type="warning"
          :disabled="!selectedRowKeys.length"
          @click="handleBatchHide"
          style="margin-right: 8px"
        >
          <template #icon>
            <i class="i-carbon:view-off" />
          </template>
          批量隐藏
        </n-button>
        <n-button
          type="success"
          :disabled="!selectedRowKeys.length"
          @click="handleBatchShow"
          style="margin-right: 8px"
        >
          <template #icon>
            <i class="i-carbon:view" />
          </template>
          批量显示
        </n-button>
        <n-button
          type="error"
          :disabled="!selectedRowKeys.length"
          @click="handleBatchDelete"
        >
          <template #icon>
            <i class="i-carbon:trash-can" />
          </template>
          批量删除
        </n-button>
      </template>
    </PageHeader>

    <!-- 统计卡片 -->
    <div class="flex-shrink-0 mb-4">
      <div class="statistics-cards">
        <n-card title="总评论数" class="stat-card">
          <n-statistic label="总评论数" :value="statistics.totalComments" />
        </n-card>
        <n-card title="显示评论" class="stat-card">
          <n-statistic label="显示评论" :value="statistics.visibleComments" />
        </n-card>
        <n-card title="隐藏评论" class="stat-card">
          <n-statistic label="隐藏评论" :value="statistics.hiddenComments" />
        </n-card>
        <n-card title="今日评论" class="stat-card">
          <n-statistic label="今日评论" :value="statistics.todayComments" />
        </n-card>
        <n-card title="平均评分" class="stat-card">
          <n-statistic label="平均评分" :value="statistics.averageRating" />
        </n-card>
      </div>
    </div>

    <!-- 课程评分排行 -->
    <div class="flex-shrink-0 mb-4">
      <n-card title="课程评分排行" class="ranking-card">
        <n-list>
          <n-list-item v-for="(item, index) in courseRanking" :key="item.courseId">
            <template #prefix>
              <n-tag :type="index < 3 ? 'success' : 'default'">{{ index + 1 }}</n-tag>
            </template>
            <n-thing>
              <template #header>{{ item.courseName }}</template>
              <template #description>
                <n-space>
                  <n-rate :value="item.averageRating" readonly size="small" />
                  <span>{{ item.averageRating }}分</span>
                  <span>{{ item.commentCount }}条评论</span>
                </n-space>
              </template>
            </n-thing>
          </n-list-item>
        </n-list>
      </n-card>
    </div>

    <n-card class="flex-1 flex flex-col overflow-hidden">
      <!-- 搜索区域 -->
      <div class="flex-shrink-0 mb-4">
        <n-form inline :model="searchParams" label-placement="left">
          <n-form-item label="课程">
            <n-input
              :value="searchParams.courseName"
              placeholder="请选择课程"
              readonly
              clearable
              @click="showCourseModal = true"
              @clear="handleClearCourse"
              style="width: 200px; cursor: pointer"
            >
              <template #suffix>
                <i class="i-carbon:search" />
              </template>
            </n-input>
          </n-form-item>
          <n-form-item label="用户ID">
            <n-input
              v-model:value="searchParams.userId"
              placeholder="输入用户ID"
              clearable
              style="width: 120px"
            />
          </n-form-item>
          <n-form-item label="关键词">
            <n-input
              v-model:value="searchParams.keyword"
              placeholder="搜索关键词"
              clearable
              style="width: 150px"
            />
          </n-form-item>
          <n-form-item label="状态">
            <n-select
              v-model:value="searchParams.status"
              placeholder="选择状态"
              :options="statusOptions"
              clearable
              style="width: 120px"
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
          :row-key="(row) => row.id"
          :checked-row-keys="selectedRowKeys"
          @update:checked-row-keys="handleSelectionChange"
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

    <!-- 评论详情弹窗 -->
    <n-modal v-model:show="showReplyModal" preset="dialog" title="评论详情">
      <template #default>
        <div class="reply-modal-content">
          <div class="comment-info">
            <p><strong>学生昵称：</strong>{{ currentComment?.nickname }}</p>
            <!-- <p><strong>用户邮箱：</strong>{{ currentComment?.userName }}</p> -->
            <!-- <p><strong>课程：</strong>{{ currentComment?.courseName }}</p> -->
            <p><strong>评分：</strong>
              <n-rate :value="currentComment?.score" readonly size="small" />
            </p>
            <p><strong>评论时间：</strong>{{ formatTime(currentComment?.createTime) }}</p>
            <p><strong>评论内容：</strong></p>
            <div class="comment-content">{{ currentComment?.content }}</div>
            <p v-if="currentComment?.replyContent"><strong>回复内容：</strong></p>
            <div v-if="currentComment?.replyContent" class="comment-content">{{ currentComment?.replyContent }}</div>
          </div>
        </div>
      </template>
      <template #action>
        <n-button @click="showReplyModal = false">关闭</n-button>
      </template>
    </n-modal>

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
              <n-button type="primary" @click="handleCourseSearch">搜索</n-button>
              <n-button @click="handleCourseReset" style="margin-left: 8px">重置</n-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h, watch } from 'vue'
import { PageHeader } from '@/components'
import { NButton, NTag, NRate, NSpace, NList, NListItem, NThing, useMessage } from 'naive-ui'

const message = useMessage()

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const showReplyModal = ref(false)
const replyLoading = ref(false)
const currentComment = ref(null)
const courseOptions = ref([])
const selectedRowKeys = ref([])
const courseRanking = ref([])

// 课程选择弹窗相关
const showCourseModal = ref(false)
const courseLoading = ref(false)
const courseTableData = ref([])

// 监听课程弹窗打开
watch(showCourseModal, (newVal) => {
  if (newVal) {
    coursePagination.page = 1
    loadCourses()
  }
})

// 搜索参数
const searchParams = reactive({
  courseId: null,
  courseName: '',
  userId: null,
  status: null,
  keyword: ''
})

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
  prefix: ({ itemCount }) => `共 ${itemCount} 条`,
  simple: false
})

// 回复表单
const replyForm = reactive({
  content: ''
})

// 统计信息
const statistics = ref({
  totalComments: 0,
  averageRating: 0,
  visibleComments: 0,
  hiddenComments: 0,
  todayComments: 0
})

import * as api from './api'

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

// 状态选项
const statusOptions = [
  { label: '显示', value: 1 },
  { label: '隐藏', value: 0 }
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
    title: '学生昵称',
    key: 'nickname',
    width: 120
  },
  {
    title: '课程名称',
    key: 'courseName',
    width: 200,
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '评分',
    key: 'score',
    width: 120,
    render: (row) => h(NRate, { value: row.score, readonly: true, size: 'small' })
  },
  {
    title: '评论内容',
    key: 'content',
    width: 300,
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) => h(
      NTag,
      { type: row.status === 1 ? 'success' : 'error' },
      { default: () => row.status === 1 ? '显示' : '隐藏' }
    )
  },
  {
    title: '评论时间',
    key: 'createTime',
    width: 160,
    render: (row) => formatTime(row.createTime)
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    render: (row) => [
      h(
        NButton,
        {
          size: 'small',
          type: 'primary',
          onClick: () => handleViewDetail(row),
          style: { marginRight: '8px' }
        },
        { default: () => '查看详情' }
      ),
      h(
        NButton,
        {
          size: 'small',
          type: row.status === 1 ? 'warning' : 'success',
          onClick: () => handleToggleStatus(row)
        },
        { default: () => row.status === 1 ? '隐藏' : '显示' }
      ),
      h(
        NButton,
        {
          size: 'small',
          type: 'error',
          onClick: () => handleDelete(row),
          style: { marginLeft: '8px' }
        },
        { default: () => '删除' }
      )
    ]
  }
]

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.pageSize,
      courseId: searchParams.courseId || undefined,
      userId: searchParams.userId || undefined,
      status: searchParams.status !== null ? searchParams.status : undefined,
      keyword: searchParams.keyword || undefined
    }

    const response = await api.default.getList(params)
    const data = response?.data || response || {}
    // console.log('API响应数据:', data) // 调试日志
    tableData.value = data.list || []
    pagination.itemCount = data.total || 0
  } catch (error) {
    console.error('加载数据失败:', error)
    message.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 加载统计信息
const loadStatistics = async () => {
  try {
    const response = await api.default.getStatistics()
    const data = response?.data || response || {}
    statistics.value = {
      totalComments: data.totalComments || 0,
      averageRating: data.averageRating || 0,
      visibleComments: data.visibleComments || 0,
      hiddenComments: data.hiddenComments || 0,
      todayComments: data.todayComments || 0
    }
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}

// 加载课程评分排行
const loadCourseRanking = async () => {
  try {
    const response = await api.default.getCourseRatingRank({ limit: 10 })
    const data = response?.data || response || {}
    courseRanking.value = data.list || data || []
  } catch (error) {
    console.error('加载课程评分排行失败:', error)
  }
}

const loadCourses = async () => {
  courseLoading.value = true
  try {
    const params = {
      page: coursePagination.page,
      size: coursePagination.pageSize,
      ...courseSearch
    }
    
    const response = await api.default.getCourses(params)
    const data = response?.data || response || {}
    const list = data.list || data.records || []
    const total = data.total || data.totalCount || 0
    
    courseTableData.value = list
    coursePagination.itemCount = total
    
    if (list.length === 0 && coursePagination.page === 1) {
      message.warning('暂无课程数据，请先创建课程')
    }
  } catch (error) {
    console.error('加载课程列表失败:', error)
    message.error('加载课程列表失败')
    courseTableData.value = []
    coursePagination.itemCount = 0
  } finally {
    courseLoading.value = false
  }
}

const handleCourseSearch = () => {
  coursePagination.page = 1
  loadCourses()
}

const handleCourseReset = () => {
  Object.assign(courseSearch, {
    title: '',
    status: 1
  })
  coursePagination.page = 1
  loadCourses()
}

const handleCoursePageChange = (page) => {
  coursePagination.page = page
  loadCourses()
}

const handleCoursePageSizeChange = (pageSize) => {
  coursePagination.pageSize = pageSize
  coursePagination.page = 1
  loadCourses()
}

const handleSelectCourse = (course) => {
  searchParams.courseId = course.id
  searchParams.courseName = course.title
  showCourseModal.value = false
  message.success(`已选择课程：${course.title}`)
}

const handleClearCourse = () => {
  searchParams.courseId = null
  searchParams.courseName = ''
}

// 事件处理
const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchParams, {
    courseId: null,
    courseName: '',
    userId: null,
    status: null,
    keyword: ''
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

const handleViewDetail = async (row) => {
  try {
    const response = await api.default.getDetail(row.id)
    const data = response?.data || response || {}
    // console.log('API响应数据:', data) // 调试日志
    currentComment.value = data
    showReplyModal.value = true
  } catch (error) {
    console.error('获取评论详情失败:', error)
    message.error('获取评论详情失败')
  }
}

const handleToggleStatus = async (row) => {
  try {
    await api.default.toggleStatus(row.id)
    message.success('状态切换成功')
    loadData()
  } catch (error) {
    console.error('状态切换失败:', error)
    message.error('状态切换失败')
  }
}

const handleDelete = async (row) => {
  try {
    await api.default.delete(row.id)
    message.success('删除成功')
    loadData()
    loadStatistics()
  } catch (error) {
    console.error('删除失败:', error)
    message.error('删除失败')
  }
}

const handleSelectionChange = (keys) => {
  selectedRowKeys.value = keys
}

const handleBatchHide = async () => {
  try {
    await api.default.batchHide(selectedRowKeys.value)
    message.success('批量隐藏成功')
    selectedRowKeys.value = []
    loadData()
    loadStatistics()
  } catch (error) {
    console.error('批量隐藏失败:', error)
    message.error('批量隐藏失败')
  }
}

const handleBatchShow = async () => {
  try {
    await api.default.batchShow(selectedRowKeys.value)
    message.success('批量显示成功')
    selectedRowKeys.value = []
    loadData()
    loadStatistics()
  } catch (error) {
    console.error('批量显示失败:', error)
    message.error('批量显示失败')
  }
}

const handleBatchDelete = async () => {
  try {
    await api.default.batchDelete(selectedRowKeys.value)
    message.success('批量删除成功')
    selectedRowKeys.value = []
    loadData()
    loadStatistics()
  } catch (error) {
    console.error('批量删除失败:', error)
    message.error('批量删除失败')
  }
}

const handleReplySubmit = () => {
  showReplyModal.value = false
}

// 时间格式化函数
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 生命周期
onMounted(() => {
  loadData()
  loadStatistics()
  loadCourseRanking()
})
</script>

<style scoped>
/* 页面容器 */
.course-comment-container {
  padding: 16px;
}

.statistics-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
}

.stat-card {
  text-align: center;
}

.ranking-card {
  margin-bottom: 16px;
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

.reply-modal-content {
  max-width: 500px;
}

.comment-info {
  background: #f5f5f5;
  padding: 12px;
  border-radius: 6px;
  margin-bottom: 16px;
}

.comment-content {
  background: white;
  padding: 8px;
  border-radius: 4px;
  border: 1px solid #e0e0e0;
  margin-top: 8px;
  max-height: 100px;
  overflow-y: auto;
}
</style>