<template>
  <div class="teacher-course-view h-full flex flex-col">
    <PageHeader class="flex-shrink-0">
      <template #title>课程浏览统计</template>
    </PageHeader>

    <!-- 统计卡片 -->
    <div class="flex-shrink-0 mb-4">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <n-card>
          <n-statistic label="总浏览量" :value="statistics.totalViews" />
        </n-card>
        <n-card>
          <n-statistic label="课程总数" :value="statistics.totalCourses" />
        </n-card>
        <n-card>
          <n-statistic label="已发布课程" :value="statistics.publishedCourses" />
        </n-card>
        <n-card>
          <n-statistic label="学生总数" :value="statistics.totalStudents" />
        </n-card>
      </div>
    </div>

    <n-card class="flex-1 flex flex-col overflow-hidden">
      <!-- 搜索区域 -->
      <div class="flex-shrink-0 mb-4">
        <n-form inline :model="searchForm" label-placement="left">
          <n-form-item label="课程ID">
            <n-input
              v-model:value="searchForm.courseId"
              placeholder="请输入课程ID"
              clearable
              style="width: 200px"
              @keyup.enter="handleSearch"
            />
          </n-form-item>
          <n-form-item label="用户ID">
            <n-input
              v-model:value="searchForm.userId"
              placeholder="请输入用户ID"
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

    <!-- 浏览详情弹窗 -->
    <n-modal v-model:show="showDetailModal" :mask-closable="false">
      <n-card
        style="width: 600px"
        title="浏览详情"
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
        
        <div v-if="selectedView">
          <n-descriptions :column="2" bordered>
            <n-descriptions-item label="记录ID">
              {{ selectedView.id }}
            </n-descriptions-item>
            <n-descriptions-item label="用户ID">
              {{ selectedView.userId }}
            </n-descriptions-item>
            <n-descriptions-item label="课程ID">
              {{ selectedView.courseId }}
            </n-descriptions-item>
            <n-descriptions-item label="IP地址">
              {{ selectedView.ipAddress }}
            </n-descriptions-item>
            <n-descriptions-item label="浏览时间">
              {{ new Date(selectedView.viewTime).toLocaleString('zh-CN') }}
            </n-descriptions-item>
            <n-descriptions-item label="创建时间">
              {{ new Date(selectedView.createTime).toLocaleString('zh-CN') }}
            </n-descriptions-item>
            <n-descriptions-item label="用户代理" :span="2">
              <div class="break-all">{{ selectedView.userAgent }}</div>
            </n-descriptions-item>
          </n-descriptions>
        </div>
      </n-card>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h } from 'vue'
import { NButton, NTag, useMessage } from 'naive-ui'
import { PageHeader } from '@/components'
import api from './api'

const message = useMessage()

// 响应式数据
const loading = ref(false)
const showDetailModal = ref(false)
const tableData = ref([])
const selectedView = ref(null)

// 统计数据
const statistics = reactive({
  totalViews: 0,
  totalCourses: 0,
  publishedCourses: 0,
  totalStudents: 0
})

// 搜索表单
const searchForm = reactive({
  courseId: '',
  userId: ''
})

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
    title: '用户ID',
    key: 'userId',
    width: 100
  },
  {
    title: '课程ID',
    key: 'courseId',
    width: 100
  },
  {
    title: 'IP地址',
    key: 'ipAddress',
    width: 140
  },
  {
    title: '浏览器',
    key: 'userAgent',
    width: 120,
    render(row) {
      const userAgent = row.userAgent || ''
      if (userAgent.includes('Chrome')) {
        return h(NTag, { type: 'info', size: 'small' }, { default: () => 'Chrome' })
      } else if (userAgent.includes('Firefox')) {
        return h(NTag, { type: 'warning', size: 'small' }, { default: () => 'Firefox' })
      } else if (userAgent.includes('Safari')) {
        return h(NTag, { type: 'success', size: 'small' }, { default: () => 'Safari' })
      } else if (userAgent.includes('Edge')) {
        return h(NTag, { type: 'primary', size: 'small' }, { default: () => 'Edge' })
      } else {
        return h(NTag, { type: 'default', size: 'small' }, { default: () => '其他' })
      }
    }
  },
  {
    title: '浏览时间',
    key: 'viewTime',
    width: 180,
    render(row) {
      return new Date(row.viewTime).toLocaleString('zh-CN')
    }
  },
  {
    title: '创建时间',
    key: 'createTime',
    width: 180,
    render(row) {
      return new Date(row.createTime).toLocaleString('zh-CN')
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 120,
    render(row) {
      return h(NButton, {
        size: 'small',
        type: 'primary',
        onClick: () => handleViewDetail(row)
      }, { default: () => '查看详情' })
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
    const response = await api.getCourseViewPage(params)
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
    const response = await api.getCourseViewStatistics()
    Object.assign(statistics, response.data)
  } catch (error) {
    message.error('加载统计数据失败')
  }
}



const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    courseId: '',
    userId: ''
  })
  pagination.page = 1
  loadData()
}

const handleViewDetail = (row) => {
  selectedView.value = row
  showDetailModal.value = true
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
})
</script>

<style scoped>
.teacher-course-view {
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