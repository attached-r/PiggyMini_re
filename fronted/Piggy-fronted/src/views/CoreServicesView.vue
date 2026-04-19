<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/auth'

const router = useRouter()
const selectedAccount = ref(null)
const activeService = ref('account')

onMounted(() => {
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
  
  // 检查是否已选择账户
  const savedAccount = localStorage.getItem('selectedAccount')
  if (!savedAccount) {
    router.replace('/accounts')
    return
  }
  
  selectedAccount.value = JSON.parse(savedAccount)
})

const services = [
  {
    id: 'auth',
    name: 'Auth 服务',
    icon: 'M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z',
    description: '管理账户认证和安全设置',
    color: 'bg-blue-500'
  },
  {
    id: 'account',
    name: 'Account 服务',
    icon: 'M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z',
    description: '管理银行账户信息和余额',
    color: 'bg-green-500'
  },
  {
    id: 'transaction',
    name: 'Transaction 服务',
    icon: 'M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z',
    description: '管理交易记录和转账',
    color: 'bg-purple-500'
  },
  {
    id: 'budget',
    name: 'Budget 服务',
    icon: 'M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z',
    description: '管理预算和支出计划',
    color: 'bg-yellow-500'
  },
  {
    id: 'report',
    name: 'Report 服务',
    icon: 'M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z',
    description: '生成财务报告和分析',
    color: 'bg-red-500'
  },
  {
    id: 'ai',
    name: 'AI 服务',
    icon: 'M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z',
    description: '智能财务建议和分析',
    color: 'bg-indigo-500'
  }
]

const selectService = (serviceId) => {
  activeService.value = serviceId
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
  }).format(balance)
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
            <span class="text-xs text-blue-600 font-medium block -mt-1">核心服务</span>
          </div>
        </div>
        
        <div class="flex items-center space-x-4">
          <!-- Selected Account Info -->
          <div v-if="selectedAccount" class="flex items-center space-x-3">
            <div class="bg-primary-50 text-primary-600 px-3 py-1.5 rounded-lg text-sm font-medium">
              {{ selectedAccount.accountName }}
            </div>
            <button
              @click="switchAccount"
              class="text-sm text-gray-600 hover:text-primary-600 transition-colors"
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
          <p class="text-sm text-gray-600">账户类型: {{ selectedAccount.accountType === 'ONLINE' ? '在线账户' : selectedAccount.accountType === 'CASH' ? '现金' : selectedAccount.accountType === 'BANK' ? '银行账户' : selectedAccount.accountType === 'CREDIT' ? '信用卡' : selectedAccount.accountType === 'INVESTMENT' ? '投资账户' : selectedAccount.accountType }}</p>
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
      <!-- Services Grid -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
        <div
          v-for="service in services"
          :key="service.id"
          @click="selectService(service.id)"
          :class="[
            'bg-white rounded-xl shadow-sm border border-gray-200 p-6 cursor-pointer transition-all duration-200',
            activeService === service.id
              ? 'border-primary-400 shadow-md bg-primary-50'
              : 'hover:border-primary-200 hover:shadow-md'
          ]"
        >
          <div class="flex items-start justify-between mb-4">
            <div :class="[service.color, 'w-12 h-12 rounded-lg flex items-center justify-center text-white']">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="service.icon"></path>
              </svg>
            </div>
            <div v-if="activeService === service.id" class="text-primary-600">
              <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
            </div>
          </div>
          <h3 class="text-lg font-semibold text-gray-900 mb-2">{{ service.name }}</h3>
          <p class="text-sm text-gray-600">{{ service.description }}</p>
        </div>
      </div>

      <!-- Service Content -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <!-- Auth Service -->
        <div v-if="activeService === 'auth'" class="space-y-6">
          <div class="flex items-center justify-between pb-4 border-b border-gray-200">
            <h2 class="text-xl font-semibold text-gray-900">Auth 服务</h2>
            <span class="bg-blue-100 text-blue-800 text-xs font-medium px-3 py-1 rounded-full">安全设置</span>
          </div>
          <div class="space-y-4">
            <p class="text-gray-600">管理您的账户认证和安全设置，包括密码修改、两步验证等功能。</p>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div class="bg-gray-50 rounded-lg p-4">
                <h4 class="font-medium text-gray-900 mb-2">密码管理</h4>
                <p class="text-sm text-gray-600 mb-3">修改您的登录密码，确保账户安全。</p>
                <button class="btn-primary text-sm px-4 py-2">修改密码</button>
              </div>
              <div class="bg-gray-50 rounded-lg p-4">
                <h4 class="font-medium text-gray-900 mb-2">两步验证</h4>
                <p class="text-sm text-gray-600 mb-3">启用两步验证，提高账户安全性。</p>
                <button class="btn-primary text-sm px-4 py-2">设置验证</button>
              </div>
            </div>
          </div>
        </div>

        <!-- Account Service -->
        <div v-if="activeService === 'account'" class="space-y-6">
          <div class="flex items-center justify-between pb-4 border-b border-gray-200">
            <h2 class="text-xl font-semibold text-gray-900">Account 服务</h2>
            <span class="bg-green-100 text-green-800 text-xs font-medium px-3 py-1 rounded-full">账户管理</span>
          </div>
          <div class="space-y-4">
            <p class="text-gray-600">管理您的银行账户信息，查看余额和账户详情。</p>
            <div class="bg-gray-50 rounded-lg p-4">
              <h4 class="font-medium text-gray-900 mb-2">账户详情</h4>
              <div class="space-y-2">
                  <div class="flex justify-between">
                    <span class="text-sm text-gray-600">账户名称</span>
                    <span class="text-sm font-medium text-gray-900">{{ selectedAccount?.accountName }}</span>
                  </div>
                  <div class="flex justify-between">
                    <span class="text-sm text-gray-600">账户类型</span>
                    <span class="text-sm font-medium text-gray-900">{{ selectedAccount?.accountType === 'ONLINE' ? '在线账户' : selectedAccount?.accountType === 'CASH' ? '现金' : selectedAccount?.accountType === 'BANK' ? '银行账户' : selectedAccount?.accountType === 'CREDIT' ? '信用卡' : selectedAccount?.accountType === 'INVESTMENT' ? '投资账户' : selectedAccount?.accountType }}</span>
                  </div>
                  <div class="flex justify-between">
                    <span class="text-sm text-gray-600">账户余额</span>
                    <span :class="[
                      'text-sm font-medium',
                      selectedAccount?.balance < 0 ? 'text-red-600' : 'text-green-600'
                    ]">
                      {{ selectedAccount ? formatBalance(selectedAccount.balance) : '¥0.00' }}
                    </span>
                  </div>
                </div>
            </div>
            <div class="flex justify-end">
              <button class="btn-primary px-4 py-2">创建新账户</button>
            </div>
          </div>
        </div>

        <!-- Transaction Service -->
        <div v-if="activeService === 'transaction'" class="space-y-6">
          <div class="flex items-center justify-between pb-4 border-b border-gray-200">
            <h2 class="text-xl font-semibold text-gray-900">Transaction 服务</h2>
            <span class="bg-purple-100 text-purple-800 text-xs font-medium px-3 py-1 rounded-full">交易管理</span>
          </div>
          <div class="space-y-4">
            <p class="text-gray-600">管理您的交易记录，进行转账和支付操作。</p>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div class="bg-gray-50 rounded-lg p-4">
                <h4 class="font-medium text-gray-900 mb-2">转账</h4>
                <p class="text-sm text-gray-600 mb-3">向其他账户转账。</p>
                <button class="btn-primary text-sm px-4 py-2">发起转账</button>
              </div>
              <div class="bg-gray-50 rounded-lg p-4">
                <h4 class="font-medium text-gray-900 mb-2">交易记录</h4>
                <p class="text-sm text-gray-600 mb-3">查看历史交易记录。</p>
                <button class="btn-primary text-sm px-4 py-2">查看记录</button>
              </div>
            </div>
          </div>
        </div>

        <!-- Budget Service -->
        <div v-if="activeService === 'budget'" class="space-y-6">
          <div class="flex items-center justify-between pb-4 border-b border-gray-200">
            <h2 class="text-xl font-semibold text-gray-900">Budget 服务</h2>
            <span class="bg-yellow-100 text-yellow-800 text-xs font-medium px-3 py-1 rounded-full">预算管理</span>
          </div>
          <div class="space-y-4">
            <p class="text-gray-600">管理您的预算计划，跟踪支出情况。</p>
            <div class="bg-gray-50 rounded-lg p-4">
              <h4 class="font-medium text-gray-900 mb-2">预算设置</h4>
              <p class="text-sm text-gray-600 mb-3">创建和管理您的预算计划。</p>
              <button class="btn-primary text-sm px-4 py-2">设置预算</button>
            </div>
          </div>
        </div>

        <!-- Report Service -->
        <div v-if="activeService === 'report'" class="space-y-6">
          <div class="flex items-center justify-between pb-4 border-b border-gray-200">
            <h2 class="text-xl font-semibold text-gray-900">Report 服务</h2>
            <span class="bg-red-100 text-red-800 text-xs font-medium px-3 py-1 rounded-full">报告分析</span>
          </div>
          <div class="space-y-4">
            <p class="text-gray-600">生成财务报告，分析您的收支情况。</p>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div class="bg-gray-50 rounded-lg p-4">
                <h4 class="font-medium text-gray-900 mb-2">月度报告</h4>
                <p class="text-sm text-gray-600 mb-3">生成月度财务报告。</p>
                <button class="btn-primary text-sm px-4 py-2">生成报告</button>
              </div>
              <div class="bg-gray-50 rounded-lg p-4">
                <h4 class="font-medium text-gray-900 mb-2">年度报告</h4>
                <p class="text-sm text-gray-600 mb-3">生成年度财务报告。</p>
                <button class="btn-primary text-sm px-4 py-2">生成报告</button>
              </div>
            </div>
          </div>
        </div>

        <!-- AI Service -->
        <div v-if="activeService === 'ai'" class="space-y-6">
          <div class="flex items-center justify-between pb-4 border-b border-gray-200">
            <h2 class="text-xl font-semibold text-gray-900">AI 服务</h2>
            <span class="bg-indigo-100 text-indigo-800 text-xs font-medium px-3 py-1 rounded-full">智能分析</span>
          </div>
          <div class="space-y-4">
            <p class="text-gray-600">获取智能财务建议和分析，优化您的财务管理。</p>
            <div class="bg-gray-50 rounded-lg p-4">
              <h4 class="font-medium text-gray-900 mb-2">财务建议</h4>
              <p class="text-sm text-gray-600 mb-3">获取基于您的财务状况的智能建议。</p>
              <button class="btn-primary text-sm px-4 py-2">获取建议</button>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- Footer -->
    <footer class="bg-white border-t border-gray-200 py-6 mt-8">
      <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
        <p class="text-gray-500 text-sm">&copy; 2024 Piggy. All rights reserved.</p>
      </div>
    </footer>
  </div>
</template>
