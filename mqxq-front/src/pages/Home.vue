<template>
  <div class="home-page">
    <Navbar />
    
    <!-- AI 聊天助手 -->
    <AIChatAssistant />
    
    <!-- 搜索区域 -->
    <section class="search-section bg-white py-8 shadow-sm">
      <div class="container mx-auto px-4">
        <div class="max-w-3xl mx-auto">
          <div class="search-input-wrapper flex items-center gap-3">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索课程..."
              size="large"
              class="search-input flex-1"
              @keyup.enter="handleSearch"
              clearable
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button 
              type="primary" 
              size="large"
              @click="handleSearch"
              class="search-button"
            >
              搜索
            </el-button>
          </div>
        </div>
      </div>
    </section>

    <!-- 轮播图区域 -->
    <section class="hero-section">
      <div class="container mx-auto px-4">
        <el-carousel 
          height="400px" 
          class="hero-carousel"
          v-loading="loading.banner"
          :autoplay="true"
          :interval="5000"
          indicator-position="outside"
        >
          <el-carousel-item v-for="item in bannerList" :key="item.id">
            <div class="carousel-item" :style="{ backgroundImage: `url(${item.carouselUrl})` }">
              <div class="carousel-content">
                <h2 class="text-4xl font-bold text-white mb-4">
                  {{ item.title }}
                </h2>
                <p class="text-xl text-white/90 mb-6">
                  {{ item.description }}
                </p>
                <el-button 
                  v-if="item.linkUrl" 
                  type="primary" 
                  size="large" 
                  @click="handleBannerClick(item)"
                  class="banner-button"
                >
                  {{ item.buttonText }}
                </el-button>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
    </section>

     <!-- 最新公告 -->
    <section class="announcements py-12 bg-gray-50">
      <div class="container mx-auto px-4">
        <div class="flex justify-between items-center mb-8">
          <h2 class="text-3xl font-bold">最新公告</h2>
          <el-button type="primary" plain @click="$router.push('/announcements')">
            查看更多
          </el-button>
        </div>
        <div 
          class="announcement-list bg-white rounded-lg shadow-sm"
          v-loading="loading.announcements"
        >
          <div
            v-for="(announcement, index) in announcements"
            :key="announcement.id"
            class="announcement-item flex items-center justify-between p-4 hover:bg-gray-50 transition-colors cursor-pointer"
            :class="{ 'border-b border-gray-100': index < announcements.length - 1 }"
            @click="handleAnnouncementClick(announcement)"
          >
            <div class="announcement-content flex-1">
              <div class="flex items-center gap-3 mb-2">
                <span class="announcement-type px-2 py-1 text-xs rounded bg-blue-100 text-blue-800">
                  {{ announcement.categoryName || '公告' }}
                </span>
                <h3 class="font-semibold text-lg text-gray-800 line-clamp-1">{{ announcement.title }}</h3>
              </div>
            </div>
            <div class="announcement-meta text-right ml-4">
              <span class="text-sm text-gray-500">{{ formatDate(announcement.publishDate) }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 课程分类导航 -->
    <section class="categories-section py-12 bg-gray-50">
      <div class="container mx-auto px-4">
        <h2 class="text-3xl font-bold text-center mb-8">课程分类</h2>
        <div 
          class="categories-container max-w-6xl mx-auto"
          v-loading="loading.categories"
        >
          <!-- 一级分类横向排列 -->
          <div class="top-categories-row flex flex-wrap justify-center gap-4 mb-6">
            <div
              v-for="category in categories"
              :key="category.id"
              class="top-category-item"
              :class="{ active: activeTopCategoryId === category.id }"
              @click="toggleTopCategory(category.id)"
            >
              <div class="flex items-center gap-2">
                <el-icon :size="20">
                  <component :is="VideoPlay" />
                </el-icon>
                <span class="font-semibold">{{ category.name }}</span>
              </div>
            </div>
          </div>
          
          <!-- 二级分类展开区域 -->
          <transition name="slide-fade">
            <div 
              v-if="activeTopCategoryId && activeCategory"
              class="sub-categories-area bg-white rounded-lg shadow-md p-6"
            >
              <div class="flex items-center mb-4 pb-3 border-b">
                <el-icon :size="24" color="#2563eb" class="mr-2">
                  <component :is="VideoPlay" />
                </el-icon>
                <h3 class="text-xl font-bold text-gray-800">{{ activeCategory.name }}</h3>
                <span class="ml-auto text-sm text-gray-500">
                  共 {{ activeCategory.children?.length || 0 }} 个子分类
                </span>
              </div>
              
              <div 
                v-if="activeCategory.children && activeCategory.children.length > 0"
                class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-3"
              >
                <div
                  v-for="child in activeCategory.children"
                  :key="child.id"
                  class="child-category-card bg-gray-50 rounded-lg px-4 py-3 text-center hover:bg-blue-50 hover:shadow-md transition-all cursor-pointer border border-transparent hover:border-blue-300"
                  @click="handleCategoryClick(child)"
                >
                  <span class="text-sm font-medium text-gray-700 hover:text-blue-600">{{ child.name }}</span>
                </div>
              </div>
              <div v-else class="text-center text-gray-400 py-8">
                暂无子分类
              </div>
            </div>
          </transition>
        </div>
      </div>
    </section>

    <!-- 热门课程推荐 -->
    <section class="popular-courses py-12">
      <div class="container mx-auto px-4">
        <div class="flex justify-between items-center mb-8">
          <h2 class="text-3xl font-bold">热门课程</h2>
          <el-button type="primary" plain @click="$router.push('/courses')">
            查看更多
          </el-button>
        </div>
        <div 
          class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6"
          v-loading="loading.courses"
        >
          <div
            v-for="course in popularCourses"
            :key="course.id"
            class="course-card bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow overflow-hidden cursor-pointer"
            @click="handleCourseClick(course)"
          >
            <div class="course-image">
              <img :src="course.cover" :alt="course.title" class="w-full h-48 object-cover" />
              <div class="course-badge" v-if="course.status === 1">
                <span class="bg-red-500 text-white px-2 py-1 text-xs rounded">热门</span>
              </div>
            </div>
            <div class="course-content p-4">
              <h3 class="font-semibold text-lg mb-2 line-clamp-2">{{ course.title }}</h3>
              <p class="text-gray-600 text-sm mb-3 line-clamp-2">{{ course.description }}</p>
              <div class="course-stats flex items-center justify-between text-sm text-gray-500 mb-3">
                <span>{{ course.buyCount }}人购买</span>
                <span>{{ course.lessonNum }}课时</span>
                <span>{{ Math.floor(course.durationSum / 60) }}分钟</span>
              </div>
              <div class="course-footer flex items-center justify-between">
                <div class="price">
                  <span class="text-2xl font-bold text-red-500">¥{{ course.price }}</span>
                </div>
                <div class="view-count text-sm text-gray-600">
                  {{ course.viewCount }}次游览
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Search, 
  VideoPlay, 
  Document, 
  DataAnalysis, 
  User, 
  Trophy,
  Star
} from '@element-plus/icons-vue'

// API 导入
import Navbar from '@/components/Navbar.vue'
import AIChatAssistant from '@/components/AIChatAssistant.vue'
import { publicApi } from '@/api'
import type { Carousel } from '@/api/types/carousel'
import type { CourseSubject, Course } from '@/api/types/course'
import type { Announcement } from '@/api/types/announcement'

const router = useRouter()

// 加载状态
const loading = ref({
  banner: false,
  categories: false,
  courses: false,
  announcements: false
})

// 搜索关键词
const searchKeyword = ref('')

// 轮播图数据
const bannerList = ref<Carousel[]>([])

// 课程分类数据
const categories = ref<CourseSubject[]>([])

// 当前激活的一级分类ID
const activeTopCategoryId = ref<number | null>(null)

// 当前激活的一级分类对象
const activeCategory = ref<CourseSubject | null>(null)

// 热门课程数据
const popularCourses = ref<Course[]>([])

// 公告数据
const announcements = ref<Announcement[]>([])


// 默认轮播图文案
const defaultBannerTexts = [
  {
    title: '精彩课程推荐',
    description: '发现更多优质学习内容',
    buttonText: '立即探索'
  },
  {
    title: '名师在线授课',
    description: '跟随行业专家，掌握核心技能',
    buttonText: '查看课程'
  },
  {
    title: '系统化学习路径',
    description: '从入门到精通，助力职业成长',
    buttonText: '开始学习'
  },
  {
    title: '实战项目驱动',
    description: '理论结合实践，快速提升能力',
    buttonText: '了解更多'
  },
  {
    title: '随时随地学习',
    description: '灵活安排时间，高效掌握知识',
    buttonText: '立即加入'
  }
]

// 获取轮播图数据
const fetchBannerList = async () => {
  loading.value.banner = true
  try {
    const data = await publicApi.getHomepageCarousel(5)
    // 为每张轮播图添加默认文案
    bannerList.value = data.map((item, index) => ({
      ...item,
      title: item.title || defaultBannerTexts[index]?.title || '精彩课程推荐',
      description: item.description || defaultBannerTexts[index]?.description || '发现更多优质学习内容',
      buttonText: item.buttonText || defaultBannerTexts[index]?.buttonText || '了解更多'
    }))
  } catch (error) {
    console.error('获取轮播图失败:', error)
    ElMessage.error('获取轮播图失败')
  } finally {
    loading.value.banner = false
  }
}

// 获取课程分类数据
const fetchCategories = async () => {
  loading.value.categories = true
  try {
    const data = await publicApi.getSubjectTree()
    // API返回的数据已经是树形结构，直接使用
    // 只获取一级分类（parentId为0的项）
    categories.value = data.filter(item => item.parentId === 0)
    
    // 默认展开第一个分类
    if (categories.value.length > 0) {
      activeTopCategoryId.value = categories.value[0].id
      activeCategory.value = categories.value[0]
    }
    
    console.log('分类数据:', categories.value)
  } catch (error) {
    console.error('获取课程分类失败:', error)
    ElMessage.error('获取课程分类失败')
  } finally {
    loading.value.categories = false
  }
}

// 切换一级分类
const toggleTopCategory = (categoryId: number) => {
  if (activeTopCategoryId.value === categoryId) {
    // 如果点击的是当前激活的分类，则收起
    activeTopCategoryId.value = null
    activeCategory.value = null
  } else {
    // 否则展开新的分类
    activeTopCategoryId.value = categoryId
    activeCategory.value = categories.value.find(cat => cat.id === categoryId) || null
  }
}

// 获取热门课程数据
const fetchPopularCourses = async () => {
  loading.value.courses = true
  try {
    const data = await publicApi.getHotCourses(8)
    popularCourses.value = data
  } catch (error) {
    console.error('获取热门课程失败:', error)
    ElMessage.error('获取热门课程失败')
  } finally {
    loading.value.courses = false
  }
}

// 获取公告数据
const fetchAnnouncements = async () => {
  loading.value.announcements = true
  try {
    const data = await publicApi.getLatestAnnouncements(6)
    announcements.value = data
  } catch (error) {
    console.error('获取公告失败:', error)
    ElMessage.error('获取公告失败')
  } finally {
    loading.value.announcements = false
  }
}

// 事件处理函数
const handleSearch = async () => {
  if (searchKeyword.value.trim()) {
    router.push({
      path: '/search',
      query: { q: searchKeyword.value.trim() }
    })
  }
}

const handleBannerClick = (banner: Carousel) => {
  if (banner.linkUrl) {
    if (banner.linkUrl.startsWith('http')) {
      window.open(banner.linkUrl, '_blank')
    } else {
      router.push(banner.linkUrl)
    }
  }
}

const handleCategoryClick = (category: CourseSubject) => {
  router.push({
    path: '/courses',
    query: { subjectId: category.id }
  })
}

const handleCourseClick = (course: Course) => {
  router.push(`/courses/${course.id}`)
}

const handleAnnouncementClick = (announcement: Announcement) => {
  router.push(`/announcements/${announcement.id}`)
}

const handleActionClick = (action: any) => {
  router.push(action.route)
}

// 工具函数
const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: 'numeric',
    minute: 'numeric',
    second: 'numeric'
  })
}

const getAnnouncementTypeClass = (type: string) => {
  const typeClasses: Record<string, string> = {
    system: 'bg-blue-100 text-blue-800',
    notice: 'bg-red-100 text-red-800',
    course: 'bg-green-100 text-green-800',
    activity: 'bg-purple-100 text-purple-800'
  }
  return typeClasses[type] || 'bg-gray-100 text-gray-800'
}

// 获取分类图标
const getCategoryIcon = (iconName: string) => {
  const iconMap: Record<string, any> = {
    VideoPlay,
    Document,
    DataAnalysis,
    Star,
    Trophy,
    User
  }
  return iconMap[iconName] || VideoPlay
}

// 初始化数据
const initPageData = async () => {
  await Promise.all([
    fetchBannerList(),
    fetchCategories(),
    fetchPopularCourses(),
    fetchAnnouncements()
  ])
}

// 生命周期
onMounted(() => {
  initPageData()
})
</script>

<style scoped lang="scss">
.home-page {
  min-height: 100vh;
  min-width: 100vw;
  margin: 0 auto;
  padding: 0 20px;
}

.hero-section {
  .hero-carousel {
    border-radius: 12px;
    overflow: hidden;
    
    :deep(.el-carousel__indicators) {
      bottom: -35px;
    }
    
    :deep(.el-carousel__indicator) {
      .el-carousel__button {
        width: 30px;
        height: 4px;
        border-radius: 2px;
        background: rgba(79, 70, 229, 0.3);
      }
      
      &.is-active .el-carousel__button {
        background: #4F46E5;
      }
    }
  }
  
  .carousel-item {
    height: 400px;
    background-size: cover;
    background-position: center;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: linear-gradient(
        to bottom,
        rgba(0, 0, 0, 0.3) 0%,
        rgba(0, 0, 0, 0.5) 100%
      );
    }
  }
  
  .carousel-content {
    text-align: center;
    position: relative;
    z-index: 1;
    max-width: 800px;
    padding: 0 20px;
    
    h2 {
      text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
      animation: fadeInDown 0.8s ease;
    }
    
    p {
      text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
      animation: fadeInUp 0.8s ease 0.2s both;
    }
    
    .banner-button {
      animation: fadeInUp 0.8s ease 0.4s both;
      box-shadow: 0 4px 12px rgba(79, 70, 229, 0.4);
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(79, 70, 229, 0.5);
      }
    }
  }
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.search-section {
  .search-input-wrapper {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .search-input {
    flex: 1;
    
    :deep(.el-input__wrapper) {
      border-radius: 25px;
      padding: 10px 20px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
      transition: all 0.3s ease;
      
      &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
      }
      
      &.is-focus {
        box-shadow: 0 4px 16px rgba(33, 150, 243, 0.2);
      }
    }
    
    :deep(.el-input__prefix) {
      font-size: 18px;
      color: #999;
    }
  }

  .search-button {
    border-radius: 25px;
    padding: 28px 32px;
    font-weight: 500;
    box-shadow: 0 2px 8px rgba(33, 150, 243, 0.3);
    transition: all 0.3s ease;
    white-space: nowrap;
    
    &:hover {
      box-shadow: 0 4px 12px rgba(33, 150, 243, 0.4);
      transform: translateY(-1px);
    }
    
    &:active {
      transform: translateY(0);
    }
  }
}

.categories-container {
  .top-categories-row {
    .top-category-item {
      background: white;
      padding: 12px 24px;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s ease;
      border: 2px solid transparent;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      
      &:hover {
        border-color: #2563eb;
        box-shadow: 0 4px 12px rgba(37, 99, 235, 0.2);
        transform: translateY(-2px);
      }
      
      &.active {
        background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
        color: white;
        border-color: #2563eb;
        box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
        
        .el-icon {
          color: white;
        }
      }
    }
  }

  .sub-categories-area {
    animation: slideDown 0.3s ease;
  }

  .child-category-card {
    transition: all 0.2s ease;
    
    &:hover {
      transform: translateY(-2px);
    }
  }
}

// 动画效果
.slide-fade-enter-active {
  transition: all 0.3s ease;
}

.slide-fade-leave-active {
  transition: all 0.2s ease;
}

.slide-fade-enter-from {
  transform: translateY(-10px);
  opacity: 0;
}

.slide-fade-leave-to {
  transform: translateY(-10px);
  opacity: 0;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.course-card {
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-4px);
  }
  
  .course-image {
    position: relative;
    
    .course-badge {
      position: absolute;
      top: 12px;
      right: 12px;
    }
  }
}

.announcement-list {
  .announcement-item {
    transition: all 0.2s ease;
    
    &:hover {
      background-color: #f9fafb;
    }
    
    &:first-child {
      border-top-left-radius: 8px;
      border-top-right-radius: 8px;
    }
    
    &:last-child {
      border-bottom-left-radius: 8px;
      border-bottom-right-radius: 8px;
    }
  }
  
  .announcement-content {
    min-width: 0; // 确保flex子元素可以收缩
  }
  
  .announcement-meta {
    flex-shrink: 0;
  }
}

.action-card {
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-4px);
  }
}

// 文本截断样式
.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>