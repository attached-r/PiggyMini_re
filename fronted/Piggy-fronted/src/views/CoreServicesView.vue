<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/auth'
import { getAccounts } from '@/api/account'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(true)
const selectedAccount = ref(null)
const accounts = ref([])

onMounted(async () => {
  // 检查是否已登录
  if (!authApi.isAuthenticated()) {
    router.replace('/login')
    return
  }
  
  // 如果是管理员，跳转到管理后台
  if (authApi.isAdmin()) {
    router.replace('/home')
    return
  }
  
  // 加载账户信息
  await loadAccounts()
})

const loadAccounts = async () => {
  loading.value = true
  try {
    const response = await getAccounts(1, 100)
    let accountList = []
    if (response.data?.records) {
      accountList = response.data.records
    } else if (Array.isArray(response.data)) {
      accountList = response.data
    }
    accounts.value = accountList
    
    // 获取当前选中的账户
    const savedAccountId = localStorage.getItem('selectedAccountId')
    if (savedAccountId) {
      selectedAccount.value = accountList.find(acc => acc.id === Number(savedAccountId))
    } else if (accountList.length > 0) {
      selectedAccount.value = accountList[0]
      localStorage.setItem('selectedAccountId', accountList[0].id)
    }
  } catch (err) {
    console.error('加载账户失败:', err)
    ElMessage.error('加载账户信息失败')
  } finally {
    loading.value = false
  }
}

const switchAccount = () => {
  router.push('/accounts')
}

const handleLogout = () => {
  authApi.logout()
  router.push('/login')
}

const formatBalance = (balance) => {
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    minimumFractionDigits: 2
  }).format(balance || 0)
}

// 服务列表 - 带有实际跳转路径
const services = [
  {
    id: 'transaction',
    name: '交易服务',
    icon: 'M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z',
    description: '记录收支、管理交易',
    color: 'bg-purple-500',
    path: '/transactions'
  },
  {
    id: 'budget',
    name: '预算服务',
    icon: 'M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z',
    description: '制定预算、跟踪执行',
    color: 'bg-yellow-500',
    path: '/budgets'
  },
  {
    id: 'report',
    name: '报表服务',
    icon: 'M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z',
    description: '分析报表、查看趋势',
    color: 'bg-blue-500',
    path: '/reports'
  },
  {
    id: 'account',
    name: '账户管理',
    icon: 'M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z',
    description: '管理账户、查看余额',
    color: 'bg-green-500',
    path: '/accounts'
  }
]

// 跳转到指定服务
const goToService = (servicePath) => {
  router.push(servicePath)
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
            <span class="text-xs text-indigo-600 font-medium block -mt-1">核心服务</span>
          </div>
        </div>
        
        <div class="flex items-center space-x-4">
          <!-- Selected Account Info -->
          <div v-if="selectedAccount" class="flex items-center space-x-3">
            <div class="bg-indigo-50 text-indigo-600 px-3 py-1.5 rounded-lg text-sm font-medium">
              {{ selectedAccount.accountName }}
            </div>
            <button
              @click="switchAccount"
              class="text-sm text-gray-600 hover:text-indigo-600 transition-colors"
            >
              切换账户
            </button>
          </div>
          
          <!-- Logout Button -->
          <button
            @click="handleLogout"
            class="flex items-center space-x-2 text-gray-600 hover:text-red-600 transition-colors"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path>
            </svg>
            <span class="text-sm font-medium">退出登录</span>
          </button>
        </div>
      </div>
    </header>

    <!-- Account Info Bar -->
    <div v-if="selectedAccount" class="bg-white border-b border-gray-200 py-4 px-6">
      <div class="max-w-6xl mx-auto flex items-center justify-between">
        <div>
          <h2 class="text-lg font-semibold text-gray-900">当前账户: {{ selectedAccount.accountName }}</h2>
          <p class="text-sm text-gray-600">
            账户类型: {{ selectedAccount.accountType === 'DEBIT_CARD' ? '储蓄卡' : 
                           selectedAccount.accountType === 'CREDIT_CARD' ? '信用卡' : 
                           selectedAccount.accountType === 'CASH' ? '现金' : 
                           selectedAccount.accountType === 'ALIPAY' ? '支付宝' : 
                           selectedAccount.accountType === 'WECHAT' ? '微信' : 
                           selectedAccount.accountType === 'INVESTMENT' ? '投资账户' : 
                           selectedAccount.accountType }}
          </p>
        </div>
        <div class="text-right">
          <p class="text-sm text-gray-600">账户余额</p>
          <p :class="[
            'text-2xl font-bold',
            selectedAccount.balance < 0 ? 'text-red-600' : 'text-green-600'
          ]">
            {{ formatBalance(selectedAccount.balance) }}
          </p>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <main class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Page Title -->
      <div class="text-center mb-10">
        <h1 class="text-3xl font-bold text-gray-900 mb-3">选择服务</h1>
        <p class="text-gray-600">请选择一个服务开始管理您的财务</p>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="flex justify-center items-center py-20">
        <div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-indigo-600"></div>
      </div>

      <!-- Services Grid -->
      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <button
          v-for="service in services"
          :key="service.id"
          @click="goToService(service.path)"
          class="bg-white rounded-2xl shadow-sm border border-gray-200 p-6 text-left hover:shadow-lg hover:border-indigo-300 hover:-translate-y-1 transition-all duration-300 group"
        >
          <div :class="[service.color, 'w-14 h-14 rounded-xl flex items-center justify-center text-white mb-5 group-hover:scale-110 transition-transform']">
            <svg class="w-7 h-7" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="service.icon"></path>
            </svg>
          </div>
          <h3 class="text-lg font-semibold text-gray-900 mb-2">{{ service.name }}</h3>
          <p class="text-sm text-gray-500 mb-4">{{ service.description }}</p>
          <div class="flex items-center text-indigo-600 text-sm font-medium">
            <span>开始使用</span>
            <svg class="w-4 h-4 ml-1 group-hover:translate-x-1 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
            </svg>
          </div>
        </button>
      </div>

      <!-- Quick Tips -->
      <div class="mt-12 bg-gradient-to-r from-indigo-500 to-purple-600 rounded-2xl p-8 text-white">
        <h3 class="text-xl font-bold mb-4">💡 快速提示</h3>
        <ul class="space-y-2 text-indigo-100">
          <li class="flex items-start">
            <span class="mr-2">•</span>
            <span>在交易服务中记录您的每一笔收支</span>
          </li>
          <li class="flex items-start">
            <span class="mr-2">•</span>
            <span>使用预算服务规划您的月度支出</span>
          </li>
          <li class="flex items-start">
            <span class="mr-2">•</span>
            <span>定期查看报表了解您的财务状况</span>
          </li>
        </ul>
      </div>
    </main>

    <!-- Footer -->
    <footer class="bg-white border-t border-gray-200 py-6 mt-auto">
      <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
        <p class="text-gray-500 text-sm">© 2024 Piggy. All rights reserved.</p>
      </div>
    </footer>
  </div>
</template>
