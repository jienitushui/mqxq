<template>
  <div class="checkout-page">
    <Navbar />
    <div class="checkout-container">
    <div class="page-header">
      <h1>订单结算</h1>
    </div>

    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>正在创建订单...</p>
    </div>

    <div v-else class="checkout-content">
      <!-- 课程信息 -->
      <div class="course-section">
        <h3>课程信息</h3>
        <div class="course-card" v-if="courseInfo">
          <div class="course-image">
            <img :src="courseInfo.coverImage || '/default-course.jpg'" :alt="courseInfo.name">
          </div>
          <div class="course-info">
            <h4>{{ courseInfo.name || '课程名称' }}</h4>
            <p class="course-desc">{{ courseInfo.description || '暂无课程描述' }}</p>
            <div class="course-meta">
              <span class="teacher">讲师：{{ courseInfo.teacherName || '未知' }}</span>
              <span class="duration">时长：{{ courseInfo.duration || 0 }}小时</span>
            </div>
          </div>
          <div class="price-info">
            <div class="original-price" v-if="courseInfo.discountPrice && courseInfo.discountPrice < courseInfo.price">
              ¥{{ courseInfo.price }}
            </div>
            <div class="current-price">
              ¥{{ courseInfo.discountPrice || courseInfo.price || 0 }}
            </div>
          </div>
        </div>
      </div>



      <!-- 价格明细 -->
      <div class="price-section">
        <h3>价格明细</h3>
        <div class="price-details">
          <div class="price-row">
            <span>课程价格</span>
            <span>¥{{ coursePrice }}</span>
          </div>

          <div class="price-row total-row">
            <span>实付金额</span>
            <span class="total-price">¥{{ finalPrice }}</span>
          </div>
        </div>
      </div>

      <!-- 支付方式 -->
      <div class="payment-section">
        <h3>支付方式</h3>
        <div class="payment-methods">
          <label class="payment-method" v-for="method in paymentMethods" :key="method.value">
            <input 
              type="radio" 
              :value="method.value" 
              v-model="selectedPaymentMethod"
              name="paymentMethod"
            >
            <div class="method-info">
              <div class="method-icon">{{ method.icon }}</div>
              <div class="method-details">
                <div class="method-name">{{ method.name }}</div>
                <div class="method-desc">{{ method.description }}</div>
              </div>
            </div>
          </label>
        </div>
      </div>

      <!-- 提交订单 -->
      <div class="submit-section">
        <div class="agreement">
          <label>
            <input type="checkbox" v-model="agreeTerms">
            我已阅读并同意 <a href="/terms" target="_blank">《用户协议》</a> 和 <a href="/privacy" target="_blank">《隐私政策》</a>
          </label>
        </div>
        <button 
          class="submit-btn"
          :disabled="!canSubmit"
          @click="createOrder"
        >
          创建订单,去支付 ¥{{ finalPrice }}
        </button>
      </div>
    </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import Navbar from '../../components/Navbar.vue'
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi } from '../../api/user'
import { publicApi } from '../../api/public'
import { getCourseDetail } from '../../utils/courseApi'

const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const courseInfo = ref(null)

const selectedPaymentMethod = ref('alipay')
const agreeTerms = ref(false)


// 价格相关
const coursePrice = ref(0)

const finalPrice = ref(0)

// 支付方式
const paymentMethods = [
  {
    value: 'alipay',
    name: '支付宝',
    icon: '💙',
    description: '安全快捷，支持花呗分期付款'
  }
]

// 计算属性
const canSubmit = computed(() => {
  return agreeTerms.value && selectedPaymentMethod.value && !loading.value
})

// 获取课程信息
const loadCourseInfo = async () => {
  try {
    const courseId = Number(route.params.courseId)
    console.log('获取到的课程ID:', courseId)
    console.log('路由参数:', route.params)
    
    if (!courseId || isNaN(courseId)) {
      ElMessage.error('课程信息不存在')
      router.push('/courses')
      return
    }

    // 使用智能接口获取课程详情
    const response = await getCourseDetail(courseId)
    console.log('API返回的课程数据:', response)
    
    courseInfo.value = {
      id: response.id,
      name: response.title, // API返回的是title字段
      description: response.description,
      teacherName: '讲师', // 需要从其他地方获取讲师信息
      duration: Math.floor((response.durationSum || 0) / 60), // 转换为小时
      price: response.price || 0,
      discountPrice: response.price || 0, // 如果没有折扣价，使用原价
      coverImage: response.cover // API返回的是cover字段
    }
    
    console.log('处理后的课程信息:', courseInfo.value)
    coursePrice.value = courseInfo.value.discountPrice || courseInfo.value.price
    calculateTotal()
  } catch (error) {
    console.error('获取课程信息失败:', error)
  }
}



// 计算总价
const calculateTotal = () => {
  finalPrice.value = coursePrice.value
}

// 创建订单
const createOrder = async () => {
  if (!canSubmit.value) return
  
  try {
    loading.value = true
    
    const order = await userApi.createCourseOrder(courseInfo.value.id)
    
    ElMessage.success('订单创建成功')
    
    // 跳转到支付页面
    router.push(`/payment/${order.id}`)
    
  } catch (error) {
    ElMessage.error('创建订单失败')
  } finally {
    loading.value = false
  }
}

// 生命周期
onMounted(() => {
  loadCourseInfo()
})
</script>

<style scoped>
.checkout-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.checkout-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
  text-align: center;
}

.page-header h1 {
  margin: 0;
  color: #333;
}

.loading-state {
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

.checkout-content > div {
  background: white;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.checkout-content h3 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 18px;
  border-bottom: 2px solid #2196F3;
  padding-bottom: 8px;
}

.course-card {
  display: flex;
  gap: 20px;
  align-items: center;
}

.course-image {
  flex-shrink: 0;
}

.course-image img {
  width: 120px;
  height: 80px;
  object-fit: cover;
  border-radius: 6px;
}

.course-info {
  flex: 1;
}

.course-info h4 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 16px;
}

.course-desc {
  margin: 0 0 12px 0;
  color: #666;
  font-size: 14px;
  line-height: 1.4;
}

.course-meta {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #888;
}

.price-info {
  text-align: right;
}

.original-price {
  text-decoration: line-through;
  color: #999;
  font-size: 14px;
  margin-bottom: 4px;
}

.current-price {
  font-size: 20px;
  font-weight: bold;
  color: #e74c3c;
}

.coupon-selector select {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  background: white;
}

.price-details {
  border: 1px solid #eee;
  border-radius: 6px;
  overflow: hidden;
}

.price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #eee;
}

.price-row:last-child {
  border-bottom: none;
}

.total-row {
  background: #f8f9fa;
  font-weight: bold;
  font-size: 16px;
}

.discount {
  color: #28a745;
}

.total-price {
  color: #e74c3c;
  font-size: 18px;
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

.payment-method:hover {
  border-color: #2196F3;
}

.payment-method input[type="radio"] {
  margin-right: 12px;
}

.payment-method input[type="radio"]:checked + .method-info {
  color: #2196F3;
}

.method-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.method-icon {
  font-size: 24px;
}

.method-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.method-desc {
  font-size: 14px;
  color: #666;
}

.agreement {
  margin-bottom: 20px;
  text-align: center;
}

.agreement label {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 14px;
  color: #666;
}

.agreement a {
  color: #2196F3;
  text-decoration: none;
}

.agreement a:hover {
  text-decoration: underline;
}

.submit-btn {
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
}

.submit-btn:hover:not(:disabled) {
  background: #1976D2;
}

.submit-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .checkout-container {
    padding: 10px;
  }

  .course-card {
    flex-direction: column;
    text-align: center;
  }

  .course-image img {
    width: 100%;
    height: 150px;
  }

  .price-info {
    text-align: center;
    width: 100%;
  }

  .course-meta {
    justify-content: center;
  }
}
</style>