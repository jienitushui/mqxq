<template>
  <div class="batch-delete-test">
    <h3>批量删除功能测试</h3>
    
    <div class="test-section">
      <h4>1. 测试数据</h4>
      <pre>{{ JSON.stringify(testData, null, 2) }}</pre>
    </div>
    
    <div class="test-section">
      <h4>2. 选中的ID</h4>
      <p>选中数量: {{ selectedIds.length }}</p>
      <p>选中ID: {{ selectedIds.join(', ') }}</p>
    </div>
    
    <div class="test-section">
      <h4>3. 测试表格</h4>
      <el-table
        :data="testData"
        @selection-change="handleSelectionChange"
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="courseId" label="课程ID" width="100" />
        <el-table-column prop="courseTitle" label="课程标题" />
      </el-table>
    </div>
    
    <div class="test-section">
      <h4>4. 批量操作</h4>
      <el-button 
        type="danger" 
        @click="testBatchDelete"
        :disabled="selectedIds.length === 0"
      >
        测试批量删除 ({{ selectedIds.length }})
      </el-button>
    </div>
    
    <div class="test-section">
      <h4>5. 请求日志</h4>
      <div class="log-container">
        <div v-for="(log, index) in logs" :key="index" class="log-item">
          <span class="log-time">{{ log.time }}</span>
          <span class="log-type" :class="log.type">{{ log.type }}</span>
          <span class="log-message">{{ log.message }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi } from '../api/user'
import type { CourseView } from '../api/types/course'

// 测试数据
const testData = ref<CourseView[]>([
  {
    id: 244,
    userId: 18,
    courseId: 20,
    viewTime: '2026-01-17 14:09:22',
    ipAddress: '127.0.0.1',
    createTime: '2026-01-17 14:02:22',
    courseTitle: '测试课程1',
    courseDescription: '这是一个测试课程'
  },
  {
    id: 245,
    userId: 18,
    courseId: 21,
    viewTime: '2026-01-17 15:09:22',
    ipAddress: '127.0.0.1',
    createTime: '2026-01-17 15:02:22',
    courseTitle: '测试课程2',
    courseDescription: '这是另一个测试课程'
  }
])

const selectedIds = ref<number[]>([])
const logs = ref<Array<{ time: string, type: string, message: string }>>([])

// 添加日志
const addLog = (type: string, message: string) => {
  logs.value.unshift({
    time: new Date().toLocaleTimeString(),
    type,
    message
  })
  
  // 限制日志数量
  if (logs.value.length > 10) {
    logs.value = logs.value.slice(0, 10)
  }
}

// 选择变化
const handleSelectionChange = (selection: CourseView[]) => {
  selectedIds.value = selection.map(item => item.id)
  addLog('INFO', `选中了 ${selection.length} 条记录: [${selectedIds.value.join(', ')}]`)
}

// 测试批量删除
const testBatchDelete = async () => {
  try {
    addLog('INFO', `开始批量删除，ID列表: [${selectedIds.value.join(', ')}]`)
    
    // 模拟API调用
    await userApi.batchDeleteCourseViews(selectedIds.value)
    
    addLog('SUCCESS', '批量删除成功')
    ElMessage.success('批量删除成功')
    
    // 从测试数据中移除已删除的记录
    testData.value = testData.value.filter(item => !selectedIds.value.includes(item.id))
    selectedIds.value = []
    
  } catch (error) {
    addLog('ERROR', `批量删除失败: ${error}`)
    ElMessage.error('批量删除失败')
    console.error('批量删除失败:', error)
  }
}
</script>

<style scoped>
.batch-delete-test {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.test-section {
  margin-bottom: 30px;
  padding: 15px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.test-section h4 {
  margin: 0 0 15px 0;
  color: #1f2937;
}

.log-container {
  max-height: 200px;
  overflow-y: auto;
  background: #f9fafb;
  padding: 10px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 12px;
}

.log-item {
  display: flex;
  gap: 10px;
  margin-bottom: 5px;
  padding: 2px 0;
}

.log-time {
  color: #6b7280;
  min-width: 80px;
}

.log-type {
  min-width: 60px;
  font-weight: bold;
}

.log-type.INFO {
  color: #3b82f6;
}

.log-type.SUCCESS {
  color: #10b981;
}

.log-type.ERROR {
  color: #ef4444;
}

.log-message {
  flex: 1;
  color: #1f2937;
}

pre {
  background: #f3f4f6;
  padding: 10px;
  border-radius: 4px;
  font-size: 12px;
  overflow-x: auto;
}
</style>