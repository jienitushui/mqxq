<template>
  <div class="section-management h-full flex flex-col">
    <PageHeader class="flex-shrink-0">
      <template #title>
        小节管理
        <span v-if="currentChapterInfo" class="text-sm text-gray-500 ml-2">
          - {{ currentChapterInfo }}
        </span>
      </template>
      <template #extra>
        <n-button @click="handleBatchDelete" :disabled="!selectedRowKeys.length" type="error" style="margin-right: 8px">
          <template #icon>
            <i class="i-carbon:trash-can" />
          </template>
          批量删除
        </n-button>
        <n-button type="primary" @click="handleAdd">
          <template #icon>
            <i class="i-carbon:add" />
          </template>
          新增小节
        </n-button>
      </template>
    </PageHeader>

    <n-card class="flex-1 flex flex-col overflow-hidden">
      <!-- 搜索区域 -->
      <div class="flex-shrink-0 mb-4">
        <n-form inline :model="searchForm" label-placement="left">
          <n-form-item label="课程">
            <n-input
              :value="searchForm.courseName"
              placeholder="请选择课程"
              readonly
              clearable
              @click="courseSelectMode = 'search'; showCourseModal = true"
              @clear="handleClearCourse('search')"
              style="width: 200px; cursor: pointer"
            >
              <template #suffix>
                <i class="i-carbon:search" />
              </template>
            </n-input>
          </n-form-item>
          <n-form-item label="章节">
            <n-input
              :value="searchForm.chapterName"
              placeholder="请选择章节"
              readonly
              clearable
              :disabled="!searchForm.courseId"
              @click="chapterSelectMode = 'search'; showChapterModal = true"
              @clear="handleClearChapter('search')"
              style="width: 200px; cursor: pointer"
            >
              <template #suffix>
                <i class="i-carbon:search" />
              </template>
            </n-input>
          </n-form-item>
          <n-form-item label="小节名称">
            <n-input
              v-model:value="searchForm.title"
              placeholder="请输入小节名称"
              clearable
              @keyup.enter="handleSearch"
            />
          </n-form-item>
          <n-form-item label="状态">
            <n-select
              v-model:value="searchForm.status"
              placeholder="请选择状态"
              :options="statusOptions"
              clearable
              style="width: 120px"
            />
          </n-form-item>
          <n-form-item>
            <n-button type="primary" @click="handleSearch">搜索</n-button>
            <n-button @click="handleReset" style="margin-left: 8px">重置</n-button>
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
          v-model:checked-row-keys="selectedRowKeys"
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
      
      <!-- 分页信息 -->
      <div v-if="!loading && tableData.length > 0" class="flex justify-between items-center mt-4 text-sm text-gray-500">
        <div>
          显示第 {{ (pagination.page - 1) * pagination.pageSize + 1 }} 到 
          {{ Math.min(pagination.page * pagination.pageSize, pagination.itemCount) }} 条，
          共 {{ pagination.itemCount }} 条记录
        </div>
        <div v-if="selectedRowKeys.length > 0">
          已选择 {{ selectedRowKeys.length }} 项
        </div>
      </div>
    </n-card>

    <!-- 新增/编辑弹窗 -->
    <n-modal v-model:show="showModal" :mask-closable="false">
      <n-card
        style="width: 700px"
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
              :value="formData.courseName"
              placeholder="请选择课程"
              readonly
              @click="courseSelectMode = 'form'; showCourseModal = true"
              style="cursor: pointer"
            >
              <template #suffix>
                <i class="i-carbon:search" />
              </template>
            </n-input>
          </n-form-item>
          <n-form-item label="章节" path="chapterId">
            <n-input
              :value="formData.chapterName"
              placeholder="请选择章节"
              readonly
              :disabled="!formData.courseId"
              @click="chapterSelectMode = 'form'; showChapterModal = true"
              style="cursor: pointer"
            >
              <template #suffix>
                <i class="i-carbon:search" />
              </template>
            </n-input>
          </n-form-item>
          <n-form-item label="小节标题" path="title">
            <n-input v-model:value="formData.title" placeholder="请输入小节标题" />
          </n-form-item>
          <n-form-item label="小节内容">
            <n-input
              v-model:value="formData.content"
              type="textarea"
              placeholder="请输入小节内容（可选）"
              :rows="4"
            />
          </n-form-item>
          <n-form-item label="视频文件" path="videoUrl">
            <n-upload
              v-model:file-list="videoFileList"
              action="/api/public/files/video"
              :max="1"
              list-type="text"
              accept="video/*"
              :show-file-list="true"
              @before-upload="handleVideoBeforeUpload"
              @finish="handleVideoUploadFinish"
              @remove="handleRemoveVideo"
              @error="handleVideoUploadError"
            >
              <n-button v-if="videoFileList.length === 0">
                <template #icon>
                  <i class="i-carbon:cloud-upload" />
                </template>
                上传视频文件
              </n-button>
            </n-upload>
            <div v-if="formData.videoUrl" class="text-xs text-gray-500 mt-1">
              当前视频: {{ formData.videoUrl }}
            </div>
          </n-form-item>
          <n-form-item label="时长(秒)" path="duration">
            <n-input-number
              v-model:value="formData.duration"
              placeholder="请输入视频时长"
              :min="0"
            />
          </n-form-item>
          <n-form-item label="发布状态" path="status">
            <n-select
              v-model:value="formData.status"
              placeholder="请选择状态"
              :options="statusOptions"
            />
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

    <!-- 课程选择弹窗 -->
    <n-modal v-model:show="showCourseModal" :mask-closable="false">
      <n-card
        style="width: 900px"
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
        
        <!-- 课程搜索 -->
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
            <n-form-item>
              <n-button type="primary" @click="handleCourseSearch">搜索</n-button>
              <n-button @click="handleCourseReset" style="margin-left: 8px">重置</n-button>
            </n-form-item>
          </n-form>
        </div>

        <!-- 课程列表 -->
        <n-data-table
          :columns="courseColumns"
          :data="courseTableData"
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

    <!-- 章节选择弹窗 -->
    <n-modal v-model:show="showChapterModal" :mask-closable="false">
      <n-card
        style="width: 900px"
        title="选择章节"
        :bordered="false"
        size="huge"
        role="dialog"
        aria-modal="true"
      >
        <template #header-extra>
          <n-button quaternary circle @click="showChapterModal = false">
            <template #icon>
              <i class="i-carbon:close" />
            </template>
          </n-button>
        </template>
        
        <!-- 章节搜索 -->
        <div class="mb-4">
          <n-form inline>
            <n-form-item label="章节标题">
              <n-input
                v-model:value="chapterSearch.title"
                placeholder="请输入章节标题"
                clearable
                @keyup.enter="handleChapterSearch"
                style="width: 200px"
              />
            </n-form-item>
            <n-form-item>
              <n-button type="primary" @click="handleChapterSearch">搜索</n-button>
              <n-button @click="handleChapterReset" style="margin-left: 8px">重置</n-button>
            </n-form-item>
          </n-form>
        </div>

        <!-- 章节列表 -->
        <n-data-table
          :columns="chapterColumns"
          :data="chapterTableData"
          :loading="chapterLoading"
          :pagination="chapterPagination"
          :row-key="row => row.id"
          @update:page="handleChapterPageChange"
          @update:page-size="handleChapterPageSizeChange"
          remote
          :max-height="400"
          striped
          size="small"
        >
          <template #empty>
            <n-empty description="暂无章节数据" />
          </template>
        </n-data-table>
      </n-card>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { NButton, NTag, NPopconfirm, NEmpty, useMessage } from 'naive-ui'
import { PageHeader } from '@/components'
import { handle401Error } from '@/utils'
import api from './api'

const message = useMessage()
const route = useRoute()

// 响应式数据
const loading = ref(false)
const showModal = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const formRef = ref(null)
const selectedRowKeys = ref([])
const courseOptions = ref([])
const chapterOptions = ref([])
const formChapterOptions = ref([])
const videoFileList = ref([])
const chapterNameCache = ref({}) // 缓存章节名称
const currentChapterInfo = ref('') // 当前章节信息
const courseNameMap = ref({}) // 课程ID到名称的映射
const chapterNameMap = ref({}) // 章节ID到名称的映射

// 课程和章节选择弹窗相关
const showCourseModal = ref(false)
const showChapterModal = ref(false)
const courseLoading = ref(false)
const chapterLoading = ref(false)
const courseTableData = ref([])
const chapterTableData = ref([])
const courseSelectMode = ref('search') // 'search' 或 'form'
const chapterSelectMode = ref('search') // 'search' 或 'form'

// 监听课程弹窗打开
watch(showCourseModal, (newVal) => {
  if (newVal) {
    coursePagination.page = 1
    loadCourses()
  }
})

// 监听章节弹窗打开
watch(showChapterModal, (newVal) => {
  if (newVal) {
    chapterPagination.page = 1
    const courseId = chapterSelectMode.value === 'search' ? searchForm.courseId : formData.courseId
    if (courseId) {
      loadChapters(courseId)
    } else {
      message.warning('请先选择课程')
      showChapterModal.value = false
    }
  }
})

// 搜索表单
const searchForm = reactive({
  courseId: null,
  courseName: '',
  chapterId: null,
  chapterName: '',
  title: '',
  status: null
})

// 课程搜索表单
const courseSearch = reactive({
  title: '',
  status: 1
})

// 章节搜索表单
const chapterSearch = reactive({
  title: ''
})

// 状态选项
const statusOptions = [
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
  prefix: ({ itemCount }) => `共 ${itemCount} 条`,
  simple: false
})

// 章节分页配置
const chapterPagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  showQuickJumper: true,
  prefix: ({ itemCount }) => `共 ${itemCount} 条`,
  simple: false
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
  courseName: '',
  chapterId: null,
  chapterName: '',
  title: '',
  content: '',
  videoUrl: '',
  duration: 0,
  status: 0
})

// 表单验证规则
const formRules = {
  courseId: [
    { required: true, message: '请选择课程', trigger: 'change', type: 'number' }
  ],
  chapterId: [
    { required: true, message: '请选择章节', trigger: 'change', type: 'number' }
  ],
  title: [
    { required: true, message: '请输入小节标题', trigger: 'blur' },
    { min: 2, max: 50, message: '小节标题长度应在2-50个字符之间', trigger: 'blur' }
  ],
  duration: [
    { type: 'number', min: 0, message: '时长不能小于0', trigger: 'blur' }
  ]
}

// 计算属性
const modalTitle = computed(() => formData.id ? '编辑小节' : '新增小节')

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
      return row.status === 1 ? '启用' : '禁用'
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

// 章节表格列配置
const chapterColumns = [
  {
    title: 'ID',
    key: 'id',
    width: 80
  },
  {
    title: '章节标题',
    key: 'title',
    ellipsis: {
      tooltip: true
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
          onClick: () => handleSelectChapter(row)
        },
        { default: () => '选择' }
      )
    }
  }
]

// 表格列配置
const columns = [
  {
    type: 'selection'
  },
  {
    title: 'ID',
    key: 'id',
    width: 80
  },
  {
    title: '课程名称',
    key: 'courseName',
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '章节名称',
    key: 'chapterName',
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '小节标题',
    key: 'title',
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '时长',
    key: 'duration',
    width: 100,
    render(row) {
      if (!row.duration) return '-'
      const minutes = Math.floor(row.duration / 60)
      const seconds = row.duration % 60
      return `${minutes}:${seconds.toString().padStart(2, '0')}`
    }
  },

  {
    title: '状态',
    key: 'status',
    width: 100,
    render(row) {
      return h(NTag, {
        type: row.status === 1 ? 'success' : 'warning',
        size: 'small'
      }, {
        default: () => row.status === 1 ? '已发布' : '未发布'
      })
    }
  },
  {
    title: '创建时间',
    key: 'createTime',
    width: 180,
    render(row) {
      return formatDateTime(row.createTime)
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
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
          NPopconfirm,
          {
            onPositiveClick: () => handleDelete(row.id)
          },
          {
            default: () => '确定删除这个小节吗？',
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

// 按需加载章节名称映射
const loadChapterNames = async (chapterIds, courseIds) => {
  if (!chapterIds || chapterIds.length === 0) return
  
  // 过滤出未映射的章节ID
  const unmappedIds = chapterIds.filter(id => !chapterNameMap.value[id])
  if (unmappedIds.length === 0) return
  
  try {
    console.log('需要加载的章节ID:', unmappedIds)
    
    // 获取唯一的课程ID列表
    const uniqueCourseIds = [...new Set(courseIds.filter(Boolean))]
    
    // 为每个课程加载章节
    for (const courseId of uniqueCourseIds) {
      try {
        const chapterRes = await api.getChapters({ 
          courseId: courseId,
          page: 1,
          size: 100
        })
        const chapterData = chapterRes?.data || chapterRes || {}
        const chapters = chapterData.list || chapterData.records || []
        
        // 更新映射
        chapters.forEach(chapter => {
          if (unmappedIds.includes(chapter.id)) {
            chapterNameMap.value[chapter.id] = chapter.title
          }
        })
      } catch (error) {
        console.error(`加载课程${courseId}的章节失败:`, error)
      }
    }
    
    console.log('章节名称映射已更新:', chapterNameMap.value)
  } catch (error) {
    console.error('加载章节名称失败:', error)
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
    
    // 过滤空值参数
    Object.keys(params).forEach(key => {
      if (params[key] === null || params[key] === undefined || params[key] === '') {
        delete params[key]
      }
    })
    
    const res = await api.getList(params)
    const data = res?.data || res || {}
    let sections = data.list || data.records || []
    
    // 收集需要映射的ID
    if (sections.length > 0) {
      const courseIds = [...new Set(sections.map(s => s.courseId).filter(Boolean))]
      const chapterIds = [...new Set(sections.map(s => s.chapterId).filter(Boolean))]
      
      // 按需加载课程和章节名称
      await loadCourseNames(courseIds)
      await loadChapterNames(chapterIds, courseIds)
      
      // 使用映射数据
      sections = sections.map(section => ({
        ...section,
        courseName: courseNameMap.value[section.courseId] || `课程ID: ${section.courseId}`,
        chapterName: chapterNameMap.value[section.chapterId] || `章节ID: ${section.chapterId}`
      }))
    }
    
    console.log('映射后的小节数据:', sections)
    
    tableData.value = sections
    pagination.itemCount = data.total || data.totalCount || 0
    
    // 如果当前页没有数据且不是第一页，自动跳转到第一页
    if (sections.length === 0 && pagination.page > 1 && pagination.itemCount > 0) {
      pagination.page = 1
      loadData()
      return
    }
    
  } catch (error) {
    // 处理401错误，直接重定向到登录页面
    if (await handle401Error(error, message)) return
    
    message.error('加载数据失败')
    console.error(error)
    tableData.value = []
    pagination.itemCount = 0
  } finally {
    loading.value = false
  }
}

const loadCourses = async () => {
  courseLoading.value = true
  try {
    const params = {
      page: coursePagination.page,
      size: coursePagination.pageSize,
      ...courseSearch
    }
    
    const res = await api.getCourses(params)
    const data = res?.data || res || {}
    const list = data.list || data.records || []
    const total = data.total || data.totalCount || 0
    
    courseTableData.value = list
    coursePagination.itemCount = total
    
    if (list.length === 0 && coursePagination.page === 1) {
      message.warning('暂无课程数据，请先创建课程')
    }
  } catch (error) {
    if (await handle401Error(error, message)) return
    console.error('加载课程列表失败', error)
    message.error('加载课程列表失败')
    courseTableData.value = []
    coursePagination.itemCount = 0
  } finally {
    courseLoading.value = false
  }
}

const handleCourseSearch = () => {
  coursePagination.page = 1
  loadCourses()
}

const handleCourseReset = () => {
  Object.assign(courseSearch, {
    title: '',
    status: 1
  })
  coursePagination.page = 1
  loadCourses()
}

const handleCoursePageChange = (page) => {
  coursePagination.page = page
  loadCourses()
}

const handleCoursePageSizeChange = (pageSize) => {
  coursePagination.pageSize = pageSize
  coursePagination.page = 1
  loadCourses()
}

const handleSelectCourse = (course) => {
  if (courseSelectMode.value === 'search') {
    searchForm.courseId = course.id
    searchForm.courseName = course.title
    // 清空章节选择
    searchForm.chapterId = null
    searchForm.chapterName = ''
  } else {
    formData.courseId = course.id
    formData.courseName = course.title
    // 清空章节选择
    formData.chapterId = null
    formData.chapterName = ''
  }
  showCourseModal.value = false
  message.success(`已选择课程：${course.title}`)
}

const handleClearCourse = (mode) => {
  if (mode === 'search') {
    searchForm.courseId = null
    searchForm.courseName = ''
    searchForm.chapterId = null
    searchForm.chapterName = ''
  } else {
    formData.courseId = null
    formData.courseName = ''
    formData.chapterId = null
    formData.chapterName = ''
  }
}

const loadChapters = async (courseId) => {
  if (!courseId) {
    chapterTableData.value = []
    return
  }
  
  chapterLoading.value = true
  try {
    const params = {
      page: chapterPagination.page,
      size: chapterPagination.pageSize,
      courseId: courseId,
      ...chapterSearch
    }
    
    const res = await api.getChapters(params)
    const data = res?.data || res || {}
    const list = data.list || data.records || []
    const total = data.total || data.totalCount || 0
    
    chapterTableData.value = list
    chapterPagination.itemCount = total
    
    if (list.length === 0 && chapterPagination.page === 1) {
      message.warning('该课程暂无章节数据')
    }
  } catch (error) {
    if (await handle401Error(error, message)) return
    console.error('加载章节列表失败', error)
    message.error('加载章节列表失败')
    chapterTableData.value = []
    chapterPagination.itemCount = 0
  } finally {
    chapterLoading.value = false
  }
}

const handleChapterSearch = () => {
  chapterPagination.page = 1
  const courseId = chapterSelectMode.value === 'search' ? searchForm.courseId : formData.courseId
  loadChapters(courseId)
}

const handleChapterReset = () => {
  Object.assign(chapterSearch, {
    title: ''
  })
  chapterPagination.page = 1
  const courseId = chapterSelectMode.value === 'search' ? searchForm.courseId : formData.courseId
  loadChapters(courseId)
}

const handleChapterPageChange = (page) => {
  chapterPagination.page = page
  const courseId = chapterSelectMode.value === 'search' ? searchForm.courseId : formData.courseId
  loadChapters(courseId)
}

const handleChapterPageSizeChange = (pageSize) => {
  chapterPagination.pageSize = pageSize
  chapterPagination.page = 1
  const courseId = chapterSelectMode.value === 'search' ? searchForm.courseId : formData.courseId
  loadChapters(courseId)
}

const handleSelectChapter = (chapter) => {
  if (chapterSelectMode.value === 'search') {
    searchForm.chapterId = chapter.id
    searchForm.chapterName = chapter.title
  } else {
    formData.chapterId = chapter.id
    formData.chapterName = chapter.title
  }
  showChapterModal.value = false
  message.success(`已选择章节：${chapter.title}`)
}

const handleClearChapter = (mode) => {
  if (mode === 'search') {
    searchForm.chapterId = null
    searchForm.chapterName = ''
  } else {
    formData.chapterId = null
    formData.chapterName = ''
  }
}

const handleCourseChange = (courseId) => {
  searchForm.chapterId = null
  searchForm.chapterName = ''
}

const handleFormCourseChange = (courseId) => {
  formData.chapterId = null
  formData.chapterName = ''
}

const handleSearch = async () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    courseId: null,
    courseName: '',
    chapterId: null,
    chapterName: '',
    title: '',
    status: null
  })
  chapterOptions.value = []
  pagination.page = 1
  loadData()
}

const handlePageChange = (page) => {
  if (page === pagination.page) return
  pagination.page = page
  loadData()
}

const handlePageSizeChange = (pageSize) => {
  if (pageSize === pagination.pageSize) return
  pagination.pageSize = pageSize
  pagination.page = 1
  loadData()
}

const handleAdd = () => {
  Object.assign(formData, {
    id: null,
    courseId: null,
    courseName: '',
    chapterId: null,
    chapterName: '',
    title: '',
    content: '',
    videoUrl: '',
    duration: 0,
    status: 0
  })
  formChapterOptions.value = []
  videoFileList.value = []
  showModal.value = true
}

const handleEdit = async (row) => {
  try {
    const res = await api.getDetail(row.id)
    const detail = res.data || res
    Object.assign(formData, {
      ...detail,
      courseName: row.courseName || '',
      chapterName: row.chapterName || ''
    })
    
    // 如果有视频URL，设置到文件列表中
    if (formData.videoUrl) {
      videoFileList.value = [
        {
          id: 'current-video',
          name: '当前视频文件',
          status: 'finished',
          url: formData.videoUrl
        }
      ]
    } else {
      videoFileList.value = []
    }
    
    showModal.value = true
  } catch (error) {
    if (await handle401Error(error, message)) return
    message.error('获取小节详情失败')
    console.error(error)
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitLoading.value = true

    if (formData.id) {
      await api.update(formData.id, formData)
      message.success('更新成功')
    } else {
      await api.create(formData)
      message.success('创建成功')
    }
    
    showModal.value = false
    loadData()
  } catch (error) {
    if (await handle401Error(error, message)) return
    message.error(error?.message || '操作失败')
    console.error(error)
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
    if (await handle401Error(error, message)) return
    message.error('删除失败')
    console.error(error)
  }
}

const handleBatchDelete = async () => {
  if (!selectedRowKeys.value || selectedRowKeys.value.length === 0) {
    message.warning('请选择要删除的小节')
    return
  }
  
  try {
    // 如果API支持批量删除，使用批量删除接口
    if (api.batchDelete) {
      await api.batchDelete(selectedRowKeys.value)
    } else {
      // 否则逐个删除
      await Promise.all(selectedRowKeys.value.map(id => api.delete(id)))
    }
    message.success(`成功删除 ${selectedRowKeys.value.length} 个小节`)
    selectedRowKeys.value = []
    loadData()
  } catch (error) {
    if (await handle401Error(error, message)) return
    message.error('批量删除失败')
    console.error(error)
  }
}

const handleStatusChange = async (id, status) => {
  try {
    await api.updateStatus(id, status)
    message.success(status === 1 ? '发布成功' : '取消发布成功')
    loadData()
  } catch (error) {
    message.error('状态更新失败')
    console.error(error)
  }
}

// 视频上传相关方法
const handleVideoBeforeUpload = (data) => {
  const rawFile = data.file.file

  if (!rawFile) {
    message.error('无法读取文件，请重试')
    return false
  }

  const isVideo = rawFile.type?.startsWith('video/')
  if (!isVideo) {
    message.error('只能上传视频文件')
    return false
  }

  // 检查文件大小 (限制为100MB)
  if (rawFile.size === undefined || rawFile.size === null) {
    message.error('无法获取文件大小，请重新选择文件')
    return false
  }

  const isLt100M = rawFile.size / 1024 / 1024 < 100
  if (!isLt100M) {
    message.error('视频大小不能超过100MB')
    return false
  }

  return true
}

const handleVideoUploadFinish = ({ file, event }) => {
  try {
    // 接口直接返回 URL 字符串
    const videoUrl = event.target.response
    console.log('视频上传响应:', videoUrl)
    
    if (videoUrl && typeof videoUrl === 'string' && videoUrl.startsWith('http')) {
      formData.videoUrl = videoUrl
      message.success('视频上传成功')
      file.status = 'finished'
      file.url = videoUrl
    } else {
      console.error('无效的视频URL:', videoUrl)
      message.error('上传成功但未获取到有效的视频地址')
    }
  } catch (error) {
    console.error('视频上传处理失败:', error)
    message.error('视频上传失败')
  }
}

// 通用文件删除函数
const deleteFileFromServer = async (fileUrl) => {
  try {
    if (!fileUrl) return
    
    // 从URL中提取文件路径，例如：http://localhost:9900/mxng/video/xxx.mp4 -> mxng/video/xxx.mp4
    const url = new URL(fileUrl)
    const pathUrl = url.pathname.substring(1) // 移除开头的 '/'
    
    const response = await fetch(`/api/public/files/delete?pathUrl=${encodeURIComponent(pathUrl)}`, {
      method: 'DELETE'
    })
    
    if (!response.ok) {
      throw new Error(`删除失败: ${response.status}`)
    }
    
    return true
  } catch (error) {
    console.error('删除文件失败:', error)
    throw error
  }
}

const handleRemoveVideo = async (file) => {
  try {
    // 如果有文件URL，调用删除接口
    if (file.url || formData.videoUrl) {
      const fileUrl = file.url || formData.videoUrl
      await deleteFileFromServer(fileUrl)
      message.success('视频删除成功')
    }
    
    formData.videoUrl = ''
    videoFileList.value = []
    return true
  } catch (error) {
    console.error('删除视频失败:', error)
    message.error('删除视频失败')
    return false
  }
}

const handleVideoUploadError = ({ file, event }) => {
  console.error('视频上传失败:', event)
  message.error('视频上传失败，请重试')
  
  // 移除失败的文件
  const index = videoFileList.value.findIndex(f => f.id === file.id)
  if (index > -1) {
    videoFileList.value.splice(index, 1)
  }
}

// 时间格式化函数
const formatDateTime = (timestamp) => {
  if (!timestamp) return '-'
  const date = new Date(timestamp)
  const Y = date.getFullYear()
  const M = (date.getMonth() + 1).toString().padStart(2, '0')
  const D = date.getDate().toString().padStart(2, '0')
  const h = date.getHours().toString().padStart(2, '0')
  const m = date.getMinutes().toString().padStart(2, '0')
  const s = date.getSeconds().toString().padStart(2, '0')
  return `${Y}-${M}-${D} ${h}:${m}:${s}`
}

// 生命周期
onMounted(async () => {
  // 检查是否从章节管理页面跳转过来
  const { chapterId, chapterTitle, courseId, courseName } = route.query
  
  if (chapterId && courseId) {
    // 设置搜索条件
    searchForm.courseId = Number(courseId)
    searchForm.courseName = courseName || ''
    searchForm.chapterId = Number(chapterId)
    searchForm.chapterName = chapterTitle || ''
    
    // 将跳转过来的课程和章节信息也加入映射
    if (courseId && courseName) {
      courseNameMap.value[Number(courseId)] = courseName
    }
    if (chapterId && chapterTitle) {
      chapterNameMap.value[Number(chapterId)] = chapterTitle
    }
    
    // 设置当前章节信息显示
    if (chapterTitle && courseName) {
      currentChapterInfo.value = `${courseName} - ${chapterTitle}`
      message.success(`已定位到：${courseName} - ${chapterTitle}`)
    }
  }
  
  // 加载小节数据
  loadData()
})
</script>

<style scoped>
/* 页面容器 */
.section-management {
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

/* 确保卡片内容区域布局正确 */
:deep(.n-card__content) {
  display: flex;
  flex-direction: column;
  padding: 20px;
}

/* 表格行悬停效果 */
:deep(.n-data-table-tr:hover) {
  background-color: var(--n-color-hover);
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

/* 表格加载状态 */
:deep(.n-data-table--loading) {
  min-height: 200px;
}

/* 空状态样式 */
:deep(.n-data-table-empty) {
  padding: 40px 0;
}
</style>