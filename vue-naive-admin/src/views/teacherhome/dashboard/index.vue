<template>
  <AppPage>
    <div class="teacher-dashboard">
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
          <!-- 课程相关 -->
          <div class="overview-group">
            <h3 class="group-title">课程相关</h3>
            <div class="stats-grid">
              <n-statistic label="总课程数" :value="overview.totalCourses || 0" />
              <n-statistic label="已发布" :value="overview.publishedCourses || 0" />
              <n-statistic label="未发布" :value="overview.unpublishedCourses || 0" />
              <n-statistic label="总浏览量" :value="overview.totalViews || 0" />
              <n-statistic label="总销售额" :value="overview.totalRevenue || 0">
                <template #suffix>元</template>
              </n-statistic>
              <n-statistic label="平均评分" :value="overview.avgRating || 0" :precision="1">
                <template #suffix>/5</template>
              </n-statistic>
              <n-statistic label="评论数" :value="overview.totalComments || 0" />
            </div>
          </div>

          <!-- 学员相关 -->
          <div class="overview-group">
            <h3 class="group-title">学员相关</h3>
            <div class="stats-grid">
              <n-statistic label="总学员数" :value="overview.totalStudents || 0" />
              <n-statistic label="已加入" :value="overview.joinedStudents || 0" />
              <n-statistic label="学习中" :value="overview.studyingStudents || 0" />
              <n-statistic label="已完成" :value="overview.completedStudents || 0" />
              <n-statistic label="完成率" :value="overview.completionRate || 0" :precision="1">
                <template #suffix>%</template>
              </n-statistic>
            </div>
          </div>

          <!-- 作业相关 -->
          <div class="overview-group">
            <h3 class="group-title">作业相关</h3>
            <div class="stats-grid">
              <n-statistic label="总作业数" :value="overview.totalHomeworks || 0" />
              <n-statistic label="已发布" :value="overview.publishedHomeworks || 0" />
              <n-statistic label="待批改" :value="overview.pendingGradingCount || 0" />
              <n-statistic label="已批改" :value="overview.gradedCount || 0" />
              <n-statistic label="平均分" :value="overview.avgHomeworkScore || 0" :precision="1">
                <template #suffix>分</template>
              </n-statistic>
            </div>
          </div>

          <!-- 收入相关 -->
          <div class="overview-group">
            <h3 class="group-title">收入相关</h3>
            <div class="stats-grid">
              <n-statistic label="今日收入" :value="overview.todayRevenue || 0">
                <template #suffix>元</template>
              </n-statistic>
              <n-statistic label="本月收入" :value="overview.monthRevenue || 0">
                <template #suffix>元</template>
              </n-statistic>
              <n-statistic label="累计收入" :value="overview.cumulativeRevenue || 0">
                <template #suffix>元</template>
              </n-statistic>
              <n-statistic label="今日订单数" :value="overview.todayOrderCount || 0" />
            </div>
          </div>
        </div>
      </n-card>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <!-- 第一行：饼图 -->
      <div class="charts-row">
        <n-card title="课程状态分布" class="chart-card">
          <div class="chart-container">
            <VChart v-if="courseStatusData.length > 0" :option="courseStatusOption" autoresize />
            <n-empty v-else description="暂无数据" />
          </div>
        </n-card>

        <n-card title="学员学习状态分布" class="chart-card">
          <div class="chart-container">
            <VChart v-if="studentStatusData.length > 0" :option="studentStatusOption" autoresize />
            <n-empty v-else description="暂无数据" />
          </div>
        </n-card>
      </div>

      <!-- 第二行：趋势图 -->
      <div class="charts-row">
        <n-card title="课程浏览量趋势" class="chart-card">
          <template #header-extra>
            <n-select
              v-model:value="trendDays"
              :options="daysOptions"
              style="width: 120px"
              @update:value="loadTrendData"
            />
          </template>
          <div class="chart-container">
            <VChart v-if="courseViewTrendData.length > 0" :option="courseViewTrendOption" autoresize />
            <n-empty v-else description="暂无数据" />
          </div>
        </n-card>

        <n-card title="收入趋势" class="chart-card">
          <template #header-extra>
            <n-select
              v-model:value="revenueTrendDays"
              :options="daysOptions"
              style="width: 120px"
              @update:value="loadRevenueTrend"
            />
          </template>
          <div class="chart-container">
            <VChart v-if="revenueTrendData.length > 0" :option="revenueTrendOption" autoresize />
            <n-empty v-else description="暂无数据" />
          </div>
        </n-card>
      </div>

      <!-- 第三行：趋势图和排行 -->
      <div class="charts-row">
        <n-card title="学员增长趋势" class="chart-card">
          <template #header-extra>
            <n-select
              v-model:value="studentGrowthDays"
              :options="daysOptions"
              style="width: 120px"
              @update:value="loadStudentGrowthTrend"
            />
          </template>
          <div class="chart-container">
            <VChart v-if="studentGrowthTrendData.length > 0" :option="studentGrowthTrendOption" autoresize />
            <n-empty v-else description="暂无数据" />
          </div>
        </n-card>

        <n-card title="课程销售排行" class="chart-card">
          <template #header-extra>
            <n-select
              v-model:value="topCoursesLimit"
              :options="limitOptions"
              style="width: 120px"
              @update:value="loadTopCourses"
            />
          </template>
          <div class="chart-container">
            <VChart v-if="topCoursesData.length > 0" :option="topCoursesOption" autoresize />
            <n-empty v-else description="暂无数据" />
          </div>
        </n-card>
      </div>

      <!-- 第四行：完成率排行 -->
      <div class="charts-row">
        <n-card title="课程完成率排行" class="chart-card">
          <template #header-extra>
            <n-select
              v-model:value="completionRankingLimit"
              :options="limitOptions"
              style="width: 120px"
              @update:value="loadCompletionRanking"
            />
          </template>
          <div class="chart-container">
            <VChart v-if="completionRankingData.length > 0" :option="completionRankingOption" autoresize />
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
const courseStatusData = ref([])
const studentStatusData = ref([])

// 趋势数据
const trendDays = ref(30)
const revenueTrendDays = ref(30)
const studentGrowthDays = ref(30)
const courseViewTrendData = ref([])
const revenueTrendData = ref([])
const studentGrowthTrendData = ref([])

// 排行数据
const topCoursesLimit = ref(10)
const completionRankingLimit = ref(10)
const topCoursesData = ref([])
const completionRankingData = ref([])

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
const courseStatusOption = computed(() => ({
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
      name: '课程状态',
      type: 'pie',
      radius: '50%',
      data: courseStatusData.value,
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }
  ]
}))

const studentStatusOption = computed(() => ({
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
      name: '学习状态',
      type: 'pie',
      radius: '50%',
      data: studentStatusData.value,
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }
  ]
}))

// 折线图配置
const courseViewTrendOption = computed(() => ({
  tooltip: {
    trigger: 'axis'
  },
  xAxis: {
    type: 'category',
    data: courseViewTrendData.value.map(item => item.date)
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      name: '浏览量',
      type: 'line',
      data: courseViewTrendData.value.map(item => item.count),
      smooth: true,
      areaStyle: {}
    }
  ]
}))

const revenueTrendOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    formatter: (params) => {
      const param = params[0]
      return `${param.name}<br/>${param.seriesName}: ¥${param.value}`
    }
  },
  xAxis: {
    type: 'category',
    data: revenueTrendData.value.map(item => item.date)
  },
  yAxis: {
    type: 'value',
    axisLabel: {
      formatter: (value) => `¥${value}`
    }
  },
  series: [
    {
      name: '收入',
      type: 'line',
      data: revenueTrendData.value.map(item => item.amount || 0),
      smooth: true,
      areaStyle: {}
    }
  ]
}))

const studentGrowthTrendOption = computed(() => ({
  tooltip: {
    trigger: 'axis'
  },
  xAxis: {
    type: 'category',
    data: studentGrowthTrendData.value.map(item => item.date)
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      name: '新增学员',
      type: 'line',
      data: studentGrowthTrendData.value.map(item => item.count),
      smooth: true,
      areaStyle: {}
    }
  ]
}))

// 柱状图配置
const topCoursesOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    }
  },
  xAxis: {
    type: 'value'
  },
  yAxis: {
    type: 'category',
    data: topCoursesData.value.map(item => item.name)
  },
  series: [
    {
      name: '销售额',
      type: 'bar',
      data: topCoursesData.value.map(item => item.amount || 0),
      label: {
        show: true,
        formatter: (params) => `¥${params.value}`
      }
    }
  ]
}))

const completionRankingOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    formatter: (params) => {
      const param = params[0]
      return `${param.name}<br/>完成率: ${param.value}%`
    }
  },
  xAxis: {
    type: 'value',
    max: 100,
    axisLabel: {
      formatter: '{value}%'
    }
  },
  yAxis: {
    type: 'category',
    data: completionRankingData.value.map(item => item.name)
  },
  series: [
    {
      name: '完成率',
      type: 'bar',
      data: completionRankingData.value.map(item => item.completionRate || 0),
      label: {
        show: true,
        formatter: (params) => `${params.value}%`
      }
    }
  ]
}))

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

const loadCourseStatusDistribution = async () => {
  try {
    const response = await api.getCourseStatusDistribution()
    courseStatusData.value = (response.data || []).map(item => ({
      value: item.value,
      name: item.name
    }))
  } catch (error) {
    console.error('加载课程状态分布失败:', error)
  }
}

const loadStudentStatusDistribution = async () => {
  try {
    const response = await api.getStudentStatusDistribution()
    studentStatusData.value = (response.data || []).map(item => ({
      value: item.value,
      name: item.name
    }))
  } catch (error) {
    console.error('加载学员状态分布失败:', error)
  }
}

const loadTrendData = async () => {
  try {
    const response = await api.getCourseViewTrend({ days: trendDays.value })
    courseViewTrendData.value = response.data || []
  } catch (error) {
    console.error('加载浏览量趋势失败:', error)
  }
}

const loadRevenueTrend = async () => {
  try {
    const response = await api.getRevenueTrend({ days: revenueTrendDays.value })
    revenueTrendData.value = response.data || []
  } catch (error) {
    console.error('加载收入趋势失败:', error)
  }
}

const loadStudentGrowthTrend = async () => {
  try {
    const response = await api.getStudentGrowthTrend({ days: studentGrowthDays.value })
    studentGrowthTrendData.value = response.data || []
  } catch (error) {
    console.error('加载学员增长趋势失败:', error)
  }
}

const loadTopCourses = async () => {
  try {
    const response = await api.getTopCourses({ limit: topCoursesLimit.value })
    topCoursesData.value = response.data || []
  } catch (error) {
    console.error('加载课程销售排行失败:', error)
  }
}

const loadCompletionRanking = async () => {
  try {
    const response = await api.getCourseCompletionRanking({ limit: completionRankingLimit.value })
    completionRankingData.value = response.data || []
  } catch (error) {
    console.error('加载完成率排行失败:', error)
  }
}

const refreshData = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadOverview(),
      loadCourseStatusDistribution(),
      loadStudentStatusDistribution(),
      loadTrendData(),
      loadRevenueTrend(),
      loadStudentGrowthTrend(),
      loadTopCourses(),
      loadCompletionRanking()
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
.teacher-dashboard {
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

