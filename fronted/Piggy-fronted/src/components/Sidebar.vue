<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { authApi } from '@/api/auth'
import { ElMessage } from 'element-plus'
import { useTheme, getThemeList } from '@/composables/useTheme'

const props = defineProps({
  collapsed: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['toggle'])

const router = useRouter()
const route = useRoute()

// 主题相关
const { currentTheme, setTheme } = useTheme()
const themeList = getThemeList()
const showThemePicker = ref(false)

// 头像上传相关
const showAvatarModal = ref(false)
const avatarFileInput = ref(null)
const previewAvatar = ref('')
const uploadingAvatar = ref(false)
// 用于触发头像更新
const avatarRefreshKey = ref(0)

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
  // 引用 avatarRefreshKey 以触发更新
  void avatarRefreshKey.value
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
    const result = await authApi.updateAvatar(previewAvatar.value)
    ElMessage.success('头像更新成功')
    showAvatarModal.value = false
    // 触发头像刷新（通过改变 key 强制重新渲染）
    avatarRefreshKey.value++
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

// 主题切换
const selectTheme = (themeId) => {
  setTheme(themeId)
  showThemePicker.value = false
}

// 获取当前主题信息
const currentThemeInfo = computed(() => {
  return themeList.find(t => t.id === currentTheme.value) || themeList[0]
})

// 侧边栏样式类 - 根据主题变化
const sidebarClasses = computed(() => {
  const isDark = ['dark', 'black', 'pixel', 'nietzsche'].includes(currentTheme.value)
  return {
    container: isDark 
      ? 'bg-gradient-to-b from-slate-900 to-slate-800 border-slate-700/50' 
      : 'bg-white border-slate-200 shadow-md',
    logo: isDark ? 'text-white' : 'text-slate-800',
    navButton: isDark ? 'text-slate-300 hover:text-white' : 'text-slate-600 hover:text-slate-900',
    navActive: isDark ? 'bg-indigo-500/20 text-indigo-400' : 'bg-indigo-50 text-indigo-600',
    border: isDark ? 'border-slate-700/50' : 'border-slate-200',
    muted: isDark ? 'text-slate-400' : 'text-slate-500',
  }
})

// 点击其他地方关闭主题选择器
const handleClickOutside = (event) => {
  const sidebar = document.querySelector('aside')
  if (sidebar && !sidebar.contains(event.target)) {
    showThemePicker.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <aside 
    class="fixed left-0 top-0 h-full border-r z-40 transition-all duration-300 flex flex-col"
    :class="[sidebarClasses.container, collapsed ? 'w-16' : 'w-56']"
  >
    <!-- Logo 区域 -->
    <div class="h-16 flex items-center justify-center border-b px-4" :class="sidebarClasses.border">
      <div class="w-10 h-10 bg-indigo-500 rounded-xl flex items-center justify-center shadow-lg shadow-indigo-500/30 flex-shrink-0">
        <span class="font-black text-xl text-white">P</span>
      </div>
      <span v-if="!collapsed" class="ml-3 text-xl font-bold" :class="sidebarClasses.logo">Piggy</span>
    </div>

    <!-- 导航菜单 -->
    <nav class="flex-1 py-4 overflow-y-auto">
      <ul class="space-y-1 px-2">
        <li v-for="item in menuItems" :key="item.path">
          <button
            @click="navigateTo(item.path)"
            class="w-full flex items-center px-3 py-3 rounded-lg transition-all duration-200 border-l-2"
            :class="[
              isActive(item.path)
                ? [sidebarClasses.navActive, 'border-indigo-500']
                : [sidebarClasses.navButton, 'hover:border-slate-400 border-transparent']
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
      class="mx-2 mb-2 flex items-center justify-center px-3 py-2 rounded-lg transition-colors"
      :class="sidebarClasses.muted + ' hover:bg-gray-100 dark:hover:bg-slate-700'"
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

    <!-- 主题切换 -->
    <div class="px-2 mb-2 relative">
      <!-- 收起时：单图标 + 下拉菜单 -->
      <div v-if="collapsed" class="relative">
        <button
          @click="showThemePicker = !showThemePicker"
          class="w-full flex items-center justify-center px-3 py-2 rounded-lg transition-colors"
          :class="showThemePicker 
            ? 'bg-gray-200 dark:bg-slate-700' 
            : sidebarClasses.muted + ' hover:bg-gray-100 dark:hover:bg-slate-700'"
          :title="currentThemeInfo.name"
        >
          <span class="text-lg">{{ currentThemeInfo.icon }}</span>
        </button>
        
        <!-- 收起时的下拉菜单 -->
        <div 
          v-if="showThemePicker"
          class="absolute bottom-full left-full mb-2 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg shadow-xl overflow-hidden z-50 min-w-[140px]"
          @click.stop
        >
          <div class="p-2 border-b border-slate-200 dark:border-slate-700">
            <span class="text-xs text-slate-500 dark:text-slate-400">选择主题</span>
          </div>
          <div 
            v-for="theme in themeList" 
            :key="theme.id"
            @click="selectTheme(theme.id)"
            class="flex items-center px-3 py-2 cursor-pointer transition-colors"
            :class="currentTheme === theme.id 
              ? 'bg-indigo-50 dark:bg-indigo-500/20 text-indigo-600 dark:text-indigo-400' 
              : 'text-slate-600 dark:text-slate-300 hover:bg-gray-50 dark:hover:bg-slate-700'"
          >
            <span class="text-lg">{{ theme.icon }}</span>
            <span class="ml-2 text-sm flex-1">{{ theme.name }}</span>
            <span v-if="currentTheme === theme.id" class="text-indigo-500 dark:text-indigo-400">✓</span>
          </div>
        </div>
      </div>
      
      <!-- 展开时：主题选择器 -->
      <button
        v-else
        @click="showThemePicker = !showThemePicker"
        class="w-full flex items-center px-3 py-2 rounded-lg transition-colors"
        :class="sidebarClasses.muted + ' hover:bg-gray-100 dark:hover:bg-slate-700'"
      >
        <span class="text-lg">{{ currentThemeInfo.icon }}</span>
        <span class="ml-3 text-sm">{{ currentThemeInfo.name }}</span>
        <svg 
          class="w-4 h-4 ml-auto transition-transform duration-200" 
          :class="showThemePicker ? 'rotate-180' : ''"
          fill="none" 
          stroke="currentColor" 
          viewBox="0 0 24 24"
        >
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
        </svg>
      </button>

      <!-- 主题列表下拉 -->
      <div 
        v-if="showThemePicker && !collapsed"
        class="absolute bottom-full left-0 right-0 mb-2 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg shadow-xl overflow-hidden z-50"
      >
        <div 
          v-for="theme in themeList" 
          :key="theme.id"
          @click="selectTheme(theme.id)"
          class="flex items-center px-3 py-2 cursor-pointer transition-colors"
          :class="currentTheme === theme.id 
            ? 'bg-indigo-50 dark:bg-indigo-500/20 text-indigo-600 dark:text-indigo-400' 
            : 'text-slate-600 dark:text-slate-300 hover:bg-gray-50 dark:hover:bg-slate-700'"
        >
          <span class="text-lg">{{ theme.icon }}</span>
          <span class="ml-3 text-sm flex-1">{{ theme.name }}</span>
          <span v-if="currentTheme === theme.id" class="text-indigo-500 dark:text-indigo-400">✓</span>
        </div>
      </div>
    </div>

    <!-- 用户信息 -->
    <div class="border-t p-4" :class="sidebarClasses.border">
      <div class="flex items-center" :class="collapsed ? 'justify-center' : ''">
        <!-- 头像 -->
        <div 
          @click="openAvatarModal"
          class="w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0 overflow-hidden bg-slate-200 dark:bg-slate-700 cursor-pointer hover:ring-2 hover:ring-indigo-500 transition-all"
          :title="collapsed ? '点击更换头像' : ''"
        >
          <img 
            v-if="userAvatar" 
            :src="userAvatar" 
            alt="用户头像" 
            class="w-full h-full object-cover"
          />
          <span v-else class="text-sm font-medium text-slate-600 dark:text-slate-300">{{ username.charAt(0).toUpperCase() }}</span>
        </div>
        <div v-if="!collapsed" class="ml-3 flex-1 min-w-0 flex items-center">
          <div class="flex-1 min-w-0">
            <p class="text-sm font-medium truncate" :class="sidebarClasses.logo">{{ username }}</p>
            <p class="text-xs truncate cursor-pointer hover:text-indigo-500 transition-colors" :class="sidebarClasses.muted" @click="openAvatarModal">更换头像</p>
          </div>
          <button 
            @click="handleLogout"
            class="ml-2 transition-colors"
            :class="sidebarClasses.muted + ' hover:text-red-500'"
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
    <div class="bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-2xl w-full max-w-sm p-6">
      <div class="flex items-center justify-between mb-6">
        <h3 class="text-xl font-bold text-slate-800 dark:text-white">更换头像</h3>
        <button @click="closeAvatarModal" class="text-slate-400 hover:text-slate-600 dark:hover:text-white transition-colors">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
          </svg>
        </button>
      </div>

      <!-- 头像预览 -->
      <div class="flex flex-col items-center mb-6">
        <div class="w-24 h-24 rounded-full overflow-hidden bg-slate-100 dark:bg-slate-700 mb-4 ring-4 ring-slate-200 dark:ring-slate-600">
          <img 
            v-if="previewAvatar" 
            :src="previewAvatar" 
            alt="头像预览" 
            class="w-full h-full object-cover"
          />
          <div v-else class="w-full h-full flex items-center justify-center">
            <span class="text-3xl font-medium text-slate-400 dark:text-slate-300">{{ username.charAt(0).toUpperCase() }}</span>
          </div>
        </div>
        <p class="text-sm text-slate-500 dark:text-slate-400">支持 JPG、PNG 图片，最大 2MB</p>
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
          class="px-4 py-2 bg-slate-100 dark:bg-slate-700 hover:bg-slate-200 dark:hover:bg-slate-600 text-slate-700 dark:text-slate-300 rounded-lg transition-colors flex items-center space-x-2"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
          </svg>
          <span>选择图片</span>
        </button>
        <button
          @click="uploadAvatar"
          :disabled="uploadingAvatar || !previewAvatar"
          class="px-6 py-2 bg-indigo-500 hover:bg-indigo-600 text-white rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center space-x-2"
        >
          <span>{{ uploadingAvatar ? '上传中...' : '保存' }}</span>
        </button>
      </div>
    </div>
  </div>
</template>
