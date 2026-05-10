<template>
  <div class="statistics-container">
    <!-- 统计卡片 -->
    <div class="stats-cards">
      <n-card class="stat-card hover-card">
        <div class="stat-content">
          <div class="stat-icon star-icon">
            <div class="i-carbon:star"></div>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ teacherStats.averageRating || 5 }}</div>
            <div class="stat-label">平均评分</div>
          </div>
        </div>
      </n-card>

      <n-card class="stat-card hover-card">
        <div class="stat-content">
          <div class="stat-icon user-icon">
            <div class="i-carbon:user-multiple"></div>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ teacherStats.totalStudents || 1 }}</div>
            <div class="stat-label">总学生数</div>
          </div>
        </div>
      </n-card>

      <n-card class="stat-card hover-card">
        <div class="stat-content">
          <div class="stat-icon book-icon">
            <div class="i-carbon:book"></div>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ teacherStats.totalCourses || 1 }}</div>
            <div class="stat-label">总课程数</div>
          </div>
        </div>
      </n-card>
    </div>

    <!-- 作业统计卡片 -->
    <div class="homework-stats">
      <n-card title="作业统计" class="homework-stats-card">
        <div class="homework-stats-grid">
          <div class="homework-stat-item">
            <div class="homework-stat-value">{{ homeworkStats.totalHomework || 0 }}</div>
            <div class="homework-stat-label">总作业数</div>
          </div>
          <div class="homework-stat-item">
            <div class="homework-stat-value">{{ homeworkStats.publishedHomework || 0 }}</div>
            <div class="homework-stat-label">已发布</div>
          </div>
          <div class="homework-stat-item">
            <div class="homework-stat-value">{{ homeworkStats.upcomingDeadlines || 0 }}</div>
            <div class="homework-stat-label">即将到期</div>
          </div>
          <div class="homework-stat-item">
            <div class="homework-stat-value">{{ homeworkStats.totalSubmissions || 0 }}</div>
            <div class="homework-stat-label">总提交数</div>
          </div>
          <div class="homework-stat-item">
            <div class="homework-stat-value">{{ homeworkStats.gradedSubmissions || 0 }}</div>
            <div class="homework-stat-label">已批改</div>
          </div>
          <div class="homework-stat-item">
            <div class="homework-stat-value">{{ homeworkStats.pendingGrading || 0 }}</div>
            <div class="homework-stat-label">待批改</div>
          </div>
        </div>
      </n-card>
    </div>

    <!-- 内容区域 -->
    <div class="content-area">
      <!-- 最近课程 -->
      <n-card title="最近课程" class="recent-courses">
        <n-spin :show="coursesLoading">
          <div v-if="recentCourses.length === 0" class="empty-state">
            <n-empty description="暂无课程数据" />
          </div>
          <div v-else class="courses-list">
            <div 
              v-for="course in recentCourses" 
              :key="course.id" 
              class="course-item"
            >
              <div class="course-cover">
                <img :src="course.cover" :alt="course.title" />
              </div>
              <div class="course-info">
                <h3 class="course-title">{{ course.title }}</h3>
                <p class="course-description">{{ course.description }}</p>
                <div class="course-meta">
                  <span class="course-price">¥{{ course.price }}</span>
                  <span class="course-lessons">{{ course.lessonNum }}课时</span>
                  <span class="course-duration">{{ course.durationSum }}分钟</span>
                </div>
                <div class="course-stats">
                  <span class="view-count">观看: {{ course.viewCount }}</span>
                  <span class="buy-count">购买: {{ course.buyCount }}</span>
                </div>
              </div>
            </div>
          </div>
        </n-spin>
      </n-card>

      <!-- 待批改作业 -->
      <n-card title="待批改作业" class="pending-homeworks">
        <n-spin :show="homeworksLoading">
          <div v-if="pendingHomeworks.length === 0" class="empty-state">
            <n-empty description="暂无待批改作业" />
          </div>
          <div v-else class="homeworks-list">
            <div 
              v-for="homework in pendingHomeworks" 
              :key="homework.id" 
              class="homework-item"
            >
              <div class="homework-info">
                <h3 class="homework-title">{{ homework.title }}</h3>
                <p class="homework-content">{{ homework.content }}</p>
                <div class="homework-meta">
                  <span class="homework-score">总分: {{ homework.score }}</span>
                  <span class="homework-status" :class="getStatusClass(homework.status)">
                    {{ getStatusText(homework.status) }}
                  </span>
                </div>
                <div class="homework-time">
                  <span class="start-time">开始: {{ formatTime(homework.startTime) }}</span>
                  <span class="end-time">截止: {{ formatTime(homework.endTime) }}</span>
                </div>
              </div>
              <div class="homework-actions">
                <n-button type="primary" size="small" @click="handleGradeHomework(homework)">
                  批改作业
                </n-button>
              </div>
            </div>
          </div>
        </n-spin>
      </n-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { NCard, NIcon, NSpin, NEmpty, NButton } from 'naive-ui'
import api from './api'
import { router } from '@/router'

// 响应式数据
const teacherStats = ref({})
const recentCourses = ref([])
const pendingHomeworks = ref([])
const homeworkStats = ref({})

const coursesLoading = ref(false)
const homeworksLoading = ref(false)

// 获取教师统计数据
const getTeacherStats = async () => {
  try {
    const { data } = await api.getStatistics()
    teacherStats.value = data
  } catch (error) {
    console.error('获取教师统计数据失败:', error)
  }
}

// 获取最近课程
const getRecentCourses = async () => {
  coursesLoading.value = true
  try {
    const { data } = await api.getRecentCourses({ page: 1, size: 5 })
    recentCourses.value = data.list || []
  } catch (error) {
    console.error('获取最近课程失败:', error)
  } finally {
    coursesLoading.value = false
  }
}

// 获取待批改作业
const getPendingHomeworks = async () => {
  homeworksLoading.value = true
  try {
    const { data } = await api.getPendingHomeworks()
    pendingHomeworks.value = data || []
  } catch (error) {
    console.error('获取待批改作业失败:', error)
  } finally {
    homeworksLoading.value = false
  }
}

// 获取作业统计数据
const getHomeworkStats = async () => {
  try {
    const { data } = await api.getHomeworkStatistics()
    homeworkStats.value = data
  } catch (error) {
    console.error('获取作业统计数据失败:', error)
  }
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN')
}

// 获取状态样式类
const getStatusClass = (status) => {
  const statusMap = {
    0: 'status-draft',
    1: 'status-published',
    2: 'status-ended'
  }
  return statusMap[status] || ''
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '草稿',
    1: '已发布',
    2: '已结束'
  }
  return statusMap[status] || '未知'
}

// 处理批改作业
const handleGradeHomework = (homework) => {
  // 跳转到作业批改页面，传递作业ID
  // window.open(`/teacherhome/grade?homeworkId=${homework.id}`, '_blank')
  // router.push(`/teacherhome/grade?homeworkId=${homework.id}`)
  router.push(`/teacherhome/grade`)
}

// 页面加载时获取数据
onMounted(() => {
  getTeacherStats()
  getRecentCourses()
  getPendingHomeworks()
  getHomeworkStats()
})
</script>

<style scoped>
.statistics-container {
  padding: 20px;
  /* max-width: 1200px;
  margin: 0 auto; */
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(24, 160, 88, 0.1);
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

.homework-stats {
  margin-bottom: 20px;
}

.homework-stats-card {
  border-radius: 8px;
}

.homework-stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 20px;
}

.homework-stat-item {
  text-align: center;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.homework-stat-value {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.homework-stat-label {
  font-size: 12px;
  color: #666;
}

.content-area {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.recent-courses,
.pending-homeworks {
  border-radius: 8px;
}

.empty-state {
  padding: 40px;
  text-align: center;
}

.courses-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.course-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.course-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.course-cover {
  width: 80px;
  height: 60px;
  border-radius: 4px;
  overflow: hidden;
}

.course-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.course-info {
  flex: 1;
}

.course-title {
  font-size: 16px;
  font-weight: bold;
  margin: 0 0 8px 0;
  color: #333;
}

.course-description {
  font-size: 14px;
  color: #666;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.course-meta {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
}

.course-meta span {
  font-size: 12px;
  color: #888;
}

.course-price {
  color: #f56565 !important;
  font-weight: bold;
}

.course-stats {
  display: flex;
  gap: 12px;
}

.course-stats span {
  font-size: 12px;
  color: #888;
}

.homeworks-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.homework-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.homework-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.homework-info {
  flex: 1;
}

.homework-title {
  font-size: 16px;
  font-weight: bold;
  margin: 0 0 8px 0;
  color: #333;
}

.homework-content {
  font-size: 14px;
  color: #666;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.homework-meta {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
}

.homework-meta span {
  font-size: 12px;
}

.homework-score {
  color: #f56565;
  font-weight: bold;
}

.homework-status {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-draft {
  background-color: #fef5e7;
  color: #d69e2e;
}

.status-published {
  background-color: #e6fffa;
  color: #38b2ac;
}

.status-ended {
  background-color: #fed7d7;
  color: #e53e3e;
}

.homework-time {
  display: flex;
  gap: 12px;
}

.homework-time span {
  font-size: 12px;
  color: #888;
}

.homework-actions {
  margin-left: 16px;
}

@media (max-width: 768px) {
  .content-area {
    grid-template-columns: 1fr;
  }
  
  .stats-cards {
    grid-template-columns: 1fr;
  }
  
  .homework-stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>