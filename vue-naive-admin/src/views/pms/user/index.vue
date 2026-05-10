<template>
  <div class="user-management h-full flex flex-col">
    <PageHeader class="flex-shrink-0">
      <template #title>用户管理</template>
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
          新增用户
        </n-button>
      </template>
    </PageHeader>

    <n-card class="flex-1 flex flex-col overflow-hidden">
      <!-- 搜索区域 -->
      <div class="flex-shrink-0 mb-4">
        <n-form inline :model="searchForm" label-placement="left">
          <n-form-item label="关键词">
            <n-input
              v-model:value="searchForm.keyword"
              placeholder="用户名/姓名"
              clearable
              @keyup.enter="handleSearch"
            />
          </n-form-item>
          <n-form-item label="用户类型">
            <n-select
              v-model:value="searchForm.userType"
              placeholder="请选择用户类型"
              clearable
              :options="userTypeOptions"
              style="width: 150px"
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
          <n-form-item label="用户名" path="username">
            <n-input v-model:value="formData.user.username" placeholder="请输入用户名" />
          </n-form-item>
          <n-form-item label="密码" path="password" v-if="!formData.user.id">
            <n-input
              v-model:value="formData.user.password"
              type="password"
              placeholder="请输入密码"
              show-password-on="mousedown"
            />
          </n-form-item>
          <n-form-item label="姓名" path="name">
            <n-input v-model:value="formData.user.name" placeholder="请输入姓名" />
          </n-form-item>
          <n-form-item label="手机号" path="phone">
            <n-input v-model:value="formData.user.phone" placeholder="请输入手机号" />
          </n-form-item>

          <n-form-item label="角色" path="role">
            <n-select
              v-model:value="formData.role"
              placeholder="请选择角色"
              :options="roleOptions"
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
import { NButton, NTag, NPopconfirm, NSwitch, useMessage } from 'naive-ui'
import { PageHeader } from '@/components'
import api from './api'
import roleApi from '../role/api'

const message = useMessage()

// 响应式数据
const loading = ref(false)
const showModal = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const formRef = ref(null)
const selectedRowKeys = ref([])
const roleOptions = ref([])

// 搜索表单
const searchForm = reactive({
  keyword: '',
  userType: null
})

// 用户类型选项
const userTypeOptions = [
  { label: '管理员', value: 'ADMIN' },
  { label: '教师', value: 'TEACHER' },
  { label: '普通用户', value: 'USER' }
]

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
  user: {
    id: null,
    username: '',
    password: '',
    name: '',
    phone: ''
  },
  role: null
})

// 表单验证规则
const formRules = {
  'user.username': [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  'user.password': [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  'user.name': [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
}

// 计算属性
const modalTitle = computed(() => formData.user.id ? '编辑用户' : '新增用户')

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
    title: '用户名',
    key: 'username',
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '姓名',
    key: 'name',
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '手机号',
    key: 'phone',
    width: 120
  },

  {
    title: '用户类型',
    key: 'roleList',
    width: 100,
    render(row) {
      const role = row.roleList?.[0]
      const roleMap = {
        '管理员': { text: '管理员', type: 'error' },
        '教师': { text: '教师', type: 'warning' },
        '用户': { text: '用户', type: 'info' },
      }
      const config = roleMap[role] || { text: '未知', type: 'default' }
      return h(NTag, { type: config.type, bordered: false }, { default: () => config.text })
    }
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render(row) {
      return h(NSwitch, {
        value: row.status === 1,
        onUpdateValue: (value) => handleStatusChange(row.id, value ? 1 : 0)
      })
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
            type: 'warning',
            secondary: true,
            onClick: () => handleResetPassword(row.id)
          },
          { default: () => '重置密码' }
        ),
        h(
          NPopconfirm,
          {
            onPositiveClick: () => handleDelete(row.id)
          },
          {
            default: () => '确定删除这个用户吗？',
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
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
      ...searchForm
    }
    const res = await api.getList(params)
    // 安全地访问数据，处理不同的响应结构
    const data = res?.data || res || {}
    tableData.value = data.list || data.records || []
    pagination.itemCount = data.total || data.totalCount || 0
  } catch (error) {
    message.error('加载数据失败')
    console.error(error)
    // 设置默认值避免页面崩溃
    tableData.value = []
    pagination.itemCount = 0
  } finally {
    loading.value = false
  }
}

const loadRoles = async () => {
  try {
    const res = await roleApi.getList()
    // 安全地访问数据
    const roles = res?.data || res || []
    roleOptions.value = (Array.isArray(roles) ? roles : []).map(role => ({
      label: role.roleName || role.name,
      value: role.roleCode || role.code || role.id
    }))
  } catch (error) {
    console.error('加载角色列表失败', error)
    // 设置默认值
    roleOptions.value = []
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    keyword: '',
    userType: null
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
    user: {
      id: null,
      username: '',
      password: '',
      name: '',
      phone: ''
    },
    role: null
  })
  showModal.value = true
}

const handleEdit = async (row) => {
  try {
    const res = await api.getUserDetail(row.id)
    const { roleList, ...user } = res.data
    Object.assign(formData, {
      user: user,
      role: roleList?.[0] || null
    })
    showModal.value = true
  } catch (error) {
    message.error('获取用户详情失败')
    console.error(error)
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitLoading.value = true

    // 直接使用用户数据，不需要包装在 user 属性中
    const payload = {
      ...formData.user,
      // 如果需要角色信息，可以添加到用户对象中
      roles: [formData.role]
    }
    if (formData.user.id) {
      await api.update(payload)
      message.success('更新成功')
    } else {
      await api.create(payload)
      message.success('创建成功')
    }
    
    showModal.value = false
    loadData()
  } catch (error) {
    if (error?.message) {
      message.error(error.message)
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
    message.warning('请选择要删除的用户')
    return
  }
  
  try {
    await api.batchDelete({ ids: selectedRowKeys.value })
    message.success('批量删除成功')
    selectedRowKeys.value = []
    loadData()
  } catch (error) {
    message.error('批量删除失败')
    console.error(error)
  }
}

const handleStatusChange = async (id, status) => {
  try {
    await api.updateStatus({ id, status })
    message.success(status === 1 ? '启用成功' : '禁用成功')
    loadData()
  } catch (error) {
    message.error('状态更新失败')
    console.error(error)
  }
}

const handleResetPassword = async (id) => {
  try {
    await api.resetPassword(id)
    // 弹出密码重置成功
    
    message.success('密码重置成功')
  } catch (error) {
    message.error('密码重置失败')
    console.error(error)
  }
}

// 生命周期
onMounted(() => {
  loadData()
  loadRoles()
})
</script>

<style scoped>
.user-management {
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