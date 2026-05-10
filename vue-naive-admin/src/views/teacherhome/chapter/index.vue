<template>
  <div class="teacher-chapter-management h-full flex flex-col">
    <PageHeader class="flex-shrink-0">
      <template #title>
        章节管理
        <span v-if="currentCourseName" class="text-sm text-gray-500 ml-2">
          - {{ currentCourseName }}
        </span>
      </template>
      <template #extra>
        <n-button type="primary" @click="handleAdd">
          <template #icon>
            <i class="i-carbon:add" />
          </template>
          新增章节
        </n-button>
      </template>
    </PageHeader>

    <n-card class="flex-1 flex flex-col overflow-hidden">
      <!-- 搜索区域 -->
      <div class="flex-shrink-0 mb-4">
        <n-form inline :model="searchForm" label-placement="left">
          <n-form-item label="课程">
            <n-input
              :value="getCourseName(searchForm.courseId)"
              placeholder="请选择课程"
              readonly
              clearable
              @click="openCourseSelector('search')"
              @clear="searchForm.courseId = null"
              style="width: 200px; cursor: pointer"
            >
              <template #suffix>
                <i class="i-carbon:search" />
              </template>
            </n-input>
          </n-form-item>
          <n-form-item label="章节名称">
            <n-input
              v-model:value="searchForm.title"
              placeholder="请输入章节名称"
              clearable
              @keyup.enter="handleSearch"
            />
          </n-form-item>
          <n-form-item>
            <n-button type="primary" @click="handleSearch">
              <template #icon>
                <i class="i-carbon:search" />
              </template>
              搜索
            </n-button>
            <n-button @click="handleReset" style="margin-left: 8px">
              <template #icon>
                <i class="i-carbon:reset" />
              </template>
              重置
            </n-button>
          </n-form-item>
        </n-form>
      </div>

      <!-- 表格 -->
      <div class="flex-1 min-h-0">
        <n-data-table
          :columns="columns"
          :data="tableData"
          :loading="loading"
          :pagination="pagination"
          :row-key="row => row.id"
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
          remote
          :scroll-x="1200"
          :max-height="800"
          virtual-scroll
          striped
          size="small"
          :bordered="false"
        >
          <template #empty>
            <n-empty description="暂无数据" />
          </template>
        </n-data-table>
      </div>
    </n-card>

    <!-- 课程选择弹窗 -->
    <n-modal v-model:show="showCourseModal" :mask-closable="false">
      <n-card
        style="width: 800px"
        title="选择课程"
        :bordered="false"
        size="huge"
        role="dialog"
        aria-modal="true"
      >
        <template #header-extra>
          <n-button quaternary circle @click="showCourseModal = false">
            <template #icon>
              <i class="i-carbon:close" />
            </template>
          </n-button>
        </template>
        
        <!-- 搜索区域 -->
        <div class="mb-4">
          <n-form inline>
            <n-form-item label="课程名称">
              <n-input
                v-model:value="courseSearch.title"
                placeholder="请输入课程名称"
                clearable
                @keyup.enter="handleCourseSearch"
                style="width: 200px"
              />
            </n-form-item>
            <n-form-item label="状态">
              <n-select
                v-model:value="courseSearch.status"
                placeholder="请选择状态"
                :options="statusOptions"
                clearable
                style="width: 120px"
              />
            </n-form-item>
            <n-form-item>
              <n-button type="primary" @click="handleCourseSearch">
                <template #icon>
                  <i class="i-carbon:search" />
                </template>
                搜索
              </n-button>
              <n-button @click="handleCourseReset" style="margin-left: 8px">
                <template #icon>
                  <i class="i-carbon:reset" />
                </template>
                重置
              </n-button>
            </n-form-item>
          </n-form>
        </div>

        <!-- 课程列表 -->
        <n-data-table
          :columns="courseColumns"
          :data="courseList"
          :loading="courseLoading"
          :pagination="coursePagination"
          :row-key="row => row.id"
          @update:page="handleCoursePageChange"
          @update:page-size="handleCoursePageSizeChange"
          remote
          :max-height="400"
          striped
          size="small"
        >
          <template #empty>
            <n-empty description="暂无课程数据" />
          </template>
        </n-data-table>
      </n-card>
    </n-modal>

    <!-- 新增/编辑弹窗 -->
    <n-modal v-model:show="showModal" :mask-closable="false">
      <n-card
        style="width: 600px"
        :title="modalTitle"
        :bordered="false"
        size="huge"
        role="dialog"
        aria-modal="true"
      >
        <template #header-extra>
          <n-button quaternary circle @click="showModal = false">
            <template #icon>
              <i class="i-carbon:close" />
            </template>
          </n-button>
        </template>
        
        <n-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-placement="left"
          label-width="80px"
          require-mark-placement="right-hanging"
        >
          <n-form-item label="课程" path="courseId">
            <n-input
              :value="getCourseName(formData.courseId)"
              placeholder="请选择课程"
              readonly
              @click="openCourseSelector('form')"
              style="cursor: pointer"
            >
              <template #suffix>
                <i class="i-carbon:search" />
              </template>
            </n-input>
          </n-form-item>
          <n-form-item label="章节标题" path="title">
            <n-input v-model:value="formData.title" placeholder="请输入章节标题" />
          </n-form-item>

        </n-form>

        <template #footer>
          <div class="flex justify-end space-x-2">
            <n-button @click="showModal = false">取消</n-button>
            <n-button type="primary" :loading="submitLoading" @click="handleSubmit">
              确定
            </n-button>
          </div>
        </template>
      </n-card>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NPopconfirm, useMessage } from 'naive-ui'
import { PageHeader } from '@/components'
import { formatDateTime } from '@/utils'
import api from './api'

const message = useMessage()
const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const showModal = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const formRef = ref(null)
const courseOptions = ref([])
const currentCourseName = ref('')
const courseNameMap = ref({}) // 课程ID到名称的映射

// 课程选择弹窗相关
const showCourseModal = ref(false)
const courseLoading = ref(false)
const courseList = ref([])
const courseSelectorType = ref('search') // 'search' 或 'form'

// 课程搜索表单
const courseSearch = reactive({
  title: '',
  status: null
})

// 课程状态选项
const statusOptions = [
  { label: '全部', value: null },
  { label: '未发布', value: 0 },
  { label: '已发布', value: 1 }
]

// 课程分页配置
const coursePagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  showQuickJumper: true,
  prefix: ({ itemCount }) => `共 ${itemCount} 条`
})

// 搜索表单
const searchForm = reactive({
  courseId: null,
  title: ''
})

// 分页配置
const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100],
  showQuickJumper: true,
  prefix: ({ itemCount }) => `共 ${itemCount} 条`,
  simple: false,
  disabled: false
})

// 表单数据
const formData = reactive({
  id: null,
  courseId: null,
  title: ''
})

// 表单验证规则
const formRules = {
  courseId: [
    { required: true, message: '请选择课程', trigger: 'change', type: 'number' }
  ],
  title: [
    { required: true, message: '请输入章节标题', trigger: 'blur' },
    { min: 2, max: 100, message: '章节标题长度应在2-100个字符之间', trigger: 'blur' }
  ]
}

// 计算属性
const modalTitle = computed(() => formData.id ? '编辑章节' : '新增章节')

// 课程表格列配置
const courseColumns = [
  {
    title: 'ID',
    key: 'id',
    width: 80
  },
  {
    title: '课程名称',
    key: 'title',
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render(row) {
      return row.status === 1 ? '已发布' : '未发布'
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    render(row) {
      return h(
        NButton,
        {
          size: 'small',
          type: 'primary',
          onClick: () => handleSelectCourse(row)
        },
        { default: () => '选择' }
      )
    }
  }
]

// 表格列配置
const columns = [
  {
    title: 'ID',
    key: 'id',
    width: 80
  },
  {
    title: '课程名称',
    key: 'courseName',
    width: 150,
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '章节标题',
    key: 'title',
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '创建者ID',
    key: 'createUser',
    width: 100
  },
  {
    title: '创建时间',
    key: 'createTime',
    width: 180,
    render(row) {
      return formatDateTime(row.createTime) || '-'
    }
  },
  {
    title: '修改者ID',
    key: 'updateUser',
    width: 100
  },
  {
    title: '修改时间',
    key: 'updateTime',
    width: 180,
    render(row) {
      return formatDateTime(row.updateTime) || '-'
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 250,
    render(row) {
      return h('div', { class: 'flex space-x-2' }, [
        h(
          NButton,
          {
            size: 'small',
            type: 'primary',
            secondary: true,
            onClick: () => handleEdit(row)
          },
          { default: () => '编辑' }
        ),
        h(
          NButton,
          {
            size: 'small',
            type: 'info',
            secondary: true,
            onClick: () => handleManageSections(row)
          },
          { default: () => '小节管理' }
        ),
        h(
          NPopconfirm,
          {
            onPositiveClick: () => handleDelete(row.id)
          },
          {
            default: () => '确定删除这个章节吗？',
            trigger: () => h(
              NButton,
              {
                size: 'small',
                type: 'error',
                secondary: true
              },
              { default: () => '删除' }
            )
          }
        )
      ])
    }
  }
]

// 按需加载课程名称映射
const loadCourseNames = async (courseIds) => {
  if (!courseIds || courseIds.length === 0) return
  
  // 过滤出未映射的课程ID
  const unmappedIds = courseIds.filter(id => !courseNameMap.value[id])
  if (unmappedIds.length === 0) return
  
  try {
    console.log('需要加载的课程ID:', unmappedIds)
    
    // 批量查询课程信息
    const courseRes = await api.getCourses({ 
      page: 1, 
      size: 100,
      status: 1 
    })
    const courseData = courseRes?.data || courseRes || {}
    const courses = courseData.list || courseData.records || []
    
    // 更新映射
    courses.forEach(course => {
      if (unmappedIds.includes(course.id)) {
        courseNameMap.value[course.id] = course.title
      }
    })
    
    console.log('课程名称映射已更新:', courseNameMap.value)
  } catch (error) {
    console.error('加载课程名称失败:', error)
  }
}

// 方法
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.pageSize,
      ...searchForm
    }
    const res = await api.getList(params)
    const data = res?.data || res || {}
    const list = data.list || data.records || []
    const total = data.total || data.totalCount || 0
    
    // 收集需要映射的课程ID
    if (list.length > 0) {
      const courseIds = [...new Set(list.map(item => item.courseId).filter(Boolean))]
      
      // 按需加载课程名称
      await loadCourseNames(courseIds)
      
      // 使用映射数据
      list.forEach(item => {
        if (item.courseId) {
          item.courseName = courseNameMap.value[item.courseId] || `课程ID: ${item.courseId}`
        }
      })
    }
    
    tableData.value = list
    pagination.itemCount = total
  } catch (error) {
    message.error('加载数据失败')
    console.error(error)
    tableData.value = []
    pagination.itemCount = 0
  } finally {
    loading.value = false
  }
}

// 加载课程列表（用于弹窗选择）
const loadCourseList = async () => {
  courseLoading.value = true
  try {
    const params = {
      page: coursePagination.page,
      size: coursePagination.pageSize,
      title: courseSearch.title || '',
      status: courseSearch.status ?? ''
    }
    const res = await api.getCourses(params)
    const data = res?.data || res || {}
    courseList.value = data.list || data.records || []
    coursePagination.itemCount = data.total || data.totalCount || 0
    
    // 同时更新 courseOptions 用于显示名称
    courseOptions.value = courseList.value.map(course => ({
      label: course.title,
      value: course.id
    }))
  } catch (error) {
    console.error('加载课程列表失败', error)
    message.error('加载课程列表失败')
    courseList.value = []
    coursePagination.itemCount = 0
  } finally {
    courseLoading.value = false
  }
}

// 打开课程选择器
const openCourseSelector = (type) => {
  courseSelectorType.value = type
  courseSearch.title = ''
  courseSearch.status = null
  coursePagination.page = 1
  showCourseModal.value = true
  loadCourseList()
}

// 选择课程
const handleSelectCourse = (course) => {
  if (courseSelectorType.value === 'search') {
    searchForm.courseId = course.id
  } else {
    formData.courseId = course.id
  }
  
  // 更新 courseOptions
  const existingOption = courseOptions.value.find(opt => opt.value === course.id)
  if (!existingOption) {
    courseOptions.value.push({
      label: course.title,
      value: course.id
    })
  }
  
  showCourseModal.value = false
  message.success(`已选择课程：${course.title}`)
}

// 获取课程名称
const getCourseName = (courseId) => {
  if (!courseId) return ''
  const course = courseOptions.value.find(opt => opt.value === courseId)
  return course ? course.label : ''
}

// 课程搜索
const handleCourseSearch = () => {
  coursePagination.page = 1
  loadCourseList()
}

// 课程搜索重置
const handleCourseReset = () => {
  courseSearch.title = ''
  courseSearch.status = null
  coursePagination.page = 1
  loadCourseList()
}

// 课程分页
const handleCoursePageChange = (page) => {
  coursePagination.page = page
  loadCourseList()
}

const handleCoursePageSizeChange = (pageSize) => {
  coursePagination.pageSize = pageSize
  coursePagination.page = 1
  loadCourseList()
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    courseId: null,
    title: ''
  })
  pagination.page = 1
  loadData()
}

const handlePageChange = (page) => {
  pagination.page = page
  loadData()
}

const handlePageSizeChange = (pageSize) => {
  pagination.pageSize = pageSize
  pagination.page = 1
  loadData()
}

const handleAdd = () => {
  Object.assign(formData, {
    id: null,
    courseId: null,
    title: ''
  })
  showModal.value = true
}

const handleEdit = async (row) => {
  try {
    const res = await api.getDetail(row.id)
    Object.assign(formData, res.data)
    showModal.value = true
  } catch (error) {
    message.error('获取章节详情失败')
    console.error(error)
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitLoading.value = true

    console.log('提交的章节数据:', formData)
    
    // 准备提交的数据
    const submitData = {
      courseId: formData.courseId,
      title: formData.title?.trim()
    }
    
    console.log('处理后的提交数据:', submitData)

    if (formData.id) {
      await api.update(formData.id, submitData)
      message.success('更新成功')
    } else {
      await api.create(submitData)
      message.success('创建成功')
    }
    
    showModal.value = false
    loadData()
  } catch (error) {
    console.error('提交失败:', error)
    
    if (error?.response?.data?.message) {
      message.error(error.response.data.message)
    } else if (error?.message) {
      message.error(error.message)
    } else {
      message.error('操作失败，请稍后重试')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await api.delete(id)
    message.success('删除成功')
    loadData()
  } catch (error) {
    message.error('删除失败')
    console.error(error)
  }
}

const handleManageSections = (row) => {
  // 跳转到小节管理页面，传递章节信息
  try {
    const courseName = row.courseName || '未知课程'
    
    // 使用 router.push 进行跳转
    router.push({
      path: '/teacherhome/section',
      query: {
        chapterId: row.id,
        chapterTitle: row.title,
        courseId: row.courseId,
        courseName: courseName
      }
    }).catch((err) => {
      console.error('路由跳转错误:', err)
      // 如果路由跳转失败，使用 window.location 作为备选方案
      const url = `#/teacherhome/section?chapterId=${row.id}&chapterTitle=${encodeURIComponent(row.title)}&courseId=${row.courseId}&courseName=${encodeURIComponent(courseName)}`
      window.location.href = url
    })
  } catch (error) {
    console.error('跳转失败:', error)
    message.error('跳转失败，请稍后重试')
  }
}

// 生命周期
onMounted(() => {
  // 检查是否从课程管理页面跳转过来
  const courseId = route.query.courseId
  const courseName = route.query.courseName
  
  if (courseId && courseName) {
    // 如果有课程信息，直接设置搜索条件和映射
    searchForm.courseId = Number(courseId)
    currentCourseName.value = courseName
    
    // 将跳转过来的课程信息也加入映射
    courseNameMap.value[Number(courseId)] = courseName
    
    // 添加当前课程到选项中
    courseOptions.value = [{
      label: courseName,
      value: Number(courseId)
    }]
    
    message.success(`已选择课程：${courseName}`)
  }
  
  loadData()
})
</script>

<style scoped>
.teacher-chapter-management {
  padding: 16px;
}

/* 确保表格可以正确滚动 */
:deep(.n-data-table-wrapper) {
  display: flex;
  flex-direction: column;
  max-height: 100%;
}

:deep(.n-data-table) {
  display: flex;
  flex-direction: column;
}

:deep(.n-data-table-base-table-header) {
  flex-shrink: 0;
}

:deep(.n-data-table-base-table) {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

:deep(.n-data-table-base-table-body) {
  flex: 1;
  overflow-y: auto;
  min-height: 200px;
}

/* 分页样式优化 */
:deep(.n-pagination) {
  margin-top: 16px;
  justify-content: center;
  flex-shrink: 0;
  padding: 8px 0;
  border-top: 1px solid var(--n-border-color);
}

/* 表格滚动条样式 */
:deep(.n-scrollbar-rail) {
  right: 2px;
}

:deep(.n-scrollbar-rail--vertical) {
  width: 6px;
}

:deep(.n-scrollbar-rail--horizontal) {
  height: 6px;
}

/* 确保卡片内容区域布局正确 */
:deep(.n-card__content) {
  display: flex;
  flex-direction: column;
  padding: 20px;
}
</style>