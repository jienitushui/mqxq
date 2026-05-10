<template>
  <div class="carousel-management h-full flex flex-col">
    <PageHeader class="flex-shrink-0">
      <template #title>轮播图管理</template>
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
          新增轮播图
        </n-button>
      </template>
    </PageHeader>

    <n-card class="flex-1 flex flex-col overflow-hidden">
      <!-- 搜索区域 -->
      <div class="flex-shrink-0 mb-4">
        <n-form inline :model="searchForm" label-placement="left">
          <n-form-item label="图片地址">
            <n-input
              v-model:value="searchForm.carouselUrl"
              placeholder="请输入图片地址"
              clearable
              @keyup.enter="handleSearch"
            />
          </n-form-item>
          <n-form-item label="链接地址">
            <n-input
              v-model:value="searchForm.linkUrl"
              placeholder="请输入链接地址"
              clearable
              @keyup.enter="handleSearch"
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
    </n-card>

    <!-- 查看详情弹窗 -->
    <n-modal v-model:show="showViewModal" :mask-closable="false">
      <n-card
        style="width: 700px"
        title="轮播图详情"
        :bordered="false"
        size="huge"
        role="dialog"
        aria-modal="true"
      >
        <template #header-extra>
          <n-button quaternary circle @click="showViewModal = false">
            <template #icon>
              <i class="i-carbon:close" />
            </template>
          </n-button>
        </template>
        
        <div class="space-y-4">
          <div class="grid grid-cols-2 gap-4">
            <div>
              <span class="font-semibold text-gray-600">ID：</span>
              <div class="mt-1">{{ viewData.id }}</div>
            </div>
            <div>
              <span class="font-semibold text-gray-600">排序值：</span>
              <div class="mt-1">{{ viewData.sort }}</div>
            </div>
          </div>
          
          <div>
            <span class="font-semibold text-gray-600">图片地址：</span>
            <div class="mt-1 break-all">{{ viewData.carouselUrl }}</div>
          </div>
          
          <div>
            <span class="font-semibold text-gray-600">链接地址：</span>
            <div class="mt-1 break-all">{{ viewData.linkUrl || '无链接' }}</div>
          </div>
          
          <div class="grid grid-cols-2 gap-4">
            <div>
              <span class="font-semibold text-gray-600">创建时间：</span>
              <div class="mt-1">{{ formatDateTime(viewData.createTime) }}</div>
            </div>
            <div>
              <span class="font-semibold text-gray-600">更新时间：</span>
              <div class="mt-1">{{ formatDateTime(viewData.updateTime) }}</div>
            </div>
          </div>
          
          <div class="mt-6">
            <div class="font-semibold text-gray-600 mb-3">图片预览：</div>
            <div class="flex justify-center">
              <img 
                :src="viewData.carouselUrl" 
                alt="轮播图预览"
                class="max-w-full max-h-64 rounded-lg border shadow-sm"
                @error="handleImageError"
              />
            </div>
          </div>
        </div>

        <template #footer>
          <div class="flex justify-end">
            <n-button @click="showViewModal = false">关闭</n-button>
          </div>
        </template>
      </n-card>
    </n-modal>

    <!-- 新增/编辑弹窗 -->
    <n-modal v-model:show="showModal" :mask-closable="false">
      <n-card
        style="width: 800px"
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
          <n-form-item label="图片上传" path="carouselUrl">
            <div class="space-y-3">
              <n-upload
                v-model:file-list="imageFileList"
                action="/api/public/files/image"
                :show-file-list="true"
                :max="1"
                list-type="image-card"
                accept=".png,.jpg,.jpeg,.gif,.webp"
                @before-upload="handleBeforeUpload"
                @finish="handleUploadFinish"
                @remove="handleRemoveImage"
                @error="handleUploadError"
              >
                <n-button>
                  <template #icon>
                    <i class="i-carbon:cloud-upload" />
                  </template>
                  上传图片
                </n-button>
              </n-upload>
              
              <div class="text-xs text-gray-500">
                支持 PNG、JPG、JPEG、GIF、WebP 格式，建议尺寸 1920x600
              </div>
              
              <!-- 手动输入图片地址 -->
              <n-divider>或手动输入图片地址</n-divider>
              <n-input 
                v-model:value="formData.carouselUrl" 
                placeholder="请输入图片地址"
                @blur="handleImagePreview"
              />
            </div>
          </n-form-item>
          
          <n-form-item label="链接地址" path="linkUrl">
            <n-input 
              v-model:value="formData.linkUrl" 
              placeholder="请输入链接地址（可选）" 
            />
          </n-form-item>
          
          <n-form-item label="排序值" path="sort">
            <n-input-number 
              v-model:value="formData.sort" 
              placeholder="请输入排序值"
              :min="0"
              :max="9999"
              style="width: 100%"
            />
          </n-form-item>
          
          <!-- 图片预览 -->
          <n-form-item label="图片预览" v-if="imagePreviewUrl">
            <div class="flex justify-center w-full">
              <img 
                :src="imagePreviewUrl" 
                alt="图片预览"
                class="max-w-full max-h-48 rounded-lg border shadow-sm"
                @error="handleImageError"
              />
            </div>
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
import { 
  NButton, 
  // NTag, 
  NPopconfirm, 
  useMessage,
  NCard,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NDataTable,
  NModal,
  // NImage,
  NUpload,
  NDivider
} from 'naive-ui'

import { PageHeader } from '@/components'

// 导入API
import api from './api'

const message = useMessage()

// 响应式数据
const loading = ref(false)
const showModal = ref(false)
const showViewModal = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const formRef = ref(null)
const selectedRowKeys = ref([])
const imagePreviewUrl = ref('')
const imageFileList = ref([])

// 查看详情数据
const viewData = ref({})

// 搜索表单
const searchForm = reactive({
  carouselUrl: '',
  linkUrl: ''
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
  carouselUrl: '',
  linkUrl: '',
  sort: 0
})

// 表单验证规则
const formRules = {
  carouselUrl: [
    { required: true, message: '请输入图片地址', trigger: 'blur' },
    { 
      pattern: /^https?:\/\/.+\.(jpg|jpeg|png|gif|webp)$/i, 
      message: '请输入有效的图片地址', 
      trigger: 'blur' 
    }
  ],
  sort: [
    { required: true, message: '请输入排序值', trigger: 'blur', type: 'number' },
    { type: 'number', min: 0, max: 9999, message: '排序值必须在0-9999之间', trigger: 'blur' }
  ]
}

// 计算属性
const modalTitle = computed(() => formData.id ? '编辑轮播图' : '新增轮播图')

// 时间格式化函数
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  
  try {
    const date = new Date(dateTime)
    if (isNaN(date.getTime())) return dateTime
    
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  } catch (error) {
    return dateTime
  }
}

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
    title: '图片预览',
    key: 'carouselUrl',
    width: 120,
    render(row) {
      return h('img', {
        src: row.carouselUrl,
        alt: '轮播图',
        style: 'width: 80px; height: 45px; object-fit: cover; border-radius: 4px; border: 1px solid #e0e0e0;',
        onError: (e) => {
          e.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iODAiIGhlaWdodD0iNDUiIHZpZXdCb3g9IjAgMCA4MCA0NSIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHJlY3Qgd2lkdGg9IjgwIiBoZWlnaHQ9IjQ1IiBmaWxsPSIjRjVGNUY1Ii8+CjxwYXRoIGQ9Ik0zNSAyMEg0NVYyNUgzNVYyMFoiIGZpbGw9IiNEOUQ5RDkiLz4KPHA+PGZvbnQgZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIxMCIgZmlsbD0iIzk5OTk5OSI+5Zu+54mH5Yqg6L295aSx6LSlPC9mb250PjwvcD4KPC9zdmc+'
        }
      })
    }
  },
  {
    title: '图片地址',
    key: 'carouselUrl',
    ellipsis: {
      tooltip: true
    },
    render(row) {
      return h('span', {
        class: 'text-blue-600 cursor-pointer',
        onClick: () => window.open(row.carouselUrl, '_blank')
      }, row.carouselUrl)
    }
  },
  {
    title: '链接地址',
    key: 'linkUrl',
    ellipsis: {
      tooltip: true
    },
    render(row) {
      if (!row.linkUrl) return '无链接'
      return h('span', {
        class: 'text-blue-600 cursor-pointer',
        onClick: () => window.open(row.linkUrl, '_blank')
      }, row.linkUrl)
    }
  },
  {
    title: '排序值',
    key: 'sort',
    width: 100,
    sorter: true
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
    width: 200,
    render(row) {
      return h('div', { class: 'flex space-x-2' }, [
        h(
          NButton,
          {
            size: 'small',
            type: 'info',
            secondary: true,
            onClick: () => handleView(row)
          },
          { default: () => '查看' }
        ),
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
            default: () => '确定删除这个轮播图吗？',
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
    tableData.value = data.list || data.records || []
    pagination.itemCount = data.total || data.totalCount || 0
  } catch (error) {
    message.error('加载数据失败')
    console.error(error)
    tableData.value = []
    pagination.itemCount = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    carouselUrl: '',
    linkUrl: ''
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
    carouselUrl: '',
    linkUrl: '',
    sort: 0
  })
  imagePreviewUrl.value = ''
  imageFileList.value = []
  showModal.value = true
}

const handleView = async (row) => {
  try {
    const res = await api.getDetail(row.id)
    const data = res.data || res
    
    viewData.value = {
      id: data.id,
      carouselUrl: data.carouselUrl || '',
      linkUrl: data.linkUrl || '',
      sort: data.sort || 0,
      createTime: data.createTime || '',
      updateTime: data.updateTime || ''
    }
    showViewModal.value = true
  } catch (error) {
    message.error('获取轮播图详情失败')
    console.error(error)
  }
}

const handleEdit = async (row) => {
  try {
    const res = await api.getDetail(row.id)
    const data = res.data || res
    Object.assign(formData, {
      id: data.id,
      carouselUrl: data.carouselUrl || '',
      linkUrl: data.linkUrl || '',
      sort: data.sort || 0
    })
    imagePreviewUrl.value = data.carouselUrl || ''
    
    // 如果有图片地址，设置文件列表用于显示
    if (data.carouselUrl) {
      imageFileList.value = [{
        id: 'existing',
        name: '当前图片',
        status: 'finished',
        url: data.carouselUrl
      }]
    } else {
      imageFileList.value = []
    }
    
    showModal.value = true
  } catch (error) {
    message.error('获取轮播图详情失败')
    console.error(error)
  }
}

const handleImagePreview = () => {
  if (formData.carouselUrl && /^https?:\/\/.+\.(jpg|jpeg|png|gif|webp)$/i.test(formData.carouselUrl)) {
    imagePreviewUrl.value = formData.carouselUrl
  } else {
    imagePreviewUrl.value = ''
  }
}

const handleImageError = (e) => {
  e.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjEwMCIgdmlld0JveD0iMCAwIDIwMCAxMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSIyMDAiIGhlaWdodD0iMTAwIiBmaWxsPSIjRjVGNUY1Ii8+CjxwYXRoIGQ9Ik04NSA0MEg5NVY1MEg4NVY0MFoiIGZpbGw9IiNEOUQ5RDkiLz4KPHA+PGZvbnQgZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIxMiIgZmlsbD0iIzk5OTk5OSI+5Zu+54mH5Yqg6L295aSx6LSlPC9mb250PjwvcD4KPC9zdmc+'
}

// 图片上传相关方法
const handleBeforeUpload = (data) => {
  const rawFile = data.file.file
  
  // 检查文件类型
  if (!rawFile.type.startsWith('image/')) {
    message.error('只能上传图片文件')
    return false
  }
  
  // 检查文件大小 (5MB)
  if (rawFile.size > 5 * 1024 * 1024) {
    message.error('图片大小不能超过 5MB')
    return false
  }
  
  return true
}

// const handleImageUpload = async ({ file, onFinish, onError }) => {
//   try {
//     const uploadFormData = new FormData()
//     uploadFormData.append('file', file.file)
    
//     // 调用上传API
//     const res = await api.uploadImage(uploadFormData)
    
//     console.log('上传响应:', res)
    
//     // 根据实际返回的数据格式处理
//     let imageUrl = ''
//     if (typeof res === 'string') {
//       imageUrl = res
//     } else if (res && res.data) {
//       imageUrl = res.data
//     } else if (res) {
//       imageUrl = res
//     }
    
//     if (imageUrl) {
//       // 设置图片地址
//       formData.carouselUrl = imageUrl
//       imagePreviewUrl.value = imageUrl
      
//       console.log('设置图片地址:', imageUrl)
//       message.success('图片上传成功')
//       onFinish()
//     } else {
//       throw new Error('上传响应格式错误')
//     }
//   } catch (error) {
//     console.error('图片上传失败:', error)
//     message.error('图片上传失败: ' + (error.message || '未知错误'))
//     onError()
//   }
// }

const handleRemoveImage = () => {
  formData.carouselUrl = ''
  imagePreviewUrl.value = ''
  imageFileList.value = []
}

const handleUploadFinish = ({ file, event }) => {
  try {
    console.log('上传完成:', { file, event })
    
    // 从响应中获取图片URL
    let imageUrl = ''
    if (event && event.target && event.target.response) {
      const response = event.target.response
      console.log('响应内容:', response)
      
      // 如果响应是字符串，直接使用
      if (typeof response === 'string') {
        imageUrl = response
      } else if (response.data) {
        imageUrl = response.data
      }
    }
    
    if (imageUrl) {
      formData.carouselUrl = imageUrl
      imagePreviewUrl.value = imageUrl
      message.success('图片上传成功')
      console.log('设置图片地址:', imageUrl)
    } else {
      message.error('获取图片地址失败')
    }
  } catch (error) {
    console.error('处理上传结果失败:', error)
    message.error('处理上传结果失败')
  }
}

const handleUploadError = ({ file, event }) => {
  console.error('上传失败:', event)
  message.error('图片上传失败')
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    
    submitLoading.value = true

    const submitData = { 
      carouselUrl: formData.carouselUrl.trim(),
      linkUrl: formData.linkUrl?.trim() || '',
      sort: Number(formData.sort)
    }
    
    if (formData.id) {
      submitData.id = formData.id
    }

    console.log('提交数据:', submitData)

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
    console.error('提交错误:', error)
    if (error?.response?.data?.message) {
      message.error(error.response.data.message)
    } else {
      message.error(error?.message || '操作失败')
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

const handleBatchDelete = async () => {
  if (!selectedRowKeys.value.length) {
    message.warning('请选择要删除的轮播图')
    return
  }
  
  try {
    await api.batchDelete(selectedRowKeys.value)
    message.success('批量删除成功')
    selectedRowKeys.value = []
    loadData()
  } catch (error) {
    message.error('批量删除失败')
    console.error(error)
  }
}

// 生命周期
onMounted(() => {
  loadData()
})
</script>

<style scoped>
/* 页面容器 */
.carousel-management {
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