<template>
  <AppPage show-footer>
    <n-card title="💰 收入管理" class="mb-16">
      <template #header-extra>
        <n-button @click="refreshData" :loading="loading">
          <template #icon>
            <i class="i-material-symbols:refresh" />
          </template>
          刷新数据
        </n-button>
      </template>

      <!-- 收入统计卡片 -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-16 mb-24">
        <n-card>
          <n-statistic label="总收入" :value="revenueStats.totalRevenue" :precision="2">
            <template #prefix>¥</template>
          </n-statistic>
        </n-card>
        <n-card>
          <n-statistic label="总订单数" :value="orderStats.totalOrders">
            <template #suffix>单</template>
          </n-statistic>
        </n-card>
        <n-card>
          <n-statistic label="已完成订单" :value="orderStats.completedOrders">
            <template #suffix>单</template>
          </n-statistic>
        </n-card>
        <n-card>
          <n-statistic label="待处理订单" :value="orderStats.pendingOrders">
            <template #suffix>单</template>
          </n-statistic>
        </n-card>
      </div>



      <!-- 订单列表 -->
      <n-card title="📋 订单列表">
        <template #header-extra>
          <n-space>
            <n-select
              v-model:value="searchForm.status"
              placeholder="订单状态"
              clearable
              style="width: 120px"
              :options="statusOptions"
              @update:value="handleSearch"
            />

          </n-space>
        </template>

        <n-data-table
          :columns="columns"
          :data="tableData"
          :loading="tableLoading"
          :pagination="pagination"
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
      </n-card>
    </n-card>
  </AppPage>
</template>

<script setup>
import { ref, reactive, onMounted, h } from 'vue'
import { NButton, NTag, useMessage } from 'naive-ui'
import api from './api'

// 格式化日期时间
const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  try {
    // 处理 "Fri Sep 12 18:56:55 CST 2025" 格式的日期字符串
    let date = new Date(dateStr)
    
    // 如果直接解析失败，尝试其他方法
    if (isNaN(date.getTime())) {
      // 尝试移除时区信息再解析
      const cleanDateStr = dateStr.replace(/\s+CST\s+/, ' ')
      date = new Date(cleanDateStr)
      
      // 如果还是失败，尝试手动解析
      if (isNaN(date.getTime())) {
        const match = dateStr.match(/(\w+)\s+(\w+)\s+(\d+)\s+(\d+):(\d+):(\d+)\s+\w+\s+(\d+)/)
        if (match) {
          const [, , month, day, hour, minute, second, year] = match
          const monthMap = {
            'Jan': 0, 'Feb': 1, 'Mar': 2, 'Apr': 3, 'May': 4, 'Jun': 5,
            'Jul': 6, 'Aug': 7, 'Sep': 8, 'Oct': 9, 'Nov': 10, 'Dec': 11
          }
          date = new Date(parseInt(year), monthMap[month], parseInt(day), parseInt(hour), parseInt(minute), parseInt(second))
        }
      }
    }
    
    // 最终检查日期是否有效
    if (isNaN(date.getTime())) {
      return dateStr
    }
    
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (error) {
    console.error('日期格式化错误:', error, dateStr)
    return dateStr
  }
}

// 格式化日期（用于API参数）
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return d.toISOString().split('T')[0]
}




const message = useMessage()
const loading = ref(false)
const tableLoading = ref(false)

// 收入统计数据
const revenueStats = reactive({
  totalRevenue: 0,
  monthlyRevenue: 0,
  pendingRevenue: 0,
  settledRevenue: 0
})

// 订单统计数据
const orderStats = reactive({
  totalOrders: 0,
  completedOrders: 0,
  pendingOrders: 0
})

// 搜索表单
const searchForm = reactive({
  status: null,
  dateRange: null
})

// 状态选项
const statusOptions = [
  { label: '待支付', value: 'NOT_PAY' },
  { label: '已完成', value: 'DONE' },
  { label: '已取消', value: 'CANCEL' },
  { label: '已退款', value: 'REFUND_DONE' },
  { label: '已评价', value: 'COMMENT_DONE' }
]

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

// 表格列定义
const columns = [
  {
    title: '课程图片',
    key: 'goodsImg',
    width: 80,
    render: (row) => h('img', {
      src: row.goodsImg,
      style: { width: '50px', height: '50px', objectFit: 'cover', borderRadius: '4px' },
      onError: (e) => { e.target.src = '/default-course.png' }
    })
  },
  { title: '订单号', key: 'orderNo', width: 180 },
  { title: '课程名称', key: 'courseName', ellipsis: { tooltip: true } },
  { title: '学员id', key: 'studentName', width: 120 },
  { 
    title: '订单金额', 
    key: 'amount', 
    width: 120,
    render: (row) => `¥${row.amount?.toFixed(2) || '0.00'}`
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) => {
      const statusMap = {
        'NOT_PAY': { type: 'warning', text: '待支付' },
        'DONE': { type: 'success', text: '已完成' },
        'CANCEL': { type: 'error', text: '已取消' },
        'REFUND_DONE': { type: 'info', text: '已退款' },
        'COMMENT_DONE': { type: 'default', text: '已评价' }
      }
      const status = statusMap[row.status] || { type: 'default', text: '未知' }
      return h(NTag, { type: status.type }, { default: () => status.text })
    }
  },
  { title: '下单时间', key: 'createTime', width: 160 },
  { 
    title: '支付时间', 
    key: 'payTime', 
    width: 160,
    render: (row) => row.payTime || '-'
  },
  {
    title: '操作',
    key: 'actions',
    width: 120,
    render: (row) => h(
      NButton,
      {
        size: 'small',
        type: 'primary',
        ghost: true,
        onClick: () => viewOrderDetail(row.id)
      },
      { default: () => '查看详情' }
    )
  }
]



// 加载收入数据
const loadRevenueData = async () => {
  try {
    loading.value = true
    const res = await api.getMyRevenue()
    const data = res?.data || {}
    
    // 根据API返回的数据结构更新统计信息
    // API返回: { "completedOrders": 0, "totalRevenue": 0, "totalOrders": 1 }
    Object.assign(revenueStats, {
      totalRevenue: data.totalRevenue || 0,
      monthlyRevenue: data.totalRevenue || 0, // 直接使用总收入作为月收入
      pendingRevenue: 0, // 待结算收入暂时设为0，因为API没有提供此字段
      settledRevenue: data.totalRevenue || 0 // 已结算收入等于总收入
    })

    // 更新订单统计数据
    Object.assign(orderStats, {
      totalOrders: data.totalOrders || 0,
      completedOrders: data.completedOrders || 0,
      pendingOrders: (data.totalOrders || 0) - (data.completedOrders || 0)
    })


  } catch (error) {
    console.error('加载收入数据失败:', error)
    message.error('加载收入数据失败')
  } finally {
    loading.value = false
  }
}

// 加载订单列表
const loadOrderList = async () => {
  try {
    tableLoading.value = true
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize
    }
    
    // 添加状态筛选
    if (searchForm.status) {
      params.status = searchForm.status
    }
    
    // 添加日期范围筛选
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = formatDate(searchForm.dateRange[0])
      params.endDate = formatDate(searchForm.dateRange[1])
    }
    
    const res = await api.getMyCourseOrders(params)
    const data = res?.data || {}
    
    // 根据API返回的数据结构处理订单列表
    const orderList = (data.list || []).map(order => ({
      id: order.id,
      orderNo: order.orderNo,
      courseName: order.goodsName,
      studentName: order.userId,
      amount: order.goodsPrice,
      status: order.status,
      createTime: formatDateTime(order.createTime),
      payTime: order.payTime ? formatDateTime(order.payTime) : null,
      goodsImg: order.goodsImg,
      payNo: order.payNo
    }))
    
    tableData.value = orderList
    pagination.itemCount = data.total || 0
  } catch (error) {
    console.error('加载订单列表失败:', error)
    message.error('加载订单列表失败')
  } finally {
    tableLoading.value = false
  }
}

// 查看订单详情
const viewOrderDetail = async (orderId) => {
  try {
    const res = await api.getOrderDetail(orderId)
    const orderDetail = res?.data || {}
    
    // 这里可以打开一个详情弹窗或跳转到详情页面
    message.info(`订单详情：${JSON.stringify(orderDetail)}`)
  } catch (error) {
    console.error('获取订单详情失败:', error)
    message.error('获取订单详情失败')
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.page = 1
  loadOrderList()
}

// 分页处理
const handlePageChange = (page) => {
  pagination.page = page
  loadOrderList()
}

const handlePageSizeChange = (pageSize) => {
  pagination.pageSize = pageSize
  pagination.page = 1
  loadOrderList()
}

// 刷新数据
const refreshData = () => {
  loadRevenueData()
  loadOrderList()
}

onMounted(() => {
  refreshData()
})
</script>