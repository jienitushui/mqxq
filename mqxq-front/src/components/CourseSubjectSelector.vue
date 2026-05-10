<template>
  <div class="course-subject-selector">
    <div v-if="loading" class="loading">加载中...</div>
    
    <div v-else-if="error" class="error">{{ error }}</div>
    
    <div v-else class="subject-list">
      <!-- 顶级分类 -->
      <div 
        v-for="topSubject in topLevelSubjects" 
        :key="topSubject.id"
        class="subject-group"
      >
        <div 
          class="subject-item top-level"
          :class="{ active: selectedSubjectId === topSubject.id }"
          @click="selectSubject(topSubject.id)"
        >
          {{ topSubject.name }}
        </div>
        
        <!-- 子分类 -->
        <div v-if="getChildren(topSubject.id).length > 0" class="children">
          <div
            v-for="child in getChildren(topSubject.id)"
            :key="child.id"
            class="subject-item child-level"
            :class="{ active: selectedSubjectId === child.id }"
            @click="selectSubject(child.id)"
          >
            {{ child.name }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useCourseSubject } from '@/composables/useCourseSubject'

const props = defineProps<{
  modelValue?: number
}>()

const emit = defineEmits<{
  'update:modelValue': [value: number]
  'change': [subjectId: number, subjectName: string]
}>()

const {
  topLevelSubjects,
  loading,
  error,
  fetchAllSubjects,
  getChildren,
  getSubjectName
} = useCourseSubject()

const selectedSubjectId = ref<number | undefined>(props.modelValue)

const selectSubject = (subjectId: number) => {
  selectedSubjectId.value = subjectId
  emit('update:modelValue', subjectId)
  emit('change', subjectId, getSubjectName(subjectId))
}

onMounted(() => {
  fetchAllSubjects()
})
</script>

<style scoped>
.course-subject-selector {
  width: 100%;
}

.loading,
.error {
  padding: 20px;
  text-align: center;
  color: #666;
}

.error {
  color: #ff4757;
}

.subject-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.subject-group {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
}

.subject-item {
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-bottom: 1px solid #f0f0f0;
}

.subject-item:last-child {
  border-bottom: none;
}

.subject-item.top-level {
  background: #f8f9fa;
  font-weight: bold;
  font-size: 16px;
}

.subject-item.child-level {
  padding-left: 32px;
  font-size: 14px;
}

.subject-item:hover {
  background: #e3f2fd;
}

.subject-item.active {
  background: #2196f3;
  color: white;
}

.children {
  background: white;
}
</style>
