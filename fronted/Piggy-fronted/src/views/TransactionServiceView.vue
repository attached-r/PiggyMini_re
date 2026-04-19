<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/auth'
import { getTransactions, createTransaction, deleteTransaction } from '@/api/transaction'
import { getAccounts } from '@/api/account'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

// Transaction types
const transactionTypes = [
  { value: 'EXPENSE', label: '支出', sign: '-', icon: '💸', color: 'red' },
  { value: 'INCOME', label: '收入', sign: '+', icon: '💰', color: 'green' },
  { value: 'TRANSFER', label: '转账', sign: '=', icon: '🔄', color: 'blue' }
]

// Data
const allAccounts = ref([])
const selectedAccountId = ref(null)
const selectedAccount = ref(null)
const loadingAccounts = ref(false)
const transactions = ref([])
const loadingTransactions = ref(false)
const selectedTransactionType = ref('ALL')
const showTransactionModal = ref(false)
const submittingTransaction = ref(false)
const expenseParentCategory = ref('餐饮')

// Form
const transactionForm = ref({
  type: 'EXPENSE',
  amount: '',
  category: '',
  description: '',
  tradeTime: '',
  remark: '',
  tags: '',
  targetAccountId: null
})

// Categories
const expenseCategoriesByParent = {
  '餐饮': [
    { value: 'FOOD_BREAKFAST', label: '早餐', icon: '🥐' },
    { value: 'FOOD_LUNCH', label: '午餐', icon: '🍱' },
    { value: 'FOOD_DINNER', label: '晚餐', icon: '🍜' }
  ],
  '交通': [
    { value: 'TRANSPORT_SUBWAY', label: '地铁', icon: '🚇' },
    { value: 'TRANSPORT_BUS', label: '公交', icon: '🚌' }
  ]
}

const parentCategories = Object.keys(expenseCategoriesByParent)

const currentExpenseCategories = computed(() => {
  return expenseCategoriesByParent[expenseParentCategory.value] || []
})

const categoryMap = computed(() => {
  const map = {}
  for (const parent of Object.keys(expenseCategoriesByParent)) {
    for (const cat of expenseCategoriesByParent[parent]) {
      map[cat.value] = cat
    }
  }
  return map
})

const targetAccountOptions = computed(() => {
  return allAccounts.value.filter(acc => acc.id !== selectedAccountId.value)
})

const currentTransactionType = computed(() => {
  return transactionTypes.find(t => t.value === transactionForm.value.type) || transactionTypes[0]
})

// Methods
const goBack = () => router.push('/dashboard')
const handleLogout = () => { authApi.logout(); router.push('/login') }

const formatBalance = (balance) => {
  return new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(balance || 0)
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

const getCategoryInfo = (category) => categoryMap.value[category] || { label: category, icon: '📦' }
const getTransactionTypeInfo = (type) => transactionTypes.find(t => t.value === type) || transactionTypes[0]

const getTransactionBgClass = (type) => {
  const isExpense = type === 'EXPENSE' || type === 'expense'
  const isIncome = type === 'INCOME' || type === 'income'
  return ['w-12 h-12 rounded-full flex items-center justify-center text-2xl', isExpense ? 'bg-red-100' : isIncome ? 'bg-green-100' : 'bg-blue-100']
}

const getTransactionBadgeClass = (type) => {
  const isExpense = type === 'EXPENSE' || type === 'expense'
  const isIncome = type === 'INCOME' || type === 'income'
  return isExpense ? 'bg-red-100 text-red-700' : isIncome ? 'bg-green-100 text-green-700' : 'bg-blue-100 text-blue-700'
}

const getTransactionAmountClass = (type) => {
  const isExpense = type === 'EXPENSE' || type === 'expense'
  const isIncome = type === 'INCOME' || type === 'income'
  return isExpense ? 'text-red-600' : isIncome ? 'text-green-600' : 'text-blue-600'
}

const getTransactionLabel = (t) => {
  const isExpense = t.transactionType === 'EXPENSE' || t.transactionType === 'expense'
  const isIncome = t.transactionType === 'INCOME' || t.transactionType === 'income'
  return isExpense ? (getCategoryInfo(t.category)?.label || '支出') : isIncome ? '收入' : '转账'
}

// Load data
const loadAccounts = async () => {
  loadingAccounts.value = true
  try {
    const response = await getAccounts(1, 100)
    let accountList = []
    if (Array.isArray(response?.data?.data?.records)) {
      accountList = response.data.data.records
    } else if (Array.isArray(response?.data?.data)) {
      accountList = response.data.data
    }
    allAccounts.value = accountList
    
    const savedAccountId = localStorage.getItem('selectedAccountId')
    if (savedAccountId) {
      selectedAccountId.value = Number(savedAccountId)
      selectedAccount.value = accountList.find(acc => acc.id === Number(savedAccountId))
    } else if (accountList.length > 0 && !selectedAccountId.value) {
      selectedAccountId.value = accountList[0].id
      selectedAccount.value = accountList[0]
      localStorage.setItem('selectedAccountId', accountList[0].id)
    }
  } catch (err) {
    console.error('Failed to load accounts:', err)
    ElMessage.error('加载账户列表失败')
  } finally {
    loadingAccounts.value = false
  }
}

const loadTransactions = async () => {
  if (!selectedAccountId.value) {
    transactions.value = []
    return
  }
  loadingTransactions.value = true
  try {
    const params = { accountId: selectedAccountId.value, page: 1, size: 50 }
    const response = await getTransactions(params)
    let records = []
    if (response?.data?.data?.records) {
      records = response.data.data.records
    } else if (response?.data?.data) {
      records = response.data.data
    }
    transactions.value = records
  } catch (err) {
    console.error('Failed to load transactions:', err)
    transactions.value = []
  } finally {
    loadingTransactions.value = false
  }
}

const getCurrentDateTime = () => {
  const now = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}T${pad(now.getHours())}:${pad(now.getMinutes())}`
}

const openTransactionModal = (type = 'EXPENSE') => {
  transactionForm.value = {
    type: type,
    amount: '',
    category: type === 'EXPENSE' ? 'FOOD_LUNCH' : '',
    description: type === 'EXPENSE' ? '午餐' : '',
    tradeTime: getCurrentDateTime(),
    remark: '',
    tags: '',
    targetAccountId: type === 'TRANSFER' && targetAccountOptions.value.length > 0 ? targetAccountOptions.value[0].id : null
  }
  if (type === 'EXPENSE') expenseParentCategory.value = '餐饮'
  showTransactionModal.value = true
}

const onExpenseParentChange = () => {
  const categories = currentExpenseCategories.value
  if (categories.length > 0) {
    transactionForm.value.category = categories[0].value
    transactionForm.value.description = categories[0].label
  }
}

const onExpenseCategoryChange = () => {
  const cat = categoryMap.value[transactionForm.value.category]
  if (cat) transactionForm.value.description = cat.label
}

const handleSubmitTransaction = async () => {
  const amount = parseFloat(transactionForm.value.amount)
  if (!amount || amount <= 0) {
    ElMessage.warning('请输入正确的金额')
    return
  }
  if (!selectedAccountId.value) {
    ElMessage.warning('请先选择一个账户')
    return
  }
  if (transactionForm.value.type === 'TRANSFER' && !transactionForm.value.targetAccountId) {
    ElMessage.warning('请选择目标账户')
    return
  }

  if (transactionForm.value.type === 'EXPENSE') {
    const currentBalance = selectedAccount.value ? selectedAccount.value.balance : 0
    if (amount > currentBalance) {
      ElMessageBox.alert(
        `您的账户余额为 ${formatBalance(currentBalance)}，不足以支付这笔 ${formatBalance(amount)} 的支出。\n\n建议：\n1. 充值账户\n2. 选择余额充足的账户\n3. 减少支出金额`,
        '余额不足',
        { confirmButtonText: '我知道了', type: 'warning' }
      )
      return
    }
  }

  submittingTransaction.value = true
  try {
    const data = {
      accountId: selectedAccountId.value,
      type: transactionForm.value.type,
      amount: amount,
      category: transactionForm.value.category || null,
      description: transactionForm.value.description || '',
      tradeTime: transactionForm.value.tradeTime,
      remark: transactionForm.value.remark || '',
      tags: transactionForm.value.tags || '',
      targetAccountId: transactionForm.value.targetAccountId || null
    }
    await createTransaction(data)
    ElMessage.success(`${currentTransactionType.value.label}记录已添加`)
    showTransactionModal.value = false
    await loadTransactions()
    await loadAccounts()
  } catch (err) {
    console.error('Failed to add transaction:', err)
    ElMessage.error(err.response?.data?.message || err.response?.data?.msg || err.message || '添加交易失败，请重试')
  } finally {
    submittingTransaction.value = false
  }
}

const handleDeleteTransaction = async (transaction) => {
  try {
    await ElMessageBox.confirm('确定要删除这笔交易记录吗？', '删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteTransaction(transaction.id)
    ElMessage.success('删除成功')
    await loadTransactions()
    await loadAccounts()
  } catch (err) {
    if (err !== 'cancel') {
      console.error('Failed to delete:', err)
      ElMessage.error(err.response?.data?.message || '删除失败，请重试')
    }
  }
}

// Computed
const stats = computed(() => {
  let expense = 0, income = 0, transfer = 0
  transactions.value.forEach(t => {
    const amount = parseFloat(t.amount || 0)
    const type = t.transactionType
    if (type === 'EXPENSE' || type === 'expense') expense += amount
    else if (type === 'INCOME' || type === 'income') income += amount
    else if (type === 'TRANSFER' || type === 'transfer') transfer += amount
  })
  return { expense, income, transfer, count: transactions.value.length }
})

const filteredTransactions = computed(() => {
  if (selectedTransactionType.value === 'ALL') return transactions.value
  return transactions.value.filter(t => t.transactionType === selectedTransactionType.value || t.transactionType?.toString() === selectedTransactionType.value)
})

// Watch
watch(selectedAccountId, (newId) => {
  if (newId) {
    localStorage.setItem('selectedAccountId', newId)
    selectedAccount.value = allAccounts.value.find(acc => acc.id === Number(newId))
    loadTransactions()
    loadAccounts()
  }
})

watch(() => transactionForm.value.type, (newType) => {
  if (newType === 'EXPENSE') {
    expenseParentCategory.value = '餐饮'
    transactionForm.value.category = 'FOOD_LUNCH'
    transactionForm.value.description = '午餐'
    transactionForm.value.targetAccountId = null
  } else if (newType === 'TRANSFER') {
    transactionForm.value.category = ''
    transactionForm.value.description = ''
    if (targetAccountOptions.value.length > 0) {
      transactionForm.value.targetAccountId = targetAccountOptions.value[0].id
    }
  } else {
    transactionForm.value.category = ''
    transactionForm.value.description = ''
    transactionForm.value.targetAccountId = null
  }
})

onMounted(async () => {
  if (!authApi.isAuthenticated()) {
    router.replace('/login')
    return
  }
  await loadAccounts()
  if (selectedAccountId.value) await loadTransactions()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Header -->
    <header class="bg-white border-b border-gray-200 sticky top-0 z-30">
      <div class="flex items-center justify-between px-6 py-4">
        <div class="flex items-center space-x-3">
          <button @click="goBack" class="text-gray-600 hover:text-primary-600 transition-colors">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
            </svg>
          </button>
          <div>
            <span class="text-xl font-bold text-gray-900 block">Piggy</span>
            <span class="text-xs text-blue-600 font-medium block -mt-1">交易服务</span>
          </div>
        </div>
        <button @click="handleLogout" class="flex items-center space-x-2 text-gray-600 hover:text-red-600 transition-colors">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path>
          </svg>
          <span class="text-sm font-medium">退出登录</span>
        </button>
      </div>
    </header>

    <!-- Account Selector -->
    <div class="bg-white border-b border-gray-200 py-4 px-6">
      <div class="max-w-6xl mx-auto">
        <div class="flex items-center space-x-4">
          <label class="text-sm font-medium text-gray-700">选择账户：</label>
          <select v-model="selectedAccountId" class="border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary-500 min-w-[200px]">
            <option v-if="allAccounts.length === 0" value="">暂无账户</option>
            <option v-for="account in allAccounts" :key="account.id" :value="account.id">{{ account.accountName }} - {{ formatBalance(account.balance) }}</option>
          </select>
          <span v-if="selectedAccount" class="text-sm text-gray-500">余额: <span class="font-medium text-blue-600">{{ formatBalance(selectedAccount.balance) }}</span></span>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <main class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Stats -->
      <div class="grid grid-cols-3 gap-4 mb-6">
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-4">
          <div class="flex items-center space-x-2 mb-1"><span class="text-lg">💸</span><span class="text-sm text-gray-500">总支出</span></div>
          <div class="text-xl font-bold text-red-600">{{ formatBalance(stats.expense) }}</div>
        </div>
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-4">
          <div class="flex items-center space-x-2 mb-1"><span class="text-lg">💰</span><span class="text-sm text-gray-500">总收入</span></div>
          <div class="text-xl font-bold text-green-600">{{ formatBalance(stats.income) }}</div>
        </div>
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-4">
          <div class="flex items-center space-x-2 mb-1"><span class="text-lg">🔄</span><span class="text-sm text-gray-500">转账</span></div>
          <div class="text-xl font-bold text-blue-600">{{ formatBalance(stats.transfer) }}</div>
        </div>
      </div>

      <!-- Actions -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 mb-6">
        <div class="flex items-center justify-between flex-wrap gap-4">
          <h2 class="text-lg font-semibold text-gray-900">交易操作</h2>
          <div class="flex space-x-3">
            <button @click="openTransactionModal('EXPENSE')" class="btn-primary px-4 py-2 flex items-center bg-red-600 hover:bg-red-700"><span class="mr-2">💸</span>记支出</button>
            <button @click="openTransactionModal('INCOME')" class="btn-primary px-4 py-2 flex items-center bg-green-600 hover:bg-green-700"><span class="mr-2">💰</span>记收入</button>
            <button @click="openTransactionModal('TRANSFER')" class="btn-primary px-4 py-2 flex items-center bg-blue-600 hover:bg-blue-700" :disabled="targetAccountOptions.length === 0"><span class="mr-2">🔄</span>转账</button>
          </div>
        </div>
      </div>

      <!-- Filter -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-4 mb-6">
        <div class="flex items-center space-x-2">
          <span class="text-sm text-gray-600">筛选：</span>
          <button v-for="type in [{value:'ALL',label:'全部'},{value:'EXPENSE',label:'支出'},{value:'INCOME',label:'收入'},{value:'TRANSFER',label:'转账'}]" :key="type.value" @click="selectedTransactionType = type.value" :class="['px-3 py-1 rounded-full text-sm transition-colors', selectedTransactionType === type.value ? 'bg-primary-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200']">{{ type.label }}</button>
        </div>
      </div>

      <!-- List -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-lg font-semibold text-gray-900">交易记录</h2>
          <span class="text-sm text-gray-500">{{ filteredTransactions.length }} 笔</span>
        </div>

        <div v-if="loadingTransactions" class="flex justify-center items-center py-12">
          <div class="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-primary-600"></div>
        </div>

        <div v-else-if="filteredTransactions.length === 0" class="text-center py-12">
          <svg class="w-16 h-16 text-gray-300 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"></path>
          </svg>
          <p class="text-gray-500">暂无交易记录</p>
        </div>

        <div v-else class="space-y-3">
          <div v-for="transaction in filteredTransactions" :key="transaction.id" class="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:bg-gray-50">
            <div class="flex items-center space-x-4">
              <div :class="getTransactionBgClass(transaction.transactionType)">{{ getTransactionTypeInfo(transaction.transactionType).icon }}</div>
              <div>
                <div class="flex items-center space-x-2">
                  <span class="font-medium text-gray-900">{{ getTransactionLabel(transaction) }}</span>
                  <span :class="['text-xs px-2 py-0.5 rounded', getTransactionBadgeClass(transaction.transactionType)]">{{ getTransactionTypeInfo(transaction.transactionType).label }}</span>
                </div>
                <p class="text-sm text-gray-500 mt-1">{{ transaction.transactionType === 'TRANSFER' || transaction.transactionType === 'transfer' ? `转至账户ID: ${transaction.targetAccountId}` : (transaction.remark || getCategoryInfo(transaction.category)?.label || '-') }}</p>
                <p class="text-xs text-gray-400 mt-1">{{ formatDateTime(transaction.transactionTime || transaction.createTime) }}</p>
              </div>
            </div>
            <div class="flex items-center space-x-4">
              <span :class="['text-lg font-bold', getTransactionAmountClass(transaction.transactionType)]">{{ getTransactionTypeInfo(transaction.transactionType).sign }}{{ formatBalance(transaction.amount) }}</span>
              <button @click="handleDeleteTransaction(transaction)" class="text-gray-400 hover:text-red-600 transition-colors">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- Modal -->
    <div v-if="showTransactionModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-xl shadow-xl w-full max-w-md p-6 max-h-[90vh] overflow-y-auto">
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-xl font-bold text-gray-900">{{ currentTransactionType.icon }} 记{{ currentTransactionType.label }}</h3>
          <button @click="showTransactionModal = false" class="text-gray-400 hover:text-gray-600">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
        </div>

        <div class="flex space-x-2 mb-6">
          <button v-for="type in transactionTypes" :key="type.value" @click="transactionForm.type = type.value" :class="['flex-1 py-2 rounded-lg text-center transition-colors', transactionForm.type === type.value ? (type.color === 'red' ? 'bg-red-100 text-red-700 border-2 border-red-500' : type.color === 'green' ? 'bg-green-100 text-green-700 border-2 border-green-500' : 'bg-blue-100 text-blue-700 border-2 border-blue-500') : 'bg-gray-100 text-gray-500']">{{ type.icon }} {{ type.label }}</button>
        </div>

        <form @submit.prevent="handleSubmitTransaction" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">金额</label>
            <input v-model="transactionForm.amount" type="number" step="0.01" min="0" placeholder="0.00" class="input-field text-xl" required>
          </div>

          <template v-if="transactionForm.type === 'EXPENSE'">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">大分类</label>
              <select v-model="expenseParentCategory" @change="onExpenseParentChange" class="input-field">
                <option v-for="parent in parentCategories" :key="parent" :value="parent">{{ parent }}</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">分类</label>
              <select v-model="transactionForm.category" @change="onExpenseCategoryChange" class="input-field">
                <option v-for="cat in currentExpenseCategories" :key="cat.value" :value="cat.value">{{ cat.icon }} {{ cat.label }}</option>
              </select>
            </div>
          </template>

          <template v-if="transactionForm.type === 'TRANSFER'">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">目标账户</label>
              <select v-model="transactionForm.targetAccountId" class="input-field" required>
                <option v-if="targetAccountOptions.length === 0" value="">暂无其他账户</option>
                <option v-for="acc in targetAccountOptions" :key="acc.id" :value="acc.id">{{ acc.accountName }} ({{ formatBalance(acc.balance) }})</option>
              </select>
            </div>
          </template>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">时间</label>
            <input v-model="transactionForm.tradeTime" type="datetime-local" class="input-field" required>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">备注</label>
            <input v-model="transactionForm.remark" type="text" placeholder="可选" class="input-field">
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">标签</label>
            <input v-model="transactionForm.tags" type="text" placeholder="可选，多个标签用逗号分隔" class="input-field">
          </div>
          <div class="flex space-x-4 pt-4">
            <button type="button" @click="showTransactionModal = false" class="flex-1 btn-secondary">取消</button>
            <button type="submit" :disabled="submittingTransaction" class="flex-1 btn-primary">{{ submittingTransaction ? '提交中...' : '确定' }}</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
