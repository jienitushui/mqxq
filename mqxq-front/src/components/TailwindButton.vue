<template>
  <button
    :class="buttonClasses"
    :disabled="disabled || loading"
    @click="handleClick"
  >
    <i v-if="loading" class="fas fa-spinner fa-spin mr-2"></i>
    <i v-else-if="icon" :class="icon" class="mr-2"></i>
    <slot></slot>
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue'
// 简化的工具函数
const cn = (...classes: (string | undefined | null | boolean)[]): string => {
  return classes.filter(Boolean).join(' ')
}

const commonClasses = {
  button: {
    primary: 'bg-primary hover:bg-primary/90 text-white font-medium py-2 px-4 rounded-lg shadow-md hover:shadow-lg transition-all duration-300',
    secondary: 'bg-secondary hover:bg-secondary/90 text-white font-medium py-2 px-4 rounded-lg shadow-md hover:shadow-lg transition-all duration-300',
    outline: 'border-2 border-primary text-primary hover:bg-primary hover:text-white font-medium py-2 px-4 rounded-lg transition-all duration-300',
    ghost: 'text-primary hover:bg-primary/10 font-medium py-2 px-4 rounded-lg transition-all duration-300',
  }
}

interface Props {
  variant?: 'primary' | 'secondary' | 'outline' | 'ghost'
  size?: 'small' | 'default' | 'large'
  disabled?: boolean
  loading?: boolean
  icon?: string
  fullWidth?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'primary',
  size: 'default',
  disabled: false,
  loading: false,
  fullWidth: false,
})

const emit = defineEmits<{
  click: [event: MouseEvent]
}>()

const buttonClasses = computed(() => {
  const baseClasses = commonClasses.button[props.variant]
  
  const sizeClasses = {
    small: 'py-1 px-3 text-sm',
    default: 'py-2 px-4',
    large: 'py-3 px-6 text-lg',
  }
  
  return cn(
    baseClasses,
    sizeClasses[props.size],
    props.fullWidth && 'w-full',
    (props.disabled || props.loading) && 'opacity-50 cursor-not-allowed',
    'inline-flex items-center justify-center'
  )
})

const handleClick = (event: MouseEvent) => {
  if (!props.disabled && !props.loading) {
    emit('click', event)
  }
}
</script>

<style scoped>
/* 组件样式 */
</style>