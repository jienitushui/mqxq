<template>
  <Navbar />
  <div class="my-courses">
    
    <div class="page-header">
      <h1>我的课程</h1>
      <p>管理和查看您已购买的课程</p>
    </div>

    <!-- 筛选器 -->
    <div class="filters">
      <el-radio-group v-model="filters.status" @change="handleStatusChange">
        <el-radio-button :value="undefined">全部</el-radio-button>
        <el-radio-button :value="0">已加入</el-radio-button>
        <el-radio-button :value="1">学习中</el-radio-button>
        <el-radio-button :value="2">已完成</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 课程列表 -->
    <div class="course-list" v-loading="loading">
      <div v-if="courseList.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无课程数据">
          <el-button type="primary" @click="$router.push('/courses')">
            去选课
          </el-button>
        </el-empty>
      </div>

      <div v-else class="course-grid">
        <div 
          v-for="course in courseList" 
          :key="course.id"
          class="course-card"
        >
          <div class="course-image">
            <img 
              :src="getCourseImage(course.courseId)" 
              :alt="`课程${course.courseId}`"
              @error="handleImageError"
            />
            <div class="status-badge">
              <el-tag 
                :type="CourseStatusColors[course.status]"
                size="small"
              >
                {{ CourseStatusLabels[course.status] }}
              </el-tag>
            </div>
          </div>

          <div class="course-info">
            <h3 class="course-title">课程名称：{{ course.course?.title || `课程 ${course.courseId}` }}</h3>
            
            <div class="course-meta">
              <div class="meta-item">
                <el-icon><Calendar /></el-icon>
                <span>开始时间：{{ formatDate(course.createTime) }}</span>
              </div>
              <div class="meta-item">
                <el-icon><Clock /></el-icon>
                <span>更新时间：{{ formatDate(course.updateTime) }}</span>
              </div>
              <div class="meta-item">
                <el-icon><Document /></el-icon>
                <span>订单号：{{ course.orderId }}</span>
              </div>
            </div>

            <div class="course-actions">
              <el-button 
                type="primary" 
                size="small"
                @click="enterCourse(course)"
              >
                {{ course.status === 2 ? '回顾课程' : '进入学习' }}
              </el-button>
              <el-button 
                size="small" 
                @click="viewCourseDetail(course)"
              >
                课程详情
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Calendar, Clock, Document } from '@element-plus/icons-vue'
import Navbar from '../../components/Navbar.vue'
import { userApi } from '../../api/user'
import { publicApi } from '../../api/public'
import type { MyCourse } from '../../api/types/course'
import { CourseStatusLabels, CourseStatusColors } from '../../api/types/course'
import type { PageParams } from '../../api/types/common'

// 路由
const router = useRouter()

// 响应式数据
const loading = ref(false)
const courseList = ref<MyCourse[]>([])
const total = ref(0)

// 筛选条件
const filters = reactive({
  status: undefined as number | undefined
})

// 分页参数
const pagination = reactive({
  pageNum: 1,
  pageSize: 10
})

// 获取课程详情
const fetchCourseDetails = async (courseIds: number[]) => {
  try {
    const courseDetails = await Promise.all(
      courseIds.map(async (courseId) => {
        try {
          return await publicApi.getCourseDetail(courseId)
        } catch (error) {
          console.warn(`获取课程${courseId}详情失败:`, error)
          return null
        }
      })
    )
    return courseDetails.filter(course => course !== null)
  } catch (error) {
    console.error('批量获取课程详情失败:', error)
    return []
  }
}

// 获取课程列表
const fetchCourseList = async () => {
  try {
    loading.value = true
    const params: PageParams & { status?: number } = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...filters
    }
    
    const response = await userApi.getMyCourseList(params)
    courseList.value = response.list
    total.value = response.total

    // 获取课程详情
    if (response.list.length > 0) {
      const courseIds = response.list.map(course => course.courseId)
      const courseDetails = await fetchCourseDetails(courseIds)
      
      // 将课程详情关联到我的课程数据
      courseList.value = response.list.map(myCourse => {
        const courseDetail = courseDetails.find(detail => detail?.id === myCourse.courseId)
        return {
          ...myCourse,
          course: courseDetail
        }
      })
    }
  } catch (error) {
    console.error('获取课程列表失败:', error)
    ElMessage.error('获取课程列表失败')
  } finally {
    loading.value = false
  }
}

// 状态筛选变化
const handleStatusChange = () => {
  pagination.pageNum = 1
  fetchCourseList()
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  fetchCourseList()
}

// 当前页变化
const handleCurrentChange = (page: number) => {
  pagination.pageNum = page
  fetchCourseList()
}

// 进入课程学习
const enterCourse = (course: MyCourse) => {
  // 使用Vue Router跳转到课程学习页面
  router.push(`/courses/${course.courseId}/learn`)
}

// 查看课程详情
const viewCourseDetail = (course: MyCourse) => {
  // 跳转到课程详情页面，并传递来源信息以便返回
  router.push({
    name: 'CourseDetail',
    params: { id: course.courseId },
    query: { from: 'my-courses' }
  })
}

// 获取课程图片
const getCourseImage = (courseId: number) => {
  const course = courseList.value.find(c => c.courseId === courseId)
  return course?.course?.cover || `https://picsum.photos/300/200?random=${courseId}`
}

// 图片加载失败处理
const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.src = '/src/assets/default-course.svg' // 默认课程图片
}

// 格式化日期
const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 组件挂载时获取数据
onMounted(() => {
  fetchCourseList()
})
</script>

<style scoped lang="scss">

.my-courses {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;

  .page-header {
    margin-top: 30px;
    margin-bottom: 32px;
    text-align: center;

    h1 {
      font-size: 32px;
      font-weight: 600;
      color: #1f2937;
      margin-bottom: 8px;
    }

    p {
      font-size: 16px;
      color: #6b7280;
    }
  }

  .filters {
    margin-bottom: 24px;
    display: flex;
    justify-content: center;

    .el-radio-group {
      background: #f9fafb;
      border-radius: 8px;
      padding: 4px;
    }
  }

  .course-list {
    min-height: 400px;

    .empty-state {
      display: flex;
      justify-content: center;
      align-items: center;
      height: 400px;
    }

    .course-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
      gap: 24px;
    }

    .course-card {
      background: white;
      border-radius: 12px;
      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
      overflow: hidden;
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 10px 25px -3px rgba(0, 0, 0, 0.1);
      }

      .course-image {
        position: relative;
        height: 200px;
        overflow: hidden;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          transition: transform 0.3s ease;
        }

        &:hover img {
          transform: scale(1.05);
        }

        .status-badge {
          position: absolute;
          top: 12px;
          right: 12px;
        }
      }

      .course-info {
        padding: 20px;

        .course-title {
          font-size: 18px;
          font-weight: 600;
          color: #1f2937;
          margin-bottom: 16px;
          line-height: 1.4;
        }

        .course-meta {
          margin-bottom: 20px;

          .meta-item {
            display: flex;
            align-items: center;
            margin-bottom: 8px;
            font-size: 14px;
            color: #6b7280;

            .el-icon {
              margin-right: 8px;
              color: #9ca3af;
            }

            &:last-child {
              margin-bottom: 0;
            }
          }
        }

        .course-actions {
          display: flex;
          gap: 12px;

          .el-button {
            flex: 1;
          }
        }
      }
    }
  }

  .pagination {
    margin-top: 32px;
    display: flex;
    justify-content: center;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .my-courses {
    padding: 16px;

    .course-list .course-grid {
      grid-template-columns: 1fr;
      gap: 16px;
    }

    .filters {
      .el-radio-group {
        width: 100%;
        display: flex;
        flex-wrap: wrap;
        justify-content: center;
      }
    }
  }
}
</style>