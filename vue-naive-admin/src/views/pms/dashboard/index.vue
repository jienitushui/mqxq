<template>
  <AppPage>
    <div class="admin-dashboard">
      <PageHeader>
        <template #title>数据大屏</template>
        <template #extra>
          <n-button @click="refreshData" type="primary" :loading="loading">
            <template #icon>
              <i class="i-carbon:refresh" />
            </template>
            刷新数据
          </n-button>
        </template>
      </PageHeader>

    <!-- 概览数据卡片 -->
    <div class="overview-section">
      <n-card title="核心指标概览" class="overview-card">
        <div class="overview-grid">
          <!-- 平台整体 -->
          <div class="overview-group">
            <h3 class="group-title">平台整体</h3>
            <div class="stats-grid">
              <n-statistic label="总用户数" :value="overview.totalUsers || 0" />
              <n-statistic label="总教师数" :value="overview.totalTeachers || 0" />
              <n-statistic label="总学员数" :value="overview.totalStudents || 0" />
              <n-statistic label="总课程数" :value="overview.totalCourses || 0" />
              <n-statistic label="总订单数" :value="overview.totalOrders || 0" />
              <n-statistic label="总收入" :value="overview.totalRevenue || 0">
                <template #suffix>元</template>
              </n-statistic>
            </div>
          </div>

          <!-- 今日数据 -->
          <div class="overview-group">
            <h3 class="group-title">今日数据</h3>
            <div class="stats-grid">
              <n-statistic label="新增用户" :value="overview.todayNewUsers || 0" />
              <n-statistic label="新增订单" :value="overview.todayNewOrders || 0" />
              <n-statistic label="今日收入" :value="overview.todayRevenue || 0">
                <template #suffix>元</template>
              </n-statistic>
            </div>
          </div>

          <!-- 学习相关 -->
          <div class="overview-group">
            <h3 class="group-title">学习相关</h3>
            <div class="stats-grid">
              <n-statistic label="学习记录数" :value="overview.totalLearningRecords || 0" />
              <n-statistic label="完成率" :value="overview.completionRate || 0" :precision="1">
                <template #suffix>%</template>
              </n-statistic>
              <n-statistic label="总浏览量" :value="overview.totalViews || 0" />
              <n-statistic label="独立访客" :value="overview.uniqueVisitors || 0" />
            </div>
          </div>

          <!-- 内容相关 -->
          <div class="overview-group">
            <h3 class="group-title">内容相关</h3>
            <div class="stats-grid">
              <n-statistic label="作业数" :value="overview.totalHomeworks || 0" />
              <n-statistic label="提交数" :value="overview.totalSubmissions || 0" />
              <n-statistic label="评论数" :value="overview.totalComments || 0" />
              <n-statistic label="公告数" :value="overview.totalAnnouncements || 0" />
              <n-statistic label="章节数" :value="overview.totalChapters || 0" />
              <n-statistic label="小节数" :value="overview.totalSections || 0" />
            </div>
          </div>
        </div>
      </n-card>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <!-- 第一行：分布饼图 -->
      <div class="charts-row">
        <n-card title="用户角色分布" class="chart-card">
          <div class="chart-container">
            <VChart v-if="userRoleData.length > 0" :option="userRoleOption" autoresize />
            <n-empty v-else description="暂无数据" />
          </div>
        </n-card>

        <n-card title="用户状态分布" class="chart-card">
          <div class="chart-container">
            <VChart v-if="userStatusData.length > 0" :option="userStatusOption" autoresize />
            <n-empty v-else description="暂无数据" />
          </div>
        </n-card>
      </div>

      <!-- 第二行：分布饼图 -->
      <div class="charts-row">
        <n-card title="课程分类分布" class="chart-card">
          <div class="chart-container">
            <VChart v-if="courseCategoryData.length > 0" :option="courseCategoryOption" autoresize />
            <n-empty v-else description="暂无数据" />
          </div>
        </n-card>

        <n-card title="课程状态分布" class="chart-card">
          <div class="chart-container">
            <VChart v-if="courseStatusData.length > 0" :option="courseStatusOption" autoresize />
            <n-empty v-else description="暂无数据" />
          </div>
        </n-card>
      </div>

      <!-- 第三行：分布饼图 -->
      <div class="charts-row">
        <n-card title="订单状态分布" class="chart-card">
          <div class="chart-container">
            <VChart v-if="orderStatusData.length > 0" :option="orderStatusOption" autoresize />
            <n-empty v-else description="暂无数据" />
          </div>
        </n-card>

        <n-card title="学习状态分布" class="chart-card">
          <div class="chart-container">
            <VChart v-if="learningStatusData.length > 0" :option="learningStatusOption" autoresize />
            <n-empty v-else description="暂无数据" />
          </div>
        </n-card>
      </div>

      <!-- 第四行：趋势图 -->
      <div class="charts-row">
        <n-card title="用户注册趋势" class="chart-card">
          <template #header-extra>
            <n-select
              v-model:value="userRegTrendDays"
              :options="daysOptions"
              style="width: 120px"
              @update:value="loadUserRegistrationTrend"
            />
          </template>
          <div class="chart-container">
            <VChart v-if="userRegTrendData.length > 0" :option="userRegTrendOption" autoresize />
            <n-empty v-else description="暂无数据" />
          </div>
        </n-card>

        <n-card title="订单金额趋势" class="chart-card">
          <template #header-extra>
            <n-select
              v-model:value="orderRevenueTrendDays"
              :options="daysOptions"
              style="width: 120px"
              @update:value="loadOrderRevenueTrend"
            />
          </template>
          <div class="chart-container">
            <VChart v-if="orderRevenueTrendData.length > 0" :option="orderRevenueTrendOption" autoresize />
            <n-empty v-else description="暂无数据" />
          </div>
        </n-card>
      </div>

      <!-- 第五行：趋势图 -->
      <div class="charts-row">
        <n-card title="订单数量趋势" class="chart-card">
          <template #header-extra>
            <n-select
              v-model:value="orderCountTrendDays"
              :options="daysOptions"
              style="width: 120px"
              @update:value="loadOrderCountTrend"
            />
          </template>
          <div class="chart-container">
            <VChart v-if="orderCountTrendData.length > 0" :option="orderCountTrendOption" autoresize />
            <n-empty v-else description="暂无数据" />
          </div>
        </n-card>

        <n-card title="课程创建趋势" class="chart-card">
          <template #header-extra>
            <n-select
              v-model:value="courseCreationTrendDays"
              :options="daysOptions"
              style="width: 120px"
              @update:value="loadCourseCreationTrend"
            />
          </template>
          <div class="chart-container">
            <VChart v-if="courseCreationTrendData.length > 0" :option="courseCreationTrendOption" autoresize />
            <n-empty v-else description="暂无数据" />
          </div>
        </n-card>
      </div>

      <!-- 第六行：排行图 -->
      <div class="charts-row">
        <n-card title="课程浏览量排行" class="chart-card">
          <template #header-extra>
            <n-select
              v-model:value="topCoursesByViewsLimit"
              :options="limitOptions"
              style="width: 120px"
              @update:value="loadTopCoursesByViews"
            />
          </template>
          <div class="chart-container">
            <VChart v-if="topCoursesByViewsData.length > 0" :option="topCoursesByViewsOption" autoresize />
            <n-empty v-else description="暂无数据" />
          </div>
        </n-card>

        <n-card title="课程销售额排行" class="chart-card">
          <template #header-extra>
            <n-select
              v-model:value="topCoursesByRevenueLimit"
              :options="limitOptions"
              style="width: 120px"
              @update:value="loadTopCoursesByRevenue"
            />
          </template>
          <div class="chart-container">
            <VChart v-if="topCoursesByRevenueData.length > 0" :option="topCoursesByRevenueOption" autoresize />
            <n-empty v-else description="暂无数据" />
          </div>
        </n-card>
      </div>

      <!-- 第七行：教师收入排行 -->
      <div class="charts-row">
        <n-card title="教师收入排行" class="chart-card">
          <template #header-extra>
            <n-select
              v-model:value="topTeachersLimit"
              :options="limitOptions"
              style="width: 120px"
              @update:value="loadTopTeachers"
            />
          </template>
          <div class="chart-container">
            <VChart v-if="topTeachersData.length > 0" :option="topTeachersOption" autoresize />
            <n-empty v-else description="暂无数据" />
          </div>
        </n-card>
      </div>
    </div>
    </div>
  </AppPage>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { NCard, NStatistic, NSelect, NEmpty, NButton, useMessage } from 'naive-ui'
import { PageHeader, AppPage } from '@/components'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import * as echarts from 'echarts/core'
import { UniversalTransition } from 'echarts/features'
import { CanvasRenderer } from 'echarts/renderers'
import VChart from 'vue-echarts'
import api from './api'

const message = useMessage()

// 注册 ECharts 组件
echarts.use([
  TooltipComponent,
  GridComponent,
  LegendComponent,
  BarChart,
  LineChart,
  CanvasRenderer,
  UniversalTransition,
  PieChart,
])

// 响应式数据
const loading = ref(false)
const overview = reactive({})

// 分布数据
const userRoleData = ref([])
const userStatusData = ref([])
const courseCategoryData = ref([])
const courseStatusData = ref([])
const orderStatusData = ref([])
const learningStatusData = ref([])

// 趋势数据
const userRegTrendDays = ref(30)
const orderRevenueTrendDays = ref(30)
const orderCountTrendDays = ref(30)
const courseCreationTrendDays = ref(30)
const userRegTrendData = ref([])
const orderRevenueTrendData = ref([])
const orderCountTrendData = ref([])
const courseCreationTrendData = ref([])

// 排行数据
const topCoursesByViewsLimit = ref(10)
const topCoursesByRevenueLimit = ref(10)
const topTeachersLimit = ref(10)
const topCoursesByViewsData = ref([])
const topCoursesByRevenueData = ref([])
const topTeachersData = ref([])

// 选项配置
const daysOptions = [
  { label: '最近7天', value: 7 },
  { label: '最近30天', value: 30 },
  { label: '最近90天', value: 90 },
  { label: '最近180天', value: 180 },
  { label: '最近365天', value: 365 }
]

const limitOptions = [
  { label: 'TOP 5', value: 5 },
  { label: 'TOP 10', value: 10 },
  { label: 'TOP 20', value: 20 },
  { label: 'TOP 50', value: 50 }
]

// 饼图配置
const createPieOption = (data, name) => ({
  tooltip: {
    trigger: 'item',
    formatter: '{a} <br/>{b}: {c} ({d}%)'
  },
  legend: {
    orient: 'vertical',
    left: 'left'
  },
  series: [
    {
      name,
      type: 'pie',
      radius: '50%',
      data,
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }
  ]
})

const userRoleOption = computed(() => createPieOption(
  userRoleData.value.map(item => ({ value: item.value, name: item.name })),
  '用户角色'
))

const userStatusOption = computed(() => createPieOption(
  userStatusData.value.map(item => ({ value: item.value, name: item.name })),
  '用户状态'
))

const courseCategoryOption = computed(() => createPieOption(
  courseCategoryData.value.map(item => ({ value: item.value, name: item.name })),
  '课程分类'
))

const courseStatusOption = computed(() => createPieOption(
  courseStatusData.value.map(item => ({ value: item.value, name: item.name })),
  '课程状态'
))

const orderStatusOption = computed(() => createPieOption(
  orderStatusData.value.map(item => ({ value: item.value, name: item.name })),
  '订单状态'
))

const learningStatusOption = computed(() => createPieOption(
  learningStatusData.value.map(item => ({ value: item.value, name: item.name })),
  '学习状态'
))

// 折线图配置
const createLineOption = (data, name, valueKey = 'count', formatter) => ({
  tooltip: {
    trigger: 'axis',
    formatter: formatter || ((params) => {
      const param = params[0]
      return `${param.name}<br/>${param.seriesName}: ${param.value}`
    })
  },
  xAxis: {
    type: 'category',
    data: data.map(item => item.date)
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      name,
      type: 'line',
      data: data.map(item => item[valueKey] || 0),
      smooth: true,
      areaStyle: {}
    }
  ]
})

const userRegTrendOption = computed(() => createLineOption(
  userRegTrendData.value,
  '新增用户',
  'count'
))

const orderRevenueTrendOption = computed(() => createLineOption(
  orderRevenueTrendData.value,
  '订单金额',
  'amount',
  (params) => {
    const param = params[0]
    return `${param.name}<br/>${param.seriesName}: ¥${param.value}`
  }
))

const orderCountTrendOption = computed(() => createLineOption(
  orderCountTrendData.value,
  '订单数量',
  'count'
))

const courseCreationTrendOption = computed(() => createLineOption(
  courseCreationTrendData.value,
  '新增课程',
  'count'
))

// 柱状图配置
const createBarOption = (data, name, valueKey, labelFormatter, tooltipFormatter) => ({
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    },
    formatter: tooltipFormatter || ((params) => {
      const param = params[0]
      return `${param.name}<br/>${param.seriesName}: ${param.value}`
    })
  },
  xAxis: {
    type: 'value'
  },
  yAxis: {
    type: 'category',
    data: data.map(item => item.name)
  },
  series: [
    {
      name,
      type: 'bar',
      data: data.map(item => item[valueKey] || 0),
      label: {
        show: true,
        formatter: labelFormatter ? (params) => labelFormatter(params.value) : undefined
      }
    }
  ]
})

const topCoursesByViewsOption = computed(() => createBarOption(
  topCoursesByViewsData.value,
  '浏览量',
  'count',
  null,
  null
))

const topCoursesByRevenueOption = computed(() => createBarOption(
  topCoursesByRevenueData.value,
  '销售额',
  'amount',
  (value) => `¥${value}`,
  (params) => {
    const param = params[0]
    return `${param.name}<br/>${param.seriesName}: ¥${param.value}`
  }
))

const topTeachersOption = computed(() => createBarOption(
  topTeachersData.value,
  '收入',
  'amount',
  (value) => `¥${value}`,
  (params) => {
    const param = params[0]
    return `${param.name}<br/>${param.seriesName}: ¥${param.value}`
  }
))

// 加载数据方法
const loadOverview = async () => {
  try {
    const response = await api.getOverview()
    Object.assign(overview, response.data || {})
  } catch (error) {
    console.error('加载概览数据失败:', error)
    message.error('加载概览数据失败')
  }
}

const loadDistributions = async () => {
  try {
    const [
      userRoleRes,
      userStatusRes,
      courseCategoryRes,
      courseStatusRes,
      orderStatusRes,
      learningStatusRes
    ] = await Promise.all([
      api.getUserRoleDistribution(),
      api.getUserStatusDistribution(),
      api.getCourseCategoryDistribution(),
      api.getCourseStatusDistribution(),
      api.getOrderStatusDistribution(),
      api.getLearningStatusDistribution()
    ])

    userRoleData.value = userRoleRes.data || []
    userStatusData.value = userStatusRes.data || []
    courseCategoryData.value = courseCategoryRes.data || []
    courseStatusData.value = courseStatusRes.data || []
    orderStatusData.value = orderStatusRes.data || []
    learningStatusData.value = learningStatusRes.data || []
  } catch (error) {
    console.error('加载分布数据失败:', error)
  }
}

const loadUserRegistrationTrend = async () => {
  try {
    const response = await api.getUserRegistrationTrend({ days: userRegTrendDays.value })
    const rawData = response.data || []
    
    // 填充缺失的日期数据
    if (rawData.length > 0) {
      const dataMap = new Map(rawData.map(item => [item.date, item]))
      const filledData = []
      const endDate = new Date()
      const startDate = new Date()
      startDate.setDate(endDate.getDate() - userRegTrendDays.value + 1)
      
      for (let d = new Date(startDate); d <= endDate; d.setDate(d.getDate() + 1)) {
        const dateStr = d.toISOString().split('T')[0]
        filledData.push(dataMap.get(dateStr) || { date: dateStr, count: 0 })
      }
      
      userRegTrendData.value = filledData
    } else {
      userRegTrendData.value = []
    }
  } catch (error) {
    console.error('加载用户注册趋势失败:', error)
  }
}

const loadOrderRevenueTrend = async () => {
  try {
    const response = await api.getOrderRevenueTrend({ days: orderRevenueTrendDays.value })
    const rawData = response.data || []
    
    // 填充缺失的日期数据
    if (rawData.length > 0) {
      const dataMap = new Map(rawData.map(item => [item.date, item]))
      const filledData = []
      const endDate = new Date()
      const startDate = new Date()
      startDate.setDate(endDate.getDate() - orderRevenueTrendDays.value + 1)
      
      for (let d = new Date(startDate); d <= endDate; d.setDate(d.getDate() + 1)) {
        const dateStr = d.toISOString().split('T')[0]
        filledData.push(dataMap.get(dateStr) || { date: dateStr, amount: 0 })
      }
      
      orderRevenueTrendData.value = filledData
    } else {
      orderRevenueTrendData.value = []
    }
  } catch (error) {
    console.error('加载订单金额趋势失败:', error)
  }
}

const loadOrderCountTrend = async () => {
  try {
    const response = await api.getOrderCountTrend({ days: orderCountTrendDays.value })
    const rawData = response.data || []
    
    // 填充缺失的日期数据
    if (rawData.length > 0) {
      const dataMap = new Map(rawData.map(item => [item.date, item]))
      const filledData = []
      const endDate = new Date()
      const startDate = new Date()
      startDate.setDate(endDate.getDate() - orderCountTrendDays.value + 1)
      
      for (let d = new Date(startDate); d <= endDate; d.setDate(d.getDate() + 1)) {
        const dateStr = d.toISOString().split('T')[0]
        filledData.push(dataMap.get(dateStr) || { date: dateStr, count: 0, amount: 0, percentage: 0 })
      }
      
      orderCountTrendData.value = filledData
    } else {
      orderCountTrendData.value = []
    }
  } catch (error) {
    console.error('加载订单数量趋势失败:', error)
  }
}

const loadCourseCreationTrend = async () => {
  try {
    const response = await api.getCourseCreationTrend({ days: courseCreationTrendDays.value })
    const rawData = response.data || []
    
    // 填充缺失的日期数据
    if (rawData.length > 0) {
      const dataMap = new Map(rawData.map(item => [item.date, item]))
      const filledData = []
      const endDate = new Date()
      const startDate = new Date()
      startDate.setDate(endDate.getDate() - courseCreationTrendDays.value + 1)
      
      for (let d = new Date(startDate); d <= endDate; d.setDate(d.getDate() + 1)) {
        const dateStr = d.toISOString().split('T')[0]
        filledData.push(dataMap.get(dateStr) || { date: dateStr, count: 0 })
      }
      
      courseCreationTrendData.value = filledData
    } else {
      courseCreationTrendData.value = []
    }
  } catch (error) {
    console.error('加载课程创建趋势失败:', error)
  }
}

const loadTopCoursesByViews = async () => {
  try {
    const response = await api.getTopCoursesByViews({ limit: topCoursesByViewsLimit.value })
    topCoursesByViewsData.value = response.data || []
  } catch (error) {
    console.error('加载课程浏览量排行失败:', error)
  }
}

const loadTopCoursesByRevenue = async () => {
  try {
    const response = await api.getTopCoursesByRevenue({ limit: topCoursesByRevenueLimit.value })
    topCoursesByRevenueData.value = response.data || []
  } catch (error) {
    console.error('加载课程销售额排行失败:', error)
  }
}

const loadTopTeachers = async () => {
  try {
    const response = await api.getTopTeachers({ limit: topTeachersLimit.value })
    topTeachersData.value = response.data || []
  } catch (error) {
    console.error('加载教师收入排行失败:', error)
  }
}

const refreshData = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadOverview(),
      loadDistributions(),
      loadUserRegistrationTrend(),
      loadOrderRevenueTrend(),
      loadOrderCountTrend(),
      loadCourseCreationTrend(),
      loadTopCoursesByViews(),
      loadTopCoursesByRevenue(),
      loadTopTeachers()
    ])
    message.success('数据刷新成功')
  } catch (error) {
    message.error('数据刷新失败')
  } finally {
    loading.value = false
  }
}

// 生命周期
onMounted(() => {
  refreshData()
})
</script>

<style scoped>
.admin-dashboard {
  width: 100%;
}

.overview-section {
  margin-bottom: 20px;
}

.overview-card {
  border-radius: 8px;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
}

.overview-group {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.group-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 16px;
  color: #333;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 16px;
}

.charts-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.charts-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 20px;
}

.chart-card {
  border-radius: 8px;
}

.chart-container {
  height: 400px;
  width: 100%;
}

@media (max-width: 1200px) {
  .charts-row {
    grid-template-columns: 1fr;
  }
  
  .overview-grid {
    grid-template-columns: 1fr;
  }
}
</style>

