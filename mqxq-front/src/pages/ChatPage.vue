<template>
  <div class="chat-page">
    <!-- 侧边栏 - 会话列表 -->
    <div class="chat-sidebar" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
      <button @click="toggleSidebar" class="sidebar-toggle">
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                :d="sidebarCollapsed ? 'M9 5l7 7-7 7' : 'M15 19l-7-7 7-7'" />
        </svg>
      </button>
      
      <ChatSessionList
        v-if="!sidebarCollapsed"
        ref="sessionListRef"
        :current-session-id="currentSessionId"
        @select-session="handleSelectSession"
        @create-session="handleCreateSession"
        @delete-session="handleDeleteSession"
      />
    </div>

    <!-- 主聊天区域 -->
    <div class="chat-main">
      <div v-if="!currentSessionId" class="chat-welcome">
        <div class="welcome-content">
          <div class="welcome-icon">💬</div>
          <h2 class="text-2xl font-bold mb-2">欢迎使用 AI 学习助手</h2>
          <p class="text-gray-600 mb-6">选择一个对话或创建新对话开始聊天</p>
          <button @click="handleCreateSession" class="start-chat-btn">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
            开始新对话
          </button>
        </div>
      </div>

      <div v-else class="chat-container">
        <!-- 聊天头部 -->
        <div class="chat-header">
          <div class="flex items-center gap-3">
            <button @click="goBack" class="back-btn" title="返回">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
              </svg>
            </button>
            <div class="chat-avatar">
              <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 20 20">
                <path d="M2 5a2 2 0 012-2h7a2 2 0 012 2v4a2 2 0 01-2 2H9l-3 3v-3H4a2 2 0 01-2-2V5z" />
              </svg>
            </div>
            <div>
              <h3 class="font-semibold">AI 学习助手</h3>
              <p class="text-xs text-gray-500">在线</p>
            </div>
          </div>
        </div>

        <!-- 消息列表 -->
        <div ref="messagesContainer" class="messages-container">
          <!-- 欢迎消息 -->
          <div v-if="messages.length === 0" class="welcome-message">
            <div class="welcome-icon-small">👋</div>
            <h4 class="text-lg font-semibold mb-2">{{ sessionTitle }}</h4>
            <p class="text-sm text-gray-600 mb-4">{{ sessionDescribe }}</p>
            
            <!-- 热门问题 -->
            <div v-if="hotExamples.length > 0" class="examples-grid">
              <button
                v-for="example in hotExamples"
                :key="example.title"
                @click="sendExample(example.describe)"
                class="example-card"
              >
                <div class="font-medium">{{ example.title }}</div>
                <div class="text-xs text-gray-500 mt-1">{{ example.describe }}</div>
              </button>
            </div>
          </div>

          <!-- 聊天消息 -->
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="message"
            :class="msg.role === 'user' ? 'message-user' : 'message-assistant'"
          >
            <div class="message-wrapper">
              <div v-if="msg.role === 'assistant'" class="message-avatar">
                <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M2 5a2 2 0 012-2h7a2 2 0 012 2v4a2 2 0 01-2 2H9l-3 3v-3H4a2 2 0 01-2-2V5z" />
                </svg>
              </div>
              <div class="message-content">
                <MessageContent :content="msg.content" :params="msg.params" />
              </div>
              
              <!-- 显示工具调用参数（非课程信息） -->
              <div v-if="msg.params && !hasCourseInfo(msg.params)" class="message-params">
                <div class="params-header">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                          d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  相关信息
                </div>
                <pre class="params-content">{{ JSON.stringify(msg.params, null, 2) }}</pre>
              </div>
            </div>
          </div>

          <!-- 加载中 -->
          <div v-if="isLoading" class="message message-assistant">
            <div class="message-wrapper">
              <div class="message-avatar">
                <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M2 5a2 2 0 012-2h7a2 2 0 012 2v4a2 2 0 01-2 2H9l-3 3v-3H4a2 2 0 01-2-2V5z" />
                </svg>
              </div>
              <div class="message-bubble">
                <div class="typing-indicator">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-area">
          <div class="input-wrapper">
            <textarea
              v-model="inputMessage"
              @keydown.enter.exact.prevent="sendMessage"
              placeholder="输入你的问题... (Enter 发送)"
              rows="1"
              class="message-input"
              :disabled="isLoading"
            ></textarea>
            <button
              @click="isLoading ? stopGeneration() : sendMessage()"
              :disabled="!inputMessage.trim() && !isLoading"
              class="send-btn"
              :class="{ 'stop-btn': isLoading }"
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
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { chatApi } from '@/api/chat'
import ChatSessionList from '@/components/ChatSessionList.vue'
import MessageContent from '@/components/MessageContent.vue'
import type { ChatMessage, SessionExample, SessionListItemVO } from '@/api/types/chat'

const route = useRoute()
const router = useRouter()

// 状态
const sidebarCollapsed = ref(false)
const currentSessionId = ref('')
const sessionTitle = ref('你好！我是 AI 学习助手')
const sessionDescribe = ref('我可以帮你解答学习中的问题')
const messages = ref<ChatMessage[]>([])
const hotExamples = ref<SessionExample[]>([])
const inputMessage = ref('')
const isLoading = ref(false)
const messagesContainer = ref<HTMLElement>()
const sessionListRef = ref<InstanceType<typeof ChatSessionList>>()
let cancelChat: (() => void) | null = null

// 切换侧边栏
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 检查 params 是否包含课程信息或订单信息
const hasCourseInfo = (params: Record<string, any> | null | undefined) => {
  if (!params) return false
  return Object.keys(params).some(key => key.startsWith('courseInfo_') || key.startsWith('orderInfo_'))
}

// 创建新会话
const handleCreateSession = async () => {
  try {
    const session = await chatApi.createSession(3)
    currentSessionId.value = session.sessionId
    sessionTitle.value = session.title
    sessionDescribe.value = session.describe
    hotExamples.value = session.examples || []
    messages.value = []
    
    // 刷新会话列表
    sessionListRef.value?.refresh()
  } catch (error) {
    console.error('创建会话失败:', error)
  }
}

// 选择会话
const handleSelectSession = async (session: SessionListItemVO) => {
  currentSessionId.value = session.sessionId
  messages.value = []
  
  // 更新会话标题和描述
  sessionTitle.value = session.title || 'AI 学习助手'
  sessionDescribe.value = '继续你的对话'
  
  // 清空热门问题（已有会话不显示）
  hotExamples.value = []
  
  try {
    // 加载历史消息
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

// 处理删除会话
const handleDeleteSession = (sessionId: string) => {
  // 如果删除的是当前会话，清空当前会话
  if (currentSessionId.value === sessionId) {
    currentSessionId.value = ''
    messages.value = []
    sessionTitle.value = '你好！我是 AI 学习助手'
    sessionDescribe.value = '我可以帮你解答学习中的问题'
    hotExamples.value = []
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
  scrollToBottom()

  let assistantMessage = ''
  let assistantMessageIndex = -1

  try {
    cancelChat = await chatApi.chat(
      {
        question,
        sessionId: currentSessionId.value
      },
      // onMessage
      (data: string) => {
        assistantMessage += data
        
        if (assistantMessageIndex === -1) {
          const newMessage: ChatMessage = {
            id: Date.now().toString(),
            sessionId: currentSessionId.value,
            role: 'assistant',
            content: assistantMessage,
            createdAt: new Date().toISOString()
          }
          messages.value.push(newMessage)
          assistantMessageIndex = messages.value.length - 1
        } else {
          messages.value[assistantMessageIndex].content = assistantMessage
        }
        
        scrollToBottom()
      },
      // onParam
      (params: Record<string, any>) => {
        if (assistantMessageIndex !== -1) {
          messages.value[assistantMessageIndex].params = params
        }
      },
      // onError
      (error: Error) => {
        console.error('聊天错误:', error)
        isLoading.value = false
        cancelChat = null
      },
      // onComplete
      () => {
        isLoading.value = false
        cancelChat = null
        // 刷新会话列表
        sessionListRef.value?.refresh()
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
    if (cancelChat) {
      cancelChat()
      cancelChat = null
    }
    await chatApi.stop(currentSessionId.value)
    isLoading.value = false
  } catch (error) {
    console.error('停止生成失败:', error)
    isLoading.value = false
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

// 初始化 - 检查URL参数
onMounted(async () => {
  const sessionId = route.query.sessionId as string
  if (sessionId) {
    // 如果有会话ID，加载该会话
    try {
      currentSessionId.value = sessionId
      sessionTitle.value = 'AI 学习助手'
      sessionDescribe.value = '继续你的对话'
      hotExamples.value = []
      
      // 加载历史消息
      const history = await chatApi.getSessionMessages(sessionId)
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
      console.error('加载会话失败:', error)
    }
  }
})
</script>

<style scoped lang="scss">
.chat-page {
  display: flex;
  height: 100vh;
  background: #f7f9fc;
}

// 侧边栏
.chat-sidebar {
  width: 300px;
  background: white;
  border-right: 1px solid #e5e7eb;
  position: relative;
  transition: all 0.3s;

  &.sidebar-collapsed {
    width: 60px;
  }
}

.sidebar-toggle {
  position: absolute;
  top: 16px;
  right: -12px;
  width: 24px;
  height: 24px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 10;
  transition: all 0.2s;

  &:hover {
    background: #f7f9fc;
  }
}

// 主聊天区域
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-welcome {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.welcome-content {
  text-align: center;
  max-width: 500px;
  padding: 32px;
}

.welcome-icon {
  font-size: 64px;
  margin-bottom: 24px;
}

.start-chat-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
  }
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
  margin: 16px;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.chat-header {
  padding: 16px 24px;
  border-bottom: 1px solid #e5e7eb;
  background: white;
}

.back-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f7f9fc;
  border: 1px solid #e5e7eb;
  color: #374151;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #e5e7eb;
    color: #667eea;
    border-color: #667eea;
  }
}

.chat-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #667eea;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: #f7f9fc;
}

.welcome-message {
  text-align: center;
  padding: 48px 24px;
}

.welcome-icon-small {
  font-size: 48px;
  margin-bottom: 16px;
}

.examples-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.example-card {
  padding: 16px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  text-align: left;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: #667eea;
    background: #f0f4ff;
    transform: translateY(-2px);
  }
}

.message {
  margin-bottom: 24px;
}

.message-wrapper {
  display: flex;
  gap: 12px;
  max-width: 80%;
}

.message-user {
  display: flex;
  justify-content: flex-end;
}

.message-assistant {
  .message-wrapper {
    flex-direction: row;
  }
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #667eea;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.message-content {
  flex: 1;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  overflow: hidden;
}

.message-user .message-content {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  
  :deep(.text-block) {
    color: white;
  }
  
  :deep(.inline-code) {
    background: rgba(255, 255, 255, 0.2) !important;
    color: white !important;
  }
}

.message-params {
  margin-top: 8px;
  padding: 12px;
  background: #f0f4ff;
  border: 1px solid #c7d2fe;
  border-radius: 8px;
  font-size: 13px;
}

.params-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
  color: #667eea;
  margin-bottom: 8px;
}

.params-content {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  color: #4b5563;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 4px 0;

  span {
    width: 8px;
    height: 8px;
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
    transform: translateY(-10px);
  }
}

.input-area {
  padding: 16px 24px;
  background: white;
  border-top: 1px solid #e5e7eb;
}

.input-wrapper {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.message-input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  font-size: 15px;
  resize: none;
  max-height: 120px;
  font-family: inherit;
  transition: border-color 0.2s;

  &:focus {
    outline: none;
    border-color: #667eea;
  }

  &:disabled {
    background: #f3f4f6;
    cursor: not-allowed;
  }
}

.send-btn {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
    box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.stop-btn {
  background: #ef4444;

  &:hover {
    background: #dc2626;
  }
}
</style>
