<template>
  <div class="view-history">
    <Navbar />
    
    <div class="container">
      <div class="page-header">
        <h1 class="page-title">浏览记录</h1>
        <div class="header-actions">
          <el-button 
            type="danger" 
            :icon="Delete" 
            @click="handleClearAll"
            :disabled="loading || total === 0"
          >
            清空记录
          </el-button>
        </div>
      </div>



      <!-- 批量操作 -->
      <div class="batch-actions" v-if="selectedIds.length > 0">
        <el-alert
          :title="`已选择 ${selectedIds.length} 条记录`"
          type="info"
          show-icon
          :closable="false"
        >
          <template #default>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleBatchDelete"
            >
              批量删除
            </el-button>
          </template>
        </el-alert>
      </div>

      <!-- 浏览记录列表 -->
      <div class="history-list">
        <el-table
          v-loading="loading"
          :data="historyList"
          @selection-change="handleSelectionChange"
          empty-text="暂无浏览记录"
        >
          <el-table-column type="selection" width="55" />
          
          <el-table-column label="课程信息" min-width="280">
            <template #default="{ row }">
              <div class="course-info">
                <img 
                  :src="row.courseCover || '/default-course.svg'" 
                  :alt="row.courseTitle"
                  class="course-cover"
                  @error="handleImageError"
                />
                <div class="course-details">
                  <h4 class="course-title">{{ row.courseTitle }}</h4>
                  <p class="course-desc">{{ truncateText(row.courseDescription, 50) }}</p>
                  <div class="course-meta">
                    <span class="meta-item">
                      <i class="icon">💰</i>
                      {{ row.coursePrice === 0 ? '免费' : `¥${row.coursePrice}` }}
                    </span>
                    <span class="meta-item">
                      <i class="icon">👁️</i>
                      {{ row.courseViewCount }}次观看
                    </span>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="浏览时间" width="140" align="center">
            <template #default="{ row }">
              <div class="view-time">
                <div class="date">{{ formatDate(row.viewTime) }}</div>
                <div class="time">{{ formatTime(row.viewTime) }}</div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="访问信息" width="160">
            <template #default="{ row }">
              <div class="access-info">
                <div class="ip-info" v-if="row.ipAddress">
                  <i class="icon">🌐</i>
                  <span>{{ row.ipAddress }}</span>
                </div>
                <div class="browser-info" v-if="row.userAgent">
                  <i class="icon">💻</i>
                  <span>{{ getBrowserName(row.userAgent) }}</span>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="220" fixed="right" align="center">
            <template #default="{ row }">
              <el-button 
                type="primary" 
                size="small" 
                @click="goToCourse(row.courseId)"
              >
                查看课程
              </el-button>
              <el-button 
                type="info" 
                size="small" 
                @click="viewDetail(row)"
              >
                详情
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                @click="handleDelete(row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && total === 0" class="empty-state">
        <h3>暂无浏览记录</h3>
        <p>开始浏览课程，记录将会显示在这里</p>
        <el-button type="primary" @click="goToCourseList">
          去浏览课程
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import Navbar from '../../components/Navbar.vue'
import { userApi } from '../../api/user'
import { getCourseDetail } from '../../utils/courseApi'
import type { CourseView } from '../../api/types/course'
import type { PageParams } from '../../api/types/common'

// 路由
const router = useRouter()

// 响应式数据
const loading = ref(false)
const historyList = ref<CourseView[]>([])
const total = ref(0)
const selectedIds = ref<number[]>([])

// 分页参数
const pagination = reactive({
  pageNum: 1,
  pageSize: 20
})

// 获取浏览记录列表
const fetchHistoryList = async () => {
  try {
    loading.value = true
    const params: PageParams = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }

    const response = await userApi.getCourseViewPage(params)
    
    // 获取浏览记录
    const viewRecords = response.list || []
    total.value = response.total || 0
    
    // 如果有浏览记录，批量获取课程信息
    if (viewRecords.length > 0) {
      await enrichWithCourseInfo(viewRecords)
    } else {
      historyList.value = []
    }
  } catch (error) {
    console.error('获取浏览记录失败:', error)
    ElMessage.error('获取浏览记录失败')
    historyList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 为浏览记录补充课程信息
const enrichWithCourseInfo = async (viewRecords: CourseView[]) => {
  try {
    // 获取所有唯一的课程ID
    const courseIds = [...new Set(viewRecords.map(record => record.courseId))]
    
    // 批量获取课程信息
    const coursePromises = courseIds.map(async (courseId) => {
      try {
        const course = await getCourseDetail(courseId)
        return { courseId, course }
      } catch (error) {
        console.warn(`获取课程${courseId}信息失败:`, error)
        return { courseId, course: null }
      }
    })
    
    const courseResults = await Promise.all(coursePromises)
    const courseMap = new Map(courseResults.map(result => [result.courseId, result.course]))
    
    // 将课程信息关联到浏览记录
    historyList.value = viewRecords.map(record => {
      const course = courseMap.get(record.courseId)
      return {
        ...record,
        course: course || undefined,
        // 扁平化课程信息以便显示
        courseTitle: course?.title || '未知课程',
        courseDescription: course?.description || '',
        courseCover: course?.cover || '',
        coursePrice: course?.price ?? 0,
        courseViewCount: course?.viewCount || 0
      }
    })
  } catch (error) {
    console.error('获取课程信息失败:', error)
    // 如果获取课程信息失败，至少显示浏览记录
    historyList.value = viewRecords.map(record => ({
      ...record,
      courseTitle: `课程ID: ${record.courseId}`,
      courseDescription: '课程信息获取失败',
      courseCover: '',
      coursePrice: 0,
      courseViewCount: 0
    }))
  }
}


// 选择变化
const handleSelectionChange = (selection: CourseView[]) => {
  selectedIds.value = selection.map(item => item.id)
  console.log('选中的记录:', selection)
  console.log('选中的ID列表:', selectedIds.value)
}

// 删除单条记录
const handleDelete = async (row: CourseView) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除课程"${row.courseTitle}"的浏览记录吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await userApi.deleteCourseView(row.id)
    ElMessage.success('删除成功')
    
    // 如果当前页没有数据了，回到上一页
    if (historyList.value.length === 1 && pagination.pageNum > 1) {
      pagination.pageNum--
    }
    
    await fetchHistoryList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除浏览记录失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    if (selectedIds.value.length === 0) {
      ElMessage.warning('请先选择要删除的记录')
      return
    }

    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedIds.value.length} 条浏览记录吗？`,
      '确认批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    console.log('批量删除的ID列表:', selectedIds.value)
    
    await userApi.batchDeleteCourseViews(selectedIds.value)
    ElMessage.success('批量删除成功')
    
    selectedIds.value = []
    await fetchHistoryList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败，请重试')
    }
  }
}

// 清空所有记录
const handleClearAll = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有浏览记录吗？此操作不可恢复！',
      '确认清空',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await userApi.clearCourseViews()
    ElMessage.success('清空成功')
    
    selectedIds.value = []
    pagination.pageNum = 1
    await fetchHistoryList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空浏览记录失败:', error)
      ElMessage.error('清空失败')
    }
  }
}

// 分页处理
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  fetchHistoryList()
}

const handleCurrentChange = (page: number) => {
  pagination.pageNum = page
  fetchHistoryList()
}

// 跳转到课程详情
const goToCourse = (courseId: number) => {
  router.push({ name: 'CourseDetail', params: { id: courseId } })
}

// 查看浏览记录详情
const viewDetail = (row: CourseView) => {
  router.push({ name: 'ViewHistoryDetail', params: { id: row.id } })
}

// 跳转到课程列表
const goToCourseList = () => {
  router.push({ name: 'CourseList' })
}

// 工具函数
const truncateText = (text: string, maxLength: number) => {
  if (!text) return ''
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

const formatDate = (dateTime: string) => {
  return new Date(dateTime).toLocaleDateString('zh-CN')
}

const formatTime = (dateTime: string) => {
  return new Date(dateTime).toLocaleTimeString('zh-CN', { 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.src = '/default-course.svg'
}

// 从 User Agent 中提取浏览器名称
const getBrowserName = (userAgent: string) => {
  if (!userAgent) return '未知浏览器'
  
  if (userAgent.includes('Edg/')) return 'Edge'
  if (userAgent.includes('Chrome/')) return 'Chrome'
  if (userAgent.includes('Firefox/')) return 'Firefox'
  if (userAgent.includes('Safari/') && !userAgent.includes('Chrome/')) return 'Safari'
  if (userAgent.includes('Opera/')) return 'Opera'
  
  return '其他浏览器'
}

// 页面加载时获取数据
onMounted(() => {
  fetchHistoryList()
})
</script>

<style scoped>
.view-history {
  min-height: 100vh;
  background: #f8f9fa;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}

.header-actions {
  display: flex;
  gap: 12px;
}



.batch-actions {
  margin-bottom: 20px;
}

.history-list {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.course-info {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.course-cover {
  width: 80px;
  height: 60px;
  object-fit: cover;
  border-radius: 6px;
  flex-shrink: 0;
}

.course-details {
  flex: 1;
  min-width: 0;
}

.course-title {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.4;
}

.course-desc {
  margin: 0 0 8px 0;
  font-size: 12px;
  color: #6b7280;
  line-height: 1.4;
}

.course-meta {
  display: flex;
  gap: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #6b7280;
}

.meta-item .icon {
  font-size: 12px;
}

.view-time {
  text-align: center;
}

.view-time .date {
  font-size: 14px;
  color: #1f2937;
  margin-bottom: 2px;
  font-weight: 500;
}

.view-time .time {
  font-size: 12px;
  color: #6b7280;
}

.access-info {
  font-size: 12px;
}

.access-info > div {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 6px;
  color: #6b7280;
}

.access-info > div:last-child {
  margin-bottom: 0;
}

.access-info .icon {
  font-size: 12px;
  width: 14px;
  text-align: center;
}

.ip-info span {
  font-family: monospace;
  font-size: 11px;
  background: #f3f4f6;
  padding: 1px 4px;
  border-radius: 3px;
}

.browser-info span {
  font-weight: 500;
  font-size: 11px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-state h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: #1f2937;
}

.empty-state p {
  margin: 0 0 24px 0;
  color: #6b7280;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .container {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: center;
  }
  
  .course-info {
    flex-direction: column;
    gap: 8px;
  }
  
  .course-cover {
    width: 100%;
    height: 80px;
  }
  
  .course-meta {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .view-time .date {
    font-size: 12px;
  }
  
  .view-time .time {
    font-size: 11px;
  }
  
  .access-info {
    font-size: 11px;
  }
  
  /* 移动端隐藏部分列 */
  .history-list :deep(.el-table__header-wrapper) .el-table__header th:nth-child(4) {
    display: none;
  }
  
  .history-list :deep(.el-table__body-wrapper) .el-table__body td:nth-child(4) {
    display: none;
  }
}
</style>