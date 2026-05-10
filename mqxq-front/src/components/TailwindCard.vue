<template>
  <div :class="cardClasses">
    <div v-if="$slots.header" class="mb-4 pb-4 border-b border-gray-200">
      <slot name="header"></slot>
    </div>
    
    <div class="flex-1">
      <slot></slot>
    </div>
    
    <div v-if="$slots.footer" class="mt-4 pt-4 border-t border-gray-200">
      <slot name="footer"></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
// 简化的工具函数
const cn = (...classes: (string | undefined | null | boolean)[]): string => {
  return classes.filter(Boolean).join(' ')
}

const commonClasses = {
  card: {
    default: 'bg-white rounded-xl shadow-lg p-6 transition-all duration-300 hover:shadow-xl',
    bordered: 'bg-white border border-gray-200 rounded-xl p-6 transition-all duration-300 hover:border-primary/30',
    elevated: 'bg-white rounded-xl shadow-lg p-6 transition-all duration-300 hover:shadow-xl',
  }
}

interface Props {
  variant?: 'default' | 'bordered' | 'elevated'
  padding?: 'none' | 'small' | 'default' | 'large'
  hoverable?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'default',
  padding: 'default',
  hoverable: true,
})

const cardClasses = computed(() => {
  const baseClasses = commonClasses.card[props.variant]
  
  const paddingClasses = {
    none: 'p-0',
    small: 'p-4',
    default: 'p-6',
    large: 'p-8',
  }
  
  return cn(
    baseClasses.replace('p-6', ''), // 移除默认padding
    paddingClasses[props.padding],
    props.hoverable && 'hover:transform hover:scale-[1.02]',
    'flex flex-col'
  )
})
</script>

<style scoped>
/* 组件样式 */
</style>