# 码趣星球 (MQXQ) — 青少儿编程在线教育平台

## 项目简介

码趣星球是一个面向青少儿编程教育的在线学习平台，包含用户端（学生）、教师端和管理员端三个角色体系。平台提供课程浏览与购买、在线学习、作业提交与批改、AI 智能助手、数据大屏等功能，后端基于 Spring Boot + Sa-Token 统一认证，前端采用 Vue 3 双端架构。

## 技术栈

| 层级 | 技术 | 版本 |
|---|---|---|
| **后端框架** | Spring Boot | 3.2.0 |
| **语言** | Java | 17 |
| **认证** | Sa-Token | 1.38 |
| **ORM** | MyBatis + PageHelper | 3.0.3 / 1.4.6 |
| **数据库** | MySQL | 8.0 |
| **缓存** | Redis (Lettuce) | 7 |
| **消息队列** | RabbitMQ | 3.7 |
| **搜索/向量** | Elasticsearch | 8.15 |
| **对象存储** | MinIO | 7.1 |
| **AI 大模型** | Spring AI + DashScope (GLM-5) | 1.0.0-M6 |
| **支付** | 支付宝沙箱 SDK | 4.35 |
| **API 文档** | Knife4j (OpenAPI 3) | 4.5 |
| **用户端前端** | Vue 3 + TypeScript + Element Plus + Tailwind CSS | Vite 7 |
| **管理端前端** | Vue 3 + Naive UI + Pinia + ECharts | Vite 6 |

## 项目结构

```
mqxq/
├── mqxq/                    # 后端 (Spring Boot)
│   ├── src/main/java/       # Java 源码
│   │   └── com/jieni/mqxq/
│   │       ├── controller/  # REST 控制器 (auth/admin/teacher/user/publicapi)
│   │       ├── service/     # 业务逻辑层
│   │       ├── domain/      # 实体、DTO、VO
│   │       ├── mapper/      # MyBatis Mapper
│   │       ├── agent/       # YuManus AI 智能体 (ReAct Agent)
│   │       ├── tools/       # Agent 工具 (搜索/抓取/终端/Word生成等)
│   │       ├── common/      # 常量、配置、工具类
│   │       └── config/      # Spring 配置类
│   ├── src/main/resources/
│   │   ├── application.yml  # 主配置
│   │   └── mapper/          # MyBatis XML
│   ├── sql/                 # 数据库建表脚本
│   └── docker-compose.yml   # 基础设施部署
│
├── mqxq-front/              # 用户端前端 (Vue 3 + TS + Element Plus)
│   ├── src/pages/           # 页面组件
│   │   ├── auth/            #   登录/注册/忘记密码/邮箱验证
│   │   ├── courses/         #   课程列表/详情/学习/章节
│   │   ├── learning/        #   我的课程/浏览记录
│   │   ├── homework/        #   作业列表/详情/提交
│   │   ├── orders/          #   订单/结算/支付
│   │   ├── profile/         #   个人中心
│   │   ├── announcements/   #   公告
│   │   └── ChatPage.vue     #   AI 在线客服
│   ├── src/router/          # 路由配置
│   └── vite.config.ts       # Vite 配置
│
├── vue-naive-admin/         # 教师/管理员端 (Vue 3 + Naive UI)
│   ├── src/views/
│   │   ├── login/           #   登录页
│   │   ├── teacherhome/     #   教师端页面
│   │   │   ├── dashboard/   #     数据大屏
│   │   │   ├── course/      #     课程管理
│   │   │   ├── course-view/ #     课程浏览记录
│   │   │   ├── course-student/ #  课程学生管理
│   │   │   ├── homework/    #     作业管理
│   │   │   ├── grade/       #     批改管理
│   │   │   ├── submission-manage/ # 提交管理
│   │   │   ├── manus-chat/  #     AI 课件助手
│   │   │   ├── statistics/  #     统计概览
│   │   │   └── order/       #     订单管理
│   │   ├── pms/             #   管理员端页面
│   │   │   ├── user/        #     用户管理
│   │   │   ├── course/      #     课程监管
│   │   │   ├── order/       #     订单管理
│   │   │   ├── dashboard/   #     数据大屏
│   │   │   └── operation/   #     运营配置
│   │   ├── home/            #   首页
│   │   └── profile/         #   个人设置
│   ├── src/settings.js      # 权限菜单配置
│   └── vite.config.js       # Vite 配置 (端口 3200)
│
└── 项目文档/                # 技术文档
    ├── mqxq-后端代码地图.md
    ├── mqxq-front-前端代码地图.md
    ├── vue-naive-admin-管理后台代码地图.md
    ├── 学生端功能说明/        # 12 篇
    ├── 教师端功能说明/        # 10 篇
    ├── 管理员端功能说明/      # 6 篇
    └── 公共参考/             # 14 篇 (认证/数据库/向量库/已知问题等)
```

## 核心功能

### 用户端（学生）
- **课程中心**：浏览/搜索课程，查看详情与章节
- **课程购买**：支付宝沙箱支付，订单管理
- **在线学习**：视频播放，学习进度追踪，浏览记录
- **作业系统**：查看作业、在线提交、查看批改结果
- **AI 助手**：Spring AI + RAG 向量检索的在线客服（课程推荐/答疑）
- **个人中心**：资料编辑、邮箱绑定、密码修改

### 教师端
- **课程管理**：创建/编辑课程、章节与小节、发布/下架
- **学生管理**：查看课程学生、评论管理、作业发布
- **作业批改**：查看提交、逐份/批量批改、统计分析
- **AI 课件助手**：YuManus ReAct 智能体，7 工具自主规划，SSE 流式输出，生成 Word 文档
- **数据大屏**：课程/学员/作业/收入核心指标 + ECharts 图表（饼图/折线图/柱状图）
- **订单管理**：查看课程相关订单

### 管理员端
- **用户管理**：CRUD、角色分配、密码重置、启禁用
- **课程监管**：全站课程审核/上下架
- **订单管理**：全站订单查看与统计
- **数据大屏**：平台级统计数据 + 图表
- **运营配置**：轮播图、公告分类、公告管理、课程分类

## 认证体系

三角色共用统一认证端点 `/api/auth/**`，基于 Sa-Token 实现：

- **登录**：BCrypt 密码校验 + 算术验证码（Kaptcha）+ 角色校验
- **鉴权**：`@SaCheckRole("管理员"/"教师")` 注解控制端点访问
- **Token**：simple-uuid 格式，30 天有效期，前端 Bearer 透传
- **RBAC**：user → user_role → role 三表多对多，中文角色名鉴权

## AI 子系统

| 特性 | 教师端 YuManus | 用户端 ChatClient |
|---|---|---|
| 架构 | ReAct Agent（多步循环） | Spring AI ChatClient（单轮/流式） |
| 工具 | 7 个（搜索/抓取/终端/Word生成等） | CourseTools（课程查询） |
| 检索 | 无向量搜索 | RAG 向量检索（Elasticsearch） |
| 输出 | SSE + Word 文件生成 | SSE 流式文本 |
| 持久化 | 无（每次新建实例） | Redis + MySQL 双层 |
| 模型 | DashScope GLM-5 | DashScope GLM-5 |

## 快速开始

### 环境要求

- Java 17+
- Node.js 18+
- MySQL 8.0
- Redis 7+
- Elasticsearch 8.x（用户端 AI 助手需要）
- RabbitMQ 3.x
- MinIO

### 1. 启动基础设施

```bash
cd mqxq
docker-compose up -d
```

包含：MySQL(3306)、Redis(6379)、RabbitMQ(5673/15672)、MinIO(9000/9001)、Elasticsearch(19200)、Kibana(15601)

### 2. 初始化数据库

```bash
# 建表脚本已挂载到 Docker MySQL 的 initdb 目录
# 或手动执行：
mysql -u root -p < mqxq/sql/mqxq结构.sql
```

### 3. 启动后端

修改 `mqxq/src/main/resources/application.yml` 中的数据库、Redis、MinIO 等连接信息，然后：

```bash
cd mqxq
./mvnw spring-boot:run
# 后端运行在 http://localhost:9999
# API 文档: http://localhost:9999/doc.html
```

### 4. 启动用户端前端

```bash
cd mqxq-front
npm install
npm run dev
# 默认 http://localhost:5173
```

### 5. 启动管理端前端

```bash
cd vue-naive-admin
pnpm install
pnpm dev
# 默认 http://localhost:3200
# API 代理到 http://159.75.11.181:9999（可修改 .env.development）
```

### 默认账号

| 角色 | 用户名 | 密码 |
|---|---|---|
| 管理员 | admin | 123456 |
| 教师 | teacher1 | 123456 |

## API 概览

| 路径前缀 | 认证 | 说明 |
|---|---|---|
| `/api/auth/**` | 部分放行 | 统一认证（登录/注册/验证码/修改密码等） |
| `/api/public/**` | 无需认证 | 公开接口（课程浏览/公告/轮播图） |
| `/api/user/**` | 需登录 | 用户端接口（学习/作业/订单/AI聊天/支付） |
| `/api/teacher/**` | `@SaCheckRole("教师")` | 教师端接口（课程管理/批改/大屏/AI教案） |
| `/api/admin/**` | `@SaCheckRole("管理员")` | 管理员接口（用户/课程/订单/运营配置） |

## 数据库表

| 表 | 说明 |
|---|---|
| `user` | 用户（学生/教师/管理员共用） |
| `role` / `user_role` | 角色与用户-角色关联 |
| `course` / `chapter` / `section` | 课程、章节、小节 |
| `course_subject` | 课程分类（树形） |
| `course_comment` | 课程评价（1-5 星） |
| `course_view` | 课程浏览记录 |
| `my_course` | 学生选课记录（已加入/学习中/已完成） |
| `course_homework` / `homework_submission` | 作业与提交 |
| `orders` | 订单（支付宝支付） |
| `announcements` / `announcement_categories` | 公告与分类 |
| `carousel` | 首页轮播图 |
| `chat_session` / `chat_message` | AI 聊天会话与消息 |
| `logs` | 系统操作日志 |

## 文档索引

| 类别 | 文档数 | 主要内容 |
|---|---|---|
| 代码地图 | 3 | 后端/用户端前端/管理端前端整体架构 |
| 学生端功能说明 | 12 | 登录注册、课程、作业、订单、AI助手、支付等 |
| 教师端功能说明 | 10 | 课程管理、批改、AI教案、数据大屏、Agent等 |
| 管理员端功能说明 | 6 | 用户管理、课程监管、订单、大屏、运营配置 |
| 公共参考 | 14 | 认证体系、数据库结构、ES向量、已知问题等 |

详见 `项目文档/` 目录。

## License

Private — All rights reserved.
