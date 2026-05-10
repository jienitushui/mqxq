<template>
  <div class="order-management h-full flex flex-col">
    <PageHeader class="flex-shrink-0">
      <template #title>订单管理</template>
    </PageHeader>

    <!-- 统计卡片 -->
    <div class="flex-shrink-0 mb-4">
      <n-grid :cols="4" :x-gap="16" :y-gap="16">
        <n-grid-item>
          <n-card>
            <n-statistic label="总订单数" :value="statistics.totalOrders" />
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card>
            <n-statistic label="待处理订单" :value="statistics.pendingOrders" />
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card>
            <n-statistic
              label="已完成订单"
              :value="statistics.completedOrders"
            />
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card>
            <n-statistic
              label="已取消订单"
              :value="statistics.cancelledOrders"
            />
          </n-card>
        </n-grid-item>
      </n-grid>
    </div>

    <n-card class="flex-1 flex flex-col overflow-hidden">
      <!-- 搜索区域 -->
      <div class="flex-shrink-0 mb-4">
        <n-form
          ref="searchFormRef"
          :model="searchForm"
          label-placement="left"
          :label-width="80"
          inline
        >
          <n-form-item label="订单号">
            <n-input
              v-model:value="searchForm.orderNo"
              placeholder="请输入订单号"
              clearable
            />
          </n-form-item>
          <n-form-item label="用户ID">
            <n-input
              v-model:value="searchForm.userId"
              placeholder="请输入用户ID"
              clearable
            />
          </n-form-item>
          <n-form-item label="商品ID">
            <n-input
              v-model:value="searchForm.goodsId"
              placeholder="请输入商品ID"
              clearable
            />
          </n-form-item>
          <n-form-item label="订单状态">
            <n-select
              v-model:value="searchForm.status"
              placeholder="请选择订单状态"
              style="width: 200px"
              clearable
              :options="statusOptions"
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
          :row-key="(row) => row.id"
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

    <!-- 订单详情弹窗 -->
    <n-modal
      v-model:show="showDetailModal"
      preset="card"
      title="订单详情"
      size="large"
      :mask-closable="false"
    >
      <div v-if="orderDetail" class="order-detail">
        <n-descriptions :column="2" bordered>
          <n-descriptions-item label="订单号">
            {{ orderDetail.orderNo }}
          </n-descriptions-item>
          <n-descriptions-item label="订单状态">
            <n-tag :type="getStatusType(orderDetail.status)">
              {{ getStatusText(orderDetail.status) }}
            </n-tag>
          </n-descriptions-item>
          <n-descriptions-item label="用户ID">
            {{ orderDetail.userId }}
          </n-descriptions-item>
          <n-descriptions-item label="商品ID">
            {{ orderDetail.goodsId }}
          </n-descriptions-item>
          <n-descriptions-item label="商品名称">
            {{ orderDetail.goodsName }}
          </n-descriptions-item>
          <n-descriptions-item label="商品价格">
            ¥{{ orderDetail.goodsPrice }}
          </n-descriptions-item>
          <n-descriptions-item label="支付单号">
            {{ orderDetail.payNo || "未支付" }}
          </n-descriptions-item>
          <n-descriptions-item label="创建时间">
            {{ formatDateTime(orderDetail.createTime) }}
          </n-descriptions-item>
          <n-descriptions-item label="支付时间">
            {{ formatDateTime(orderDetail.payTime) || "未支付" }}
          </n-descriptions-item>
          <n-descriptions-item label="商品图片" span="2">
            <n-image
              v-if="orderDetail.goodsImg"
              :src="orderDetail.goodsImg"
              width="100"
            />
            <span v-else>无图片</span>
          </n-descriptions-item>
        </n-descriptions>
      </div>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showDetailModal = false">关闭</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h } from "vue";
import { NButton, NTag, NPopconfirm, useMessage } from "naive-ui";
import PageHeader from "@/components/common/PageHeader.vue";
import { orderApi } from "./api";

// 格式化时间函数
const formatDateTime = (dateString) => {
  if (!dateString) return "未知";
  
  try {
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return dateString;
    
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  } catch (error) {
    return dateString;
  }
};

const message = useMessage();

// 响应式数据
const loading = ref(false);
const tableData = ref([]);
const showDetailModal = ref(false);
const orderDetail = ref(null);

// 统计数据
const statistics = ref({
  totalOrders: 0,
  pendingOrders: 0,
  completedOrders: 0,
  cancelledOrders: 0,
});

// 搜索表单
const searchForm = reactive({
  orderNo: "",
  userId: null,
  goodsId: null,
  status: null,
});

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
});

// 订单状态选项
const statusOptions = [
  { label: "待支付", value: "NOT_PAY" },
  { label: "已完成", value: "DONE" },
  { label: "已取消", value: "CANCEL" },
];

// 表格列配置
const columns = [
  {
    title: "订单号",
    key: "orderNo",
    width: 200,
  },
  {
    title: "用户ID",
    key: "userId",
    width: 100,
  },
  {
    title: "商品名称",
    key: "goodsName",
    width: 200,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: "商品价格",
    key: "goodsPrice",
    width: 120,
    render(row) {
      return `¥${row.goodsPrice}`;
    },
  },
  {
    title: "订单状态",
    key: "status",
    width: 100,
    render(row) {
      return h(
        NTag,
        {
          type: getStatusType(row.status),
        },
        {
          default: () => getStatusText(row.status),
        }
      );
    },
  },
  {
    title: "支付单号",
    key: "payNo",
    width: 180,
    render(row) {
      return row.payNo || "未支付";
    },
  },
  {
    title: "创建时间",
    key: "createTime",
    width: 180,
    render(row) {
      return formatDateTime(row.createTime);
    },
  },
  {
    title: "操作",
    key: "actions",
    width: 150,
    render(row) {
      return h("div", { class: "flex gap-2" }, [
        h(
          NButton,
          {
            size: "small",
            type: "primary",
            ghost: true,
            onClick: () => handleViewDetail(row.id),
          },
          {
            default: () => "查看",
            icon: () => h("i", { class: "i-carbon:view" }),
          }
        ),
        h(
          NPopconfirm,
          {
            onPositiveClick: () => handleDelete(row.id),
          },
          {
            trigger: () =>
              h(
                NButton,
                {
                  size: "small",
                  type: "error",
                  ghost: true,
                },
                {
                  default: () => "删除",
                  icon: () => h("i", { class: "i-carbon:trash-can" }),
                }
              ),
            default: () => "确定删除这个订单吗？",
          }
        ),
      ]);
    },
  },
];

// 获取订单状态类型
const getStatusType = (status) => {
  const typeMap = {
    NOT_PAY: "warning",
    DONE: "success",
    CANCEL: "error",
  };
  return typeMap[status] || "default";
};

// 获取订单状态文本
const getStatusText = (status) => {
  const textMap = {
    NOT_PAY: "待支付",
    DONE: "已完成",
    CANCEL: "已取消",
  };
  return textMap[status] || "未知";
};

// 获取订单列表
const getOrderList = async () => {
  try {
    loading.value = true;
    const params = {
      page: pagination.page,
      size: pagination.pageSize,
      ...searchForm,
    };

    const response = await orderApi.getOrderList(params);

    // 根据实际API返回格式处理数据
    // API直接返回数据对象，包含 total, list 等字段
    // console.log(response);
    tableData.value = response.data.list || [];
    pagination.itemCount = response.data.total || 0;
  } catch (error) {
    console.error("获取订单列表失败:", error);
    message.error("获取订单列表失败");
  } finally {
    loading.value = false;
  }
};

// 获取统计数据
const getStatistics = async () => {
  try {
    const response = await orderApi.getOrderStatistics();

    // 根据实际API返回格式处理统计数据
    if (response && response.data) {
      // API返回包装格式 {code, data}
      const data = response.data;
      statistics.value = {
        totalOrders: data.totalOrders || 0,
        pendingOrders: data.pendingOrders || 0,
        completedOrders: data.completedOrders || 0,
        cancelledOrders: data.cancelledOrders || 0,
      };
    }
  } catch (error) {
    console.error("获取统计数据失败:", error);
    // 从订单列表数据计算统计信息
    calculateStatisticsFromList();
  }
};

// 查看订单详情
const handleViewDetail = async (orderId) => {
  try {
    const response = await orderApi.getOrderDetail(orderId);

    orderDetail.value = response.data;
    showDetailModal.value = true;
  } catch (error) {
    console.error("获取订单详情失败:", error);
    message.error("获取订单详情失败");
  }
};

// 删除订单
const handleDelete = async (orderId) => {
  try {
    await orderApi.deleteOrder(orderId);
    message.success("删除成功");
    await getOrderList();
    await getStatistics();
  } catch (error) {
    console.error("删除订单失败:", error);
    message.error("删除失败");
  }
};

// 搜索
const handleSearch = () => {
  pagination.page = 1;
  getOrderList();
};

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    orderNo: "",
    userId: null,
    goodsId: null,
    status: null,
  });
  pagination.page = 1;
  getOrderList();
};

// 分页变化
const handlePageChange = (page) => {
  pagination.page = page;
  getOrderList();
};

// 每页大小变化
const handlePageSizeChange = (pageSize) => {
  pagination.pageSize = pageSize;
  pagination.page = 1;
  getOrderList();
};

// 初始化
onMounted(() => {
  getOrderList();
  getStatistics();
});
</script>

<style scoped>
.order-management {
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

.page-header {
  margin-bottom: 16px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.statistics-cards {
  margin-bottom: 16px;
}

.search-card {
  margin-bottom: 16px;
}

.search-form {
  margin-bottom: 16px;
}

.search-actions {
  display: flex;
  justify-content: flex-end;
}

.table-card {
  margin-bottom: 16px;
}

.order-detail {
  padding: 16px 0;
}

.w-full {
  width: 100%;
}

.flex {
  display: flex;
}

.gap-2 {
  gap: 8px;
}
</style>