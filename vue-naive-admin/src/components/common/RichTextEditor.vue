<template>
  <div class="rich-text-editor">
    <div class="toolbar" v-if="!readonly">
      <n-button-group size="small">
        <n-button @click="execCommand('bold')" :type="isActive('bold') ? 'primary' : 'default'">
          <template #icon>
            <i class="i-carbon:text-bold" />
          </template>
        </n-button>
        <n-button @click="execCommand('italic')" :type="isActive('italic') ? 'primary' : 'default'">
          <template #icon>
            <i class="i-carbon:text-italic" />
          </template>
        </n-button>
        <n-button @click="execCommand('underline')" :type="isActive('underline') ? 'primary' : 'default'">
          <template #icon>
            <i class="i-carbon:text-underline" />
          </template>
        </n-button>
      </n-button-group>
      
      <n-button-group size="small" style="margin-left: 8px">
        <n-button @click="execCommand('justifyLeft')" :type="isActive('justifyLeft') ? 'primary' : 'default'">
          <template #icon>
            <i class="i-carbon:text-align-left" />
          </template>
        </n-button>
        <n-button @click="execCommand('justifyCenter')" :type="isActive('justifyCenter') ? 'primary' : 'default'">
          <template #icon>
            <i class="i-carbon:text-align-center" />
          </template>
        </n-button>
        <n-button @click="execCommand('justifyRight')" :type="isActive('justifyRight') ? 'primary' : 'default'">
          <template #icon>
            <i class="i-carbon:text-align-right" />
          </template>
        </n-button>
      </n-button-group>

      <n-button-group size="small" style="margin-left: 8px">
        <n-button @click="execCommand('insertUnorderedList')" :type="isActive('insertUnorderedList') ? 'primary' : 'default'">
          <template #icon>
            <i class="i-carbon:list" />
          </template>
        </n-button>
        <n-button @click="execCommand('insertOrderedList')" :type="isActive('insertOrderedList') ? 'primary' : 'default'">
          <template #icon>
            <i class="i-carbon:list-numbered" />
          </template>
        </n-button>
      </n-button-group>
    </div>
    
    <div
      ref="editorRef"
      class="editor-content"
      :contenteditable="!readonly"
      @input="handleInput"
      @blur="handleBlur"
      @focus="handleFocus"
      v-html="modelValue"
    ></div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import { NButton, NButtonGroup } from 'naive-ui'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  readonly: {
    type: Boolean,
    default: false
  },
  placeholder: {
    type: String,
    default: '请输入内容...'
  }
})

const emit = defineEmits(['update:modelValue', 'blur', 'focus'])

const editorRef = ref(null)

const execCommand = (command, value = null) => {
  document.execCommand(command, false, value)
  editorRef.value?.focus()
  handleInput()
}

const isActive = (command) => {
  return document.queryCommandState(command)
}

const handleInput = () => {
  const content = editorRef.value?.innerHTML || ''
  emit('update:modelValue', content)
}

const handleBlur = () => {
  emit('blur')
}

const handleFocus = () => {
  emit('focus')
}

watch(() => props.modelValue, (newValue) => {
  if (editorRef.value && editorRef.value.innerHTML !== newValue) {
    editorRef.value.innerHTML = newValue || ''
  }
}, { immediate: true })

// 设置占位符
watch(() => props.placeholder, () => {
  nextTick(() => {
    if (editorRef.value && !props.modelValue) {
      editorRef.value.setAttribute('data-placeholder', props.placeholder)
    }
  })
}, { immediate: true })
</script>

<style scoped>
.rich-text-editor {
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  overflow: hidden;
}

.toolbar {
  padding: 8px;
  border-bottom: 1px solid #d9d9d9;
  background-color: #fafafa;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.editor-content {
  min-height: 120px;
  padding: 12px;
  outline: none;
  line-height: 1.6;
}

.editor-content:empty:before {
  content: attr(data-placeholder);
  color: #c0c4cc;
  font-style: italic;
}

.editor-content:focus {
  box-shadow: none;
}

/* 编辑器内容样式 */
.editor-content :deep(p) {
  margin: 0 0 8px 0;
}

.editor-content :deep(ul),
.editor-content :deep(ol) {
  margin: 8px 0;
  padding-left: 20px;
}

.editor-content :deep(li) {
  margin: 4px 0;
}

.editor-content :deep(strong) {
  font-weight: bold;
}

.editor-content :deep(em) {
  font-style: italic;
}

.editor-content :deep(u) {
  text-decoration: underline;
}
</style>