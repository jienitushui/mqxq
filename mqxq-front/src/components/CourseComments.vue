<template>
  <div class="course-comments">

    <!-- 发表评论 -->
    <div v-if="isAuthenticated && (canComment || isEditing)" class="comment-form">
      <h3>{{ isEditing ? '修改评价' : '发表评价' }}</h3>
      <div class="rating-input">
        <span>评分：</span>
        <div class="stars-input">
          <span 
            v-for="i in 5" 
            :key="i"
            :class="['star', { active: i <= newComment.score }]"
            @click="setRating(i)"
          >
            ★
          </span>
        </div>
      </div>
      
      <textarea
        v-model="newComment.content"
        placeholder="分享你的学习感受..."
        class="comment-textarea"
        rows="4"
      ></textarea>
      
      <div class="form-actions">
        <button 
          v-if="isEditing"
          @click="cancelEdit" 
          class="btn-cancel"
        >
          取消
        </button>
        <button 
          @click="submitComment" 
          :disabled="!canSubmit"
          class="btn-submit"
        >
          {{ isEditing ? '修改评价' : '发表评价' }}
        </button>
      </div>
    </div>

    <!-- 评论列表 -->
    <div class="comments-list">
      <div class="list-header">
        <h3>学员评价 ({{ pagination.total }})</h3>
        <select v-model="orderBy" @change="fetchComments" class="sort-select">
          <option value="createTime">最新发布</option>
          <option value="score">评分排序</option>
        </select>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>正在加载评论...</p>
      </div>

      <!-- 空状态 -->
      <div v-else-if="comments.length === 0" class="empty-state">
        <div class="empty-icon">💬</div>
        <p>暂无评价，快来发表第一条评价吧！</p>
      </div>

      <!-- 评论项 -->
      <div v-else class="comment-items">
        <div 
          v-for="comment in comments" 
          :key="comment.id"
          class="comment-item"
        >
          <div class="comment-header">
            <div class="user-info">
              <img :src="comment.avatar || defaultAvatar" :alt="comment.nickname" class="user-avatar" />
              <div class="user-details">
                <span class="username">用户名称：{{ comment.nickname }}</span>
                <div class="comment-meta">
                  <div class="stars">
                    <span 
                      v-for="i in 5" 
                      :key="i"
                      :class="['star', { filled: i <= comment.score }]"
                    >
                      ★
                    </span>
                  </div>
                  <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
                </div>
              </div>
            </div>
            
            <!-- 编辑和删除按钮 - 只对自己的评论显示 -->
            <div v-if="isAuthenticated && comment.userId === authStore.userInfo?.userId" class="comment-actions">
              <button @click="handleEditComment(comment)" class="btn-edit">
                编辑
              </button>
              <button @click="handleDeleteComment(comment.id)" class="btn-delete">
                删除
              </button>
            </div>
          </div>
          
          <div class="comment-content">
            {{ comment.content }}
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="pagination.total > 0" class="pagination-wrapper">
        <Pagination
          :current="pagination.current"
          :total="pagination.total"
          :page-size="pagination.size"
          @change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useAuthStore } from '@/store'
import { publicApi, userApi } from '@/api'
import Pagination from './Pagination.vue'

// Props
const props = defineProps({
  courseId: {
    type: [Number, String],
    required: true
  }
})

const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const comments = ref([])
const statistics = ref(null)
const canComment = ref(false)
const orderBy = ref('createTime')

// 分页信息
const pagination = reactive({
  current: 1,
  total: 0,
  size: 10
})

// 新评论
const newComment = reactive({
  courseId: props.courseId,
  score: 5,
  content: ''
})

// 编辑评论相关
const isEditing = ref(false)
const editingCommentId = ref(null)

// 计算属性
const isAuthenticated = computed(() => authStore.isAuthenticated)
const canSubmit = computed(() => newComment.content.trim().length > 0 && newComment.score > 0)
const defaultAvatar = computed(() => '/default-avatar.png')

// 获取评论列表
const fetchComments = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.current,
      size: pagination.size,
      orderBy: orderBy.value === 'createTime' ? 'create_time DESC' : orderBy.value
    }
    
    const response = await publicApi.getCourseComments(props.courseId, params)
    comments.value = response.list || response.records || []
    pagination.total = response.total || 0
    pagination.current = response.pageNum || response.current || 1
  } catch (error) {
    console.error('获取评论失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取评论统计
const fetchStatistics = async () => {
  try {
    const response = await publicApi.getCourseCommentStatistics(props.courseId)
    statistics.value = response
  } catch (error) {
    console.error('获取评论统计失败:', error)
  }
}

// 检查评论权限
const checkCommentPermission = async () => {
  if (!isAuthenticated.value) {
    canComment.value = false
    return
  }
  
  try {
    const response = await userApi.checkUserComment(props.courseId)
    canComment.value = !response.hasCommented // 如果已评论则不能再评论
  } catch (error) {
    console.error('检查评论权限失败:', error)
    canComment.value = false
  }
}

// 设置评分
const setRating = (score) => {
  newComment.score = score
}

// 编辑评论
const handleEditComment = (comment) => {
  isEditing.value = true
  editingCommentId.value = comment.id
  newComment.score = comment.score
  newComment.content = comment.content
  
  // 滚动到评论表单
  document.querySelector('.comment-form')?.scrollIntoView({ behavior: 'smooth' })
}

// 取消编辑
const cancelEdit = () => {
  isEditing.value = false
  editingCommentId.value = null
  newComment.score = 5
  newComment.content = ''
}

// 提交评论
const submitComment = async () => {
  try {
    if (isEditing.value) {
      // 修改评论
      await userApi.updateComment(editingCommentId.value, {
        content: newComment.content,
        score: newComment.score
      })
      console.log('评论修改成功')
      cancelEdit()
    } else {
      // 新建评论
      await userApi.createComment(newComment)
      console.log('评论发表成功')
      // 重置表单
      newComment.content = ''
      newComment.score = 5
    }
    
    // 刷新数据
    await Promise.all([
      fetchComments(),
      fetchStatistics(),
      checkCommentPermission()
    ])
  } catch (error) {
    console.error(isEditing.value ? '修改评论失败:' : '发表评论失败:', error)
  }
}

// 删除评论
const handleDeleteComment = async (commentId) => {
  try {
    const confirmed = confirm('确定要删除这条评论吗？')
    if (!confirmed) return
    
    await userApi.deleteComment(commentId)
    console.log('评论删除成功')
    
    // 刷新评论列表
    await Promise.all([
      fetchComments(),
      fetchStatistics(),
      checkCommentPermission()
    ])
  } catch (error) {
    console.error('删除评论失败:', error)
  }
}

// 分页处理
const handlePageChange = (page) => {
  pagination.current = page
  fetchComments()
}

// 计算百分比
const getPercentage = (count) => {
  if (!statistics.value?.totalCount) return 0
  return (count / statistics.value.totalCount * 100).toFixed(1)
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  const now = new Date()
  const diff = now - date
  
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 60) {
    return `${minutes}分钟前`
  } else if (hours < 24) {
    return `${hours}小时前`
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}

// 监听认证状态变化
watch(isAuthenticated, () => {
  checkCommentPermission()
})

// 页面挂载
onMounted(() => {
  fetchComments()
  fetchStatistics()
  checkCommentPermission()
})
</script>

<style scoped>
.course-comments {
  width: 100%;
  max-width: none;
}

/* 评论统计 */
.comment-statistics {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
}

.stats-summary {
  display: flex;
  gap: 40px;
  align-items: flex-start;
}

.average-score {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 150px;
  padding: 10px;
}

.score-number {
  font-size: 2.5rem;
  font-weight: bold;
  color: #f39c12;
  display: block;
  margin-bottom: 8px;
}

.stars {
  margin: 8px 0;
  display: flex;
  justify-content: center;
  gap: 2px;
}

.star {
  color: #ddd;
  font-size: 18px;
  transition: color 0.2s ease;
}

.star.filled {
  color: #f39c12;
}

.total-count {
  font-size: 14px;
  color: #666;
  margin-top: 8px;
}

.score-distribution {
  flex: 1;
}

.score-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.score-label {
  width: 40px;
  font-size: 14px;
  color: #666;
}

.bar-container {
  flex: 1;
  height: 8px;
  background: #e9ecef;
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: #f39c12;
  transition: width 0.3s ease;
}

.score-count {
  width: 30px;
  text-align: right;
  font-size: 14px;
  color: #666;
}

/* 评论表单 */
.comment-form {
  background: white;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  margin-bottom: 30px;
}

.comment-form h3 {
  margin-bottom: 15px;
  color: #333;
}

.rating-input {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.stars-input {
  display: flex;
  gap: 2px;
}

.stars-input .star {
  font-size: 20px;
  color: #ddd;
  cursor: pointer;
  transition: color 0.2s;
}

.stars-input .star.active,
.stars-input .star:hover {
  color: #f39c12;
}

.comment-textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  resize: vertical;
  font-family: inherit;
  margin-bottom: 15px;
}

.form-actions {
  text-align: right;
}

.btn-submit {
  padding: 10px 20px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.3s;
}

.btn-submit:hover:not(:disabled) {
  background: #5a6fd8;
}

.btn-submit:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.btn-cancel {
  padding: 10px 20px;
  background: #6c757d;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.3s;
  margin-right: 10px;
}

.btn-cancel:hover {
  background: #545b62;
}

/* 评论列表 */
.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.list-header h3 {
  color: #333;
}

.sort-select {
  padding: 6px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}



.comment-item {
  background: white;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  margin-bottom: 15px;
}

.comment-header {
  margin-bottom: 12px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.username {
  font-weight: 500;
  color: #333;
  display: block;
  margin-bottom: 4px;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.comment-meta .stars {
  display: flex;
}

.comment-meta .star {
  font-size: 14px;
  margin: 0 1px;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-content {
  text-align: start;
  line-height: 1.6;
  color: #333;
}

.comment-actions {
  display: flex;
  gap: 8px;
}

.btn-edit {
  padding: 4px 8px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-edit:hover {
  background: #0056b3;
}

.btn-delete {
  padding: 4px 8px;
  background: #dc3545;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-delete:hover {
  background: #c82333;
}

/* 加载和空状态 */
.loading-state,
.empty-state {
  text-align: center;
  padding: 40px 0;
  color: #666;
}

.loading-spinner {
  width: 30px;
  height: 30px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 15px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 15px;
}

.pagination-wrapper {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stats-summary {
    flex-direction: column;
    gap: 20px;
    align-items: center;
  }
  
  .average-score {
    min-width: auto;
    width: 100%;
    text-align: center;
  }
  
  .score-distribution {
    width: 100%;
  }
  
  .list-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .user-info {
    gap: 8px;
  }
  
  .comment-item {
    padding: 15px;
  }
  
  .comment-header {
    flex-direction: column;
    gap: 10px;
  }
  
  .comment-actions {
    align-self: flex-end;
  }
}
</style>