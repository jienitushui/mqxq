<template>
  <div class="announcement-list-page min-h-screen bg-gray-50">
    <Navbar />
    
    <!-- 页面头部 - 增强视觉效果 -->
    <div class="relative bg-gradient-to-br from-blue-600 via-blue-500 to-blue-400 text-white py-20 overflow-hidden">
      <!-- 背景装饰 -->
      <div class="absolute inset-0 bg-black bg-opacity-20"></div>
      <div class="absolute top-0 left-0 w-full h-full">
        <div class="absolute top-10 left-10 w-20 h-20 bg-white bg-opacity-10 rounded-full animate-pulse"></div>
        <div class="absolute top-32 right-20 w-16 h-16 bg-white bg-opacity-10 rounded-full animate-pulse delay-1000"></div>
        <div class="absolute bottom-20 left-1/4 w-12 h-12 bg-white bg-opacity-10 rounded-full animate-pulse delay-500"></div>
      </div>
      
      <div class="container mx-auto px-4 relative z-10">
        <div class="text-center">
          <div class="inline-flex items-center justify-center w-16 h-16 bg-white bg-opacity-20 rounded-full mb-6">
            <i class="fas fa-bullhorn text-2xl"></i>
          </div>
          <h1 class="text-4xl md:text-5xl font-bold mb-4 bg-clip-text text-transparent bg-gradient-to-r from-white to-blue-50">
            系统公告
          </h1>
          <p class="text-xl text-blue-50 max-w-2xl mx-auto leading-relaxed">
            获取最新的平台动态和重要通知，第一时间了解系统更新
          </p>
          <div class="mt-8 flex justify-center">
            <div class="flex items-center space-x-6 text-sm">
              <div class="flex items-center">
                <i class="fas fa-clock mr-2"></i>
                <span>实时更新</span>
              </div>
              <div class="flex items-center">
                <i class="fas fa-bell mr-2"></i>
                <span>重要通知</span>
              </div>
              <div class="flex items-center">
                <i class="fas fa-star mr-2"></i>
                <span>官方发布</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="container mx-auto px-4 py-8 -mt-10 relative z-20">
      <!-- 筛选和搜索区域 - 美化卡片 -->
      <div class="bg-white rounded-2xl shadow-xl border border-gray-100 p-8 mb-8 backdrop-blur-sm">
        <div class="flex flex-col lg:flex-row gap-6">
          <!-- 搜索框 - 增强样式 -->
          <div class="flex-1">
            <label class="block text-sm font-medium text-gray-700 mb-2">
              <i class="fas fa-search mr-2 text-blue-500"></i>
              搜索公告
            </label>
            <el-input
              v-model="searchParams.keyword"
              placeholder="输入关键词搜索公告标题或内容..."
              clearable
              @keyup.enter="handleSearch"
              class="search-input"
              size="large"
            >
              <template #prefix>
                <i class="fas fa-search text-gray-400"></i>
              </template>
            </el-input>
          </div>
          
          <!-- 分类筛选 - 增强样式 -->
          <div class="w-full lg:w-64">
            <label class="block text-sm font-medium text-gray-700 mb-2">
              <i class="fas fa-tag mr-2 text-blue-500"></i>
              公告分类
            </label>
            <el-select
              v-model="searchParams.categoryId"
              placeholder="选择分类筛选"
              clearable
              class="w-full category-select"
              size="large"
              @change="handleSearch"
            >
              <el-option
                v-for="category in categories"
                :key="category.id"
                :label="category.name"
                :value="category.id"
              />
            </el-select>
          </div>
          
          <!-- 搜索按钮 - 增强样式 -->
          <div class="flex items-end">
            <el-button 
              type="primary" 
              @click="handleSearch" 
              :loading="loading"
              size="large"
              class="search-btn px-8 py-3"
            >
              <i class="fas fa-search mr-2"></i>
              {{ loading ? '搜索中...' : '搜索' }}
            </el-button>
          </div>
        </div>
        
        <!-- 快速筛选标签 -->
        <div class="mt-6 pt-6 border-t border-gray-100">
          <div class="flex items-center justify-between">
            <span class="text-sm font-medium text-gray-600">快速筛选：</span>
            <div class="flex flex-wrap gap-2">
              <el-tag 
                v-for="category in categories" 
                :key="category.id"
                :type="searchParams.categoryId === category.id ? 'primary' : undefined"
                class="cursor-pointer hover:scale-105 transition-transform"
                @click="quickFilter(category.id)"
              >
                {{ category.name }}
              </el-tag>
              <el-tag 
                :type="!searchParams.categoryId && !searchParams.keyword ? 'primary' : undefined"
                class="cursor-pointer hover:scale-105 transition-transform"
                @click="clearFilters"
              >
                全部
              </el-tag>
            </div>
          </div>
        </div>
      </div>

      <!-- 公告列表 -->
      <div class="space-y-4">
        <!-- 加载状态 -->
        <div v-if="loading" class="text-center py-12">
          <el-icon class="is-loading text-2xl text-blue-500 mb-2">
            <Loading />
          </el-icon>
          <p class="text-gray-500">加载中...</p>
        </div>

        <!-- 公告卡片列表 - 美化设计 -->
        <div v-else-if="announcements && announcements.length > 0" class="grid gap-6">
          <div
            v-for="(announcement, index) in announcements"
            :key="announcement.id"
            class="group bg-white rounded-2xl shadow-lg hover:shadow-2xl transition-all duration-300 cursor-pointer border border-gray-100 hover:border-indigo-200 transform hover:-translate-y-1"
            @click="goToDetail(announcement.id)"
          >
            <div class="p-8">
              <div class="flex items-start justify-between">
                <div class="flex-1">
                  <!-- 公告标题 - 增强样式 -->
                  <div class="flex items-center mb-4">
                    <div class="w-1 h-8 bg-gradient-to-b from-indigo-500 to-purple-500 rounded-full mr-4"></div>
                    <h3 class="text-xl font-bold text-gray-900 group-hover:text-indigo-600 transition-colors duration-200 line-clamp-2">
                      {{ announcement.title }}
                    </h3>
                  </div>
                  
                  <!-- 公告摘要 - 增强样式 -->
                  <p class="text-gray-600 text-base leading-relaxed line-clamp-3 mb-6">
                    {{ getContentPreview(announcement.content) }}
                  </p>
                  
                  <!-- 公告信息 - 美化标签 -->
                  <div class="flex items-center flex-wrap gap-4">
                    <div class="flex items-center px-3 py-1 bg-blue-50 text-blue-600 rounded-full text-sm font-medium">
                      <i class="fas fa-calendar-alt mr-2"></i>
                      {{ formatDate(announcement.publishDate) }}
                    </div>
                    <div 
                      v-if="getCategoryName(announcement.categoryId)"
                      class="flex items-center px-3 py-1 bg-blue-50 text-blue-600 rounded-full text-sm font-medium"
                    >
                      <i class="fas fa-tag mr-2"></i>
                      {{ getCategoryName(announcement.categoryId) }}
                    </div>
                    <div class="flex items-center px-3 py-1 bg-green-50 text-green-600 rounded-full text-sm font-medium">
                      <i class="fas fa-eye mr-2"></i>
                      查看详情
                    </div>
                  </div>
                </div>
                
                <!-- 右侧装饰 -->
                <div class="ml-6 flex-shrink-0 flex flex-col items-center">
                  <div class="w-12 h-12 bg-gradient-to-br from-blue-500 to-blue-400 rounded-full flex items-center justify-center text-white group-hover:scale-110 transition-transform duration-200">
                    <i class="fas fa-chevron-right text-lg"></i>
                  </div>
                  <div class="mt-2 text-xs text-gray-400 font-medium">
                    #{{ String(index + 1).padStart(2, '0') }}
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 底部装饰线 -->
            <div class="h-1 bg-gradient-to-r from-blue-500 via-blue-400 to-blue-300 rounded-b-2xl opacity-0 group-hover:opacity-100 transition-opacity duration-200"></div>
          </div>
        </div>

        <!-- 空状态 - 美化设计 -->
        <div v-else-if="!loading && (!announcements || announcements.length === 0)" class="text-center py-20">
          <div class="max-w-md mx-auto">
            <!-- 空状态插图 -->
            <div class="relative mb-8">
              <div class="w-32 h-32 mx-auto bg-gradient-to-br from-gray-100 to-gray-200 rounded-full flex items-center justify-center">
                <i class="fas fa-bullhorn text-4xl text-gray-400"></i>
              </div>
              <div class="absolute -top-2 -right-2 w-8 h-8 bg-yellow-400 rounded-full flex items-center justify-center animate-bounce">
                <i class="fas fa-search text-sm text-white"></i>
              </div>
            </div>
            
            <h3 class="text-2xl font-bold text-gray-900 mb-4">
              {{ searchParams.keyword || searchParams.categoryId ? '未找到相关公告' : '暂无公告发布' }}
            </h3>
            <p class="text-gray-500 text-lg leading-relaxed mb-8">
              {{ searchParams.keyword || searchParams.categoryId 
                ? '尝试调整搜索条件或清除筛选器' 
                : '系统暂时没有发布任何公告，请稍后再来查看' 
              }}
            </p>
            
            <!-- 操作按钮 -->
            <div class="flex flex-col sm:flex-row gap-4 justify-center">
              <el-button 
                v-if="searchParams.keyword || searchParams.categoryId"
                type="primary" 
                size="large"
                @click="clearFilters"
                class="px-8"
              >
                <i class="fas fa-refresh mr-2"></i>
                清除筛选
              </el-button>
              <el-button 
                size="large"
                @click="$router.push('/')"
                class="px-8"
              >
                <i class="fas fa-home mr-2"></i>
                返回首页
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 - 美化样式 -->
      <div v-if="total > 0 && announcements && announcements.length > 0" class="mt-12">
        <div class="bg-white rounded-2xl shadow-lg border border-gray-100 p-6">
          <div class="flex flex-col sm:flex-row items-center justify-between gap-4">
            <div class="text-sm text-gray-600">
              共 <span class="font-semibold text-blue-600">{{ total }}</span> 条公告
            </div>
            <el-pagination
              v-model:current-page="searchParams.page"
              v-model:page-size="searchParams.size"
              :total="total"
              :page-sizes="[10, 20, 50]"
              layout="sizes, prev, pager, next, jumper"
              @size-change="(size: number) => { searchParams.size = size; searchParams.page = 1; getAnnouncementList(); }"
              @current-change="(page: number) => { searchParams.page = page; getAnnouncementList(); }"
              class="pagination-custom"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import Navbar from '@/components/Navbar.vue'
import { publicApi } from '@/api'
import type { Announcement, AnnouncementCategory, AnnouncementSearchParams } from '@/api'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const announcements = ref<Announcement[]>([])
const categories = ref<AnnouncementCategory[]>([])
const total = ref(0)

// 搜索参数
const searchParams = reactive<AnnouncementSearchParams>({
  page: 1,
  size: 10,
  keyword: '',
  categoryId: undefined
})

// 获取公告列表
const getAnnouncementList = async () => {
  try {
    loading.value = true
    const response = await publicApi.getAnnouncementList(searchParams)
    // 根据实际API返回结构调整
    announcements.value = (response as any).data?.list || response.list || (response as any).records || []
    total.value = (response as any).data?.total || response.total || 0
  } catch (error) {
    console.error('获取公告列表失败:', error)
    ElMessage.error('获取公告列表失败')
    announcements.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 获取公告分类
const getCategories = async () => {
  try {
    const response = await publicApi.getAllAnnouncementCategories()
    // 根据实际API返回结构调整
    categories.value = (response as any).data?.list || (response as any).list || response || []
  } catch (error) {
    console.error('获取公告分类失败:', error)
    categories.value = []
  }
}

// 搜索处理
const handleSearch = () => {
  searchParams.page = 1
  getAnnouncementList()
}

// 快速筛选
const quickFilter = (categoryId: number) => {
  searchParams.categoryId = categoryId
  searchParams.keyword = ''
  handleSearch()
}

// 清除筛选
const clearFilters = () => {
  searchParams.categoryId = undefined
  searchParams.keyword = ''
  handleSearch()
}

// 跳转到详情页
const goToDetail = (id: number) => {
  router.push({ name: 'AnnouncementDetail', params: { id } })
}

// 获取内容预览
const getContentPreview = (content: string) => {
  // 移除HTML标签并截取前100个字符
  const plainText = content.replace(/<[^>]*>/g, '')
  return plainText.length > 100 ? plainText.substring(0, 100) + '...' : plainText
}

// 获取分类名称
const getCategoryName = (categoryId: number) => {
  const category = categories.value.find(c => c.id === categoryId)
  return category?.name || ''
}

// 格式化日期
const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// 组件挂载时获取数据
onMounted(() => {
  getCategories()
  getAnnouncementList()
})
</script>

<style scoped>
/* 文本截断 */
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 搜索输入框样式 */
.search-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  border: 2px solid #e5e7eb;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.search-input :deep(.el-input__wrapper:hover) {
  border-color: #3b82f6;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
}

.search-input :deep(.el-input__wrapper.is-focus) {
  border-color: #2563eb;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.2);
}

/* 分类选择器样式 */
.category-select :deep(.el-select__wrapper) {
  border-radius: 12px;
  border: 2px solid #e5e7eb;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.category-select :deep(.el-select__wrapper:hover) {
  border-color: #3b82f6;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
}

.category-select :deep(.el-select__wrapper.is-focused) {
  border-color: #2563eb;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.2);
}

/* 搜索按钮样式 */
.search-btn {
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  border: none;
  border-radius: 12px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.4);
}

/* 分页样式 */
.pagination-custom :deep(.el-pagination) {
  justify-content: center;
}

.pagination-custom :deep(.el-pager li) {
  border-radius: 8px;
  margin: 0 2px;
  transition: all 0.3s ease;
}

.pagination-custom :deep(.el-pager li:hover) {
  background: linear-gradient(135deg, #2563eb, #3b82f6);
  color: white;
}

.pagination-custom :deep(.el-pager li.is-active) {
  background: linear-gradient(135deg, #2563eb, #3b82f6);
  color: white;
}

/* 动画效果 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.announcement-list-page {
  animation: fadeInUp 0.6s ease-out;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-input,
  .category-select {
    margin-bottom: 1rem;
  }
  
  .search-btn {
    width: 100%;
  }
}
</style>