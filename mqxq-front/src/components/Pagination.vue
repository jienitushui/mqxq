<template>
  <div class="pagination">
    <button 
      class="pagination-btn" 
      :disabled="current <= 1"
      @click="handlePageChange(current - 1)"
    >
      上一页
    </button>
    
    <div class="pagination-pages">
      <button
        v-for="page in visiblePages"
        :key="page"
        class="pagination-page"
        :class="{ active: page === current }"
        @click="handlePageChange(page)"
      >
        {{ page }}
      </button>
    </div>
    
    <button 
      class="pagination-btn" 
      :disabled="current >= totalPages"
      @click="handlePageChange(current + 1)"
    >
      下一页
    </button>
    
    <div class="pagination-info">
      共 {{ total }} 条，第 {{ current }}/{{ totalPages }} 页
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  current: {
    type: Number,
    default: 1
  },
  total: {
    type: Number,
    default: 0
  },
  pageSize: {
    type: Number,
    default: 10
  }
})

const emit = defineEmits(['change'])

const totalPages = computed(() => {
  return Math.ceil(props.total / props.pageSize) || 1
})

const visiblePages = computed(() => {
  const pages = []
  const total = totalPages.value
  const current = props.current
  
  if (total <= 7) {
    // 如果总页数小于等于7，显示所有页码
    for (let i = 1; i <= total; i++) {
      pages.push(i)
    }
  } else {
    // 否则显示部分页码
    if (current <= 4) {
      for (let i = 1; i <= 5; i++) {
        pages.push(i)
      }
      pages.push('...')
      pages.push(total)
    } else if (current >= total - 3) {
      pages.push(1)
      pages.push('...')
      for (let i = total - 4; i <= total; i++) {
        pages.push(i)
      }
    } else {
      pages.push(1)
      pages.push('...')
      for (let i = current - 1; i <= current + 1; i++) {
        pages.push(i)
      }
      pages.push('...')
      pages.push(total)
    }
  }
  
  return pages
})

const handlePageChange = (page) => {
  if (page !== props.current && page >= 1 && page <= totalPages.value) {
    emit('change', page)
  }
}
</script>

<style scoped>
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
}

.pagination-btn,
.pagination-page {
  padding: 8px 12px;
  border: 1px solid #ddd;
  background: white;
  color: #333;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
}

.pagination-btn:hover:not(:disabled),
.pagination-page:hover {
  background: #f5f5f5;
  border-color: #999;
}

.pagination-btn:disabled {
  background: #f5f5f5;
  color: #999;
  cursor: not-allowed;
}

.pagination-page.active {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.pagination-info {
  margin-left: 16px;
  font-size: 14px;
  color: #666;
}

@media (max-width: 768px) {
  .pagination {
    flex-direction: column;
    gap: 12px;
  }
  
  .pagination-pages {
    display: flex;
    gap: 4px;
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .pagination-info {
    margin-left: 0;
  }
}
</style>