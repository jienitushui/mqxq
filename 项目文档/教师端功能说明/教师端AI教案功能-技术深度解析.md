# 教师端AI教案功能 — 技术深度解析

## 一、功能概述

教师端AI教案模块基于 **YuManus 超级智能体** 实现，采用 ReAct（Reasoning + Acting）循环模式，通过7个工具自主规划、多步执行任务，最终生成 Word 文档(.docx)供教师下载。前端通过 SSE 流式接收 Agent 每一步的执行过程，实时展示思考与工具调用结果。

本系统包含**两套独立的 AI 子系统**：教师端 YuManus 智能体（本篇重点）和用户端在线客服 AI（作对比参考）。

### 功能清单

| 功能 | 前端页面/组件 | 后端端点 | 认证要求 |
|---|---|---|---|
| 流式对话（SSE） | manus-chat/index.vue | `GET /api/teacher/manus/chat` | `@SaCheckRole("教师")` |
| 下载生成的文件 | manus-chat/index.vue | `GET /api/teacher/manus/download/{fileType}/{fileName}` | `@SaCheckRole("教师")` |
| 手动清理临时文件 | 未对接 | `POST /api/teacher/manus/cleanup` | `@SaCheckRole("教师")` |
| 文件目录统计 | 未对接 | `GET /api/teacher/manus/file-stats` | `@SaCheckRole("教师")` |

### 两套 AI 系统对比

> 完整的两套 AI 子系统对比见 [学生端AI助手-ChatClient vs YuManus对比](../学生端功能说明/学生端AI助手功能-技术深度解析.md#四学生端-chatclient-vs-教师端-yumanus-对比)。教师端 YuManus 特点：ReAct 多步循环 + 7工具 + SseEmitter + 无持久化 + Word 文件生成；用户端 ChatClient 特点：Spring AI 流式 + RAG 向量检索 + Redis/MySQL 双层持久化 + CourseTools。

### Agent 继承链

```
BaseAgent (基础代理抽象类)
  ├── 状态管理: IDLE / RUNNING / FINISHED / ERROR
  ├── 执行循环: while (state != FINISHED && step < maxSteps)
  ├── SSE 流式: CompletableFuture.runAsync() + SseEmitter
  └── 消息优化: limitMessageHistory() + compressToolResponse()
      │
      └── ReActAgent (ReAct 模式抽象类)
          ├── think(): boolean — 思考并决定是否需要行动
          └── act(): String — 执行行动
              │
              └── ToolCallAgent (工具调用代理)
                  ├── think(): 调用大模型 → 解析 ToolCall → 判断是否需执行
                  ├── act(): 执行工具调用 → 处理返回结果
                  ├── JSON 修复: fixToolCallArgumentsWithReflection
                  └── 任务完成检测: isTaskCompleted()
                      │
                      └── YuManus (AI 超级智能体)
                          ├── 7 个工具
                          ├── maxSteps: 13
                          ├── maxMessageHistory: 15
                          └── maxToolResponseLength: 2000
```

---

## 二、前端关键逻辑

### 2.1 AI 课件助手页（manus-chat/index.vue）

#### 页面布局

```
┌──────────────────────────────────────────────────────────────┐
│ PageHeader "AI 课件助手"                                     │
│ 副标题: "智能辅助制作青少儿编程课件"           [清空对话按钮]   │
├──────────────────────────────────────────────────────────────┤
│ .chat-messages (消息列表区域, 可滚动)                          │
│                                                              │
│ ┌─ 空状态 ──────────────────────────────────────────────┐    │
│ │ 🤖 AI 课件助手                                         │    │
│ │ "我可以帮你制作编程课件..."                             │    │
│ │ 快速提问:                                              │    │
│ │ [设计Scratch入门课程大纲] [Python练习题] [循环教学案例]  │    │
│ └────────────────────────────────────────────────────────┘    │
│                                                              │
│ ┌─ 用户消息 (蓝色背景 #2080f0) ────────────────────────┐    │
│ │ 👤 用户  HH:mm                                       │    │
│ │ 消息文本                                              │    │
│ └────────────────────────────────────────────────────────┘    │
│                                                              │
│ ┌─ AI 消息 (白色背景) ────────────────────────────────┐    │
│ │ 🤖 AI 课件助手  HH:mm                                │    │
│ │ ┌─ step-item (info 标签) ─────────────────────────┐  │    │
│ │ │ 步骤 1: 思考中...                               │  │    │
│ │ └─────────────────────────────────────────────────┘  │    │
│ │ ┌─ step-item (info 标签) ─────────────────────────┐  │    │
│ │ │ 步骤 2: 工具 WebSearchTool 返回的结果：...      │  │    │
│ │ └─────────────────────────────────────────────────┘  │    │
│ │ ┌─ step-item (download) ──────────────────────────┐  │    │
│ │ │ 📄 文件已生成: 课件.docx                        │  │    │
│ │ │ [⬇ 下载文件] (链接按钮)                         │  │    │
│ │ └─────────────────────────────────────────────────┘  │    │
│ └────────────────────────────────────────────────────────┘    │
├──────────────────────────────────────────────────────────────┤
│ .chat-input-area (输入区域)                                   │
│ ┌──────────────────────────────────────┬──────────┐          │
│ │ n-input (textarea)                   │ 发送按钮  │          │
│ └──────────────────────────────────────┴──────────┘          │
└──────────────────────────────────────────────────────────────┘
```

#### 数据状态

```javascript
const messages = ref([])           // 消息列表
const inputMessage = ref("")       // 输入框绑定值
const isLoading = ref(false)       // 是否正在等待AI响应
const chatContainerRef = ref(null) // 聊天容器DOM引用
let currentEventSource = null      // ⚠ 冗余：声明但未被赋值（fetch 替代了 EventSource）
```

#### 消息数据结构

**用户消息**：
```javascript
{
  role: "user",
  content: "帮我设计一个Scratch入门课程大纲",
  time: "14:30"
}
```

**AI 助手消息**：
```javascript
{
  role: "assistant",
  steps: [
    { type: "step", label: "步骤 1", content: "正在搜索相关资料...", isError: false },
    { type: "step", label: "步骤 2", content: "工具 WebSearchTool 返回的结果：...", isError: false },
    { type: "download", label: "文件已生成", content: "Scratch入门课程大纲.docx",
      downloadUrl: "http://xxx/api/teacher/manus/download/word/Scratch入门课程大纲.docx" },
    { type: "text", label: "完成", content: "课件已生成完毕", isError: false }
  ],
  time: "14:30",
  isStreaming: true  // 流式接收中为 true，接收完毕设为 false
}
```

#### 数据加载流程 — SSE 流式请求

```
用户输入消息 → handleSendMessage()
│
├── 1. 添加用户消息到 messages
├── 2. 创建 AI 助手消息占位（steps: [], isStreaming: true）
├── 3. isLoading = true
│
├── 4. 发起 SSE 请求
│   ├── const token = localStorage.getItem("token") || sessionStorage.getItem("token")
│   ├── const response = await fetch(
│   │     `/api/teacher/manus/chat?message=${encodeURIComponent(userMessage)}`,
│   │     { method: "GET", headers: { Authorization: `Bearer ${token}`, Accept: "text/event-stream" } }
│   │   )
│   │
│   └── 5. 流式读取
│       ├── const reader = response.body.getReader()
│       ├── const decoder = new TextDecoder()
│       ├── let buffer = ""
│       │
│       ├── while (true):
│       │   ├── { done, value } = await reader.read()
│       │   ├── if (done) break
│       │   ├── buffer += decoder.decode(value, { stream: true })
│       │   ├── lines = buffer.split("\n")
│       │   ├── buffer = lines.pop()  // 保留不完整行
│       │   │
│       │   └── for each line:
│       │       ├── if (line.startsWith("data:"))
│       │       │   ├── data = line.substring(5).trim()
│       │       │   └── step = parseStep(data) → push to assistant message steps
│       │       └── scrollToBottom()
│       │
│       └── 6. 流结束
│           ├── isStreaming = false
│           ├── isLoading = false
│           └── if (steps 为空) → 添加默认步骤 "AI 正在思考..."
│
└── 7. 错误处理
    ├── response.status === 401 → handle401Error() 弹出重新登录确认
    ├── response.status === 403 → "没有权限访问该功能"
    └── 其他 → "发送消息失败，请稍后重试"
```

#### ⚠ 使用原生 fetch 而非 axios

manus-chat 目录下**没有 `api.js` 文件**。API 调用直接使用原生 `fetch` 写在 `index.vue` 中，原因：
- SSE 流式响应需要 `ReadableStream` 支持
- 项目封装的 `request`（axios）不原生支持 SSE
- `EventSource` API 不支持自定义 `Authorization` header

#### SSE 数据解析逻辑（parseStep 函数）

解析后端 SSE 返回的 `data:` 行内容，按优先级匹配：

| 优先级 | 匹配规则 | 返回 type | 示例 |
|---|---|---|---|
| 1 | `下载链接[：:] http...docx` | `download` | `下载链接：http://xxx/word/课件.docx` |
| 2 | `Step N: ...` | `step` | `Step 1: 工具 WebSearchTool 返回的结果："..."` |
| 3 | 以 `执行错误：` 开头或包含 `错误` | `step` + isError | `执行错误：连接超时` |
| 4 | 以 `执行结束：` 开头 | `text` + label="完成" | `执行结束：任务完成` |
| 5 | 其他文本 | `text` | 默认，超100字符截断 |

下载链接解析正则：`/下载链接[：:]\s*(http[s]?:\/\/[^\s\n]+\.docx)/`

**步骤内容截断**：超过100字符的步骤内容截断显示。

### 2.2 快速提问

3 个预设问题：

| # | 预设问题 |
|---|---|
| 1 | "帮我设计一个适合8-10岁儿童的Scratch入门课程大纲，生成Word文档" |
| 2 | "为Python基础课程生成5个适合12岁孩子的编程练习题，生成Word文档" |
| 3 | "创建一个关于循环结构的趣味教学案例，用游戏化方式讲解，生成Word文档" |

点击后调用 `handleQuickQuestion(question)` 填入输入框并立即发送。

### 2.3 文件下载

当 SSE 步骤中解析出 `.docx` 下载链接时，渲染为下载按钮（`n-button` + `tag="a"` + `target="_blank"`），直接链接到后端返回的 URL。

下载 URL 格式：`http://{host}:{port}/api/teacher/manus/download/word/{fileName}`

### 2.4 路由与导航

| 路由 | 权限码 | 菜单名 | 菜单组 |
|---|---|---|---|
| `/teacherhome/manus-chat` | CoursewareAssistant | AI 课件助手 | AIAssistant (order:5) |

AI 助手菜单组下仅有 "AI 课件助手" 一项。仅对 "教师" 角色可见（在 `teacherPermissions` 中定义），管理员端无此功能入口。

### 2.5 Vite 代理配置

开发环境下 `/api` 请求被代理到 `http://159.75.11.181:9999`（后端服务器）。

### 2.6 样式要点

| 元素 | 样式 |
|---|---|
| 用户消息 | 蓝色背景 #2080f0，白色文字 |
| AI 消息 | 白色背景 |
| AI 头像 | 绿色 #18a058 |
| 用户头像 | 蓝色 |
| 步骤标签 info | 浅蓝 #e0f2fe |
| 步骤标签 error | 红色 |
| 步骤标签 success | 绿色 |
| 下载区域 | 浅蓝背景 #f0f9ff，蓝色边框 #bae6fd |
| 响应式 | 768px 以下移动端适配 |
| 消息动画 | fadeIn 0.3s |

---

## 三、后端执行流程

### 3.1 Sa-Token 认证架构

> Sa-Token 认证架构详见 [认证系统指南](../公共参考/认证系统指南.md) 及 [公共参考-认证与权限体系](../../公共参考/公共数据模型与基础配置.md#二认证与权限体系sa-token)

**控制器级别鉴权**：`ManusTeacherController → @SaCheckRole("教师")`，所有端点需登录 + 教师角色。

| 端点 | 类注解 | 实际认证 |
|---|---|---|
| `GET /api/teacher/manus/chat` | `@SaCheckRole("教师")` | 需登录+教师角色 |
| `GET /api/teacher/manus/download/{fileType}/{fileName}` | `@SaCheckRole("教师")` | 需登录+教师角色 |
| `POST /api/teacher/manus/cleanup` | `@SaCheckRole("教师")` | 需登录+教师角色 |
| `GET /api/teacher/manus/file-stats` | `@SaCheckRole("教师")` | 需登录+教师角色 |

⚠ 下载端点无教师归属校验：任意教师可下载他人 Agent 生成的文件（文件路径为全局临时目录）。

### 3.2 ManusTeacherController 端点详析

| # | HTTP方法 | 路径 | 方法签名 | 参数 | 说明 |
|---|---|---|---|---|---|
| 1 | GET | `/chat` | `doChatWithManus(@RequestParam String message)` | message(必填) | 流式调用 Manus 智能体 |
| 2 | GET | `/download/{fileType}/{fileName}` | `downloadFile(fileType, fileName)` | fileType(word/pdf/file), fileName | 下载 Agent 生成的文件 |
| 3 | POST | `/cleanup` | `cleanupTempFiles()` | 无 | 手动触发临时文件清理 |
| 4 | GET | `/file-stats` | `getFileStats()` | 无 | 获取文件目录统计 |

### 3.3 流式对话（`GET /api/teacher/manus/chat`）

```
请求参数: message (必填, @RequestParam)
│
┌─ Sa-Token 校验: @SaCheckRole("教师")
│
┌─ 步骤1: 创建 YuManus 实例
│  └── YuManus yuManus = new YuManus(dashscopeChatModel, allTools, myLoggerAdvisor)
│      ⚠ 每次请求新建实例，无会话状态保持
│
┌─ 步骤2: 启动流式执行
│  └── return yuManus.runStream(message)
│
└─ 内部执行 (BaseAgent.runStream):
    ├── 创建 SseEmitter(300000L)  ← 5分钟超时
    ├── CompletableFuture.runAsync() → 异步执行 ReAct 循环
    │
    ├── while (state != FINISHED && stepNumber < maxSteps(13)):
    │   │
    │   ├── think()  ──────────────────────────────────────────┐
    │   │   ├── 将 nextStepPrompt 加入 messageList              │
    │   │   ├── limitMessageHistory() — 保留首条+最近15条       │
    │   │   ├── 调用 chatClient.prompt()                        │
    │   │   │   .system(systemPrompt)                           │
    │   │   │   .tools(availableTools)                          │
    │   │   │   .call().chatResponse()                          │
    │   │   ├── 解析 AssistantMessage 中的 ToolCall 列表        │
    │   │   ├── 修复 JSON 参数 (fixToolCallArgumentsWithReflection) │
    │   │   ├── 无 ToolCall → consecutiveNoToolCallCount++      │
    │   │   │   ├── isTaskCompleted() → FINISHED               │
    │   │   │   └── 连续3次无 ToolCall → FINISHED              │
    │   │   └── 有 ToolCall → return true (需要 act)           │
    │   │                                                        │
    │   ├── act()  ───────────────────────────────────────────┤ │
    │   │   ├── 预修复所有 ToolCall 的 JSON 参数               │ │
    │   │   ├── 构建 Prompt + toolCallingManager.executeToolCalls() │
    │   │   ├── compressToolResponses() — 超过2000字符截断     │ │
    │   │   ├── 更新 messageList (助手消息+工具响应)           │ │
    │   │   ├── 检查是否调用了 doTerminate → FINISHED          │ │
    │   │   └── 返回工具调用结果字符串                         │ │
    │   │                                                        │
    │   ├── sseEmitter.send(result)  ← 推送当前步骤结果         │
    │   └── 检查 sseConnectionActive → 连接关闭则停止循环      │
    │                                                            │
    └── finally: sseEmitter.complete() / sseEmitter.error()
```

#### ReAct 循环示例

```
用户输入: "帮我设计一个适合8-10岁儿童的Scratch入门课程大纲"

Step 1 (think): 大模型判断需要搜索 Scratch 课程设计资料
Step 2 (act):   调用 WebSearchTool.searchWeb("Scratch入门课程大纲 儿童编程")
                → 返回搜索结果摘要

Step 3 (think): 大模型判断需要获取网页详细内容
Step 4 (act):   调用 WebScrapingTool.scrapeWebPage("https://...")
                → 返回网页文本（限制10000字符）

Step 5 (think): 大模型整合资料，判断需要生成 Word 文档
Step 6 (act):   调用 WordGenerationTool.generateWordWithAlternatingTextAndImages(
                  fileName="Scratch入门课程大纲.docx",
                  textContents=["# Scratch入门课程大纲", "## 第一课...", ...],
                  imagePaths=[]
                )
                → 返回下载链接: http://xxx/api/teacher/manus/download/word/Scratch入门课程大纲.docx

Step 7 (think): 任务完成，无需更多操作
Step 8 (act):   调用 TerminateTool.doTerminate()
                → 状态设为 FINISHED
```

### 3.4 BaseAgent — 消息历史管理

#### limitMessageHistory()

```
messageList 长度 > maxMessageHistory(15) 时:
│
├── 保留第一条 UserMessage（初始任务描述）
├── 保留最近 maxMessageHistory 条消息
├── 确保 ToolResponseMessage 总与 AssistantMessage 配对
│   └── 移除孤立的 ToolResponseMessage
└── ⚠ 此策略可能导致中间推理步骤丢失，影响长任务连贯性
```

#### compressToolResponse()

```
工具返回结果长度 > maxToolResponseLength(2000) 时:
│
├── 保留开头 maxToolResponseLength/2 字符（即1000字符）
├── 中间替换为 "... (内容已压缩，原始长度: N 字符，已截断) ..."
└── 保留结尾 (maxToolResponseLength - maxToolResponseLength/2 - 100) 字符（即900字符）
    ⚠ 文档原描述为保留开头/结尾各500字符，实际为动态计算：headLength = maxToolResponseLength/2, tailLength = maxToolResponseLength - headLength - 100
```

### 3.5 ToolCallAgent — JSON 修复机制

大模型返回的 ToolCall 参数 JSON 常存在格式问题，通过多级回退修复：

```
fixToolCallArgumentsWithReflection()
│
├── 第1级: fixJsonString() — 正则修复
│   ├── 转义未转义的换行符、制表符等控制字符
│   └── 修复嵌套数组结构问题
│
├── 第2级: 结构修复
│   └── 尝试重新解析 JSON
│
├── 第3级: 激进修复
│   └── 更激进的修复策略
│
└── 使用反射修改 ToolCall 的 arguments 字段
    ⚠ 反射修改不可变对象，依赖 JVM 实现细节
```

### 3.6 ToolCallAgent — 任务完成检测

```typescript
isTaskCompleted(result):
│
├── 匹配中文完成关键词: "任务完成", "已完成", "完成", "任务结束", "已结束", "结束",
│   "任务已全部完成", "所有任务已完成", "工作完成", "处理完成",
│   "执行完成", "操作完成", "已完成所有", "全部完成"
├── 匹配英文完成关键词: "finished", "completed", "done", "task completed", "all done"
├── 排除包含继续关键词的情况:
│   "还需要", "接下来", "下一步", "继续", "待处理"
│   ⚠ continuationKeywords 数组中 "还需要" 被重复6次，疑为复制粘贴错误
│   ⚠ 文档原描述含 "还需要继续"/"but"/"however"/"yet"，实际代码中不存在
└── 仅当匹配完成关键词且不包含继续关键词时返回 true
```

### 3.7 工具定义详析

#### ToolRegistration — 工具注册

`@Configuration` 类，`@Bean allTools()` 方法注册7个工具：

| # | Tool 类 | 能力 | 参数 | 风险等级 |
|---|---|---|---|---|
| 1 | FileOperationTool | 读写文件 | `readFile(fileName)`, `writeFile(fileName, content)` | ⚠ 高 — 可读写服务器任意文件 |
| 2 | WebSearchTool | 网页搜索 | `searchWeb(query)` — SearchAPI + 百度引擎，前5条 | 低 |
| 3 | WebScrapingTool | 网页抓取 | `scrapeWebPage(url)` — Jsoup 抓取，限制10000字符 | 低 |
| 4 | ResourceDownloadTool | 资源下载 | `downloadResource(url, fileName)` — 下载到 tmp/download | ⚠ 中 — 可下载任意URL到服务器 |
| 5 | TerminalOperationTool | 执行终端命令 | `executeTerminalCommand(command)` — cmd.exe /c | ⚠ **极高** — 可执行任意系统命令 |
| 6 | WordGenerationTool | 生成Word文档 | `generateWordWithAlternatingTextAndImages(fileName, textContents, imagePaths)` | 低 |
| 7 | TerminateTool | 终止任务 | `doTerminate()` — 无参数 | 低 |

#### FileOperationTool 详析

```
readFile(fileName)
│
├── 读取工作目录下文件
│   └── ⚠ 路径未做安全限制，可读取服务器任意文件
│
└── 返回文件内容字符串

writeFile(fileName, content)
│
├── 写入文件到工作目录
│   └── ⚠ 路径未做安全限制，可写入服务器任意位置
│
└── 返回 "文件已写入: fileName"
```

#### TerminalOperationTool 详析

```
executeTerminalCommand(command)
│
├── ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command)
│   ⚠ 文档原写 Runtime.getRuntime().exec()，实际已改为 ProcessBuilder
│
├── 读取进程输出流和错误流
│
├── process.waitFor()  ⚠ 无超时参数，将无限期阻塞等待进程结束
│   ⚠ 文档原描述"超过60秒强制终止"不正确，实际无任何超时机制
│
└── 返回命令输出字符串

⚠ 极高危: 可执行任意系统命令，且无超时保护
   例: "del /s /q C:\\" 或 "rm -rf /" 或 "net user hacker ..."
   无任何命令白名单或黑名单过滤
```

#### WordGenerationTool 详析

```
generateWordWithAlternatingTextAndImages(fileName, textContents, imagePaths)
│
├── 创建 Word 文档（使用 Apache POI 或类似库）
│
├── Markdown → Word 格式转换:
│   ├── 标题: # → Heading1, ## → Heading2, ### → Heading3
│   ├── 列表: - → Bullet, 1. → Numbered
│   ├── 粗体: **text** → Bold
│   ├── 斜体: *text* → Italic
│   ├── 代码块: ``` → Monospace/Code style
│   ├── 中文标题: 一、→ Heading1, 二、→ Heading2
│   └── 图片: 插入 imagePaths 中的图片
│
├── 保存到 tmp/word/{fileName}
│   ⚠ WordGenerationTool 内含 sanitizeFileName() 方法（移除 \/:*?"<>|、空格→下划线、
│     限制长度200字符、确保 .docx 后缀），文件名安全性有基本保障
│
└── 返回下载链接: "下载链接：http://{host}:{port}/api/teacher/manus/download/word/{fileName}"
    ⚠ host 和 port 从配置注入，生产环境需正确配置
```

#### WebSearchTool 详析

```
searchWeb(query)
│
├── 使用 SearchAPI（需 apiKey 配置）
│
├── 搜索引擎: 百度
│
├── 返回前5条结果:
│   { title, link, snippet }
│
└── 格式化为文本: "标题: xxx\n链接: xxx\n摘要: xxx\n---"
```

#### WebScrapingTool 详析

```
scrapeWebPage(url)
│
├── 使用 Jsoup 抓取网页
│
├── 内容提取:
│   ├── 标题 (title 标签)
│   ├── 段落 (p 标签)
│   └── 列表 (ul/ol 标签)
│
├── 限制: 最多 10000 字符
│
└── 返回: "标题: xxx\n内容: xxx"
```

#### ResourceDownloadTool 详析

```
downloadResource(url, fileName)
│
├── 从 URL 下载文件
│
├── 保存到 tmp/download/{fileName}
│
└── 返回: "文件已下载到: tmp/download/{fileName}"
    ⚠ 可下载任意 URL 内容到服务器（SSRF 风险）
```

### 3.8 文件下载（`GET /api/teacher/manus/download/{fileType}/{fileName}`）

```
请求参数: fileType (word/pdf/file), fileName
│
┌─ Sa-Token 校验: @SaCheckRole("教师")
│
┌─ 步骤1: 构建文件路径
│  └── File file = new File(System.getProperty("user.dir") + "/tmp/" + fileType + "/" + fileName)
│
┌─ 步骤2: 检查文件存在
│  └── 不存在 → 404
│
┌─ 步骤3: 构建响应
│  ├── Content-Type: application/octet-stream
│  ├── Content-Disposition: attachment; filename*=UTF-8''{URL编码文件名}
│  └── body: new FileSystemResource(file)
│
└─ 返回 ResponseEntity<FileSystemResource>

⚠ 文件路径未做安全限制: fileType 和 fileName 可包含路径遍历字符 (../)
   例: /api/teacher/manus/download/word/../../application.yml → 可能读取配置文件
⚠ 无教师归属校验: 任意教师可下载他人 Agent 生成的文件
```

### 3.9 文件清理（`POST /api/teacher/manus/cleanup`）

```
│
┌─ Sa-Token 校验: @SaCheckRole("教师")
│
└── fileCleanupScheduledTask.manualCleanup()
    ├── 扫描 tmp/ 目录下的文件
    ├── 删除超过保留时间的临时文件
    └── 默认保留时间: 可配置（定时任务每天凌晨3点自动执行）
```

### 3.10 文件统计（`GET /api/teacher/manus/file-stats`）

```
│
┌─ Sa-Token 校验: @SaCheckRole("教师")
│
└── 返回 FileStatsDTO
    ├── baseDir: 临时文件根目录路径
    ├── retentionHours: 文件保留小时数
    └── directories: [
        { name: "file", fileCount, totalSize },
        { name: "word", fileCount, totalSize },
        { name: "download", fileCount, totalSize }
      ]
```

### 3.11 YuManus 配置

| 配置项 | 值 | 说明 |
|---|---|---|
| name | "yuManus" | Agent 名称 |
| systemPrompt | "You are YuManus, an AI assistant..." | 系统提示词（英文） |
| nextStepPrompt | "Select appropriate tools..." | 下一步提示词 |
| maxSteps | 13 | 最大执行步数 |
| maxMessageHistory | 15 | 消息历史最大长度（0=不限制，但设为15） |
| maxToolResponseLength | 2000 | 工具返回结果最大字符数 |
| chatModel | dashscopeChatModel | 阿里云 DashScope 大模型 |
| advisor | MyLoggerAdvisor | 日志 Advisor |

### 3.12 Spring AI 配置

> SpringAIConfig Bean 定义及 Advisor 清单详见 [公共参考-Spring AI 配置](../../公共参考/系统基础设施配置汇总.md#一spring-ai-与-advisor-配置)。本模块 YuManus 仅使用 MyLoggerAdvisor，不使用用户端的 MessageChatMemoryAdvisor/SafeGuardAdvisor/QuestionAnswerAdvisor。

### 3.13 定时任务

> 系统定时任务总览详见 [公共参考-定时任务配置](../../公共参考/系统基础设施配置汇总.md#三定时任务配置)。本模块相关：FileCleanupScheduledTask（每天凌晨3点清理 Agent 生成文件）。

### 3.14 数据库表结构（用户端 AI 聊天，供对比参考）

> 数据库表结构详见 [数据库表结构汇总](../../公共参考/数据库表结构汇总.md)（chat_session / chat_message）

⚠ **YuManus 教师端无数据库交互**：每次请求新建实例，无会话持久化，无历史记录存储。

### 3.15 Elasticsearch / 向量搜索

> 向量库配置详见 [公共参考-ES 向量配置](../../公共参考/系统基础设施配置汇总.md#二elasticsearch-向量存储配置)。仅用于用户端客服 AI（RAG 检索增强），教师端 YuManus 不使用向量搜索。

---

## 四、前后端交互时序

### AI 课件助手对话

```
manus-chat/index.vue → 用户输入消息
│
├── 前端: 添加用户消息 + 创建 AI 消息占位
│
├── 前端: fetch GET /api/teacher/manus/chat?message=...
│   ├── Headers: Authorization: Bearer {token}, Accept: text/event-stream
│   │
│   └── 后端: ManusTeacherController.doChatWithManus(message)
│       │
│       ├── 创建 YuManus 实例（每次新建）
│       │
│       └── yuManus.runStream(message)
│           │
│           ├── 创建 SseEmitter (5分钟超时)
│           ├── CompletableFuture.runAsync() → ReAct 循环
│           │   │
│           │   ├── Step 1: think() → 调用大模型 → 解析 ToolCall
│           │   ├── Step 2: act() → 执行工具 → 返回结果
│           │   ├── Step 3: think() → 继续/完成判断
│           │   ├── ...
│           │   ├── Step N: act() → doTerminate() → FINISHED
│           │   │
│           │   └── 每步: sseEmitter.send(result)
│           │
│           └── sseEmitter.complete()
│
├── 前端: ReadableStream 逐块读取
│   ├── 解析 SSE data: 行
│   ├── parseStep() → 识别步骤/下载/完成/错误
│   └── 实时追加到 AI 消息 steps 数组
│
├── 前端: 检测到下载链接
│   └── 渲染下载按钮 → 用户点击 →
│       GET /api/teacher/manus/download/word/{fileName}
│       → 浏览器下载 .docx 文件
│
└── 前端: 流结束 → isStreaming = false
```

### 文件下载流程

```
用户点击下载按钮 → GET /api/teacher/manus/download/word/课件.docx
│
├── 后端: 构建文件路径 {user.dir}/tmp/word/课件.docx
├── 后端: 返回 FileSystemResource (application/octet-stream)
└── 浏览器: 下载 .docx 文件
```

---

## 五、已知问题与风险点

> 跨模块系统级问题见 [公共参考-系统级已知问题与风险清单](../../公共参考/系统级已知问题与风险清单.md)，此处仅列本模块独有问题。

| # | 问题 | 严重度 | 说明 |
|---|---|---|---|
| 1 | **TerminalOperationTool 任意命令执行** | **极高** | `executeTerminalCommand(command)` 直接执行 `cmd.exe /c {command}`，无白名单/黑名单，任意教师可通过 AI 对话执行系统级命令（删文件、创建用户等） |
| 2 | **FileOperationTool 任意文件读写** | **极高** | `readFile`/`writeFile` 未做路径安全限制，AI Agent 可读写服务器任意文件（配置文件、数据库凭据等） |
| 3 | **下载端点路径遍历** | 高 | `/download/{fileType}/{fileName}` 未过滤 `../`，攻击者可构造路径读取服务器任意文件（如 `../../application.yml`）。⚠ WordGenerationTool 生成的文件有 sanitizeFileName 保护，但下载端点本身未对 fileName 做路径遍历校验 |
| 4 | **ResourceDownloadTool SSRF** | 高 | `downloadResource(url, fileName)` 可下载任意 URL 内容到服务器，存在服务端请求伪造风险（访问内网服务） |
| 5 | **无会话状态保持** | 高 | 每次请求新建 YuManus 实例，对话历史仅存于内存，无法多轮连续对话。README 明确说明 "不支持会话状态保持" |
| 6 | **无历史记录持久化** | 高 | 教师端 AI 对话无数据库存储，刷新页面即丢失所有对话记录，与用户端客服 AI 的 Redis+MySQL 双层持久化形成鲜明对比 |
| 7 | **SSE 5分钟超时限制** | 中 | `SseEmitter(300000L)` 仅5分钟超时，复杂任务（多步搜索+抓取+生成）可能超时被切断 |
| 8 | **maxSteps 限制** | 中 | 最大13步执行限制，对于需要深度搜索+多源整合+长文档生成的任务可能不够 |
| 9 | **下载端点无归属校验** | 中 | 任意教师可下载他人 Agent 生成的文件，临时文件目录为全局共享 |
| 10 | **前端 parseStep 正则脆弱** | 中 | 解析 SSE 数据依赖固定文本格式（"Step N:"、"下载链接："、"执行结束："），后端格式变化即失效 |
| 11 | **冗余代码** | 低 | `currentEventSource` 变量声明但未赋值，`onBeforeUnmount` 中的清理代码无实际作用 |
| 12 | **前端无 api.js** | 低 | API 调用直接写在组件中，不符合项目其他页面的 api.js 模块化约定 |
| 13 | **Word MIME 类型硬编码** | 低 | 下载端点 fileType 参数支持 word/pdf/file，但返回 Content-Type 始终为 `application/octet-stream` |
| 14 | **系统提示词为英文** | 低 | YuManus 的 systemPrompt 为英文，但面向中文教师用户，可能影响中文场景理解 |
| 15 | **EmbeddingController 无鉴权** | 低 | `/embedding` 路径无 Sa-Token 保护，虽然不在教师端路径下，但属于安全隐患 |
| 16 | **课程向量化定时任务已禁用** | 低 | CourseEmbeddingScheduledTask 被整体注释，向量数据依赖课程增删改时实时同步，若同步失败无兜底机制 |
| 17 | **临时文件依赖定时清理** | 低 | Agent 生成的 Word 文件仅在每天凌晨3点清理，大量并发使用时磁盘可能耗尽 |
| 18 | **前端只匹配 .docx 下载** | 低 | `parseStep` 正则仅匹配 `.docx` 后缀，若 Agent 生成其他格式文件（.pdf、.txt）无法被前端识别为下载链接 |
| 19 | **控制器方法名错误** | 低 | 文档原写 `chatWithManus`，实际控制器方法名为 `doChatWithManus`，不影响运行但影响代码定位 |
| 20 | **TerminalOperationTool 无超时保护** | 高 | 文档原描述"60秒强制终止"，实际 `process.waitFor()` 无超时参数，恶意或卡死命令将无限期阻塞 Agent 线程 |
| 21 | **continuationKeywords 重复项** | 低 | `isTaskCompleted` 中的 continuationKeywords 数组含6个重复的"还需要"，疑为复制粘贴错误，缺少原文档描述的"还需要继续"/"but"/"however"/"yet" |
| 22 | **FileCleanupTool 未注册** | 低 | FileCleanupTool.java 文件存在但未在 ToolRegistration 中注册（注释说明"Agent 不需要，文件清理由定时任务处理"），7个工具列表正确但应说明此排除 |

---

> **相关参考文档**
> - Agent功能完整说明（接口+文件生成下载+文件清理）：[Agent功能完整说明](Agent功能完整说明.md)
> - Agent优化记录（Token优化+提示词+修复记录）：[Agent优化记录](Agent优化记录.md)
