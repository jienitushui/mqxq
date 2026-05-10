<template>
  <div class="course-card" @click="handleClick">
    <!-- 课程封面 -->
    <div class="course-cover">
      <img :src="course.cover" :alt="course.title" />
      
      <!-- 热门标签 -->
      <div v-if="showHotBadge" class="hot-badge">
        🔥 热门
      </div>
      
      <!-- 免费标签 -->
      <div v-if="course.price === 0" class="free-badge" :class="{ 'with-hot': showHotBadge }">
        免费
      </div>
    
    </div>

    <!-- 课程信息 -->
    <div class="course-info">
      <h3 class="course-title">{{ course.title }}</h3>
      
      <p class="course-description" v-if="course.description">
        {{ truncateText(course.description, 80) }}
      </p>

      <!-- 课程统计 -->
      <div class="course-stats">
        <div class="stat-row">
          <span class="stat-label">{{ course.buyCount || 0 }}人购买</span>
          <span class="stat-label">{{ course.lessonNum }}课时</span>
        </div>
        
        <div class="stat-row">
          <span class="stat-price">¥{{ course.price }}</span>
          <span class="stat-label">{{ formatViewCount(course.viewCount) }}次游览</span>
        </div>
      </div>

      <!-- 课程状态 -->
      <div class="course-status">
        <span :class="['status-badge', getStatusClass(course.status)]">
          {{ getStatusText(course.status) }}
        </span>
        
        <span class="publish-time">
          {{ formatDate(course.publishTime) }}
        </span>
      </div>
    </div>

    <!-- 悬浮操作 -->
    <div class="course-actions">
      <button class="action-btn secondary" @click.stop="handlePreview">
        预览课程
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store'
import { userApi } from '@/api'

const router = useRouter()
const authStore = useAuthStore()

// Props
const props = defineProps({
  course: {
    type: Object,
    required: true
  },
  showHotBadge: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['click', 'learn', 'preview'])

// 计算属性
const courseId = computed(() => props.course.id)

// 方法
const handleClick = () => {
  emit('click', courseId.value)
}

const handleLearn = async () => {
  if (!authStore.isAuthenticated) {
    router.push({ name: 'Login', query: { redirect: router.currentRoute.value.fullPath } })
    return
  }

  if (props.course.price === 0) {
    try {
      const hasJoined = await userApi.checkCourseJoinStatus(courseId.value)
      if (!hasJoined) {
        await userApi.joinCourse(courseId.value)
      }
      router.push({ name: 'CourseLearn', params: { id: courseId.value } })
    } catch (error) {
      console.error('加入课程失败:', error)
    }
  } else {
    router.push({ name: 'Checkout', params: { courseId: courseId.value } })
  }
  emit('learn', courseId.value)
}

const handlePreview = () => {
  router.push({ name: 'CourseDetail', params: { id: courseId.value } })
  emit('preview', courseId.value)
}

// 工具函数
const truncateText = (text, maxLength) => {
  if (!text) return ''
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

const formatViewCount = (count) => {
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + '万'
  }
  if (count >= 1000) {
    return (count / 1000).toFixed(1) + 'k'
  }
  return count.toString()
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

const getStatusText = (status) => {
  const statusMap = {
    0: '草稿',
    1: '已发布',
    2: '已下架'
  }
  return statusMap[status] || '未知'
}

const getStatusClass = (status) => {
  const classMap = {
    0: 'draft',
    1: 'published',
    2: 'offline'
  }
  return classMap[status] || 'unknown'
}
</script>

<style scoped>
.course-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
}

.course-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.course-card:hover .course-actions {
  opacity: 1;
  visibility: visible;
}

/* 课程封面 */
.course-cover {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.course-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.course-card:hover .course-cover img {
  transform: scale(1.05);
}

/* 标签 */
.hot-badge,
.free-badge,
.price-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: bold;
  color: white;
  z-index: 2;
}

.hot-badge {
  background: linear-gradient(45deg, #ff6b6b, #ff8e53);
  z-index: 3;
}

.free-badge {
  background: linear-gradient(45deg, #4ecdc4, #44a08d);
}

.free-badge.with-hot {
  top: 50px;
}

.price-badge {
  background: linear-gradient(45deg, #667eea, #764ba2);
  font-size: 14px;
}

.price-badge.with-hot {
  top: 50px;
}

/* 课程信息 */
.course-info {
  padding: 20px;
}

.course-title {
  font-size: 1.1rem;
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-description {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 15px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 课程统计 */
.course-stats {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 15px;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.stat-price {
  font-size: 18px;
  font-weight: bold;
  color: #ff4757;
}

/* 课程状态 */
.course-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.draft {
  background: #fff3cd;
  color: #856404;
}

.status-badge.published {
  background: #d4edda;
  color: #155724;
}

.status-badge.offline {
  background: #f8d7da;
  color: #721c24;
}

.publish-time {
  font-size: 12px;
  color: #999;
}

/* 悬浮操作 */
.course-actions {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 15px;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s ease;
}

.action-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 120px;
}

.action-btn.primary {
  background: #667eea;
  color: white;
}

.action-btn.primary:hover {
  background: #5a6fd8;
}

.action-btn.secondary {
  background: transparent;
  color: white;
  border: 2px solid white;
}

.action-btn.secondary:hover {
  background: white;
  color: #333;
}

/* 响dding: 10px 20px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 120px;
}

.action-btn.primary {
  background: #667eea;
  color: white;
}

.action-btn.primary:hover {
  background: #5a6fd8;
}

.action-btn.secondary {
  background: transparent;
  color: white;
  border: 2px solid white;
}

.action-btn.secondary:hover {
  background: white;
  color: #333;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .course-cover {
    height: 180px;
  }
  
  .course-info {
    padding: 15px;
  }
  
  .course-title {
    font-size: 1rem;
  }
  
  .course-stats {
    grid-template-columns: 1fr;
    gap: 6px;
  }
}
</style>