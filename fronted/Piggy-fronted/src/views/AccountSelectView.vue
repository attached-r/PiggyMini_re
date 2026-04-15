<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/auth'
import { getAccounts, createAccount as createAccountApi } from '@/api/account'

const router = useRouter()
const accounts = ref([])
const loading = ref(true)
const error = ref('')
const selectedAccount = ref(null)

// 创建账户模态框
const showCreateModal = ref(false)
const creatingAccount = ref(false)
const createError = ref('')

// 账户表单数据
const accountForm = ref({
  accountName: '',
  accountType: 'ONLINE',
  balance: 0,
  remark: ''
})

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
  
  loadAccounts()
})

const loadAccounts = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const response = await getAccounts()
    // 根据接口文档，账户列表在 response.data.records 中
    accounts.value = response.data?.records || []
    
    // 检查是否有默认选中的账户
    const savedAccountId = localStorage.getItem('selectedAccountId')
    if (savedAccountId) {
      selectedAccount.value = accounts.value.find(account => account.id === savedAccountId)
    }
  } catch (err) {
    console.error('加载账户失败:', err)
    error.value = '加载账户失败，请稍后重试'
    
    // 模拟数据，用于演示
    accounts.value = [
      {
        id: '1',
        accountName: '个人储蓄账户',
        balance: 15000.00,
        accountType: 'DEBIT_CARD',
        status: 'active'
      },
      {
        id: '2',
        accountName: '信用卡账户',
        balance: -2500.00,
        accountType: 'CREDIT_CARD',
        status: 'active'
      },
      {
        id: '3',
        accountName: '投资账户',
        balance: 50000.00,
        accountType: 'INVESTMENT',
        status: 'active'
      }
    ]
  } finally {
    loading.value = false
  }
}

const selectAccount = (account) => {
  selectedAccount.value = account
  // 保存选中的账户ID到本地存储
  localStorage.setItem('selectedAccountId', account.id)
  localStorage.setItem('selectedAccount', JSON.stringify(account))
}

const proceedToServices = () => {
  if (!selectedAccount.value) {
    error.value = '请选择一个账户'
    return
  }
  
  // 跳转到核心功能页面
  router.push('/services')
}

const getAccountTypeText = (type) => {
    const typeMap = {
      ONLINE: '在线账户 🌐',
      CASH: '现金 💵',
      BANK: '银行账户 🏦',
      CREDIT: '信用卡 💳',
      INVESTMENT: '投资账户 📈',
      OTHER: '其他 📦'
    }
    return typeMap[type] || type
  }

const getAccountStatusText = (status) => {
  const statusMap = {
    active: { text: '正常', class: 'text-green-600 bg-green-50' },
    inactive: { text: '未激活', class: 'text-gray-600 bg-gray-50' },
    frozen: { text: '已冻结', class: 'text-yellow-600 bg-yellow-50' }
  }
  return statusMap[status] || { text: '未知', class: 'text-gray-600 bg-gray-50' }
}

const formatBalance = (balance) => {
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    minimumFractionDigits: 2
  }).format(balance)
}

const createAccount = () => {
  showCreateModal.value = true
}

const handleCreateAccount = async () => {
  createError.value = ''
  
  // 验证表单
  if (!accountForm.value.accountName.trim()) {
    createError.value = '请输入账户名称'
    return
  }
  
  if (accountForm.value.balance < 0) {
    createError.value = '账户余额不能为负数'
    return
  }
  
  creatingAccount.value = true
  
  try {
    const response = await createAccountApi(accountForm.value)
    console.log('创建账户成功:', response)
    
    // 关闭模态框
    showCreateModal.value = false
    
    // 重新加载账户列表
    await loadAccounts()
    
    // 选择新创建的账户
    if (response.data) {
      selectedAccount.value = response.data
      localStorage.setItem('selectedAccountId', response.data.id)
      localStorage.setItem('selectedAccount', JSON.stringify(response.data))
    }
  } catch (err) {
    console.error('创建账户失败:', err)
    createError.value = err.message || '创建账户失败，请稍后重试'
  } finally {
    creatingAccount.value = false
  }
}

const cancelCreateAccount = () => {
  showCreateModal.value = false
  createError.value = ''
  
  // 重置表单
  accountForm.value = {
    accountName: '',
    accountType: 'ONLINE',
    balance: 0,
    remark: ''
  }
}

const handleLogout = () => {
  authApi.logout()
  router.push('/login')
}
</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-primary-50 via-white to-primary-100">
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
            <span class="text-xs text-blue-600 font-medium block -mt-1">账户选择</span>
          </div>
        </div>
        
        <div class="flex items-center space-x-4">
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

    <!-- Main Content -->
    <main class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Page Header -->
      <div class="text-center mb-12">
        <h1 class="text-3xl font-bold text-gray-900 mb-4">选择您的账户</h1>
        <p class="text-gray-600 max-w-2xl mx-auto">
          请选择一个账户以访问相关服务。您可以随时在服务页面切换账户。
        </p>
      </div>

      <!-- Error Message -->
      <div v-if="error" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6 flex items-center">
        <svg class="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
          <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/>
        </svg>
        {{ error }}
      </div>

      <!-- Accounts Grid -->
      <div v-if="!loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
        <div
          v-for="account in accounts"
          :key="account.id"
          @click="selectAccount(account)"
          :class="[
            'bg-white rounded-xl shadow-sm border border-gray-200 p-6 cursor-pointer transition-all duration-200',
            selectedAccount?.id === account.id
              ? 'border-primary-400 shadow-md bg-primary-50'
              : 'hover:border-primary-200 hover:shadow-md'
          ]"
        >
          <div class="flex items-start justify-between mb-4">
            <div>
              <h3 class="text-lg font-semibold text-gray-900">{{ account.accountName }}</h3>
              <span :class="getAccountStatusText(account.status).class" class="inline-block px-3 py-1 rounded-full text-xs font-medium mt-2">
                {{ getAccountStatusText(account.status).text }}
              </span>
            </div>
            <div :class="[
              'w-12 h-12 rounded-lg flex items-center justify-center',
              account.accountType === 'CREDIT_CARD' ? 'bg-red-50 text-red-600' : 'bg-green-50 text-green-600'
            ]">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
              </svg>
            </div>
          </div>
          
          <div class="space-y-2">
            <div class="flex justify-between items-center">
              <span class="text-sm text-gray-600">账户类型</span>
              <span class="text-sm font-medium text-gray-900">{{ getAccountTypeText(account.accountType) }}</span>
            </div>
            <div class="flex justify-between items-center pt-2 border-t border-gray-100">
              <span class="text-sm text-gray-600">账户余额</span>
              <span :class="[
                'text-lg font-bold',
                account.balance < 0 ? 'text-red-600' : 'text-green-600'
              ]">
                {{ formatBalance(account.balance) }}
              </span>
            </div>
          </div>
          
          <!-- Selection Indicator -->
          <div v-if="selectedAccount?.id === account.id" class="mt-4 flex items-center justify-end">
            <div class="flex items-center space-x-2 text-primary-600">
              <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
              <span class="text-sm font-medium">已选择</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Loading State -->
      <div v-else class="flex justify-center items-center py-20">
        <div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary-600"></div>
      </div>

      <!-- Empty State -->
      <div v-if="!loading && accounts.length === 0" class="text-center py-20">
        <div class="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
          <svg class="w-10 h-10 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
          </svg>
        </div>
        <h3 class="text-lg font-medium text-gray-900 mb-2">暂无账户</h3>
        <p class="text-gray-600 mb-6">您还没有创建任何账户</p>
        <button 
          @click="createAccount"
          class="btn-primary px-6 py-2"
        >
          创建账户
        </button>
      </div>

      <!-- Action Button -->
      <div v-if="!loading && accounts.length > 0" class="flex justify-center mt-8">
        <button
          @click="proceedToServices"
          :disabled="!selectedAccount"
          :class="[
            'btn-primary px-8 py-3 text-lg font-medium',
            !selectedAccount && 'opacity-50 cursor-not-allowed'
          ]"
        >
          进入服务
        </button>
      </div>
    </main>

    <!-- Footer -->
    <footer class="bg-white border-t border-gray-200 py-6">
      <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
        <p class="text-gray-500 text-sm">&copy; 2024 Piggy. All rights reserved.</p>
      </div>
    </footer>

    <!-- Create Account Modal -->
    <div v-if="showCreateModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-xl shadow-xl w-full max-w-md p-6">
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-xl font-bold text-gray-900">创建新账户</h3>
          <button
            @click="cancelCreateAccount"
            class="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
        </div>

        <!-- Error Message -->
        <div v-if="createError" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-4 flex items-center">
          <svg class="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/>
          </svg>
          {{ createError }}
        </div>

        <!-- Account Form -->
        <form @submit.prevent="handleCreateAccount" class="space-y-4">
          <!-- Account Name -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              账户名称
            </label>
            <input
              v-model="accountForm.accountName"
              type="text"
              placeholder="请输入账户名称"
              class="input-field"
              :disabled="creatingAccount"
            />
          </div>

          <!-- Account Type -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              账户类型
            </label>
            <select
              v-model="accountForm.accountType"
              class="input-field"
              :disabled="creatingAccount"
            >
              <option value="ONLINE">在线账户</option>
              <option value="CASH">现金</option>
              <option value="BANK">银行账户</option>
              <option value="CREDIT">信用卡</option>
              <option value="INVESTMENT">投资账户</option>
              <option value="OTHER">其他</option>
            </select>
          </div>

          <!-- Initial Balance -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              初始余额
            </label>
            <input
              v-model.number="accountForm.balance"
              type="number"
              min="0"
              step="0.01"
              placeholder="请输入初始余额"
              class="input-field"
              :disabled="creatingAccount"
            />
          </div>

          <!-- Remark -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              备注
            </label>
            <textarea
              v-model="accountForm.remark"
              placeholder="请输入备注信息"
              rows="3"
              class="input-field"
              :disabled="creatingAccount"
            ></textarea>
          </div>

          <!-- Action Buttons -->
          <div class="flex space-x-4 pt-4">
            <button
              type="button"
              @click="cancelCreateAccount"
              class="flex-1 btn-secondary py-2"
              :disabled="creatingAccount"
            >
              取消
            </button>
            <button
              type="submit"
              class="flex-1 btn-primary py-2"
              :disabled="creatingAccount"
            >
              <svg v-if="creatingAccount" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              <span v-if="creatingAccount">创建中...</span>
              <span v-else>创建账户</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
