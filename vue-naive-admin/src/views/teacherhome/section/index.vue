<template>
  <div class="teacher-section-management h-full flex flex-col">
    <PageHeader class="flex-shrink-0">
      <template #title>小节管理</template>
      <template #extra>
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
              :value="getCourseName(searchForm.courseId)"
              placeholder="请选择课程"
              readonly
              clearable
              @click="openCourseSelector('search')"
              @clear="handleClearSearchCourse"
              style="width: 200px; cursor: pointer"
            >
              <template #suffix>
                <i class="i-carbon:search" />
              </template>
            </n-input>
          </n-form-item>
          <n-form-item label="章节">
            <n-select
              v-model:value="searchForm.chapterId"
              placeholder="请选择章节"
              :options="chapterOptions"
              clearable
              style="width: 200px"
            />
          </n-form-item>
          <n-form-item label="小节名称">
            <n-input
              v-model:value="searchForm.title"
              placeholder="请输入小节名称"
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
          :scroll-x="1400"
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
          <n-form-item label="章节" path="chapterId">
            <n-select
              v-model:value="formData.chapterId"
              placeholder="请选择章节"
              :options="formChapterOptions"
            />
          </n-form-item>
          <n-form-item label="小节标题" path="title">
            <n-input v-model:value="formData.title" placeholder="请输入小节标题" />
          </n-form-item>
          <n-form-item label="小节内容" path="content">
            <n-input
              v-model:value="formData.content"
              type="textarea"
              placeholder="请输入小节内容"
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
              placeholder="请输入视频时长(秒)"
              :min="0"
            />
          </n-form-item>
          <n-form-item label="状态" path="status">
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h, computed } from 'vue'
import { useRoute } from 'vue-router'
import { NButton, NTag, NPopconfirm, NSwitch, useMessage } from 'naive-ui'
import { PageHeader } from '@/components'
import api from './api'

const message = useMessage()
const route = useRoute()

// 响应式数据
const loading = ref(false)
const showModal = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const formRef = ref(null)
const courseOptions = ref([])
const chapterOptions = ref([])
const formChapterOptions = ref([])
const videoFileList = ref([])
const courseNameMap = ref({}) // 课程ID到名称的映射
const chapterNameMap = ref({}) // 章节ID到名称的映射

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

// 状态选项
const statusOptions = [
  { label: '未发布', value: 0 },
  { label: '已发布', value: 1 }
]

// 搜索表单
const searchForm = reactive({
  courseId: null,
  chapterId: null,
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
  chapterId: null,
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
    width: 80,
    fixed: 'left'
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
    title: '章节名称',
    key: 'chapterName',
    width: 150,
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '小节名称',
    key: 'title',
    width: 200,
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '小节内容',
    key: 'content',
    width: 200,
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '视频URL',
    key: 'videoUrl',
    width: 150,
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '时长(秒)',
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
    width: 80,
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
    width: 150,
    render(row) {
      return formatDateTime(row.createTime)
    }
  },
  {
    title: '创建者ID',
    key: 'createUser',
    width: 100
  },
  {
    title: '修改时间',
    key: 'updateTime',
    width: 150,
    render(row) {
      return formatDateTime(row.updateTime)
    }
  },
  {
    title: '修改者ID',
    key: 'updateUser',
    width: 100
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right',
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
            type: row.status === 1 ? 'warning' : 'success',
            secondary: true,
            onClick: () => toggleStatus(row)
          },
          { default: () => row.status === 1 ? '下线' : '上线' }
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
        const chapterRes = await api.getChapters(courseId)
        const chapterData = chapterRes?.data || chapterRes || []
        const chapters = Array.isArray(chapterData) ? chapterData : []
        
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
    let sections = data.list || data.records || []
    const total = data.total || data.totalCount || 0
    
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
    
    tableData.value = sections
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

const loadCourses = async () => {
  try {
    const res = await api.getCourses()
    console.log('课程列表响应:', res)
    const data = res?.data || res || {}
    const courses = data.list || data.records || []
    console.log('解析的课程数据:', courses)
    courseOptions.value = courses.map(course => ({
      label: course.title,
      value: course.id
    }))
    console.log('课程选项:', courseOptions.value)
  } catch (error) {
    console.error('加载课程列表失败', error)
    courseOptions.value = []
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
    const newOptions = courseList.value.map(course => ({
      label: course.title,
      value: course.id
    }))
    
    // 合并选项，避免覆盖已选择的课程
    newOptions.forEach(newOpt => {
      const exists = courseOptions.value.find(opt => opt.value === newOpt.value)
      if (!exists) {
        courseOptions.value.push(newOpt)
      }
    })
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
    handleCourseChange(course.id)
  } else {
    formData.courseId = course.id
    handleFormCourseChange(course.id)
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

// 清除搜索区域的课程选择
const handleClearSearchCourse = () => {
  searchForm.courseId = null
  searchForm.chapterId = null
  chapterOptions.value = []
}

const loadChapters = async (courseId) => {
  if (!courseId) {
    chapterOptions.value = []
    return
  }
  
  try {
    const res = await api.getChapters(courseId)
    const data = res?.data || res || []
    chapterOptions.value = (Array.isArray(data) ? data : []).map(chapter => ({
      label: chapter.title,
      value: chapter.id
    }))
  } catch (error) {
    console.error('加载章节列表失败', error)
    chapterOptions.value = []
  }
}

const loadFormChapters = async (courseId) => {
  if (!courseId) {
    formChapterOptions.value = []
    return
  }
  
  try {
    const res = await api.getChapters(courseId)
    const data = res?.data || res || []
    formChapterOptions.value = (Array.isArray(data) ? data : []).map(chapter => ({
      label: chapter.title,
      value: chapter.id
    }))
  } catch (error) {
    console.error('加载章节列表失败', error)
    formChapterOptions.value = []
  }
}

const handleCourseChange = (courseId) => {
  searchForm.chapterId = null
  loadChapters(courseId)
}

const handleFormCourseChange = (courseId) => {
  formData.chapterId = null
  loadFormChapters(courseId)
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    courseId: null,
    chapterId: null,
    title: ''
  })
  chapterOptions.value = []
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
    chapterId: null,
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
    Object.assign(formData, res.data)
    
    // 加载对应课程的章节列表
    if (formData.courseId) {
      await loadFormChapters(formData.courseId)
    }
    
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
    message.error('删除失败')
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
    if (!fileUrl) return;
    
    // 从URL中提取文件路径，例如：http://localhost:9900/mxng/video/xxx.mp4 -> mxng/video/xxx.mp4
    const url = new URL(fileUrl);
    const pathUrl = url.pathname.substring(1); // 移除开头的 '/'
    
    const response = await fetch(`/api/public/files/delete?pathUrl=${encodeURIComponent(pathUrl)}`, {
      method: 'DELETE'
    });
    
    if (!response.ok) {
      throw new Error(`删除失败: ${response.status}`);
    }
    
    return true;
  } catch (error) {
    console.error('删除文件失败:', error);
    throw error;
  }
};

const handleRemoveVideo = async (file) => {
  try {
    // 如果有文件URL，调用删除接口
    if (file.url || formData.videoUrl) {
      const fileUrl = file.url || formData.videoUrl;
      await deleteFileFromServer(fileUrl);
      message.success('视频删除成功');
    }
    
    formData.videoUrl = ''
    videoFileList.value = []
    return true
  } catch (error) {
    console.error('删除视频失败:', error);
    message.error('删除视频失败');
    return false;
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

// 切换小节状态
const toggleStatus = async (row) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await api.updateStatus(row.id, newStatus)
    message.success(`小节已${newStatus === 1 ? '发布' : '下线'}`)
    await loadData()
  } catch (error) {
    console.error('状态更新失败:', error)
    message.error('状态更新失败')
  }
}

// 查看小节统计
const viewStatistics = async (courseId) => {
  try {
    const res = await api.getSectionStatistics(courseId)
    const stats = res?.data || {}
    message.info(`课程统计：总小节 ${stats.totalSections || 0} 个，已发布 ${stats.publishedSections || 0} 个`)
  } catch (error) {
    console.error('获取统计失败:', error)
    message.error('获取统计失败')
  }
}

// 生命周期
onMounted(async () => {
  await loadCourses()
  
  // 检查是否从章节管理页面跳转过来
  const { chapterId, chapterTitle, courseId, courseName } = route.query
  
  if (chapterId && courseId) {
    // 设置搜索条件
    searchForm.courseId = Number(courseId)
    searchForm.chapterId = Number(chapterId)
    
    // 将跳转过来的课程和章节信息也加入映射
    if (courseId && courseName) {
      courseNameMap.value[Number(courseId)] = courseName
    }
    if (chapterId && chapterTitle) {
      chapterNameMap.value[Number(chapterId)] = chapterTitle
    }
    
    // 加载对应课程的章节列表
    await loadChapters(Number(courseId))
    
    // 显示提示信息
    if (chapterTitle && courseName) {
      message.success(`已定位到：${courseName} - ${chapterTitle}`)
    }
    
    // 自动搜索
    handleSearch()
  } else {
    loadData()
  }
})
</script>

<style scoped>
.teacher-section-management {
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