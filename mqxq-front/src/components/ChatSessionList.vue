<template>
  <div class="chat-session-list">
    <!-- 头部 -->
    <div class="session-header">
      <h3 class="text-lg font-semibold">我的对话</h3>
      <button @click="createNewSession" class="new-session-btn">
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
        </svg>
        新对话
      </button>
    </div>

    <!-- 会话列表 -->
    <div class="session-list">
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p class="text-sm text-gray-500">加载中...</p>
      </div>

      <div v-else-if="sessions.length === 0" class="empty-state">
        <svg class="w-16 h-16 text-gray-300 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
        </svg>
        <p class="text-gray-500">还没有对话记录</p>
        <p class="text-sm text-gray-400 mt-2">开始一个新对话吧</p>
      </div>

      <div v-else class="space-y-2">
        <div
          v-for="session in sessions"
          :key="session.sessionId"
          class="session-item"
          :class="{ 'session-item-active': currentSessionId === session.sessionId }"
        >
          <div class="session-icon">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                    d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" />
            </svg>
          </div>
          <div class="session-content" @click="selectSession(session)">
            <h4 class="session-title">{{ session.title || '未命名对话' }}</h4>
            <p class="session-time">{{ formatTime(session.updateTime) }}</p>
          </div>
          <button 
            @click.stop="confirmDeleteSession(session)"
            class="delete-session-btn"
            title="删除对话"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                    d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { chatApi } from '@/api/chat'
import type { SessionListItemVO } from '@/api/types/chat'

// Props
interface Props {
  currentSessionId?: string
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  selectSession: [session: SessionListItemVO]
  createSession: []
  deleteSession: [sessionId: string]
}>()

// 状态
const sessions = ref<SessionListItemVO[]>([])
const loading = ref(false)

// 加载会话列表
const loadSessions = async () => {
  loading.value = true
  try {
    const data = await chatApi.getSessionList()
    sessions.value = data || []
  } catch (error) {
    console.error('加载会话列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 选择会话
const selectSession = (session: SessionListItemVO) => {
  emit('selectSession', session)
}

// 创建新会话
const createNewSession = () => {
  emit('createSession')
}

// 删除会话
const deleteSession = async (sessionId: string) => {
  try {
    await chatApi.deleteSession(sessionId)
    
    // 从列表中移除
    sessions.value = sessions.value.filter(s => s.sessionId !== sessionId)
    
    // 通知父组件
    emit('deleteSession', sessionId)
    
    console.log('会话删除成功')
  } catch (error) {
    console.error('删除会话失败:', error)
    throw error
  }
}

// 确认删除会话
const confirmDeleteSession = async (session: SessionListItemVO) => {
  if (confirm(`确定要删除对话"${session.title || '未命名对话'}"吗？此操作不可恢复。`)) {
    try {
      await deleteSession(session.sessionId)
    } catch (error) {
      alert('删除对话失败，请稍后重试')
    }
  }
}

// 格式化时间
const formatTime = (timeStr: string) => {
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  // 小于1分钟
  if (diff < 60 * 1000) {
    return '刚刚'
  }
  
  // 小于1小时
  if (diff < 60 * 60 * 1000) {
    return `${Math.floor(diff / (60 * 1000))}分钟前`
  }
  
  // 小于1天
  if (diff < 24 * 60 * 60 * 1000) {
    return `${Math.floor(diff / (60 * 60 * 1000))}小时前`
  }
  
  // 小于7天
  if (diff < 7 * 24 * 60 * 60 * 1000) {
    return `${Math.floor(diff / (24 * 60 * 60 * 1000))}天前`
  }
  
  // 显示日期
  return date.toLocaleDateString('zh-CN', { 
    month: 'numeric', 
    day: 'numeric' 
  })
}

// 刷新列表
const refresh = () => {
  loadSessions()
}

// 挂载时加载
onMounted(() => {
  loadSessions()
})

// 暴露方法
defineExpose({
  refresh
})
</script>

<style scoped lang="scss">
.chat-session-list {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: white;
}

.session-header {
  padding: 16px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.new-session-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
  }
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 16px;
  text-align: center;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #e5e7eb;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.session-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  transition: all 0.2s;
  border: 1px solid transparent;

  &:hover {
    background: #eff6ff;
    border-color: #bfdbfe;
    
    .delete-session-btn {
      opacity: 1;
    }
  }
}

.session-item-active {
  background: #dbeafe;
  border-color: #3b82f6;
}

.session-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.2);
}

.session-content {
  flex: 1;
  min-width: 0;
  cursor: pointer;
}

.delete-session-btn {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  background: transparent;
  border: none;
  color: #ef4444;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  opacity: 0;
  flex-shrink: 0;

  &:hover {
    background: #fef2f2;
    color: #dc2626;
  }

  &:active {
    transform: scale(0.95);
  }
}

.session-title {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}

.session-time {
  font-size: 12px;
  color: #9ca3af;
}
</style>
