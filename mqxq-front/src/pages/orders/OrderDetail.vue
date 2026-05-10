<template>
  <div class="order-detail-page">
    <Navbar />
    <div class="order-detail-container">
    <div class="page-header">
      <button class="back-btn" @click="goBack">
        ← 返回订单列表
      </button>
      <h1>订单详情</h1>
    </div>

    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else-if="order" class="order-detail">
      <!-- 订单状态 -->
      <div class="status-section">
        <div class="status-icon" :class="getStatusClass(order.status)">
          {{ getStatusIcon(order.status) }}
        </div>
        <div class="status-info">
          <h2>{{ getStatusText(order.status) }}</h2>
          <p class="status-desc">{{ getStatusDescription(order.status) }}</p>
        </div>
      </div>

      <!-- 订单信息 -->
      <div class="info-section">
        <h3>订单信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <label>订单号：</label>
            <span>{{ order.orderNo }}</span>
            <button class="copy-btn" @click="copyOrderNo">复制</button>
          </div>
          <div class="info-item">
            <label>创建时间：</label>
            <span>{{ formatTime(order.createTime) }}</span>
          </div>
          <div class="info-item" v-if="order.paymentTime">
            <label>支付时间：</label>
            <span>{{ formatTime(order.paymentTime) }}</span>
          </div>
          <div class="info-item" v-if="order.paymentMethod">
            <label>支付方式：</label>
            <span>{{ getPaymentMethodText(order.paymentMethod) }}</span>
          </div>
        </div>
      </div>

      <!-- 课程信息 -->
      <div class="course-section">
        <h3>课程信息</h3>
        <div class="course-card">
          <div class="course-info">
            <h4>课程名称：{{ order.courseName }}</h4>
            <div class="course-meta">
              <span>课程ID: {{ order.courseId }}</span>
            </div>
          </div>
          <div class="price-info">
            <div class="price-row" v-if="order.coursePrice !== order.actualPrice">
              <span class="label">原价：</span>
              <span class="original-price">¥{{ order.coursePrice }}</span>
            </div>
            <div class="price-row">
              <span class="label">实付：</span>
              <span class="actual-price">¥{{ order.actualPrice }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-section">
        <button 
          v-if="order.status === 'NOT_PAY'" 
          class="btn btn-primary btn-large"
          @click="goToPayment"
        >
          立即支付
        </button>
        <button 
          v-if="order.status === 'NOT_PAY'" 
          class="btn btn-secondary"
          @click="cancelOrder"
        >
          取消订单
        </button>
        <button 
          v-if="order.status === 'PAID' || order.status === 'DONE'" 
          class="btn btn-secondary"
          @click="requestRefund"
        >
          申请退款
        </button>
        <button 
          v-if="order.status === 'PAID' || order.status === 'DONE'" 
          class="btn btn-outline"
          @click="goToCourse"
        >
          去学习
        </button>
      </div>
    </div>

    <div v-else class="error-state">
      <div class="error-icon">❌</div>
      <p>订单不存在或已被删除</p>
      <button class="btn btn-primary" @click="goBack">返回订单列表</button>
    </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import Navbar from '../../components/Navbar.vue'
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '../../api/user'
import { paymentApi } from '../../api/payment'
import type { Order } from '../../api/types/order'

const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const order = ref<Order | null>(null)

// 获取订单详情
const loadOrderDetail = async () => {
  try {
    loading.value = true
    // 尝试从不同的路由参数中获取订单ID
    const orderId = Number(route.params.id) || Number(route.params.orderId) || Number(route.query.id)
    
    console.log('订单详情页面 - 路由参数:', route.params)
    console.log('订单详情页面 - 查询参数:', route.query)
    console.log('订单详情页面 - 解析的订单ID:', orderId)
    
    if (!orderId || isNaN(orderId)) {
      // ElMessage.error('订单ID无效')
      router.push('/orders')
      return
    }
    
    const response = await userApi.getOrderDetail(orderId)
    console.log('订单详情API响应:', response)
    
    
    // 直接使用API返回的数据
    const orderData = response as any
    order.value = {
      id: orderData.id,
      orderNo: orderData.orderNo,
      userId: orderData.userId,
      courseId: orderData.goodsId, // API返回的是goodsId
      courseName: orderData.goodsName, // API返回的是goodsName
      coursePrice: orderData.goodsPrice, // API返回的是goodsPrice
      actualPrice: orderData.goodsPrice,
      goodsImg: orderData.goodsImg, // 商品图片
      paymentMethod: orderData.payNo ? 'alipay' : null, // 根据payNo判断支付方式
      paymentTime: orderData.payTime,
      payNo: orderData.payNo,
      status: orderData.status,
      createTime: orderData.createTime,
      updateTime: orderData.createTime
    }
    
    console.log('处理后的订单详情:', order.value)
  } catch (error) {
    ElMessage.error('获取订单详情失败')
    console.error('订单详情获取错误:', error)
  } finally {
    loading.value = false
  }
}

// 页面跳转
const goBack = () => {
  router.push('/orders')
}

const goToPayment = async () => {
  if (!order.value) return
  
  try {
    // 调用支付接口，与OrderList.vue保持一致
    await paymentApi.alipayPay(order.value.orderNo)
    ElMessage.success('正在跳转到支付页面...')
  } catch (error) {
    console.error('支付跳转失败:', error)
    ElMessage.error('支付跳转失败，请稍后重试')
    // 如果API调用失败，回退到页面跳转
    router.push(`/payment/${order.value.id}`)
  }
}

const goToCourse = () => {
  if (order.value) {
    router.push(`/courses/${order.value.courseId}`)
  }
}

// 订单操作
const cancelOrder = async () => {
  if (!order.value) return
  
  try {
    await ElMessageBox.confirm('确定要取消这个订单吗？', '确认取消', {
      type: 'warning'
    })
    
    await userApi.cancelOrder(order.value.orderNo)
    ElMessage.success('订单已取消')
    loadOrderDetail() // 重新加载订单详情
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消订单失败')
    }
  }
}

const requestRefund = async () => {
  if (!order.value) return
  
  try {
    await ElMessageBox.confirm('确定要申请退款吗？退款将在3-5个工作日内到账。', '确认退款', {
      type: 'warning'
    })
    
    // 这里需要调用退款接口
    ElMessage.success('退款申请已提交，请耐心等待处理')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('申请退款失败')
    }
  }
}

// 复制订单号
const copyOrderNo = async () => {
  if (!order.value) return
  
  try {
    await navigator.clipboard.writeText(order.value.orderNo)
    ElMessage.success('订单号已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败，请手动复制')
  }
}

// 工具函数
const formatTime = (time: string) => {
  return new Date(time).toLocaleString()
}

const getStatusText = (status: number | string) => {
  // 处理字符串状态
  if (typeof status === 'string') {
    const stringStatusMap = {
      'CANCEL': '已取消',
      'NOT_PAY': '待支付',
      'DONE': '已完成',
      'REFUND_DONE': '已退款',
      'COMMENT_DONE': '已评价',
      'PAID': '已支付',
      'CANCELLED': '已取消',
      'REFUNDED': '已退款'
    }
    return stringStatusMap[status] || status
  }
  
  // 处理数字状态
  const statusMap = {
    0: '待支付',
    1: '已支付',
    2: '已取消',
    3: '已退款'
  }
  return statusMap[status] || '未知状态'
}

const getStatusDescription = (status: number | string) => {
  // 处理字符串状态
  if (typeof status === 'string') {
    const stringDescMap = {
      'CANCEL': '订单已取消，如有疑问请联系客服',
      'NOT_PAY': '请在24小时内完成支付，超时订单将自动取消',
      'DONE': '订单已完成，感谢您的购买',
      'REFUND_DONE': '退款已处理，款项将在3-5个工作日内到账',
      'COMMENT_DONE': '订单已完成并已评价，感谢您的反馈',
      'PAID': '支付成功，可以开始学习课程了',
      'CANCELLED': '订单已取消，如有疑问请联系客服',
      'REFUNDED': '退款已处理，款项将在3-5个工作日内到账'
    }
    return stringDescMap[status] || ''
  }
  
  // 处理数字状态
  const descMap = {
    0: '请在24小时内完成支付，超时订单将自动取消',
    1: '支付成功，可以开始学习课程了',
    2: '订单已取消，如有疑问请联系客服',
    3: '退款已处理，款项将在3-5个工作日内到账'
  }
  return descMap[status] || ''
}

const getStatusIcon = (status: number | string) => {
  // 处理字符串状态
  if (typeof status === 'string') {
    const stringIconMap = {
      'CANCEL': '❌',
      'NOT_PAY': '⏰',
      'DONE': '🎉',
      'REFUND_DONE': '💰',
      'COMMENT_DONE': '⭐',
      'PAID': '✅',
      'CANCELLED': '❌',
      'REFUNDED': '💰'
    }
    return stringIconMap[status] || '❓'
  }
  
  // 处理数字状态
  const iconMap = {
    0: '⏰',
    1: '✅',
    2: '❌',
    3: '💰'
  }
  return iconMap[status] || '❓'
}

const getStatusClass = (status: number | string) => {
  // 处理字符串状态
  if (typeof status === 'string') {
    const stringClassMap = {
      'CANCEL': 'status-cancelled',
      'NOT_PAY': 'status-pending',
      'DONE': 'status-completed',
      'REFUND_DONE': 'status-refunded',
      'COMMENT_DONE': 'status-commented',
      'PAID': 'status-paid',
      'CANCELLED': 'status-cancelled',
      'REFUNDED': 'status-refunded'
    }
    return stringClassMap[status] || ''
  }
  
  // 处理数字状态
  const classMap = {
    0: 'status-pending',
    1: 'status-paid',
    2: 'status-cancelled',
    3: 'status-refunded'
  }
  return classMap[status] || ''
}

const getPaymentMethodText = (method: string) => {
  const methodMap = {
    'alipay': '支付宝',
    'wechat': '微信支付',
    'bank': '银行卡'
  }
  return methodMap[method] || method
}

// 生命周期
onMounted(() => {
  loadOrderDetail()
})
</script>

<style scoped>
.order-detail-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.order-detail-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 30px;
  position: relative;
}

.back-btn {
  border: 1px solid #ddd;  
  background: white;
  color: #007bff;
  /* cursor: pointer; */
  font-size: 14px;
  padding: 8px 12px;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.back-btn:hover {
  background-color: #f8f9fa;
  text-decoration: underline;
}

.page-header h1 {
  margin: 0;
  color: #333;
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
}

.loading-state, .error-state {
  text-align: center;
  padding: 60px 20px;
  color: #666;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #007bff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.order-detail {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.status-section {
  display: flex;
  align-items: center;
  padding: 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.status-icon {
  font-size: 48px;
  margin-right: 20px;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.2);
}

.status-info h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
}

.status-desc {
  margin: 0;
  opacity: 0.9;
  font-size: 14px;
}

.info-section, .course-section {
  padding: 30px;
  border-bottom: 1px solid #eee;
}

.info-section h3, .course-section h3 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 18px;
}

.info-grid {
  display: grid;
  gap: 15px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.info-item label {
  font-weight: 500;
  color: #666;
  min-width: 80px;
}

.info-item span {
  color: #333;
}

.copy-btn {
  background: #f8f9fa;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 12px;
  cursor: pointer;
  color: #666;
}

.copy-btn:hover {
  background: #e9ecef;
}

.course-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.course-info h4 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 16px;
}

.course-meta {
  font-size: 14px;
  color: #666;
}

.price-info {
  text-align: right;
}

.price-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 5px;
}

.price-row:last-child {
  margin-bottom: 0;
}

.price-row .label {
  font-size: 14px;
  color: #666;
}

.original-price {
  text-decoration: line-through;
  color: #999;
  font-size: 14px;
}

.actual-price {
  font-size: 18px;
  font-weight: bold;
  color: #e74c3c;
}

.action-section {
  padding: 30px;
  display: flex;
  gap: 15px;
  justify-content: center;
}

.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  text-decoration: none;
  display: inline-block;
  text-align: center;
}

.btn-large {
  padding: 15px 30px;
  font-size: 16px;
}

.btn-primary {
  background: #007bff;
  color: white;
}

.btn-primary:hover {
  background: #0056b3;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #545b62;
}

.btn-outline {
  background: transparent;
  color: #007bff;
  border: 1px solid #007bff;
}

.btn-outline:hover {
  background: #007bff;
  color: white;
}

@media (max-width: 768px) {
  .order-detail-container {
    padding: 10px;
  }

  .status-section {
    flex-direction: column;
    text-align: center;
    padding: 20px;
  }

  .status-icon {
    margin-right: 0;
    margin-bottom: 15px;
  }

  .course-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .price-info {
    text-align: left;
    width: 100%;
  }

  .action-section {
    flex-direction: column;
    padding: 20px;
  }

  .btn {
    width: 100%;
  }
}
</style>