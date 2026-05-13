# 码趣星球（MQXQ）后端 — 代码地图

## 一、项目概述

- **项目名称**：码趣星球（mqxq）
- **项目定位**：面向少儿编程教育的在线学习平台后端系统
- **开发语言**：Java 17
- **核心框架**：Spring Boot 3.2.0
- **构建工具**：Maven
- **基础包名**：`com.jieni.mqxq`
- **服务端口**：9999

核心功能：课程管理、用户认证与授权、在线支付（支付宝沙箱）、AI 智能助教对话（RAG + Agent）、作业提交与批改、文件存储（MinIO）、数据大屏等。

---

## 二、目录结构

```
mqxq/
├── pom.xml                        # Maven 核心配置
├── docker-compose.yml             # Docker Compose 编排
├── sql/                           # 数据库脚本
│   ├── mqxq结构.sql                # 完整建表脚本
├── src/main/java/com/jieni/mqxq/ # Java 源码
├── src/main/resources/
│   ├── application.yml            # 主配置文件
│   └── mapper/                    # MyBatis XML 映射（19个）
└── src/test/                      # 测试代码
```

---

## 三、源码包结构

基础路径：`src/main/java/com/jieni/mqxq/`

| 包 | 职责 |
|---|---|
| `controller/auth/` | 统一认证接口（登录/注册/验证码/密码管理） |
| `controller/admin/` | 管理员端接口（课程/用户/订单/公告/轮播图/数据大屏等） |
| `controller/teacher/` | 教师端接口（课程/章节/小节/作业/评论/Manus 智能体/数据大屏） |
| `controller/user/` | 学生端接口（课程浏览/购买/评论/作业提交/聊天/支付） |
| `controller/publicapi/` | 公共免登录接口（课程列表/公告/轮播图/文件上传） |
| `controller/embedding/` | 向量化接口（课程向量存储/检索） |
| `domain/entity/` | 数据库实体类（19个） |
| `domain/dto/` | 请求 DTO（auth/chapter/chat/comment/content/course/courseview/homework/order/role/section/user） |
| `domain/vo/` | 响应 VO（auth/chapter/chat/comment/content/course/courseview/dashboard/homework/order/role/section/user） |
| `dao/` | MyBatis DAO 接口（19个） |
| `service/` | 业务接口层（按领域分包：auth/chat/content/course/dashboard/homework/infrastructure/order） |
| `service/impl/` | 业务实现层 |
| `common/config/` | 配置类（Redis/CORS/MinIO/支付宝/验证码/线程池/RabbitMQ/SpringAI） |
| `common/constants/` | 常量类（Course/Login/Order） |
| `common/enums/` | 枚举类（ContentType/LogModule/ResultCode/UserType/ChatEventType/MessageType） |
| `common/login/` | 登录策略模式（LoginStrategy + Admin/Student/Teacher 三种实现） |
| `common/security/` | 安全管理（LoginSecurityManager：IP 锁定/验证码验证） |
| `common/scheduled/` | 定时任务（课程向量同步/文件清理/聊天消息同步） |
| `common/system/` | 系统组件（AsyncTaskFactory） |
| `agent/` | AI 智能体框架（BaseAgent/ReActAgent/ToolCallAgent/YuManus） |
| `tools/` | Agent 工具集（CourseTools/WebSearch/WebScraping/FileOperation/WordGeneration/Terminate） |
| `advisor/` | Spring AI Advisor（MyLoggerAdvisor/ReReadingAdvisor） |
| `memory/` | 对话记忆（RedisChatMemory/RedisMessage/MessageUtil） |
| `mq/` | 消息队列（EmailExchange） |
| `redisService/` | Redis 业务服务（LoginRService） |
| `util/` | 工具类（IpUtils/PicUtil/SaUtil/EmailUtils/RedisUtil/RabbitMqHelper/SpringUtils/FUtil/ExcelExportUtil） |
| `exception/` | 自定义异常（MyException） |

---

## 四、数据库模型（19 张表）

### 核心实体

| 实体 | 表名 | 说明 |
|---|---|---|
| User | `user` | 用户表（管理员/教师/学生共用） |
| Role | `role` | 角色表 |
| UserRole | `user_role` | 用户-角色关联 |
| Course | `course` | 课程表 |
| CourseSubject | `course_subject` | 课程分类（树形结构） |
| Chapter | `chapter` | 课程章节 |
| Section | `section` | 课程小节（含视频） |
| CourseView | `course_view` | 课程浏览记录 |
| CourseComment | `course_comment` | 课程评论 |
| CourseHomework | `course_homework` | 课程作业 |
| HomeworkSubmission | `homework_submission` | 作业提交 |
| MyCourse | `my_course` | 我的课程（已购/学习中/已完成） |
| Orders | `orders` | 订单表 |
| Carousel | `carousel` | 首页轮播图 |
| AnnouncementCategories | `announcement_categories` | 公告分类 |
| Announcements | `announcements` | 公告详情 |
| Logs | `logs` | 系统日志 |
| ChatSession | `chat_session` | AI 对话会话 |
| ChatMessage | `chat_message` | AI 对话消息 |

### 关键关系

```
User --(user_role)--> Role
Course --(teacher_id)--> User(教师)
Course --(subject_id)--> CourseSubject
Course --(1:N)--> Chapter --(1:N)--> Section
Course --(1:N)--> CourseHomework --(1:N)--> HomeworkSubmission
User --(my_course)--> Course --(order_id)--> Orders
User --(chat_session)--> ChatSession --(chat_message)--> ChatMessage
```

---

## 五、API 端点全览

### 5.1 认证接口 `/api/auth/*`

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/auth/login` | 登录 |
| POST | `/api/auth/register` | 注册 |
| GET | `/api/auth/userinfo` | 获取当前用户信息 |
| POST | `/api/auth/logout` | 登出 |
| POST | `/api/auth/refresh-token` | 刷新令牌 |
| POST | `/api/auth/change-password` | 修改密码 |
| POST | `/api/auth/send-email-code` | 发送邮箱验证码 |
| POST | `/api/auth/forgot-password` | 忘记密码重置 |
| POST | `/api/auth/update-profile` | 更新用户资料 |
| POST | `/api/auth/bind-email` | 绑定/更换邮箱 |
| GET | `/api/auth/check-username` | 检查用户名可用性 |
| GET | `/api/auth/captcha` | 获取图形验证码 |

### 5.2 管理员接口 `/api/admin/*`（需 `@SaCheckRole("管理员")`）

课程/章节/小节/订单/角色/轮播图/分类/评论/浏览记录/我的课程/公告/作业/用户管理/数据大屏

### 5.3 教师接口 `/api/teacher/*`（需 `@SaCheckRole("教师")`）

课程/章节/小节/浏览统计/作业/作业批改/评论/订单/学员/**Manus 智能体（SSE 流式）**/数据大屏

### 5.4 学生接口 `/api/user/*`（需 `@SaCheckRole("用户")`）

课程浏览/章节/小节/评论/浏览记录/订单/我的课程/作业/提交记录/**AI 聊天（SSE）**/对话会话/支付宝支付

### 5.5 公共接口 `/api/public/*`（免登录）

课程列表/详情/热门/免费/搜索/轮播图/分类/章节小节/评论/公告/文件上传

### 5.6 向量化接口 `/embedding/*`

批量向量化/文本向量化/删除/相似度搜索/课程向量化

---

## 六、AI 智能体架构（项目核心特色）

### Agent 继承体系

```
BaseAgent (抽象类) — 状态管理 / 执行控制 / run() + runStream()
└── ReActAgent (抽象类) — think() + act() 推理模式
    └── ToolCallAgent (核心实现) — 工具调用管理 / JSON 参数修复 / 智能终止
        └── YuManus (Spring Bean) — 实际使用的智能体
            ├── DashScope ChatModel (glm-5)
            ├── 注册 7 个工具
            └── maxSteps=13, maxMessageHistory=15
```

### Agent 工具集

| 工具 | 功能 |
|---|---|
| FileOperationTool | 文件读写 |
| WebSearchTool | 网页搜索（百度引擎） |
| WebScrapingTool | 网页内容抓取 |
| ResourceDownloadTool | 资源下载 |
| TerminalOperationTool | 终端命令执行 |
| WordGenerationTool | Word 文档生成 |
| TerminateTool | 任务终止信号 |
| CourseTools | 课程查询/预下单（ChatClient 默认工具） |

### RAG 对话系统

```
用户请求 → ChatController (SSE)
  → ChatServiceImpl → ChatClient (Spring AI)
    → MessageChatMemoryAdvisor (Redis 会话记忆)
    → SafeGuardAdvisor (敏感词过滤)
    → CourseTools (课程查询/预下单)
    → DashScope ChatModel (glm-5)
    → RAG: Elasticsearch 向量库检索
      → EmbeddingModel (text-embedding-v3, 1024维)
```

---

## 七、关键依赖

| 依赖 | 版本 | 用途 |
|---|---|---|
| Spring Boot | 3.2.0 | 核心框架 |
| MyBatis | 3.0.3 | ORM |
| Sa-Token | 1.38.0 | 权限认证 |
| Spring AI Alibaba | 1.0.0-M6.1 | AI 大模型 |
| Spring AI ES Store | 1.0.0-M6 | 向量存储 |
| Elasticsearch | 8.15.5 | ES 客户端 |
| Alipay SDK | 4.35.79.ALL | 支付宝沙箱 |
| MinIO | 7.1.0 | 对象存储 |
| RabbitMQ | Boot 默认 | 消息队列 |
| Hutool | 5.8.25 | 工具类库 |
| PageHelper | 1.4.6 | 分页 |
| Knife4j | 4.5.0 | API 文档 |
| Apache POI | 5.2.3 | Excel/Word |
| iText | 9.1.0 | PDF 生成 |

---

## 八、基础设施（Docker Compose）

| 服务 | 端口 | 说明 |
|---|---|---|
| MySQL 8.0 | 3306 | 主数据库 |
| Redis 7 | 6379 | 缓存/会话/验证码 |
| RabbitMQ 3.7.4 | 5673/15672 | 消息队列 |
| MinIO | 9000/9001 | 对象存储 |
| Elasticsearch 8.13.4 | 19200/19300 | 向量存储+搜索 |
| Kibana 8.13.4 | 15601 | ES 可视化 |

---

## 九、架构特点

1. **三层架构**：Controller → Service → DAO（严格分层）
2. **策略模式**：LoginStrategy + Admin/Student/Teacher 三种登录实现
3. **RAG 架构**：Spring AI + ES 向量库 + DashScope Embedding
4. **Agent 模式**：ReAct 推理循环 + ToolCall 工具调用 → YuManus 自主智能体
5. **SSE 流式**：AI 对话和 Manus 智能体均使用 SSE
6. **RBAC 权限**：Sa-Token + 三角色细粒度 API 权限
7. **Redis 会话记忆**：多轮对话上下文保持
8. **统一响应**：`Result<T>` 泛型类
9. **DTO/VO 分层**：请求 DTO / 响应 VO 与实体隔离
10. **消息队列**：RabbitMQ 异步任务（邮件等）

---

## 十、关键文件索引

| 类别 | 路径 |
|---|---|
| 启动入口 | `src/main/java/com/jieni/mqxq/MqxqApplication.java` |
| 主配置 | `src/main/resources/application.yml` |
| Maven | `pom.xml` |
| Docker | `docker-compose.yml` |
| 数据库 | `sql/mqxq结构.sql` |
| AI 配置 | `common/config/SpringAIConfig.java` |
| Agent 入口 | `agent/YuManus.java` |
| Agent 基类 | `agent/BaseAgent.java` |
| 工具调用 | `agent/ToolCallAgent.java` |
| 课程工具 | `tools/CourseTools.java` |
| 对话记忆 | `memory/RedisChatMemory.java` |
| 认证控制 | `controller/auth/CompleteUnifiedAuthController.java` |
| AI 聊天 | `controller/user/ChatController.java` |
| Manus | `controller/teacher/ManusTeacherController.java` |
| 支付 | `controller/user/AliPayController.java` |
| 登录安全 | `common/security/LoginSecurityManager.java` |
| 统一响应 | `common/Result.java` |
