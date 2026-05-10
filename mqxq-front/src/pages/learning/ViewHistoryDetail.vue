<template>
  <div class="view-history-detail">
    <Navbar />
    
    <div class="container">
      <!-- 返回按钮 -->
      <div class="back-button">
        <el-button @click="goBack" :icon="ArrowLeft">
          返回浏览记录
        </el-button>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="5" animated />
      </div>

      <!-- 浏览记录详情 -->
      <div v-else-if="viewRecord.id" class="detail-content">
        <div class="detail-card">
          <h1 class="detail-title">浏览记录详情</h1>
          
          <div class="detail-info">
            <div class="info-row">
              <label class="info-label">记录ID：</label>
              <span class="info-value">{{ viewRecord.id }}</span>
            </div>
            
            <div class="info-row">
              <label class="info-label">课程ID：</label>
              <span class="info-value">{{ viewRecord.courseId }}</span>
            </div>
            
            <div class="info-row">
              <label class="info-label">课程名称：</label>
              <span class="info-value">{{ viewRecord.courseTitle || viewRecord.course?.title || '未知课程' }}</span>
            </div>
            
            <div class="info-row">
              <label class="info-label">浏览时间：</label>
              <span class="info-value">{{ formatDateTime(viewRecord.viewTime) }}</span>
            </div>
            
            <div class="info-row" v-if="viewRecord.createTime">
              <label class="info-label">记录创建时间：</label>
              <span class="info-value">{{ formatDateTime(viewRecord.createTime) }}</span>
            </div>
            
            <div class="info-row" v-if="viewRecord.ipAddress">
              <label class="info-label">访问IP：</label>
              <span class="info-value ip-address">{{ viewRecord.ipAddress }}</span>
            </div>
            
            <div class="info-row" v-if="viewRecord.userAgent">
              <label class="info-label">浏览器：</label>
              <span class="info-value">{{ getBrowserName(viewRecord.userAgent) }}</span>
            </div>
            
            <div class="info-row" v-if="viewRecord.userAgent">
              <label class="info-label">User Agent：</label>
              <span class="info-value user-agent">{{ viewRecord.userAgent }}</span>
            </div>
          </div>

          <!-- 课程信息 -->
          <div v-if="viewRecord.course || viewRecord.courseTitle" class="course-section">
            <h3 class="section-title">关联课程信息</h3>
            <div class="course-card">
              <img 
                :src="viewRecord.courseCover || viewRecord.course?.cover || '/default-course.svg'" 
                :alt="viewRecord.courseTitle || viewRecord.course?.title"
                class="course-cover"
                @error="handleImageError"
              />
              <div class="course-info">
                <h4 class="course-title">
                  {{ viewRecord.courseTitle || viewRecord.course?.title }}
                </h4>
                <p class="course-desc">
                  {{ viewRecord.courseDescription || viewRecord.course?.description }}
                </p>
                <div class="course-meta">
                  <span class="meta-item">
                    <i class="icon">💰</i>
                    {{ (viewRecord.coursePrice ?? viewRecord.course?.price) === 0 ? '免费' : `¥${viewRecord.coursePrice ?? viewRecord.course?.price}` }}
                  </span>
                  <span class="meta-item">
                    <i class="icon">👁️</i>
                    {{ viewRecord.courseViewCount || viewRecord.course?.viewCount }}次观看
                  </span>
                  <span class="meta-item" v-if="viewRecord.course?.lessonNum">
                    {{ viewRecord.course.lessonNum }}课时
                  </span>
                </div>
              </div>
            </div>
            
            <div class="course-actions">
              <el-button 
                type="primary" 
                @click="goToCourse"
                :disabled="!viewRecord.courseId"
              >
                查看课程详情
              </el-button>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="detail-actions">
            <el-button type="danger" @click="handleDelete">
              删除此记录
            </el-button>
          </div>
        </div>
      </div>

      <!-- 错误状态 -->
      <div v-else class="error-state">
        <div class="error-icon">❌</div>
        <h3>记录不存在</h3>
        <p>该浏览记录可能已被删除或不存在</p>
        <el-button type="primary" @click="goBack">
          返回浏览记录
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import Navbar from '../../components/Navbar.vue'
import { userApi } from '../../api/user'
import { getCourseDetail } from '../../utils/courseApi'
import type { CourseView } from '../../api/types/course'

// 路由
const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const viewRecord = ref<CourseView>({} as CourseView)

// 获取浏览记录详情
const fetchViewDetail = async (id: number) => {
  try {
    loading.value = true
    const response = await userApi.getCourseViewDetail(id)
    
    // 获取浏览记录基本信息
    viewRecord.value = response
    
    // 获取关联的课程信息
    if (response.courseId) {
      await enrichViewRecordWithCourse(response)
    }
  } catch (error) {
    console.error('获取浏览记录详情失败:', error)
    ElMessage.error('获取浏览记录详情失败')
  } finally {
    loading.value = false
  }
}

// 为浏览记录补充课程信息
const enrichViewRecordWithCourse = async (record: CourseView) => {
  try {
    const course = await getCourseDetail(record.courseId)
    
    viewRecord.value = {
      ...record,
      course,
      // 扁平化课程信息
      courseTitle: course.title,
      courseDescription: course.description,
      courseCover: course.cover,
      coursePrice: course.price,
      courseViewCount: course.viewCount
    }
  } catch (error) {
    console.warn('获取课程信息失败:', error)
    // 如果获取课程信息失败，至少显示基本信息
    viewRecord.value = {
      ...record,
      courseTitle: `课程ID: ${record.courseId}`,
      courseDescription: '课程信息获取失败',
      courseCover: '',
      coursePrice: 0,
      courseViewCount: 0
    }
  }
}

// 删除记录
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条浏览记录吗？',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await userApi.deleteCourseView(viewRecord.value.id)
    ElMessage.success('删除成功')
    
    // 返回浏览记录列表
    goBack()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除浏览记录失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 跳转到课程详情
const goToCourse = () => {
  if (viewRecord.value.courseId) {
    router.push({ 
      name: 'CourseDetail', 
      params: { id: viewRecord.value.courseId } 
    })
  }
}

// 返回上一页
const goBack = () => {
  router.push({ name: 'ViewHistory' })
}

// 工具函数
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.src = '/default-course.svg'
}

// 从 User Agent 中提取浏览器名称
const getBrowserName = (userAgent: string) => {
  if (!userAgent) return '未知浏览器'
  
  if (userAgent.includes('Edg/')) return 'Microsoft Edge'
  if (userAgent.includes('Chrome/')) return 'Google Chrome'
  if (userAgent.includes('Firefox/')) return 'Mozilla Firefox'
  if (userAgent.includes('Safari/') && !userAgent.includes('Chrome/')) return 'Safari'
  if (userAgent.includes('Opera/')) return 'Opera'
  
  return '其他浏览器'
}

// 页面加载时获取数据
onMounted(() => {
  const id = Number(route.params.id)
  if (id && !isNaN(id)) {
    fetchViewDetail(id)
  } else {
    ElMessage.error('无效的记录ID')
    goBack()
  }
})
</script>

<style scoped>
.view-history-detail {
  min-height: 100vh;
  background: #f8f9fa;
}

.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.back-button {
  margin-bottom: 20px;
  text-align: left;
}

.loading-state {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.detail-content {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.detail-card {
  padding: 30px;
}

.detail-title {
  margin: 0 0 24px 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}

.detail-info {
  margin-bottom: 30px;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
}

.info-row:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.info-label {
  width: 120px;
  font-weight: 600;
  color: #374151;
  flex-shrink: 0;
}

.info-value {
  color: #1f2937;
  flex: 1;
  word-break: break-all;
}

.ip-address {
  font-family: monospace;
  background: #f3f4f6;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
}

.user-agent {
  font-size: 12px;
  color: #6b7280;
  line-height: 1.4;
}

.course-section {
  margin-bottom: 30px;
  padding-top: 24px;
  border-top: 1px solid #e5e7eb;
}

.section-title {
  margin: 0 0 20px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.course-card {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: #f9fafb;
  border-radius: 8px;
  margin-bottom: 20px;
}

.course-cover {
  width: 120px;
  height: 90px;
  object-fit: cover;
  border-radius: 6px;
  flex-shrink: 0;
}

.course-info {
  flex: 1;
  min-width: 0;
}

.course-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.4;
}

.course-desc {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #6b7280;
  line-height: 1.5;
}

.course-meta {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #6b7280;
}

.meta-item .icon {
  font-size: 14px;
}

.course-actions {
  text-align: left;
}

.detail-actions {
  padding-top: 24px;
  border-top: 1px solid #e5e7eb;
  text-align: left;
}

.error-state {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.error-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.error-state h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: #1f2937;
}

.error-state p {
  margin: 0 0 24px 0;
  color: #6b7280;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .container {
    padding: 16px;
  }
  
  .detail-card {
    padding: 20px;
  }
  
  .course-card {
    flex-direction: column;
  }
  
  .course-cover {
    width: 100%;
    height: 150px;
  }
  
  .info-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .info-label {
    width: auto;
  }
}
</style>