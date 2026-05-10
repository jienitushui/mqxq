<template>
  <div class="page-header">
    <div class="page-header-content">
      <div class="page-header-title">
        <slot name="title">页面标题</slot>
      </div>
      <div class="page-header-extra" v-if="$slots.extra">
        <slot name="extra"></slot>
      </div>
    </div>
  </div>
</template>

<script setup>
// 页面头部组件
</script>

<style scoped>
.page-header {
  margin-bottom: 16px;
}

.page-header-content {
  display: flex;
  align-items: center;
  padding: 16px 0;
}

.page-header-title {
  flex: 1;
  font-size: 20px;
  font-weight: 600;
  color: var(--text-color-1);
}

.page-header-extra {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
</style>