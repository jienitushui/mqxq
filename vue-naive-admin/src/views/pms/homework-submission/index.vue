<template>
  <div class="homework-submission h-full flex flex-col">
    <PageHeader class="flex-shrink-0 header-with-button">
      <template #title>作业提交管理</template>
      <template #extra>
        <div style="display: flex; justify-content: flex-end; width: 100%;">
          <n-button @click="handleExport" type="primary">
            <template #icon>
              <i class="i-carbon:download" />
            </template>
            导出提交数据
          </n-button>
        </div>
      </template>
    </PageHeader>

    <!-- 统计卡片 -->
    <div class="flex-shrink-0 mb-4">
      <n-grid :cols="4" :x-gap="16" :y-gap="16">
        <n-grid-item>
          <n-card>
            <n-statistic label="总提交数" :value="statistics.totalSubmissions || 0" />
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card>
            <n-statistic label="已批改" :value="statistics.gradedCount || 0" />
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card>
            <n-statistic label="待批改" :value="statistics.ungradedCount || 0" />
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card>
            <n-statistic 
              label="平均分" 
              :value="statistics.avgScore || 0" 
              :precision="1"
            />
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card>
            <n-statistic label="最高分" :value="statistics.maxScore || 0" />
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card>
            <n-statistic label="最低分" :value="statistics.minScore || 0" />
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card>
            <n-statistic 
              label="及格率" 
              :value="statistics.passRate || 0" 
              suffix="%" 
              :precision="1"
            />
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card>
            <n-statistic 
              label="优秀率" 
              :value="statistics.excellentRate || 0" 
              suffix="%" 
              :precision="1"
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
          <n-form-item label="学生ID">
            <n-input
              v-model:value="searchForm.studentId"
              placeholder="请输入学生ID"
              clearable
            />
          </n-form-item>
          <n-form-item label="作业ID">
            <n-input
              v-model:value="searchForm.homeworkId"
              placeholder="请输入作业ID"
              clearable
            />
          </n-form-item>
          <n-form-item label="学生账号">
            <n-input
              v-model:value="searchForm.studentName"
              placeholder="请输入学生账号"
              clearable
            />
          </n-form-item>
          <n-form-item label="作业标题">
            <n-input
              v-model:value="searchForm.homeworkTitle"
              placeholder="请输入作业标题"
              clearable
            />
          </n-form-item>
          <n-form-item label="提交状态">
            <n-select
              v-model:value="searchForm.status"
              placeholder="请选择提交状态"
              clearable
              style="width: 200px"
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

    <!-- 作业详情弹窗 -->
    <n-modal
      v-model:show="showDetailModal"
      preset="card"
      title="作业提交详情"
      size="large"
      :mask-closable="false"
    >
      <div v-if="submissionDetail" class="submission-detail">
        <n-descriptions :column="2" bordered>
          <n-descriptions-item label="提交ID">
            {{ submissionDetail.id }}
          </n-descriptions-item>
          <n-descriptions-item label="学生ID">
            {{ submissionDetail.studentId }}
          </n-descriptions-item>
          <n-descriptions-item label="作业ID">
            {{ submissionDetail.homeworkId }}
          </n-descriptions-item>
          <n-descriptions-item label="作业标题">
            {{ submissionDetail.homeworkTitle }}
          </n-descriptions-item>
          <n-descriptions-item label="提交状态">
            <n-tag :type="getStatusType(submissionDetail.status)">
              {{ getStatusText(submissionDetail.status) }}
            </n-tag>
          </n-descriptions-item>
          <n-descriptions-item label="批改状态">
            <n-tag :type="getStatusType(submissionDetail.status)">
              {{ getStatusText(submissionDetail.status) }}
            </n-tag>
          </n-descriptions-item>
          <n-descriptions-item label="提交时间">
            {{ formatDateTime(submissionDetail.submitTime) }}
          </n-descriptions-item>
          <n-descriptions-item label="批改时间">
            {{ formatDateTime(submissionDetail.gradeTime) || "未批改" }}
          </n-descriptions-item>
          <n-descriptions-item label="分数">
            {{ submissionDetail.score || "未评分" }}
          </n-descriptions-item>
          <n-descriptions-item label="满分">
            {{ submissionDetail.maxScore || "未设置" }}
          </n-descriptions-item>
          <n-descriptions-item label="作业内容" span="2">
            <div class="content-box">
              {{ submissionDetail.content || "无内容" }}
            </div>
          </n-descriptions-item>
          <n-descriptions-item label="教师评语" span="2">
            <div class="content-box">
              {{ submissionDetail.teacherComment || "无评语" }}
            </div>
          </n-descriptions-item>
          <n-descriptions-item label="附件" span="2">
            <div v-if="submissionDetail.attachments && submissionDetail.attachments.length > 0">
              <n-space>
                <n-button
                  v-for="attachment in submissionDetail.attachments"
                  :key="attachment.id"
                  size="small"
                  @click="downloadAttachment(attachment)"
                >
                  <template #icon>
                    <i class="i-carbon:document" />
                  </template>
                  {{ attachment.name }}
                </n-button>
              </n-space>
            </div>
            <span v-else>无附件</span>
          </n-descriptions-item>
        </n-descriptions>
      </div>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showDetailModal = false">关闭</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 批改弹窗 -->
    <n-modal
      v-model:show="showGradeModal"
      preset="card"
      title="批改作业"
      size="large"
      :mask-closable="false"
    >
      <n-form
        ref="gradeFormRef"
        :model="gradeForm"
        :rules="gradeRules"
        label-placement="top"
      >
        <n-form-item label="分数" path="score">
          <n-input-number
            v-model:value="gradeForm.score"
            :min="0"
            :max="gradeForm.totalScore"
            placeholder="请输入分数"
            style="width: 100%"
          />
          <template #feedback>
            满分：{{ gradeForm.totalScore }}
          </template>
        </n-form-item>
        <n-form-item label="评语" path="feedback">
          <n-input
            v-model:value="gradeForm.feedback"
            type="textarea"
            placeholder="请输入评语"
            :rows="4"
          />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showGradeModal = false">取消</n-button>
          <n-button type="primary" @click="handleGradeSubmit">
            提交批改
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h } from "vue";
import { NButton, NTag, NPopconfirm, useMessage } from "naive-ui";
import PageHeader from "@/components/common/PageHeader.vue";
import homeworkSubmissionApi from "./api";

const message = useMessage();

// 格式化时间函数
const formatDateTime = (dateString) => {
  if (!dateString) return "";
  
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

// 响应式数据
const loading = ref(false);
const tableData = ref([]);
const showDetailModal = ref(false);
const showGradeModal = ref(false);
const submissionDetail = ref(null);

// 统计数据
const statistics = ref({
  totalSubmissions: 0,
  gradedCount: 0,
  ungradedCount: 0,
  avgScore: 0,
  maxScore: 0,
  minScore: 0,
  passRate: 0,
  excellentRate: 0
});

// 搜索表单
const searchForm = reactive({
  studentId: "",
  homeworkId: "",
  studentName: "",
  homeworkTitle: "",
  status: null,
});

// 批改表单
const gradeForm = reactive({
  id: null,
  score: null,
  totalScore: 100,
  feedback: "",
});

// 批改表单验证规则
const gradeRules = {
  score: [
    { required: true, message: "请输入分数", trigger: "blur" },
    { type: "number", min: 0, message: "分数不能小于0", trigger: "blur" },
  ],
  feedback: [
    { required: true, message: "请输入评语", trigger: "blur" },
  ],
};

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

// 提交状态选项 (根据API返回的status字段)
const statusOptions = ref([
  { label: "已提交待批改", value: 0 },
  { label: "已批改", value: 1 },
]);

// 表格列配置
const columns = [
  {
    title: "提交ID",
    key: "id",
    width: 100,
  },
  {
    title: "学生ID",
    key: "studentId",
    width: 100,
  },
  {
    title: "作业ID",
    key: "homeworkId",
    width: 100,
  },
  {
    title: "作业标题",
    key: "homeworkTitle",
    width: 200,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: "提交状态",
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
    title: "分数",
    key: "score",
    width: 80,
    render(row) {
      return row.score ? `${row.score}/${row.maxScore}` : "未评分";
    },
  },
  {
    title: "提交时间",
    key: "submitTime",
    width: 180,
    render(row) {
      return formatDateTime(row.submitTime);
    },
  },
  {
    title: "操作",
    key: "actions",
    width: 200,
    render(row) {
      const actions = [
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
      ];

      // 如果状态是0（已提交待批改），显示批改按钮
      if (row.status === 0) {
        actions.push(
          h(
            NButton,
            {
              size: "small",
              type: "success",
              ghost: true,
              onClick: () => handleGrade(row),
            },
            {
              default: () => "批改",
              icon: () => h("i", { class: "i-carbon:edit" }),
            }
          )
        );
      }

      actions.push(
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
            default: () => "确定删除这个提交记录吗？",
          }
        )
      );

      return h("div", { class: "flex gap-2" }, actions);
    },
  },
];

// 获取提交状态类型
const getStatusType = (status) => {
  const typeMap = {
    0: "warning", // 已提交待批改
    1: "success", // 已批改
  };
  return typeMap[status] || "default";
};

// 获取提交状态文本
const getStatusText = (status) => {
  const textMap = {
    0: "已提交待批改",
    1: "已批改",
  };
  return textMap[status] || "未知";
};

// 获取作业提交列表
const getSubmissionList = async () => {
  try {
    loading.value = true;
    const params = {
      page: pagination.page,
      size: pagination.pageSize,
      ...searchForm,
    };

    const response = await homeworkSubmissionApi.getSubmissionList(params);
    tableData.value = response.data.list || [];
    pagination.itemCount = response.data.total || 0;
  } catch (error) {
    console.error("获取作业提交列表失败:", error);
    message.error("获取作业提交列表失败");
  } finally {
    loading.value = false;
  }
};

// 获取统计数据
const getStatistics = async () => {
  try {
    const response = await homeworkSubmissionApi.getSubmissionStatistics();
    if (response && response.data) {
      statistics.value = {
        totalSubmissions: response.data.totalSubmissions || 0,
        gradedCount: response.data.gradedCount || 0,
        ungradedCount: response.data.ungradedCount || 0,
        avgScore: response.data.avgScore || 0,
        maxScore: response.data.maxScore || 0,
        minScore: response.data.minScore || 0,
        passRate: response.data.passRate || 0,
        excellentRate: response.data.excellentRate || 0
      };
    }
  } catch (error) {
    console.error("获取统计数据失败:", error);
  }
};

// 查看提交详情
const handleViewDetail = async (submissionId) => {
  try {
    const response = await homeworkSubmissionApi.getSubmissionDetail(submissionId);
    submissionDetail.value = response.data;
    showDetailModal.value = true;
  } catch (error) {
    console.error("获取提交详情失败:", error);
    message.error("获取提交详情失败");
  }
};

// 批改作业
const handleGrade = (row) => {
  gradeForm.id = row.id;
  gradeForm.score = row.score;
  gradeForm.totalScore = row.maxScore || 100;
  gradeForm.feedback = row.teacherComment || "";
  showGradeModal.value = true;
};

// 提交批改
const handleGradeSubmit = async () => {
  try {
    await homeworkSubmissionApi.gradeSubmission(gradeForm.id, {
      score: gradeForm.score,
      feedback: gradeForm.feedback,
    });
    
    message.success("批改成功");
    showGradeModal.value = false;
    await getSubmissionList();
    await getStatistics();
  } catch (error) {
    console.error("批改失败:", error);
    message.error("批改失败");
  }
};

// 删除提交记录
const handleDelete = async (submissionId) => {
  try {
    await homeworkSubmissionApi.deleteSubmission(submissionId);
    message.success("删除成功");
    await getSubmissionList();
    await getStatistics();
  } catch (error) {
    console.error("删除提交记录失败:", error);
    message.error("删除失败");
  }
};

// 下载附件
const downloadAttachment = (attachment) => {
  // 创建下载链接
  const link = document.createElement('a');
  link.href = attachment.url;
  link.download = attachment.name;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

// 搜索
const handleSearch = () => {
  pagination.page = 1;
  getSubmissionList();
};

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    studentId: "",
    homeworkId: "",
    studentName: "",
    homeworkTitle: "",
    status: null,
  });
  pagination.page = 1;
  getSubmissionList();
};

// 分页变化
const handlePageChange = (page) => {
  pagination.page = page;
  getSubmissionList();
};

// 每页大小变化
const handlePageSizeChange = (pageSize) => {
  pagination.pageSize = pageSize;
  pagination.page = 1;
  getSubmissionList();
};

// 导出作业提交数据
const handleExport = async () => {
  try {
    const params = {}
    if (searchForm.studentId) {
      params.studentId = searchForm.studentId
    }
    if (searchForm.homeworkId) {
      params.homeworkId = searchForm.homeworkId
    }
    if (searchForm.studentName) {
      params.studentName = searchForm.studentName
    }
    if (searchForm.homeworkTitle) {
      params.homeworkTitle = searchForm.homeworkTitle
    }
    if (searchForm.status !== null && searchForm.status !== undefined) {
      params.status = searchForm.status
    }
    
    const blob = await homeworkSubmissionApi.exportSubmissions(params)
    
    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `作业提交数据_${Date.now()}.xlsx`
    document.body.appendChild(link)
    link.click()
    
    // 清理
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    message.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    message.error('导出失败')
  }
}

// 初始化
onMounted(() => {
  getSubmissionList();
  getStatistics();
});
</script>

<style scoped>
.homework-submission {
  padding: 16px;
}

/* 强制按钮居右 */
.header-with-button :deep(.page-header-content) {
  display: flex;
  justify-content: space-between;
  width: 100%;
}

.header-with-button :deep(.page-header-extra) {
  margin-left: auto;
  display: flex;
  justify-content: flex-end;
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
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.submission-detail {
  padding: 16px 0;
}

.content-box {
  max-height: 200px;
  overflow-y: auto;
  padding: 8px;
  background-color: #f5f5f5;
  border-radius: 4px;
  white-space: pre-wrap;
}

.flex {
  display: flex;
}

.gap-2 {
  gap: 8px;
}
</style>