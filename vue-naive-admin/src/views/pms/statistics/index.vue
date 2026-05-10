<template>
  <div class="statistics-dashboard">
    <n-card title="数据统计仪表板" :bordered="false">
      <!-- 概览统计卡片 -->
      <div class="overview-cards mb-6">
        <n-grid :cols="4" :x-gap="16" :y-gap="16">
          <n-grid-item>
            <n-card size="small" hoverable>
              <n-statistic label="总用户数" :value="userStats.totalUsers" />
              <template #footer>
                <n-text depth="3">用户统计</n-text>
              </template>
            </n-card>
          </n-grid-item>
          <n-grid-item>
            <n-card size="small" hoverable>
              <n-statistic label="总课程数" :value="courseStats.totalCourses" />
              <template #footer>
                <n-text depth="3">课程统计</n-text>
              </template>
            </n-card>
          </n-grid-item>
          <n-grid-item>
            <n-card size="small" hoverable>
              <n-statistic label="总订单数" :value="orderStats.totalOrders" />
              <template #footer>
                <n-text depth="3">订单统计</n-text>
              </template>
            </n-card>
          </n-grid-item>
          <n-grid-item>
            <n-card size="small" hoverable>
              <n-statistic label="总收入" :value="revenueStats.totalRevenue">
                <template #prefix>¥</template>
              </n-statistic>
              <template #footer>
                <n-text depth="3">收入统计</n-text>
              </template>
            </n-card>
          </n-grid-item>
        </n-grid>
      </div>

      <!-- 详细统计区域 -->
      <n-tabs type="line" animated>
        <!-- 用户统计 -->
        <n-tab-pane name="users" tab="用户统计">
          <n-card title="用户类型分布" size="small" class="mb-4">
            <n-grid :cols="3" :x-gap="16" :y-gap="16">
              <n-grid-item>
                <n-statistic label="学生用户" :value="userStats.studentCount" />
              </n-grid-item>
              <n-grid-item>
                <n-statistic label="教师用户" :value="userStats.teacherCount" />
              </n-grid-item>
              <n-grid-item>
                <n-statistic label="管理员" :value="userStats.adminCount" />
              </n-grid-item>
            </n-grid>
          </n-card>
        </n-tab-pane>

        <!-- 课程统计 -->
        <n-tab-pane name="courses" tab="课程统计">
          <n-grid :cols="2" :x-gap="16" :y-gap="16">
            <n-grid-item>
              <n-card title="课程统计" size="small">
                <n-grid :cols="2" :x-gap="16" :y-gap="16">
                  <n-grid-item>
                    <n-statistic label="总课程数" :value="courseStats.totalCourses" />
                  </n-grid-item>
                  <n-grid-item>
                    <n-statistic label="已发布课程" :value="courseStats.publishedCourses" />
                  </n-grid-item>
                  <n-grid-item>
                    <n-statistic label="未发布课程" :value="courseStats.unpublishedCourses" />
                  </n-grid-item>
                </n-grid>
              </n-card>
            </n-grid-item>
            <n-grid-item>
              <n-card title="浏览统计" size="small">
                <n-grid :cols="2" :x-gap="16" :y-gap="16">
                  <n-grid-item>
                    <n-statistic label="总浏览次数" :value="browseStats.totalViews" />
                  </n-grid-item>
                  <n-grid-item>
                    <n-statistic label="独立用户数" :value="browseStats.uniqueUsers" />
                  </n-grid-item>
                  <n-grid-item>
                    <n-statistic label="独立课程数" :value="browseStats.uniqueCourses" />
                  </n-grid-item>
                </n-grid>
              </n-card>
            </n-grid-item>
          </n-grid>
        </n-tab-pane>

        <!-- 学习统计 -->
        <n-tab-pane name="learning" tab="学习统计">
          <n-card title="总体学习统计" size="small" class="mb-4">
            <n-grid :cols="4" :x-gap="16" :y-gap="16">
              <n-grid-item>
                <n-statistic label="学习用户数" :value="learningStats.totalUsers" />
              </n-grid-item>
              <n-grid-item>
                <n-statistic label="总注册数" :value="learningStats.totalEnrollments" />
              </n-grid-item>
              <n-grid-item>
                <n-statistic label="课程总数" :value="learningStats.totalCourses" />
              </n-grid-item>
              <n-grid-item>
                <!-- <n-statistic label="完成率" :value="learningStats.completionRate" suffix="%" /> -->
              </n-grid-item>
            </n-grid>
          </n-card>

          <n-grid :cols="2" :x-gap="16" :y-gap="16">
            <n-grid-item>
              <n-card title="章节统计" size="small">
                <n-grid :cols="3" :x-gap="16" :y-gap="16">
                  <n-grid-item>
                    <n-statistic label="总章节数" :value="chapterStats.totalChapters" />
                  </n-grid-item>
                  <n-grid-item>
                    <n-statistic label="已发布小节" :value="chapterStats.publishedSections" />
                  </n-grid-item>
                  <n-grid-item>
                    <n-statistic label="未发布小节" :value="chapterStats.unpublishedSections" />
                  </n-grid-item>
                </n-grid>
              </n-card>
            </n-grid-item>
            <n-grid-item>
              <n-card title="小节统计" size="small">
                <n-grid :cols="3" :x-gap="16" :y-gap="16">
                  <n-grid-item>
                    <n-statistic label="总小节数" :value="sectionStats.totalSections" />
                  </n-grid-item>
                  <n-grid-item>
                    <n-statistic label="已发布小节" :value="sectionStats.publishedSections" />
                  </n-grid-item>
                  <n-grid-item>
                    <n-statistic label="未发布小节" :value="sectionStats.unpublishedSections" />
                  </n-grid-item>
                  <n-grid-item>
                    <n-statistic label="有视频小节" :value="sectionStats.sectionsWithVideo" />
                  </n-grid-item>
                  <n-grid-item>
                    <n-statistic label="无视频小节" :value="sectionStats.sectionsWithoutVideo" />
                  </n-grid-item>
                </n-grid>
              </n-card>
            </n-grid-item>
          </n-grid>
        </n-tab-pane>

        <!-- 作业统计 -->
        <n-tab-pane name="homework" tab="作业统计">
          <n-grid :cols="2" :x-gap="16" :y-gap="16">
            <n-grid-item>
              <n-card title="作业统计" size="small">
                <n-grid :cols="2" :x-gap="16" :y-gap="16">
                  <n-grid-item>
                    <n-statistic label="总作业数" :value="homeworkStats.totalHomework" />
                  </n-grid-item>
                  <n-grid-item>
                    <n-statistic label="已发布作业" :value="homeworkStats.publishedHomework" />
                  </n-grid-item>
                  <n-grid-item>
                    <n-statistic label="草稿作业" :value="homeworkStats.draftHomework" />
                  </n-grid-item>
                  <n-grid-item>
                    <n-statistic label="待批改数" :value="homeworkStats.pendingGrading" />
                  </n-grid-item>
                  <n-grid-item>
                    <n-statistic label="今日作业" :value="homeworkStats.todayHomework" />
                  </n-grid-item>
                  <n-grid-item>
                    <n-statistic label="即将截止" :value="homeworkStats.upcomingDeadlines" />
                  </n-grid-item>
                </n-grid>
              </n-card>
            </n-grid-item>
            <n-grid-item>
              <n-card title="提交统计" size="small">
                <n-grid :cols="2" :x-gap="16" :y-gap="16">
                  <n-grid-item>
                    <n-statistic label="总提交数" :value="submissionStats.totalSubmissions" />
                  </n-grid-item>
                  <n-grid-item>
                    <n-statistic label="已批改数" :value="submissionStats.gradedCount" />
                  </n-grid-item>
                  <n-grid-item>
                    <n-statistic label="未批改数" :value="submissionStats.ungradedCount" />
                  </n-grid-item>
                  <n-grid-item>
                    <n-statistic label="平均分数" :value="submissionStats.avgScore" suffix="分" />
                  </n-grid-item>
                  <n-grid-item>
                    <n-statistic label="批改率" :value="submissionStats.gradeRate" suffix="%" />
                  </n-grid-item>
                </n-grid>
              </n-card>
            </n-grid-item>
          </n-grid>
        </n-tab-pane>

        <!-- 订单统计 -->
        <n-tab-pane name="orders" tab="订单统计">
          <n-grid :cols="2" :x-gap="16" :y-gap="16">
            <n-grid-item>
              <n-card title="订单统计" size="small">
                <n-grid :cols="2" :x-gap="16" :y-gap="16">
                  <n-grid-item>
                    <n-statistic label="总订单数" :value="orderStats.totalOrders" />
                  </n-grid-item>
                  <n-grid-item>
                    <n-statistic label="已完成订单" :value="orderStats.completedOrders" />
                  </n-grid-item>
                  <n-grid-item>
                    <n-statistic label="待支付订单" :value="orderStats.pendingOrders" />
                  </n-grid-item>
                  <n-grid-item>
                    <n-statistic label="已取消订单" :value="orderStats.cancelledOrders" />
                  </n-grid-item>
                </n-grid>
              </n-card>
            </n-grid-item>
            <n-grid-item>
              <n-card title="收入统计" size="small">
                <n-grid :cols="2" :x-gap="16" :y-gap="16">
                  <n-grid-item>
                    <n-statistic label="总收入" :value="revenueStats.totalRevenue">
                      <template #prefix>¥</template>
                    </n-statistic>
                  </n-grid-item>
                  <n-grid-item>
                    <n-statistic label="近期订单数" :value="revenueStats.recentOrdersCount" />
                  </n-grid-item>
                  <n-grid-item>
                    <n-statistic label="已完成订单收入" :value="revenueStats.completedOrdersRevenue">
                      <template #prefix>¥</template>
                    </n-statistic>
                  </n-grid-item>
                  <n-grid-item>
                    <n-statistic label="平均订单金额" :value="revenueStats.averageOrderValue">
                      <template #prefix>¥</template>
                    </n-statistic>
                  </n-grid-item>
                </n-grid>
              </n-card>
            </n-grid-item>
          </n-grid>
        </n-tab-pane>

        <!-- 评论统计 -->
        <n-tab-pane name="comments" tab="评论统计">
          <n-card title="评论统计信息" size="small">
            <n-grid :cols="5" :x-gap="16" :y-gap="16">
              <n-grid-item>
                <n-statistic label="总评论数" :value="commentStats.totalComments" />
              </n-grid-item>
              <n-grid-item>
                <n-statistic label="可见评论" :value="commentStats.visibleComments" />
              </n-grid-item>
              <n-grid-item>
                <n-statistic label="隐藏评论" :value="commentStats.hiddenComments" />
              </n-grid-item>
              <n-grid-item>
                <n-statistic label="今日评论" :value="commentStats.todayComments" />
              </n-grid-item>
              <n-grid-item>
                <n-statistic label="平均评分" :value="commentStats.averageRating" suffix="分" />
              </n-grid-item>
            </n-grid>
          </n-card>
        </n-tab-pane>
      </n-tabs>

      <!-- 刷新按钮 -->
      <div class="refresh-area mt-6">
        <n-button type="primary" @click="refreshAllStats" :loading="loading">
          <template #icon>
            <div class="i-carbon:refresh" />
          </template>
          刷新所有统计数据
        </n-button>
        <n-text depth="3" class="ml-4">
          最后更新时间: {{ lastUpdateTime }}
        </n-text>
      </div>
    </n-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { statisticsApi } from './api'

const message = useMessage()
const loading = ref(false)
const lastUpdateTime = ref('')

// 各种统计数据
const userStats = reactive({
  totalUsers: 0,
  studentCount: 0,
  teacherCount: 0,
  adminCount: 0
})

const courseStats = reactive({
  totalCourses: 0,
  publishedCourses: 0,
  unpublishedCourses: 0
})

const browseStats = reactive({
  totalViews: 0,
  uniqueUsers: 0,
  uniqueCourses: 0
})

const learningStats = reactive({
  totalUsers: 0,
  totalEnrollments: 0,
  totalCourses: 0,
  completionRate: 0
})

const chapterStats = reactive({
  totalChapters: 0,
  publishedSections: 0,
  unpublishedSections: 0
})

const sectionStats = reactive({
  totalSections: 0,
  publishedSections: 0,
  unpublishedSections: 0,
  sectionsWithVideo: 0,
  sectionsWithoutVideo: 0
})

const homeworkStats = reactive({
  totalHomework: 0,
  publishedHomework: 0,
  draftHomework: 0,
  pendingGrading: 0,
  todayHomework: 0,
  upcomingDeadlines: 0,
  totalSubmissions: 0,
  gradedSubmissions: 0
})

const submissionStats = reactive({
  totalSubmissions: 0,
  gradedCount: 0,
  ungradedCount: 0,
  avgScore: 0,
  gradeRate: 0
})

const orderStats = reactive({
  totalOrders: 0,
  completedOrders: 0,
  pendingOrders: 0,
  cancelledOrders: 0
})

const revenueStats = reactive({
  totalRevenue: 0,
  recentOrdersCount: 0,
  completedOrdersRevenue: 0,
  averageOrderValue: 0
})

const commentStats = reactive({
  totalComments: 0,
  averageRating: 0,
  visibleComments: 0,
  hiddenComments: 0,
  todayComments: 0
})

// 获取所有统计数据
const fetchAllStats = async () => {
  loading.value = true
  try {
    const promises = [
      statisticsApi.getUserStats(),
      statisticsApi.getCourseStats(),
      statisticsApi.getBrowseStats(),
      statisticsApi.getLearningStats(),
      statisticsApi.getChapterStats(),
      statisticsApi.getSectionStats(),
      statisticsApi.getHomeworkStats(),
      statisticsApi.getSubmissionStats(),
      statisticsApi.getOrderStats(),
      statisticsApi.getRevenueStats(),
      statisticsApi.getCommentStats()
    ]

    const results = await Promise.allSettled(promises)
    
    // 处理用户统计
    if (results[0].status === 'fulfilled') {
      const userData = results[0].value.data
      if (Array.isArray(userData)) {
        let totalUsers = 0
        userData.forEach(item => {
          totalUsers += item.count
          if (item.userTypeCode === 'ADMIN') {
            userStats.adminCount = item.count
          } else if (item.userTypeCode === 'TEACHER') {
            userStats.teacherCount = item.count
          } else if (item.userTypeCode === 'USER') {
            userStats.studentCount = item.count
          }
        })
        userStats.totalUsers = totalUsers
      }
    }
    
    // 处理课程统计
    if (results[1].status === 'fulfilled') {
      const courseData = results[1].value.data
      courseStats.totalCourses = courseData.totalCourses || 0
      courseStats.publishedCourses = courseData.publishedCourses || 0
      courseStats.unpublishedCourses = courseData.unpublishedCourses || 0
      courseStats.averageRating = courseData.averageRating || 0
    }
    
    // 处理浏览统计
    if (results[2].status === 'fulfilled') {
      Object.assign(browseStats, results[2].value.data)
    }
    
    // 处理学习统计
    if (results[3].status === 'fulfilled') {
      const learningData = results[3].value.data
      learningStats.totalUsers = learningData.totalUsers || 0
      learningStats.totalEnrollments = learningData.totalEnrollments || 0
      learningStats.totalCourses = learningData.totalCourses || 0
      learningStats.completionRate = learningData.completionRate || 0
    }
    
    // 处理章节统计
    if (results[4].status === 'fulfilled') {
      const chapterData = results[4].value.data
      chapterStats.totalChapters = chapterData.totalChapters || 0
      chapterStats.publishedSections = chapterData.publishedSections || 0
      chapterStats.unpublishedSections = chapterData.unpublishedSections || 0
    }
    
    // 处理小节统计
    if (results[5].status === 'fulfilled') {
      const sectionData = results[5].value.data
      sectionStats.totalSections = sectionData.totalSections || 0
      sectionStats.publishedSections = sectionData.publishedSections || 0
      sectionStats.unpublishedSections = sectionData.unpublishedSections || 0
      sectionStats.sectionsWithVideo = sectionData.sectionsWithVideo || 0
      sectionStats.sectionsWithoutVideo = sectionData.sectionsWithoutVideo || 0
    }
    
    // 处理作业统计
    if (results[6].status === 'fulfilled') {
      const homeworkData = results[6].value.data
      homeworkStats.totalHomework = homeworkData.totalHomework || 0
      homeworkStats.publishedHomework = homeworkData.publishedHomework || 0
      homeworkStats.draftHomework = homeworkData.draftHomework || 0
      homeworkStats.pendingGrading = homeworkData.pendingGrading || 0
      homeworkStats.todayHomework = homeworkData.todayHomework || 0
      homeworkStats.upcomingDeadlines = homeworkData.upcomingDeadlines || 0
      homeworkStats.totalSubmissions = homeworkData.totalSubmissions || 0
      homeworkStats.gradedSubmissions = homeworkData.gradedSubmissions || 0
    }
    
    // 处理提交统计
    if (results[7].status === 'fulfilled') {
      const submissionData = results[7].value.data
      submissionStats.totalSubmissions = submissionData.totalSubmissions || 0
      submissionStats.gradedCount = submissionData.gradedCount || 0
      submissionStats.ungradedCount = submissionData.ungradedCount || 0
      submissionStats.avgScore = submissionData.avgScore || 0
      submissionStats.gradeRate = submissionData.gradeRate || 0
    }
    
    // 处理订单统计
    if (results[8].status === 'fulfilled') {
      Object.assign(orderStats, results[8].value.data)
    }
    
    // 处理收入统计
    if (results[9].status === 'fulfilled') {
      const revenueData = results[9].value.data
      revenueStats.totalRevenue = revenueData.totalRevenue || 0
      revenueStats.recentOrdersCount = revenueData.recentOrders ? revenueData.recentOrders.length : 0
      
      // 计算已完成订单的收入
      let completedRevenue = 0
      if (revenueData.recentOrders) {
        completedRevenue = revenueData.recentOrders
          .filter(order => order.status === 'DONE')
          .reduce((sum, order) => sum + (order.goodsPrice || 0), 0)
      }
      revenueStats.completedOrdersRevenue = completedRevenue
      
      // 计算平均订单金额
      if (revenueStats.recentOrdersCount > 0) {
        const totalOrderValue = revenueData.recentOrders
          .reduce((sum, order) => sum + (order.goodsPrice || 0), 0)
        revenueStats.averageOrderValue = (totalOrderValue / revenueStats.recentOrdersCount).toFixed(2)
      } else {
        revenueStats.averageOrderValue = 0
      }
    }
    
    // 处理评论统计
    if (results[10].status === 'fulfilled') {
      const commentData = results[10].value.data
      commentStats.totalComments = commentData.totalComments || 0
      commentStats.averageRating = commentData.averageRating || 0
      commentStats.visibleComments = commentData.visibleComments || 0
      commentStats.hiddenComments = commentData.hiddenComments || 0
      commentStats.todayComments = commentData.todayComments || 0
    }
    
    lastUpdateTime.value = new Date().toLocaleString()
    message.success('统计数据更新成功')
  } catch (error) {
    message.error('获取统计数据失败')
    console.error('获取统计数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 刷新所有统计数据
const refreshAllStats = () => {
  fetchAllStats()
}

onMounted(() => {
  fetchAllStats()
})
</script>

<style scoped>
.statistics-dashboard {
  padding: 16px;
}

.overview-cards {
  margin-bottom: 24px;
}

.refresh-area {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  border-top: 1px solid var(--border-color);
}
</style>