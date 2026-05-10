# 码趣星球前端（MQXQ-Front）— 代码地图

## 一、项目概述

- **项目名称**：mqxq-front（码趣星球在线学习平台前端）
- **项目定位**：面向学员用户的在线教育前端应用
- **技术栈**：Vue 3 + TypeScript + Vite 7 + Element Plus + Tailwind CSS
- **状态管理**：Pinia 3 + pinia-plugin-persistedstate
- **HTTP 客户端**：Axios
- **包管理器**：pnpm

核心功能：课程浏览/学习、作业提交、订单支付、AI 学习助手（SSE 流式）、个人中心等。

---

## 二、目录结构

```
mqxq-front/
├── index.html                  # HTML 入口
├── package.json                # 依赖与脚本
├── pnpm-lock.yaml              # pnpm 锁文件
├── vite.config.ts              # Vite 构建配置
├── tsconfig.json               # TS 根配置
├── tailwind.config.js          # Tailwind CSS 配置
├── postcss.config.js           # PostCSS 配置
├── public/                     # 静态资源（不经过构建）
├── dist/                       # 构建产物
└── src/                        # 源代码
    ├── main.ts                 # 应用入口
    ├── App.vue                 # 根组件（仅 <router-view />）
    ├── style.css               # 全局样式
    ├── api/                    # API 服务层
    │   ├── index.ts            # 统一导出
    │   ├── auth.ts             # 认证 API
    │   ├── user.ts             # 用户 API（需登录）
    │   ├── public.ts           # 公共 API（无需登录）
    │   ├── chat.ts             # AI 聊天 API（SSE 流式）
    │   ├── file.ts             # 文件上传 API
    │   ├── payment.ts          # 支付 API（支付宝）
    │   └── types/              # API 类型定义
    │       ├── common.ts       # 通用类型（分页/结果/文件上传）
    │       ├── auth.ts         # 认证类型
    │       ├── course.ts       # 课程类型
    │       ├── chat.ts         # 聊天类型
    │       ├── order.ts        # 订单类型
    │       ├── homework.ts     # 作业类型
    │       ├── announcement.ts # 公告类型
    │       └── carousel.ts     # 轮播图类型
    ├── assets/                 # 静态资源（经构建处理）
    ├── components/             # 公共组件
    ├── composables/            # 组合式函数
    ├── pages/                  # 页面组件
    ├── router/                 # 路由配置
    ├── store/                  # 状态管理
    ├── style/                  # 样式目录
    └── utils/                  # 工具函数
```

---

## 三、API 服务层

### HTTP 基础设施

- **Axios baseURL**：`http://159.75.11.181:9999`（硬编码）
- **timeout**：12000ms
- **Token 添加**：同时设置 `satoken`、`token`、`mqxqtoken`、`Authorization: Bearer` 四个请求头

### API 模块

| 模块 | 文件 | 路径前缀 | 认证 | 功能 |
|---|---|---|---|---|
| authApi | `api/auth.ts` | `/api/auth/` | 部分需 | 登录/注册/登出/刷新/验证码/邮箱/改密/忘密/绑定/资料 |
| publicApi | `api/public.ts` | `/api/public/` | 不需 | 课程查询/搜索/热门/免费/分类树/章节/评论/公告/轮播 |
| userApi | `api/user.ts` | `/api/user/` | 需 | 课程详情(个人化)/我的课程/学习进度/评论/章节/作业/订单/浏览记录 |
| chatApi | `api/chat.ts` | `/api/user/` | 需 | 创建会话/热门问题/会话列表/历史消息/SSE 流式聊天/停止/删除 |
| fileApi | `api/file.ts` | `/api/public/files/` | 不需 | 智能上传/图片/视频/头像/通用上传/删除 |
| paymentApi | `api/payment.ts` | `/api/alipay/` | 需 | 支付宝支付/退款/异步通知 |

### SSE 流式聊天

- 使用原生 `fetch` API（非 Axios），手动设置 Token 请求头
- `ReadableStream` 逐行读取 SSE 数据
- 事件类型：`1001`(DATA)、`1002`(STOP)、`1003`(PARAM-工具调用参数)
- 返回取消函数，支持中断生成

---

## 四、路由结构

使用 `createWebHistory`（HTML5 History 模式），所有页面懒加载。

| 路径 | 路由名 | 认证 | 功能 |
|---|---|---|---|
| `/` | Home | 否 | 首页（轮播图/搜索/热门课程/公告） |
| `/search` | Search | 否 | 搜索结果 |
| `/auth/login` | Login | requiresGuest | 登录 |
| `/auth/register` | Register | requiresGuest | 注册 |
| `/auth/forgot-password` | ForgotPassword | requiresGuest | 忘记密码 |
| `/auth/verify-email` | VerifyEmail | 否 | 邮箱验证 |
| `/courses` | CourseList | 否 | 课程列表 |
| `/courses/:id` | CourseDetail | 否 | 课程详情 |
| `/courses/:id/learn` | CourseLearn | 是 | 课程学习（视频） |
| `/courses/:id/chapters` | CourseChapters | 否 | 章节目录 |
| `/learning/courses` | MyCourses | 是 | 我的课程 |
| `/learning/history` | ViewHistory | 是 | 浏览记录 |
| `/learning/history/:id` | ViewHistoryDetail | 是 | 记录详情 |
| `/homework` | HomeworkList | 是 | 我的作业 |
| `/homework/:id` | HomeworkDetail | 是 | 作业详情 |
| `/homework/:id/submit` | HomeworkSubmit | 是 | 作业提交 |
| `/homework/submissions` | HomeworkSubmissions | 是 | 提交记录 |
| `/orders` | OrderList | 是 | 我的订单 |
| `/orders/:id` | OrderDetail | 是 | 订单详情 |
| `/orders/checkout/:courseId` | Checkout | 是 | 结算页 |
| `/payment/:orderNo` | Payment | 是 | 支付页 |
| `/profile` | Profile | 是 | 个人中心 |
| `/profile/settings` | ProfileSettings | 是 | 个人设置 |
| `/announcements` | AnnouncementList | 否 | 公告列表 |
| `/announcements/:id` | AnnouncementDetail | 否 | 公告详情 |
| `/chat` | Chat | 是 | AI 学习助手 |

**路由守卫**：
- `requiresAuth`：未登录跳转登录页，携带 redirect
- `requiresGuest`：已登录跳转首页
- 页面标题自动设置：`"{title} - 码趣星球在线学习平台"`

---

## 五、状态管理

### 唯一 Store：auth（`store/modules/auth.ts`）

**State**：accessToken / userInfo / expiresIn / tokenExpireTime

**Getters**：isAuthenticated / isLoggedIn

**Actions**：setToken / setUserInfo / resetToken / toLogin / switchCurrentRole / resetLoginState / logout / isTokenExpiringSoon / refreshToken / getUserInfo / login / checkLoginStatus / initAuth / setAuth / updateProfile

**持久化**：`pinia-plugin-persistedstate`，key=`mqxq-auth`，存储到 localStorage

**Token 多重存储**：同时使用 localStorage（`mqxqtoken`、`user`）、Cookie（`mqxqtoken`）和 Pinia 持久化

---

## 六、页面模块

| 模块 | 页面 | 功能 |
|---|---|---|
| 首页 | Home.vue | 轮播图/搜索/热门课程/公告/AI 悬浮助手 |
| 搜索 | Search.vue | 搜索结果展示 |
| 认证 | Login/Register/ForgotPassword/VerifyEmail | 登录/注册/找回密码/邮箱验证 |
| 课程 | CourseList/CourseDetail/CourseLearn/CourseChapters | 列表/详情/视频学习/章节目录 |
| 学习 | MyCourses/ViewHistory/ViewHistoryDetail | 我的课程/浏览记录 |
| 作业 | HomeworkList/HomeworkDetail/HomeworkSubmit/HomeworkSubmissions | 作业列表/详情/提交/提交记录 |
| 订单 | OrderList/OrderDetail/Checkout | 订单管理/结算 |
| 支付 | Payment.vue | 支付宝支付 |
| 个人 | Profile/ProfileSettings | 个人中心/设置 |
| 公告 | AnnouncementList/AnnouncementDetail | 公告浏览 |
| 聊天 | ChatPage.vue | AI 学习助手完整页面 |

---

## 七、公共组件

| 组件 | 功能 |
|---|---|
| Navbar.vue | 导航栏 |
| CourseCard.vue | 课程卡片 |
| CourseComments.vue | 课程评论 |
| CourseSubjectSelector.vue | 课程分类选择器 |
| Pagination.vue | 分页 |
| AIChatAssistant.vue | AI 聊天悬浮助手（右下角，可展开/调整大小） |
| ChatSessionList.vue | 聊天会话列表 |
| MessageContent.vue | 消息渲染（文本/代码块/课程卡片/订单卡片） |
| AccessModal.vue | 访问权限弹窗 |
| TailwindButton.vue | Tailwind 风格按钮 |
| TailwindCard.vue | Tailwind 风格卡片 |

---

## 八、关键依赖

| 依赖 | 版本 | 用途 |
|---|---|---|
| vue | ^3.5.18 | 核心框架 |
| vue-router | ^4.5.1 | 路由 |
| pinia | ^3.0.3 | 状态管理 |
| pinia-plugin-persistedstate | ^4.5.0 | 状态持久化 |
| axios | ^1.11.0 | HTTP 客户端 |
| element-plus | ^2.11.2 | UI 组件库 |
| @fortawesome/fontawesome-free | ^7.0.1 | 图标库 |
| marked | ^17.0.1 | Markdown 解析 |
| vite | ^7.1.2 | 构建工具 |
| typescript | ~5.8.3 | 类型系统 |
| tailwindcss | ^3.4.17 | 原子化 CSS |
| sass | ^1.92.1 | CSS 预处理器 |

---

## 九、构建与部署

- **开发**：`pnpm dev`
- **构建**：`pnpm build`（vue-tsc 类型检查 + vite build）
- **预览**：`pnpm preview`
- **产物目录**：`dist/`
- **后端地址**：硬编码 `http://159.75.11.181:9999`，无环境变量切换

---

## 十、架构特征与注意事项

1. **无全局 Layout**：每个页面自行引入 Navbar，App.vue 仅 `<router-view />`
2. **AI 助手全局植入**：Home.vue 引入 AIChatAssistant 悬浮窗
3. **双重认证守卫**：内联守卫 + permission-guard.ts（完整守卫未启用）
4. **Token 多重存储**：localStorage + Cookie + Pinia，key 不一致
5. **API 路径不一致**：部分用 `/api/user/`，部分用 `/user/`
6. **后端地址硬编码**：未配置 Vite 代理，多处直接写 IP
7. **Tailwind + Element Plus 混用**：UI 样式两套体系并存
8. **单 Store 架构**：仅 auth 一个 Store，其他状态组件内局部管理
