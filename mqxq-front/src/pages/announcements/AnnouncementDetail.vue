<template>
  <div class="announcement-detail-page min-h-screen bg-gray-50">
    <Navbar />
    
    <!-- 加载状�?-->
    <div v-if="loading" class="min-h-screen flex items-center justify-center">
      <div class="text-center">
        <div class="relative">
          <div class="w-20 h-20 border-4 border-indigo-200 border-t-indigo-600 rounded-full animate-spin mx-auto mb-6"></div>
          <div class="absolute inset-0 flex items-center justify-center">
            <i class="fas fa-file-alt text-indigo-600 text-xl"></i>
          </div>
        </div>
        <h3 class="text-xl font-semibold text-gray-900 mb-2">正在加载公告详情</h3>
        <p class="text-gray-500">请稍�?..</p>
      </div>
    </div>

    <!-- 公告详情内容 -->
    <div v-else-if="announcement" class="min-h-screen">
      <!-- 面包屑导�?-->
      <div class="bg-white border-b border-gray-200 shadow-sm">
        <div class="container mx-auto px-4 py-4">
          <el-breadcrumb separator="/" class="breadcrumb-custom">
            <el-breadcrumb-item>
              <router-link to="/" class="flex items-center text-indigo-600 hover:text-indigo-800 transition-colors">
                <i class="fas fa-home mr-2"></i>
                首页
              </router-link>
            </el-breadcrumb-item>
            <el-breadcrumb-item>
              <router-link to="/announcements" class="flex items-center text-indigo-600 hover:text-indigo-800 transition-colors">
                <i class="fas fa-bullhorn mr-2"></i>
                系统公告
              </router-link>
            </el-breadcrumb-item>
            <el-breadcrumb-item class="text-gray-500 flex items-center">
              <i class="fas fa-file-alt mr-2"></i>
              公告详情
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
      </div>

      <!-- 公告内容 -->
      <div class="container mx-auto px-4 py-8">
        <div class="max-w-5xl mx-auto">
          <!-- 公告头部信息 -->
          <div class="bg-white rounded-3xl shadow-2xl border border-gray-100 overflow-hidden mb-8">
            <div class="h-2 bg-gradient-to-r from-blue-500 via-blue-400 to-blue-300"></div>
            
            <div class="p-10">
              <div class="text-center mb-8">
                <div class="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-br from-blue-500 to-blue-400 rounded-full mb-6">
                  <i class="fas fa-bullhorn text-white text-xl"></i>
                </div>
                <h1 class="text-3xl md:text-4xl font-bold text-gray-900 mb-4 leading-tight">
                  {{ announcement.title }}
                </h1>
              </div>
              
              <div class="flex flex-wrap items-center justify-center gap-4 mb-8">
                <div class="flex items-center px-4 py-2 bg-blue-50 text-blue-700 rounded-full border border-blue-200">
                  <i class="fas fa-calendar-alt mr-3 text-blue-500"></i>
                  <span class="font-medium">发布时间</span>
                  <span class="ml-2 font-semibold">{{ formatDateTime(announcement.publishDate) }}</span>
                </div>
                
                <div v-if="categoryName" class="flex items-center px-4 py-2 bg-blue-50 text-blue-700 rounded-full border border-blue-200">
                  <i class="fas fa-tag mr-3 text-blue-500"></i>
                  <span class="font-medium">分类</span>
                  <span class="ml-2 font-semibold">{{ categoryName }}</span>
                </div>
                
                <div class="flex items-center px-4 py-2 bg-green-50 text-green-700 rounded-full border border-green-200">
                  <i class="fas fa-clock mr-3 text-green-500"></i>
                  <span class="font-medium">更新时间</span>
                  <span class="ml-2 font-semibold">{{ formatDateTime(announcement.updateTime) }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 公告内容 -->
          <div class="bg-white rounded-3xl shadow-2xl border border-gray-100 p-10 mb-8">
            <div class="prose prose-lg max-w-none">
              <div 
                class="announcement-content text-gray-800 leading-relaxed text-lg"
                v-html="processedContent"
                @click="handleContentClick"
              ></div>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="bg-white rounded-3xl shadow-2xl border border-gray-100 p-8 mb-8">
            <div class="flex flex-col lg:flex-row gap-6 justify-between items-center">
              <div class="flex flex-col sm:flex-row gap-4">
                <el-button 
                  @click="goBack" 
                  size="large"
                  class="action-btn back-btn px-8 py-3"
                >
                  <i class="fas fa-arrow-left mr-2"></i>
                  返回列表
                </el-button>
                
                <el-button 
                  type="primary" 
                  @click="shareAnnouncement" 
                  size="large"
                  class="action-btn share-btn px-8 py-3"
                >
                  <i class="fas fa-share-alt mr-2"></i>
                  分享公告
                </el-button>
              </div>
            </div>
          </div>

          <!-- 相关公告推荐 -->
          <div v-if="relatedAnnouncements.length > 0" class="bg-white rounded-3xl shadow-2xl border border-gray-100 p-8">
            <div class="text-center mb-8">
              <div class="inline-flex items-center justify-center w-12 h-12 bg-gradient-to-br from-blue-500 to-blue-400 rounded-full mb-4">
                <i class="fas fa-list-ul text-white"></i>
              </div>
              <h3 class="text-2xl font-bold text-gray-900">相关公告推荐</h3>
              <p class="text-gray-500 mt-2">您可能感兴趣的其他公告</p>
            </div>
            
            <div class="grid gap-4">
              <div
                v-for="(related, index) in relatedAnnouncements"
                :key="related.id"
                class="group flex items-center justify-between p-6 hover:bg-gradient-to-r hover:from-indigo-50 hover:to-purple-50 rounded-2xl cursor-pointer transition-all duration-300 border border-gray-100 hover:border-indigo-200"
                @click="goToAnnouncement(related.id)"
              >
                <div class="flex items-center flex-1">
                  <div class="w-10 h-10 bg-gradient-to-br from-blue-500 to-blue-400 rounded-full flex items-center justify-center text-white font-bold mr-4">
                    {{ index + 1 }}
                  </div>
                  <div class="flex-1">
                    <h4 class="font-semibold text-gray-900 group-hover:text-blue-600 transition-colors mb-1">
                      {{ related.title }}
                    </h4>
                    <p class="text-sm text-gray-500 flex items-center">
                      <i class="fas fa-calendar-alt mr-2"></i>
                      {{ formatDate(related.publishDate) }}
                    </p>
                  </div>
                </div>
                <div class="ml-4 w-8 h-8 bg-gray-100 group-hover:bg-blue-500 rounded-full flex items-center justify-center transition-colors">
                  <i class="fas fa-chevron-right text-gray-400 group-hover:text-white text-sm"></i>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 错误状�?-->
    <div v-else class="min-h-screen flex items-center justify-center bg-gray-50">
      <div class="text-center max-w-md mx-auto px-4">
        <div class="relative mb-8">
          <div class="w-32 h-32 mx-auto bg-gradient-to-br from-red-100 to-orange-100 rounded-full flex items-center justify-center">
            <i class="fas fa-exclamation-triangle text-5xl text-red-500"></i>
          </div>
          <div class="absolute -bottom-2 -right-2 w-12 h-12 bg-red-500 rounded-full flex items-center justify-center animate-pulse">
            <i class="fas fa-times text-white"></i>
          </div>
        </div>
        
        <h3 class="text-3xl font-bold text-gray-900 mb-4">公告不存在</h3>
        <p class="text-gray-500 text-lg leading-relaxed mb-8">
          抱歉，您访问的公告不存在或已被删除。<br>
          请检查链接是否正确或返回公告列表。
        </p>
        
        <div class="flex flex-col sm:flex-row gap-4 justify-center">
          <el-button 
            type="primary" 
            size="large"
            @click="goBack"
            class="px-8 py-3"
          >
            <i class="fas fa-arrow-left mr-2"></i>
            返回公告列表
          </el-button>
          <el-button 
            size="large"
            @click="$router.push('/')"
            class="px-8 py-3"
          >
            <i class="fas fa-home mr-2"></i>
            返回首页
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import Navbar from '@/components/Navbar.vue'
import { publicApi } from '@/api'
import type { Announcement, AnnouncementCategory } from '@/api'

const route = useRoute()
const router = useRouter()

// 响应式数�?
const loading = ref(false)
const announcement = ref<Announcement | null>(null)
const categories = ref<AnnouncementCategory[]>([])
const relatedAnnouncements = ref<Announcement[]>([])

// 计算属�?
const categoryName = computed(() => {
  if (!announcement.value) return ''
  const category = categories.value.find(c => c.id === announcement.value!.categoryId)
  return category?.name || ''
})

// 处理内容中的媒体文件
const processedContent = computed(() => {
  if (!announcement.value?.content) return ''
  
  let content = announcement.value.content
  
  // 处理图片URL - 自动渲染为img标签
  const imageRegex = /(https?:\/\/[^\s<>"]+\.(?:png|jpg|jpeg|gif|webp|svg))/gi
  content = content.replace(imageRegex, (match) => {
    return `<div class="media-container image-container clickable-image-container" data-image-src="${match}">
      <img src="${match}" alt="公告图片" class="announcement-image" loading="lazy" />
      <div class="media-overlay">
        <i class="fas fa-search-plus"></i>
        <span class="overlay-text">点击查看大图</span>
      </div>
    </div>`
  })
  
  // 处理视频URL
  const videoRegex = /(https?:\/\/[^\s<>"]+\.(?:mp4|webm|ogg|avi|mov))/gi
  content = content.replace(videoRegex, (match) => {
    return `<div class="media-container video-container">
      <video controls class="announcement-video" preload="metadata">
        <source src="${match}" type="video/mp4">
        您的浏览器不支持视频播放�?
      </video>
    </div>`
  })
  
  return content
})

// 获取公告详情
const getAnnouncementDetail = async () => {
  const id = Number(route.params.id)
  if (!id) {
    ElMessage.error('公告ID无效')
    return
  }

  try {
    loading.value = true
    const response = await publicApi.getAnnouncementDetail(id)
    announcement.value = (response as any).data || response
    
    if (announcement.value && announcement.value.categoryId) {
      getRelatedAnnouncements(announcement.value.categoryId, id)
    }
  } catch (error) {
    console.error('获取公告详情失败:', error)
    ElMessage.error('获取公告详情失败')
    announcement.value = null
  } finally {
    loading.value = false
  }
}

// 获取公告分类
const getCategories = async () => {
  try {
    const response = await publicApi.getAllAnnouncementCategories()
    categories.value = (response as any).data?.list || (response as any).list || response || []
  } catch (error) {
    console.error('获取公告分类失败:', error)
    categories.value = []
  }
}

// 获取相关公告
const getRelatedAnnouncements = async (categoryId: number, currentId: number) => {
  try {
    const response = await publicApi.getAnnouncementsByCategory(categoryId)
    const announcements = (response as any).data?.list || (response as any).list || response || []
    relatedAnnouncements.value = announcements
      .filter((item: any) => item.id !== currentId)
      .slice(0, 5)
  } catch (error) {
    console.error('获取相关公告失败:', error)
    relatedAnnouncements.value = []
  }
}

// 跳转到指定公�?
const goToAnnouncement = (id: number) => {
  router.push({ name: 'AnnouncementDetail', params: { id } })
  getAnnouncementDetail()
}

// 返回列表
const goBack = () => {
  router.push({ name: 'AnnouncementList' })
}

// 分享公告
const shareAnnouncement = () => {
  const url = window.location.href
  const title = announcement.value?.title || '系统公告'
  
  if (navigator.share) {
    navigator.share({ title, url }).catch(() => {
      copyToClipboard(url)
    })
  } else {
    copyToClipboard(url)
  }
}

// 复制到剪贴板
const copyToClipboard = (text: string) => {
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('链接已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制链接')
  })
}

// 格式化日期时�?
const formatDateTime = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 格式化日�?
const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// 显示图片模态框
const showImageModal = (imgSrc: string) => {
  console.log('显示图片模态框:', imgSrc)
  
  const modal = document.createElement('div')
  modal.className = 'image-modal'
  modal.innerHTML = `
    <div class="image-modal-backdrop">
      <div class="image-modal-content">
        <img src="${imgSrc}" alt="放大图片" class="modal-image" />
        <button class="modal-close">
          <i class="fas fa-times"></i>
        </button>
      </div>
    </div>
  `
  
  const style = document.createElement('style')
  style.id = 'image-modal-style'
  style.textContent = `
    .image-modal {
      position: fixed;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      z-index: 9999;
      animation: fadeIn 0.3s ease;
    }
    
    .image-modal-backdrop {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: rgba(0, 0, 0, 0.9);
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 2rem;
      cursor: pointer;
    }
    
    .image-modal-content {
      position: relative;
      max-width: 90%;
      max-height: 90%;
      cursor: default;
    }
    
    .modal-image {
      max-width: 100%;
      max-height: 100%;
      border-radius: 1rem;
      box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
      display: block;
    }
    
    .modal-close {
      position: absolute;
      top: -3rem;
      right: -1rem;
      width: 3rem;
      height: 3rem;
      background: rgba(255, 255, 255, 0.2);
      border: none;
      border-radius: 50%;
      color: white;
      font-size: 1.2rem;
      cursor: pointer;
      transition: all 0.3s ease;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    
    .modal-close:hover {
      background: rgba(255, 255, 255, 0.3);
      transform: scale(1.1);
    }
    
    @keyframes fadeIn {
      from { opacity: 0; transform: scale(0.8); }
      to { opacity: 1; transform: scale(1); }
    }
  `
  
  const existingStyle = document.getElementById('image-modal-style')
  if (existingStyle) {
    document.head.removeChild(existingStyle)
  }
  
  document.head.appendChild(style)
  document.body.appendChild(modal)
  document.body.style.overflow = 'hidden'
  
  const closeModal = () => {
    if (document.body.contains(modal)) {
      document.body.removeChild(modal)
    }
    if (document.head.contains(style)) {
      document.head.removeChild(style)
    }
    document.body.style.overflow = ''
    document.removeEventListener('keydown', handleKeydown)
  }
  
  modal.querySelector('.modal-close')?.addEventListener('click', (e) => {
    e.stopPropagation()
    closeModal()
  })
  
  modal.querySelector('.image-modal-backdrop')?.addEventListener('click', (e) => {
    if (e.target === e.currentTarget) {
      closeModal()
    }
  })
  
  modal.querySelector('.image-modal-content')?.addEventListener('click', (e) => {
    e.stopPropagation()
  })
  
  const handleKeydown = (e: KeyboardEvent) => {
    if (e.key === 'Escape') {
      closeModal()
    }
  }
  document.addEventListener('keydown', handleKeydown)
}

// 处理内容区域点击事件
const handleContentClick = (event: Event) => {
  const target = event.target as HTMLElement
  console.log('内容区域点击:', target.tagName, target.className)
  
  let currentElement = target
  while (currentElement && currentElement !== event.currentTarget) {
    if (currentElement.classList.contains('clickable-image-container')) {
      const imgSrc = currentElement.getAttribute('data-image-src')
      if (imgSrc) {
        console.log('找到图片容器，显示图�?', imgSrc)
        event.preventDefault()
        event.stopPropagation()
        showImageModal(imgSrc)
        return
      }
    }
    currentElement = currentElement.parentElement as HTMLElement
  }
}

// 组件挂载
onMounted(() => {
  getCategories()
  getAnnouncementDetail()
})

// 组件卸载
onUnmounted(() => {
  const existingModal = document.querySelector('.image-modal')
  if (existingModal) {
    document.body.removeChild(existingModal)
  }
  const existingStyle = document.getElementById('image-modal-style')
  if (existingStyle) {
    document.head.removeChild(existingStyle)
  }
  document.body.style.overflow = ''
})
</script>

<style scoped>
.breadcrumb-custom :deep(.el-breadcrumb__item) {
  font-weight: 500;
}

.breadcrumb-custom :deep(.el-breadcrumb__separator) {
  color: #2563eb;
  font-weight: bold;
}

.action-btn {
  border-radius: 12px;
  font-weight: 600;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.back-btn {
  background: linear-gradient(135deg, #f3f4f6, #e5e7eb);
  color: #374151;
  border-color: #d1d5db;
}

.back-btn:hover {
  background: linear-gradient(135deg, #e5e7eb, #d1d5db);
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.share-btn {
  background: linear-gradient(135deg, #2563eb, #3b82f6);
  border: none;
}

.share-btn:hover {
  background: linear-gradient(135deg, #1d4ed8, #2563eb);
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.4);
}

.announcement-content {
  line-height: 2;
  font-size: 1.1rem;
}

.announcement-content :deep(.media-container) {
  margin: 2em 0;
  position: relative;
  border-radius: 1rem;
  overflow: hidden;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.announcement-content :deep(.media-container:hover) {
  transform: translateY(-5px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
}

.announcement-content :deep(.image-container) {
  position: relative;
  display: inline-block;
  width: 100%;
  cursor: pointer;
}

.announcement-content :deep(.clickable-image-container) {
  cursor: pointer !important;
}

.announcement-content :deep(.announcement-image) {
  width: 100%;
  height: auto;
  display: block;
  border-radius: 1rem;
  transition: all 0.3s ease;
  cursor: pointer;
}

.announcement-content :deep(.media-overlay) {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  border-radius: 1rem;
  cursor: pointer;
}

.announcement-content :deep(.image-container:hover .media-overlay) {
  opacity: 1;
}

.announcement-content :deep(.media-overlay i) {
  color: white;
  font-size: 2rem;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  margin-bottom: 0.5rem;
}

.announcement-content :deep(.overlay-text) {
  color: white;
  font-size: 0.9rem;
  font-weight: 600;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.announcement-content :deep(.video-container) {
  background: linear-gradient(135deg, #1e293b, #334155);
  padding: 1rem;
  border-radius: 1rem;
}

.announcement-content :deep(.announcement-video) {
  width: 100%;
  height: auto;
  border-radius: 0.5rem;
  background: #000;
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(50px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.announcement-detail-page {
  animation: slideInUp 0.6s ease-out;
}

@media (max-width: 768px) {
  .announcement-content {
    font-size: 1rem;
    line-height: 1.8;
  }
  
  .action-btn {
    width: 100%;
    margin-bottom: 0.5rem;
  }
}
</style>