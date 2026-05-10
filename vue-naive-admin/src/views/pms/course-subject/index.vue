<template>
  <div class="course-subject-management h-full flex flex-col">
    <PageHeader>
      <template #title>课程分类管理</template>
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
          新增分类
        </n-button>
      </template>
    </PageHeader>

    <n-card class="flex-1 overflow-y-auto">
      <!-- 搜索区域 -->
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

      <!-- 表格 -->
      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="false"
        :row-key="row => row.id"
        v-model:checked-row-keys="selectedRowKeys"
        children-key="children"
        default-expand-all
      />
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
          label-width="80px"
          require-mark-placement="right-hanging"
        >
          <n-form-item label="科目名称" path="name">
            <n-input v-model:value="formData.name" placeholder="请输入科目名称（最多50个字符）" maxlength="50" show-count />
          </n-form-item>
          <n-form-item label="父级分类" path="parentId">
            <n-tree-select
              v-model:value="formData.parentId"
              placeholder="请选择父级分类（不选则为顶级分类）"
              :options="treeOptions"
              clearable
              key-field="value"
              label-field="label"
              children-field="children"
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
import api from './api'

const message = useMessage()

// 响应式数据
const loading = ref(false)
const showModal = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const formRef = ref(null)
const selectedRowKeys = ref([])
const treeOptions = ref([])

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
  pageSizes: [10, 20, 50, 100]
})

// 表单数据
const formData = reactive({
  id: null,
  name: '',
  parentId: null
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur', max: 50 }
  ]
}

// 计算属性
const modalTitle = computed(() => formData.id ? '编辑分类' : '新增分类')

// 表格列配置
const columns = [
  {
    type: 'selection'
  },
    {
    title: 'ID',
    key: 'id',
    width: 100
  },
  {
    title: '科目名称',
    key: 'name',
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '层级',
    key: 'level',
    width: 80,
    render(row) {
      return row.parentId === 0 || row.parentId === null ? '顶级' : '子级'
    }
  },
  {
    title: '创建人',
    key: 'createUser',
    width: 100,
    render(row) {
      return row.createUser || '-'
    }
  },
  {
    title: '创建时间',
    key: 'createTime',
    width: 180,
    render(row) {
      return row.createTime ? new Date(row.createTime).toLocaleString('zh-CN') : '-'
    }
  },
  {
    title: '更新人',
    key: 'updateUser',
    width: 100,
    render(row) {
      return row.updateUser || '-'
    }
  },
  {
    title: '更新时间',
    key: 'updateTime',
    width: 180,
    render(row) {
      return row.updateTime ? new Date(row.updateTime).toLocaleString('zh-CN') : '-'
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    render(row) {
      return [
        h(NButton, {
          size: 'small',
          type: 'primary',
          style: { marginRight: '8px' },
          onClick: () => handleEdit(row)
        }, { default: () => '编辑' }),
        h(NPopconfirm, {
          onPositiveClick: () => handleDelete(row.id)
        }, {
          default: () => '确定删除这个分类吗？',
          trigger: () => h(NButton, {
            size: 'small',
            type: 'error'
          }, { default: () => '删除' })
        })
      ]
    }
  }
]

// 方法
const loadData = async () => {
  loading.value = true
  try {
    let response
    if (searchForm.name) {
      // 如果有搜索条件，使用分页接口
      const params = {
        pageNum: 1,
        pageSize: 1000,
        name: searchForm.name
      }
      response = await api.getSubjectPage(params)
      tableData.value = response.data.list || []
    } else {
      // 没有搜索条件时，使用树形接口
      response = await api.getSubjectTree()
      tableData.value = response.data || []
    }
    console.log('加载的数据:', tableData.value)
  } catch (error) {
    message.error('加载数据失败')
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

const loadTreeOptions = async () => {
  try {
    // 先尝试获取树形数据
    const response = await api.getSubjectTree()
    if (response.data && response.data.length > 0) {
      // 确保树形数据格式正确
      const formatTreeData = (data) => {
        return data.map(item => ({
          label: item.name,
          value: item.id,
          children: item.children && item.children.length > 0 ? formatTreeData(item.children) : undefined
        }))
      }
      treeOptions.value = formatTreeData(response.data)
    } else {
      // 如果树形接口没有数据，使用分页接口获取所有数据
      const pageResponse = await api.getSubjectPage({ page: 1, size: 1000 })
      if (pageResponse.data && pageResponse.data.list) {
        const buildTree = (items, parentId = null) => {
          const children = items
            .filter(item => {
              // 处理parentId为0或null的情况
              const itemParentId = item.parentId === 0 ? null : item.parentId
              return itemParentId === parentId
            })
            .map(item => ({
              label: item.name,
              value: item.id,
              children: buildTree(items, item.id)
            }))
          return children.length > 0 ? children : undefined
        }
        
        const treeData = buildTree(pageResponse.data.list, null)
        treeOptions.value = treeData || []
        
        // 调试输出
        console.log('原始数据:', pageResponse.data.list)
        console.log('构建的树形数据:', treeData)
      }
    }
  } catch (error) {
    message.error('加载分类树失败')
    console.error('加载分类树失败:', error)
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

const handleAdd = () => {
  Object.assign(formData, {
    id: null,
    name: '',
    parentId: null
  })
  showModal.value = true
}

const handleEdit = (row) => {
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    parentId: row.parentId
  })
  showModal.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitLoading.value = true
    
    if (formData.id) {
      await api.updateSubject(formData.id, formData)
      message.success('更新成功')
    } else {
      await api.createSubject(formData)
      message.success('创建成功')
    }
    
    showModal.value = false
    loadData()
    loadTreeOptions()
  } catch (error) {
    message.error(formData.id ? '更新失败' : '创建失败')
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await api.deleteSubject(id)
    message.success('删除成功')
    loadData()
    loadTreeOptions()
  } catch (error) {
    message.error('删除失败')
  }
}

const handleBatchDelete = async () => {
  try {
    await api.batchDeleteSubjects(selectedRowKeys.value)
    message.success('批量删除成功')
    selectedRowKeys.value = []
    loadData()
    loadTreeOptions()
  } catch (error) {
    message.error('批量删除失败')
  }
}



// 生命周期
onMounted(() => {
  loadData()
  loadTreeOptions()
})
</script>