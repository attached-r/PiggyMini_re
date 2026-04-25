<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  type: {
    type: String,
    default: 'info' // success | error | warning | info
  },
  title: {
    type: String,
    default: ''
  },
  message: {
    type: String,
    default: ''
  },
  confirmText: {
    type: String,
    default: '确定'
  },
  cancelText: {
    type: String,
    default: '取消'
  },
  showCancel: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close', 'confirm', 'cancel'])

// 图标映射
const icons = {
  success: '✅',
  error: '❌',
  warning: '⚠️',
  info: '💡'
}

// 样式映射
const styles = {
  success: {
    bg: 'from-emerald-500/20 to-emerald-600/10',
    border: 'border-emerald-500/50',
    icon: 'text-emerald-400'
  },
  error: {
    bg: 'from-red-500/20 to-red-600/10',
    border: 'border-red-500/50',
    icon: 'text-red-400'
  },
  warning: {
    bg: 'from-amber-500/20 to-amber-600/10',
    border: 'border-amber-500/50',
    icon: 'text-amber-400'
  },
  info: {
    bg: 'from-indigo-500/20 to-indigo-600/10',
    border: 'border-indigo-500/50',
    icon: 'text-indigo-400'
  }
}

const currentStyle = ref(styles[props.type] || styles.info)

watch(() => props.type, (newType) => {
  currentStyle.value = styles[newType] || styles.info
}, { immediate: true })

const handleClose = () => {
  emit('close')
}

const handleConfirm = () => {
  emit('confirm')
}

const handleCancel = () => {
  emit('cancel')
}
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div 
        v-if="show" 
        class="fixed inset-0 z-50 flex items-center justify-center p-4"
        @click.self="handleClose"
      >
        <!-- 遮罩层 -->
        <div class="absolute inset-0 bg-black/60 backdrop-blur-sm"></div>
        
        <!-- 弹窗内容 -->
        <div 
          class="relative w-full max-w-md bg-gradient-to-br from-slate-800 to-slate-900 rounded-2xl shadow-2xl border border-slate-700/50 overflow-hidden animate-modal-in"
        >
          <!-- 顶部装饰条 -->
          <div 
            class="h-1.5 bg-gradient-to-r"
            :class="[
              type === 'success' ? 'from-emerald-400 to-emerald-600' : '',
              type === 'error' ? 'from-red-400 to-red-600' : '',
              type === 'warning' ? 'from-amber-400 to-amber-600' : '',
              type === 'info' ? 'from-indigo-400 to-indigo-600' : ''
            ]"
          ></div>
          
          <div class="p-6">
            <!-- 关闭按钮 -->
            <button 
              @click="handleClose"
              class="absolute top-4 right-4 w-8 h-8 rounded-full bg-slate-700/50 hover:bg-slate-600/50 flex items-center justify-center text-slate-400 hover:text-white transition-colors"
            >
              ✕
            </button>
            
            <!-- 内容区 -->
            <div class="flex items-start gap-4">
              <!-- 图标 -->
              <div 
                class="flex-shrink-0 w-12 h-12 rounded-xl flex items-center justify-center text-2xl"
                :class="[
                  type === 'success' ? 'bg-emerald-500/20' : '',
                  type === 'error' ? 'bg-red-500/20' : '',
                  type === 'warning' ? 'bg-amber-500/20' : '',
                  type === 'info' ? 'bg-indigo-500/20' : ''
                ]"
              >
                {{ icons[type] }}
              </div>
              
              <!-- 文字内容 -->
              <div class="flex-1 min-w-0">
                <h3 v-if="title" class="text-lg font-semibold text-white mb-2">
                  {{ title }}
                </h3>
                <p class="text-slate-300 leading-relaxed whitespace-pre-line" :class="title ? '' : 'pt-1'">
                  {{ message }}
                </p>
              </div>
            </div>
            
            <!-- 按钮区 -->
            <div class="flex items-center justify-end gap-3 mt-6">
              <button 
                v-if="showCancel"
                @click="handleCancel"
                class="px-5 py-2.5 rounded-xl bg-slate-700/50 hover:bg-slate-600/50 text-slate-300 hover:text-white font-medium transition-all border border-slate-600/50 hover:border-slate-500/50"
              >
                {{ cancelText }}
              </button>
              <button 
                @click="handleConfirm"
                class="px-5 py-2.5 rounded-xl font-medium transition-all"
                :class="[
                  type === 'success' ? 'bg-emerald-500 hover:bg-emerald-600 text-white' : '',
                  type === 'error' ? 'bg-red-500 hover:bg-red-600 text-white' : '',
                  type === 'warning' ? 'bg-amber-500 hover:bg-amber-600 text-white' : '',
                  type === 'info' ? 'bg-indigo-500 hover:bg-indigo-600 text-white' : ''
                ]"
              >
                {{ confirmText }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
/* 弹窗动画 */
.modal-enter-active,
.modal-leave-active {
  transition: all 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .animate-modal-in,
.modal-leave-to .animate-modal-in {
  transform: scale(0.9) translateY(20px);
  opacity: 0;
}

.animate-modal-in {
  animation: modalIn 0.3s ease-out forwards;
}

@keyframes modalIn {
  from {
    transform: scale(0.95) translateY(10px);
    opacity: 0;
  }
  to {
    transform: scale(1) translateY(0);
    opacity: 1;
  }
}
</style>
