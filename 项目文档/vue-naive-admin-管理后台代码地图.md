# 码趣星球管理后台（vue-naive-admin）— 代码地图

## 一、项目概述

- **项目名称**：vue-naive-admin（码趣星球后台管理系统）
- **版本**：2.0.0
- **项目定位**：青少儿编程教学平台的后台管理端（管理员 + 教师双角色）
- **技术栈**：JavaScript（非 TypeScript）+ Vue 3 + Vite + Pinia + Naive UI + UnoCSS
- **基础模板**：基于 Ronnie Zhang 的 [vue-naive-admin](https://isme.top) 二次开发
- **包管理器**：pnpm

核心功能：管理员端（用户/课程/作业/公告/数据大屏等全量管理）、教师端（课程教学/作业批改/收入管理/AI 课件助手）。

---

## 二、目录结构

```
vue-naive-admin/
├── .env                        # 通用环境变量（VITE_TITLE = '码趣星球'）
├── .env.development            # 开发环境（Hash 路由 + 代理配置）
├── .env.production             # 生产环境（History 路由 + 相对路径）
├── build/                      # 构建辅助
│   ├── index.js                # 图标/页面路径收集工具
│   └── plugin-isme/            # 自定义 Vite 插件
│       ├── icons.js            # 虚拟模块 isme:icons（图标 safelist）
│       └── page-pathes.js      # 虚拟模块 isme:page-pathes
├── nginx.conf.example          # Nginx 部署配置
├── public/                     # 静态资源
└── src/                        # 源代码
    ├── main.js                 # 应用入口
    ├── App.vue                 # 根组件（布局切换/KeepAlive/主题）
    ├── settings.js             # 全局配置（默认布局/主题色/权限菜单定义）
    ├── api/                    # 全局公共 API
    │   └── index.js            # getUser
    ├── assets/                 # 静态资源（SVG 图标 + 图片）
    ├── components/             # 公共组件
    │   ├── common/             # 通用 UI 组件
    │   │   ├── AppCard.vue         # 卡片容器
    │   │   ├── AppPage.vue         # 页面容器（回顶/Footer）
    │   │   ├── CommonPage.vue      # 通用页面
    │   │   ├── LayoutSetting.vue   # 布局设置面板
    │   │   ├── PageHeader.vue      # 页面标题头
    │   │   ├── RichTextEditor.vue  # 富文本编辑器
    │   │   ├── TheFooter.vue       # 全局页脚
    │   │   ├── TheLogo.vue         # Logo
    │   │   ├── ThemeSetting.vue    # 主题设置
    │   │   └── ToggleTheme.vue     # 明暗切换
    │   └── me/                  # 业务封装组件
    │       ├── crud/
    │       │   ├── index.vue       # MeCrud（搜索+表格+分页+导出 Excel）
    │       │   └── QueryItem.vue   # MeQueryItem（搜索条件项）
    │       └── modal/
    │           ├── index.vue       # MeModal（可拖拽模态框）
    │           └── utils.js        # 拖拽初始化
    ├── composables/             # 组合式函数
    │   ├── useAliveData.js      # KeepAlive 数据持久化
    │   ├── useCrud.js           # CRUD 操作封装
    │   ├── useForm.js           # 表单封装
    │   └── useModal.js          # 弹窗封装
    ├── directives/              # 自定义指令
    │   └── index.js             # v-permission 权限指令
    ├── layouts/                 # 布局组件
    │   ├── empty/               # 空布局（登录页/错误页）
    │   ├── full/                # 完整布局（侧栏+Header+Tab 栏+内容）
    │   ├── normal/              # 标准布局（侧栏+Header+内容，默认）
    │   ├── simple/              # 简约布局（侧栏+内容）
    │   └── components/          # 布局子组件
    │       ├── BeginnerGuide.vue    # 新手引导
    │       ├── BreadCrumb.vue       # 面包屑
    │       ├── Fullscreen.vue       # 全屏切换
    │       ├── MenuCollapse.vue     # 侧栏折叠
    │       ├── RoleSelect.vue       # 角色切换弹窗
    │       ├── SideLogo.vue         # 侧栏 Logo
    │       ├── SideMenu.vue         # 侧栏菜单
    │       ├── UserAvatar.vue       # 用户头像下拉
    │       └── tab/                 # Tab 标签栏 + 右键菜单
    ├── router/                  # 路由
    │   ├── index.js             # 路由实例（Hash/History 切换）
    │   ├── basic-routes.js      # 静态路由（/login, /404, /403, /profile）
    │   └── guards/              # 路由守卫
    │       ├── permission-guard.js  # 权限守卫（Token/动态路由）
    │       ├── page-loading-guard.js
    │       ├── page-title-guard.js
    │       └── tab-guard.js
    ├── store/                   # 状态管理（Pinia）
    │   ├── helper.js            # 辅助函数
    │   └── modules/
    │       ├── app.js           # 应用状态（布局/暗色/折叠/主题色）
    │       ├── auth.js          # 认证状态（Token/登录/登出/刷新）
    │       ├── permission.js    # 权限状态（菜单/动态路由生成）
    │       ├── router.js        # 路由实例引用
    │       ├── tab.js           # Tab 标签状态
    │       └── user.js          # 用户信息
    ├── styles/                  # 全局样式
    │   ├── global.css           # 动画/滚动条/主题切换
    │   └── reset.css            # CSS 重置
    ├── utils/                   # 工具函数
    │   ├── common.js            # 日期格式化/节流/防抖/sleep/ResizeObserver
    │   ├── is.js                # 类型判断工具集
    │   ├── naiveTools.js        # Naive UI 全局 API（message/dialog/notification/loadingBar）
    │   ├── http/                # HTTP 请求层
    │   │   ├── index.js         # Axios 实例
    │   │   ├── interceptors.js  # 拦截器（Token/无感刷新/错误处理）
    │   │   └── helpers.js       # 错误码处理
    │   └── storage/             # 本地存储
    │       ├── index.js         # lStorage/sStorage 工厂
    │       └── storage.js       # Storage 类（前缀/过期/序列化）
    └── views/                   # 页面视图
        ├── login/               # 登录页（双角色/验证码/忘记密码）
        ├── error-page/          # 403, 404
        ├── profile/             # 个人资料
        ├── pms/                 # 管理员端模块
        │   ├── dashboard/       #   数据大屏
        │   ├── user/            #   用户管理
        │   ├── role/            #   角色管理
        │   ├── course/          #   课程管理
        │   ├── chapter/         #   章节管理
        │   ├── section/         #   小节管理
        │   ├── course-subject/  #   课程分类
        │   ├── course-comment/  #   课程评论
        │   ├── my-course/       #   我的课程
        │   ├── announcement/    #   公告管理
        │   ├── announcement-category/ # 公告分类
        │   ├── carousel/        #   轮播图管理
        │   ├── homework/        #   作业列表
        │   ├── homework-submission/ # 作业提交
        │   ├── statistics/      #   统计仪表板
        │   ├── course-browse/   #   课程浏览
        │   └── order-management/ #  订单管理
        └── teacherhome/         # 教师端模块
            ├── dashboard/       #   数据大屏
            ├── course/          #   我的课程
            ├── chapter/         #   章节管理
            ├── section/         #   小节管理
            ├── homework/        #   作业发布
            ├── grade/           #   作业批改
            ├── submission-manage/ # 提交管理
            ├── my-course-students/ # 我的学员
            ├── course-comment/  #   课程评论
            ├── statistics/      #   数据统计
            ├── course-view/     #   课程浏览
            ├── revenue/         #   收入管理
            └── manus-chat/      #   AI 课件助手（SSE 流式聊天）
```

---

## 三、路由系统

### 路由模式

根据 `VITE_USE_HASH` 环境变量切换 Hash / History 模式。

### 静态路由

| 路径 | 名称 | 布局 | 说明 |
|---|---|---|---|
| `/login` | Login | empty | 登录页 |
| `/404` | 404 | empty | 页面不存在 |
| `/403` | 403 | empty | 无权限 |
| `/profile` | Profile | normal | 个人资料 |

### 动态路由生成（核心设计）

不使用传统前端路由表，而是在 `settings.js` 中定义权限配置，权限守卫中动态生成路由并 `router.addRoute()`。

生成流程：
1. 过滤 `type === 'MENU'` 的权限项
2. `generateRoute()` 生成扁平路由对象
3. 外链映射到 iframe 页面
4. `import.meta.glob('@/views/**/*.vue')` 解析组件
5. 逐个 `router.addRoute()` 添加

### 路由守卫链

| 守卫 | 功能 |
|---|---|
| page-loading-guard | 顶部加载进度条 |
| permission-guard | Token 校验 + 白名单 + 动态路由加载 |
| page-title-guard | 页面标题 `页面标题 | 码趣星球` |
| tab-guard | 自动添加 Tab 标签 |

---

## 四、权限菜单体系

> 权限菜单定义（不含路由路径）见 [公共参考-前端权限体系](../公共参考/系统基础设施配置汇总.md#四前端开发环境配置)。以下为含路由路径的完整菜单树，用于代码导航。

### 管理员权限（adminPermissions）

```
码趣星球
├── 数据大屏                 -> /pms/dashboard
├── 系统管理
│   ├── 用户管理             -> /pms/user
│   └── 角色管理             -> /pms/role
├── 课程管理
│   ├── 课程管理             -> /pms/course
│   ├── 章节管理             -> /pms/chapter
│   ├── 小节管理             -> /pms/section
│   ├── 课程分类             -> /pms/course-subject
│   ├── 课程评论             -> /pms/course-comment
│   └── 我的课程             -> /pms/my-course
├── 内容管理
│   ├── 公告管理             -> /pms/announcement
│   ├── 公告分类             -> /pms/announcement-category
│   └── 轮播图管理           -> /pms/carousel
├── 作业管理
│   ├── 作业列表             -> /pms/homework
│   └── 作业提交             -> /pms/homework-submission
└── 数据分析
    ├── 统计仪表板           -> /pms/statistics
    ├── 课程浏览             -> /pms/course-browse
    └── 订单管理             -> /pms/order-management
```

### 教师权限（teacherPermissions）

```
码趣星球
├── 数据大屏                 -> /teacherhome/dashboard
├── 课程教学
│   ├── 我的课程             -> /teacherhome/course
│   ├── 章节管理             -> /teacherhome/chapter
│   └── 小节管理             -> /teacherhome/section
├── 作业管理
│   ├── 作业发布             -> /teacherhome/homework
│   ├── 作业批改             -> /teacherhome/grade
│   └── 作业提交管理         -> /teacherhome/submission-manage
├── 学员管理
│   ├── 我的学员             -> /teacherhome/my-course-students
│   └── 课程评论             -> /teacherhome/course-comment
├── 教学分析
│   ├── 数据统计             -> /teacherhome/statistics
│   ├── 课程浏览             -> /teacherhome/course-view
│   └── 收入管理             -> /teacherhome/revenue
└── AI 助手
    └── AI 课件助手          -> /teacherhome/manus-chat
```

---

## 五、状态管理（Pinia）

| Store | 持久化 | 说明 |
|---|---|---|
| **app** | sessionStorage（collapsed/layout/primaryColor） | 侧栏折叠/暗色模式/布局/主题色 |
| **auth** | localStorage（全量） | Token/登录/登出/切换角色/刷新 Token |
| **user** | localStorage（全量） | 用户信息/当前角色/角色列表 |
| **permission** | 无 | 菜单/动态路由生成 |
| **router** | 无 | 路由实例引用 + resetRouter() |
| **tab** | sessionStorage（tabs） | Tab 标签栏状态 |

### Token 无感刷新

- 请求前自动检查是否 30 分钟内过期，过期则刷新
- 401 响应自动刷新并重试原请求
- 请求队列机制防止并发刷新

---

## 六、HTTP 请求层

### 架构

```
utils/http/
├── index.js         # createAxios() 工厂函数，导出 request 实例
├── interceptors.js  # 请求/响应拦截器（Token 注入/无感刷新/错误处理）
└── helpers.js       # 错误码映射（401/403/404/500/业务码）
```

### 请求拦截器

1. `needToken === false` 直接放行
2. Token 即将过期 → 触发刷新（防并发队列）
3. 注入 `Authorization: Bearer <token>`

### 响应拦截器

- 成功码 `[0, 200, '0', '200']` 返回 data
- 401 → 尝试刷新 Token → 成功重试 → 失败登出
- 其他错误码走 `resolveResError()` 映射

### 业务错误码

| 码值 | 含义 |
|---|---|
| 4001 | 用户名或密码错误 |
| 4002 | 验证码错误或已过期 |
| 4003 | 账户已被锁定 |
| 4005 | 权限不足 |
| 4006 | 用户名已存在 |
| 4007 | 邮箱已存在 |
| 4008 | 邮箱验证码错误 |
| 5001-5005 | 邮件/验证码/密码相关错误 |
| 401 | Token 无效 |
| 403 | 请求被拒绝 |
| 404 | 资源不存在 |
| 500 | 服务器异常 |

---

## 七、API 服务层

采用**页面级 API** 模式，每个视图模块自带 `api.js`。

### 管理员 API（`/api/admin/`）

| 模块 | 页面 | 核心功能 |
|---|---|---|
| 用户管理 | `pms/user/api.js` | CRUD + 批量删除 + 重置密码 + 状态切换 + 角色查询 |
| 角色管理 | `pms/role/api.js` | CRUD + 批量删除 + 权限分配 |
| 课程管理 | `pms/course/api.js` | CRUD + 发布/下架 + 统计 + 分类/教师列表 |
| 作业管理 | `pms/homework/api.js` | 列表/强制发布/取消/删除/统计/排行/导出 |
| 统计仪表板 | `pms/statistics/api.js` | 用户/课程/浏览/学习/作业/订单/评论等统计 |
| 数据大屏 | `pms/dashboard/api.js` | 概览/分布/趋势/排行 |
| 订单管理 | `pms/order-management/api.js` | 列表/详情/删除/统计 |

### 教师 API（`/api/teacher/`）

| 模块 | 页面 | 核心功能 |
|---|---|---|
| 教师首页 | `teacherhome/api.js` | 课程/学生/作业统计/评论/待处理/热门/收入 |
| 教师课程 | `teacherhome/course/api.js` | CRUD + 发布/下架 + 分类 |
| 教师作业 | `teacherhome/homework/api.js` | CRUD + 发布 + 导出（blob） |
| AI 课件助手 | `manus-chat/` | SSE 流式 GET `/api/teacher/manus/chat?message=...` |

### 公共 API

| 模块 | 路径 |
|---|---|
| 认证 | `/api/auth/*`（登录/登出/验证码/邮箱/改密/资料/刷新） |
| 个人资料 | `/api/auth/update-profile`, `/api/public/files/avatar` |
| 课程分类 | `/api/public/course-subject/tree` |

---

## 八、布局系统

4 种布局，页面通过 `route.meta.layout` 指定：

| 布局 | 结构 | 使用场景 |
|---|---|---|
| **empty** | 纯 slot | 登录页、错误页 |
| **normal**（默认） | 侧栏 + Header + 内容 | 日常管理页面 |
| **full** | 侧栏 + Header + Tab 栏 + 内容 | 带 Tab 标签页面 |
| **simple** | 侧栏 + 内容 | 极简页面 |

侧栏：展开 220px / 折叠 64px，过渡动画。

---

## 九、组合式函数（Composables）

| 函数 | 功能 |
|---|---|
| **useCrud** | 完整 CRUD 流程封装：handleAdd/Edit/View/Delete/Save，自动管理 modal/form/okLoading |
| **useForm** | 表单封装：formRef, formModel(深拷贝 init), validation, required 规则 |
| **useModal** | 弹窗封装：modalRef, okLoading |
| **useAliveData** | KeepAlive 数据持久化：基于 Map 按路由 name 缓存响应式数据 |

---

## 十、特色功能

### AI 课件助手

- 原生 `fetch` + `ReadableStream` 处理 SSE
- 支持步骤解析（Step/下载链接/错误/完成）
- 生成 Word 文档(.docx)提供下载
- 后端 API：GET `/api/teacher/manus/chat?message=...`

### CRUD 表格组件（MeCrud）

- 集成搜索表单 + NDataTable + 分页
- 支持前端/后端分页切换
- Excel 导出（xlsx 库）
- 约定分页接口格式（pageNo/pageSize/pageData/total）

### 可拖拽弹窗（MeModal）

- 基于n-modal封装，标题栏拖拽
- 命令式调用（open/close/handleOk/handleCancel）

### 角色切换

UserAvatar 下拉 → RoleSelect 选择角色 → authStore.switchCurrentRole() → location.reload() → 权限守卫重新生成路由

### 权限指令

`v-permission="'user:delete'"` — 根据当前路由 `meta.btns` 判断按钮权限，无权限移除 DOM

---

## 十一、关键依赖

| 依赖 | 版本 | 用途 |
|---|---|---|
| vue | ^3.5.17 | 核心框架 |
| vue-router | ^4.5.1 | 路由 |
| pinia | ^3.0.3 | 状态管理 |
| naive-ui | ^2.42.0 | UI 组件库 |
| axios | ^1.10.0 | HTTP 客户端 |
| echarts | ^5.6.0 | 图表库 |
| vue-echarts | ^7.0.3 | Vue ECharts |
| xlsx | ^0.18.5 | Excel 导出 |
| dayjs | ^1.11.13 | 日期处理 |
| lodash-es | ^4.17.21 | 工具函数 |
| @vueuse/core | ^13.3.0 | 组合式工具集 |
| vite | ^6.3.5 | 构建工具 |
| unocss | ^66.2.3 | 原子化 CSS |
| @iconify/json | ^2.2.350 | 图标数据集 |

---

## 十二、构建与部署

| 命令 | 说明 |
|---|---|
| `pnpm dev` | 开发服务器（端口 3200，代理 `/api` → 后端） |
| `pnpm build` | 生产构建 |
| `pnpm preview` | 预览构建产物 |
| `pnpm lint:fix` | ESLint 自动修复 |

### Nginx 部署

- 监听 3200 端口
- 前端：`try_files $uri $uri/ /index.html`（SPA History 模式）
- 后端：`/api` → `http://159.75.11.181:9999`

### UnoCSS 配置

- 预设：presetWind3 + presetAttributify + presetIcons + presetRemToPx
- 图标集：`i-fe:`（feather）、`i-me:`（isme）、`i-carbon:`（iconify）
- 快捷方式：`wh-full`、`f-c-c`、`flex-col`、`card-border`、`auto-bg` 等
- 主题色：`primary: rgba(var(--primary-color))`

### 自动导入

- `unplugin-auto-import`：vue/vue-router API 自动导入
- `unplugin-vue-components`：NaiveUI 组件自动解析

---

## 十三、架构特征

1. **零 TypeScript**：全项目纯 JS，降低学习门槛
2. **前端权限驱动**：菜单/路由由 `settings.js` 前端配置驱动，非后端返回
3. **扁平化路由**：所有页面一级路由，无嵌套，解决 KeepAlive 难题
4. **模块零耦合**：每个视图模块独立，删除不影响其他
5. **页面级 API**：每个页面自带 `api.js`，非集中式
6. **双角色体系**：管理员与教师两套完全不同的菜单/页面/API
7. **4 种布局**：empty/normal/full/simple，页面级指定
8. **全局 NaiveUI 工具**：`createDiscreteApi` 在 setup 外也可用 message/dialog
9. **MeCrud/MeModal/useCrud**：高度封装的 CRUD 体系，减少重复代码
10. **SSE AI 聊天**：教师端 AI 课件助手，原生 fetch 处理流式响应
