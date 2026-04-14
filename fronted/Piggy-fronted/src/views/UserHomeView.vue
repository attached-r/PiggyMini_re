<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/auth'

const router = useRouter()
const user = ref(null)
const activeTab = ref('profile')
const showProfileDropdown = ref(false)

// 点击外部关闭下拉菜单
const closeDropdown = () => {
  showProfileDropdown.value = false
}

// 用户账户信息
const accountInfo = ref({
  username: '',
  email: '',
  phone: '',
  createdAt: '',
  lastLogin: '',
  status: 'active'
})

// 用户统计数据
const stats = ref([
  { title: '个人资料完整度', value: '85%', icon: 'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z', color: 'bg-purple-500' }
])

// 修改密码表单
const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 修改邮箱表单
const emailForm = ref({
  newEmail: '',
  confirmEmail: ''
})

const isChangingPassword = ref(false)
const isChangingEmail = ref(false)

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
  
  // 添加全局点击事件监听器
  document.addEventListener('click', closeDropdown)
})

const loadUserInfo = () => {
  const token = localStorage.getItem('token')
  const username = localStorage.getItem('username')
  
  if (token) {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]))
      user.value = {
        username: payload.username || payload.sub || username || '用户',
        email: payload.email || 'user@example.com'
      }
    } catch (e) {
      user.value = {
        username: username || '用户',
        email: 'user@example.com'
      }
    }
  }
  
  // 加载账户信息（模拟数据）
  accountInfo.value = {
    username: user.value?.username || '用户',
    email: user.value?.email || 'user@example.com',
    phone: '138****8888',
    createdAt: '2024-01-01',
    lastLogin: new Date().toLocaleString('zh-CN'),
    status: 'active'
  }
}

const handleLogout = () => {
  authApi.logout()
  router.push('/login')
}

const toggleProfileDropdown = () => {
  showProfileDropdown.value = !showProfileDropdown.value
}

const goToProfile = () => {
  activeTab.value = 'profile'
  showProfileDropdown.value = false
}

const goToSecurity = () => {
  activeTab.value = 'security'
  showProfileDropdown.value = false
}

const handleChangePassword = () => {
  isChangingPassword.value = true
  // TODO: 调用后端 API 修改密码
  setTimeout(() => {
    alert('密码修改成功！')
    passwordForm.value = {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
    isChangingPassword.value = false
  }, 1000)
}

const handleChangeEmail = () => {
  if (emailForm.value.newEmail !== emailForm.value.confirmEmail) {
    alert('两次输入的邮箱不一致')
    return
  }
  isChangingEmail.value = true
  // TODO: 调用后端 API 修改邮箱
  setTimeout(() => {
    alert('邮箱修改成功！')
    accountInfo.value.email = emailForm.value.newEmail
    emailForm.value = {
      newEmail: '',
      confirmEmail: ''
    }
    isChangingEmail.value = false
  }, 1000)
}

const getStatusText = (status) => {
  const statusMap = {
    active: { text: '正常', class: 'text-green-600 bg-green-50' },
    inactive: { text: '未激活', class: 'text-gray-600 bg-gray-50' },
    banned: { text: '已封禁', class: 'text-red-600 bg-red-50' }
  }
  return statusMap[status] || { text: '未知', class: 'text-gray-600 bg-gray-50' }
}
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Header -->
    <header class="bg-white border-b border-gray-200 sticky top-0 z-30">
      <div class="flex items-center justify-between px-6 py-4">
        <div class="flex items-center space-x-3">
          <div class="w-10 h-10 bg-primary-600 rounded-lg flex items-center justify-center">
            <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
            </svg>
          </div>
          <div>
            <span class="text-xl font-bold text-gray-900 block">Piggy</span>
            <span class="text-xs text-blue-600 font-medium block -mt-1">用户中心</span>
          </div>
        </div>
        
        <div class="flex items-center space-x-4">
          <!-- Profile Dropdown -->
            <div class="relative" @click.stop>
              <button
                @click.stop="toggleProfileDropdown"
                class="flex items-center space-x-2 hover:bg-gray-50 rounded-lg px-3 py-2 transition-colors"
              >
                <div class="w-9 h-9 bg-primary-100 rounded-full flex items-center justify-center">
                  <svg class="w-5 h-5 text-primary-600" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd"/>
                  </svg>
                </div>
                <span class="text-gray-700 text-sm font-medium">{{ user?.username || '用户' }}</span>
                <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
                </svg>
              </button>
              
              <!-- Dropdown Menu -->
              <div
                v-show="showProfileDropdown"
                class="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-gray-200 py-1 z-50"
                @click.stop
              >
                <button
                  @click.stop="goToProfile"
                  class="w-full flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                >
                  <svg class="w-4 h-4 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                  </svg>
                  个人资料
                </button>
                <button
                  @click.stop="goToSecurity"
                  class="w-full flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                >
                  <svg class="w-4 h-4 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
                  </svg>
                  账户安全
                </button>
                <div class="border-t border-gray-200 my-1"></div>
                <button
                  @click.stop="handleLogout"
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
    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="grid grid-cols-1 lg:grid-cols-4 gap-6">
        <!-- Sidebar Menu -->
        <div class="lg:col-span-1">
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <!-- User Avatar -->
            <div class="text-center mb-6">
              <div class="w-20 h-20 bg-gradient-to-br from-primary-400 to-primary-600 rounded-full flex items-center justify-center mx-auto mb-3">
                <svg class="w-10 h-10 text-white" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd"/>
                </svg>
              </div>
              <h3 class="text-lg font-semibold text-gray-900">{{ accountInfo.username }}</h3>
              <p class="text-sm text-gray-500 mt-1">{{ accountInfo.email }}</p>
              <span :class="getStatusText(accountInfo.status).class" class="inline-block px-3 py-1 rounded-full text-xs font-medium mt-2">
                {{ getStatusText(accountInfo.status).text }}
              </span>
            </div>

            <!-- Navigation Menu -->
            <nav class="space-y-2">
              <button
                @click="activeTab = 'profile'"
                :class="[
                  'w-full flex items-center px-4 py-2.5 rounded-lg transition-colors',
                  activeTab === 'profile'
                    ? 'bg-primary-50 text-primary-600'
                    : 'text-gray-700 hover:bg-gray-100'
                ]"
              >
                <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                </svg>
                个人资料
              </button>
              
              <button
                @click="activeTab = 'security'"
                :class="[
                  'w-full flex items-center px-4 py-2.5 rounded-lg transition-colors',
                  activeTab === 'security'
                    ? 'bg-primary-50 text-primary-600'
                    : 'text-gray-700 hover:bg-gray-100'
                ]"
              >
                <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
                </svg>
                账户安全
              </button>
            </nav>
          </div>
        </div>

        <!-- Main Content Area -->
        <div class="lg:col-span-3">
          <!-- Stats Cards -->
          <div class="grid grid-cols-1 md:grid-cols-1 gap-6 mb-6">
            <div v-for="(stat, index) in stats" :key="index" class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-sm text-gray-600 mb-1">{{ stat.title }}</p>
                  <p class="text-2xl font-bold text-gray-900">{{ stat.value }}</p>
                </div>
                <div :class="[stat.color, 'w-12 h-12 rounded-lg flex items-center justify-center']">
                  <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="stat.icon"></path>
                  </svg>
                </div>
              </div>
            </div>
          </div>

          <!-- Tab Content -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <!-- Profile Tab -->
            <div v-if="activeTab === 'profile'" class="space-y-6">
              <div class="flex items-center justify-between pb-4 border-b border-gray-200">
                <h2 class="text-lg font-semibold text-gray-900">个人资料</h2>
                <button class="text-primary-600 hover:text-primary-700 text-sm font-medium">
                  编辑资料
                </button>
              </div>
              
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label class="block text-sm font-medium text-gray-500 mb-1">用户名</label>
                  <div class="flex items-center px-4 py-2.5 bg-gray-50 rounded-lg border border-gray-200">
                    <svg class="w-5 h-5 text-gray-400 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                    </svg>
                    <span class="text-gray-900">{{ accountInfo.username }}</span>
                  </div>
                </div>
                
                <div>
                  <label class="block text-sm font-medium text-gray-500 mb-1">邮箱地址</label>
                  <div class="flex items-center px-4 py-2.5 bg-gray-50 rounded-lg border border-gray-200">
                    <svg class="w-5 h-5 text-gray-400 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path>
                    </svg>
                    <span class="text-gray-900">{{ accountInfo.email }}</span>
                  </div>
                </div>
                
                <div>
                  <label class="block text-sm font-medium text-gray-500 mb-1">手机号码</label>
                  <div class="flex items-center px-4 py-2.5 bg-gray-50 rounded-lg border border-gray-200">
                    <svg class="w-5 h-5 text-gray-400 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z"></path>
                    </svg>
                    <span class="text-gray-900">{{ accountInfo.phone }}</span>
                  </div>
                </div>
                
                <div>
                  <label class="block text-sm font-medium text-gray-500 mb-1">注册时间</label>
                  <div class="flex items-center px-4 py-2.5 bg-gray-50 rounded-lg border border-gray-200">
                    <svg class="w-5 h-5 text-gray-400 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path>
                    </svg>
                    <span class="text-gray-900">{{ accountInfo.createdAt }}</span>
                  </div>
                </div>
                
                <div>
                  <label class="block text-sm font-medium text-gray-500 mb-1">账户状态</label>
                  <div class="flex items-center px-4 py-2.5 bg-gray-50 rounded-lg border border-gray-200">
                    <svg class="w-5 h-5 text-gray-400 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"></path>
                    </svg>
                    <span :class="getStatusText(accountInfo.status).class" class="px-3 py-1 rounded-full text-sm font-medium">
                      {{ getStatusText(accountInfo.status).text }}
                    </span>
                  </div>
                </div>
                
                <div>
                  <label class="block text-sm font-medium text-gray-500 mb-1">最后登录</label>
                  <div class="flex items-center px-4 py-2.5 bg-gray-50 rounded-lg border border-gray-200">
                    <svg class="w-5 h-5 text-gray-400 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1"></path>
                    </svg>
                    <span class="text-gray-900">{{ accountInfo.lastLogin }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Security Tab -->
            <div v-if="activeTab === 'security'" class="space-y-6">
              <div class="pb-4 border-b border-gray-200">
                <h2 class="text-lg font-semibold text-gray-900">账户安全</h2>
                <p class="text-sm text-gray-500 mt-1">管理您的登录密码和邮箱地址</p>
              </div>
              
              <!-- Change Password -->
              <div>
                <h3 class="text-sm font-semibold text-gray-700 mb-4 flex items-center">
                  <svg class="w-5 h-5 mr-2 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
                  </svg>
                  修改密码
                </h3>
                <div class="space-y-4 bg-gray-50 rounded-lg p-4">
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-2">当前密码</label>
                    <input
                      v-model="passwordForm.currentPassword"
                      type="password"
                      placeholder="请输入当前密码"
                      class="input-field"
                    />
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-2">新密码</label>
                    <input
                      v-model="passwordForm.newPassword"
                      type="password"
                      placeholder="请输入新密码"
                      class="input-field"
                    />
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-2">确认新密码</label>
                    <input
                      v-model="passwordForm.confirmPassword"
                      type="password"
                      placeholder="请再次输入新密码"
                      class="input-field"
                    />
                  </div>
                  <button
                    @click="handleChangePassword"
                    :disabled="isChangingPassword"
                    class="btn-primary w-full flex items-center justify-center py-2.5"
                  >
                    <svg v-if="isChangingPassword" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    <span v-if="isChangingPassword">修改中...</span>
                    <span v-else>确认修改</span>
                  </button>
                </div>
              </div>

              <!-- Change Email -->
              <div class="pt-6 border-t border-gray-200">
                <h3 class="text-sm font-semibold text-gray-700 mb-4 flex items-center">
                  <svg class="w-5 h-5 mr-2 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path>
                  </svg>
                  修改邮箱
                </h3>
                <div class="space-y-4 bg-gray-50 rounded-lg p-4">
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-2">新邮箱地址</label>
                    <input
                      v-model="emailForm.newEmail"
                      type="email"
                      placeholder="请输入新邮箱"
                      class="input-field"
                    />
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-2">确认邮箱</label>
                    <input
                      v-model="emailForm.confirmEmail"
                      type="email"
                      placeholder="请再次输入新邮箱"
                      class="input-field"
                    />
                  </div>
                  <button
                    @click="handleChangeEmail"
                    :disabled="isChangingEmail"
                    class="btn-primary w-full flex items-center justify-center py-2.5"
                  >
                    <svg v-if="isChangingEmail" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    <span v-if="isChangingEmail">修改中...</span>
                    <span v-else>确认修改</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>
