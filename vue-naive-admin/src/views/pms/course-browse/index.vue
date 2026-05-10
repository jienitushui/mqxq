<template>
  <div class="course-browse-management h-full flex flex-col">
    <PageHeader class="flex-shrink-0">
      <template #title>课程浏览管理</template>
      <template #extra>
        <n-button 
          type="error" 
          :disabled="!selectedRowKeys.length"
          @click="handleBatchDelete"
          style="margin-right: 8px"
        >
          <template #icon>
            <i class="i-carbon:trash-can" />
          </template>
          批量删除
        </n-button>
        <n-button type="info" @click="showStatsModal = true">
          <template #icon>
            <i class="i-carbon:analytics" />
          </template>
          浏览统计
        </n-button>
      </template>
    </PageHeader>

    <n-card class="flex-1 flex flex-col overflow-hidden">
      <!-- 搜索区域 -->
      <div class="flex-shrink-0 mb-4">
        <n-form inline :model="searchForm" label-placement="left">
          <n-form-item label="课程ID">
            <n-input v-model:value="searchForm.courseId" placeholder="请输入课程ID" clearable />
          </n-form-item>
          <n-form-item label="用户ID">
            <n-input v-model:value="searchForm.userId" placeholder="请输入用户ID" clearable />
          </n-form-item>
          <n-form-item label="IP地址">
            <n-input v-model:value="searchForm.ipAddress" placeholder="请输入IP地址" clearable />
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

    <!-- 详情弹窗 -->
    <n-modal v-model:show="showDetailModal" preset="card" title="浏览记录详情" style="width: 600px">
      <div v-if="currentRecord">
        <n-descriptions :column="2" bordered>
          <n-descriptions-item label="记录ID">{{ currentRecord.id }}</n-descriptions-item>
          <n-descriptions-item label="用户ID">{{ currentRecord.userId }}</n-descriptions-item>
          <n-descriptions-item label="课程ID">{{ currentRecord.courseId }}</n-descriptions-item>
          <n-descriptions-item label="浏览时间">{{ currentRecord.viewTime }}</n-descriptions-item>
          <n-descriptions-item label="IP地址">{{ currentRecord.ipAddress }}</n-descriptions-item>
          <n-descriptions-item label="用户代理" span="2">{{ currentRecord.userAgent }}</n-descriptions-item>
          <n-descriptions-item label="创建时间">{{ currentRecord.createTime }}</n-descriptions-item>
          <!-- <n-descriptions-item label="更新时间">{{ currentRecord.updateTime }}</n-descriptions-item> -->
        </n-descriptions>
      </div>
    </n-modal>

    <!-- 统计弹窗 -->
    <n-modal v-model:show="showStatsModal" preset="card" title="浏览统计" style="width: 800px">
      <div class="stats-content">
        <n-grid :cols="3" :x-gap="16" :y-gap="16">
          <n-grid-item>
            <n-statistic label="总浏览次数" :value="stats.totalViews" />
          </n-grid-item>
          <n-grid-item>
            <n-statistic label="独立用户数" :value="stats.uniqueUsers" />
          </n-grid-item>
          <n-grid-item>
            <n-statistic label="独立课程数" :value="stats.uniqueCourses" />
          </n-grid-item>
        </n-grid>
        
        <!-- 这里可以添加图表组件 -->
        <!-- <div class="chart-area mt-4">
          <n-card title="浏览趋势图" size="small">
            <div style="height: 300px; display: flex; align-items: center; justify-content: center;">
              图表区域（可集成ECharts）
            </div>
          </n-card>
        </div> -->
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, h } from 'vue'
import { PageHeader } from '@/components'
import { useMessage, useDialog } from 'naive-ui'

import { courseBrowseApi } from './api'
const message = useMessage()

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const selectedRowKeys = ref([])
const showDetailModal = ref(false)
const showStatsModal = ref(false)
const currentRecord = ref(null)

// 搜索表单
const searchForm = reactive({
  courseId: null,
  userId: null,
  ipAddress: ''
})

// 统计数据
const stats = reactive({
  totalViews: 0,
  uniqueUsers: 0,
  uniqueCourses: 0
})

const dialog = useDialog()

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
  { type: 'selection' },
  { title: 'ID', key: 'id', width: 80 },
  { title: '用户ID', key: 'userId' },
  { title: '课程ID', key: 'courseId' },
  { title: '浏览时间', key: 'viewTime' },
  { title: 'IP地址', key: 'ipAddress' },
  { title: '用户代理', key: 'userAgent', ellipsis: { tooltip: true } },
  { title: '创建时间', key: 'createTime' },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    render: (row) => [
      h('n-button', 
        { 
          size: 'small', 
          type: 'info',
          onClick: () => handleViewDetail(row)
        }, 
        '详情'
      ),
      h('n-button', 
        { 
          size: 'small', 
          type: 'error',
          style: 'margin-left: 8px',
          onClick: () => handleDelete(row.id)
        }, 
        '删除'
      )
    ]
  }
]

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
      courseId: searchForm.courseId,
      userId: searchForm.userId,
      ipAddress: searchForm.ipAddress
    }
    
    // 清理空值参数
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) {
        delete params[key]
      }
    })
    
    const response = await courseBrowseApi.getBrowseList(params)
    tableData.value = response.data?.records || response.data?.list || []
    pagination.itemCount = response.data?.total || 0
  } catch (error) {
    message.error('获取数据失败')
    console.error('获取浏览记录失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    courseId: null,
    userId: null,
    ipAddress: ''
  })
  pagination.page = 1
  fetchData()
}

// 查看详情
const handleViewDetail = async (row) => {
  try {
    const response = await courseBrowseApi.getBrowseDetail(row.id)
    currentRecord.value = response.data
    showDetailModal.value = true
  } catch (error) {
    message.error('获取详情失败')
  }
}

// 删除
const handleDelete = (id) => {
  dialog.warning({
    title: '确认删除',
    content: '确定要删除这条浏览记录吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await courseBrowseApi.deleteBrowse(id)
        message.success('删除成功')
        fetchData()
      } catch (error) {
        message.error('删除失败')
      }
    }
  })
}

// 批量删除
const handleBatchDelete = () => {
  dialog.warning({
    title: '确认批量删除',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 条记录吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await courseBrowseApi.batchDeleteBrowse(selectedRowKeys.value)
        message.success('批量删除成功')
        selectedRowKeys.value = []
        fetchData()
      } catch (error) {
        message.error('批量删除失败')
      }
    }
  })
}

// 获取统计数据
const fetchStats = async () => {
  try {
    const response = await courseBrowseApi.getBrowseStats()
    Object.assign(stats, response.data)
  } catch (error) {
    message.error('获取统计数据失败')
  }
}

// 分页处理
const handlePageChange = (page) => {
  pagination.page = page
  fetchData()
}

const handlePageSizeChange = (pageSize) => {
  pagination.pageSize = pageSize
  pagination.page = 1
  fetchData()
}

// 监听统计弹窗显示
watch(() => showStatsModal.value, (show) => {
  if (show) {
    fetchStats()
  }
})

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
/* 页面容器 */
.course-browse-management {
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

.stats-content {
  padding: 16px 0;
}

.chart-area {
  margin-top: 24px;
}
</style>