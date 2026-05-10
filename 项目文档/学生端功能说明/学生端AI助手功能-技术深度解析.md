# 学生端AI助手功能 — 技术深度解析

## 一、功能概述

学生端 AI 助手是基于 **Spring AI + DashScope** 构建的智能学习对话系统，采用 RAG 检索增强 + 工具调用架构，支持流式对话、课程推荐、课程下单等交互。

### 功能清单

| 功能 | 前端组件 | 后端端点 | 认证要求 |
|---|---|---|---|
| SSE 流式聊天 | ChatPage / AIChatAssistant | `POST /api/user/chat` | `@SaCheckLogin + @SaCheckRole("用户")` |
| 停止生成 | 两个组件共用 | `POST /api/user/chat/stop` | 同上 |
| 创建会话 | 两个组件共用 | `POST /api/user/session` | 同上 |
| 获取热门问题 | AIChatAssistant | `GET /api/user/session/hot` | 同上 |
| 会话列表 | ChatSessionList / AIChatAssistant | `GET /api/user/session/list` | 同上 |
| 会话消息 | 两个组件共用 | `GET /api/user/session/{sessionId}` | 同上 |
| 删除会话 | 两个组件共用 | `DELETE /api/user/session/{sessionId}` | 同上 |

### 架构全景

```
学生端 ChatClient（单步 LLM + Advisor 链）
├── Spring AI ChatClient
│   ├── SafeGuardAdvisor        → 敏感词过滤（血腥/暴力/色情/政治/宗教）
│   ├── MessageChatMemoryAdvisor → Redis 多轮对话记忆
│   ├── QuestionAnswerAdvisor   → RAG: Elasticsearch 向量检索(topK=10, threshold=0.7)
│   └── CourseTools             → queryCourseById / prePlaceOrder
│
├── DashScope ChatModel (glm-5)
│   └── text-embedding-v3 (1024维) → Elasticsearch 向量库
│
├── SSE 流式输出 → ChatEventVO (1001 DATA / 1003 PARAM / 1002 STOP)
│
└── 三级持久化: Redis 实时 + MySQL 异步 + 定时批量同步

教师端 YuManus（多步 Agent，独立系统）
├── BaseAgent → ReActAgent → ToolCallAgent → YuManus
├── 7 个工具: FileOperation/WebSearch/WebScraping/ResourceDownload/Terminal/WordGeneration/Terminate
├── SseEmitter 输出（非 ChatEventVO）
└── 无持久化、无 RAG、无敏感词过滤
```

---

## 二、前端关键逻辑

### 2.1 两个入口组件对比

| 特性 | ChatPage.vue（完整页） | AIChatAssistant.vue（悬浮窗） |
|---|---|---|
| 布局 | 全页 flex：侧栏(300px) + 主区域 | 固定悬浮窗(680×700px，可缩放) |
| 会话列表 | 独立 ChatSessionList 组件 | 内联历史面板（滑出，max 200px） |
| 自动初始化 | 否（等待用户操作或 URL 参数） | 是（首次打开自动 initSession） |
| 热门问题刷新 | 无独立刷新按钮 | 有"换一换"按钮 → chatApi.getHotExamples() |
| 展开全页 | N/A | "展开"按钮 → /chat?sessionId=... |
| 缩放支持 | 无 | 8 手柄拖拽缩放（680-900 × 500-900） |
| Auth 检查 | 路由守卫保证 | FAB 点击时检查 isAuthenticated |
| 错误显示 | console.log | 追加为 assistant 消息 |
| Params 展示 | 非课程/订单参数显示 JSON 信息框 | 仅卡片渲染，无原始 JSON |

### 2.2 SSE 流式通信机制

**前端实现**：使用原生 `fetch` + `ReadableStream`（因 EventSource 仅支持 GET 请求），支持 AbortController 取消、四头部 Token 注入、buffer 防断行处理。详细实现代码见 [聊天功能-前端实现说明](聊天功能-前端实现说明.md)。

**后端实现**：Spring WebMvc SseEmitter + ChatEventVO 封装，三种事件类型（DATA/STOP/PARAM）。详细流程见 [聊天功能-后端实现说明](聊天功能-后端实现说明.md)。

**共享模型定义**（ChatEventVO/SessionVO/MessageVO 等）详见 [公共参考-聊天/AI对话共享模型](../../公共参考/公共数据模型与基础配置.md#四聊天ai-对话共享模型)。

### 2.3 消息累积与渲染

两个组件使用相同的累积模式：

```typescript
let fullMessage = ''
let assistantMessageIndex = -1

// onMessage 回调：
(data: string) => {
  fullMessage += data  // 逐块追加

  if (assistantMessageIndex === -1) {
    // 首块：创建 assistant 消息并 push
    messages.value.push({
      id: Date.now().toString(),
      sessionId: currentSessionId.value,
      role: 'assistant',
      content: fullMessage,
      createdAt: new Date().toISOString()
    })
    assistantMessageIndex = messages.value.length - 1
  } else {
    // 后续块：原地替换完整内容
    messages.value[assistantMessageIndex].content = fullMessage
  }
  scrollToBottom()
}

// onParam 回调：
(params) => {
  if (assistantMessageIndex >= 0) {
    messages.value[assistantMessageIndex].params = params  // ⚠ 替换而非合并
  }
}
```

### 2.4 消息内容渲染（MessageContent.vue）

**文本解析**：将原始内容按代码块正则分割为 `ContentBlock[]`

```typescript
const codeBlockRegex = /```(\w+)?\n?([\s\S]*?)```/g
// 匹配: ```language\n...code...\n```
// 非代码部分 → type:'text'，代码部分 → type:'code'
```

**文本块格式化**（`v-html` 渲染）：
```
\n → <br>
`code` → <code class="inline-code">code</code>
**bold** → <strong>bold</strong>
```

**代码块**：深色主题头部（语言标签 + 复制按钮），`navigator.clipboard.writeText()` 复制。

**课程卡片**（`courseInfo_*` 前缀参数）：

```
┌──────────────────────────────┐
│ [封面图]  课程名称            │
│           详情(截断60字)      │
│           X人购买  X浏览      │
│           ¥价格  X课时+X分钟  │
│                        [查看] │
└──────────────────────────────┘
```

**订单卡片**（`orderInfo_*` 前缀参数，优先级高于课程卡片）：

| 订单状态 | 卡片样式 | 操作按钮 |
|---|---|---|
| NOT_PAY | 状态标签(橙色) + 课程名+价格+时间 | "立即支付" → 支付宝 |
| PAID/DONE | 状态标签(绿色) + 课程名+价格+时间 | "查看详情" → 订单详情 |
| CANCELLED/REFUNDED | 状态标签(灰色) + 课程名+价格+时间 | "查看详情" |
| ERROR | 红色主题 + errorMessage + 提示 | "查看我的订单" → 订单列表 |

**渲染优先级**：`orderCards.length > 0` 时隐藏 courseCards，`hasCourseInfo(params)` 为 true 时隐藏原始 JSON 展示。

### 2.5 会话生命周期

```
创建会话 → chatApi.createSession(3)
├── POST /api/user/session { params: { n: 3 } }
├── 返回 SessionVO: { sessionId, title, describe, examples[] }
└── examples 即热门问题，用于欢迎页展示

发送消息 → chatApi.chat({ question, sessionId }, onMessage, onParam, onError, onComplete)
├── POST /api/user/chat (SSE 流式)
├── 逐块接收 AI 回复 + 工具调用参数
└── 完成后刷新侧栏会话列表

停止生成 → stopGeneration()
├── cancelChat()               // 客户端中断 fetch
└── chatApi.stop(sessionId)    // 通知后端停止

切换会话 → loadHistorySession(session)
├── GET /api/user/session/{sessionId}
├── 映射 MessageVO.type ('USER'/'ASSISTANT') → role ('user'/'assistant')
└── 替换 messages 数组

删除会话 → chatApi.deleteSession(sessionId)
├── DELETE /api/user/session/{sessionId}
├── 从列表中移除
└── 若为当前会话 → 自动创建新会话

悬浮窗→全页 → openFullPage()
├── router.push('/chat?sessionId=' + currentSessionId)
└── 全页组件从后端重新加载消息（不共享内存状态）
```

### 2.6 热门问题机制

**来源**：后端 `application.yml` 静态配置 20 个示例，分 4 类：
- 课程推荐(5)：年龄适配课程、课程对比、学习路径
- 课程报名(4)：报名流程、年龄适合、价格咨询
- 课程介绍(4)：课程详情、课时、学习成果
- 知识讲解(7)：循环、变量、Python turtle、坐标、Scratch

**分配方式**：`RandomUtil.randomEleList()` 从 20 个中随机抽取 N 个。

**悬浮窗**有独立刷新按钮调用 `chatApi.getHotExamples(3)`；完整页仅在创建会话时获取。

### 2.7 状态管理

**全部为组件本地 ref，无 Pinia Store**。两个组件各自独立维护相同结构的状态：

```typescript
const currentSessionId = ref('')
const sessionTitle = ref('你好！我是 AI 学习助手')
const sessionDescribe = ref('我可以帮你解答学习中的问题')
const messages = ref<ChatMessage[]>([])
const hotExamples = ref<SessionExample[]>([])
const inputMessage = ref('')
const isLoading = ref(false)
let cancelChat: (() => void) | null = null  // 非响应式，仅命令式使用
```

**跨组件不共享状态**：悬浮窗→全页仅传递 `sessionId`，全页组件从后端重新加载全部消息。

---

## 三、后端执行流程

### 3.1 Sa-Token 认证

`ChatController` 和 `SessionController` 均标注：
- `@SaCheckLogin` — 要求有效 token
- `@SaCheckRole("用户")` — 要求"用户"角色

`SaUtil.getLoginId()` 从 `StpUtil.getLoginId()` 提取用户 ID，构建会话标识 `conversationId = "{userId}_{sessionId}"`。

### 3.2 SSE 流式聊天（`POST /api/user/chat`）

```
请求体: { params: { question, sessionId } }
│
┌─ 步骤1: 认证 + 构建 conversationId
│  ├── StpUtil.getLoginId() → userId
│  └── conversationId = "{userId}_{sessionId}"
│
┌─ 步骤2: 生成 requestId
│  └── requestId = IdUtil.fastSimpleUUID()
│
┌─ 步骤3: 设置生成状态
│  └── GENERATE_STATUS.put(sessionId, true)  // 用于支持 stop()
│
┌─ 步骤4: 构建 ChatClient 请求
│  ├── .system(SYSTEM_PROMPT)
│  ├── .advisors(QuestionAnswerAdvisor)        // RAG 检索
│  ├── .advisors(memory advisor param)         // Redis 记忆注入
│  │   key: CHAT_MEMORY_CONVERSATION_ID_KEY = conversationId
│  ├── .toolContext(requestId, conversationId) // 传递给 CourseTools
│  └── .user(question)
│
┌─ 步骤5: 流式执行 .stream().chatResponse()
│  │
│  ├── .onErrorContinue(...)     → 跳过 DashScope NPE 异常块
│  ├── .doOnSubscribe(...)       → GENERATE_STATUS = true
│  ├── .doOnComplete(...)        → 保存会话 + 异步同步消息到 MySQL
│  ├── .doOnError(...)           → 仅日志，不中断流
│  ├── .doOnCancel(...)          → 保存部分输出到 Redis
│  ├── .takeWhile(status==true)  → 响应 stop() 调用
│  │
│  ├── .map(...) → ChatEventVO(1001, tokenText)  // 每个token → DATA事件
│  │   └── 当 finishReason="STOP" 时:
│  │       映射 messageId → requestId 到 ToolResultHolder
│  │       （用于后续从 ToolResultHolder 取 params）
│  │
│  ├── .filter(...) → 跳过空 DATA 事件
│  └── .onErrorResume(...) → 最终安全网（替换为 STOP 或错误消息）
│
┌─ 步骤6: 追加 PARAM + STOP 事件
│  └── .concatWith(Flux.defer(() -> {
│        Map<String, Object> params = ToolResultHolder.get(requestId)
│        if (params != null && !params.isEmpty())
│          → 发射 ChatEventVO(1003, params)   // PARAM 事件
│        ToolResultHolder.remove(requestId)     // 清理
│        → 发射 ChatEventVO(1002, null)        // STOP 事件
│      }))
│
└─ 返回 Flux<ChatEventVO> → Spring WebFlux 自动序列化为 SSE
```

### 3.3 ChatEventVO 结构

> 聊天/AI共享模型（ChatEventVO、SessionVO、MessageVO 等定义）详见 [公共参考-聊天/AI对话共享模型](../../公共参考/公共数据模型与基础配置.md#四聊天ai-对话共享模型)

### 3.4 RAG 检索增强

```java
new QuestionAnswerAdvisor(vectorStore,
    SearchRequest.builder()
        .query("")                      // 使用用户问题原文检索
        .topK(10)                       // 返回最相似的 10 个文档
        .similarityThreshold(0.7)       // 相似度阈值 0.7
        .build())
```

**检索流程**：
```
用户问题 → text-embedding-v3(1024维) 向量化
→ Elasticsearch 相似度搜索(topK=10, threshold≥0.7)
→ 匹配的课程文档注入 prompt 上下文
→ LLM 基于上下文生成回答
```

**课程向量化**：`CourseEmbeddingScheduledTask` 定时任务将课程数据嵌入 ES。

### 3.5 Redis 会话记忆（RedisChatMemory）

**Key 格式**：`CHAT:{conversationId}`，其中 `conversationId = "{userId}_{sessionId}"`

**数据结构**：Redis List，每个元素为 `RedisMessage` 的 JSON 序列化

**核心操作**：

| 操作 | Redis 命令 | 说明 |
|---|---|---|
| `add(conversationId, messages)` | RPUSH | 追加消息到列表尾部 |
| `get(conversationId, lastN)` | LRANGE 0 lastN | 读取最近 N 条消息 |
| `clear(conversationId)` | DEL | 清空会话记忆 |

**RedisMessage 结构**：
```json
{
  "messageType": "USER | ASSISTANT | SYSTEM | TOOL",
  "textContent": "消息文本",
  "toolCalls": [],
  "toolResponses": [],
  "params": {}         // MyAssistantMessage 独有：courseInfo/orderInfo
}
```

**序列化/反序列化**（MessageUtil）：
- `toJson()` — 处理 `MyAssistantMessage` 的 params：优先从消息对象取，否则通过 messageId→requestId 从 ToolResultHolder 取
- `toMessage()` — 反序列化时重建正确子类型：SYSTEM→SystemMessage, USER→UserMessage, ASSISTANT→MyAssistantMessage, TOOL→ToolResponseMessage

### 3.6 CourseTools — AI 工具函数

#### queryCourseById

```
LLM 决定调用 queryCourseById(courseId=5)
│
├── courseDao.queryById(5) → Course 实体
├── 转换为 CourseInfo VO:
│   title→name, description→detail, BigDecimal→Double, Date→LocalDateTime
├── 存入 ToolResultHolder:
│   put(requestId, "courseInfo_5", courseInfo)
└── 返回 CourseInfo 给 LLM（LLM 据此生成文本回答）

流结束后 → PARAM 事件:
{ "courseInfo_5": { id:5, name:"Python入门", detail:"...", buyCount:120, viewCount:500, price:99.0, lessonNum:12, durationSum:720 } }
```

#### prePlaceOrder

```
LLM 决定调用 prePlaceOrder(courseId=5)
│
├── 从 conversationId 解析 userId: "12_abc123" → userId=12
├── CoursePurchaseService.purchaseCourse(12, 5)
│   │
│   ├── 成功 → OrderVO
│   │   ├── 转换为 OrderInfo
│   │   └── ToolResultHolder.put(requestId, "orderInfo_{orderId}", orderInfo)
│   │
│   ├── MyException（业务错误，如已购买）
│   │   ├── OrderInfo { status:"ERROR", errorMessage:"您已购买该课程" }
│   │   └── ToolResultHolder.put(requestId, "orderInfo_error_5", errorInfo)
│   │
│   └── 系统异常 → 同上错误模式
│
└── 返回 OrderInfo 给 LLM（LLM 据此引导用户）

流结束后 → PARAM 事件:
成功: { "orderInfo_89": { id:89, orderNo:"ORD...", courseName:"Python入门", price:99.0, status:"NOT_PAY", courseId:5 } }
失败: { "orderInfo_error_5": { status:"ERROR", errorMessage:"您已购买该课程", courseId:5 } }
```

**ToolResultHolder**：`ConcurrentHashMap<String, Map<String, Object>>`，作为工具执行（Advisor 链内部）与 SSE 响应组装（ChatServiceImpl）之间的桥梁。

### 3.7 SafeGuardAdvisor — 敏感词过滤

使用 Spring AI 内置 `SafeGuardAdvisor`：

```java
SafeGuardAdvisor.builder()
    .sensitiveWords("血腥", "暴力", "伤害", "色情", "政治", "宗教")
    .violationMessage("敏感词提示：请勿输入敏感词！")
    .build()
```

用户消息包含任一敏感词时，Advisor 拦截请求并直接返回提示消息，不调用 LLM。

### 3.8 停止生成（`POST /api/user/chat/stop`）

```java
public void stop(String sessionId) {
    GENERATE_STATUS.remove(sessionId);  // 移除 key
}
```

流式管道中的 `.takeWhile(s -> GENERATE_STATUS.get(sessionId) == true)` 检测到 key 被移除后终止 Flux。`.doOnCancel()` 回调将已累积的部分输出保存到 Redis。

### 3.9 会话管理

#### 创建会话（`POST /api/user/session`）

```
请求体: { params: { n: 3 } }  // n = 热门问题数量
│
├── 从 SessionProperties 读取配置:
│   ├── title: "AI 学习助手"
│   ├── describe: "我可以帮你..."
│   └── examples: 20 个预置示例
│
├── RandomUtil.randomEleList(examples, n) → 随机选 n 个
│
├── sessionId = IdUtil.fastSimpleUUID()
│
├── ⚠ 此时**不持久化到 MySQL**
│   会话仅在首次聊天消息发送时才写入 DB（懒持久化，避免页面刷新产生空会话）
│
└── 返回 SessionVO: { sessionId, title, describe, examples[] }
```

#### 会话列表（`GET /api/user/session/list`）

```sql
SELECT ... FROM chat_session WHERE user_id = #{userId} ORDER BY update_time DESC
```

#### 会话消息（`GET /api/user/session/{sessionId}`）

```
├── 优先从 Redis 读取: chatMemory.get(conversationId, 1000)
│
├── Redis 为空时从 MySQL 恢复:
│   ├── chatMessageDao.selectBySessionId(sessionId)
│   ├── 转换为 Spring AI Message 格式
│   ├── 清空 Redis → 重写 Redis（restore 流程）
│   └── 仅返回 USER + ASSISTANT 类型
│
└── MyAssistantMessage 的 params 一并返回
```

#### 删除会话（`DELETE /api/user/session/{sessionId}`）

```
├── 验证会话归属当前用户
├── RedisChatMemory.clear(conversationId)        // 清 Redis
├── chatMessageDao.deleteBySessionId(sessionId)  // 清消息
├── chatSessionDao.deleteBySessionId(sessionId)  // 清会话
└── Redis/MySQL 删除失败仅记日志，不阻塞会话记录删除
```

### 3.10 消息持久化机制

#### 三级持久化策略

| 级别 | 时机 | 存储 | 说明 |
|---|---|---|---|
| **实时** | 每个 token 流式写入 | Redis List | ChatMemoryAdvisor 自动管理 |
| **异步** | 流完成(doOnComplete) | MySQL | @Async 线程池，含去重检查 |
| **批量** | 每天凌晨 1 点 | MySQL | 定时任务扫描 Redis → 批量同步 |

#### 异步保存（doOnComplete → syncMessagesToDatabase）

```
├── 保存用户消息 → chatMessageService.saveMessageToDatabase() (@Async)
│   ├── 获取 max message_order
│   ├── 去重检查（content + type）
│   └── batchSave
│
├── 读取 AI 最新回复 → chatMemory.get(conversationId, 10)
│   ├── MyAssistantMessage → 直接提取 params
│   └── 否则 → ToolResultHolder(messageId→requestId) 取 params
│
└── 保存 AI 回复（含 params JSON 序列化）
```

#### 定时批量同步（每天 01:00）

```
├── 扫描 Redis 所有 CHAT:* 键
├── 解析 conversationId → userId + sessionId
├── 检查 MySQL 是否有对应会话（无则跳过）
├── 读取 MySQL 已有消息（用于去重）
├── 读取 Redis 最多 10000 条消息
├── 过滤 USER + ASSISTANT 类型
├── 跳过内容已存在的消息
└── batchSave 新消息
```

### 3.11 数据库表结构

> 数据库表结构详见 [公共参考-数据库表结构汇总](../../公共参考/数据库表结构汇总.md)

### 3.12 关键 MyBatis 查询

```sql
-- 保存会话
INSERT INTO chat_session (session_id, user_id, title, ...) VALUES (...)

-- 按用户查询会话列表
SELECT ... FROM chat_session WHERE user_id = #{userId} ORDER BY update_time DESC

-- 批量保存消息
INSERT INTO chat_message (session_id, user_id, message_type, content, params, message_order)
VALUES <foreach>(#{item.sessionId}, #{item.userId}, ...)</foreach>

-- 按会话查询消息
SELECT ... FROM chat_message WHERE session_id = #{sessionId}
ORDER BY message_order ASC, create_time ASC

-- 获取最大排序号
SELECT COALESCE(MAX(message_order), 0) FROM chat_message WHERE session_id = #{sessionId}

-- 删除会话消息
DELETE FROM chat_message WHERE session_id = #{sessionId}
```

---

## 四、学生端 ChatClient vs 教师端 YuManus 对比

> 两套 AI 子系统的完整对比详见 [教师端AI教案功能-两套AI系统对比](../教师端功能说明/教师端AI教案功能-技术深度解析.md#两套-ai-系统对比)。学生端 ChatClient 特点：Spring AI 流式 + Redis/MySQL 双层持久化 + RAG 向量检索 + 敏感词过滤；教师端 YuManus 特点：ReAct 多步循环 + 7工具 + Word 文件生成 + 无持久化。

---

## 五、已知问题与风险点

> 跨模块系统级问题详见 [公共参考-系统级已知问题与风险清单](../../公共参考/系统级已知问题与风险清单.md)
>
> - 热门课程全量加载 → [三13-热门课程全量加载](../../公共参考/系统级已知问题与风险清单.md#三13-热门课程全量加载)
> - 无 Redis 缓存 → [二5-无Redis缓存](../../公共参考/系统级已知问题与风险清单.md#二5-无-redis-缓存)
> - 课程搜索仅匹配标题 → [三11-课程搜索仅匹配标题](../../公共参考/系统级已知问题与风险清单.md#三11-课程搜索仅匹配标题)

| # | 问题 | 严重度 | 说明 |
|---|---|---|---|
| 1 | **前端逻辑重复** | 中 | ChatPage.vue 和 AIChatAssistant.vue 重复实现 sendMessage/stopGeneration/会话管理等，应提取为 useChat() composable |
| 2 | **PARAM 事件替换而非合并** | 中 | `onParam` 回调 `messages[index].params = params` 替换整体，多次工具调用仅保留最后结果 |
| 3 | **消息 ID 碰撞风险** | 低 | 使用 `Date.now().toString()` 作 ID，同毫秒内两条消息会冲突 |
| 4 | **会话懒持久化可能丢数据** | 中 | 创建会话时不写 MySQL，仅首次聊天时写入；Redis 宕机则空会话丢失 |
| 5 | **DashScope NPE 容错** | 低 | 三层错误处理（onErrorContinue + doOnError + onErrorResume）应对 DashScope SDK 偶发 NPE |
| 6 | **跨组件不共享状态** | 低 | 悬浮窗→全页仅传 sessionId，全页重新从后端加载消息，增加请求开销 |
| 7 | **SSE Token 手动注入** | 低 | 不走 Axios 拦截器，手动从 localStorage 取 token 并设置 4 个头部，与 Axios 路径不一致 |

---

> **相关参考文档**
> - 前端实现详情：[聊天功能-前端实现说明](聊天功能-前端实现说明.md)
> - 后端实现详情：[聊天功能-后端实现说明](聊天功能-后端实现说明.md)
