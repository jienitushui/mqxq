<template>
  <div class="message-content-wrapper">
    <div v-for="(block, index) in parsedBlocks" :key="index">
      <!-- 文本块 -->
      <div v-if="block.type === 'text'" class="text-block" v-html="formatText(block.content)"></div>
      
      <!-- 代码块 -->
      <div v-else-if="block.type === 'code'" class="code-block">
        <div class="code-header">
          <span class="code-language">{{ block.language || 'code' }}</span>
          <button @click="copyCode(block.content, index)" class="copy-btn">
            <svg v-if="!copiedIndex[index]" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                    d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
            </svg>
            <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
            </svg>
            <span class="ml-1">{{ copiedIndex[index] ? '已复制' : '复制' }}</span>
          </button>
        </div>
        <pre class="code-content"><code>{{ block.content }}</code></pre>
      </div>
    </div>

    <!-- 课程卡片 (只在没有订单卡片时显示) -->
    <div v-if="courseCards.length > 0 && orderCards.length === 0" class="course-cards-container">
      <div v-for="course in courseCards" :key="course.id" class="course-card-wrapper">
        <div class="course-card" @click="goToCourse(course.id)">
          <div class="course-cover">
            <img :src="course.cover" :alt="course.name" />
          </div>
          <div class="course-info">
            <h4 class="course-title">{{ course.name }}</h4>
            <p class="course-detail">{{ truncateText(course.detail, 60) }}</p>
            <div class="course-stats">
              <span class="stat-item">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                </svg>
                {{ course.buyCount }}人购买
              </span>
              <span class="stat-item">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                </svg>
                {{ course.viewCount }}次浏览
              </span>
            </div>
            <div class="course-footer">
              <span class="course-price">¥{{ course.price }}</span>
              <span class="course-lessons">{{ course.lessonNum }}课时 · {{ formatDuration(course.durationSum) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!-- 订单卡片 -->
    <div v-if="orderCards.length > 0" class="order-cards-container">
      <div v-for="order in orderCards" :key="order.id || order.courseId" class="order-card-wrapper">
        <!-- 错误订单卡片 -->
        <div v-if="order.status === 'ERROR'" class="order-card error-order-card">
          <div class="order-header error-header">
            <div class="order-no">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
              <span class="error-title">订单异常</span>
            </div>
            <span class="order-status status-error">
              需要处理
            </span>
          </div>
          <div class="order-body">
            <div class="error-message">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <p class="error-text">{{ order.errorMessage }}</p>
            </div>
            <div class="error-hint">
              <p>💡 建议操作：</p>
              <ul>
                <li>前往"我的订单"页面查看待支付订单</li>
                <li>完成支付或取消现有订单后重新购买</li>
              </ul>
            </div>
          </div>
          <div class="order-footer">
            <button 
              @click.stop="goToMyOrders"
              class="order-btn order-btn-primary"
            >
              查看我的订单
            </button>
          </div>
        </div>
        
        <!-- 正常订单卡片 -->
        <div v-else class="order-card" @click="goToOrder(order.id)">
          <div class="order-header">
            <div class="order-no">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
              订单号：{{ order.orderNo }}
            </div>
            <span class="order-status" :class="getOrderStatusClass(order.status)">
              {{ getOrderStatusText(order.status) }}
            </span>
          </div>
          <div class="order-body">
            <div class="order-course">
              <h4 class="order-course-name">{{ order.courseName }}</h4>
              <div class="order-info-row">
                <span class="order-info-label">课程价格：</span>
                <span class="order-price">¥{{ order.price }}</span>
              </div>
              <div class="order-info-row">
                <span class="order-info-label">创建时间：</span>
                <span class="order-time">{{ formatOrderTime(order.createTime) }}</span>
              </div>
            </div>
          </div>
          <div class="order-footer">
            <button 
              v-if="order.status === 'NOT_PAY'" 
              @click.stop="payOrder(order.id)"
              class="order-btn order-btn-primary"
            >
              立即支付
            </button>
            <button 
              @click.stop="viewOrderDetail(order.id)"
              class="order-btn order-btn-secondary"
            >
              查看详情
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

interface Props {
  content: string
  params?: Record<string, any> | null
}

const props = defineProps<Props>()
const router = useRouter()

interface ContentBlock {
  type: 'text' | 'code'
  content: string
  language?: string
}

const copiedIndex = ref<Record<number, boolean>>({})

// 提取课程信息
const courseCards = computed(() => {
  if (!props.params) return []
  
  const courses: any[] = []
  Object.entries(props.params).forEach(([key, value]) => {
    if (key.startsWith('courseInfo_') && value && typeof value === 'object') {
      courses.push(value)
    }
  })
  
  return courses
})

// 提取订单信息（包括错误订单）
const orderCards = computed(() => {
  if (!props.params) return []
  
  const orders: any[] = []
  Object.entries(props.params).forEach(([key, value]) => {
    if (key.startsWith('orderInfo_') && value && typeof value === 'object') {
      orders.push(value)
    }
  })
  
  return orders
})

// 解析消息内容，分离文本和代码块
const parsedBlocks = computed<ContentBlock[]>(() => {
  const blocks: ContentBlock[] = []
  const content = props.content
  
  // 匹配代码块: ```language\ncode\n``` 或 ```\ncode\n```
  const codeBlockRegex = /```(\w+)?\n?([\s\S]*?)```/g
  let lastIndex = 0
  let match
  
  while ((match = codeBlockRegex.exec(content)) !== null) {
    // 添加代码块之前的文本
    if (match.index > lastIndex) {
      const textContent = content.substring(lastIndex, match.index)
      if (textContent.trim()) {
        blocks.push({
          type: 'text',
          content: textContent
        })
      }
    }
    
    // 添加代码块
    blocks.push({
      type: 'code',
      language: match[1] || 'code',
      content: match[2].trim()
    })
    
    lastIndex = match.index + match[0].length
  }
  
  // 添加最后的文本
  if (lastIndex < content.length) {
    const textContent = content.substring(lastIndex)
    if (textContent.trim()) {
      blocks.push({
        type: 'text',
        content: textContent
      })
    }
  }
  
  // 如果没有代码块，返回整个内容作为文本
  if (blocks.length === 0) {
    blocks.push({
      type: 'text',
      content: content
    })
  }
  
  return blocks
})

// 格式化文本（处理行内代码、加粗等）
const formatText = (text: string) => {
  return text
    .replace(/\n/g, '<br>')
    .replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>')
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
}

// 复制代码
const copyCode = async (code: string, index: number) => {
  try {
    await navigator.clipboard.writeText(code)
    copiedIndex.value[index] = true
    
    // 2秒后重置状态
    setTimeout(() => {
      copiedIndex.value[index] = false
    }, 2000)
  } catch (error) {
    console.error('复制失败:', error)
  }
}

// 截断文本
const truncateText = (text: string, maxLength: number) => {
  if (!text) return ''
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

// 格式化时长
const formatDuration = (minutes: number) => {
  if (minutes < 60) return `${minutes}分钟`
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return mins > 0 ? `${hours}小时${mins}分钟` : `${hours}小时`
}

// 跳转到课程详情
const goToCourse = (courseId: number) => {
  router.push({ name: 'CourseDetail', params: { id: courseId } })
}

// 跳转到订单详情
const goToOrder = (orderId: number) => {
  if (!orderId) return
  router.push({ name: 'OrderDetail', params: { id: orderId } })
}

// 查看订单详情
const viewOrderDetail = (orderId: number) => {
  if (!orderId) return
  router.push({ name: 'OrderDetail', params: { id: orderId } })
}

// 跳转到我的订单页面
const goToMyOrders = () => {
  router.push({ name: 'OrderList' })
}

// 支付订单
const payOrder = async (orderId: number) => {
  try {
    // 首先需要获取订单号，因为支付接口需要orderNo而不是orderId
    // 从orderCards中查找对应的订单
    const order = orderCards.value.find(o => o.id === orderId)
    if (!order || !order.orderNo) {
      console.error('订单信息不完整')
      return
    }
    
    // 调用支付接口
    const { paymentApi } = await import('../api/payment')
    await paymentApi.alipayPay(order.orderNo)
  } catch (error) {
    console.error('支付跳转失败:', error)
    // 如果API调用失败，回退到页面跳转
    router.push({ name: 'OrderDetail', params: { id: orderId } })
  }
}

// 获取订单状态文本
const getOrderStatusText = (status: string | number) => {
  const statusMap: Record<string, string> = {
    'NOT_PAY': '待支付',
    'PAID': '已支付',
    'CANCELLED': '已取消',
    'REFUNDED': '已退款',
    'REFUND_PENDING': '退款中',
    'DONE': '已完成',
    'ERROR': '异常'
  }
  return statusMap[String(status)] || '未知状态'
}

// 获取订单状态样式类
const getOrderStatusClass = (status: string | number) => {
  const statusClassMap: Record<string, string> = {
    'NOT_PAY': 'status-pending',
    'PAID': 'status-success',
    'CANCELLED': 'status-cancelled',
    'REFUNDED': 'status-refunded',
    'REFUND_PENDING': 'status-refunding',
    'DONE': 'status-success',
    'ERROR': 'status-error'
  }
  return statusClassMap[String(status)] || ''
}

// 格式化订单时间
const formatOrderTime = (timeStr: string) => {
  try {
    const date = new Date(timeStr)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return timeStr
  }
}
</script>

<style scoped lang="scss">
.message-content-wrapper {
  width: 100%;
}

.text-block {
  padding: 12px 16px;
  font-size: 15px;
  line-height: 1.6;
  word-wrap: break-word;
  text-align: left;
  
  :deep(.inline-code) {
    background: rgba(0, 0, 0, 0.05);
    padding: 2px 8px;
    border-radius: 4px;
    font-family: 'Courier New', monospace;
    font-size: 14px;
  }
  
  :deep(strong) {
    font-weight: 600;
  }
}

.code-block {
  margin: 8px 0;
  border-radius: 8px;
  overflow: hidden;
  background: #1e1e1e;
  border: 1px solid #333;
}

.code-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: #2d2d2d;
  border-bottom: 1px solid #333;
}

.code-language {
  font-size: 12px;
  color: #888;
  text-transform: uppercase;
  font-weight: 500;
}

.copy-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  background: transparent;
  color: #888;
  border: 1px solid #444;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    background: #3b82f6;
    color: #fff;
    border-color: #3b82f6;
  }
}

.code-content {
  padding: 16px;
  margin: 0;
  overflow-x: auto;
  
  code {
    font-family: 'Courier New', 'Consolas', monospace;
    font-size: 14px;
    line-height: 1.5;
    color: #d4d4d4;
    white-space: pre;
  }
}

// 课程卡片样式
.course-cards-container {
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  text-align: left;
}

.course-card-wrapper {
  width: 100%;
}

.course-card {
  display: flex;
  gap: 16px;
  background: #f7f9fc;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
    border-color: #667eea;
  }

  // 响应式：小屏幕下垂直布局
  @media (max-width: 600px) {
    flex-direction: column;
    gap: 0;
  }
}

.course-cover {
  width: 180px;
  height: 135px;
  flex-shrink: 0;
  overflow: hidden;
  background: #e5e7eb;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  // 响应式：小屏幕下全宽
  @media (max-width: 600px) {
    width: 100%;
    height: 160px;
  }
}

.course-info {
  flex: 1;
  padding: 12px 16px 12px 0;
  display: flex;
  flex-direction: column;
  gap: 8px;

  // 响应式：小屏幕下调整内边距
  @media (max-width: 600px) {
    padding: 12px;
  }
}

.course-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
  line-height: 1.4;
  text-align: left;

  // 响应式：小屏幕下调整字体
  @media (max-width: 600px) {
    font-size: 15px;
  }
}

.course-detail {
  font-size: 13px;
  color: #6b7280;
  margin: 0;
  line-height: 1.5;
  text-align: left;
}

.course-stats {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #6b7280;

  // 响应式：小屏幕下调整间距和字体
  @media (max-width: 600px) {
    gap: 12px;
    font-size: 12px;
    flex-wrap: wrap;
  }
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;

  svg {
    flex-shrink: 0;
  }
}

.course-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
}

.course-price {
  font-size: 18px;
  font-weight: 700;
  color: #667eea;

  // 响应式：小屏幕下调整字体
  @media (max-width: 600px) {
    font-size: 16px;
  }
}

.course-lessons {
  font-size: 13px;
  color: #6b7280;

  // 响应式：小屏幕下调整字体
  @media (max-width: 600px) {
    font-size: 12px;
  }
}

// 订单卡片样式
.order-cards-container {
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  text-align: left;
}

.order-card-wrapper {
  width: 100%;
}

.order-card {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
    border-color: #667eea;
  }
}

.order-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f7f9fc;
  border-bottom: 1px solid #e5e7eb;
}

.order-no {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6b7280;
  font-weight: 500;
}

.order-status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;

  &.status-pending {
    background: #fef3c7;
    color: #d97706;
  }

  &.status-success {
    background: #d1fae5;
    color: #059669;
  }

  &.status-cancelled {
    background: #fee2e2;
    color: #dc2626;
  }

  &.status-refunded {
    background: #e0e7ff;
    color: #6366f1;
  }

  &.status-refunding {
    background: #fef3c7;
    color: #d97706;
  }

  &.status-error {
    background: #fee2e2;
    color: #dc2626;
  }
}

.order-body {
  padding: 16px;
}

.order-course {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.order-course-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
  line-height: 1.4;
}

.order-info-row {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.order-info-label {
  color: #6b7280;
  margin-right: 8px;
}

.order-price {
  font-size: 18px;
  font-weight: 700;
  color: #667eea;
}

.order-time {
  color: #374151;
}

.order-footer {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  background: #f7f9fc;
  border-top: 1px solid #e5e7eb;
}

.order-btn {
  flex: 1;
  padding: 10px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;

  &:hover {
    transform: translateY(-1px);
  }
}

.order-btn-primary {
  background: #667eea;
  color: white;

  &:hover {
    background: #5568d3;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  }
}

.order-btn-secondary {
  background: white;
  color: #667eea;
  border: 1px solid #667eea;

  &:hover {
    background: #f0f4ff;
  }
}

// 错误订单卡片样式
.error-order-card {
  border-color: #fca5a5;
  background: #fef2f2;

  &:hover {
    border-color: #f87171;
    box-shadow: 0 8px 20px rgba(248, 113, 113, 0.2);
  }
}

.error-header {
  background: #fee2e2;
  border-bottom-color: #fca5a5;

  .order-no {
    color: #991b1b;
    font-weight: 600;
    gap: 8px;

    svg {
      width: 20px;
      height: 20px;
    }
  }

  .error-title {
    font-size: 14px;
  }
}

.error-message {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background: white;
  border-radius: 8px;
  border: 1px solid #fca5a5;
  margin-bottom: 16px;

  svg {
    flex-shrink: 0;
    color: #dc2626;
    margin-top: 2px;
  }

  .error-text {
    flex: 1;
    margin: 0;
    color: #991b1b;
    font-size: 14px;
    line-height: 1.6;
    font-weight: 500;
  }
}

.error-hint {
  padding: 12px 16px;
  background: #fffbeb;
  border-radius: 8px;
  border: 1px solid #fde68a;

  p {
    margin: 0 0 8px 0;
    color: #92400e;
    font-size: 13px;
    font-weight: 600;
  }

  ul {
    margin: 0;
    padding-left: 20px;
    color: #78350f;
    font-size: 13px;
    line-height: 1.8;

    li {
      margin-bottom: 4px;

      &:last-child {
        margin-bottom: 0;
      }
    }
  }
}

.order-btn {
  svg {
    width: 16px;
    height: 16px;
  }
}
</style>
