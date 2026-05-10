# API 使用说明

本文档说明如何使用项目中的API接口。

## 项目API结构

```
src/api/
├── types/              # TypeScript 类型定义
│   ├── common.ts       # 通用类型
│   ├── auth.ts         # 认证相关类型
│   ├── course.ts       # 课程相关类型
│   ├── announcement.ts # 公告类型
│   ├── carousel.ts     # 轮播图类型
│   ├── homework.ts     # 作业类型
│   └── order.ts        # 订单类型
├── auth.ts             # 认证API
├── public.ts           # 公共API（无需登录）
├── user.ts             # 用户API（需要登录）
├── file.ts             # 文件管理API
├── payment.ts          # 支付API
└── index.ts            # 统一导出
```

## 使用方法

### 1. 导入API

```typescript
// 导入特定API模块
import { authApi, userApi, publicApi, fileApi, paymentApi } from '@/api'

// 导入类型定义
import type { LoginRequest, Course, Order } from '@/api'
```

### 2. 认证相关 (authApi)

#### 用户登录
```typescript
const loginData: LoginRequest = {
  username: 'admin',
  password: '123456',
  captcha: 'abcd',
  captchaKey: 'key123'
}

try {
  const result = await authApi.login(loginData)
  console.log('登录成功:', result)
} catch (error) {
  console.error('登录失败:', error)
}
```

#### 用户注册
```typescript
const registerData: RegisterRequest = {
  username: 'newuser',
  password: '123456',
  confirmPassword: '123456',
  email: 'user@example.com',
  nickname: '新用户',
  captcha: 'abcd',
  captchaKey: 'key123',
  emailCode: '123456',
  userType: 1 // 1-学生 2-教师
}

await authApi.register(registerData)
```

### 3. 公共API (publicApi)

#### 获取课程列表
```typescript
const courseParams = {
  page: 1,
  size: 10,
  title: '前端',
  subjectId: 1
}

const courses = await publicApi.getCourseList(courseParams)
console.log('课程列表:', courses)
```

#### 获取轮播图
```typescript
const carousels = await publicApi.getHomepageCarousel(5)
console.log('首页轮播图:', carousels)
```

### 4. 用户API (userApi)

#### 我的课程列表
```typescript
const myCourses = await userApi.getMyCourseList({
  page: 1,
  size: 10,
  status: 1 // 学习状态
})
```

#### 购买课程
```typescript
// 检查购买状态
const isPurchased = await userApi.checkPurchaseStatus(courseId)

if (!isPurchased) {
  // 购买课程
  const order = await userApi.purchaseCourse(courseId)
  console.log('订单创建成功:', order)
}
```

#### 加入课程
```typescript
await userApi.joinCourse(courseId)
console.log('加入课程成功')
```

#### 更新学习进度
```typescript
await userApi.updateStudyDuration(courseId, 300, sectionId) // 学习5分钟
```

### 5. 文件管理 (fileApi)

#### 上传头像
```typescript
const avatarFile = event.target.files[0]
const result = await fileApi.uploadAvatar(avatarFile)
console.log('头像上传成功:', result.url)
```

#### 智能文件上传
```typescript
const file = event.target.files[0]
const result = await fileApi.uploadAuto(file)
console.log('文件上传成功:', result)
```

### 6. 支付API (paymentApi)

> 支付宝完整集成流程（沙箱配置/回调/签名/退款）详见 [支付宝支付集成功能-技术深度解析](../学生端功能说明/支付宝支付集成功能-技术深度解析.md)

#### 支付宝支付
```typescript
// 创建订单后，发起支付
await paymentApi.alipayPay(orderNo)
// 页面会自动跳转到支付宝收银台
```

#### 申请退款
```typescript
const refundResult = await paymentApi.alipayRefund(orderNo)
console.log('退款结果:', refundResult)
```

### 7. 作业管理

#### 获取课程作业
```typescript
const homeworks = await userApi.getCourseHomeworks(courseId, {
  page: 1,
  size: 10
})
```

#### 提交作业
```typescript
const submission: HomeworkSubmission = {
  homeworkId: 1,
  studentId: 1,
  content: '作业内容',
  attachmentUrl: 'http://example.com/file.pdf'
}

await userApi.submitHomework(submission)
```

### 8. 订单管理

#### 获取我的订单
```typescript
const orders = await userApi.getMyOrders({
  page: 1,
  size: 10,
  status: 'PAID' // 订单状态
})
```

#### 取消订单
```typescript
await userApi.cancelOrder(orderId)
```

## 错误处理

所有API调用都应该使用 try-catch 进行错误处理：

```typescript
try {
  const result = await publicApi.getCourseDetail(courseId)
  // 处理成功结果
} catch (error) {
  // 处理错误
  console.error('API调用失败:', error)
  // 可以根据error.response.status进行不同处理
}
```

## 请求拦截器

项目已配置请求拦截器，会自动：
- 添加认证token到请求头
- 处理响应错误
- 显示错误提示信息

## 类型安全

项目使用TypeScript，所有API都有完整的类型定义，确保：
- 参数类型检查
- 返回值类型提示
- IDE智能提示和自动补全

## 注意事项

1. **认证状态**：需要登录的API会自动检查token，过期会跳转到登录页
2. **错误提示**：API错误会自动显示ElMessage提示
3. **加载状态**：建议在组件中维护loading状态
4. **分页数据**：列表接口返回PageResult类型，包含records、total等字段
5. **文件上传**：使用FormData格式，自动设置Content-Type

## 示例：完整的课程购买流程

```typescript
import { userApi, paymentApi } from '@/api'
import { ElMessage } from 'element-plus'

async function purchaseCourse(courseId: number) {
  try {
    // 1. 检查是否已购买
    const isPurchased = await userApi.checkPurchaseStatus(courseId)
    if (isPurchased) {
      ElMessage.warning('您已购买过此课程')
      return
    }

    // 2. 检查学习权限
    const canStudy = await userApi.checkCanStudy(courseId)
    if (canStudy) {
      ElMessage.info('此课程无需购买')
      return
    }

    // 3. 创建订单
    const order = await userApi.purchaseCourse(courseId)
    ElMessage.success('订单创建成功')

    // 4. 发起支付
    await paymentApi.alipayPay(order.orderNo)
    
  } catch (error) {
    console.error('购买课程失败:', error)
  }
}
```