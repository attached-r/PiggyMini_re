<script setup>
import { ref, onMounted } from 'vue'

const toasts = ref([])
let toastId = 0

// 添加 Toast
const addToast = (options) => {
  const id = ++toastId
  const toast = {
    id,
    type: options.type || 'info',
    message: options.message,
    duration: options.duration || 3000
  }
  
  toasts.value.push(toast)
  
  // 自动移除
  if (toast.duration > 0) {
    setTimeout(() => {
      removeToast(id)
    }, toast.duration)
  }
  
  return id
}

// 移除 Toast
const removeToast = (id) => {
  const index = toasts.value.findIndex(t => t.id === id)
  if (index > -1) {
    toasts.value.splice(index, 1)
  }
}

// 便捷方法
const success = (message, duration) => addToast({ type: 'success', message, duration })
const error = (message, duration) => addToast({ type: 'error', message, duration })
const warning = (message, duration) => addToast({ type: 'warning', message, duration })
const info = (message, duration) => addToast({ type: 'info', message, duration })

// 图标
const icons = {
  success: '✓',
  error: '✕',
  warning: '⚠',
  info: '●'
}

defineExpose({ addToast, removeToast, success, error, warning, info })
</script>

<template>
  <Teleport to="body">
    <div class="fixed top-4 right-4 z-[100] flex flex-col gap-2 pointer-events-none">
      <TransitionGroup name="toast">
        <div 
          v-for="toast in toasts" 
          :key="toast.id"
          class="pointer-events-auto flex items-center gap-3 px-4 py-3 rounded-xl shadow-lg backdrop-blur-md border min-w-[280px] max-w-sm"
          :class="[
            toast.type === 'success' ? 'bg-emerald-500/90 border-emerald-400/50' : '',
            toast.type === 'error' ? 'bg-red-500/90 border-red-400/50' : '',
            toast.type === 'warning' ? 'bg-amber-500/90 border-amber-400/50' : '',
            toast.type === 'info' ? 'bg-indigo-500/90 border-indigo-400/50' : ''
          ]"
        >
          <!-- 图标 -->
          <span 
            class="flex-shrink-0 w-6 h-6 rounded-full flex items-center justify-center text-sm font-bold text-white"
            :class="[
              toast.type === 'success' ? 'bg-emerald-600' : '',
              toast.type === 'error' ? 'bg-red-600' : '',
              toast.type === 'warning' ? 'bg-amber-600' : '',
              toast.type === 'info' ? 'bg-indigo-600' : ''
            ]"
          >
            {{ icons[toast.type] }}
          </span>
          
          <!-- 消息 -->
          <p class="flex-1 text-white text-sm font-medium leading-tight">
            {{ toast.message }}
          </p>
          
          <!-- 关闭按钮 -->
          <button 
            @click="removeToast(toast.id)"
            class="flex-shrink-0 w-5 h-5 rounded hover:bg-white/20 flex items-center justify-center text-white/70 hover:text-white transition-colors"
          >
            ✕
          </button>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<style scoped>
.toast-enter-active {
  animation: toastIn 0.3s ease-out;
}

.toast-leave-active {
  animation: toastOut 0.2s ease-in;
}

@keyframes toastIn {
  from {
    opacity: 0;
    transform: translateX(100%);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes toastOut {
  from {
    opacity: 1;
    transform: translateX(0);
  }
  to {
    opacity: 0;
    transform: translateX(100%);
  }
}
</style>
