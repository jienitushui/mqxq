<template>
  <div class="order-list-page">
    <Navbar />
    <div class="order-list-container">
    <div class="page-header">
      <h1>我的订单</h1>
      <div class="order-stats">
        <div class="stat-item">
          <span class="stat-label">总订单</span>
          <span class="stat-value">{{ statistics.totalOrders }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">已支付</span>
          <span class="stat-value">{{ statistics.paidOrders }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">未支付</span>
          <span class="stat-value">{{ statistics.unpaidOrders }}</span>
        </div>
      </div>
    </div>

    <!-- 筛选器 -->
    <div class="filter-section">
      <div class="filter-item">
        <label>订单状态：</label>
        <select v-model="searchParams.status" @change="loadOrders">
          <option value="">全部</option>
          <option value="NOT_PAY">待支付</option>
          <option value="CANCEL">已取消</option>
          <option value="DONE">已完成</option>
          <option value="REFUND_DONE">已退款</option>
        </select>
      </div>
    </div>

    <!-- 订单列表 -->
    <div class="order-list" v-loading="loading">
      <div v-if="orders.length === 0" class="empty-state">
        <div class="empty-icon">📋</div>
        <p>暂无订单</p>
      </div>
      
      <div v-else>
        <div 
          v-for="order in orders" 
          :key="order.id" 
          class="order-card"
          @click="goToOrderDetail(order.id)"
        >
          <div class="order-header">
            <div class="order-info">
              <span class="order-no">订单号：{{ order.orderNo }}</span>
              <span class="order-time">{{ formatTime(order.createTime) }}</span>
            </div>
            <div class="order-status" :class="getStatusClass(order.status)">
              {{ getStatusText(order.status) }}
            </div>
          </div>
          
          <div class="order-content">
            <div class="course-info">
              <div class="course-details">
                <img v-if="order.goodsImg" :src="order.goodsImg" alt="商品图片" class="goods-img" />
                <div class="course-text">
                  <h3 class="course-name">{{ order.courseName }}</h3>
                  <div class="price-info">
                    <span class="actual-price">¥{{ order.actualPrice }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="order-actions">
            <button 
              v-if="order.status === 'NOT_PAY'" 
              class="btn btn-primary"
              @click.stop="goToPayment(order.orderNo)"
            >
              立即支付
            </button>
            <button 
              v-if="order.status === 'NOT_PAY'" 
              class="btn btn-secondary"
              @click.stop="cancelOrder(order.orderNo)"
            >
              取消订单
            </button>
            <button 
              v-if="order.status === 'PAID'" 
              class="btn btn-secondary"
              @click.stop="requestRefund(order.orderNo)"
            >
              申请退款
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="total > 0">
      <button 
        :disabled="getValidPage() <= 1"
        @click="changePage(getValidPage() - 1)"
        class="btn btn-secondary"
      >
        上一页
      </button>
      <span class="page-info">
        第 {{ getValidPage() }} 页，共 {{ Math.ceil(total / getValidSize()) }} 页
      </span>
      <button 
        :disabled="getValidPage() >= Math.ceil(total / getValidSize())"
        @click="changePage(getValidPage() + 1)"
        class="btn btn-secondary"
      >
        下一页
      </button>
    </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import Navbar from '../../components/Navbar.vue'
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '../../api/user'
import { paymentApi } from '../../api/payment'
import type { Order, OrderSearchParams, OrderStatistics } from '../../api/types/order'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const orders = ref<Order[]>([])
const total = ref(0)
const statistics = ref<OrderStatistics>({
  totalOrders: 0,
  paidOrders: 0,
  unpaidOrders: 0,
  totalAmount: 0,
  paidAmount: 0
})

const searchParams = reactive<OrderSearchParams>({
  page: 1,
  size: 10,
  status: ''
})

// 确保分页参数始终有效
const getValidPage = () => searchParams.page || 1
const getValidSize = () => searchParams.size || 10

// 加载订单列表
const loadOrders = async () => {
  try {
    loading.value = true
    const response = await userApi.getMyOrders(searchParams)
    
    console.log('API返回的完整响应:', response)
    
    // 根据实际API响应结构解析数据
    const apiResponse = response as any
    
    // 处理直接返回分页数据的情况
    if (apiResponse && apiResponse.list) {
      console.log('API返回的list数组:', apiResponse.list)
      console.log('list数组长度:', apiResponse.list.length)
      
      const mappedOrders = apiResponse.list.map((item: any) => {
        console.log('处理订单项:', item)
        return {
          id: item.id,
          orderNo: item.orderNo,
          userId: item.userId,
          courseId: item.goodsId, // API返回的是goodsId
          courseName: item.goodsName, // API返回的是goodsName
          coursePrice: item.goodsPrice, // API返回的是goodsPrice
          actualPrice: item.goodsPrice,
          goodsImg: item.goodsImg, // 添加商品图片
          paymentMethod: null,
          paymentTime: item.payTime,
          payNo: item.payNo,
          status: item.status, // 保持原始状态字符串
          createTime: item.createTime,
          updateTime: item.createTime
        }
      })
      
      console.log('映射完成的订单数组:', mappedOrders)
      orders.value = mappedOrders
      total.value = apiResponse.total || 0
      
      // 加载完订单后更新统计信息
      loadStatistics()
    } 
    // 处理嵌套data结构的情况
    else if (apiResponse.data && apiResponse.data.data && apiResponse.data.data.list) {
      console.log('API返回的list数组:', apiResponse.data.data.list)
      console.log('list数组长度:', apiResponse.data.data.list.length)
      
      const mappedOrders = apiResponse.data.data.list.map((item: any) => {
        console.log('处理订单项:', item)
        return {
          id: item.id,
          orderNo: item.orderNo,
          userId: item.userId,
          courseId: item.goodsId,
          courseName: item.goodsName,
          coursePrice: item.goodsPrice,
          actualPrice: item.goodsPrice,
          goodsImg: item.goodsImg,
          paymentMethod: null,
          paymentTime: item.payTime,
          payNo: item.payNo,
          status: item.status,
          createTime: item.createTime,
          updateTime: item.createTime
        }
      })
      
      console.log('映射完成的订单数组:', mappedOrders)
      orders.value = mappedOrders
      total.value = apiResponse.data.data.total
      
      // 加载完订单后更新统计信息
      loadStatistics()
    } else {
      orders.value = []
      total.value = 0
    }
    
    console.log('处理后的订单列表:', orders.value)
    console.log('订单数量:', orders.value.length)
    
  } catch (error) {
    ElMessage.error('加载订单列表失败')
    console.error('订单列表加载错误:', error)
  } finally {
    loading.value = false
  }
}

// 加载订单统计
const loadStatistics = async () => {
  // 根据当前订单数据计算统计信息
  statistics.value = {
    totalOrders: orders.value.length,
    paidOrders: orders.value.filter(o => o.status === 'DONE').length,
    unpaidOrders: orders.value.filter(o => o.status === 'NOT_PAY').length,
    totalAmount: orders.value.reduce((sum, o) => sum + o.actualPrice, 0),
    paidAmount: orders.value.filter(o => o.status === 'DONE').reduce((sum, o) => sum + o.actualPrice, 0)
  }
}

// 页面跳转
const goToOrderDetail = (orderId: number) => {
  console.log('跳转到订单详情，订单ID:', orderId)
  if (!orderId || isNaN(orderId)) {
    // ElMessage.error('订单ID无效')
    console.error('订单ID无效')
    return
  }
  router.push(`/orders/${orderId}`)
}

const goToPayment = async (orderNo: string) => {
  try {
    // 调用支付接口，与OrderDetail.vue保持一致
    await paymentApi.alipayPay(orderNo)
    ElMessage.success('正在跳转到支付页面...')
  } catch (error) {
    console.error('支付跳转失败:', error)
    ElMessage.error('支付跳转失败，请稍后重试')
    // 如果API调用失败，回退到页面跳转
    router.push(`/payment/${orderNo}`)
  }
}

// 取消订单
const cancelOrder = async (orderNo: string) => {
  try {
    await ElMessageBox.confirm('确定要取消这个订单吗？', '确认取消', {
      type: 'warning'
    })
    
    await userApi.cancelOrder(orderNo)
    ElMessage.success('订单已取消')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消订单失败')
    }
  }
}

// 申请退款
const requestRefund = async (orderNo: string) => {
  try {
    await ElMessageBox.confirm('确定要申请退款吗？退款将原路返回到您的支付账户。', '确认退款', {
      type: 'warning'
    })
    
    // 调用支付宝退款接口
    const response = await paymentApi.alipayRefund(orderNo)
    
    if ((response as any).code === 200) {
      ElMessage.success('退款申请已提交，请等待处理')
      // 重新加载订单列表以更新状态
      loadOrders()
    } else {
      ElMessage.error((response as any).msg || '退款申请失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退款失败:', error)
      ElMessage.error('退款申请失败，请稍后重试')
    }
  }
}

// 分页
const changePage = (page: number) => {
  searchParams.page = page
  loadOrders()
}

// 工具函数
const formatTime = (time: string) => {
  return new Date(time).toLocaleString()
}

const getStatusText = (status: number | string) => {
  // 处理字符串状态
  if (typeof status === 'string') {
    const stringStatusMap: Record<string, string> = {
      'CANCEL': '已取消',
      'NOT_PAY': '待支付',
      'DONE': '已完成',
      'REFUND_DONE': '已退款'
    }
    return stringStatusMap[status] || status
  }
  
  // 处理数字状态
  const statusMap: Record<number, string> = {
    0: '待支付',
    1: '已支付',
    2: '已取消',
    3: '已退款'
  }
  return statusMap[status] || '未知'
}

const getStatusClass = (status: number | string) => {
  // 处理字符串状态
  if (typeof status === 'string') {
    const stringClassMap: Record<string, string> = {
      'CANCEL': 'status-cancelled',
      'NOT_PAY': 'status-pending',
      'DONE': 'status-completed',
      'REFUND_DONE': 'status-refunded',
      'PAID': 'status-paid',
      'CANCELLED': 'status-cancelled',
      'REFUNDED': 'status-refunded'
    }
    return stringClassMap[status] || ''
  }
  
  // 处理数字状态
  const classMap: Record<number, string> = {
    0: 'status-pending',
    1: 'status-paid',
    2: 'status-cancelled',
    3: 'status-refunded'
  }
  return classMap[status] || ''
}



// 生命周期
onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.order-list-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.order-list-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.page-header h1 {
  margin: 0;
  color: #333;
}

.order-stats {
  display: flex;
  gap: 30px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.stat-value {
  display: block;
  font-size: 24px;
  font-weight: bold;
  color: #007bff;
}

.filter-section {
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-item label {
  font-weight: 500;
  color: #333;
}

.filter-item select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
}

.order-list {
  min-height: 400px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #666;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.order-card {
  background: white;
  border: 1px solid #eee;
  border-radius: 8px;
  margin-bottom: 16px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.order-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.order-no {
  font-weight: 500;
  color: #333;
}

.order-time {
  font-size: 14px;
  color: #666;
}

.order-status {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
}

.status-pending {
  background: #fff3cd;
  color: #856404;
}

.status-paid {
  background: #d4edda;
  color: #155724;
}

.status-cancelled {
  background: #f8d7da;
  color: #721c24;
}

.status-refunded {
  background: #d1ecf1;
  color: #0c5460;
}

.status-completed {
  background: #d4edda;
  color: #155724;
}

.status-commented {
  background: #e2e3e5;
  color: #383d41;
}

.order-content {
  margin-bottom: 15px;
}

.course-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.course-details {
  display: flex;
  align-items: center;
  gap: 15px;
  width: 100%;
}

.goods-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #eee;
}

.course-text {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.course-name {
  margin: 0;
  color: #333;
  font-size: 16px;
  flex: 1;
}

.price-info {
  display: flex;
  align-items: center;
  gap: 10px;
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

.order-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
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

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 30px;
  padding: 20px;
}

.page-info {
  color: #666;
  font-size: 14px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 20px;
    align-items: flex-start;
  }

  .order-stats {
    width: 100%;
    justify-content: space-around;
  }

  .course-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .order-actions {
    justify-content: flex-start;
  }
}
</style>