<template>
  <div class="payment-page">
    <Navbar />
    <div class="payment-container">
    <div class="payment-header">
      <h1>订单支付</h1>

    </div>

    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>正在加载订单信息...</p>
    </div>

    <div v-else-if="order" class="payment-content">
      <!-- 订单信息 -->
      <div class="order-info-section">
        <h3>订单信息</h3>
        <div class="order-summary">
          <div class="order-item">
            <div class="item-info">
              <h4>课程名称：{{ order.courseName }}</h4>
              <p class="order-no">订单号：{{ order.orderNo }}</p>
            </div>
            <div class="item-price">
              <span class="price">¥{{ order.actualPrice }}</span>
            </div>
          </div>
          <div class="total-section">
            <div class="total-row">
              <span>应付金额：</span>
              <span class="total-amount">¥{{ order.actualPrice }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 支付方式选择 -->
      <div class="payment-method-section">
        <h3>选择支付方式</h3>
        <div class="payment-methods">
          <div 
            v-for="method in paymentMethods" 
            :key="method.value"
            class="payment-method"
            :class="{ active: selectedMethod === method.value }"
            @click="selectedMethod = method.value"
          >
            <div class="method-icon">{{ method.icon }}</div>
            <div class="method-info">
              <div class="method-name">{{ method.name }}</div>
              <div class="method-desc">{{ method.description }}</div>
            </div>
            <div class="method-radio">
              <input 
                type="radio" 
                :value="method.value" 
                v-model="selectedMethod"
                :id="method.value"
              >
            </div>
          </div>
        </div>
      </div>

      <!-- 支付按钮 -->
      <div class="payment-action">
        <button 
          class="pay-btn"
          :disabled="!selectedMethod || paying"
          @click="handlePayment"
        >
          <span v-if="paying">正在跳转...</span>
          <span v-else>立即支付 ¥{{ order.actualPrice }}</span>
        </button>
        <p class="payment-tips">
          点击支付即表示您同意相关协议，支付过程中请勿关闭页面
        </p>
      </div>

      <!-- 支付帮助 -->
      <div class="payment-help">
        <h4>支付遇到问题？</h4>
        <div class="help-items">
          <div class="help-item">
            <span class="help-icon">💡</span>
            <span>确保网络连接正常</span>
          </div>
          <div class="help-item">
            <span class="help-icon">🔒</span>
            <span>支付过程安全加密</span>
          </div>
          <div class="help-item">
            <span class="help-icon">📞</span>
            <span>如有问题请联系客服：400-123-4567</span>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="error-state">
      <div class="error-icon">❌</div>
      <p>订单不存在或已过期</p>
      <button class="btn btn-primary" @click="goToOrders">返回订单列表</button>
    </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import Navbar from '../../components/Navbar.vue'
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi } from '../../api/user'
import { paymentApi } from '../../api/payment'
import type { Order } from '../../api/types/order'

const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const paying = ref(false)
const order = ref<Order | null>(null)
const selectedMethod = ref('alipay')


// 支付方式
const paymentMethods = [
  {
    value: 'alipay',
    name: '支付宝',
    icon: '💙',
    description: '安全快捷，支持花呗分期'
  }
]

// 获取订单信息
const loadOrderInfo = async () => {
  try {
    loading.value = true
    const orderId = route.params.orderNo as string
    console.log('订单ID:', orderId)
    const response = await userApi.getOrderDetail(Number(orderId))
    
    // 映射API数据到前端格式
    order.value = {
      id: response.id,
      orderNo: response.orderNo,
      userId: response.userId,
      courseId: response.goodsId,
      courseName: response.goodsName,
      coursePrice: response.goodsPrice,
      actualPrice: response.goodsPrice, // 使用goodsPrice作为actualPrice
      goodsImg: response.goodsImg,
      paymentMethod: response.payNo ? 'alipay' : null,
      paymentTime: response.payTime,
      payNo: response.payNo,
      status: response.status,
      createTime: response.createTime,
      updateTime: response.createTime
    }
    
    // 检查订单状态
    if (order.value.status !== 'NOT_PAY' && order.value.status !== '0' && order.value.status !== 0) {
      ElMessage.warning('订单状态异常')
      router.push('/orders')
      return
    }
    

    
  } catch (error) {
    ElMessage.error('获取订单信息失败')
  } finally {
    loading.value = false
  }
}



// 处理支付
const handlePayment = async () => {
  if (!order.value || !selectedMethod.value) return
  
  try {
    paying.value = true
    
    // 只支持支付宝支付
    if (selectedMethod.value === 'alipay') {
      await paymentApi.alipayPay(order.value.orderNo)
    } else {
      ElMessage.error('不支持的支付方式')
    }
    
  } catch (error) {
    ElMessage.error('支付失败，请重试')
  } finally {
    paying.value = false
  }
}

// 页面跳转
const goToOrders = () => {
  router.push('/orders')
}



// 生命周期
onMounted(() => {
  loadOrderInfo()
})


</script>

<style scoped>
.payment-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.payment-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}

.payment-header {
  text-align: center;
  border-bottom: 1px solid #eee;
}

.payment-header h1 {
  margin: 0 0 15px 0;
  color: #333;
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
  border-top: 4px solid #2196F3;
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

.payment-content > div {
  background: white;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.payment-content h3 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 18px;
  border-bottom: 2px solid #2196F3;
  padding-bottom: 8px;
}

.order-summary {
  border: 1px solid #eee;
  border-radius: 6px;
  overflow: hidden;
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #eee;
}

.item-info h4 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 16px;
}

.order-no {
  margin: 0;
  font-size: 14px;
  color: #666;
}

.item-price .price {
  font-size: 18px;
  font-weight: bold;
  color: #e74c3c;
}

.total-section {
  background: #f8f9fa;
  padding: 16px;
}

.total-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: bold;
}

.total-amount {
  color: #e74c3c;
  font-size: 20px;
}

.payment-methods {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.payment-method {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 2px solid #eee;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.payment-method:hover,
.payment-method.active {
  border-color: #2196F3;
  background: #f8f9ff;
}

.method-icon {
  font-size: 24px;
  margin-right: 12px;
}

.method-info {
  flex: 1;
}

.method-name {
  font-weight: 500;
  margin-bottom: 4px;
  color: #333;
}

.method-desc {
  font-size: 14px;
  color: #666;
}

.method-radio input[type="radio"] {
  width: 18px;
  height: 18px;
}

.payment-action {
  text-align: center;
}

.pay-btn {
  width: 100%;
  padding: 16px;
  background: #2196F3;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 12px;
}

.pay-btn:hover:not(:disabled) {
  background: #1976D2;
}

.pay-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.payment-tips {
  margin: 0;
  font-size: 14px;
  color: #666;
  line-height: 1.4;
}

.payment-help h4 {
  margin: 0 0 16px 0;
  color: #333;
  font-size: 16px;
}

.help-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.help-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #666;
}

.help-icon {
  font-size: 16px;
}

.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn-primary {
  background: #2196F3;
  color: white;
}

.btn-primary:hover {
  background: #1976D2;
}

@media (max-width: 768px) {
  .payment-container {
    padding: 10px;
  }

  .order-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .item-price {
    width: 100%;
    text-align: right;
  }

  .total-row {
    font-size: 14px;
  }

  .total-amount {
    font-size: 18px;
  }
}
</style>