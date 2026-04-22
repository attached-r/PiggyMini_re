<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/auth'
import { getAccounts } from '@/api/account'
import { getTransactions, createTransaction, deleteTransaction, TransactionTypeLabels, getCategoryInfo } from '@/api/transaction'
import { ElMessage, ElMessageBox } from 'element-plus'
import Sidebar from '@/components/Sidebar.vue'

const router = useRouter()

// 侧边栏状态
const sidebarCollapsed = ref(false)

// 数据
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

// 表单
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

// 交易类型
const transactionTypes = [
  { value: 'EXPENSE', label: '支出', icon: '💸', color: 'red' },
  { value: 'INCOME', label: '收入', icon: '💰', color: 'green' },
  { value: 'TRANSFER', label: '转账', icon: '🔄', color: 'blue' }
]

// 分类数据
const expenseCategoriesByParent = {
  '餐饮': ['FOOD_BREAKFAST', 'FOOD_LUNCH', 'FOOD_DINNER', 'FOOD_SNACK', 'FOOD_FRUIT', 'FOOD_DRINK', 'FOOD_GROCERY', 'FOOD_TREAT', 'FOOD_DELIVERY'],
  '交通': ['TRANSPORT_SUBWAY', 'TRANSPORT_BUS', 'TRANSPORT_TAXI', 'TRANSPORT_DIDI', 'TRANSPORT_FUEL', 'TRANSPORT_PARKING', 'TRANSPORT_TOLL', 'TRANSPORT_BIKE', 'TRANSPORT_TRAIN', 'TRANSPORT_FLIGHT'],
  '购物': ['SHOP_CLOTHES', 'SHOP_SHOES', 'SHOP_BAG', 'SHOP_COSMETIC', 'SHOP_DIGITAL', 'SHOP_HOME', 'SHOP_DAILY', 'SHOP_BOOK', 'SHOP_GIFT'],
  '居住': ['LIVING_RENT', 'LIVING_MORTGAGE', 'LIVING_PROPERTY', 'LIVING_WATER', 'LIVING_ELECTRIC', 'LIVING_GAS', 'LIVING_INTERNET', 'LIVING_MAINTENANCE'],
  '娱乐': ['ENTERTAIN_MOVIE', 'ENTERTAIN_GAME', 'ENTERTAIN_KTV', 'ENTERTAIN_TRAVEL', 'ENTERTAIN_SPORT', 'ENTERTAIN_GYM', 'ENTERTAIN_STREAM'],
  '医疗': ['MEDICAL_HOSPITAL', 'MEDICAL_MEDICINE', 'MEDICAL_DENTAL', 'MEDICAL_CHECKUP', 'MEDICAL_INSURANCE'],
  '教育': ['EDUCATION_TUITION', 'EDUCATION_COURSE', 'EDUCATION_EXAM', 'EDUCATION_MATERIAL'],
  '社交': ['SOCIAL_RED_PACKET', 'SOCIAL_DONATION', 'SOCIAL_TIP'],
  '其他': ['OTHER_MISC', 'OTHER_LOSS', 'OTHER_PENALTY']
}

const parentCategories = Object.keys(expenseCategoriesByParent)

const currentExpenseCategories = computed(() => {
  return expenseCategoriesByParent[expenseParentCategory.value] || []
})

const targetAccountOptions = computed(() => {
  return allAccounts.value.filter(acc => acc.id !== selectedAccountId.value)
})

const currentTransactionType = computed(() => {
  return transactionTypes.find(t => t.value === transactionForm.value.type) || transactionTypes[0]
})

// 方法
const formatBalance = (balance) => {
  return new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(balance || 0)
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

const getCategoryLabel = (category) => {
  const info = getCategoryInfo(category)
  return info.label || category
}

const getCategoryIcon = (category) => {
  const info = getCategoryInfo(category)
  return info.icon || '📦'
}

const getTransactionTypeInfo = (type) => {
  return TransactionTypeLabels[type] || { label: type, icon: '📦', sign: '', color: 'gray' }
}

const getTransactionBgClass = (type) => {
  const info = getTransactionTypeInfo(type)
  const colorClass = info.color === 'red' ? 'bg-rose-500/20' : info.color === 'green' ? 'bg-emerald-500/20' : 'bg-blue-500/20'
  return ['w-12 h-12 rounded-full flex items-center justify-center text-2xl', colorClass]
}

const getTransactionBadgeClass = (type) => {
  const info = getTransactionTypeInfo(type)
  return info.color === 'red' ? 'bg-rose-500/20 text-rose-400' : info.color === 'green' ? 'bg-emerald-500/20 text-emerald-400' : 'bg-blue-500/20 text-blue-400'
}

const getTransactionAmountClass = (type) => {
  const info = getTransactionTypeInfo(type)
  return info.color === 'red' ? 'text-rose-400' : info.color === 'green' ? 'text-emerald-400' : 'text-blue-400'
}

const getTransactionLabel = (t) => {
  const info = getTransactionTypeInfo(t.transactionType)
  if (t.transactionType === 'EXPENSE') {
    return getCategoryLabel(t.category) || '支出'
  } else if (t.transactionType === 'INCOME') {
    return '收入'
  } else {
    return '转账'
  }
}

// 加载账户
const loadAccounts = async () => {
  loadingAccounts.value = true
  try {
    const response = await getAccounts(1, 100)
    let accountList = []
    if (response.data?.records) {
      accountList = response.data.records
    } else if (Array.isArray(response.data)) {
      accountList = response.data
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
    console.error('加载账户列表失败:', err)
    ElMessage.error('加载账户列表失败')
  } finally {
    loadingAccounts.value = false
  }
}

// 加载交易记录
const loadTransactions = async () => {
  if (!selectedAccountId.value) {
    transactions.value = []
    return
  }
  loadingTransactions.value = true
  try {
    const params = { 
      accountId: selectedAccountId.value, 
      page: 1, 
      size: 50 
    }
    const response = await getTransactions(params)
    let records = []
    if (response.data?.records) {
      records = response.data.records
    } else if (Array.isArray(response.data)) {
      records = response.data
    }
    transactions.value = records
  } catch (err) {
    console.error('加载交易记录失败:', err)
    transactions.value = []
  } finally {
    loadingTransactions.value = false
  }
}

const getCurrentDateTime = () => {
  const now = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}T${pad(now.getHours())}:${pad(now.getMinutes())}:00`
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
    transactionForm.value.category = categories[0]
    const info = getCategoryInfo(categories[0])
    transactionForm.value.description = info.label
  }
}

const onExpenseCategoryChange = () => {
  const info = getCategoryInfo(transactionForm.value.category)
  if (info.label) transactionForm.value.description = info.label
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
        `您的账户余额为 ${formatBalance(currentBalance)}，不足以支付这笔 ${formatBalance(amount)} 的支出。`,
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
    console.error('添加交易失败:', err)
    ElMessage.error(err.message || '添加交易失败，请重试')
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
      console.error('删除失败:', err)
      ElMessage.error(err.message || '删除失败，请重试')
    }
  }
}

// 计算统计数据
const stats = computed(() => {
  let expense = 0, income = 0, transfer = 0
  transactions.value.forEach(t => {
    const amount = parseFloat(t.amount || 0)
    if (t.transactionType === 'EXPENSE') expense += amount
    else if (t.transactionType === 'INCOME') income += amount
    else if (t.transactionType === 'TRANSFER') transfer += amount
  })
  return { expense, income, transfer, count: transactions.value.length }
})

const filteredTransactions = computed(() => {
  if (selectedTransactionType.value === 'ALL') return transactions.value
  return transactions.value.filter(t => t.transactionType === selectedTransactionType.value)
})

// 监听账户变化
watch(selectedAccountId, (newId) => {
  if (newId) {
    localStorage.setItem('selectedAccountId', newId)
    selectedAccount.value = allAccounts.value.find(acc => acc.id === Number(newId))
    loadTransactions()
  }
})

watch(() => transactionForm.value.type, (newType) => {
  if (newType === 'EXPENSE') {
    expenseParentCategory.value = '餐饮'
    transactionForm.value.category = 'FOOD_LUNCH'
    const info = getCategoryInfo('FOOD_LUNCH')
    transactionForm.value.description = info.label
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
        <div class="max-w-7xl mx-auto px-6 py-4">
          <div class="flex items-center justify-between">
            <div>
              <h1 class="text-2xl font-bold text-white">交易记录</h1>
              <p class="text-slate-400 text-sm">管理您的收支和转账</p>
            </div>
            <div class="flex items-center space-x-4">
              <!-- 账户选择 -->
              <select v-model="selectedAccountId" class="bg-slate-800/50 border border-slate-600/50 text-slate-200 rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 min-w-[200px]">
                <option v-if="allAccounts.length === 0" value="">暂无账户</option>
                <option v-for="account in allAccounts" :key="account.id" :value="account.id">
                  {{ account.icon || '' }} {{ account.accountName }} - {{ formatBalance(account.balance) }}
                </option>
              </select>
            </div>
          </div>
        </div>
      </header>

      <!-- Main Content -->
      <main class="max-w-7xl mx-auto px-6 py-8">
        <!-- Stats -->
        <div class="grid grid-cols-3 gap-4 mb-6">
          <div class="bg-slate-800/50 border border-slate-700/50 rounded-2xl p-4">
            <div class="flex items-center space-x-2 mb-1">
              <span class="text-lg">💸</span>
              <span class="text-sm text-slate-400">总支出</span>
            </div>
            <div class="text-xl font-bold text-rose-400">{{ formatBalance(stats.expense) }}</div>
          </div>
          <div class="bg-slate-800/50 border border-slate-700/50 rounded-2xl p-4">
            <div class="flex items-center space-x-2 mb-1">
              <span class="text-lg">💰</span>
              <span class="text-sm text-slate-400">总收入</span>
            </div>
            <div class="text-xl font-bold text-emerald-400">{{ formatBalance(stats.income) }}</div>
          </div>
          <div class="bg-slate-800/50 border border-slate-700/50 rounded-2xl p-4">
            <div class="flex items-center space-x-2 mb-1">
              <span class="text-lg">🔄</span>
              <span class="text-sm text-slate-400">转账</span>
            </div>
            <div class="text-xl font-bold text-blue-400">{{ formatBalance(stats.transfer) }}</div>
          </div>
        </div>

        <!-- Actions -->
        <div class="bg-slate-800/50 border border-slate-700/50 rounded-2xl p-6 mb-6">
          <div class="flex items-center justify-between flex-wrap gap-4">
            <h2 class="text-lg font-semibold text-white">交易操作</h2>
            <div class="flex space-x-3">
              <button 
                @click="openTransactionModal('EXPENSE')" 
                class="px-4 py-2 flex items-center bg-rose-600 hover:bg-rose-700 text-white rounded-lg transition-colors"
              >
                <span class="mr-2">💸</span>记支出
              </button>
              <button 
                @click="openTransactionModal('INCOME')" 
                class="px-4 py-2 flex items-center bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg transition-colors"
              >
                <span class="mr-2">💰</span>记收入
              </button>
              <button 
                @click="openTransactionModal('TRANSFER')" 
                class="px-4 py-2 flex items-center bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                :disabled="targetAccountOptions.length === 0"
              >
                <span class="mr-2">🔄</span>转账
              </button>
            </div>
          </div>
        </div>

        <!-- Filter -->
        <div class="bg-slate-800/50 border border-slate-700/50 rounded-2xl p-4 mb-6">
          <div class="flex items-center space-x-2">
            <span class="text-sm text-slate-400">筛选：</span>
            <button 
              v-for="type in [{value:'ALL',label:'全部'},{value:'EXPENSE',label:'支出'},{value:'INCOME',label:'收入'},{value:'TRANSFER',label:'转账'}]" 
              :key="type.value" 
              @click="selectedTransactionType = type.value" 
              :class="[
                'px-3 py-1 rounded-full text-sm transition-colors',
                selectedTransactionType === type.value ? 'bg-indigo-600 text-white' : 'bg-slate-700/50 text-slate-300 hover:bg-slate-600/50'
              ]"
            >
              {{ type.label }}
            </button>
          </div>
        </div>

        <!-- List -->
        <div class="bg-slate-800/50 border border-slate-700/50 rounded-2xl p-6">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-lg font-semibold text-white">交易记录</h2>
            <span class="text-sm text-slate-400">{{ filteredTransactions.length }} 笔</span>
          </div>

          <div v-if="loadingTransactions" class="flex justify-center items-center py-12">
            <div class="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-indigo-600"></div>
          </div>

          <div v-else-if="filteredTransactions.length === 0" class="text-center py-12">
            <div class="w-16 h-16 bg-slate-700/50 rounded-full flex items-center justify-center mx-auto mb-4">
              <span class="text-3xl">📝</span>
            </div>
            <p class="text-slate-400">暂无交易记录</p>
            <p class="text-slate-500 text-sm mt-1">点击上方按钮开始记账</p>
          </div>

          <div v-else class="space-y-3">
            <div v-for="transaction in filteredTransactions" :key="transaction.id" class="flex items-center justify-between p-4 bg-slate-900/50 rounded-xl hover:bg-slate-900/70 transition-colors">
              <div class="flex items-center space-x-4">
                <div :class="getTransactionBgClass(transaction.transactionType)">
                  {{ getTransactionTypeInfo(transaction.transactionType).icon }}
                </div>
                <div>
                  <div class="flex items-center space-x-2">
                    <span class="font-medium text-white">{{ getTransactionLabel(transaction) }}</span>
                    <span :class="['text-xs px-2 py-0.5 rounded', getTransactionBadgeClass(transaction.transactionType)]">
                      {{ getTransactionTypeInfo(transaction.transactionType).label }}
                    </span>
                  </div>
                  <p class="text-sm text-slate-400 mt-1">
                    {{ transaction.transactionType === 'TRANSFER' ? `转至账户ID: ${transaction.targetAccountId}` : (transaction.remark || getCategoryLabel(transaction.category) || '-') }}
                  </p>
                  <p class="text-xs text-slate-500 mt-1">{{ formatDateTime(transaction.transactionTime || transaction.createTime) }}</p>
                </div>
              </div>
              <div class="flex items-center space-x-4">
                <span :class="['text-lg font-bold', getTransactionAmountClass(transaction.transactionType)]">
                  {{ getTransactionTypeInfo(transaction.transactionType).sign }}{{ formatBalance(transaction.amount) }}
                </span>
                <button @click="handleDeleteTransaction(transaction)" class="text-slate-400 hover:text-rose-400 transition-colors">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                  </svg>
                </button>
              </div>
            </div>
          </div>
        </div>
      </main>
    </main>

    <!-- Modal -->
    <div v-if="showTransactionModal" class="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50">
      <div class="bg-slate-800 border border-slate-700 rounded-2xl shadow-xl w-full max-w-md p-6 max-h-[90vh] overflow-y-auto">
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-xl font-bold text-white">
            {{ currentTransactionType.icon }} 记{{ currentTransactionType.label }}
          </h3>
          <button @click="showTransactionModal = false" class="text-slate-400 hover:text-white">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
        </div>

        <div class="flex space-x-2 mb-6">
          <button 
            v-for="type in transactionTypes" 
            :key="type.value" 
            @click="transactionForm.type = type.value" 
            :class="[
              'flex-1 py-2 rounded-lg text-center transition-colors',
              transactionForm.type === type.value 
                ? (type.color === 'red' ? 'bg-rose-500/20 text-rose-400 border-2 border-rose-500' 
                  : type.color === 'green' ? 'bg-emerald-500/20 text-emerald-400 border-2 border-emerald-500' 
                  : 'bg-blue-500/20 text-blue-400 border-2 border-blue-500')
                : 'bg-slate-700/50 text-slate-400'
            ]"
          >
            {{ type.icon }} {{ type.label }}
          </button>
        </div>

        <form @submit.prevent="handleSubmitTransaction" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-slate-300 mb-2">金额</label>
            <input 
              v-model="transactionForm.amount" 
              type="number" 
              step="0.01" 
              min="0" 
              placeholder="0.00" 
              class="w-full px-4 py-3 bg-slate-900/50 border border-slate-600/50 rounded-lg text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500" 
            />
          </div>

          <!-- 支出分类选择 -->
          <div v-if="transactionForm.type === 'EXPENSE'">
            <label class="block text-sm font-medium text-slate-300 mb-2">支出分类</label>
            <select 
              v-model="expenseParentCategory" 
              @change="onExpenseParentChange"
              class="w-full px-4 py-3 bg-slate-900/50 border border-slate-600/50 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-indigo-500 mb-2"
            >
              <option v-for="parent in parentCategories" :key="parent" :value="parent">{{ parent }}</option>
            </select>
            <select 
              v-model="transactionForm.category"
              @change="onExpenseCategoryChange"
              class="w-full px-4 py-3 bg-slate-900/50 border border-slate-600/50 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
            >
              <option v-for="cat in currentExpenseCategories" :key="cat" :value="cat">
                {{ getCategoryIcon(cat) }} {{ getCategoryLabel(cat) }}
              </option>
            </select>
          </div>

          <!-- 转账目标账户 -->
          <div v-if="transactionForm.type === 'TRANSFER'">
            <label class="block text-sm font-medium text-slate-300 mb-2">目标账户</label>
            <select 
              v-model="transactionForm.targetAccountId"
              class="w-full px-4 py-3 bg-slate-900/50 border border-slate-600/50 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
            >
              <option v-for="acc in targetAccountOptions" :key="acc.id" :value="acc.id">
                {{ acc.icon || '' }} {{ acc.accountName }}
              </option>
            </select>
          </div>

          <div>
            <label class="block text-sm font-medium text-slate-300 mb-2">时间</label>
            <input 
              v-model="transactionForm.tradeTime" 
              type="datetime-local" 
              class="w-full px-4 py-3 bg-slate-900/50 border border-slate-600/50 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-indigo-500" 
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-slate-300 mb-2">备注</label>
            <input 
              v-model="transactionForm.remark" 
              type="text" 
              placeholder="可选" 
              class="w-full px-4 py-3 bg-slate-900/50 border border-slate-600/50 rounded-lg text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500" 
            />
          </div>

          <div class="flex space-x-3 pt-4">
            <button 
              type="button" 
              @click="showTransactionModal = false"
              class="flex-1 py-3 bg-slate-700 hover:bg-slate-600 text-white rounded-lg transition-colors"
            >
              取消
            </button>
            <button 
              type="submit" 
              :disabled="submittingTransaction"
              :class="[
                'flex-1 py-3 rounded-lg transition-colors',
                transactionForm.type === 'EXPENSE' ? 'bg-rose-600 hover:bg-rose-700 text-white' :
                transactionForm.type === 'INCOME' ? 'bg-emerald-600 hover:bg-emerald-700 text-white' :
                'bg-blue-600 hover:bg-blue-700 text-white',
                submittingTransaction ? 'opacity-50 cursor-not-allowed' : ''
              ]"
            >
              {{ submittingTransaction ? '提交中...' : '确认' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
