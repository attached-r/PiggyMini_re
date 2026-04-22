<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { authApi } from '@/api/auth'

const props = defineProps({
  collapsed: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['toggle'])

const router = useRouter()
const route = useRoute()

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
        <div class="w-8 h-8 bg-slate-700 rounded-full flex items-center justify-center flex-shrink-0">
          <span class="text-sm font-medium text-slate-300">{{ username.charAt(0).toUpperCase() }}</span>
        </div>
        <div v-if="!collapsed" class="ml-3 flex-1 min-w-0">
          <p class="text-sm font-medium text-white truncate">{{ username }}</p>
        </div>
        <button 
          v-if="!collapsed"
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
  </aside>
</template>
