<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/auth'
import { getAccounts, AccountTypeLabels } from '@/api/account'
import { getSummary, getCategoryReport } from '@/api/report'
import { ElMessage } from 'element-plus'
import Sidebar from '@/components/Sidebar.vue'

const router = useRouter()

// 侧边栏状态
const sidebarCollapsed = ref(false)

// 数据状态
const loading = ref(true)
const accounts = ref([])
const selectedAccountId = ref(null)
const selectedAccount = ref(null)

// 报表数据
const monthlySummary = ref({ income: 0, expense: 0, balance: 0 })
const categoryReport = ref([])
const loadingReport = ref(false)

// 分类标签映射
const categoryLabels = {
  'FOOD_BREAKFAST': { label: '早餐', icon: '🥐', color: '#EF4444' },
  'FOOD_LUNCH': { label: '午餐', icon: '🍱', color: '#EF4444' },
  'FOOD_DINNER': { label: '晚餐', icon: '🍜', color: '#EF4444' },
  'FOOD_SNACK': { label: '零食', icon: '🍿', color: '#EF4444' },
  'FOOD_FRUIT': { label: '水果', icon: '🍎', color: '#EF4444' },
  'FOOD_DRINK': { label: '饮品', icon: '🥤', color: '#EF4444' },
  'FOOD_GROCERY': { label: '买菜', icon: '🥬', color: '#EF4444' },
  'FOOD_TREAT': { label: '聚餐', icon: '🍻', color: '#EF4444' },
  'FOOD_DELIVERY': { label: '外卖', icon: '🛵', color: '#EF4444' },
  'TRANSPORT_SUBWAY': { label: '地铁', icon: '🚇', color: '#F59E0B' },
  'TRANSPORT_BUS': { label: '公交', icon: '🚌', color: '#F59E0B' },
  'TRANSPORT_TAXI': { label: '打车', icon: '🚕', color: '#F59E0B' },
  'TRANSPORT_DIDI': { label: '网约车', icon: '🚗', color: '#F59E0B' },
  'TRANSPORT_FUEL': { label: '加油', icon: '⛽', color: '#F59E0B' },
  'SHOP_CLOTHES': { label: '服饰', icon: '👔', color: '#10B981' },
  'SHOP_DIGITAL': { label: '数码', icon: '💻', color: '#10B981' },
  'SHOP_DAILY': { label: '日用品', icon: '🧴', color: '#10B981' },
  'LIVING_RENT': { label: '房租', icon: '🏢', color: '#3B82F6' },
  'LIVING_ELECTRIC': { label: '电费', icon: '⚡', color: '#3B82F6' },
  'LIVING_WATER': { label: '水费', icon: '💧', color: '#3B82F6' },
  'ENTERTAIN_MOVIE': { label: '电影', icon: '🎬', color: '#8B5CF6' },
  'ENTERTAIN_GAME': { label: '游戏', icon: '🎮', color: '#8B5CF6' },
  'ENTERTAIN_TRAVEL': { label: '旅游', icon: '🏖️', color: '#8B5CF6' },
  'MEDICAL_MEDICINE': { label: '买药', icon: '💊', color: '#EC4899' },
  'EDUCATION_TUITION': { label: '学费', icon: '🎓', color: '#6366F1' },
  'OTHER_MISC': { label: '其他', icon: '📦', color: '#94A3B8' }
}

// 计算当前月份
const currentMonth = computed(() => {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
})

// 问候语
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

// 获取用户名
const username = computed(() => {
  return authApi.getNickname() || authApi.getUsername() || '用户'
})

// 格式化金额
const formatBalance = (balance) => {
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    minimumFractionDigits: 2
  }).format(balance || 0)
}

// 获取分类信息
const getCategoryInfo = (category) => {
  return categoryLabels[category] || { label: category, icon: '📦', color: '#94A3B8' }
}

// 获取分类颜色（按支出类型分组）
const getCategoryColor = (index) => {
  const colors = ['#EF4444', '#F59E0B', '#10B981', '#3B82F6', '#8B5CF6', '#EC4899', '#6366F1', '#14B8A6', '#F97316', '#84CC16']
  return colors[index % colors.length]
}

// 加载账户列表
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
    
    // 恢复上次选择的账户
    const savedAccountId = localStorage.getItem('selectedAccountId')
    if (savedAccountId) {
      selectedAccountId.value = Number(savedAccountId)
      selectedAccount.value = accountList.find(acc => acc.id === Number(savedAccountId))
    } else if (accountList.length > 0) {
      selectedAccountId.value = accountList[0].id
      selectedAccount.value = accountList[0]
      localStorage.setItem('selectedAccountId', accountList[0].id)
    }
  } catch (err) {
    console.error('加载账户失败:', err)
    ElMessage.error('加载账户列表失败')
  } finally {
    loading.value = false
  }
}

// 加载报表数据
const loadReportData = async () => {
  if (!selectedAccountId.value) return
  
  loadingReport.value = true
  try {
    // 加载月度汇总
    const summaryResponse = await getSummary('month', currentMonth.value)
    const summaryData = summaryResponse?.data?.data ?? summaryResponse?.data ?? {}
    if (summaryData) {
      monthlySummary.value = {
        income: summaryData.income || 0,
        expense: summaryData.expense || 0,
        balance: summaryData.balance || selectedAccount.value?.balance || 0
      }
    }
    
    // 加载分类报表
    const categoryResponse = await getCategoryReport('month', currentMonth.value)
    const categoryData = categoryResponse?.data?.data ?? categoryResponse?.data ?? []
    if (Array.isArray(categoryData)) {
      categoryReport.value = categoryData.slice(0, 5)
    }
  } catch (err) {
    console.error('加载报表失败:', err)
  } finally {
    loadingReport.value = false
  }
}

// 监听账户变化
const onAccountChange = () => {
  if (selectedAccountId.value) {
    localStorage.setItem('selectedAccountId', selectedAccountId.value)
    selectedAccount.value = accounts.value.find(acc => acc.id === Number(selectedAccountId.value))
    loadReportData()
  }
}

onMounted(async () => {
  if (!authApi.isAuthenticated()) {
    router.replace('/login')
    return
  }
  await loadAccounts()
  if (selectedAccountId.value) {
    await loadReportData()
  }
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
              <h1 class="text-2xl font-bold text-white">{{ greeting }}，{{ username }}</h1>
              <p class="text-slate-400 text-sm">欢迎回到您的个人财务中心</p>
            </div>
            
            <div class="flex items-center space-x-4">
              <!-- 账户选择 -->
              <select 
                v-model="selectedAccountId"
                @change="onAccountChange"
                class="bg-slate-800/50 border border-slate-600/50 text-slate-200 rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              >
                <option v-for="acc in accounts" :key="acc.id" :value="acc.id">
                  {{ acc.accountName }} - {{ formatBalance(acc.balance) }}
                </option>
              </select>
            </div>
          </div>
        </div>
      </header>

      <!-- 页面内容 -->
      <div class="max-w-7xl mx-auto px-6 py-8">
        <!-- 加载状态 -->
        <div v-if="loading" class="flex justify-center items-center py-20">
          <div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-indigo-500"></div>
        </div>

        <template v-else>
          <!-- 账户余额卡片 -->
          <div class="bg-gradient-to-br from-indigo-600 to-purple-600 rounded-2xl p-8 mb-8 shadow-xl shadow-indigo-500/20">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-indigo-200 text-sm mb-2">当前账户余额</p>
                <p class="text-4xl font-bold text-white">{{ formatBalance(selectedAccount?.balance || 0) }}</p>
                <p class="text-indigo-200 text-sm mt-2">{{ selectedAccount?.accountName || '未选择账户' }}</p>
              </div>
              <div class="flex space-x-4">
                <div class="text-center px-4">
                  <p class="text-indigo-200 text-xs mb-1">本月收入</p>
                  <p class="text-xl font-bold text-emerald-400">+{{ formatBalance(monthlySummary.income) }}</p>
                </div>
                <div class="text-center px-4 border-l border-indigo-400/30">
                  <p class="text-indigo-200 text-xs mb-1">本月支出</p>
                  <p class="text-xl font-bold text-rose-400">-{{ formatBalance(monthlySummary.expense) }}</p>
                </div>
              </div>
            </div>
          </div>

          <!-- 快捷操作 -->
          <div class="grid grid-cols-1 md:grid-cols-5 gap-4 mb-8">
            <button 
              @click="router.push('/transactions')"
              class="bg-slate-800/50 border border-slate-700/50 rounded-xl p-6 hover:bg-slate-700/50 hover:border-slate-600/50 transition-all group"
            >
              <div class="w-12 h-12 bg-rose-500/20 rounded-xl flex items-center justify-center mb-4 group-hover:bg-rose-500/30 transition-colors">
                <span class="text-2xl">💸</span>
              </div>
              <h3 class="text-white font-semibold mb-1">记支出</h3>
              <p class="text-slate-400 text-sm">记录日常支出</p>
            </button>
            
            <button 
              @click="router.push('/transactions')"
              class="bg-slate-800/50 border border-slate-700/50 rounded-xl p-6 hover:bg-slate-700/50 hover:border-slate-600/50 transition-all group"
            >
              <div class="w-12 h-12 bg-emerald-500/20 rounded-xl flex items-center justify-center mb-4 group-hover:bg-emerald-500/30 transition-colors">
                <span class="text-2xl">💰</span>
              </div>
              <h3 class="text-white font-semibold mb-1">记收入</h3>
              <p class="text-slate-400 text-sm">记录各类收入</p>
            </button>
            
            <button 
              @click="router.push('/budgets')"
              class="bg-slate-800/50 border border-slate-700/50 rounded-xl p-6 hover:bg-slate-700/50 hover:border-slate-600/50 transition-all group"
            >
              <div class="w-12 h-12 bg-amber-500/20 rounded-xl flex items-center justify-center mb-4 group-hover:bg-amber-500/30 transition-colors">
                <span class="text-2xl">📊</span>
              </div>
              <h3 class="text-white font-semibold mb-1">做预算</h3>
              <p class="text-slate-400 text-sm">规划月度预算</p>
            </button>
            
            <button 
              @click="router.push('/reports')"
              class="bg-slate-800/50 border border-slate-700/50 rounded-xl p-6 hover:bg-slate-700/50 hover:border-slate-600/50 transition-all group"
            >
              <div class="w-12 h-12 bg-violet-500/20 rounded-xl flex items-center justify-center mb-4 group-hover:bg-violet-500/30 transition-colors">
                <span class="text-2xl">📈</span>
              </div>
              <h3 class="text-white font-semibold mb-1">看报表</h3>
              <p class="text-slate-400 text-sm">分析收支情况</p>
            </button>
            
            <button 
              @click="router.push('/ai')"
              class="bg-slate-800/50 border border-slate-700/50 rounded-xl p-6 hover:bg-slate-700/50 hover:border-slate-600/50 transition-all group"
            >
              <div class="w-12 h-12 bg-pink-500/20 rounded-xl flex items-center justify-center mb-4 group-hover:bg-pink-500/30 transition-colors">
                <span class="text-2xl">🤖</span>
              </div>
              <h3 class="text-white font-semibold mb-1">AI记账</h3>
              <p class="text-slate-400 text-sm">智能分类分析</p>
            </button>
          </div>

          <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
            <!-- 分类支出 -->
            <div class="bg-slate-800/50 border border-slate-700/50 rounded-2xl p-6">
              <div class="flex items-center justify-between mb-6">
                <h3 class="text-lg font-semibold text-white">分类支出</h3>
                <button @click="router.push('/reports')" class="text-indigo-400 text-sm hover:underline">查看全部</button>
              </div>
              
              <div v-if="loadingReport" class="flex justify-center py-8">
                <div class="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-indigo-500"></div>
              </div>
              
              <div v-else-if="categoryReport.length === 0" class="text-center py-8 text-slate-400">
                <p>暂无分类数据</p>
                <p class="text-sm mt-2">开始记账后会显示分类统计</p>
              </div>
              
              <div v-else class="space-y-4">
                <div v-for="(item, index) in categoryReport" :key="item.category" class="flex items-center justify-between p-3 bg-slate-900/50 rounded-xl">
                  <div class="flex items-center space-x-3">
                    <div class="w-10 h-10 rounded-full flex items-center justify-center text-lg" 
                         :style="{ backgroundColor: getCategoryColor(index) + '20' }">
                      {{ getCategoryInfo(item.category).icon }}
                    </div>
                    <div>
                      <p class="text-white font-medium">{{ getCategoryInfo(item.category).label }}</p>
                      <p class="text-slate-400 text-xs">{{ ((item.percent || 0) * 100).toFixed(1) }}%</p>
                    </div>
                  </div>
                  <p class="text-rose-400 font-semibold">{{ formatBalance(item.amount || 0) }}</p>
                </div>
              </div>
            </div>

            <!-- 账户概览 -->
            <div class="bg-slate-800/50 border border-slate-700/50 rounded-2xl p-6">
              <div class="flex items-center justify-between mb-6">
                <h3 class="text-lg font-semibold text-white">账户概览</h3>
                <button @click="router.push('/accounts')" class="text-indigo-400 text-sm hover:underline">管理账户</button>
              </div>
              
              <div class="space-y-4">
                <div v-for="account in accounts.slice(0, 4)" :key="account.id" 
                     class="flex items-center justify-between p-4 bg-slate-900/50 rounded-xl"
                     :class="account.id === selectedAccountId ? 'ring-2 ring-indigo-500/50' : ''">
                  <div class="flex items-center space-x-3">
                    <div class="w-10 h-10 bg-slate-700 rounded-lg flex items-center justify-center">
                      <span class="text-lg">{{ account.icon || '💳' }}</span>
                    </div>
                    <div>
                      <p class="text-white font-medium">{{ account.accountName }}</p>
                      <p class="text-slate-400 text-xs">{{ AccountTypeLabels[account.accountType]?.label || account.accountType }}</p>
                    </div>
                  </div>
                  <p :class="account.balance >= 0 ? 'text-emerald-400' : 'text-rose-400'" class="font-semibold">
                    {{ formatBalance(account.balance) }}
                  </p>
                </div>
                
                <button 
                  v-if="accounts.length > 4"
                  @click="router.push('/accounts')"
                  class="w-full text-center py-3 text-indigo-400 hover:text-indigo-300 text-sm"
                >
                  查看全部 {{ accounts.length }} 个账户 →
                </button>
              </div>
            </div>
          </div>
        </template>
      </div>
    </main>
  </div>
</template>
