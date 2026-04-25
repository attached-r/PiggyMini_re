<script setup>
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { authApi } from '@/api/auth'
import { ElMessage } from 'element-plus'

const props = defineProps({
  collapsed: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['toggle'])

const router = useRouter()
const route = useRoute()

// 头像上传相关
const showAvatarModal = ref(false)
const avatarFileInput = ref(null)
const previewAvatar = ref('')
const uploadingAvatar = ref(false)

// 导航菜单
const menuItems = [
  { path: '/dashboard', label: '首页概览', icon: '🏠' },
  { path: '/transactions', label: '交易记录', icon: '💸' },
  { path: '/budgets', label: '预算管理', icon: '📊' },
  { path: '/reports', label: '财务报表', icon: '📈' },
  { path: '/ai', label: 'AI服务', icon: '🤖' },
  { path: '/accounts', label: '账户管理', icon: '💳' }
]

const currentPath = computed(() => route.path)

const isActive = (path) => currentPath.value === path

const navigateTo = (path) => {
  router.push(path)
}

const handleLogout = () => {
  authApi.logout()
  router.push('/login')
}

const username = computed(() => {
  return authApi.getNickname() || authApi.getUsername() || '用户'
})

// 头像相关方法
const userAvatar = computed(() => {
  return authApi.getAvatar() || ''
})

// 打开头像选择
const openAvatarModal = () => {
  previewAvatar.value = userAvatar.value
  showAvatarModal.value = true
}

// 触发文件选择
const triggerFileInput = () => {
  avatarFileInput.value?.click()
}

// 处理文件选择
const handleFileChange = (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }

  // 验证文件大小（最大2MB）
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过2MB')
    return
  }

  // 预览图片
  const reader = new FileReader()
  reader.onload = (e) => {
    previewAvatar.value = e.target.result
  }
  reader.readAsDataURL(file)
}

// 上传头像
const uploadAvatar = async () => {
  if (!previewAvatar.value) {
    ElMessage.error('请先选择头像图片')
    return
  }

  uploadingAvatar.value = true
  try {
    await authApi.updateAvatar(previewAvatar.value)
    ElMessage.success('头像更新成功')
    showAvatarModal.value = false
    // 触发页面刷新以更新头像显示
    window.location.reload()
  } catch (error) {
    ElMessage.error(error.message || '头像更新失败')
  } finally {
    uploadingAvatar.value = false
  }
}

// 关闭模态框
const closeAvatarModal = () => {
  showAvatarModal.value = false
  previewAvatar.value = userAvatar.value
}
</script>

<template>
  <aside 
    class="fixed left-0 top-0 h-full bg-gradient-to-b from-slate-900 to-slate-800 border-r border-slate-700/50 z-40 transition-all duration-300 flex flex-col"
    :class="collapsed ? 'w-16' : 'w-56'"
  >
    <!-- Logo 区域 -->
    <div class="h-16 flex items-center justify-center border-b border-slate-700/50 px-4">
      <div class="w-10 h-10 bg-indigo-500 rounded-xl flex items-center justify-center shadow-lg shadow-indigo-500/30 flex-shrink-0">
        <span class="font-black text-xl text-white">P</span>
      </div>
      <span v-if="!collapsed" class="ml-3 text-xl font-bold text-white">Piggy</span>
    </div>

    <!-- 导航菜单 -->
    <nav class="flex-1 py-4 overflow-y-auto">
      <ul class="space-y-1 px-2">
        <li v-for="item in menuItems" :key="item.path">
          <button
            @click="navigateTo(item.path)"
            class="w-full flex items-center px-3 py-3 rounded-lg transition-all duration-200"
            :class="[
              isActive(item.path)
                ? 'bg-indigo-500/20 text-indigo-400 border-l-2 border-indigo-500'
                : 'text-slate-300 hover:bg-slate-700/50 hover:text-white border-l-2 border-transparent'
            ]"
          >
            <span class="text-xl flex-shrink-0 w-8 flex justify-center">{{ item.icon }}</span>
            <span v-if="!collapsed" class="ml-3 font-medium text-sm">{{ item.label }}</span>
          </button>
        </li>
      </ul>
    </nav>

    <!-- 折叠按钮 -->
    <button
      @click="$emit('toggle')"
      class="mx-2 mb-2 flex items-center justify-center px-3 py-2 rounded-lg text-slate-400 hover:text-white hover:bg-slate-700/50 transition-colors"
    >
      <svg 
        class="w-5 h-5 transition-transform duration-300" 
        :class="collapsed ? 'rotate-180' : ''"
        fill="none" 
        stroke="currentColor" 
        viewBox="0 0 24 24"
      >
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 19l-7-7 7-7m8 14l-7-7 7-7"></path>
      </svg>
    </button>

    <!-- 用户信息 -->
    <div class="border-t border-slate-700/50 p-4">
      <div class="flex items-center" :class="collapsed ? 'justify-center' : ''">
        <!-- 头像 -->
        <div 
          @click="openAvatarModal"
          class="w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0 overflow-hidden bg-slate-700 cursor-pointer hover:ring-2 hover:ring-indigo-500 transition-all"
          :class="collapsed ? '' : ''"
          :title="collapsed ? '点击更换头像' : ''"
        >
          <img 
            v-if="userAvatar" 
            :src="userAvatar" 
            alt="用户头像" 
            class="w-full h-full object-cover"
          />
          <span v-else class="text-sm font-medium text-slate-300">{{ username.charAt(0).toUpperCase() }}</span>
        </div>
        <div v-if="!collapsed" class="ml-3 flex-1 min-w-0 flex items-center">
          <div class="flex-1 min-w-0">
            <p class="text-sm font-medium text-white truncate">{{ username }}</p>
            <p class="text-xs text-slate-400 truncate cursor-pointer hover:text-indigo-400" @click="openAvatarModal">更换头像</p>
          </div>
          <button 
            @click="handleLogout"
            class="ml-2 text-slate-400 hover:text-red-400 transition-colors"
            title="退出登录"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path>
            </svg>
          </button>
        </div>
      </div>
    </div>
  </aside>

  <!-- 头像上传模态框 -->
  <div v-if="showAvatarModal" class="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50">
    <div class="bg-slate-800 border border-slate-700 rounded-2xl w-full max-w-sm p-6">
      <div class="flex items-center justify-between mb-6">
        <h3 class="text-xl font-bold text-white">更换头像</h3>
        <button @click="closeAvatarModal" class="text-slate-400 hover:text-white transition-colors">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
          </svg>
        </button>
      </div>

      <!-- 头像预览 -->
      <div class="flex flex-col items-center mb-6">
        <div class="w-24 h-24 rounded-full overflow-hidden bg-slate-700 mb-4 ring-4 ring-slate-600">
          <img 
            v-if="previewAvatar" 
            :src="previewAvatar" 
            alt="头像预览" 
            class="w-full h-full object-cover"
          />
          <div v-else class="w-full h-full flex items-center justify-center">
            <span class="text-3xl font-medium text-slate-300">{{ username.charAt(0).toUpperCase() }}</span>
          </div>
        </div>
        <p class="text-sm text-slate-400">支持 JPG、PNG 图片，最大 2MB</p>
      </div>

      <!-- 文件选择 -->
      <input 
        ref="avatarFileInput"
        type="file"
        accept="image/*"
        @change="handleFileChange"
        class="hidden"
      />
      
      <div class="flex justify-center space-x-3">
        <button
          @click="triggerFileInput"
          class="px-4 py-2 bg-slate-700 hover:bg-slate-600 text-slate-300 rounded-lg transition-colors flex items-center space-x-2"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
          </svg>
          <span>选择图片</span>
        </button>
        <button
          @click="uploadAvatar"
          :disabled="uploadingAvatar || !previewAvatar"
          class="px-6 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center space-x-2"
        >
          <span>{{ uploadingAvatar ? '上传中...' : '保存' }}</span>
        </button>
      </div>
    </div>
  </div>
</template>
