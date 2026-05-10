<template>
  <div class="search-page">
    <!-- 导航栏 -->
    <Navbar />

    <!-- 搜索头部区域 -->
    <section class="search-header bg-white py-6 shadow-sm">
      <div class="container mx-auto px-4">
        <div class="max-w-4xl mx-auto">
          <!-- 搜索框 -->
          <div class="search-input-wrapper mb-4 flex items-center gap-3">
            <el-input
              v-model="searchTitle"
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
              :loading="loading.search"
              class="search-button"
            >
              搜索
            </el-button>
          </div>
        </div>
      </div>
    </section>

    <!-- 搜索结果区域 -->
    <section class="search-results py-8">
      <div class="container mx-auto px-4">
        <div class="max-w-6xl mx-auto">
          <!-- 搜索统计 -->
          <div class="search-meta mb-6">
            <div class="search-stats">
              <span class="text-gray-600">
                找到 <span class="font-semibold text-primary-600">{{ totalResults }}</span> 个相关结果
                <span v-if="currentTitle" class="ml-2">
                  关键词: "<span class="font-semibold">{{ currentTitle }}</span>"
                </span>
              </span>
            </div>
          </div>

          <!-- 搜索结果内容 -->
          <div v-loading="loading.results" class="search-content">
            <!-- 无结果状态 -->
            <div v-if="!loading.results && searchResults.length === 0 && hasSearched" class="no-results text-center py-16">
              <el-icon :size="80" class="text-gray-300 mb-4"><DocumentRemove /></el-icon>
              <h3 class="text-xl font-semibold text-gray-600 mb-2">未找到相关结果</h3>
              <p class="text-gray-500 mb-6">
                尝试使用其他关键词
              </p>
              <el-button type="primary" @click="$router.push('/courses')">
                浏览所有课程
              </el-button>
            </div>

            <!-- 搜索结果列表 -->
            <div v-else-if="searchResults.length > 0" class="results-list">
              <!-- 课程结果 -->
              <div class="course-results">
                <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                  <div
                    v-for="course in searchResults"
                    :key="course.id"
                    class="course-card bg-white rounded-lg shadow-md hover:shadow-lg transition-all duration-300 overflow-hidden cursor-pointer"
                    @click="handleCourseClick(course)"
                  >
                    <div class="course-image relative">
                      <img 
                        :src="course.cover" 
                        :alt="course.title" 
                        class="w-full h-48 object-cover"
                        @error="handleImageError"
                      />
                      <!-- <div class="course-badge" v-if="course.status === 1">
                        <span class="bg-red-500 text-white px-2 py-1 text-xs rounded">热门</span>
                      </div> -->
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

              <!-- 分页 -->
              <div class="pagination-wrapper flex justify-center mt-8">
                <el-pagination
                  v-model:current-page="pagination.currentPage"
                  v-model:page-size="pagination.pageSize"
                  :page-sizes="[12, 24, 36, 48]"
                  :total="totalResults"
                  layout="total, sizes, prev, pager, next, jumper"
                  @size-change="handleSizeChange"
                  @current-change="handleCurrentChange"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Search, 
  DocumentRemove 
} from '@element-plus/icons-vue'

import Navbar from '@/components/Navbar.vue'

// API 导入
import { publicApi } from '@/api'
import type { Course } from '@/api/types/course'
import type { PageResult } from '@/api/types/common'

const router = useRouter()
const route = useRoute()

// 响应式数据
const searchTitle = ref('')
const currentTitle = ref('')
const hasSearched = ref(false)

// 加载状态
const loading = ref({
  search: false,
  results: false
})

// 搜索结果
const searchResults = ref<Course[]>([])
const totalResults = ref(0)

// 分页
const pagination = ref({
  currentPage: 1,
  pageSize: 12
})

// 执行搜索
const performSearch = async (title: string, page = 1) => {
  if (!title.trim()) return

  loading.value.results = true
  hasSearched.value = true
  currentTitle.value = title

  try {
    // 调用真实的搜索API
    const data = await publicApi.searchCourses({
      title,
      page,
      size: pagination.value.pageSize
    })
    
    searchResults.value = data.list || []
    totalResults.value = data.total || 0
    
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败，请稍后重试')
    searchResults.value = []
    totalResults.value = 0
  } finally {
    loading.value.results = false
  }
}

// 事件处理函数
const handleSearch = () => {
  if (!searchTitle.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  pagination.value.currentPage = 1
  
  // 更新URL
  router.push({
    path: '/search',
    query: { 
      q: searchTitle.value.trim()
    }
  })
  
  performSearch(searchTitle.value.trim())
}

const handleCourseClick = (course: Course) => {
  router.push(`/courses/${course.id}`)
}

const handleCurrentChange = (page: number) => {
  pagination.value.currentPage = page
  if (currentTitle.value) {
    performSearch(currentTitle.value, page)
  }
  
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleSizeChange = (size: number) => {
  pagination.value.pageSize = size
  pagination.value.currentPage = 1
  if (currentTitle.value) {
    performSearch(currentTitle.value)
  }
}

const handleImageError = (event: Event) => {
  const target = event.target as HTMLImageElement
  target.src = '/src/assets/logo.jpeg' // 默认图片
}

// 监听路由变化
watch(() => route.query, (newQuery) => {
  const title = newQuery.q as string
  
  if (title) {
    searchTitle.value = title
    performSearch(title)
  }
}, { immediate: true })

// 生命周期
onMounted(() => {
  // 初始化页面
})
</script>

<style scoped lang="scss">
.search-page {
  min-height: 100vh;
  background-color: #f8fafc;
  min-width: 100vw;
  margin: 0 auto;
  padding: 0 20px;
}

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
  
  &:hover {
    box-shadow: 0 4px 12px rgba(33, 150, 243, 0.4);
    transform: translateY(-1px);
  }
  
  &:active {
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
    overflow: hidden;
    
    .course-badge {
      position: absolute;
      top: 12px;
      right: 12px;
      z-index: 2;
    }
  }
}

.no-results {
  .el-icon {
    margin-bottom: 16px;
  }
}

.search-meta {
  border-bottom: 1px solid #e5e7eb;
  padding-bottom: 16px;
}

.pagination-wrapper {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #e5e7eb;
}

// 文本截断样式
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

// 响应式调整
@media (max-width: 768px) {
  .course-card {
    .course-content {
      padding: 12px;
    }
  }
}
</style>