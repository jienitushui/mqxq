<template>
  <div class="announcements-management h-full flex flex-col">
    <PageHeader class="flex-shrink-0">
      <template #title>公告管理</template>
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
          新增公告
        </n-button>
      </template>
    </PageHeader>

    <n-card class="flex-1 flex flex-col overflow-hidden">
      <!-- 搜索区域 -->
      <div class="flex-shrink-0 mb-4">
        <n-form inline :model="searchForm" label-placement="left">
          <n-form-item label="公告标题">
            <n-input
              v-model:value="searchForm.title"
              placeholder="请输入公告标题"
              clearable
              @keyup.enter="handleSearch"
            />
          </n-form-item>
          <n-form-item label="分类">
            <n-select
              v-model:value="searchForm.categoryId"
              placeholder="请选择分类"
              :options="categoryOptions"
              clearable
              style="width: 150px"
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
        title="公告详情"
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
              <span class="font-semibold text-gray-600">标题：</span>
              <div class="mt-1">{{ viewData.title }}</div>
            </div>
            <div>
              <span class="font-semibold text-gray-600">分类：</span>
              <div class="mt-1">{{ viewData.categoryName || '未分类' }}</div>
            </div>
          </div>
          
          <div class="grid grid-cols-2 gap-4">
            <div>
              <span class="font-semibold text-gray-600">发布时间：</span>
              <div class="mt-1">{{ formatDateTime(viewData.publishDate) || '未设置' }}</div>
            </div>
            <div>
              <span class="font-semibold text-gray-600">创建时间：</span>
              <div class="mt-1">{{ formatDateTime(viewData.createTime) }}</div>
            </div>
          </div>
          
          <div class="mt-6">
            <div class="font-semibold text-gray-600 mb-3">内容：</div>
            <div class="p-4 bg-gray-50 rounded-lg border min-h-32 max-h-60 overflow-y-auto whitespace-pre-wrap">
              {{ viewData.content || '无内容' }}
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
          <n-form-item label="公告标题" path="title">
            <n-input v-model:value="formData.title" placeholder="请输入公告标题" />
          </n-form-item>
          <n-form-item label="公告分类" path="categoryId">
            <n-select
              v-model:value="formData.categoryId"
              placeholder="请选择公告分类"
              :options="categoryOptions"
              clearable
              filterable
              @update:value="(value) => { 
                console.log('分类选择变化 - 原值:', formData.categoryId, '新值:', value)
                formData.categoryId = value
                console.log('分类选择后 formData:', formData)
              }"
            />
            <div class="text-xs text-gray-500 mt-1">
              当前选择: {{ formData.categoryId }} (类型: {{ typeof formData.categoryId }})
            </div>
          </n-form-item>
          <n-form-item label="公告内容" path="content">
            <n-input
              v-model:value="formData.content"
              type="textarea"
              placeholder="请输入公告内容"
              :rows="8"
            />
          </n-form-item>
          <n-form-item label="发布时间" path="publishDate">
            <n-date-picker
              v-model:value="formData.publishDate"
              type="datetime"
              placeholder="请选择发布时间"
              clearable
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
import { 
  NButton, 
  NTag, 
  NPopconfirm, 
  useMessage,
  NCard,
  NForm,
  NFormItem,
  NInput,
  NSelect,
  NDataTable,
  NModal,
  NDatePicker
} from 'naive-ui'

import { PageHeader } from '@/components'
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
const categoryOptions = ref([])

// 查看详情数据
const viewData = ref({})

// 搜索表单
const searchForm = reactive({
  title: '',
  categoryId: null
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
  categoryId: undefined,
  title: '',
  content: '',
  publishDate: null
})

// 表单验证规则
const formRules = {
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' },
    { max: 255, message: '标题长度不能超过255个字符', trigger: 'blur' }
  ],
  categoryId: [
    { 
      required: true, 
      message: '请选择公告分类', 
      trigger: ['change', 'blur'],
      validator: (rule, value) => {
        if (!value && value !== 0) {
          return new Error('请选择公告分类')
        }
        console.log('选择分类:', value)
        return true
      }
    }
  ],
  content: [
    { required: true, message: '请输入公告内容', trigger: 'blur' }
  ]
}

// 计算属性
const modalTitle = computed(() => formData.id ? '编辑公告' : '新增公告')

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
    title: '公告标题',
    key: 'title',
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '分类',
    key: 'categoryName',
    width: 120,
    render(row) {
      const category = categoryOptions.value.find(cat => cat.value === row.categoryId)
      return category ? category.label : '未分类'
    }
  },
  {
    title: '发布时间',
    key: 'publishDate',
    width: 180,
    render(row) {
      return row.publishDate || '未设置'
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
            default: () => '确定删除这个公告吗？',
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

const loadCategories = async () => {
  try {
    const res = await api.getCategories()
    console.log('分类API响应:', res) // 调试日志
    const data = res?.data || res || []
    
    if (!Array.isArray(data)) {
      console.error('分类数据格式错误:', data)
      categoryOptions.value = []
      return
    }
    
    categoryOptions.value = data.map(category => ({
      label: category.name,
      value: Number(category.id) // 确保值是数字类型
    }))
    console.log('分类选项:', categoryOptions.value) // 调试日志
  } catch (error) {
    console.error('加载分类列表失败', error)
    message.error('加载分类列表失败')
    categoryOptions.value = []
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    title: '',
    categoryId: null
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
    categoryId: undefined,
    title: '',
    content: '',
    publishDate: null
  })
  showModal.value = true
}

const handleView = async (row) => {
  try {
    const res = await api.getDetail(row.id)
    const data = res.data || res
    
    // 根据categoryId查找分类名称
    const category = categoryOptions.value.find(cat => cat.value === data.categoryId)
    const categoryName = category ? category.label : '未分类'
    
    // 设置查看数据并显示模态框
    viewData.value = {
      title: data.title || '无标题',
      categoryName: categoryName,
      publishDate: data.publishDate || '未设置',
      createTime: data.createTime || '',
      content: data.content || '无内容'
    }
    showViewModal.value = true
  } catch (error) {
    message.error('获取公告详情失败')
    console.error(error)
  }
}

const handleEdit = async (row) => {
  try {
    const res = await api.getDetail(row.id)
    const data = res.data || res
    Object.assign(formData, {
      id: data.id,
      categoryId: data.categoryId ? Number(data.categoryId) : undefined,
      title: data.title,
      content: data.content,
      publishDate: data.publishDate ? new Date(data.publishDate).getTime() : null
    })
    console.log('编辑数据:', formData) // 调试日志
    showModal.value = true
  } catch (error) {
    message.error('获取公告详情失败')
    console.error(error)
  }
}

const handleSubmit = async () => {
  try {
    console.log('开始提交，当前表单数据:', formData) // 调试日志
    
    await formRef.value?.validate()
    
    // 额外检查必填字段
    if (!formData.categoryId && formData.categoryId !== 0) {
      console.log('categoryId 验证失败:', formData.categoryId)
      message.error('请选择公告分类')
      return
    }
    
    if (!formData.title?.trim()) {
      message.error('请输入公告标题')
      return
    }
    
    if (!formData.content?.trim()) {
      message.error('请输入公告内容')
      return
    }
    
    submitLoading.value = true

    // 处理提交数据
    const submitData = { 
      categoryId: Number(formData.categoryId), // 使用驼峰命名
      title: formData.title.trim(),
      content: formData.content.trim()
    }
    
    // 转换日期格式
    if (formData.publishDate) {
      submitData.publishDate = new Date(formData.publishDate).toISOString().slice(0, 19).replace('T', ' ')
    }
    
    // 如果是更新操作，添加ID
    if (formData.id) {
      submitData.id = formData.id
    }
    
    console.log('最终提交数据:', submitData) // 调试日志
    console.log('categoryId 类型:', typeof submitData.categoryId, '值:', submitData.categoryId)

    if (formData.id) {
      console.log('执行更新操作')
      const result = await api.update(formData.id, submitData)
      console.log('更新结果:', result)
      message.success('更新成功')
    } else {
      console.log('执行创建操作')
      const result = await api.create(submitData)
      console.log('创建结果:', result)
      message.success('创建成功')
    }
    
    showModal.value = false
    loadData()
  } catch (error) {
    console.error('提交完整错误信息:', error)
    console.error('错误响应数据:', error?.response?.data)
    
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
    message.warning('请选择要删除的公告')
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
  loadCategories()
})
</script>

<style scoped>
/* 页面容器 */
.announcements-management {
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