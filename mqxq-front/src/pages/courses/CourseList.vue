<template>
  <div class="course-list-page">
    <!-- 导航栏 -->
    <Navbar />
    
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="container">
        <h1 class="page-title">课程中心</h1>
        <p class="page-subtitle">发现优质课程，开启学习之旅</p>
      </div>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="search-filter-section">
      <div class="container">
        <div class="search-bar">
          <input
            v-model="searchParams.title"
            type="text"
            placeholder="搜索课程名称..."
            class="search-input"
          />
          <button 
            v-if="searchParams.title" 
            class="clear-btn" 
            @click="clearSearch"
            title="清空搜索"
          >
            ✕
          </button>
          <button class="search-btn" @click="handleSearch">
            <i class="icon-search">搜索</i>
          </button>
        </div>

        <!-- 分类筛选 -->
        <div class="filter-section">
          <div class="filter-item">
            <label>课程分类：</label>
            <select v-model="searchParams.subjectId" class="category-select">
              <option value="">全部分类</option>
              <template v-for="category in categoryTree" :key="category.id">
                <!-- 一级分类选项（禁用，仅作为分组标题） -->
                <option :value="category.id" disabled class="parent-option">{{ category.name }}</option>
                <!-- 二级分类选项（可选择） -->
                <option 
                  v-for="child in category.children" 
                  :key="child.id" 
                  :value="child.id"
                  class="child-option">
                  &nbsp;&nbsp;&nbsp;&nbsp;{{ child.name }}
                </option>
              </template>
            </select>
          </div>

          <div class="filter-item">
            <label>价格筛选：</label>
            <select v-model="priceFilter">
              <option value="">全部价格</option>
              <option value="free">免费课程</option>
            </select>
          </div>

          <div class="filter-item">
            <label>排序方式：</label>
            <select v-model="sortBy">
              <option value="createTime">最新发布</option>
              <option value="buyCount">购买人数</option>
              <option value="viewCount">游览次数</option>
              <option value="price">价格排序</option>
            </select>
          </div>
        </div>
      </div>
    </div>

    <!-- 课程内容区域 -->
    <div class="courses-section">
      <div class="container">
        <div class="content-layout">
          <!-- 主内容区 -->
          <div class="main-content">
            <div class="section-header">
              <h2 class="section-title">
                {{ currentSearchTitle ? `"${currentSearchTitle}" 的搜索结果` : '全部课程' }}
              </h2>
              <div class="course-count">
                共找到 {{ pagination.total }} 门课程
                <span v-if="pagination.total > pagination.size" class="page-info">
                  (第 {{ pagination.current }} 页，共 {{ Math.ceil(pagination.total / pagination.size) }} 页)
                </span>
              </div>
            </div>

            <!-- 加载状态 -->
            <div v-if="loading" class="loading-state">
              <div class="loading-spinner"></div>
              <p>正在加载课程...</p>
            </div>

            <!-- 空状态 -->
            <div v-else-if="courses.length === 0" class="empty-state">
              <h3>暂无课程</h3>
              <p>{{ currentSearchTitle ? '没有找到相关课程，试试其他关键词吧' : '暂时没有课程，敬请期待' }}</p>
            </div>

            <!-- 课程网格 -->
            <div v-else class="courses-grid">
              <CourseCard
                v-for="course in courses"
                :key="course.id"
                :course="course"
                @click="goToCourseDetail(course.id)"
              />
            </div>

            <!-- 分页组件 -->
            <div v-if="!loading && (courses.length > 0 || pagination.total > 0)" class="pagination-wrapper">
              <Pagination
                :current="pagination.current"
                :total="pagination.total"
                :page-size="pagination.size"
                @change="handlePageChange"
              />
            </div>
          </div>

          <!-- 侧边栏 -->
          <aside v-if="!currentSearchTitle && hotCourses.length > 0" class="sidebar">
            <div class="sidebar-sticky">
              <div class="hot-courses-card">
                <div class="card-header">
                  <span class="fire-icon">🔥</span>
                  <h3 class="card-title">热门推荐</h3>
                </div>
                <div class="hot-courses-list">
                  <div
                    v-for="(course, index) in hotCourses.slice(0, 5)"
                    :key="course.id"
                    class="hot-course-item"
                    @click="goToCourseDetail(course.id)"
                  >
                    <div class="item-rank">{{ index + 1 }}</div>
                    <div class="item-cover">
                      <img :src="course.cover" :alt="course.title" />
                    </div>
                    <div class="item-info">
                      <h4 class="item-title">{{ course.title }}</h4>
                      <div class="item-meta">
                        <span class="item-price">¥{{ course.price }}</span>
                        <span class="item-count">{{ course.buyCount }}人学习</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </aside>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { publicApi } from '@/api'
import CourseCard from '@/components/CourseCard.vue'
import Pagination from '@/components/Pagination.vue'
import Navbar from '@/components/Navbar.vue'

const router = useRouter()
const route = useRoute()

// 响应式数据
const loading = ref(false)
const courses = ref([])
const hotCourses = ref([])
const subjects = ref([])
const categoryTree = ref([])

// 当前搜索的关键词（用于显示）
const currentSearchTitle = ref('')

// 搜索参数
const searchParams = reactive({
  pageNum: 1,
  pageSize: 12,
  title: '',
  subjectId: ''
})

// 筛选参数
const priceFilter = ref('')
const sortBy = ref('createTime')

// 分页信息
const pagination = reactive({
  current: 1,
  total: 0,
  size: 12
})

// 获取课程列表
const fetchCourses = async () => {
  try {
    loading.value = true
    
    // 构建请求参数
    const params = {
      pageNum: searchParams.pageNum,
      pageSize: searchParams.pageSize,
      title: searchParams.title || undefined,
      subjectId: searchParams.subjectId || undefined
    }
    
    // 添加排序参数
    if (sortBy.value && sortBy.value !== 'createTime') {
      params.orderBy = sortBy.value
    }
    
    // 移除 undefined 值
    Object.keys(params).forEach(key => {
      if (params[key] === undefined) {
        delete params[key]
      }
    })
    
    let apiCall
    
    // 根据不同条件调用不同API
    // 优先级：免费课程 > 分类/搜索 > 纯搜索
    if (priceFilter.value === 'free') {
      // 筛选免费课程（可以带搜索关键词和分类）
      apiCall = publicApi.getFreeCourses(params)
      console.log('使用免费课程接口:', params)
    } else if (params.subjectId) {
      // 有分类筛选（可以带搜索关键词）
      apiCall = publicApi.getCourseList(params)
      console.log('使用课程列表接口（分类筛选）:', params)
    } else if (params.title) {
      // 只有搜索关键词，没有分类
      apiCall = publicApi.searchCourses(params)
      console.log('使用搜索接口:', params)
    } else {
      // 默认使用课程列表接口
      apiCall = publicApi.getCourseList(params)
      console.log('使用课程列表接口（默认）:', params)
    }
    
    const response = await apiCall
    // 适配API返回的数据结构
    const data = response.data || response
    courses.value = data.list || data.records || []
    pagination.total = data.total || 0
    pagination.current = data.pageNum || data.current || 1
    
    console.log('API响应数据:', data)
    console.log('分页信息:', { total: pagination.total, current: pagination.current })
    
  } catch (error) {
    console.error('获取课程列表失败:', error)
    courses.value = []
  } finally {
    loading.value = false
  }
}

// 获取热门课程
const fetchHotCourses = async () => {
  try {
    const response = await publicApi.getHotCourses(6)
    hotCourses.value = response || []
  } catch (error) {
    console.error('获取热门课程失败:', error)
  }
}

// 获取课程分类
const fetchSubjects = async () => {
  try {
    const response = await publicApi.getSubjectTree()
    subjects.value = response || []
    // API返回的数据已经是树形结构，直接使用
    buildCategoryTree()
  } catch (error) {
    console.error('获取课程分类失败:', error)
  }
}

// 构建分类树形结构
const buildCategoryTree = () => {
  // API返回的数据已经是树形结构，只需要过滤出一级分类
  // 一级分类的children属性已经包含了二级分类
  const topLevelCategories = subjects.value.filter(item => item.parentId === 0)
  categoryTree.value = topLevelCategories
  
  console.log('分类树结构:', categoryTree.value)
}

// 选择分类（保留用于兼容性）
const selectCategory = (categoryId) => {
  searchParams.subjectId = categoryId
  handleSearch()
}

// 搜索处理
const handleSearch = () => {
  // 更新当前搜索关键词
  currentSearchTitle.value = searchParams.title
  searchParams.pageNum = 1
  pagination.current = 1
  fetchCourses()
}

// 清空搜索
const clearSearch = () => {
  searchParams.title = ''
  searchParams.subjectId = ''
  priceFilter.value = ''
  sortBy.value = 'createTime'
  currentSearchTitle.value = ''
  handleSearch()
}

// 分页处理
const handlePageChange = (page) => {
  searchParams.pageNum = page
  pagination.current = page
  fetchCourses()
  
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 跳转到课程详情
const goToCourseDetail = (courseId) => {
  router.push({ name: 'CourseDetail', params: { id: courseId } })
}

// 初始化页面参数
const initializeFromQuery = () => {
  if (route.query.title) {
    searchParams.title = route.query.title
    currentSearchTitle.value = route.query.title
  }
  if (route.query.subjectId) {
    searchParams.subjectId = Number(route.query.subjectId)
  }
  if (route.query.page) {
    searchParams.pageNum = Number(route.query.page)
    pagination.current = Number(route.query.page)
  }
}

// 页面挂载
onMounted(() => {
  initializeFromQuery()
  fetchCourses()
  fetchHotCourses()
  fetchSubjects()
})
</script>

<style scoped>
.course-list-page {
  min-height: 100vh;
  background: #f8f9fa;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 页面头部 */
.page-header {
  background: linear-gradient(135deg, #2196F3 0%, #ffffff 100%);
  color: #333;
  padding: 60px 0;
  text-align: center;
}

.page-title {
  font-size: 2.5rem;
  font-weight: bold;
  margin-bottom: 10px;
}

.page-subtitle {
  font-size: 1.1rem;
  opacity: 0.9;
}

/* 搜索筛选区域 */
.search-filter-section {
  background: white;
  padding: 30px 0;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.search-bar {
  display: flex;
  max-width: 600px;
  margin: 0 auto 30px;
  border-radius: 25px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  position: relative;
}

.search-input {
  flex: 1;
  padding: 15px 20px;
  padding-right: 50px;
  border: none;
  font-size: 16px;
  outline: none;
}

.clear-btn {
  position: absolute;
  right: 70px;
  top: 50%;
  transform: translateY(-50%);
  padding: 5px 10px;
  background: transparent;
  color: #999;
  border: none;
  cursor: pointer;
  font-size: 18px;
  transition: color 0.3s;
}

.clear-btn:hover {
  color: #333;
}

.search-btn {
  padding: 15px 20px;
  background: #2196F3;
  color: white;
  border: none;
  cursor: pointer;
  transition: background 0.3s;
}

.search-btn:hover {
  background: #1976D2;
}

.filter-section {
  display: flex;
  justify-content: center;
  gap: 40px;
  flex-wrap: wrap;
  align-items: flex-start;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-item label {
  font-weight: 500;
  color: #333;
}

.filter-item select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

/* 分类下拉框样式 */
.category-select {
  min-width: 200px;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  background: white;
  cursor: pointer;
}

.category-select:focus {
  outline: none;
  border-color: #2196F3;
  box-shadow: 0 0 0 2px rgba(33, 150, 243, 0.2);
}

.category-select optgroup {
  font-weight: bold;
  color: #333;
}

.category-select option {
  padding: 5px;
  font-weight: normal;
}

.category-select option.parent-option {
  font-weight: bold;
  color: #333;
  background: #f5f5f5;
}

.category-select option.child-option {
  padding-left: 20px;
  font-size: 13px;
  color: #666;
}

/* 课程内容区域 */
.courses-section {
  padding: 50px 0;
  background: #f8f9fa;
}

.content-layout {
  display: flex;
  gap: 30px;
  align-items: flex-start;
}

.main-content {
  flex: 1;
  min-width: 0;
}

.section-title {
  font-size: 1.8rem;
  font-weight: bold;
  margin-bottom: 10px;
  color: #333;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.course-count {
  color: #666;
  font-size: 14px;
}

.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

@media (min-width: 768px) {
  .courses-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (min-width: 1024px) {
  .courses-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

/* 侧边栏 */
.sidebar {
  width: 320px;
  flex-shrink: 0;
}

.sidebar-sticky {
  position: sticky;
  top: 20px;
}

.hot-courses-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 20px;
  background: linear-gradient(135deg, #2196F3 0%, #1976D2 100%);
  color: white;
}

.fire-icon {
  font-size: 24px;
  animation: fire-flicker 1.5s ease-in-out infinite;
}

@keyframes fire-flicker {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.card-title {
  font-size: 18px;
  font-weight: bold;
  margin: 0;
}

.hot-courses-list {
  padding: 16px;
}

.hot-course-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 12px;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  &:hover {
    background: #f8f9fa;
    transform: translateX(4px);
  }
}

.item-rank {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #2196F3 0%, #1976D2 100%);
  color: white;
  border-radius: 4px;
  font-weight: bold;
  font-size: 14px;
  flex-shrink: 0;
}

.item-cover {
  width: 80px;
  height: 60px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.item-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.item-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
}

.item-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}

.item-price {
  color: #f56c6c;
  font-weight: bold;
  font-size: 14px;
}

.item-count {
  color: #999;
}

/* 加载状态 */
.loading-state {
  text-align: center;
  padding: 60px 0;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #2196F3;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 0;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 20px;
}

.empty-state h3 {
  font-size: 1.5rem;
  color: #333;
  margin-bottom: 10px;
}

.empty-state p {
  color: #666;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .content-layout {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
    order: -1;
  }
  
  .sidebar-sticky {
    position: static;
  }
  
  .hot-courses-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 16px;
  }
}

@media (max-width: 768px) {
  .page-title {
    font-size: 2rem;
  }
  
  .search-bar {
    margin: 0 20px 30px;
  }
  
  .filter-section {
    flex-direction: column;
    align-items: center;
    gap: 15px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .courses-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 16px;
  }
}

@media (max-width: 480px) {
  .courses-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .hot-courses-list {
    grid-template-columns: 1fr;
  }
}
</style>