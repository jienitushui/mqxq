<template>
  <div class="manus-chat-container">
    <PageHeader>
      <template #title>AI 课件助手</template>
      <template #subtitle>
        智能辅助制作青少儿编程课件
      </template>
      <template #extra>
        <n-button @click="handleClearChat" :disabled="messages.length === 0">
          <template #icon>
            <i class="i-carbon:clean" />
          </template>
          清空对话
        </n-button>
      </template>
    </PageHeader>

    <n-card class="chat-card">
      <!-- 聊天消息区域 -->
      <div ref="chatContainerRef" class="chat-messages">
        <div v-if="messages.length === 0" class="empty-state">
          <i class="i-carbon:education text-6xl text-gray-300 mb-4" />
          <p class="empty-title">AI 课件助手</p>
          <p class="empty-desc">
            我可以帮你制作适合青少儿的编程课件，包括课程大纲、教学内容、练习题等
          </p>
          <div class="quick-questions">
            <p class="quick-questions-title">💡 试试这些：</p>
            <n-space vertical size="small">
              <n-button
                size="medium"
                secondary
                block
                @click="handleQuickQuestion('帮我设计一个适合8-10岁儿童的Scratch入门课程大纲，生成Word文档')"
              >
                <template #icon>
                  <i class="i-carbon:course" />
                </template>
                设计 Scratch 入门课程大纲
              </n-button>
              <n-button
                size="medium"
                secondary
                block
                @click="handleQuickQuestion('为Python基础课程生成5个适合12岁孩子的编程练习题，生成Word文档')"
              >
                <template #icon>
                  <i class="i-carbon:task-add" />
                </template>
                生成 Python 编程练习题
              </n-button>
              <n-button
                size="medium"
                secondary
                block
                @click="handleQuickQuestion('创建一个关于循环结构的趣味教学案例，用游戏化方式讲解，生成Word文档')"
              >
                <template #icon>
                  <i class="i-carbon:game-console" />
                </template>
                创建趣味教学案例
              </n-button>
            </n-space>
          </div>
        </div>

        <div
          v-for="(msg, index) in messages"
          :key="index"
          :class="['message-item', msg.role]"
        >
          <div class="message-avatar">
            <i
              :class="
                msg.role === 'user'
                  ? 'i-carbon:user-avatar'
                  : 'i-carbon:chat-bot'
              "
            />
          </div>
          <div class="message-content">
            <div class="message-header">
              <span class="message-role">{{
                msg.role === "user" ? "我" : "AI 助手"
              }}</span>
              <span class="message-time">{{ msg.time }}</span>
            </div>
            <div class="message-text">
              <template v-if="msg.role === 'assistant'">
                <div
                  v-for="(step, stepIndex) in msg.steps"
                  :key="stepIndex"
                  class="step-item"
                >
                  <n-tag
                    v-if="step.type === 'step' || step.type === 'download'"
                    size="small"
                    :type="step.isError ? 'error' : step.type === 'download' ? 'success' : 'info'"
                    class="mb-2"
                  >
                    {{ step.label }}
                  </n-tag>
                  <div v-if="step.type === 'download'" class="download-content">
                    <div class="download-info">
                      <i class="i-carbon:document text-lg" />
                      <span class="download-filename">{{ step.content }}</span>
                    </div>
                    <n-button
                      type="primary"
                      size="small"
                      tag="a"
                      :href="step.downloadUrl"
                      target="_blank"
                      class="download-button"
                    >
                      <template #icon>
                        <i class="i-carbon:download" />
                      </template>
                      下载文件
                    </n-button>
                  </div>
                  <div v-else class="step-content">{{ step.content }}</div>
                </div>
                <div v-if="msg.isStreaming" class="streaming-indicator">
                  <n-spin size="small" />
                  <span class="ml-2">正在思考...</span>
                </div>
              </template>
              <template v-else>
                {{ msg.content }}
              </template>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input-area">
        <div class="input-wrapper">
          <n-input
            v-model:value="inputMessage"
            type="textarea"
            placeholder="描述你想制作的课件内容，例如：帮我设计一个适合10岁孩子的Python变量教学课件..."
            :autosize="{ minRows: 1, maxRows: 6 }"
            :disabled="isLoading"
            @keydown.enter="handleKeyDown"
          />
          <div class="input-tips">
            <span class="tip-text">
              <i class="i-carbon:information" />
              按 Enter 发送，Shift + Enter 换行
            </span>
          </div>
        </div>
        <n-button
          type="primary"
          size="large"
          :loading="isLoading"
          :disabled="!inputMessage.trim()"
          @click="handleSendMessage"
          class="send-button"
        >
          <template #icon>
            <i class="i-carbon:send-alt" />
          </template>
          发送
        </n-button>
      </div>
    </n-card>
  </div>
</template>

<script setup>
import { ref, nextTick, onBeforeUnmount } from "vue";
import { useMessage } from "naive-ui";
import { PageHeader } from "@/components";
import { handle401Error } from "@/utils";

const message = useMessage();

// 响应式数据
const messages = ref([]);
const inputMessage = ref("");
const isLoading = ref(false);
const chatContainerRef = ref(null);
let currentEventSource = null;

// 格式化时间
const formatTime = () => {
  const now = new Date();
  return `${now.getHours().toString().padStart(2, "0")}:${now
    .getMinutes()
    .toString()
    .padStart(2, "0")}`;
};

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainerRef.value) {
      chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight;
    }
  });
};

// 解析步骤信息
const parseStep = (data) => {
  // 匹配下载链接（更精确的匹配，只到 .docx 结束）
  const downloadMatch = data.match(/下载链接[：:]\s*(http[s]?:\/\/[^\s\n]+\.docx)/);
  if (downloadMatch) {
    const url = downloadMatch[1];
    // 从 URL 中提取文件名（URL 编码的）
    const fileNameMatch = url.match(/\/([^\/]+\.docx)$/);
    if (fileNameMatch) {
      const encodedFileName = fileNameMatch[1];
      const fileName = decodeURIComponent(encodedFileName);
      
      return {
        type: "download",
        label: "文件已生成",
        content: fileName,
        downloadUrl: url,
        isError: false,
      };
    }
  }

  // 匹配 "Step X: " 格式
  const stepMatch = data.match(/^Step\s+(\d+):\s*(.*)$/i);
  if (stepMatch) {
    const content = stepMatch[2];
    
    // 提取工具名称和返回结果
    const toolMatch = content.match(/工具\s+(\w+)\s+返回的结果[：:]\s*[""](.+?)[""]$/);
    if (toolMatch) {
      const toolName = toolMatch[1];
      const result = toolMatch[2];
      
      // 特殊处理 doTerminate 工具
      if (toolName === 'doTerminate') {
        return {
          type: "step",
          label: "完成",
          content: result,
          isError: false,
        };
      }
      
      // 其他工具只显示工具名称
      return {
        type: "step",
        label: `步骤 ${stepMatch[1]}`,
        content: `工具 ${toolName} 执行完成`,
        isError: false,
      };
    }
    
    // 匹配"思考完成"等简短信息
    if (content.includes("思考完成") || content.includes("任务结束")) {
      return {
        type: "step",
        label: `步骤 ${stepMatch[1]}`,
        content: content.length > 50 ? content.substring(0, 50) + "..." : content,
        isError: false,
      };
    }
    
    // 其他步骤信息，限制长度
    return {
      type: "step",
      label: `步骤 ${stepMatch[1]}`,
      content: content.length > 100 ? content.substring(0, 100) + "..." : content,
      isError: false,
    };
  }

  // 匹配错误信息
  if (data.startsWith("执行错误：") || data.includes("错误")) {
    return {
      type: "step",
      label: "错误",
      content: data.replace("执行错误：", ""),
      isError: true,
    };
  }

  // 匹配完成信息
  if (data.startsWith("执行结束：")) {
    return {
      type: "step",
      label: "完成",
      content: data.replace("执行结束：", ""),
      isError: false,
    };
  }

  // 普通文本，限制长度
  return {
    type: "text",
    label: "",
    content: data.length > 100 ? data.substring(0, 100) + "..." : data,
    isError: false,
  };
};

// 发送消息
const handleSendMessage = async () => {
  const userMessage = inputMessage.value.trim();
  if (!userMessage || isLoading.value) return;

  // 添加用户消息
  messages.value.push({
    role: "user",
    content: userMessage,
    time: formatTime(),
  });

  // 清空输入框
  inputMessage.value = "";
  isLoading.value = true;

  // 添加助手消息占位
  const assistantMessageIndex = messages.value.length;
  messages.value.push({
    role: "assistant",
    steps: [],
    time: formatTime(),
    isStreaming: true,
  });

  scrollToBottom();

  try {
    // 关闭之前的连接
    if (currentEventSource) {
      currentEventSource.close();
    }

    // 使用 fetch + ReadableStream 处理 SSE
    const token = localStorage.getItem("token") || sessionStorage.getItem("token");
    const response = await fetch(
      `/api/teacher/manus/chat?message=${encodeURIComponent(userMessage)}`,
      {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`,
          Accept: "text/event-stream",
        },
      }
    );

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let buffer = "";

    while (true) {
      const { done, value } = await reader.read();

      if (done) {
        console.log("流式响应结束");
        break;
      }

      // 解码数据
      buffer += decoder.decode(value, { stream: true });

      // 按行分割处理 SSE 格式
      const lines = buffer.split("\n");
      buffer = lines.pop() || ""; // 保留最后不完整的行

      for (const line of lines) {
        if (line.startsWith("data:")) {
          // 处理 "data:" 或 "data: " 两种格式
          const data = line.substring(5).trim();
          if (data) {
            const step = parseStep(data);
            messages.value[assistantMessageIndex].steps.push(step);
            scrollToBottom();
          }
        }
      }
    }

    // 标记流式响应结束
    messages.value[assistantMessageIndex].isStreaming = false;
  } catch (error) {
    console.error("发送消息失败:", error);

    // 更新错误消息
    messages.value[assistantMessageIndex].steps.push({
      type: "step",
      label: "错误",
      content: `连接失败: ${error.message}`,
      isError: true,
    });
    messages.value[assistantMessageIndex].isStreaming = false;

    if (error.message.includes("401")) {
      await handle401Error(error, message);
    } else if (error.message.includes("403")) {
      message.error("没有权限访问该功能");
    } else {
      message.error("发送消息失败，请稍后重试");
    }
  } finally {
    isLoading.value = false;
    scrollToBottom();
  }
};

// 处理键盘事件
const handleKeyDown = (e) => {
  // Enter 发送，Shift + Enter 换行
  if (e.key === "Enter" && !e.shiftKey) {
    e.preventDefault();
    handleSendMessage();
  }
};

// 快速提问
const handleQuickQuestion = (question) => {
  inputMessage.value = question;
  handleSendMessage();
};

// 清空对话
const handleClearChat = () => {
  messages.value = [];
  message.success("对话已清空");
};

// 组件卸载时关闭连接
onBeforeUnmount(() => {
  if (currentEventSource) {
    currentEventSource.close();
  }
});
</script>

<style scoped>
.manus-chat-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.chat-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  margin: 16px;
}

.chat-card :deep(.n-card__content) {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  padding: 0;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: #fafafa;
  min-height: 0;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 500px;
  height: 100%;
  padding: 40px 20px;
}

.empty-state i {
  font-size: 64px;
  color: #bbb;
}

.empty-state p {
  margin: 0;
}

.empty-title {
  font-size: 20px;
  font-weight: 600;
  color: #333 !important;
  margin-bottom: 12px !important;
}

.empty-desc {
  font-size: 15px;
  color: #666 !important;
  margin-bottom: 32px !important;
  max-width: 500px;
  line-height: 1.6;
}

.empty-state .text-gray-600 {
  color: #333 !important;
}

.empty-state .text-gray-400 {
  color: #666 !important;
}

.empty-state .text-gray-500 {
  color: #555 !important;
}

.quick-questions {
  max-width: 600px;
  width: 100%;
  margin-top: 32px;
}

.quick-questions-title {
  text-align: center;
  margin-bottom: 16px !important;
  font-size: 14px;
  font-weight: 500;
  color: #555 !important;
}

.quick-questions :deep(.n-space) {
  width: 100%;
}

.quick-questions :deep(.n-button) {
  justify-content: flex-start;
  text-align: left;
  height: auto;
  padding: 12px 16px;
}

.quick-questions :deep(.n-button .n-button__icon) {
  margin-right: 8px;
  font-size: 18px;
  display: flex;
  align-items: center;
}

.quick-questions :deep(.n-button .n-button__content) {
  display: flex;
  align-items: center;
}

.message-item {
  display: flex;
  margin-bottom: 24px;
  animation: fadeIn 0.3s ease-in;
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

.message-item.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.message-item.user .message-avatar {
  background: #2080f0;
  color: white;
  margin-left: 12px;
}

.message-item.assistant .message-avatar {
  background: #18a058;
  color: white;
  margin-right: 12px;
}

.message-content {
  max-width: 75%;
  min-width: 200px;
  flex: 1;
}

.message-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-size: 13px;
  color: #666;
}

.message-role {
  font-weight: 600;
  margin-right: 8px;
  color: #333;
}

.message-time {
  color: #999;
  font-size: 12px;
}

.message-text {
  background: white;
  padding: 14px 18px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  word-wrap: break-word;
  white-space: pre-wrap;
  line-height: 1.6;
  font-size: 14px;
}

.message-item.user .message-text {
  background: #2080f0;
  color: white;
}

.step-item {
  margin-bottom: 16px;
}

.step-item:last-child {
  margin-bottom: 0;
}

.step-item :deep(.n-tag) {
  font-weight: 500;
}

.step-item :deep(.n-tag--info-type) {
  background-color: #e0f2fe;
  color: #0369a1;
  border: none;
}

.step-content {
  padding: 8px 0 0 0;
  line-height: 1.7;
  color: #333;
}

.download-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px;
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: 8px;
  margin-top: 8px;
}

.download-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.download-info i {
  color: #0369a1;
  flex-shrink: 0;
}

.download-filename {
  font-size: 14px;
  color: #0c4a6e;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.download-button {
  flex-shrink: 0;
}

.streaming-indicator {
  display: flex;
  align-items: center;
  color: #2080f0;
  font-size: 13px;
  margin-top: 12px;
  padding: 8px 0;
}

.chat-input-area {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 20px 24px;
  background: white;
  border-top: 1px solid #e8e8e8;
  flex-shrink: 0;
}

.input-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-wrapper :deep(.n-input) {
  font-size: 14px;
}

.input-wrapper :deep(.n-input__textarea-el) {
  line-height: 1.6;
  min-height: 40px;
}

.input-tips {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  font-size: 12px;
  color: #999;
  padding: 0 4px;
}

.tip-text {
  display: flex;
  align-items: center;
  gap: 4px;
}

.send-button {
  align-self: flex-start;
  height: 48px;
  min-width: 88px;
  padding: 0 24px;
  font-size: 14px;
  font-weight: 500;
  margin-top: 0;
}

/* 滚动条样式 */
.chat-messages::-webkit-scrollbar {
  width: 8px;
}

.chat-messages::-webkit-scrollbar-track {
  background: #f0f0f0;
  border-radius: 4px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #c0c0c0;
  border-radius: 4px;
  transition: background 0.2s;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: #a0a0a0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-card {
    margin: 8px;
  }

  .chat-messages {
    padding: 16px;
  }

  .message-content {
    max-width: 85%;
  }

  .quick-questions {
    max-width: 100%;
  }

  .chat-input-area {
    padding: 16px;
  }

  .empty-state {
    min-height: 400px;
  }
}
</style>