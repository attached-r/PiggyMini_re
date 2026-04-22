<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/auth'
import { getAccounts, createAccount as createAccountApi, AccountTypeLabels } from '@/api/account'
import { ElMessage } from 'element-plus'
import Sidebar from '@/components/Sidebar.vue'

const router = useRouter()

// 侧边栏状态
const sidebarCollapsed = ref(false)

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
  accountType: 'DEBIT_CARD',
  balance: 0,
  currency: 'CNY',
  icon: '💳',
  remark: '',
  sortOrder: 0,
  isDefault: 0
})

// 账户类型选项
const accountTypeOptions = [
  { value: 'DEBIT_CARD', label: '储蓄卡', icon: '💳' },
  { value: 'CREDIT_CARD', label: '信用卡', icon: '💎' },
  { value: 'CASH', label: '现金', icon: '💵' },
  { value: 'ALIPAY', label: '支付宝', icon: '🔵' },
  { value: 'WECHAT', label: '微信', icon: '💚' },
  { value: 'BANK_TRANSFER', label: '银行转账', icon: '🏦' },
  { value: 'INVESTMENT', label: '投资账户', icon: '📈' },
  { value: 'VIRTUAL', label: '虚拟账户', icon: '🎮' },
  { value: 'OTHER', label: '其他', icon: '📦' }
]

onMounted(async () => {
  if (!authApi.isAuthenticated()) {
    router.replace('/login')
    return
  }
  
  await loadAccounts()
})

const loadAccounts = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const response = await getAccounts(1, 100)
    if (response.data?.records) {
      accounts.value = response.data.records
    } else if (Array.isArray(response.data)) {
      accounts.value = response.data
    } else {
      accounts.value = []
    }
    
    const savedAccountId = localStorage.getItem('selectedAccountId')
    if (savedAccountId) {
      selectedAccount.value = accounts.value.find(account => account.id === Number(savedAccountId))
    }
  } catch (err) {
    console.error('加载账户失败:', err)
    error.value = err.message || '加载账户失败，请稍后重试'
    accounts.value = []
  } finally {
    loading.value = false
  }
}

const selectAccount = (account) => {
  selectedAccount.value = account
  localStorage.setItem('selectedAccountId', account.id)
}

const proceedToDashboard = () => {
  if (!selectedAccount.value) {
    error.value = '请选择一个账户'
    return
  }
  
  // 跳转到仪表盘
  router.push('/dashboard')
}

const getAccountTypeText = (type) => {
  return AccountTypeLabels[type] || { label: type, icon: '📦' }
}

const formatBalance = (balance) => {
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    minimumFractionDigits: 2
  }).format(balance || 0)
}

const createAccount = () => {
  showCreateModal.value = true
}

const handleCreateAccount = async () => {
  createError.value = ''
  
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
    await createAccountApi(accountForm.value)
    ElMessage.success('账户创建成功')
    showCreateModal.value = false
    await loadAccounts()
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
  accountForm.value = {
    accountName: '',
    accountType: 'DEBIT_CARD',
    balance: 0,
    currency: 'CNY',
    icon: '💳',
    remark: '',
    sortOrder: 0,
    isDefault: 0
  }
}
</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900">
    <!-- 侧边栏 -->
    <Sidebar :collapsed="sidebarCollapsed" @toggle="sidebarCollapsed = !sidebarCollapsed" />

    <!-- 主内容区 -->
    <main 
      class="transition-all duration-300"
      :class="sidebarCollapsed ? 'ml-16' : 'ml-56'"
    >
      <!-- Header -->
      <header class="bg-slate-900/80 backdrop-blur-xl border-b border-slate-700/50 sticky top-0 z-30">
        <div class="max-w-6xl mx-auto px-6 py-4">
          <div class="flex items-center justify-between">
            <div>
              <h1 class="text-2xl font-bold text-white">账户管理</h1>
              <p class="text-slate-400 text-sm">管理您的所有账户</p>
            </div>
          </div>
        </div>
      </header>

      <!-- Main Content -->
      <main class="max-w-6xl mx-auto px-6 py-8">
        <!-- Error Message -->
        <div v-if="error" class="bg-rose-500/20 border border-rose-500/50 text-rose-400 px-4 py-3 rounded-xl mb-6 flex items-center">
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
              'bg-slate-800/50 border rounded-2xl p-6 cursor-pointer transition-all duration-200',
              selectedAccount?.id === account.id 
                ? 'border-indigo-500 ring-2 ring-indigo-500/50' 
                : 'border-slate-700/50 hover:border-slate-600/50'
            ]"
          >
            <div class="flex items-center justify-between mb-4">
              <div class="w-12 h-12 bg-slate-700/50 rounded-xl flex items-center justify-center">
                <span class="text-2xl">{{ account.icon || '💳' }}</span>
              </div>
              <span v-if="selectedAccount?.id === account.id" class="text-indigo-400">
                <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
                </svg>
              </span>
            </div>
            <h3 class="text-lg font-semibold text-white mb-1">{{ account.accountName }}</h3>
            <p class="text-slate-400 text-sm mb-3">{{ getAccountTypeText(account.accountType).label || account.accountType }}</p>
            <p :class="account.balance >= 0 ? 'text-emerald-400' : 'text-rose-400'" class="text-xl font-bold">
              {{ formatBalance(account.balance) }}
            </p>
          </div>

          <!-- Add New Account Card -->
          <div
            @click="createAccount"
            class="bg-slate-800/30 border-2 border-dashed border-slate-700/50 rounded-2xl p-6 cursor-pointer hover:border-indigo-500/50 hover:bg-slate-800/50 transition-all duration-200 flex flex-col items-center justify-center min-h-[180px]"
          >
            <div class="w-12 h-12 bg-slate-700/50 rounded-xl flex items-center justify-center mb-4">
              <svg class="w-6 h-6 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path>
              </svg>
            </div>
            <p class="text-slate-400 font-medium">添加新账户</p>
          </div>
        </div>

        <!-- Loading State -->
        <div v-else class="flex justify-center items-center py-20">
          <div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-indigo-600"></div>
        </div>

        <!-- Action Button -->
        <div v-if="accounts.length > 0" class="flex justify-center">
          <button
            @click="proceedToDashboard"
            :disabled="!selectedAccount"
            class="px-8 py-3 bg-indigo-600 hover:bg-indigo-700 text-white font-semibold rounded-xl transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {{ selectedAccount ? '进入仪表盘' : '请选择账户' }}
          </button>
        </div>
      </main>
    </main>

    <!-- Create Account Modal -->
    <div v-if="showCreateModal" class="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50">
      <div class="bg-slate-800 border border-slate-700 rounded-2xl shadow-xl w-full max-w-md p-6">
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-xl font-bold text-white">创建新账户</h3>
          <button @click="cancelCreateAccount" class="text-slate-400 hover:text-white">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
        </div>

        <div v-if="createError" class="bg-rose-500/20 border border-rose-500/50 text-rose-400 px-4 py-3 rounded-lg mb-4">
          {{ createError }}
        </div>

        <form @submit.prevent="handleCreateAccount" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-slate-300 mb-2">账户名称</label>
            <input
              v-model="accountForm.accountName"
              type="text"
              placeholder="例如：我的银行卡"
              class="w-full bg-slate-900/50 border border-slate-600/50 rounded-lg px-4 py-3 text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-slate-300 mb-2">账户类型</label>
            <div class="grid grid-cols-3 gap-2">
              <button
                v-for="option in accountTypeOptions"
                :key="option.value"
                type="button"
                @click="accountForm.accountType = option.value; accountForm.icon = option.icon"
                :class="[
                  'py-2 px-3 rounded-lg text-center transition-colors text-sm',
                  accountForm.accountType === option.value
                    ? 'bg-indigo-600 text-white'
                    : 'bg-slate-700/50 text-slate-300 hover:bg-slate-600/50'
                ]"
              >
                {{ option.icon }} {{ option.label }}
              </button>
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-slate-300 mb-2">初始余额</label>
            <input
              v-model.number="accountForm.balance"
              type="number"
              step="0.01"
              placeholder="0.00"
              class="w-full bg-slate-900/50 border border-slate-600/50 rounded-lg px-4 py-3 text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-slate-300 mb-2">备注（可选）</label>
            <input
              v-model="accountForm.remark"
              type="text"
              placeholder="添加备注信息"
              class="w-full bg-slate-900/50 border border-slate-600/50 rounded-lg px-4 py-3 text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>

          <div class="flex space-x-3 pt-4">
            <button
              type="button"
              @click="cancelCreateAccount"
              class="flex-1 py-3 bg-slate-700 hover:bg-slate-600 text-white rounded-lg transition-colors"
            >
              取消
            </button>
            <button
              type="submit"
              :disabled="creatingAccount"
              class="flex-1 py-3 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg transition-colors disabled:opacity-50"
            >
              {{ creatingAccount ? '创建中...' : '确认创建' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
