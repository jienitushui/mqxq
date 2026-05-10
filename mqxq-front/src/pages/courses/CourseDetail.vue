<template>
  <div class="course-detail">
    <!-- 导航栏 -->
    <Navbar />
    <!-- 返回按钮 -->
    <div class="back-button">
      <button @click="goBack" class="btn-back">
        ← 返回
      </button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>正在加载课程详情...</p>
    </div>

    <!-- 课程详情内容 -->
    <div v-else-if="course.id" class="course-content">
      <!-- 课程头部信息 -->
      <div class="course-header">
        <div class="course-cover">
          <img :src="course.cover" :alt="course.title" />
          
          <!-- 价格标签 -->
          <div v-if="course.price === 0" class="price-tag free">
            免费课程
          </div>
          <div v-else class="price-tag paid">
            ¥{{ course.price }}
          </div>
        </div>
        
        <div class="course-info">
          <h1 class="course-title">课程名称：{{ course.title }}</h1>
          
          <div class="course-meta">
            <div class="meta-row">
              <div class="meta-item">
                <i class="icon">📚</i>
                <span>{{ course.lessonNum }}课时</span>
              </div>
              <div class="meta-item">
                <i class="icon">⏱️</i>
                <span>{{ formatDuration(course.durationSum) }}</span>
              </div>

              <div class="meta-item">
                <i class="icon">👁️</i>
                <span>{{ course.viewCount }}次观看</span>
              </div>
            </div>
          </div>

          <!-- 课程状态 -->
          <div class="course-status-info">
            <span :class="['status-badge', getStatusClass(course.status)]">
              {{ getStatusText(course.status) }}
            </span>
            <span class="publish-time">
              发布于 {{ formatDate(course.publishTime) }}
            </span>
          </div>

          <!-- 操作按钮 -->
          <div class="course-actions">
            <button 
              v-if="!isAuthenticated"
              class="btn-primary"
              @click="goToLogin"
            >
              登录后学习
            </button>
            
            <template v-else>
              <!-- 已购买：显示开始学习 -->
              <button 
                v-if="hasPurchased"
                class="btn-primary"
                @click="startLearning"
              >
                {{ hasJoined ? '继续学习' : '开始学习' }}
              </button>
              
              <!-- 未购买：显示立即购买（包括免费课程） -->
              <button 
                v-else
                class="btn-primary"
                @click="buyCourse"
              >
                立即购买
              </button>
            </template>
            
            <button class="btn-secondary" @click="viewChapters">
              查看目录
            </button>
            
            <button 
              v-if="isAuthenticated && hasPurchased"
              class="btn-secondary" 
              @click="openCommentDialog"
            >
              {{ hasCommented ? '修改评论' : '发表评论' }}
            </button>
          </div>
        </div>
      </div>

      <!-- 课程详细信息 -->
      <div class="course-details">
        <!-- 课程介绍 -->
        <div class="detail-section">
          <h2 class="section-title">课程介绍</h2>
          <div class="description-content">
            {{ course.description || '暂无课程介绍' }}
          </div>
        </div>

        <!-- 课程统计 -->
        <div class="detail-section">
          <h2 class="section-title">课程统计</h2>
          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-number">{{ course.lessonNum }}</div>
              <div class="stat-label">课时数</div>
            </div>
            <div class="stat-card">
              <div class="stat-number">{{ formatDuration(course.durationSum) }}</div>
              <div class="stat-label">总时长</div>
            </div>

            <div class="stat-card">
              <div class="stat-number">{{ course.viewCount }}</div>
              <div class="stat-label">游览次数</div>
            </div>
          </div>
        </div>

        <!-- 课程章节预览 -->
        <div class="detail-section">
          <h2 class="section-title">课程目录</h2>
          <div v-if="chapters.length > 0" class="chapters-preview">
            <div 
              v-for="(chapter, index) in chapters.slice(0, 3)" 
              :key="chapter.id || index"
              class="chapter-item"
            >
              <h4 class="chapter-title">
                {{ chapter.title || chapter.name || `第${index + 1}章` }}
              </h4>
              <div class="chapter-info">
                <span>{{ chapter.sectionCount || chapter.sections?.length || 0 }}个小节</span>
              </div>
            </div>
            <div v-if="chapters.length > 3" class="more-chapters">
              <button @click="viewChapters" class="btn-link">
                查看全部 {{ chapters.length }} 个章节 →
              </button>
            </div>
          </div>
          <div v-else class="empty-chapters">
            <div class="debug-info" style="color: #999; font-size: 12px; margin-top: 10px;">
              调试信息: chapters数组长度 = {{ chapters.length }}
              <br>
              chapters内容: {{ JSON.stringify(chapters.slice(0, 2), null, 2) }}
            </div>
            暂无课程目录
          </div>
        </div>

        <!-- 课程评论 -->
        <div class="detail-section">
          <h2 class="section-title">学员评价</h2>
          
          <!-- 评论统计 -->
          <div v-if="commentStats" class="comment-statistics">
            <div class="stats-overview">
              <div class="average-rating">
                <div class="rating-number">{{ commentStats.averageRating.toFixed(1) }}</div>
                <div class="rating-stars">
                  <span 
                    v-for="star in 5" 
                    :key="star"
                    class="star"
                    :class="{ 
                      'full': star <= Math.floor(commentStats.averageRating),
                      'half': star === Math.ceil(commentStats.averageRating) && commentStats.averageRating % 1 !== 0
                    }"
                  >
                    ★
                  </span>
                </div>
                <div class="total-comments">共 {{ commentStats.totalComments }} 条评价</div>
              </div>
              
              <div class="rating-distribution">
                <div 
                  v-for="(count, index) in commentStats.ratingDistribution" 
                  :key="index"
                  class="rating-bar"
                >
                  <span class="rating-label">{{ index + 1 }}星</span>
                  <div class="bar-container">
                    <div 
                      class="bar-fill" 
                      :style="{ width: getPercentage(count, commentStats.totalComments) + '%' }"
                    ></div>
                  </div>
                  <span class="rating-count">{{ count }}</span>
                </div>
              </div>
            </div>
          </div>
          
          <div class="comments-container">
            <CourseComments ref="courseCommentsRef" :course-id="course.id" :refresh-trigger="refreshTrigger" />
          </div>
        </div>
      </div>
    </div>

    <!-- 错误状态 -->
    <div v-else class="error-state">
      <div class="error-icon">😕</div>
      <h3>课程不存在</h3>
      <p>抱歉，您访问的课程不存在或已被删除</p>
      <button @click="goBack" class="btn-primary">返回课程列表</button>
    </div>

    <!-- 评论对话框 -->
    <div v-if="showCommentDialog" class="comment-dialog-overlay" @click="showCommentDialog = false">
      <div class="comment-dialog" @click.stop>
        <div class="dialog-header">
          <h3>{{ isEditingComment ? '修改评论' : '发表评论' }}</h3>
          <button class="close-btn" @click="showCommentDialog = false">×</button>
        </div>
        
        <div class="dialog-content">
          <div class="form-group">
            <label>评分</label>
            <div class="score-stars">
              <span 
                v-for="star in 5" 
                :key="star"
                class="star"
                :class="{ active: star <= commentForm.score }"
                @click="commentForm.score = star"
              >
                ★
              </span>
            </div>
          </div>
          
          <div class="form-group">
            <label>评论内容</label>
            <textarea
              v-model="commentForm.content"
              placeholder="请输入您的评论..."
              rows="4"
              maxlength="500"
            ></textarea>
            <div class="char-count">{{ commentForm.content.length }}/500</div>
          </div>
        </div>
        
        <div class="dialog-footer">
          <button class="btn-secondary" @click="showCommentDialog = false">取消</button>
          <button class="btn-primary" @click="submitComment">
            {{ isEditingComment ? '修改' : '发表' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store'
import { publicApi, userApi } from '@/api'
import { getCourseDetail, recordCourseView } from '@/utils/courseApi'
import CourseComments from '@/components/CourseComments.vue'
import Navbar from '@/components/Navbar.vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

// 评论相关接口
const createComment = async (courseId, content, score, avatar, nickname) => {
  try {
    const response = await userApi.createComment({
      courseId,
      content,
      score
    })
    return response
  } catch (error) {
    throw error
  }
}

const updateComment = async (commentId, content, score) => {
  try {
    const response = await userApi.updateComment(commentId, {
      content,
      score
    })
    return response
  } catch (error) {
    throw error
  }
}

const deleteComment = async (commentId) => {
  try {
    const response = await userApi.deleteComment(commentId)
    return response
  } catch (error) {
    throw error
  }
}
const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const course = ref({})
const chapters = ref([])
const hasPurchased = ref(false)
const hasJoined = ref(false)
const commentStats = ref(null)
const hasCommented = ref(false)
const userComment = ref(null)

// 评论相关数据
const showCommentDialog = ref(false)
const commentForm = ref({
  content: '',
  score: 5
})
const isEditingComment = ref(false)
const currentCommentId = ref(null)
const courseCommentsRef = ref(null)
const refreshTrigger = ref(0)

// 刷新评论列表
const refreshComments = () => {
  // 方法1：尝试调用子组件方法
  if (courseCommentsRef.value && courseCommentsRef.value.refreshComments) {
    courseCommentsRef.value.refreshComments()
  }
  // 方法2：通过改变 prop 触发刷新
  refreshTrigger.value++
  
  // 刷新评论统计
  if (course.value.id) {
    fetchCommentStatistics(course.value.id)
  }
}

// 计算百分比
const getPercentage = (count, total) => {
  if (total === 0) return 0
  return Math.round((count / total) * 100)
}

// 计算属性
const isAuthenticated = computed(() => authStore.isAuthenticated)

// 检查用户是否已评论
const checkUserComment = async (courseId) => {
  if (!isAuthenticated.value) {
    hasCommented.value = false
    userComment.value = null
    return
  }
  
  try {
    const response = await userApi.checkUserComment(courseId)
    console.log('用户评论检查响应:', response)
    
    if (response && typeof response === 'object') {
      hasCommented.value = response.hasCommented || false
      userComment.value = response.comment || null
    } else {
      hasCommented.value = false
      userComment.value = null
    }
    
    console.log('用户是否已评论:', hasCommented.value)
  } catch (error) {
    console.error('检查用户评论失败:', error)
    hasCommented.value = false
    userComment.value = null
  }
}

// 获取课程详情
const fetchCourseDetail = async (courseId) => {
  try {
    loading.value = true
    // 使用智能接口：登录用户用 userApi，未登录用户用 publicApi
    const response = await getCourseDetail(courseId)
    course.value = response
    
    // 记录浏览历史（仅登录用户）
    await recordCourseView(courseId)
    
    // 获取评论统计
    await fetchCommentStatistics(courseId)
    
    // 如果用户已登录，检查购买、加入和评论状态
    if (isAuthenticated.value) {
      await Promise.all([
        checkPurchaseStatus(courseId),
        checkJoinStatus(courseId),
        checkUserComment(courseId)
      ])
    }
  } catch (error) {
    console.error('获取课程详情失败:', error)
    course.value = {}
  } finally {
    loading.value = false
  }
}

// 获取评论统计
const fetchCommentStatistics = async (courseId) => {
  try {
    const response = await publicApi.getCourseCommentStatistics(courseId)
    console.log('评论统计响应:', response)
    commentStats.value = response
  } catch (error) {
    console.error('获取评论统计失败:', error)
    commentStats.value = null
  }
}

// 获取课程章节
const fetchChapters = async (courseId) => {
  try {
    console.log('正在获取课程章节，courseId:', courseId)
    const response = await publicApi.getCourseStructure(courseId)
    console.log('课程章节原始响应:', response)
    
    let chaptersData = []
    
    // 处理不同的响应格式
    if (response) {
      if (response.data && Array.isArray(response.data)) {
        chaptersData = response.data
      } else if (Array.isArray(response)) {
        chaptersData = response
      } else if (response.chapters && Array.isArray(response.chapters)) {
        chaptersData = response.chapters
      } else if (response.list && Array.isArray(response.list)) {
        chaptersData = response.list
      } else {
        // 如果是单个对象，包装成数组
        chaptersData = [response]
      }
    }
    
    // 确保每个章节都有必要的字段
    chapters.value = chaptersData.map((chapter, index) => ({
      id: chapter.id || index,
      title: chapter.title || chapter.name || chapter.chapterName || `第${index + 1}章`,
      sectionCount: chapter.sectionCount || chapter.sections?.length || 0,
      sections: chapter.sections || []
    }))
    
    console.log('处理后的chapters:', chapters.value)
  } catch (error) {
    console.error('获取课程章节失败:', error)
    chapters.value = []
  }
}

// 检查购买状态
const checkPurchaseStatus = async (courseId) => {
  try {
    const response = await userApi.checkPurchaseStatus(courseId)
    console.log('购买状态响应:', response)
    
    // 严格根据 isPurchased 字段判断
    if (response && typeof response === 'object') {
      hasPurchased.value = response.isPurchased === true
    } else {
      hasPurchased.value = Boolean(response)
    }
    
    console.log('设置的购买状态:', hasPurchased.value)
  } catch (error) {
    console.error('检查购买状态失败:', error)
    hasPurchased.value = false
  }
}

// 检查加入状态
const checkJoinStatus = async (courseId) => {
  try {
    const response = await userApi.checkCourseJoinStatus(courseId)
    console.log('加入状态响应:', response)
    
    // 根据API返回的数据结构解析加入状态
    if (response && typeof response === 'object') {
      hasJoined.value = response.isJoined ||
                       response.joined || 
                       response.hasJoined || 
                       false
    } else {
      hasJoined.value = Boolean(response)
    }
    
    console.log('设置的加入状态:', hasJoined.value)
  } catch (error) {
    console.error('检查加入状态失败:', error)
    hasJoined.value = false
  }
}

// 工具函数
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

const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

const formatDuration = (seconds) => {
  if (!seconds || seconds === 0) return '0分钟'
  
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  
  if (hours > 0) {
    return `${hours}小时${minutes > 0 ? minutes + '分钟' : ''}`
  }
  return `${minutes}分钟`
}

// 页面操作
const goBack = () => {
  // 如果是从"我的课程"页面来的，返回到"我的课程"页面
  if (route.query.from === 'my-courses') {
    router.push({ name: 'MyCourses' })
  } else {
    // 否则使用浏览器历史记录返回
    router.go(-1)
  }
}

const goToLogin = () => {
  router.push({ name: 'Login', query: { redirect: route.fullPath } })
}

const startLearning = async () => {
  try {
    // 跳转到学习页面
    router.push({ name: 'CourseLearn', params: { id: course.value.id } })
  } catch (error) {
    console.error('开始学习失败:', error)
  }
}

const buyCourse = () => {
  router.push(`/orders/checkout/${course.value.id}`)
}

const viewChapters = () => {
  router.push({ name: 'CourseChapters', params: { id: course.value.id } })
}

// 评论相关方法
const openCommentDialog = () => {
  showCommentDialog.value = true
  
  // 如果用户已经评论过，填充现有评论内容
  if (hasCommented.value && userComment.value) {
    isEditingComment.value = true
    currentCommentId.value = userComment.value.id
    commentForm.value = {
      content: userComment.value.content || '',
      score: userComment.value.score || 5
    }
  } else {
    isEditingComment.value = false
    currentCommentId.value = null
    commentForm.value = {
      content: '',
      score: 5
    }
  }
}

const openEditCommentDialog = (commentId, content, score) => {
  showCommentDialog.value = true
  isEditingComment.value = true
  currentCommentId.value = commentId
  commentForm.value = {
    content,
    score
  }
}

const handleCreateComment = async () => {
  if (!course.value.id) return
  
  try {
    if (!commentForm.value.content.trim()) {
      ElMessage.warning('请输入评论内容')
      return
    }
    
    await createComment(
      course.value.id,
      commentForm.value.content,
      commentForm.value.score
    )
    
    console.log('评论发表成功')
    ElMessage.success('评论发表成功')
    showCommentDialog.value = false
    
    // 更新评论状态
    hasCommented.value = true
    await checkUserComment(course.value.id)
    
    // 刷新评论列表
    refreshComments()
    
  } catch (error) {
    console.error('发表评论失败:', error)
    ElMessage.error('发表评论失败')
  }
}

const handleUpdateComment = async () => {
  if (!currentCommentId.value) return
  
  try {
    if (!commentForm.value.content.trim()) {
      ElMessage.warning('请输入评论内容')
      return
    }
    
    await updateComment(
      currentCommentId.value,
      commentForm.value.content,
      commentForm.value.score
    )
    
    console.log('评论修改成功')
    ElMessage.success('评论修改成功')
    showCommentDialog.value = false
    
    // 更新评论状态
    await checkUserComment(course.value.id)
    
    // 刷新评论列表
    refreshComments()
  } catch (error) {
    console.error('修改评论失败:', error)
    ElMessage.error('修改评论失败')
  }
}

const handleDeleteComment = async (commentId) => {
  try {
    const confirmed = confirm('确定要删除这条评论吗？')
    if (!confirmed) return
    
    await deleteComment(commentId)
    console.log('评论删除成功')
    ElMessage.success('评论删除成功')
    // 刷新评论列表
    refreshComments()
  } catch (error) {
    console.error('删除评论失败:', error)
  }
}

const submitComment = () => {
  if (isEditingComment.value) {
    handleUpdateComment()
  } else {
    handleCreateComment()
  }
}

// 页面初始化
onMounted(async () => {
  const courseId = route.params.id
  if (courseId) {
    await fetchCourseDetail(courseId)
    await fetchChapters(courseId)
    
    // 如果没有获取到章节，尝试其他API
    if (chapters.value.length === 0) {
      console.log('尝试使用其他API获取章节')
      try {
        // 尝试使用课程章节API
        const response = await publicApi.getCourseChapters?.(courseId) || 
                         await publicApi.getChapters?.(courseId) ||
                         await publicApi.getCourseContent?.(courseId)
        
        if (response) {
          console.log('备用API响应:', response)
          chapters.value = Array.isArray(response) ? response : (response.data || [])
        }
      } catch (error) {
        console.log('备用API也失败:', error)
      }
    }
  }
})
</script>

<style scoped>
.course-detail {
  min-height: 100vh;
  background: #f8f9fa;
  padding: 20px 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 返回按钮 */
.back-button {
  max-width: 1200px;
  margin: 20px auto 20px auto;
  padding: 0 20px;
  text-align: left;
}

.btn-back {
  padding: 8px 16px;
  background: white;
  border: 1px solid #ddd;
  border-radius: 6px;
  color: #666;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-back:hover {
  background: #f8f9fa;
  border-color: #3b82f6;
  color: #3b82f6;
}

/* 加载状态 */
.loading-state {
  text-align: center;
  padding: 80px 0;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 课程内容 */
.course-content {
  max-width: 1200px;
  margin: 0 auto;
}

/* 课程头部 */
.course-header {
  display: flex;
  gap: 40px;
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.course-cover {
  position: relative;
  flex-shrink: 0;
}

.course-cover img {
  width: 400px;
  height: 250px;
  object-fit: cover;
  border-radius: 12px;
}

.price-tag {
  position: absolute;
  top: 15px;
  right: 15px;
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: bold;
  font-size: 16px;
}

.price-tag.free {
  background: linear-gradient(45deg, #4ecdc4, #44a08d);
  color: white;
}

.price-tag.paid {
  background: linear-gradient(45deg, #2196F3, #ffffff);
  color: #333;
}

.course-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.course-title {
  font-size: 2rem;
  font-weight: bold;
  color: #333;
  margin-bottom: 20px;
  line-height: 1.3;
}

.course-meta {
  margin-bottom: 20px;
}

.meta-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 15px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 6px;
  font-size: 14px;
}

.meta-item .icon {
  font-size: 16px;
}

.course-status-info {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 30px;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 15px;
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
  font-size: 14px;
  color: #666;
}

.course-actions {
  display: flex;
  gap: 15px;
  margin-top: auto;
}

.btn-primary,
.btn-secondary {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-primary {
  background: linear-gradient(45deg, #2196F3, #ffffff);
  color: #333;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(33, 150, 243, 0.4);
}

.btn-secondary {
  background: white;
  color: #2196F3;
  border: 2px solid #2196F3;
}

.btn-secondary:hover {
  background: #2196F3;
  color: white;
}

.btn-link {
  background: none;
  border: none;
  color: #2196F3;
  cursor: pointer;
  font-size: 14px;
}

.btn-link:hover {
  text-decoration: underline;
}

/* 课程详细信息 */
.course-details {
  display: grid;
  gap: 30px;
}

.detail-section {
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 1.5rem;
  font-weight: bold;
  color: #333;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #2196F3;
}

.description-content {
  line-height: 1.8;
  color: #666;
  font-size: 16px;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 20px;
}

.stat-card {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.stat-number {
  font-size: 1.8rem;
  font-weight: bold;
  color: #2196F3;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

/* 章节预览 */
.chapters-preview {
  space-y: 15px;
}

.chapter-item {
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #2196F3;
  margin-bottom: 15px;
}

.chapter-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.chapter-info {
  font-size: 14px;
  color: #666;
}

.more-chapters {
  text-align: center;
  padding: 20px;
}

.empty-chapters {
  text-align: center;
  padding: 40px;
  color: #999;
}

.comments-container {
  width: 100%;
}

/* 评论统计样式 */
.comment-statistics {
  margin-bottom: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.stats-overview {
  display: flex;
  gap: 40px;
  align-items: flex-start;
}

.average-rating {
  text-align: center;
  min-width: 150px;
}

.rating-number {
  font-size: 3rem;
  font-weight: bold;
  color: #2196F3;
  margin-bottom: 10px;
}

.rating-stars {
  display: flex;
  justify-content: center;
  gap: 2px;
  margin-bottom: 10px;
}

.rating-stars .star {
  font-size: 20px;
  color: #ddd;
}

.rating-stars .star.full {
  color: #ffd700;
}

.rating-stars .star.half {
  background: linear-gradient(90deg, #ffd700 50%, #ddd 50%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.total-comments {
  font-size: 14px;
  color: #666;
}

.rating-distribution {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.rating-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
}

.rating-label {
  min-width: 40px;
  color: #666;
}

.bar-container {
  flex: 1;
  height: 8px;
  background: #e9ecef;
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #2196F3, #64b5f6);
  transition: width 0.3s ease;
}

.rating-count {
  min-width: 30px;
  text-align: right;
  color: #666;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stats-overview {
    flex-direction: column;
    gap: 20px;
  }
  
  .average-rating {
    min-width: auto;
  }
  
  .rating-number {
    font-size: 2.5rem;
  }
}

/* 错误状态 */
.error-state {
  text-align: center;
  padding: 80px 0;
}

.error-icon {
  font-size: 4rem;
  margin-bottom: 20px;
}

.error-state h3 {
  font-size: 1.5rem;
  color: #333;
  margin-bottom: 10px;
}

.error-state p {
  color: #666;
  margin-bottom: 30px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .course-header {
    flex-direction: column;
    gap: 20px;
    padding: 20px;
  }
  
  .course-cover img {
    width: 100%;
    height: 200px;
  }
  
  .course-title {
    font-size: 1.5rem;
  }
  
  .meta-row {
    grid-template-columns: 1fr;
  }
  
  .course-actions {
    flex-direction: column;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .detail-section {
    padding: 20px;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}

/* 评论对话框样式 */
.comment-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.comment-dialog {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.dialog-header h3 {
  margin: 0;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  color: #666;
}

.dialog-content {
  padding: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}

.score-stars {
  display: flex;
  gap: 5px;
}

.star {
  font-size: 24px;
  color: #ddd;
  cursor: pointer;
  transition: color 0.2s;
}

.star.active {
  color: #ffd700;
}

.star:hover {
  color: #ffd700;
}

.form-group textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  resize: vertical;
  min-height: 100px;
}

.form-group textarea:focus {
  outline: none;
  border-color: #2196F3;
}

.char-count {
  text-align: right;
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 20px;
  border-top: 1px solid #eee;
}

.dialog-footer .btn-primary,
.dialog-footer .btn-secondary {
  padding: 8px 16px;
  font-size: 14px;
}
</style>