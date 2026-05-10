<template>
  <div class="course-chapters">
    <!-- 导航栏 -->
    <Navbar />
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="container">
        <div class="header-content">
          <button @click="goBack" class="btn-back">
            ← 返回课程详情
          </button>
          
          <div class="course-info" v-if="course.id">
            <h1 class="course-title">{{ course.title }}</h1>
            <div class="course-meta">
              <span class="meta-item">
                <i class="icon">📚</i>
                {{ chapters.length }}个章节
              </span>
              <span class="meta-item">
                <i class="icon">📝</i>
                {{ totalSections }}个小节
              </span>
              <!-- <span class="meta-item">
                <i class="icon">⏱️</i>
                {{ formatDuration(course.durationSum) }}
              </span> -->
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 章节内容 -->
    <div class="chapters-content">
      <div class="container">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
          <div class="loading-spinner"></div>
          <p>正在加载课程目录...</p>
        </div>

        <!-- 空状态 -->
        <div v-else-if="chapters.length === 0" class="empty-state">
          <div class="empty-icon">📚</div>
          <h3>暂无课程目录</h3>
          <p>该课程还没有添加章节内容</p>
        </div>

        <!-- 章节列表 -->
        <div v-else class="chapters-list">
          <div 
            v-for="(chapter, chapterIndex) in chapters" 
            :key="chapter.id"
            class="chapter-item"
          >
            <!-- 章节头部 -->
            <div class="chapter-header" @click="toggleChapter(chapter.id)">
              <div class="chapter-info">
                <div class="chapter-number">第{{ chapterIndex + 1 }}章</div>
                <div class="chapter-details">
                  <h3 class="chapter-title">{{ chapter.title }}</h3>
                  <p v-if="chapter.description" class="chapter-description">
                    {{ chapter.description }}
                  </p>
                  <div class="chapter-meta">
                    <span class="section-count">
                      {{ chapter.sections?.length || 0 }}个小节
                    </span>
                    <!-- <span class="chapter-duration">
                      {{ formatChapterDuration(chapter.sections) }}
                    </span> -->
                  </div>
                </div>
              </div>
              
              <div class="chapter-toggle">
                <i :class="['toggle-icon', { expanded: expandedChapters.includes(chapter.id) }]">
                  ▼
                </i>
              </div>
            </div>

            <!-- 小节列表 -->
            <div 
              v-show="expandedChapters.includes(chapter.id)"
              class="sections-list"
            >
              <div 
                v-for="(section, sectionIndex) in chapter.sections" 
                :key="section.id"
                class="section-item"
                @click="handleSectionClick(section)"
              >
                <div class="section-number">
                  {{ chapterIndex + 1 }}.{{ sectionIndex + 1 }}
                </div>
                
                <div class="section-info">
                  <h4 class="section-title">{{ section.title }}</h4>
                  <p v-if="section.description" class="section-description">
                    {{ section.description }}
                  </p>
                  
                  <div class="section-meta">
                    <!-- <span class="section-duration">
                      <i class="icon">⏱️</i>
                      {{ formatDuration(section.duration) }}
                    </span> -->
                    
                    <span v-if="section.isFree" class="free-badge">
                      免费试看
                    </span>
                    
                    <span :class="['section-status', getSectionStatusClass(section.status)]">
                      {{ getSectionStatusText(section.status) }}
                    </span>
                  </div>
                </div>

                <div class="section-actions">
                  <button 
                    v-if="canWatchSection(section)"
                    class="btn-play"
                    @click.stop="playSection(section)"
                  >
                    ▶️ 播放
                  </button>
                  
                  <button 
                    v-else-if="section.isFree"
                    class="btn-preview"
                    @click.stop="previewSection(section)"
                  >
                    👁️ 预览
                  </button>
                  
                  <div v-else class="locked-section">
                    🔒 需要购买
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 底部操作 -->
        <div v-if="chapters.length > 0" class="bottom-actions">
          <button @click="expandAll" class="btn-secondary">
            {{ allExpanded ? '收起全部' : '展开全部' }}
          </button>
          
          <button 
            v-if="canStartLearning"
            @click="startLearning"
            class="btn-primary"
          >
            开始学习
          </button>
        </div>
      </div>
    </div>

    <!-- 权限验证弹窗 -->
    <AccessModal
      :visible="accessModal.visible"
      :type="accessModal.type"
      :course-info="course"
      @close="closeAccessModal"
      @confirm="handleAccessConfirm"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store'
import { publicApi, userApi } from '@/api'
import { getCourseDetail } from '@/utils/courseApi'
import AccessModal from '@/components/AccessModal.vue'
import Navbar from '@/components/Navbar.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const course = ref({})
const chapters = ref([])
const expandedChapters = ref([])
const hasPurchased = ref(false)
const hasJoined = ref(false)

// 权限验证弹窗
const accessModal = ref({
  visible: false,
  type: 'login'
})

// 计算属性
const isAuthenticated = computed(() => authStore.isAuthenticated)

const totalSections = computed(() => {
  return chapters.value.reduce((total, chapter) => {
    return total + (chapter.sections?.length || 0)
  }, 0)
})

const allExpanded = computed(() => {
  return chapters.value.length > 0 && expandedChapters.value.length === chapters.value.length
})

const canStartLearning = computed(() => {
  return isAuthenticated.value && (course.value.price === 0 || hasPurchased.value)
})

// 获取课程信息
const fetchCourseInfo = async (courseId) => {
  try {
    const response = await getCourseDetail(courseId)
    course.value = response
  } catch (error) {
    console.error('获取课程信息失败:', error)
  }
}

// 获取课程章节
const fetchChapters = async (courseId) => {
  try {
    loading.value = true
    const response = await publicApi.getCourseStructure(courseId)
    chapters.value = response || []
    
    // 默认展开第一个章节
    if (chapters.value.length > 0) {
      expandedChapters.value = [chapters.value[0].id]
    }
  } catch (error) {
    console.error('获取课程章节失败:', error)
  } finally {
    loading.value = false
  }
}

// 检查购买状态
const checkPurchaseStatus = async (courseId) => {
  if (!isAuthenticated.value) {
    hasPurchased.value = false
    hasJoined.value = false
    return
  }
  
  try {
    const [purchaseResponse, joinResponse] = await Promise.all([
      userApi.checkPurchaseStatus(courseId),
      userApi.checkCourseJoinStatus(courseId)
    ])
    
    console.log('CourseChapters - 购买状态响应:', purchaseResponse)
    console.log('CourseChapters - 加入状态响应:', joinResponse)
    
    // 严格根据 isPurchased 字段判断
    if (purchaseResponse && typeof purchaseResponse === 'object') {
      hasPurchased.value = purchaseResponse.isPurchased === true
    } else {
      hasPurchased.value = Boolean(purchaseResponse)
    }
    
    // 解析加入状态
    if (joinResponse && typeof joinResponse === 'object') {
      hasJoined.value = joinResponse.isJoined ||
                       joinResponse.joined || 
                       joinResponse.hasJoined || 
                       false
    } else {
      hasJoined.value = Boolean(joinResponse)
    }
    
    console.log('CourseChapters - 设置的购买状态:', hasPurchased.value)
    console.log('CourseChapters - 设置的加入状态:', hasJoined.value)
  } catch (error) {
    console.error('检查购买状态失败:', error)
    hasPurchased.value = false
    hasJoined.value = false
  }
}

// 切换章节展开状态
const toggleChapter = (chapterId) => {
  const index = expandedChapters.value.indexOf(chapterId)
  if (index > -1) {
    expandedChapters.value.splice(index, 1)
  } else {
    expandedChapters.value.push(chapterId)
  }
}

// 展开/收起全部章节
const expandAll = () => {
  if (allExpanded.value) {
    expandedChapters.value = []
  } else {
    expandedChapters.value = chapters.value.map(chapter => chapter.id)
  }
}

// 检查是否可以观看小节
const canWatchSection = (section) => {
  if (section.isFree) return true
  if (!isAuthenticated.value) return false
  return course.value.price === 0 || hasPurchased.value
}

// 验证小节访问权限
const validateSectionAccess = async (section) => {
  // 如果是免费小节，直接允许观看
  if (section.isFree) {
    return true
  }
  
  // 检查用户是否登录
  if (!isAuthenticated.value) {
    showLoginPrompt()
    return false
  }
  
  // 如果是免费课程，检查是否已加入
  if (course.value.price === 0) {
    if (!hasJoined.value) {
      const joined = await showJoinCoursePrompt()
      if (joined) {
        hasJoined.value = true
        return true
      }
      return false
    }
    return true
  }
  
  // 如果是付费课程，检查是否已购买和加入
  if (!hasPurchased.value) {
    showPurchasePrompt()
    return false
  }
  
  if (!hasJoined.value) {
    const joined = await showJoinCoursePrompt()
    if (joined) {
      hasJoined.value = true
      return true
    }
    return false
  }
  
  return true
}

// 显示登录提示
const showLoginPrompt = () => {
  accessModal.value = {
    visible: true,
    type: 'login'
  }
}

// 显示购买提示
const showPurchasePrompt = () => {
  accessModal.value = {
    visible: true,
    type: 'purchase'
  }
}

// 显示加入课程提示
const showJoinCoursePrompt = async () => {
  return new Promise((resolve) => {
    accessModal.value = {
      visible: true,
      type: 'join'
    }
    
    // 保存resolve函数，在确认时调用
    accessModal.value.resolve = resolve
  })
}

// 关闭权限验证弹窗
const closeAccessModal = () => {
  accessModal.value.visible = false
  if (accessModal.value.resolve) {
    accessModal.value.resolve(false)
  }
}

// 处理权限验证确认
const handleAccessConfirm = async () => {
  const { type } = accessModal.value
  
  try {
    if (type === 'login') {
      router.push({ name: 'Login', query: { redirect: route.fullPath } })
    } else if (type === 'purchase') {
      router.push({ name: 'Checkout', params: { courseId: course.value.id } })
    } else if (type === 'join') {
      await userApi.joinCourse(course.value.id)
      hasJoined.value = true
      
      if (accessModal.value.resolve) {
        accessModal.value.resolve(true)
      }
    }
  } catch (error) {
    console.error('操作失败:', error)
    if (accessModal.value.resolve) {
      accessModal.value.resolve(false)
    }
  } finally {
    accessModal.value.visible = false
  }
}

// 处理小节点击
const handleSectionClick = async (section) => {
  const canWatch = await validateSectionAccess(section)
  if (canWatch) {
    playSection(section)
  }
}

// 播放小节
const playSection = (section) => {
  router.push({ 
    name: 'CourseLearn', 
    params: { id: course.value.id },
    query: { sectionId: section.id }
  })
}

// 预览小节
const previewSection = (section) => {
  // 可以打开预览模态框或跳转到预览页面
  console.log('预览小节:', section)
}

// 开始学习
const startLearning = async () => {
  try {
    // 如果是免费课程且未加入，先加入课程
    if (course.value.price === 0 && !hasJoined.value) {
      await userApi.joinCourse(course.value.id)
    }
    
    router.push({ name: 'CourseLearn', params: { id: course.value.id } })
  } catch (error) {
    console.error('开始学习失败:', error)
  }
}

// 返回上一页
const goBack = () => {
  router.go(-1)
}

// 工具函数
const formatDuration = (seconds) => {
  if (!seconds || seconds === 0) return '0分钟'
  
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  
  if (hours > 0) {
    return `${hours}小时${minutes > 0 ? minutes + '分钟' : ''}`
  }
  return `${minutes}分钟`
}

const formatChapterDuration = (sections) => {
  if (!sections || sections.length === 0) return '0分钟'
  
  const totalDuration = sections.reduce((total, section) => {
    return total + (section.duration || 0)
  }, 0)
  
  return formatDuration(totalDuration)
}

const getSectionStatusText = (status) => {
  const statusMap = {
    0: '草稿',
    1: '已发布',
    2: '已下架'
  }
  return statusMap[status] || '未知'
}

const getSectionStatusClass = (status) => {
  const classMap = {
    0: 'draft',
    1: 'published',
    2: 'offline'
  }
  return classMap[status] || 'unknown'
}

// 页面初始化
onMounted(() => {
  const courseId = route.params.id
  if (courseId) {
    fetchCourseInfo(courseId)
    fetchChapters(courseId)
    checkPurchaseStatus(courseId)
  }
})
</script>

<style scoped>
.course-chapters {
  min-height: 100vh;
  background: #f8f9fa;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 页面头部 */
.page-header {
  background: linear-gradient(135deg, #2196F3 0%, #ffffff 100%);
  color: #333;
  padding: 30px 0;
}

.header-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.btn-back {
  align-self: flex-start;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 6px;
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-back:hover {
  background: rgba(255, 255, 255, 0.3);
}

.course-title {
  font-size: 1.8rem;
  font-weight: bold;
  margin-bottom: 10px;
}

.course-meta {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  opacity: 0.9;
}

.meta-item .icon {
  font-size: 16px;
}

/* 章节内容 */
.chapters-content {
  padding: 30px 0;
}

/* 加载和空状态 */
.loading-state,
.empty-state {
  text-align: center;
  padding: 80px 0;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 20px;
}

.empty-state h3 {
  color: #333;
  margin-bottom: 10px;
}

.empty-state p {
  color: #666;
}

/* 章节列表 */
.chapters-list {
  space-y: 20px;
}

.chapter-item {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  margin-bottom: 20px;
}

.chapter-header {
  display: flex;
  align-items: center;
  padding: 20px;
  cursor: pointer;
  transition: background 0.3s ease;
}

.chapter-header:hover {
  background: #f8f9fa;
}

.chapter-info {
  flex: 1;
  display: flex;
  gap: 15px;
}

.chapter-number {
  background: #2196F3;
  color: white;
  padding: 8px 12px;
  border-radius: 6px;
  font-weight: bold;
  font-size: 14px;
  align-self: flex-start;
}

.chapter-details {
  flex: 1;
}

.chapter-title {
  font-size: 1.2rem;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.chapter-description {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 10px;
}

.chapter-meta {
  display: flex;
  gap: 15px;
  font-size: 14px;
  color: #999;
}

.chapter-toggle {
  padding: 10px;
}

.toggle-icon {
  font-size: 12px;
  color: #999;
  transition: transform 0.3s ease;
}

.toggle-icon.expanded {
  transform: rotate(180deg);
}

/* 小节列表 */
.sections-list {
  border-top: 1px solid #e9ecef;
  background: #f8f9fa;
}

.section-item {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #e9ecef;
  cursor: pointer;
  transition: background 0.3s ease;
}

.section-item:hover {
  background: #e9ecef;
}

.section-item:last-child {
  border-bottom: none;
}

.section-number {
  background: #f1f3f4;
  color: #666;
  padding: 6px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  margin-right: 15px;
}

.section-info {
  flex: 1;
}

.section-title {
  font-size: 1rem;
  font-weight: 500;
  color: #333;
  margin-bottom: 6px;
}

.section-description {
  color: #666;
  font-size: 13px;
  line-height: 1.4;
  margin-bottom: 8px;
}

.section-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
}

.section-duration {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #666;
}

.free-badge {
  background: #4ecdc4;
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 11px;
}

.section-status {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 11px;
}

.section-status.published {
  background: #d4edda;
  color: #155724;
}

.section-status.draft {
  background: #fff3cd;
  color: #856404;
}

.section-status.offline {
  background: #f8d7da;
  color: #721c24;
}

.section-actions {
  margin-left: 15px;
}

.btn-play,
.btn-preview {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-play {
  background: #2196F3;
  color: white;
}

.btn-play:hover {
  background: #1976D2;
}

.btn-preview {
  background: #f8f9fa;
  color: #666;
  border: 1px solid #ddd;
}

.btn-preview:hover {
  background: #e9ecef;
}

.locked-section {
  color: #999;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 底部操作 */
.bottom-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 30px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.btn-primary,
.btn-secondary {
  padding: 12px 24px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-primary {
  background: #2196F3;
  color: white;
}

.btn-primary:hover {
  background: #1976D2;
}

.btn-secondary {
  background: #f8f9fa;
  color: #666;
  border: 1px solid #ddd;
}

.btn-secondary:hover {
  background: #e9ecef;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .course-meta {
    flex-direction: column;
    gap: 10px;
  }
  
  .chapter-header {
    padding: 15px;
  }
  
  .chapter-info {
    flex-direction: column;
    gap: 10px;
  }
  
  .section-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
    padding: 15px;
  }
  
  .section-actions {
    margin-left: 0;
    align-self: flex-end;
  }
  
  .bottom-actions {
    flex-direction: column;
    gap: 15px;
  }
  
  .btn-primary,
  .btn-secondary {
    width: 100%;
  }
}
</style>