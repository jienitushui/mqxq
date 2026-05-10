<template>
  <div v-if="visible" class="access-modal" @click="handleBackdropClick">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h3>{{ modalTitle }}</h3>
        <button @click="close" class="btn-close">×</button>
      </div>
      
      <div class="modal-body">
        <div class="access-info">
          <div class="info-icon">
            <i v-if="type === 'login'">🔐</i>
            <i v-else-if="type === 'purchase'">💰</i>
            <i v-else-if="type === 'join'">📚</i>
          </div>
          
          <div class="info-content">
            <p class="info-message">{{ modalMessage }}</p>
            
            <!-- 课程信息 -->
            <div v-if="courseInfo" class="course-preview">
              <img :src="courseInfo.cover" :alt="courseInfo.title" class="course-cover" />
              <div class="course-details">
                <h4 class="course-title">{{ courseInfo.title }}</h4>
                <div class="course-meta">
                  <span v-if="courseInfo.price > 0" class="price">¥{{ courseInfo.price }}</span>
                  <span v-else class="free-tag">免费课程</span>
                  <span class="lesson-count">{{ courseInfo.lessonNum }}课时</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="modal-footer">
        <button @click="close" class="btn-secondary">取消</button>
        <button @click="handleConfirm" class="btn-primary" :disabled="loading">
          <span v-if="loading" class="loading-spinner"></span>
          {{ confirmButtonText }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  type: {
    type: String,
    required: true,
    validator: (value) => ['login', 'purchase', 'join'].includes(value)
  },
  courseInfo: {
    type: Object,
    default: null
  }
})

// Emits
const emit = defineEmits(['close', 'confirm'])

// 响应式数据
const loading = ref(false)

// 计算属性
const modalTitle = computed(() => {
  const titles = {
    login: '需要登录',
    purchase: '需要购买课程',
    join: '加入课程'
  }
  return titles[props.type] || ''
})

const modalMessage = computed(() => {
  const messages = {
    login: '请先登录后再观看课程内容',
    purchase: '该课程需要购买后才能观看完整内容',
    join: '需要先加入课程才能开始学习'
  }
  return messages[props.type] || ''
})

const confirmButtonText = computed(() => {
  const texts = {
    login: '立即登录',
    purchase: '立即购买',
    join: '立即加入'
  }
  return texts[props.type] || '确认'
})

// 方法
const close = () => {
  emit('close')
}

const handleBackdropClick = () => {
  close()
}

const handleConfirm = async () => {
  loading.value = true
  try {
    await emit('confirm')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.access-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 480px;
  max-width: 90vw;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: modalSlideIn 0.3s ease-out;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e9ecef;
  background: #f8f9fa;
}

.modal-header h3 {
  margin: 0;
  color: #333;
  font-size: 1.2rem;
  font-weight: 600;
}

.btn-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s ease;
}

.btn-close:hover {
  background: #e9ecef;
  color: #666;
}

.modal-body {
  padding: 24px;
}

.access-info {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.info-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(45deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.info-content {
  flex: 1;
}

.info-message {
  font-size: 16px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 20px;
}

.course-preview {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.course-cover {
  width: 80px;
  height: 60px;
  object-fit: cover;
  border-radius: 6px;
  flex-shrink: 0;
}

.course-details {
  flex: 1;
}

.course-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  line-height: 1.4;
}

.course-meta {
  display: flex;
  gap: 12px;
  align-items: center;
  font-size: 12px;
}

.price {
  color: #e74c3c;
  font-weight: 600;
}

.free-tag {
  background: #4ecdc4;
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 500;
}

.lesson-count {
  color: #666;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid #e9ecef;
  background: #f8f9fa;
}

.btn-primary,
.btn-secondary {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 100px;
  justify-content: center;
}

.btn-primary {
  background: linear-gradient(45deg, #667eea, #764ba2);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn-secondary {
  background: white;
  color: #666;
  border: 1px solid #ddd;
}

.btn-secondary:hover {
  background: #f8f9fa;
  border-color: #bbb;
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .modal-content {
    width: 95vw;
    margin: 20px;
  }
  
  .access-info {
    flex-direction: column;
    text-align: center;
  }
  
  .course-preview {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .course-cover {
    width: 120px;
    height: 90px;
  }
  
  .modal-footer {
    flex-direction: column;
  }
  
  .btn-primary,
  .btn-secondary {
    width: 100%;
  }
}
</style>