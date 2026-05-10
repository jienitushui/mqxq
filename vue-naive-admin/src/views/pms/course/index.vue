<template>
  <div class="course-management h-full flex flex-col">
    <PageHeader class="flex-shrink-0">
      <template #title>课程管理</template>
      <template #extra>
        <n-button 
          @click="handleBatchDelete" 
          :disabled="!selectedRowKeys.length" 
          type="error" 
          style="margin-right: 8px"
        >
          <template #icon>
            <i class="i-carbon:trash-can" />
          </template>
          批量删除
        </n-button>
        <n-button type="primary" @click="handleAdd">
          <template #icon>
            <i class="i-carbon:add" />
          </template>
          新增课程
        </n-button>
      </template>
    </PageHeader>

    <n-card class="flex-1 flex flex-col overflow-hidden">
      <!-- 搜索区域 -->
      <div class="flex-shrink-0 mb-4">
        <n-form inline :model="searchForm" label-placement="left">
          <n-form-item label="课程标题">
            <n-input 
              v-model:value="searchForm.title" 
              placeholder="请输入课程标题" 
              clearable 
              @keyup.enter="handleSearch"
            />
          </n-form-item>
          <n-form-item label="教师">
            <n-input
              :value="getTeacherName(searchForm.teacherId)"
              placeholder="请选择教师"
              readonly
              clearable
              @click="openTeacherSelector('search')"
              @clear="searchForm.teacherId = null"
              style="width: 150px; cursor: pointer"
            >
              <template #suffix>
                <i class="i-carbon:search" />
              </template>
            </n-input>
          </n-form-item>
          <n-form-item label="学科">
            <n-tree-select
              v-model:value="searchForm.subjectId"
              placeholder="请选择学科"
              :options="subjectOptions"
              clearable
              key-field="id"
              label-field="name"
              children-field="children"
              style="width: 200px"
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
          :row-key="(row) => row.id"
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
          :single-line="false"
        >
          <template #empty>
            <n-empty description="暂无数据" />
          </template>
        </n-data-table>
      </div>
    </n-card>

    <!-- 教师选择弹窗 -->
    <n-modal v-model:show="showTeacherModal" :mask-closable="false">
      <n-card
        style="width: 800px"
        title="选择教师"
        :bordered="false"
        size="huge"
        role="dialog"
        aria-modal="true"
      >
        <template #header-extra>
          <n-button quaternary circle @click="showTeacherModal = false">
            <template #icon>
              <i class="i-carbon:close" />
            </template>
          </n-button>
        </template>
        
        <!-- 搜索区域 -->
        <div class="mb-4">
          <n-form inline>
            <n-form-item label="教师姓名">
              <n-input
                v-model:value="teacherSearch.keyword"
                placeholder="请输入教师姓名"
                clearable
                @keyup.enter="handleTeacherSearch"
                style="width: 200px"
              />
            </n-form-item>
            <n-form-item>
              <n-button type="primary" @click="handleTeacherSearch">
                <template #icon>
                  <i class="i-carbon:search" />
                </template>
                搜索
              </n-button>
              <n-button @click="handleTeacherReset" style="margin-left: 8px">
                <template #icon>
                  <i class="i-carbon:reset" />
                </template>
                重置
              </n-button>
            </n-form-item>
          </n-form>
        </div>

        <!-- 教师列表 -->
        <n-data-table
          :columns="teacherColumns"
          :data="teacherList"
          :loading="teacherLoading"
          :pagination="teacherPagination"
          :row-key="row => row.id"
          @update:page="handleTeacherPageChange"
          @update:page-size="handleTeacherPageSizeChange"
          remote
          :max-height="400"
          striped
          size="small"
        >
          <template #empty>
            <n-empty description="暂无教师数据" />
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
        >
          <n-form-item label="课程标题" path="title">
            <n-input v-model:value="formData.title" placeholder="请输入课程标题" />
          </n-form-item>
          <n-form-item label="课程描述" path="description">
            <n-input
              v-model:value="formData.description"
              type="textarea"
              placeholder="请输入课程描述"
              :rows="3"
            />
          </n-form-item>
          <n-form-item label="课程分类" path="subjectId">
            <n-tree-select
              v-model:value="formData.subjectId"
              placeholder="请选择课程分类"
              :options="subjectOptions"
              key-field="id"
              label-field="name"
              children-field="children"
              clearable
            />
          </n-form-item>
          <n-form-item label="授课教师" path="teacherId">
            <n-input
              :value="getTeacherName(formData.teacherId)"
              placeholder="请选择授课教师"
              readonly
              @click="openTeacherSelector('form')"
              style="cursor: pointer"
            >
              <template #suffix>
                <i class="i-carbon:search" />
              </template>
            </n-input>
          </n-form-item>
          <n-form-item label="课程价格" path="price">
            <n-input-number
              v-model:value="formData.price"
              placeholder="请输入课程价格"
              :min="0"
              :precision="2"
            />
          </n-form-item>
          <n-form-item label="总课时" path="lessonNum">
            <n-input-number
              v-model:value="formData.lessonNum"
              placeholder="请输入总课时"
              :min="1"
            />
          </n-form-item>
          <n-form-item label="视频总时长" path="durationSum">
            <n-input-number
              v-model:value="formData.durationSum"
              placeholder="请输入视频总时长(秒)"
              :min="0"
            />
          </n-form-item>
          <n-form-item label="封面图片" path="cover">
            <n-upload
              v-model:file-list="coverFileList"
              action="/api/public/files/image"
              :max="1"
              list-type="image-card"
              accept="image/*"
              :show-file-list="true"
              @before-upload="handleBeforeUpload"
              @finish="handleUploadFinish"
              @remove="handleRemoveImage"
              @error="handleUploadError"
            >
              <n-button v-if="coverFileList.length === 0">
                <template #icon>
                  <i class="i-carbon:cloud-upload" />
                </template>
                上传封面图片
              </n-button>
            </n-upload>
            <div v-if="formData.cover" class="text-xs text-gray-500 mt-1">
              当前封面: {{ formData.cover }}
            </div>
          </n-form-item>
        </n-form>

        <template #footer>
          <div style="display: flex; justify-content: flex-end; gap: 12px">
            <n-button @click="showModal = false">取消</n-button>
            <n-button type="primary" @click="() => { console.log('🔥 确定按钮被点击了!'); handleSubmit(); }" :loading="submitting">
              确定
            </n-button>
          </div>
        </template>
      </n-card>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, h, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { NButton, NTag, NPopconfirm, useMessage } from 'naive-ui'
import { PageHeader } from '@/components'
import api from './api'

const message = useMessage()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const showModal = ref(false)
const formRef = ref(null)
const tableData = ref([])
const selectedRowKeys = ref([])
const coverFileList = ref([])

// 教师选择弹窗相关
const showTeacherModal = ref(false)
const teacherLoading = ref(false)
const teacherList = ref([])
const teacherSelectorType = ref('search') // 'search' 或 'form'

// 教师搜索表单
const teacherSearch = reactive({
  keyword: ''
})

// 教师分页配置
const teacherPagination = reactive({
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
  title: '',
  teacherId: null,
  status: null
})

// 表单数据
const formData = reactive({
  id: null,
  title: '',
  description: '',
  subjectId: null,
  teacherId: null,
  price: 0,
  lessonNum: 0,
  durationSum: 0,
  cover: ''
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

// 选项数据
const statusOptions = [
  { label: '未发布', value: 0 },
  { label: '已发布', value: 1 }
]

const teacherOptions = ref([]) // 用于缓存已选择的教师信息
const subjectOptions = ref([])

// 教师表格列配置
const teacherColumns = [
  {
    title: 'ID',
    key: 'id',
    width: 80
  },
  {
    title: '教师姓名',
    key: 'name',
    render(row) {
      return row.name || row.username || '-'
    }
  },
  {
    title: '用户名',
    key: 'username',
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
          onClick: () => handleSelectTeacher(row)
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
  { title: 'ID', key: 'id', width: 80 },
  { title: '课程标题', key: 'title', ellipsis: { tooltip: true } },
  { title: '教师ID', key: 'teacherId', width: 100 },
  { title: '分类', key: 'subjectName' },
  { 
    title: '价格', 
    key: 'price',
    width: 100,
    render: (row) => `¥${row.price}`
  },
  { 
    title: '课时数', 
    key: 'lessonNum',
    width: 100,
    render: (row) => row.lessonNum || 0
  },
  { 
    title: '销售数', 
    key: 'buyCount',
    width: 100,
    render: (row) => row.buyCount || 0
  },
  { 
    title: '浏览量', 
    key: 'viewCount',
    width: 100,
    render: (row) => row.viewCount || 0
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) => {
      const status = row.status
      return h(NTag, {
        type: status === 1 ? 'success' : 'warning'
      }, {
        default: () => status === 1 ? '已发布' : '未发布'
      })
    }
  },
  { 
    title: '发布时间', 
    key: 'publishTime',
    width: 180,
    render: (row) => {
      if (!row.publishTime) return '-'
      return new Date(row.publishTime).toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
  },
  { 
    title: '创建时间', 
    key: 'createTime',
    width: 180,
    render: (row) => {
      if (!row.createTime) return '-'
      return new Date(row.createTime).toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 260,
    render: (row) => {
      return h('div', { class: 'flex space-x-2' }, [
        h(NButton, {
          size: 'small',
          type: 'primary',
          secondary: true,
          onClick: () => handleEdit(row)
        }, { default: () => '编辑' }),
        
        h(NButton, {
          size: "small",
          type: "info",
          secondary: true,
          onClick: () => handleManageChapters(row),
        }, { default: () => "章节" }),
        
        h(NButton, {
          size: 'small',
          type: row.status === 1 ? 'warning' : 'success',
          secondary: true,
          onClick: () => handleToggleStatus(row)
        }, { 
          default: () => row.status === 1 ? '取消发布' : '发布'
        }),
        
        h(NPopconfirm, {
          onPositiveClick: () => handleDelete(row.id)
        }, {
          default: () => '确定删除这个课程吗？',
          trigger: () => h(NButton, {
            size: 'small',
            type: 'error',
            secondary: true
          }, { default: () => '删除' })
        })
      ])
    }
  }
]

// 表单验证规则
const formRules = {
  title: [
    { required: true, message: '请输入课程标题', trigger: 'blur' },
    {
      min: 2,
      max: 100,
      message: '课程标题长度应在2-100个字符之间',
      trigger: 'blur',
    },
  ],
  description: [
    { required: true, message: '请输入课程描述', trigger: 'blur' },
    {
      min: 10,
      max: 500,
      message: '课程描述长度应在10-500个字符之间',
      trigger: 'blur',
    },
  ],
  subjectId: [
    {
      required: true,
      message: '请选择课程分类',
      trigger: 'change',
      type: 'number',
    },
  ],
  teacherId: [
    {
      required: true,
      message: '请选择授课教师',
      trigger: 'change',
      type: 'number',
    },
  ],
  price: [
    {
      required: true,
      message: '请输入课程价格',
      trigger: 'blur',
      type: 'number',
    },
    { type: 'number', min: 0, message: '课程价格不能小于0', trigger: 'blur' },
  ],
  lessonNum: [
    {
      required: true,
      message: '请输入总课时',
      trigger: 'blur',
      type: 'number',
    },
    { type: 'number', min: 1, message: '总课时不能小于1', trigger: 'blur' },
  ],
  durationSum: [
    { type: 'number', min: 0, message: '视频总时长不能小于0', trigger: 'blur' },
  ],
}

// 计算属性
const modalTitle = computed(() => {
  return formData.id ? '编辑课程' : '新增课程'
})

// 方法
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.pageSize,
      ...searchForm
    }
    console.log('请求参数:', params)
    
    const response = await api.getPage(params)
    console.log('API响应数据:', response)
    
    const data = response?.data || response || {}
    let courses = data.list || data.records || []
    const total = data.total || data.totalCount || 0
    
    // 处理分类名称显示
    console.log('🏷️ 开始处理分类名称显示')
    console.log('课程数量:', courses.length)
    console.log('分类选项数量:', subjectOptions.value.length)
    console.log('分类选项数据:', subjectOptions.value)
    
    if (courses.length > 0 && subjectOptions.value.length > 0) {
      // 递归查找分类名称（支持多级分类）
      const findCategoryName = (categoryId, categories) => {
        for (const category of categories) {
          // 检查当前层级
          if (category.id === categoryId || category.value === categoryId) {
            console.log(`✅ 找到分类匹配: ID=${categoryId}, 名称=${category.name || category.label}`)
            return category.name || category.label
          }
          // 递归检查子分类
          if (category.children && category.children.length > 0) {
            const found = findCategoryName(categoryId, category.children)
            if (found) return found
          }
        }
        return null
      }
      
      courses = courses.map(course => {
        const subjectName = findCategoryName(course.subjectId, subjectOptions.value) || '未知分类'
        console.log(`📚 课程 "${course.title}" (ID: ${course.id}) 的分类ID: ${course.subjectId}, 分类名称: ${subjectName}`)
        return {
          ...course,
          subjectName
        }
      })
    } else {
      console.log('⚠️ 跳过分类名称处理：课程数据或分类数据为空')
      courses = courses.map(course => ({
        ...course,
        subjectName: '加载中...'
      }))
    }
    
    tableData.value = courses
    pagination.itemCount = total
    
    console.log('分页信息:', {
      current: pagination.page,
      pageSize: pagination.pageSize,
      total: total,
      totalPages: Math.ceil(total / pagination.pageSize),
      dataLength: courses.length,
      shouldShowPagination: total > 0
    })
    
    // 强制更新分页显示
    if (total > pagination.pageSize) {
      console.log('应该显示分页控件，总数:', total, '每页:', pagination.pageSize)
    }
    
    // 确保分页数据正确更新
    await nextTick()
    console.log('分页配置已更新:', pagination)
    
  } catch (error) {
    message.error('加载数据失败')
    console.error(error)
    tableData.value = []
    pagination.itemCount = 0
  } finally {
    loading.value = false
  }
}

// 加载教师列表（用于弹窗选择）
const loadTeacherList = async () => {
  teacherLoading.value = true
  try {
    const params = {
      pageNum: teacherPagination.page,
      pageSize: teacherPagination.pageSize,
      keyword: teacherSearch.keyword || ''
    }
    const res = await api.getTeachers(params)
    const data = res?.data || res || {}
    teacherList.value = data.list || data.records || []
    teacherPagination.itemCount = data.total || data.totalCount || 0
    
    // 同时更新 teacherOptions 用于显示名称
    teacherList.value.forEach(teacher => {
      const existingOption = teacherOptions.value.find(opt => opt.value === teacher.id)
      if (!existingOption) {
        teacherOptions.value.push({
          label: teacher.name || teacher.username,
          value: teacher.id
        })
      }
    })
  } catch (error) {
    console.error('加载教师列表失败', error)
    message.error('加载教师列表失败')
    teacherList.value = []
    teacherPagination.itemCount = 0
  } finally {
    teacherLoading.value = false
  }
}

// 打开教师选择器
const openTeacherSelector = (type) => {
  teacherSelectorType.value = type
  teacherSearch.keyword = ''
  teacherPagination.page = 1
  showTeacherModal.value = true
  loadTeacherList()
}

// 选择教师
const handleSelectTeacher = (teacher) => {
  if (teacherSelectorType.value === 'search') {
    searchForm.teacherId = teacher.id
  } else {
    formData.teacherId = teacher.id
  }
  
  // 更新 teacherOptions
  const existingOption = teacherOptions.value.find(opt => opt.value === teacher.id)
  if (!existingOption) {
    teacherOptions.value.push({
      label: teacher.name || teacher.username,
      value: teacher.id
    })
  }
  
  showTeacherModal.value = false
  message.success(`已选择教师：${teacher.name || teacher.username}`)
}

// 获取教师名称
const getTeacherName = (teacherId) => {
  if (!teacherId) return ''
  const teacher = teacherOptions.value.find(opt => opt.value === teacherId)
  return teacher ? teacher.label : ''
}

// 教师搜索
const handleTeacherSearch = () => {
  teacherPagination.page = 1
  loadTeacherList()
}

// 教师搜索重置
const handleTeacherReset = () => {
  teacherSearch.keyword = ''
  teacherPagination.page = 1
  loadTeacherList()
}

// 教师分页
const handleTeacherPageChange = (page) => {
  teacherPagination.page = page
  loadTeacherList()
}

const handleTeacherPageSizeChange = (pageSize) => {
  teacherPagination.pageSize = pageSize
  teacherPagination.page = 1
  loadTeacherList()
}

const loadSubjects = async () => {
  try {
    console.log('=== 开始加载课程分类 ===')
    const res = await api.getCategories()
    console.log('分类API原始响应:', res)
    
    // 根据实际API响应结构获取数据
    let subjects = []
    if (res?.data) {
      subjects = Array.isArray(res.data) ? res.data : []
    } else if (Array.isArray(res)) {
      subjects = res
    }
    
    console.log('处理后的分类数据:', subjects)

    // 转换数据格式以适配 n-tree-select
    const formatSubjects = (items) => {
      return items.map((item) => ({
        id: item.id,
        name: item.name,
        value: item.id,
        label: item.name,
        children: item.children && item.children.length > 0 ? formatSubjects(item.children) : [],
      }))
    }

    subjectOptions.value = formatSubjects(subjects)
    console.log('最终分类选项:', subjectOptions.value)

    if (subjectOptions.value.length === 0) {
      message.warning('暂无课程分类数据，请联系管理员')
    } else {
      console.log(`成功加载 ${subjectOptions.value.length} 个分类`)
    }
  } catch (error) {
    console.error('加载分类列表失败', error)
    message.error('加载课程分类失败')
    subjectOptions.value = []
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    title: '',
    teacherId: null,
    status: null
  })
  handleSearch()
}

const handlePageChange = (page) => {
  console.log('分页变化:', page)
  pagination.page = page
  loadData()
}

const handlePageSizeChange = (pageSize) => {
  console.log('页面大小变化:', pageSize)
  pagination.pageSize = pageSize
  pagination.page = 1
  loadData()
}

const handleAdd = () => {
  // 检查是否有分类数据
  if (subjectOptions.value.length === 0) {
    message.error('暂无课程分类数据，无法创建课程')
    return
  }

  Object.assign(formData, {
    id: null,
    title: '',
    description: '',
    subjectId: null,
    teacherId: null,
    price: 0,
    cover: '',
    lessonNum: 1,
    durationSum: 0,
  })
  coverFileList.value = []
  showModal.value = true
}

const handleEdit = async (row) => {
  try {
    console.log('🔍 开始获取课程详情，课程ID:', row.id)
    
    // 显示加载状态
    loading.value = true
    
    // 调用API获取课程详情
    const response = await api.getDetail(row.id)
    console.log('📋 课程详情API响应:', response)
    
    // 处理响应数据
    const courseDetail = response?.data || response || {}
    console.log('📝 课程详情数据:', courseDetail)
    
    // 将详情数据填充到表单中
    Object.assign(formData, {
      id: courseDetail.id || row.id,
      title: courseDetail.title || '',
      description: courseDetail.description || '',
      subjectId: courseDetail.subjectId || null,
      price: courseDetail.price || 0,
      lessonNum: courseDetail.lessonNum || 0,
      durationSum: courseDetail.durationSum || 0,
      cover: courseDetail.cover || '',
      teacherId: courseDetail.teacherId || null
    })
    
    console.log('✅ 表单数据已更新:', formData)
    
    // 如果有封面图片，设置到文件列表中
    if (formData.cover) {
      coverFileList.value = [
        {
          id: 'current-cover',
          name: '当前封面图片',
          status: 'finished',
          url: formData.cover,
          thumbnailUrl: formData.cover
        }
      ]
    } else {
      coverFileList.value = []
    }
    
    // 显示编辑弹窗
    showModal.value = true
    
    message.success('课程详情加载成功')
    
  } catch (error) {
    console.error('❌ 获取课程详情失败:', error)
    message.error('获取课程详情失败，请重试')
    
    // 如果获取详情失败，仍然可以使用表格数据进行编辑
    console.log('🔄 使用表格数据作为备选方案')
    Object.assign(formData, { ...row })
    
    if (formData.cover) {
      coverFileList.value = [
        {
          id: 'current-cover',
          name: '当前封面图片',
          status: 'finished',
          url: formData.cover,
          thumbnailUrl: formData.cover
        }
      ]
    } else {
      coverFileList.value = []
    }
    
    showModal.value = true
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  console.log('🚀 handleSubmit 函数被调用了!')
  console.log('=== 开始提交表单 ===')
  console.log('1. 当前表单数据:', JSON.stringify(formData, null, 2))
  console.log('2. 表单引用:', formRef.value)
  
  try {
    // 先进行手动验证，确保数据完整
    console.log('3. 开始手动验证...')
    
    if (!formData.title || formData.title.trim() === '') {
      console.log('❌ 验证失败: 课程标题为空')
      message.error('请输入课程标题')
      return
    }
    console.log('✅ 课程标题验证通过:', formData.title)
    
    if (!formData.description || formData.description.trim() === '') {
      console.log('❌ 验证失败: 课程描述为空')
      message.error('请输入课程描述')
      return
    }
    console.log('✅ 课程描述验证通过:', formData.description)
    
    if (!formData.teacherId) {
      console.log('❌ 验证失败: 教师未选择')
      message.error('请选择教师')
      return
    }
    console.log('✅ 教师验证通过:', formData.teacherId)
    
    if (!formData.subjectId) {
      console.log('❌ 验证失败: 课程分类未选择')
      console.log('当前 subjectId 值:', formData.subjectId)
      console.log('可用分类选项:', subjectOptions.value)
      message.error('请选择课程分类')
      return
    }
    console.log('✅ 课程分类验证通过:', formData.subjectId)
    
    if (formData.price === null || formData.price === undefined || formData.price === '') {
      console.log('❌ 验证失败: 课程价格为空')
      console.log('当前 price 值:', formData.price, '类型:', typeof formData.price)
      message.error('请输入课程价格')
      return
    }
    console.log('✅ 课程价格验证通过:', formData.price)
    
    if (formData.lessonNum === null || formData.lessonNum === undefined || formData.lessonNum === '') {
      console.log('❌ 验证失败: 总课时为空')
      console.log('当前 lessonNum 值:', formData.lessonNum, '类型:', typeof formData.lessonNum)
      message.error('请输入总课时')
      return
    }
    console.log('✅ 总课时验证通过:', formData.lessonNum)
    
    console.log('4. 手动验证全部通过，开始表单验证...')
    
    // 然后进行表单验证
    try {
      await formRef.value?.validate()
      console.log('✅ 表单验证通过')
    } catch (validationError) {
      console.log('❌ 表单验证失败:', validationError)
      throw validationError
    }
    
    console.log('5. 开始准备提交数据...')
    submitting.value = true
    
    // 准备提交的数据
    const submitData = {
      title: formData.title?.trim(),
      description: formData.description?.trim(),
      subjectId: formData.subjectId,
      teacherId: formData.teacherId,
      price: Number(formData.price),
      lessonNum: Number(formData.lessonNum),
      durationSum: Number(formData.durationSum || 0),
      cover: formData.cover?.trim() || "",
    }
    
    console.log('6. 最终提交数据:', JSON.stringify(submitData, null, 2))
    
    console.log('7. 开始调用API...')
    if (formData.id) {
      console.log('调用更新API:', formData.id)
      const result = await api.update(formData.id, submitData)
      console.log('更新API响应:', result)
      message.success('更新成功')
    } else {
      console.log('调用创建API')
      const result = await api.create(submitData)
      console.log('创建API响应:', result)
      message.success('创建成功')
    }
    
    console.log('8. API调用成功，关闭弹窗并刷新数据')
    showModal.value = false
    loadData()
    console.log('=== 表单提交完成 ===')
    
  } catch (error) {
    console.log('❌ 提交过程中发生错误:')
    console.error('错误详情:', error)
    console.log('错误类型:', typeof error)
    console.log('错误消息:', error?.message)
    console.log('错误响应:', error?.response)
    console.log('错误响应数据:', error?.response?.data)
    
    if (error.response && error.response.data) {
      console.log('显示后端错误信息:', error.response.data.message)
      message.error(error.response.data.message || '操作失败')
    } else if (error.message) {
      console.log('显示错误信息:', error.message)
      message.error(error.message)
    } else {
      console.log('显示默认错误信息')
      message.error('操作失败')
    }
  } finally {
    console.log('9. 重置提交状态')
    submitting.value = false
  }
}

const handleToggleStatus = async (row) => {
  try {
    const action = row.status === 1 ? '取消发布' : '发布'
    
    if (row.status === 1) {
      await api.unpublish(row.id)
    } else {
      await api.publish(row.id)
    }
    
    message.success(`${action}成功`)
    loadData()
  } catch (error) {
    message.error('操作失败')
    console.error(error)
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

const handleManageChapters = (row) => {
  // 跳转到章节管理页面，带着课程信息
  try {
    // 使用 router.push 进行跳转
    router
      .push({
        path: "/pms/chapter",
        query: {
          courseId: row.id,
          courseName: row.title,
        },
      })
      .catch((err) => {
        console.error("路由跳转错误:", err)
        // 如果路由跳转失败，使用 window.location 作为备选方案
        const url = `#/pms/chapter?courseId=${
          row.id
        }&courseName=${encodeURIComponent(row.title)}`
        window.location.href = url
      })
  } catch (error) {
    console.error("跳转失败:", error)
    message.error("跳转失败，请稍后重试")
  }
}

const handleBatchDelete = async () => {
  if (!selectedRowKeys.value || selectedRowKeys.value.length === 0) {
    message.warning('请选择要删除的课程')
    return
  }
  
  try {
    await Promise.all(selectedRowKeys.value.map(id => api.delete(id)))
    message.success(`成功删除 ${selectedRowKeys.value.length} 个课程`)
    selectedRowKeys.value = []
    loadData()
  } catch (error) {
    message.error('批量删除失败')
    console.error(error)
  }
}

// 图片上传相关方法
const handleBeforeUpload = (data) => {
  // data.file 是 UploadFileInfo 对象, data.file.file 才是原生 File 对象
  const rawFile = data.file.file

  if (!rawFile) {
    message.error('无法读取文件，请重试')
    return false
  }

  const isImage = rawFile.type?.startsWith("image/")
  if (!isImage) {
    message.error("只能上传图片文件")
    return false
  }

  // 检查文件大小
  if (rawFile.size === undefined || rawFile.size === null) {
    message.error("无法获取文件大小，请重新选择文件")
    return false
  }

  const isLt5M = rawFile.size / 1024 / 1024 < 5
  if (!isLt5M) {
    message.error("图片大小不能超过5MB")
    return false
  }

  return true
}

const handleUploadFinish = ({ file, event }) => {
  try {
    // 接口直接返回 URL 字符串，不需要 JSON.parse
    const imageUrl = event.target.response
    console.log("上传响应:", imageUrl)
    if (imageUrl && typeof imageUrl === 'string' && imageUrl.startsWith('http')) {
      formData.cover = imageUrl
      message.success("图片上传成功")
      file.status = 'finished'
      file.url = imageUrl
    } else {
      console.error("无效的图片URL:", imageUrl)
      message.error("上传成功但未获取到有效的图片地址")
    }
  } catch (error) {
    console.error("上传处理失败:", error)
    message.error("图片上传失败")
  }
}

// 通用文件删除函数
const deleteFileFromServer = async (fileUrl) => {
  try {
    if (!fileUrl) return
    
    // 从URL中提取文件路径，例如：http://localhost:9900/mxng/png/xxx.png -> mxng/png/xxx.png
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

const handleRemoveImage = async (file) => {
  try {
    // 如果有文件URL，调用删除接口
    if (file.url || formData.cover) {
      const fileUrl = file.url || formData.cover
      await deleteFileFromServer(fileUrl)
      message.success('图片删除成功')
    }
    
    formData.cover = ""
    coverFileList.value = []
    return true
  } catch (error) {
    console.error('删除图片失败:', error)
    message.error('删除图片失败')
    return false
  }
}

const handleUploadError = ({ file, event }) => {
  console.error("上传失败:", event)
  message.error("图片上传失败，请重试")
  
  // 移除失败的文件
  const index = coverFileList.value.findIndex(f => f.id === file.id)
  if (index > -1) {
    coverFileList.value.splice(index, 1)
  }
}

// 监听表单数据变化
watch(() => formData.subjectId, (newVal, oldVal) => {
  console.log('📝 课程分类变化:', { 旧值: oldVal, 新值: newVal })
})

watch(() => formData.price, (newVal, oldVal) => {
  console.log('💰 课程价格变化:', { 旧值: oldVal, 新值: newVal })
})

watch(() => formData.lessonNum, (newVal, oldVal) => {
  console.log('⏰ 总课时变化:', { 旧值: oldVal, 新值: newVal })
})

// 生命周期
onMounted(async () => {
  console.log('📱 课程管理页面已挂载')
  console.log('初始表单数据:', formData)
  
  // 先加载基础数据，再加载课程数据
  await loadSubjects()
  
  // 基础数据加载完成后再加载课程数据
  loadData()
})
</script>

<style scoped>
/* 页面容器 */
.course-management {
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