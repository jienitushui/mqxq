<template>
  <div class="announcement-categories-management h-full flex flex-col">
    <PageHeader class="flex-shrink-0">
      <template #title>公告分类管理</template>
      <template #extra>
        <n-button type="primary" @click="handleAdd">
          <template #icon>
            <i class="i-carbon:add" />
          </template>
          新增分类
        </n-button>
      </template>
    </PageHeader>

    <n-card class="flex-1 flex flex-col overflow-hidden">
      <!-- 搜索区域 -->
      <div class="flex-shrink-0 mb-4">
        <n-form inline :model="searchForm" label-placement="left">
          <n-form-item label="分类名称">
            <n-input
              v-model:value="searchForm.name"
              placeholder="请输入分类名称"
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
          label-width="100px"
          require-mark-placement="right-hanging"
        >
          <n-form-item label="分类名称" path="name">
            <n-input 
              v-model:value="formData.name" 
              placeholder="请输入分类名称" 
              maxlength="50"
              show-count
            />
          </n-form-item>
          <n-form-item label="分类描述" path="description">
            <n-input
              v-model:value="formData.description"
              type="textarea"
              placeholder="请输入分类描述（可选）"
              :rows="4"
              maxlength="500"
              show-count
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
import { NButton, NTag, NPopconfirm, useMessage } from 'naive-ui'
import { PageHeader } from '@/components'
import { formatDateTime } from '@/utils'
import api from './api'

const message = useMessage()

// 响应式数据
const loading = ref(false)
const showModal = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const formRef = ref(null)

// 搜索表单
const searchForm = reactive({
  name: ''
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

// 表单数据 - 根据数据库表结构
const formData = reactive({
  id: null,
  name: '',
  description: '',
  createTime: null,
  createUser: null,
  updateTime: null,
  updateUser: null
})

// 表单验证规则 - 根据数据库字段长度限制
const formRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { max: 50, message: '分类名称不能超过50个字符', trigger: 'blur' }
  ],
  description: [
    { max: 1000, message: '描述内容过长', trigger: 'blur' }
  ]
}

// 计算属性
const modalTitle = computed(() => formData.id ? '编辑分类' : '新增分类')

// 表格列配置 - 根据数据库表结构
const columns = [
  {
    title: 'ID',
    key: 'id',
    width: 80,
    fixed: 'left'
  },
  {
    title: '分类名称',
    key: 'name',
    width: 200,
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '分类描述',
    key: 'description',
    width: 300,
    ellipsis: {
      tooltip: true
    },
    render(row) {
      return row.description || '-'
    }
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
    title: '更新时间',
    key: 'updateTime',
    width: 180,
    render(row) {
      return formatDateTime(row.updateTime) || '-'
    }
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
          NPopconfirm,
          {
            onPositiveClick: () => handleDelete(row.id)
          },
          {
            default: () => '确定删除这个分类吗？删除后无法恢复！',
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
    
    console.log('请求参数:', params)
    
    const res = await api.getList(params)
    console.log('API响应:', res)
    
    const data = res?.data || res || {}
    tableData.value = data.list || data.records || data.content || []
    pagination.itemCount = data.total || data.totalCount || data.totalElements || 0
    
    console.log('表格数据:', tableData.value)
    console.log('总数:', pagination.itemCount)
    
  } catch (error) {
    console.error('加载数据失败:', error)
    message.error('加载数据失败: ' + (error?.message || '未知错误'))
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
    name: ''
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
    name: '',
    description: '',
    createTime: null,
    createUser: null,
    updateTime: null,
    updateUser: null
  })
  showModal.value = true
}

const handleEdit = async (row) => {
  try {
    console.log('编辑行数据:', row)
    
    // 如果需要获取详细信息，调用详情接口
    let detailData = row
    if (api.getDetail) {
      const res = await api.getDetail(row.id)
      detailData = res?.data || res || row
    }
    
    Object.assign(formData, detailData)
    showModal.value = true
  } catch (error) {
    console.error('获取分类详情失败:', error)
    message.error('获取分类详情失败: ' + (error?.message || '未知错误'))
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitLoading.value = true

    // 准备提交的数据，只包含需要的字段
    const submitData = {
      name: formData.name?.trim(),
      description: formData.description?.trim() || null
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
    console.error('操作失败:', error)
    
    // 详细的错误处理
    if (error?.response) {
      const { status, data } = error.response
      if (status === 400) {
        message.error(data?.message || '请求参数错误')
      } else if (status === 409) {
        message.error('分类名称已存在，请使用其他名称')
      } else {
        message.error(data?.message || `操作失败 (${status})`)
      }
    } else {
      message.error(error?.message || '操作失败，请稍后重试')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (id) => {
  try {
    console.log('删除ID:', id)
    await api.delete(id)
    message.success('删除成功')
    
    // 如果当前页没有数据了，回到上一页
    if (tableData.value.length === 1 && pagination.page > 1) {
      pagination.page -= 1
    }
    
    loadData()
  } catch (error) {
    console.error('删除失败:', error)
    
    if (error?.response?.status === 409) {
      message.error('该分类下还有公告，无法删除')
    } else {
      message.error(error?.response?.data?.message || error?.message || '删除失败')
    }
  }
}

// 生命周期
onMounted(() => {
  console.log('组件挂载，开始加载数据')
  loadData()
})
</script>

<style scoped>
/* 页面容器 */
.announcement-categories-management {
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

/* 响应式处理 */
@media (max-width: 768px) {
  :deep(.n-data-table) {
    font-size: 12px;
  }
}
</style>