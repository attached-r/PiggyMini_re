<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/auth'

const router = useRouter()
const user = ref(null)
const sidebarOpen = ref(true)
const currentMenu = ref('dashboard')

const menuItems = [
  { id: 'dashboard', name: '控制台', icon: 'M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6' },
  { id: 'users', name: '用户管理', icon: 'M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z' },
  { id: 'projects', name: '项目管理', icon: 'M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z' },
  { id: 'settings', name: '系统设置', icon: 'M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z' },
  { id: 'settings', name: '个人设置', icon: 'M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z' }
]

const stats = ref([
  { title: '总用户数', value: '1,234', change: '+12%', icon: 'M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z', color: 'bg-blue-500' },
  { title: '活跃项目', value: '56', change: '+8%', icon: 'M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z', color: 'bg-green-500' },
  { title: '今日访问', value: '892', change: '+23%', icon: 'M15 12a3 3 0 11-6 0 3 3 0 016 0z', color: 'bg-purple-500' },
  { title: '系统消息', value: '12', change: '-5%', icon: 'M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9', color: 'bg-orange-500' }
])

const recentActivities = ref([
  { id: 1, user: '张三', action: '创建了项目', target: '电商平台重构', time: '5 分钟前', avatar: 'M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z' },
  { id: 2, user: '李四', action: '更新了', target: '用户管理模块', time: '15 分钟前', avatar: 'M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z' },
  { id: 3, user: '王五', action: '删除了', target: '测试数据', time: '1 小时前', avatar: 'M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z' },
  { id: 4, user: '赵六', action: '提交了', target: '代码审查申请', time: '2 小时前', avatar: 'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z' }
])

onMounted(() => {
  // 检查是否是管理员
  if (!authApi.isAdmin()) {
    // 如果不是管理员，跳转到登录页
    router.replace('/login')
    return
  }
  
  const token = localStorage.getItem('token')
  if (token) {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]))
      user.value = {
        username: payload.username || payload.sub || localStorage.getItem('username') || '管理员',
        email: payload.email || 'admin@piggy.com',
        role: 'admin'
      }
    } catch (e) {
      user.value = {
        username: localStorage.getItem('username') || '管理员',
        email: 'admin@piggy.com',
        role: 'admin'
      }
    }
  }
})

const handleLogout = () => {
  authApi.logout()
  router.push('/login')
}

const toggleSidebar = () => {
  sidebarOpen.value = !sidebarOpen.value
}

const selectMenu = (menuId) => {
  currentMenu.value = menuId
}
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Sidebar -->
    <aside 
      :class="[
        'fixed top-0 left-0 z-40 h-screen transition-transform bg-white border-r border-gray-200',
        sidebarOpen ? 'translate-x-0 w-64' : '-translate-x-full w-0'
      ]"
    >
      <div class="h-full px-3 py-4 overflow-y-auto">
        <!-- Logo -->
        <div class="mb-6 px-3 pt-2">
          <div class="flex items-center space-x-3">
            <div class="w-10 h-10 bg-primary-600 rounded-lg flex items-center justify-center flex-shrink-0">
              <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
              </svg>
            </div>
            <div v-show="sidebarOpen">
              <span class="text-xl font-bold text-gray-900 block">Piggy</span>
              <span class="text-xs text-green-600 font-medium block -mt-1">管理后台</span>
            </div>
          </div>
        </div>

        <!-- Menu -->
        <ul class="space-y-2">
          <li v-for="item in menuItems" :key="item.id">
            <button
              @click="selectMenu(item.id)"
              :class="[
                'flex items-center w-full px-3 py-2.5 rounded-lg transition-colors',
                currentMenu === item.id
                  ? 'bg-primary-50 text-primary-600'
                  : 'text-gray-700 hover:bg-gray-100'
              ]"
            >
              <svg class="w-5 h-5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="item.icon"></path>
              </svg>
              <span class="ml-3" v-show="sidebarOpen">{{ item.name }}</span>
            </button>
          </li>
        </ul>

        <!-- Sidebar Footer -->
        <div class="absolute bottom-0 left-0 right-0 p-3 border-t border-gray-200" v-show="sidebarOpen">
          <div class="flex items-center space-x-3">
            <div class="w-8 h-8 bg-primary-100 rounded-full flex items-center justify-center">
              <svg class="w-5 h-5 text-primary-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd"/>
              </svg>
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-medium text-gray-900 truncate">{{ user?.username || '用户' }}</p>
              <p class="text-xs text-gray-500 truncate">{{ user?.email || 'user@example.com' }}</p>
            </div>
          </div>
        </div>
      </div>
    </aside>

    <!-- Main Content -->
    <div :class="['transition-all duration-300', sidebarOpen ? 'ml-64' : 'ml-0']">
      <!-- Header -->
      <header class="bg-white border-b border-gray-200 sticky top-0 z-30">
        <div class="flex items-center justify-between px-6 py-4">
          <div class="flex items-center space-x-4">
            <button
              @click="toggleSidebar"
              class="p-2 rounded-lg hover:bg-gray-100 transition-colors"
            >
              <svg class="w-6 h-6 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"></path>
              </svg>
            </button>
            <h1 class="text-2xl font-bold text-gray-900">控制台</h1>
          </div>
          
          <div class="flex items-center space-x-4">
            <!-- Notifications -->
            <button class="relative p-2 rounded-lg hover:bg-gray-100 transition-colors">
              <svg class="w-6 h-6 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"></path>
              </svg>
              <span class="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full"></span>
            </button>
            
            <!-- User Menu -->
            <div class="flex items-center space-x-3 pl-4 border-l border-gray-200">
              <div class="w-9 h-9 bg-primary-100 rounded-full flex items-center justify-center">
                <svg class="w-5 h-5 text-primary-600" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd"/>
                </svg>
              </div>
              <div class="hidden md:block">
              <p class="text-sm font-medium text-gray-900">{{ user?.username || '管理员' }}</p>
              <p class="text-xs text-green-600 font-medium">超级管理员</p>
            </div>
              <button
                @click="handleLogout"
                class="ml-2 px-4 py-2 text-sm text-red-600 hover:bg-red-50 rounded-lg transition-colors flex items-center space-x-2"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path>
                </svg>
                <span>退出</span>
              </button>
            </div>
          </div>
        </div>
      </header>

      <!-- Page Content -->
      <main class="p-6">
        <!-- Stats Cards -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-6">
          <div v-for="(stat, index) in stats" :key="index" class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-gray-600 mb-1">{{ stat.title }}</p>
                <p class="text-2xl font-bold text-gray-900">{{ stat.value }}</p>
                <p :class="stat.change.startsWith('+') ? 'text-green-600' : 'text-red-600'" class="text-sm mt-1">
                  {{ stat.change }} 较上月
                </p>
              </div>
              <div :class="[stat.color, 'w-12 h-12 rounded-lg flex items-center justify-center']">
                <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="stat.icon"></path>
                </svg>
              </div>
            </div>
          </div>
        </div>

        <!-- Content Grid -->
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <!-- Recent Activity -->
          <div class="lg:col-span-2 bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <h2 class="text-lg font-semibold text-gray-900 mb-4">最近动态</h2>
            <div class="space-y-4">
              <div v-for="activity in recentActivities" :key="activity.id" class="flex items-start space-x-3 pb-4 border-b border-gray-100 last:border-0 last:pb-0">
                <div class="w-10 h-10 bg-gray-100 rounded-full flex items-center justify-center flex-shrink-0">
                  <svg class="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="activity.avatar"></path>
                  </svg>
                </div>
                <div class="flex-1 min-w-0">
                  <p class="text-sm text-gray-900">
                    <span class="font-medium">{{ activity.user }}</span>
                    <span class="text-gray-600"> {{ activity.action }} </span>
                    <span class="font-medium text-primary-600">{{ activity.target }}</span>
                  </p>
                  <p class="text-xs text-gray-500 mt-1">{{ activity.time }}</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Quick Actions -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <h2 class="text-lg font-semibold text-gray-900 mb-4">快捷操作</h2>
            <div class="space-y-3">
              <button class="w-full flex items-center justify-between p-3 rounded-lg border border-gray-200 hover:border-primary-500 hover:bg-primary-50 transition-colors">
                <div class="flex items-center space-x-3">
                  <div class="w-8 h-8 bg-blue-100 rounded-lg flex items-center justify-center">
                    <svg class="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path>
                    </svg>
                  </div>
                  <span class="text-sm font-medium text-gray-900">新建项目</span>
                </div>
                <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
                </svg>
              </button>
              
              <button class="w-full flex items-center justify-between p-3 rounded-lg border border-gray-200 hover:border-primary-500 hover:bg-primary-50 transition-colors">
                <div class="flex items-center space-x-3">
                  <div class="w-8 h-8 bg-green-100 rounded-lg flex items-center justify-center">
                    <svg class="w-5 h-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"></path>
                    </svg>
                  </div>
                  <span class="text-sm font-medium text-gray-900">添加用户</span>
                </div>
                <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
                </svg>
              </button>
              
              <button class="w-full flex items-center justify-between p-3 rounded-lg border border-gray-200 hover:border-primary-500 hover:bg-primary-50 transition-colors">
                <div class="flex items-center space-x-3">
                  <div class="w-8 h-8 bg-purple-100 rounded-lg flex items-center justify-center">
                    <svg class="w-5 h-5 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                    </svg>
                  </div>
                  <span class="text-sm font-medium text-gray-900">查看报表</span>
                </div>
                <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
                </svg>
              </button>
              
              <button class="w-full flex items-center justify-between p-3 rounded-lg border border-gray-200 hover:border-primary-500 hover:bg-primary-50 transition-colors">
                <div class="flex items-center space-x-3">
                  <div class="w-8 h-8 bg-orange-100 rounded-lg flex items-center justify-center">
                    <svg class="w-5 h-5 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"></path>
                    </svg>
                  </div>
                  <span class="text-sm font-medium text-gray-900">系统设置</span>
                </div>
                <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
                </svg>
              </button>
            </div>

            <!-- System Status -->
            <div class="mt-6 pt-6 border-t border-gray-200">
              <h3 class="text-sm font-semibold text-gray-700 mb-3">系统状态</h3>
              <div class="space-y-2">
                <div class="flex items-center justify-between">
                  <span class="text-sm text-gray-600">服务器</span>
                  <span class="text-sm text-green-600 font-medium">正常运行</span>
                </div>
                <div class="flex items-center justify-between">
                  <span class="text-sm text-gray-600">数据库</span>
                  <span class="text-sm text-green-600 font-medium">连接正常</span>
                </div>
                <div class="flex items-center justify-between">
                  <span class="text-sm text-gray-600">API 网关</span>
                  <span class="text-sm text-green-600 font-medium">响应正常</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>
