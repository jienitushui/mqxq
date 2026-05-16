<template>
  <div class="teacher-course-management h-full flex flex-col">
    <PageHeader class="flex-shrink-0">
      <template #title>我的课程</template>
      <template #extra>
        <n-button type="primary" @click="handleAdd">
          <template #icon>
            <i class="i-carbon:add" />
          </template>
          创建课程
        </n-button>
      </template>
    </PageHeader>

    <!-- 统计卡片 -->
    <div class="flex-shrink-0 mb-4">
      <n-grid :cols="4" :x-gap="16">
        <n-grid-item>
          <n-card>
            <n-statistic label="总课程数" :value="statistics.totalCourses" />
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card>
            <n-statistic
              label="已发布课程"
              :value="statistics.publishedCourses"
            />
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card>
            <n-statistic label="总学员数" :value="statistics.totalStudents" />
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card>
            <n-statistic label="总浏览量" :value="statistics.totalViews" />
          </n-card>
        </n-grid-item>
      </n-grid>
    </div>

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
          <n-form-item label="课程标题" path="title">
            <n-input
              v-model:value="formData.title"
              placeholder="请输入课程标题"
            />
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
          <div class="flex justify-end space-x-2">
            <n-button @click="showModal = false">取消</n-button>
            <n-button
              type="primary"
              :loading="submitLoading"
              @click="handleSubmit"
            >
              确定
            </n-button>
          </div>
        </template>
      </n-card>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h, computed } from "vue";
import { useRouter } from "vue-router";
import {
  NButton,
  NTag,
  NPopconfirm,
  NGrid,
  NGridItem,
  NStatistic,
  useMessage,
} from "naive-ui";
import { PageHeader } from "@/components";
import { formatDateTime, handle401Error } from "@/utils";
import api from "./api";

const message = useMessage();
const router = useRouter();

// 响应式数据
const loading = ref(false);
const editLoading = ref(false); // 新增：编辑时的加载状态
const showModal = ref(false);
const submitLoading = ref(false);
const tableData = ref([]);
const formRef = ref(null);
const subjectOptions = ref([]);
const statistics = ref({
  totalCourses: 0,
  publishedCourses: 0,
  totalStudents: 0,
  totalViews: 0,
});

// 搜索表单
const searchForm = reactive({
  title: "",
  status: null,
});

// 状态选项
const statusOptions = [
  { label: "未发布", value: 0 },
  { label: "已发布", value: 1 },
];

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

// 表单数据
const formData = reactive({
  id: null,
  title: "",
  description: "",
  subjectId: null,
  price: 0,
  cover: "",
  lessonNum: 0,
  durationSum: 0, // 添加视频总时长字段
});

// 封面图片文件列表
const coverFileList = ref([]);

// 表单验证规则
const formRules = {
  title: [
    { required: true, message: "请输入课程标题", trigger: "blur" },
    {
      min: 2,
      max: 100,
      message: "课程标题长度应在2-100个字符之间",
      trigger: "blur",
    },
  ],
  description: [
    { required: true, message: "请输入课程描述", trigger: "blur" },
    {
      min: 10,
      max: 500,
      message: "课程描述长度应在10-500个字符之间",
      trigger: "blur",
    },
  ],
  subjectId: [
    {
      required: true,
      message: "请选择课程分类",
      trigger: "change",
      type: "number",
    },
  ],
  price: [
    {
      required: true,
      message: "请输入课程价格",
      trigger: "blur",
      type: "number",
    },
    { type: "number", min: 0, message: "课程价格不能小于0", trigger: "blur" },
  ],
  lessonNum: [
    {
      required: true,
      message: "请输入总课时",
      trigger: "blur",
      type: "number",
    },
    { type: "number", min: 1, message: "总课时不能小于1", trigger: "blur" },
  ],
  durationSum: [
    { type: "number", min: 0, message: "视频总时长不能小于0", trigger: "blur" },
  ],
};

// 计算属性
const modalTitle = computed(() => (formData.id ? "编辑课程" : "创建课程"));

// 表格列配置
const columns = [
  {
    title: "ID",
    key: "id",
    width: 60,
    fixed: "left",
  },
  {
    title: "课程标题",
    key: "title",
    width: 180,
    fixed: "left",
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: "价格",
    key: "price",
    width: 80,
    render: (row) => `¥${row.price}`,
  },
  {
    title: "课时数",
    key: "lessonNum",
    width: 80,
  },
  {
    title: "销售数",
    key: "buyCount",
    width: 80,
  },
  {
    title: "浏览量",
    key: "viewCount",
    width: 80,
  },
  {
    title: "状态",
    key: "status",
    width: 80,
    render(row) {
      return h(
        NTag,
        {
          type: row.status === 1 ? "success" : "warning",
          size: "small",
        },
        {
          default: () => (row.status === 1 ? "已发布" : "未发布"),
        }
      );
    },
  },
  {
    title: "创建时间",
    key: "createTime",
    width: 150,
    render(row) {
      return formatDateTime(row.createTime) || "-";
    },
  },
  {
    title: "操作",
    key: "actions",
    width: 240,
    fixed: "right",
    render(row) {
      return h("div", { class: "flex space-x-1" }, [
        h(
          NButton,
          {
            size: "small",
            type: "primary",
            secondary: true,
            loading: editLoading.value,
            onClick: () => handleEdit(row),
          },
          { default: () => "编辑" }
        ),
        h(
          NButton,
          {
            size: "small",
            type: "info",
            secondary: true,
            onClick: () => handleManageChapters(row),
          },
          { default: () => "章节" }
        ),
        h(
          NButton,
          {
            size: "small",
            type: row.status === 1 ? "warning" : "success",
            secondary: true,
            onClick: () => handleToggleStatus(row),
          },
          {
            default: () => (row.status === 1 ? "下架" : "发布"),
          }
        ),
        h(
          NPopconfirm,
          {
            onPositiveClick: () => handleDelete(row.id),
          },
          {
            default: () => "确定删除这个课程吗？",
            trigger: () =>
              h(
                NButton,
                {
                  size: "small",
                  type: "error",
                  secondary: true,
                },
                { default: () => "删除" }
              ),
          }
        ),
      ]);
    },
  },
];

// 方法
const loadData = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.page,
      size: pagination.pageSize,
      ...searchForm,
    };
    const res = await api.getList(params);
    const data = res?.data || res || {};
    let courses = data.list || data.records || [];

    // 处理分类名称显示
    if (courses.length > 0 && subjectOptions.value.length > 0) {
      courses = courses.map((course) => ({
        ...course,
        subjectName:
          subjectOptions.value.find((opt) => opt.value === course.subjectId)
            ?.label || "未知分类",
      }));
    }

    tableData.value = courses;
    pagination.itemCount = data.total || data.totalCount || 0;
  } catch (error) {
    message.error("加载数据失败");
    console.error(error);
    tableData.value = [];
    pagination.itemCount = 0;
  } finally {
    loading.value = false;
  }
};

const loadSubjects = async () => {
  try {
    const res = await api.getSubjects();
    const subjects = res?.data || res || [];

    // 转换数据格式以适配 n-tree-select
    const formatSubjects = (items) => {
      return items.map((item) => ({
        id: item.id,
        name: item.name,
        value: item.id,
        label: item.name,
        children: item.children ? formatSubjects(item.children) : undefined,
      }));
    };

    subjectOptions.value = formatSubjects(subjects);

    if (subjectOptions.value.length === 0) {
      message.warning("暂无课程分类数据，请联系管理员");
    }
  } catch (error) {
    console.error("加载分类列表失败", error);
    message.error("加载课程分类失败");
    subjectOptions.value = [];
  }
};

const loadStatistics = async () => {
  try {
    const res = await api.getStatistics();
    const data = res?.data || res || {};
    statistics.value = {
      totalCourses: data.totalCourses || 0,
      publishedCourses: data.publishedCourses || 0,
      totalStudents: data.totalStudents || 0,
      totalViews: data.totalViews || 0,
    };
  } catch (error) {
    console.error("加载统计数据失败", error);
    // 设置默认值
    statistics.value = {
      totalCourses: 0,
      publishedCourses: 0,
      totalStudents: 0,
      totalViews: 0,
    };
  }
};

const handleSearch = () => {
  pagination.page = 1;
  loadData();
};

const handleReset = () => {
  Object.assign(searchForm, {
    title: "",
    status: null,
  });
  pagination.page = 1;
  loadData();
};

const handlePageChange = (page) => {
  pagination.page = page;
  loadData();
};

const handlePageSizeChange = (pageSize) => {
  pagination.pageSize = pageSize;
  pagination.page = 1;
  loadData();
};

const handleAdd = () => {
  // 检查是否有分类数据
  if (subjectOptions.value.length === 0) {
    message.error("暂无课程分类数据，无法创建课程");
    return;
  }

  Object.assign(formData, {
    id: null,
    title: "",
    description: "",
    subjectId: null,
    price: 0,
    cover: "",
    lessonNum: 1,
    durationSum: 0,
  });
  coverFileList.value = [];
  showModal.value = true;
};

// 编辑课程 - 点击编辑按钮时调用获取课程详情API
const handleEdit = async (row) => {
  // 添加加载状态，防止重复点击
  if (editLoading.value) return;

  try {
    console.log("开始编辑课程，ID:", row.id);

    // 显示编辑加载状态
    editLoading.value = true;

    // 调用API获取课程详情
    const res = await api.getDetail(row.id);
    console.log("获取到的课程详情响应:", res);

    // 根据拦截器逻辑，成功的响应直接返回 data 对象
    // 如果响应包含 data 字段，则使用 res.data，否则直接使用 res
    const courseData = res?.data || res;

    if (!courseData) {
      throw new Error("未获取到课程数据");
    }

    console.log("解析出的课程数据:", courseData);

    // 重置表单数据，然后赋值
    Object.assign(formData, {
      id: courseData.id || row.id,
      title: courseData.title || "",
      description: courseData.description || "",
      subjectId: courseData.subjectId || null,
      price: Number(courseData.price) || 0,
      cover: courseData.cover || "",
      lessonNum: Number(courseData.lessonNum) || 1,
      durationSum: Number(courseData.durationSum) || 0,
    });

    console.log("映射后的表单数据:", formData);

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
      ];
    } else {
      coverFileList.value = [];
    }

    // 打开编辑弹窗
    showModal.value = true;

  } catch (error) {
    console.error("获取课程详情失败:", error);

    // 提供更详细的错误信息
    if (error?.response) {
      const { status, data } = error.response;
      if (status === 404) {
        message.error("课程不存在或已被删除");
      } else if (status === 403) {
        message.error("没有权限查看此课程");
      } else if (status === 401) {
        await handle401Error(error, message);
      } else if (status === 500) {
        message.error("服务器内部错误，请稍后重试");
      } else {
        message.error(data?.message || `获取课程详情失败 (${status})`);
      }
    } else if (error?.code) {
      // 处理拦截器返回的错误格式
      message.error(error.message || "获取课程详情失败");
    } else if (error?.message) {
      if (error.message.includes("Network Error")) {
        message.error("网络连接失败，请检查网络设置");
      } else {
        message.error(error.message);
      }
    } else {
      message.error("获取课程详情失败，请稍后重试");
    }
  } finally {
    // 关闭编辑加载状态
    editLoading.value = false;
  }
};

const handleSubmit = async () => {
  try {
    // 先进行表单验证
    await formRef.value?.validate();

    submitLoading.value = true;

    // 打印调试信息
    console.log("提交的表单数据:", formData);

    // 准备提交的数据
    const submitData = {
      title: formData.title?.trim(),
      description: formData.description?.trim(),
      subjectId: formData.subjectId,
      price: Number(formData.price),
      lessonNum: Number(formData.lessonNum),
      durationSum: Number(formData.durationSum || 0),
      cover: formData.cover?.trim() || "",
    };

    console.log("处理后的提交数据:", submitData);

    if (formData.id) {
      await api.update(formData.id, submitData);
      message.success("更新成功");
    } else {
      await api.create(submitData);
      message.success("创建成功");
    }

    showModal.value = false;
    loadData();
    loadStatistics(); // 刷新统计数据
  } catch (error) {
    console.error("提交失败:", error);

    // 详细的错误处理
    if (error?.response) {
      const { status, data } = error.response;
      console.error("HTTP错误:", status, data);

      if (status === 400) {
        message.error(data?.message || "请求参数错误，请检查输入数据");
      } else if (status === 401) {
        await handle401Error(error, message);
      } else if (status === 403) {
        message.error("没有权限执行此操作");
      } else if (status === 500) {
        message.error("服务器内部错误，请稍后重试");
      } else {
        message.error(data?.message || `请求失败 (${status})`);
      }
    } else if (error?.message) {
      if (error.message.includes("Network Error")) {
        message.error("网络连接失败，请检查网络设置");
      } else {
        message.error(error.message);
      }
    } else {
      message.error("操作失败，请稍后重试");
    }
  } finally {
    submitLoading.value = false;
  }
};

const handleDelete = async (id) => {
  try {
    await api.delete(id);
    message.success("删除成功");
    loadData();
  } catch (error) {
    message.error("删除失败");
    console.error(error);
  }
};

const handleToggleStatus = async (row) => {
  try {
    const action = row.status === 1 ? "取消发布" : "发布";

    if (row.status === 1) {
      await api.unpublish(row.id);
    } else {
      await api.publish(row.id);
    }

    message.success(`${action}成功`);
    loadData();
  } catch (error) {
    message.error("操作失败");
    console.error(error);
  }
};

const handleManageChapters = (row) => {
  // 跳转到章节管理页面，带着课程信息
  try {
    // 使用 router.push 进行跳转
    router
      .push({
        path: "/teacherhome/chapter",
        query: {
          courseId: row.id,
          courseName: row.title,
        },
      })
      .catch((err) => {
        console.error("路由跳转错误:", err);
        // 如果路由跳转失败，使用 window.location 作为备选方案
        const url = `#/teacherhome/chapter?courseId=${
          row.id
        }&courseName=${encodeURIComponent(row.title)}`;
        window.location.href = url;
      });
  } catch (error) {
    console.error("跳转失败:", error);
    message.error("跳转失败，请稍后重试");
  }
};

// 图片上传相关方法
const handleBeforeUpload = (data) => {
  // data.file 是 UploadFileInfo 对象, data.file.file 才是原生 File 对象
  const rawFile = data.file.file;

  if (!rawFile) {
    message.error('无法读取文件，请重试');
    return false;
  }

  const isImage = rawFile.type?.startsWith("image/");
  if (!isImage) {
    message.error("只能上传图片文件");
    return false;
  }

  // 检查文件大小
  if (rawFile.size === undefined || rawFile.size === null) {
    message.error("无法获取文件大小，请重新选择文件");
    return false;
  }

  const isLt5M = rawFile.size / 1024 / 1024 < 5;
  if (!isLt5M) {
    message.error("图片大小不能超过5MB");
    return false;
  }

  return true;
};

const handleUploadFinish = ({ file, event }) => {
  try {
    // 接口直接返回 URL 字符串，不需要 JSON.parse
    const imageUrl = event.target.response;
    console.log("上传响应:", imageUrl);
    if (imageUrl && typeof imageUrl === 'string' && imageUrl.startsWith('http')) {
      formData.cover = imageUrl;
      message.success("图片上传成功");
      file.status = 'finished';
      file.url = imageUrl;
    } else {
      console.error("无效的图片URL:", imageUrl);
      message.error("上传成功但未获取到有效的图片地址");
    }
  } catch (error) {
    console.error("上传处理失败:", error);
    message.error("图片上传失败");
  }
};

// 通用文件删除函数
const deleteFileFromServer = async (fileUrl) => {
  try {
    if (!fileUrl) return;

    // 从URL中提取文件路径，例如：http://localhost:9900/mxng/png/xxx.png -> mxng/png/xxx.png
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

const handleRemoveImage = async (file) => {
  try {
    // 如果有文件URL，调用删除接口
    if (file.url || formData.cover) {
      const fileUrl = file.url || formData.cover;
      await deleteFileFromServer(fileUrl);
      message.success('图片删除成功');
    }

    formData.cover = "";
    coverFileList.value = [];
    return true;
  } catch (error) {
    console.error('删除图片失败:', error);
    message.error('删除图片失败');
    return false;
  }
};

const handleUploadError = ({ file, event }) => {
  console.error("上传失败:", event);
  message.error("图片上传失败，请重试");

  // 移除失败的文件
  const index = coverFileList.value.findIndex(f => f.id === file.id);
  if (index > -1) {
    coverFileList.value.splice(index, 1);
  }
};

// 生命周期
onMounted(() => {
  loadData();
  loadSubjects();
  loadStatistics();
});
</script>

<style scoped>
.teacher-course-management {
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
