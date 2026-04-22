<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/auth'

const router = useRouter()
const user = ref(null)
const activeTab = ref('profile')
const showProfileDropdown = ref(false)

// 用户账户信息
const accountInfo = ref({
  username: '',
  email: '',
  createdAt: '',
  lastLogin: ''
})

onMounted(() => {
  // 检查是否已登录且是普通用户
  if (!authApi.isAuthenticated()) {
    router.replace('/login')
    return
  }
  
  // 如果是管理员，跳转到管理后台
  if (authApi.isAdmin()) {
    router.replace('/home')
    return
  }
  
  loadUserInfo()
})

const loadUserInfo = () => {
  user.value = {
    username: authApi.getUsername() || '用户',
    nickname: authApi.getNickname() || ''
  }
  
  accountInfo.value = {
    username: user.value.username,
    email: user.value.nickname ? `${user.value.nickname}@example.com` : 'user@example.com',
    createdAt: new Date().toLocaleDateString('zh-CN'),
    lastLogin: new Date().toLocaleString('zh-CN')
  }
}

const handleLogout = () => {
  authApi.logout()
  router.push('/login')
}

const toggleProfileDropdown = () => {
  showProfileDropdown.value = !showProfileDropdown.value
}

// 跳转到各功能页面
const goToDashboard = () => {
  router.push('/dashboard')
}

const goToServices = () => {
  router.push('/services')
}

const goToAccounts = () => {
  router.push('/accounts')
}
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Header -->
    <header class="bg-white border-b border-gray-200 sticky top-0 z-30">
      <div class="flex items-center justify-between px-6 py-4">
        <div class="flex items-center space-x-3">
          <div class="w-10 h-10 bg-indigo-600 rounded-lg flex items-center justify-center">
            <span class="text-xl font-bold text-white">P</span>
          </div>
          <div>
            <span class="text-xl font-bold text-gray-900 block">Piggy</span>
            <span class="text-xs text-indigo-600 font-medium block -mt-1">用户中心</span>
          </div>
        </div>
        
        <div class="flex items-center space-x-4">
          <button
            @click="goToDashboard"
            class="flex items-center space-x-2 px-4 py-2 text-gray-600 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition-colors"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"></path>
            </svg>
            <span class="text-sm font-medium">首页</span>
          </button>
          
          <!-- Profile Dropdown -->
          <div class="relative" @click.stop>
            <button
              @click.stop="toggleProfileDropdown"
              class="flex items-center space-x-2 hover:bg-gray-50 rounded-lg px-3 py-2 transition-colors"
            >
              <div class="w-9 h-9 bg-indigo-100 rounded-full flex items-center justify-center">
                <svg class="w-5 h-5 text-indigo-600" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd"/>
                </svg>
              </div>
              <span class="text-gray-700 text-sm font-medium">{{ user?.username || '用户' }}</span>
            </button>
            
            <!-- Dropdown Menu -->
            <div
              v-show="showProfileDropdown"
              class="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-gray-200 py-1 z-50"
            >
              <button
                @click="goToServices"
                class="w-full flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-indigo-50"
              >
                <svg class="w-4 h-4 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z"></path>
                </svg>
                核心服务
              </button>
              <button
                @click="goToAccounts"
                class="w-full flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-indigo-50"
              >
                <svg class="w-4 h-4 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z"></path>
                </svg>
                账户管理
              </button>
              <div class="border-t border-gray-200 my-1"></div>
              <button
                @click="handleLogout"
                class="w-full flex items-center px-4 py-2 text-sm text-red-600 hover:bg-red-50"
              >
                <svg class="w-4 h-4 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path>
                </svg>
                退出登录
              </button>
            </div>
          </div>
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <main class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Profile Card -->
      <div class="bg-white rounded-2xl shadow-sm border border-gray-200 p-8 mb-6">
        <div class="flex items-center mb-6">
          <div class="w-20 h-20 bg-gradient-to-br from-indigo-400 to-indigo-600 rounded-full flex items-center justify-center mr-6">
            <svg class="w-10 h-10 text-white" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd"/>
            </svg>
          </div>
          <div>
            <h2 class="text-2xl font-bold text-gray-900">{{ accountInfo.username }}</h2>
            <p class="text-gray-500 mt-1">个人用户</p>
          </div>
        </div>
        
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label class="block text-sm font-medium text-gray-500 mb-1">用户名</label>
            <div class="px-4 py-3 bg-gray-50 rounded-lg border border-gray-200">
              {{ accountInfo.username }}
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-500 mb-1">注册时间</label>
            <div class="px-4 py-3 bg-gray-50 rounded-lg border border-gray-200">
              {{ accountInfo.createdAt }}
            </div>
          </div>
        </div>
      </div>

      <!-- Quick Actions -->
      <div class="bg-white rounded-2xl shadow-sm border border-gray-200 p-6">
        <h3 class="text-lg font-semibold text-gray-900 mb-4">快捷操作</h3>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <button
            @click="goToDashboard"
            class="flex items-center p-4 bg-indigo-50 rounded-xl hover:bg-indigo-100 transition-colors"
          >
            <div class="w-12 h-12 bg-indigo-500 rounded-lg flex items-center justify-center mr-4">
              <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"></path>
              </svg>
            </div>
            <div class="text-left">
              <h4 class="font-medium text-gray-900">返回首页</h4>
              <p class="text-sm text-gray-500">查看财务概览</p>
            </div>
          </button>
          
          <button
            @click="goToServices"
            class="flex items-center p-4 bg-emerald-50 rounded-xl hover:bg-emerald-100 transition-colors"
          >
            <div class="w-12 h-12 bg-emerald-500 rounded-lg flex items-center justify-center mr-4">
              <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z"></path>
              </svg>
            </div>
            <div class="text-left">
              <h4 class="font-medium text-gray-900">核心服务</h4>
              <p class="text-sm text-gray-500">使用各种功能</p>
            </div>
          </button>
          
          <button
            @click="goToAccounts"
            class="flex items-center p-4 bg-purple-50 rounded-xl hover:bg-purple-100 transition-colors"
          >
            <div class="w-12 h-12 bg-purple-500 rounded-lg flex items-center justify-center mr-4">
              <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z"></path>
              </svg>
            </div>
            <div class="text-left">
              <h4 class="font-medium text-gray-900">账户管理</h4>
              <p class="text-sm text-gray-500">管理您的账户</p>
            </div>
          </button>
        </div>
      </div>
    </main>
  </div>
</template>
