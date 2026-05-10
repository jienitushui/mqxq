<template>
  <div class="my-course-management h-full flex flex-col">
    <PageHeader class="flex-shrink-0">
      <template #title>我的课程管理</template>
      <template #extra>
        <n-button 
          type="error" 
          :disabled="!selectedRowKeys.length"
          @click="handleBatchQuit"
        >
          <template #icon>
            <i class="i-carbon:user-x" />
          </template>
          批量退出课程
        </n-button>
        <n-button class="ml-2" @click="handleRefresh">
          <template #icon>
            <i class="i-carbon:refresh" />
          </template>
          刷新
        </n-button>
      </template>
    </PageHeader>

    <n-card class="flex-1 flex flex-col overflow-hidden">
      <!-- 搜索区域 -->
      <div class="flex-shrink-0 mb-4">
        <n-form inline :model="searchParams" label-placement="left">
          <n-form-item label="用户昵称">
            <n-input 
              v-model:value="searchParams.userNickname" 
              placeholder="请输入用户昵称"
              clearable
              @keyup.enter="handleSearch"
            />
          </n-form-item>
          <n-form-item label="课程名称">
            <n-input 
              v-model:value="searchParams.courseName" 
              placeholder="请输入课程名称"
              clearable
              @keyup.enter="handleSearch"
            />
          </n-form-item>
          <n-form-item label="学习状态">
            <n-select 
              v-model:value="searchParams.status" 
              placeholder="请选择学习状态"
              clearable
              :options="statusOptions"
              style="width: 200px"
            />
          </n-form-item>
          <n-form-item>
            <n-button type="primary" @click="handleSearch">
              <template #icon>
                <i class="i-carbon:search" />
              </template>
              搜索
            </n-button>
            <n-button class="ml-2" @click="handleReset">
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
    <n-modal v-model:show="showDetailModal" preset="card" title="课程学习详情" class="w-800px">
      <div v-if="currentDetail">
        <n-descriptions :column="2" bordered>
          <n-descriptions-item label="用户头像">
            <img 
              :src="currentDetail.userAvatar || '/default-avatar.png'" 
              :alt="currentDetail.userNickname"
              class="w-50 h-50 rounded-full object-cover"
            />
          </n-descriptions-item>
          <n-descriptions-item label="用户昵称">
            {{ currentDetail.userNickname || '-' }}
          </n-descriptions-item>
          <n-descriptions-item label="课程封面">
            <img 
              :src="currentDetail.courseCover || '/default-course.png'" 
              :alt="currentDetail.courseName"
              class="w-50 h-50 rounded object-cover"
            />
          </n-descriptions-item>
          <n-descriptions-item label="课程名称">
            {{ currentDetail.courseName || '-' }}
          </n-descriptions-item>
          <n-descriptions-item label="教师名称">
            {{ currentDetail.teacherName || '-' }}
          </n-descriptions-item>
          <n-descriptions-item label="用户ID">
            {{ currentDetail.userId }}
          </n-descriptions-item>
          <n-descriptions-item label="课程ID">
            {{ currentDetail.courseId }}
          </n-descriptions-item>
          <n-descriptions-item label="订单ID">
            {{ currentDetail.orderId || '无' }}
          </n-descriptions-item>
          <n-descriptions-item label="创建时间">
            {{ formatDateTime(currentDetail.createTime) }}
          </n-descriptions-item>
          <n-descriptions-item label="更新时间">
            {{ formatDateTime(currentDetail.updateTime) }}
          </n-descriptions-item>
        </n-descriptions>
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h } from 'vue'
import { NButton, NTag, NPopconfirm, useMessage } from 'naive-ui'
import PageHeader from '@/components/common/PageHeader.vue'
import { handle401Error } from '@/utils'
import api from './api'

const message = useMessage()

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const selectedRowKeys = ref([])
const showDetailModal = ref(false)
const currentDetail = ref(null)

// 搜索参数
const searchParams = reactive({
  userNickname: '',
  courseName: '',
  status: null
})

// 分页参数
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
const statusOptions = ref([
  { label: '学习中', value: 0 },
  { label: '已完成', value: 1 }
])

// 表格列配置
const columns = [
  {
    type: 'selection'
  },
  {
    title: 'ID',
    key: 'id',
    width: 80,
    align: 'center'
  },
  {
    title: '用户头像',
    key: 'userAvatar',
    width: 80,
    align: 'center',
    render: (row) => {
      return h('img', {
        src: row.userAvatar || '/default-avatar.png',
        alt: row.userNickname,
        class: 'w-50 h-50 rounded-full',
        style: 'object-fit: cover'
      })
    }
  },
  {
    title: '用户昵称',
    key: 'userNickname',
    width: 120
  },
  {
    title: '用户ID',
    key: 'userId',
    width: 80,
    align: 'center'
  },
  {
    title: '课程封面',
    key: 'courseCover',
    width: 80,
    align: 'center',
    render: (row) => {
      return h('img', {
        src: row.courseCover || '/default-course.png',
        alt: row.courseName,
        class: 'w-50 h-50 rounded',
        style: 'object-fit: cover'
      })
    }
  },
  {
    title: '课程名称',
    key: 'courseName',
    width: 150
  },
  {
    title: '课程ID',
    key: 'courseId',
    width: 80,
    align: 'center'
  },
  {
    title: '教师名称',
    key: 'teacherName',
    width: 120
  },
  {
    title: '订单ID',
    key: 'orderId',
    width: 100,
    align: 'center',
    render: (row) => row.orderId || '-'
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    align: 'center',
    render: (row) => {
      return h(NTag, { 
        type: getStatusType(row.status),
        size: 'small'
      }, () => getStatusText(row.status))
    }
  },
  {
    title: '创建时间',
    key: 'createTime',
    width: 160,
    render: (row) => formatDateTime(row.createTime)
  },
  {
    title: '操作',
    key: 'actions',
    width: 120,
    render: (row) => {
      return [
        h(NButton, 
          { 
            size: 'small', 
            type: 'primary',
            class: 'mr-2',
            onClick: () => handleViewDetail(row.id) 
          }, 
          () => '详情'
        ),
        h(NPopconfirm,
          {
            onPositiveClick: () => handleForceQuit(row.id)
          },
          {
            default: () => '确定要强制该用户退出课程吗？',
            trigger: () => h(NButton, { size: 'small', type: 'error' }, () => '退出')
          }
        )
      ]
    }
  }
]

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'info',
    1: 'success', 
  }
  return typeMap[status] || 'default'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    0: '学习中',
    1: '已完成'
  }
  return textMap[status] || '未知'
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 获取列表数据
const fetchData = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.page,
      size: pagination.pageSize,
      ...searchParams
    }
    
    const response = await api.getList(params)
    if (response.code === '200') {
      tableData.value = response.data.list || []
      pagination.itemCount = response.data.total || 0
    } else {
      // 如果没有 code 字段，直接使用响应数据
      tableData.value = response.list || []
      pagination.itemCount = response.total || 0
    }
  } catch (error) {
    // 处理401错误，直接重定向到登录页面
    if (await handle401Error(error, message)) return
    message.error('获取数据失败')
    console.error('获取数据失败:', error)
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
  Object.assign(searchParams, {
    userNickname: '',
    courseName: '',
    status: null
  })
  pagination.page = 1
  fetchData()
}

// 刷新
const handleRefresh = () => {
  fetchData()
}

// 查看详情
const handleViewDetail = async (id) => {
  try {
    const response = await api.getDetail(id)
    if (response.code === '200') {
      currentDetail.value = response.data
      showDetailModal.value = true
    } else if (response.id) {
      // 如果直接返回数据对象
      currentDetail.value = response
      showDetailModal.value = true
    }
  } catch (error) {
    if (await handle401Error(error, message)) return
    message.error('获取详情失败')
    console.error('获取详情失败:', error)
  }
}

// 强制退出课程
const handleForceQuit = async (id) => {
  try {
    const response = await api.forceQuit(id)
    if (response.code === '200') {
      message.success('强制退出成功')
      fetchData()
    }
  } catch (error) {
    if (await handle401Error(error, message)) return
    message.error('强制退出失败')
    console.error('强制退出失败:', error)
  }
}

// 批量强制退出
const handleBatchQuit = async () => {
  if (!selectedRowKeys.value.length) {
    message.warning('请选择要操作的记录')
    return
  }
  
  try {
    const response = await api.batchQuit({ ids: selectedRowKeys.value })
    if (response.code === '200') {
      message.success('批量强制退出成功')
      selectedRowKeys.value = []
      fetchData()
    }
  } catch (error) {
    if (await handle401Error(error, message)) return
    message.error('批量强制退出失败')
    console.error('批量强制退出失败:', error)
  }
}

// 分页变化
const handlePageChange = (page) => {
  pagination.page = page
  fetchData()
}

const handlePageSizeChange = (pageSize) => {
  pagination.pageSize = pageSize
  pagination.page = 1
  fetchData()
}

// 初始化
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.my-course-management {
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