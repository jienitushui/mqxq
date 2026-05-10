<template>
  <div class="ai-assistant">
    <!-- 悬浮按钮 -->
    <transition name="bounce">
      <div v-if="!isOpen" class="ai-fab-container">
        <div class="ai-fab-tooltip" :class="{ 'tooltip-login': !authStore.isAuthenticated }">
          <span v-if="authStore.isAuthenticated">有问题？问我吧！</span>
          <span v-else>登录后开始聊天</span>
        </div>
        <button
          @click="toggleChat"
          class="ai-fab"
          :class="{ 'ai-fab-pulse': !hasInteracted }"
          :title="authStore.isAuthenticated ? 'AI 学习助手' : '登录后使用 AI 学习助手'"
        >
          <svg v-if="authStore.isAuthenticated" class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                  d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" />
          </svg>
          <svg v-else class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                  d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
          </svg>
        </button>
      </div>
    </transition>

    <!-- 聊天窗口 -->
    <transition name="slide-up">
      <div 
        v-if="isOpen" 
        class="ai-chat-window"
        :style="{ width: windowWidth + 'px', height: windowHeight + 'px' }"
      >
        <!-- 调整大小手柄 -->
        <div class="resize-handles">
          <div class="resize-handle resize-left" @mousedown="startResize($event, 'left')"></div>
          <div class="resize-handle resize-right" @mousedown="startResize($event, 'right')"></div>
          <div class="resize-handle resize-top" @mousedown="startResize($event, 'top')"></div>
          <div class="resize-handle resize-bottom" @mousedown="startResize($event, 'bottom')"></div>
          <div class="resize-handle resize-top-left" @mousedown="startResize($event, 'top-left')"></div>
          <div class="resize-handle resize-top-right" @mousedown="startResize($event, 'top-right')"></div>
          <div class="resize-handle resize-bottom-left" @mousedown="startResize($event, 'bottom-left')"></div>
          <div class="resize-handle resize-bottom-right" @mousedown="startResize($event, 'bottom-right')"></div>
        </div>
        
        <!-- 头部 -->
        <div class="ai-chat-header">
          <div class="flex items-center gap-2">
            <div class="ai-avatar">
              <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                <path d="M2 5a2 2 0 012-2h7a2 2 0 012 2v4a2 2 0 01-2 2H9l-3 3v-3H4a2 2 0 01-2-2V5z" />
                <path d="M15 7v2a4 4 0 01-4 4H9.828l-1.766 1.767c.28.149.599.233.938.233h2l3 3v-3h2a2 2 0 002-2V9a2 2 0 00-2-2h-1z" />
              </svg>
            </div>
            <div>
              <h3 class="font-semibold text-white">AI 学习助手</h3>
              <p class="text-xs text-blue-100">随时为您解答</p>
            </div>
          </div>
          <div class="flex items-center gap-2">
            <button @click="toggleHistory" class="ai-history-btn" :class="{ active: showHistory }" title="历史记录">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                      d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </button>
            <button @click="createNewSession" class="ai-new-btn" title="新建会话">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
              </svg>
            </button>
            <button @click="openFullPage" class="ai-expand-btn" title="打开完整页面">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                      d="M4 8V4m0 0h4M4 4l5 5m11-1V4m0 0h-4m4 0l-5 5M4 16v4m0 0h4m-4 0l5-5m11 5l-5-5m5 5v-4m0 4h-4" />
              </svg>
            </button>
            <button @click="toggleChat" class="ai-close-btn">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>

        <!-- 历史记录面板 -->
        <transition name="slide-down">
          <div v-if="showHistory" class="ai-history-panel">
            <div class="history-header">
              <h4 class="text-sm font-semibold text-gray-700">历史会话</h4>
              <button @click="loadSessionHistory" class="refresh-history-btn" :disabled="isRefreshing">
                <svg 
                  class="w-4 h-4 transition-transform"
                  :class="{ 'animate-spin': isRefreshing }"
                  fill="none" 
                  stroke="currentColor" 
                  viewBox="0 0 24 24"
                >
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                        d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                </svg>
              </button>
            </div>
            <div class="history-list">
              <div v-if="sessionHistory.length === 0" class="history-empty">
                <p class="text-sm text-gray-400">暂无历史记录</p>
              </div>
              <div
                v-for="session in sessionHistory"
                :key="session.sessionId"
                class="history-item"
                :class="{ active: currentSessionId === session.sessionId }"
              >
                <div class="history-item-content" @click="loadHistorySession(session)">
                  <h5 class="history-item-title">{{ session.title || '未命名会话' }}</h5>
                  <p class="history-item-time">{{ formatTime(session.updateTime) }}</p>
                </div>
                <button 
                  @click.stop="confirmDeleteSession(session)"
                  class="delete-session-btn"
                  title="删除会话"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                          d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                </button>
              </div>
            </div>
          </div>
        </transition>

        <!-- 消息列表 -->
        <div ref="messagesContainer" class="ai-messages">
          <!-- 欢迎消息 -->
          <div v-if="messages.length === 0" class="ai-welcome">
            <div class="ai-welcome-icon">👋</div>
            <h4 class="text-lg font-semibold mb-2">{{ sessionTitle }}</h4>
            <p class="text-sm text-gray-600 mb-4">{{ sessionDescribe }}</p>
            
            <!-- 热门问题 -->
            <div v-if="hotExamples.length > 0" class="space-y-2">
              <div class="flex items-center justify-between mb-2">
                <p class="text-xs text-gray-500">试试这些问题：</p>
                <button 
                  @click="refreshExamples"
                  class="flex items-center gap-1 text-xs text-blue-500 hover:text-blue-600 transition-colors"
                  :disabled="isRefreshing"
                >
                  <svg 
                    class="w-4 h-4 transition-transform"
                    :class="{ 'animate-spin': isRefreshing }"
                    fill="none" 
                    stroke="currentColor" 
                    viewBox="0 0 24 24"
                  >
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                  </svg>
                  换一换
                </button>
              </div>
              <button
                v-for="example in hotExamples"
                :key="example.title + example.describe"
                @click="sendExample(example.describe)"
                class="ai-example-btn"
              >
                <div class="font-medium">{{ example.title }}</div>
                <div class="text-xs text-gray-500 mt-1">{{ example.describe }}</div>
              </button>
            </div>
          </div>

          <!-- 聊天消息 -->
          <div
            v-for="(msg, index) in messages"
            :key="index"
            class="ai-message"
            :class="msg.role === 'user' ? 'ai-message-user' : 'ai-message-assistant'"
          >
            <div class="ai-message-content">
              <div v-if="msg.role === 'assistant'" class="ai-message-avatar">
                <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M2 5a2 2 0 012-2h7a2 2 0 012 2v4a2 2 0 01-2 2H9l-3 3v-3H4a2 2 0 01-2-2V5z" />
                </svg>
              </div>
              <MessageContent :content="msg.content" :params="msg.params" />
            </div>
          </div>

          <!-- 加载中 -->
          <div v-if="isLoading" class="ai-message ai-message-assistant">
            <div class="ai-message-content">
              <div class="ai-message-avatar">
                <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M2 5a2 2 0 012-2h7a2 2 0 012 2v4a2 2 0 01-2 2H9l-3 3v-3H4a2 2 0 01-2-2V5z" />
                </svg>
              </div>
              <div class="ai-message-bubble">
                <div class="ai-typing">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入框 -->
        <div class="ai-input-area">
          <div class="ai-input-wrapper">
            <textarea
              v-model="inputMessage"
              @keydown.enter.exact.prevent="sendMessage"
              placeholder="输入你的问题..."
              rows="1"
              class="ai-input"
              :disabled="isLoading"
            ></textarea>
            <button
              @click="isLoading ? stopGeneration() : sendMessage()"
              :disabled="!inputMessage.trim() && !isLoading"
              class="ai-send-btn"
              :class="{ 'ai-stop-btn': isLoading }"
            >
              <svg v-if="!isLoading" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
              </svg>
              <svg v-else class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                <rect x="6" y="6" width="12" height="12" rx="1" />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store'
import { chatApi } from '@/api/chat'
import MessageContent from '@/components/MessageContent.vue'
import type { SessionExample, ChatMessage } from '@/api/types/chat'

const router = useRouter()
const authStore = useAuthStore()

// 状态
const isOpen = ref(false)
const hasInteracted = ref(false)
const showHistory = ref(false)
const messages = ref<ChatMessage[]>([])
const inputMessage = ref('')
const isLoading = ref(false)
const isRefreshing = ref(false)
const currentSessionId = ref('')
const sessionTitle = ref('你好！我是 AI 学习助手')
const sessionDescribe = ref('我可以帮你解答学习中的问题')
const hotExamples = ref<SessionExample[]>([])
const sessionHistory = ref<any[]>([])
const messagesContainer = ref<HTMLElement>()
let cancelChat: (() => void) | null = null

// 窗口大小状态
const windowWidth = ref(680)
const windowHeight = ref(700)
const minWidth = 680
const minHeight = 500
const maxWidth = 900
const maxHeight = 900

// 调整大小相关
let isResizing = false
let resizeDirection = ''
let startX = 0
let startY = 0
let startWidth = 0
let startHeight = 0

// 开始调整大小
const startResize = (e: MouseEvent, direction: string) => {
  e.preventDefault()
  isResizing = true
  resizeDirection = direction
  startX = e.clientX
  startY = e.clientY
  startWidth = windowWidth.value
  startHeight = windowHeight.value

  document.addEventListener('mousemove', handleResize)
  document.addEventListener('mouseup', stopResize)
  document.body.style.cursor = getCursor(direction)
  document.body.style.userSelect = 'none'
}

// 处理调整大小
const handleResize = (e: MouseEvent) => {
  if (!isResizing) return

  const deltaX = startX - e.clientX
  const deltaY = startY - e.clientY

  if (resizeDirection.includes('left')) {
    const newWidth = startWidth + deltaX
    if (newWidth >= minWidth && newWidth <= maxWidth) {
      windowWidth.value = newWidth
    }
  }

  if (resizeDirection.includes('right')) {
    const newWidth = startWidth - deltaX
    if (newWidth >= minWidth && newWidth <= maxWidth) {
      windowWidth.value = newWidth
    }
  }

  if (resizeDirection.includes('top')) {
    const newHeight = startHeight + deltaY
    if (newHeight >= minHeight && newHeight <= maxHeight) {
      windowHeight.value = newHeight
    }
  }

  if (resizeDirection.includes('bottom')) {
    const newHeight = startHeight - deltaY
    if (newHeight >= minHeight && newHeight <= maxHeight) {
      windowHeight.value = newHeight
    }
  }
}

// 停止调整大小
const stopResize = () => {
  isResizing = false
  resizeDirection = ''
  document.removeEventListener('mousemove', handleResize)
  document.removeEventListener('mouseup', stopResize)
  document.body.style.cursor = ''
  document.body.style.userSelect = ''
}

// 获取光标样式
const getCursor = (direction: string) => {
  const cursors: Record<string, string> = {
    'left': 'ew-resize',
    'right': 'ew-resize',
    'top': 'ns-resize',
    'bottom': 'ns-resize',
    'top-left': 'nwse-resize',
    'top-right': 'nesw-resize',
    'bottom-left': 'nesw-resize',
    'bottom-right': 'nwse-resize'
  }
  return cursors[direction] || 'default'
}

// 切换聊天窗口
const toggleChat = () => {
  // 检查用户是否已登录
  if (!authStore.isAuthenticated) {
    // 未登录，跳转到登录页面
    const currentPath = window.location.pathname + window.location.search
    router.push({
      name: 'Login',
      query: { redirect: currentPath }
    })
    return
  }

  isOpen.value = !isOpen.value
  hasInteracted.value = true
  
  if (isOpen.value && !currentSessionId.value) {
    initSession()
    loadSessionHistory()
  }
}

// 切换历史记录面板
const toggleHistory = () => {
  showHistory.value = !showHistory.value
  if (showHistory.value && sessionHistory.value.length === 0) {
    loadSessionHistory()
  }
}

// 加载会话历史
const loadSessionHistory = async () => {
  isRefreshing.value = true
  try {
    const history = await chatApi.getSessionList()
    sessionHistory.value = history || []
  } catch (error) {
    console.error('加载历史记录失败:', error)
  } finally {
    isRefreshing.value = false
  }
}

// 加载历史会话
const loadHistorySession = async (session: any) => {
  currentSessionId.value = session.sessionId
  sessionTitle.value = session.title || 'AI 学习助手'
  sessionDescribe.value = '继续你的对话'
  hotExamples.value = []
  messages.value = []
  showHistory.value = false
  
  try {
    const history = await chatApi.getSessionMessages(session.sessionId)
    messages.value = history.map((msg, index) => ({
      id: `${session.sessionId}-${index}`,
      sessionId: session.sessionId,
      role: msg.type === 'USER' ? 'user' : 'assistant',
      content: msg.content,
      params: msg.params,
      createdAt: new Date().toISOString()
    }))
    
    scrollToBottom()
  } catch (error) {
    console.error('加载历史消息失败:', error)
  }
}

// 删除会话
const deleteSession = async (sessionId: string) => {
  try {
    await chatApi.deleteSession(sessionId)
    
    // 从历史记录中移除
    sessionHistory.value = sessionHistory.value.filter(s => s.sessionId !== sessionId)
    
    // 如果删除的是当前会话，创建新会话
    if (currentSessionId.value === sessionId) {
      await createNewSession()
    }
    
    console.log('会话删除成功')
  } catch (error) {
    console.error('删除会话失败:', error)
    throw error
  }
}

// 确认删除会话
const confirmDeleteSession = async (session: any) => {
  if (confirm(`确定要删除会话"${session.title || '未命名会话'}"吗？此操作不可恢复。`)) {
    try {
      await deleteSession(session.sessionId)
    } catch (error) {
      alert('删除会话失败，请稍后重试')
    }
  }
}

// 格式化时间
const formatTime = (timeStr: string) => {
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60 * 1000) return '刚刚'
  if (diff < 60 * 60 * 1000) return `${Math.floor(diff / (60 * 1000))}分钟前`
  if (diff < 24 * 60 * 60 * 1000) return `${Math.floor(diff / (60 * 60 * 1000))}小时前`
  if (diff < 7 * 24 * 60 * 60 * 1000) return `${Math.floor(diff / (24 * 60 * 60 * 1000))}天前`
  
  return date.toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' })
}

// 打开完整页面
const openFullPage = () => {
  // 检查用户是否已登录
  if (!authStore.isAuthenticated) {
    // 未登录，跳转到登录页面
    const currentPath = window.location.pathname + window.location.search
    router.push({
      name: 'Login',
      query: { redirect: currentPath }
    })
    return
  }

  // 关闭悬浮窗
  isOpen.value = false
  // 跳转到聊天页面，并传递当前会话ID
  if (currentSessionId.value) {
    router.push({
      path: '/chat',
      query: { sessionId: currentSessionId.value }
    })
  } else {
    router.push('/chat')
  }
}

// 初始化会话
const initSession = async () => {
  try {
    const session = await chatApi.createSession(3)
    currentSessionId.value = session.sessionId
    sessionTitle.value = session.title || '你好！我是 AI 学习助手'
    sessionDescribe.value = session.describe || '我可以帮你解答学习中的问题'
    hotExamples.value = session.examples || []
  } catch (error) {
    console.error('创建会话失败:', error)
  }
}

// 创建新会话
const createNewSession = async () => {
  // 清空当前消息
  messages.value = []
  // 创建新会话
  await initSession()
}

// 刷新热门问题
const refreshExamples = async () => {
  if (isRefreshing.value) return
  
  isRefreshing.value = true
  try {
    const examples = await chatApi.getHotExamples(3)
    hotExamples.value = examples || []
  } catch (error) {
    console.error('刷新热门问题失败:', error)
  } finally {
    isRefreshing.value = false
  }
}

// 发送示例问题
const sendExample = (describe: string) => {
  inputMessage.value = describe
  sendMessage()
}

// 发送消息
const sendMessage = async () => {
  const question = inputMessage.value.trim()
  if (!question || isLoading.value) return

  // 添加用户消息
  const userMessage: ChatMessage = {
    id: Date.now().toString(),
    sessionId: currentSessionId.value,
    role: 'user',
    content: question,
    createdAt: new Date().toISOString()
  }
  messages.value.push(userMessage)

  inputMessage.value = ''
  isLoading.value = true

  // 滚动到底部
  scrollToBottom()

  let fullMessage = ''
  let assistantMessageIndex = -1

  try {
    // 调用流式聊天 API
    cancelChat = await chatApi.chat(
      {
        question,
        sessionId: currentSessionId.value
      },
      // onMessage: 接收每个数据片段
      (data: string) => {
        fullMessage += data
        
        // 更新或添加助手消息
        if (assistantMessageIndex === -1) {
          const newMessage: ChatMessage = {
            id: Date.now().toString(),
            sessionId: currentSessionId.value,
            role: 'assistant',
            content: fullMessage,
            createdAt: new Date().toISOString()
          }
          messages.value.push(newMessage)
          assistantMessageIndex = messages.value.length - 1
        } else {
          messages.value[assistantMessageIndex].content = fullMessage
        }
        
        scrollToBottom()
      },
      // onParam: 接收工具调用参数
      (params: Record<string, any>) => {
        // 将参数附加到助手消息
        if (assistantMessageIndex !== -1) {
          messages.value[assistantMessageIndex].params = params
        }
        console.log('收到工具调用参数:', params)
      },
      // onError: 错误处理
      (error: Error) => {
        console.error('聊天错误:', error)
        isLoading.value = false
        cancelChat = null
        
        // 添加错误提示消息
        const errorMessage: ChatMessage = {
          id: Date.now().toString(),
          sessionId: currentSessionId.value,
          role: 'assistant',
          content: '抱歉，发生了错误，请稍后重试。',
          createdAt: new Date().toISOString()
        }
        messages.value.push(errorMessage)
      },
      // onComplete: 完成处理
      () => {
        isLoading.value = false
        cancelChat = null
      }
    )
  } catch (error) {
    console.error('发送消息失败:', error)
    isLoading.value = false
    cancelChat = null
  }
}

// 停止生成
const stopGeneration = async () => {
  try {
    // 取消流式请求
    if (cancelChat) {
      cancelChat()
      cancelChat = null
    }
    // 调用后端停止接口
    await chatApi.stop(currentSessionId.value)
    isLoading.value = false
  } catch (error) {
    console.error('停止生成失败:', error)
    isLoading.value = false
  }
}

// 加载历史消息
const loadHistoryMessages = async (sessionId: string) => {
  try {
    const history = await chatApi.getSessionMessages(sessionId)
    
    // 转换历史消息格式
    messages.value = history.map((msg, index) => ({
      id: `${sessionId}-${index}`,
      sessionId: sessionId,
      role: msg.type === 'USER' ? 'user' : 'assistant',
      content: msg.content,
      params: msg.params,
      createdAt: new Date().toISOString()
    }))
    
    scrollToBottom()
  } catch (error) {
    console.error('加载历史消息失败:', error)
  }
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

// 监听消息变化，自动滚动
watch(() => messages.value.length, () => {
  scrollToBottom()
})

// 暴露方法供外部调用
defineExpose({
  toggleChat,
  loadHistoryMessages
})
</script>

<style scoped lang="scss">
.ai-assistant {
  position: fixed;
  bottom: 120px;
  right: 120px;
  z-index: 1000;
}

// 悬浮按钮容器
.ai-fab-container {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

// 悬浮按钮
.ai-fab {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  border: none;
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.4);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;

  &:hover {
    transform: scale(1.1) translateY(-2px);
    box-shadow: 0 12px 32px rgba(59, 130, 246, 0.6);
  }

  &:active {
    transform: scale(0.95);
  }
}

.ai-fab-pulse {
  animation: pulse 2s infinite;
}

// 提示文字
.ai-fab-tooltip {
  position: relative;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  padding: 14px 24px;
  border-radius: 20px;
  font-size: 16px;
  font-weight: 600;
  white-space: nowrap;
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.5);
  pointer-events: none;
  animation: tooltip-bounce 2s ease-in-out infinite;
  letter-spacing: 0.3px;
  
  &::after {
    content: '';
    position: absolute;
    bottom: -10px;
    left: 50%;
    transform: translateX(-50%);
    width: 0;
    height: 0;
    border-top: 10px solid #2563eb;
    border-left: 10px solid transparent;
    border-right: 10px solid transparent;
  }
  
  span {
    display: flex;
    align-items: center;
    gap: 8px;
    
    &::before {
      content: '💬';
      font-size: 22px;
    }
  }
}

// 登录提示样式
.tooltip-login {
  span::before {
    content: '🔐';
  }
}

@keyframes tooltip-bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-8px);
  }
}

// 提示文字动画
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(-50%) translateX(10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-50%) translateX(10px);
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 8px 24px rgba(59, 130, 246, 0.4);
  }
  50% {
    box-shadow: 0 8px 32px rgba(59, 130, 246, 0.8), 0 0 0 8px rgba(59, 130, 246, 0.2);
  }
}

// 聊天窗口
.ai-chat-window {
  position: relative;
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  backdrop-filter: blur(10px);
}

// 调整大小手柄
.resize-handles {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 10;
}

.resize-handle {
  position: absolute;
  pointer-events: all;
  
  &.resize-left,
  &.resize-right {
    width: 8px;
    top: 0;
    bottom: 0;
    cursor: ew-resize;
  }
  
  &.resize-left {
    left: 0;
  }
  
  &.resize-right {
    right: 0;
  }
  
  &.resize-top,
  &.resize-bottom {
    height: 8px;
    left: 0;
    right: 0;
    cursor: ns-resize;
  }
  
  &.resize-top {
    top: 0;
  }
  
  &.resize-bottom {
    bottom: 0;
  }
  
  &.resize-top-left,
  &.resize-top-right,
  &.resize-bottom-left,
  &.resize-bottom-right {
    width: 16px;
    height: 16px;
  }
  
  &.resize-top-left {
    top: 0;
    left: 0;
    cursor: nwse-resize;
  }
  
  &.resize-top-right {
    top: 0;
    right: 0;
    cursor: nesw-resize;
  }
  
  &.resize-bottom-left {
    bottom: 0;
    left: 0;
    cursor: nesw-resize;
  }
  
  &.resize-bottom-right {
    bottom: 0;
    right: 0;
    cursor: nwse-resize;
    
    &::after {
      content: '';
      position: absolute;
      bottom: 4px;
      right: 4px;
      width: 8px;
      height: 8px;
      border-right: 2px solid #cbd5e1;
      border-bottom: 2px solid #cbd5e1;
    }
  }
}

// 头部
.ai-chat-header {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.15);
}

.ai-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.ai-close-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.3);
  }
}

.ai-expand-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.3);
  }
}

.ai-new-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.3);
    transform: rotate(90deg);
  }
}

.ai-history-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.3);
  }

  &.active {
    background: rgba(255, 255, 255, 0.4);
  }
}

// 历史记录面板
.ai-history-panel {
  background: #f7f9fc;
  border-bottom: 1px solid #e5e7eb;
  max-height: 200px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.history-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #e5e7eb;
  background: white;
}

.refresh-history-btn {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: transparent;
  border: none;
  color: #3b82f6;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;

  &:hover:not(:disabled) {
    background: #eff6ff;
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.history-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.history-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.history-item {
  padding: 10px 12px;
  background: white;
  border-radius: 8px;
  margin-bottom: 6px;
  transition: all 0.2s;
  border: 1px solid transparent;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;

  &:hover {
    background: #eff6ff;
    border-color: #3b82f6;
    
    .delete-session-btn {
      opacity: 1;
    }
  }

  &.active {
    background: #dbeafe;
    border-color: #3b82f6;
  }
}

.history-item-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
  cursor: pointer;
}

.delete-session-btn {
  width: 24px;
  height: 24px;
  border-radius: 4px;
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

.history-item-title {
  font-size: 13px;
  font-weight: 500;
  color: #374151;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.history-item-time {
  font-size: 11px;
  color: #9ca3af;
}

// 滑动动画
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s ease;
}

.slide-down-enter-from {
  max-height: 0;
  opacity: 0;
}

.slide-down-leave-to {
  max-height: 0;
  opacity: 0;
}

// 消息区域
.ai-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: linear-gradient(to bottom, #f7f9fc 0%, #ffffff 100%);
  
  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-track {
    background: transparent;
  }
  
  &::-webkit-scrollbar-thumb {
    background: #d1d5db;
    border-radius: 3px;
    
    &:hover {
      background: #9ca3af;
    }
  }
}

.ai-welcome {
  text-align: center;
  padding: 40px 20px;
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.ai-welcome-icon {
  font-size: 56px;
  margin-bottom: 20px;
  animation: wave 2s ease-in-out infinite;
}

@keyframes wave {
  0%, 100% {
    transform: rotate(0deg);
  }
  25% {
    transform: rotate(20deg);
  }
  75% {
    transform: rotate(-20deg);
  }
}

.ai-example-btn {
  width: 100%;
  padding: 14px;
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  text-align: left;
  font-size: 14px;
  color: #374151;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);

  &:hover {
    border-color: #3b82f6;
    background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2);
  }

  &:active {
    transform: translateY(0);
  }
}

// 消息
.ai-message {
  margin-bottom: 16px;
}

.ai-message-content {
  display: flex;
  gap: 8px;
  max-width: 85%;
}

.ai-message-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.2);
}

.ai-message-user {
  display: flex;
  justify-content: flex-end;

  .ai-message-content {
    flex-direction: row-reverse;
  }

  :deep(.message-content-wrapper) {
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(59, 130, 246, 0.2);
  }

  :deep(.text-block) {
    color: white;
    padding: 10px 14px;
    font-size: 14px;
  }

  :deep(.inline-code) {
    background: rgba(255, 255, 255, 0.2) !important;
    color: white !important;
  }
}

.ai-message-assistant {
  :deep(.message-content-wrapper) {
    background: white;
    border: 1px solid #e5e7eb;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  }

  :deep(.text-block) {
    color: #374151;
    padding: 10px 14px;
    font-size: 14px;
  }

  :deep(.code-block) {
    margin: 0;
    border-radius: 0;
  }
}

// 输入中动画
.ai-typing {
  display: flex;
  gap: 4px;
  padding: 4px 0;

  span {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: #9ca3af;
    animation: typing 1.4s infinite;

    &:nth-child(2) {
      animation-delay: 0.2s;
    }

    &:nth-child(3) {
      animation-delay: 0.4s;
    }
  }
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-8px);
  }
}

// 输入区域
.ai-input-area {
  padding: 16px;
  background: white;
  border-top: 1px solid #e5e7eb;
}

.ai-input-wrapper {
  display: flex;
  gap: 8px;
  align-items: flex-end;
}

.ai-input {
  flex: 1;
  padding: 10px 12px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  resize: none;
  max-height: 100px;
  font-family: inherit;
  transition: all 0.2s;

  &:focus {
    outline: none;
    border-color: #3b82f6;
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
  }

  &:disabled {
    background: #f3f4f6;
    cursor: not-allowed;
  }
}

.ai-send-btn {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  flex-shrink: 0;

  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.ai-stop-btn {
  background: #ef4444;

  &:hover {
    background: #dc2626;
  }
}

// 动画
.bounce-enter-active {
  animation: bounce-in 0.5s;
}

.bounce-leave-active {
  animation: bounce-out 0.3s;
}

@keyframes bounce-in {
  0% {
    transform: scale(0);
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
  }
}

@keyframes bounce-out {
  0% {
    transform: scale(1);
  }
  100% {
    transform: scale(0);
  }
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from {
  transform: translateY(20px);
  opacity: 0;
}

.slide-up-leave-to {
  transform: translateY(20px);
  opacity: 0;
}

// 响应式
@media (max-width: 640px) {
  .ai-assistant {
    bottom: 16px;
    right: 16px;
  }

  .ai-chat-window {
    width: calc(100vw - 32px);
    height: calc(100vh - 100px);
  }
}
</style>
