<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/auth'
import { getAccounts } from '@/api/account'
import { getSummary, getCategoryReport, getBudgetExecution, getTrend } from '@/api/report'
import { ElMessage } from 'element-plus'
import Sidebar from '@/components/Sidebar.vue'

const router = useRouter()

// 侧边栏状态
const sidebarCollapsed = ref(false)

// Account related
const allAccounts = ref([])
const selectedAccountId = ref(null) // null 表示全部账户
const selectedAccount = ref(null)
const loadingAccounts = ref(false)

// 是否选择全部账户
const isAllAccounts = computed(() => selectedAccountId.value === null)

const selectedPeriod = ref('month')
const currentDate = new Date()

// Helper function to get last day of month
const getLastDayOfMonth = (year, month) => {
  return new Date(year, month, 0).getDate()
}

// Convert yyyy-MM (month picker) to yyyy-MM-dd (backend format)
const monthToDate = (monthStr) => {
  if (!monthStr) return ''
  const [year, month] = monthStr.split('-')
  const lastDay = getLastDayOfMonth(parseInt(year), parseInt(month))
  return `${year}-${month}-${String(lastDay).padStart(2, '0')}`
}

const selectedDate = ref(monthToDate(`${currentDate.getFullYear()}-${String(currentDate.getMonth() + 1).padStart(2, '0')}`))
const selectedMonth = ref(`${currentDate.getFullYear()}-${String(currentDate.getMonth() + 1).padStart(2, '0')}`)

watch(selectedMonth, (newMonth) => {
  selectedDate.value = monthToDate(newMonth)
})

// Trend date range
const trendStartDate = ref(`${currentDate.getFullYear()}-01-01`)
const trendEndDate = ref(monthToDate(`${currentDate.getFullYear()}-${String(currentDate.getMonth() + 1).padStart(2, '0')}`))

// Report data
const reportSummary = ref(null)
const categoryReport = ref([])
const budgetExecution = ref([])
const trendReport = ref(null)
const loadingReport = ref(false)

// Category mapping
const categoryLabels = {
  'FOOD_BREAKFAST': { label: '早餐', icon: '🥐', parent: '餐饮' },
  'FOOD_LUNCH': { label: '午餐', icon: '🍱', parent: '餐饮' },
  'FOOD_DINNER': { label: '晚餐', icon: '🍜', parent: '餐饮' },
  'FOOD_SNACK': { label: '零食', icon: '🍿', parent: '餐饮' },
  'FOOD_FRUIT': { label: '水果', icon: '🍎', parent: '餐饮' },
  'FOOD_DRINK': { label: '饮品', icon: '🥤', parent: '餐饮' },
  'FOOD_GROCERY': { label: '买菜', icon: '🥬', parent: '餐饮' },
  'FOOD_TREAT': { label: '聚餐', icon: '🍻', parent: '餐饮' },
  'FOOD_DELIVERY': { label: '外卖', icon: '🛵', parent: '餐饮' },
  'TRANSPORT_SUBWAY': { label: '地铁', icon: '🚇', parent: '交通' },
  'TRANSPORT_BUS': { label: '公交', icon: '🚌', parent: '交通' },
  'TRANSPORT_TAXI': { label: '打车', icon: '🚕', parent: '交通' },
  'TRANSPORT_DIDI': { label: '网约车', icon: '🚗', parent: '交通' },
  'TRANSPORT_FUEL': { label: '加油', icon: '⛽', parent: '交通' },
  'SHOP_CLOTHES': { label: '服饰', icon: '👔', parent: '购物' },
  'SHOP_DIGITAL': { label: '数码', icon: '💻', parent: '购物' },
  'SHOP_DAILY': { label: '日用品', icon: '🧴', parent: '购物' },
  'LIVING_RENT': { label: '房租', icon: '🏢', parent: '居住' },
  'LIVING_ELECTRIC': { label: '电费', icon: '⚡', parent: '居住' },
  'LIVING_WATER': { label: '水费', icon: '💧', parent: '居住' },
  'ENTERTAIN_MOVIE': { label: '电影', icon: '🎬', parent: '娱乐' },
  'ENTERTAIN_GAME': { label: '游戏', icon: '🎮', parent: '娱乐' },
  'ENTERTAIN_TRAVEL': { label: '旅游', icon: '🏖️', parent: '娱乐' },
  'MEDICAL_MEDICINE': { label: '买药', icon: '💊', parent: '医疗' },
  'EDUCATION_TUITION': { label: '学费', icon: '🎓', parent: '教育' },
  'OTHER_MISC': { label: '杂项', icon: '📦', parent: '其他' }
}

const getCategoryInfo = (category) => {
  return categoryLabels[category] || { label: category, icon: '📦', parent: '其他' }
}

const getCategoryColor = (index) => {
  const colors = ['#EF4444', '#F59E0B', '#10B981', '#3B82F6', '#8B5CF6', '#EC4899', '#6366F1', '#14B8A6', '#F97316', '#84CC16']
  return colors[index % colors.length]
}

// Format helpers
const formatBalance = (balance) => {
  return new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY', minimumFractionDigits: 2 }).format(balance || 0)
}

const formatPercent = (value) => `${((value || 0) * 100).toFixed(1)}%`

const getTrendClass = (trend) => {
  if (!trend) return 'text-slate-400'
  if (trend.includes('up') || trend.includes('增加')) return 'text-emerald-400'
  if (trend.includes('down') || trend.includes('减少')) return 'text-rose-400'
  return 'text-slate-400'
}

const getTrendIcon = (trend) => {
  if (!trend) return '➡️'
  if (trend.includes('up') || trend.includes('增加')) return '📈'
  if (trend.includes('down') || trend.includes('减少')) return '📉'
  return '➡️'
}

// Load accounts
const loadAccounts = async () => {
  loadingAccounts.value = true
  try {
    const response = await getAccounts(1, 100)
    console.log('Accounts response:', response)
    let accountList = []
    // 尝试多种数据路径
    if (response?.data?.data?.records && Array.isArray(response.data.data.records)) {
      accountList = response.data.data.records
    } else if (response?.data?.data && Array.isArray(response.data.data)) {
      accountList = response.data.data
    } else if (Array.isArray(response?.data)) {
      accountList = response.data
    }
    console.log('Parsed account list:', accountList)
    allAccounts.value = accountList
    
    // 默认选择全部账户（null 表示全部）
    selectedAccountId.value = null
    selectedAccount.value = null
  } catch (err) {
    console.error('Failed to load accounts:', err)
    ElMessage.error('加载账户列表失败')
  } finally {
    loadingAccounts.value = false
  }
}

// Load all report data
const loadReportData = async () => {
  loadingReport.value = true
  try {
    // 获取 accountId（null 表示全部账户）
    const accountId = selectedAccountId.value
    
    const summaryResponse = await getSummary(selectedPeriod.value, selectedMonth.value, accountId)
    console.log('Summary raw response:', summaryResponse)
    const summaryData = summaryResponse?.data?.data ?? summaryResponse?.data ?? summaryResponse
    console.log('Summary parsed data:', summaryData)
    if (summaryData && typeof summaryData === 'object') {
      reportSummary.value = summaryData
    }

    const categoryResponse = await getCategoryReport(selectedPeriod.value, selectedMonth.value, accountId)
    const categoryData = categoryResponse?.data?.data ?? categoryResponse?.data ?? []
    if (Array.isArray(categoryData)) {
      categoryReport.value = categoryData
    }

    // 预算执行只对单个账户有意义
    if (accountId) {
      const budgetResponse = await getBudgetExecution(selectedMonth.value)
      const budgetData = budgetResponse?.data?.data ?? budgetResponse?.data ?? []
      if (Array.isArray(budgetData)) {
        budgetExecution.value = budgetData
      }
    } else {
      budgetExecution.value = []
    }

    const trendResponse = await getTrend(trendStartDate.value, trendEndDate.value)
    const trendData = trendResponse?.data?.data ?? trendResponse?.data ?? null
    if (trendData && typeof trendData === 'object') {
      trendReport.value = trendData
    }
  } catch (err) {
    console.error('Failed to load report data:', err)
    ElMessage.error('加载报表数据失败')
  } finally {
    loadingReport.value = false
  }
}

// Generate trend chart data
const trendChartData = computed(() => {
  if (!trendReport.value || !trendReport.value.dates) return null
  return {
    labels: trendReport.value.dates.map(d => d.substring(5)),
    income: trendReport.value.income || [],
    expense: trendReport.value.expense || []
  }
})

const chartMaxValue = computed(() => {
  if (!trendChartData.value) return 1000
  const maxIncome = Math.max(...trendChartData.value.income, 0)
  const maxExpense = Math.max(...trendChartData.value.expense, 0)
  return Math.max(maxIncome, maxExpense) * 1.2
})

// Watchers
const onAccountChange = () => {
  if (selectedAccountId.value) {
    localStorage.setItem('selectedAccountId', selectedAccountId.value)
    selectedAccount.value = allAccounts.value.find(acc => acc.id === Number(selectedAccountId.value))
    loadReportData()
  }
}

watch(selectedAccountId, onAccountChange)

watch([selectedPeriod, selectedMonth], () => {
  loadReportData()
})

onMounted(async () => {
  if (!authApi.isAuthenticated()) {
    router.replace('/login')
    return
  }
  await loadAccounts()
  // 默认加载全部账户的报表
  await loadReportData()
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
              <h1 class="text-2xl font-bold text-white">财务报表</h1>
              <p class="text-slate-400 text-sm">分析您的收支情况</p>
            </div>
          </div>
        </div>
      </header>

      <!-- Main Content -->
      <main class="max-w-7xl mx-auto px-6 py-8">
        <!-- Query Conditions -->
        <div class="bg-slate-800/50 border border-slate-700/50 rounded-2xl p-4 mb-6">
          <div class="flex flex-wrap items-end gap-4">
            <div class="flex-1 min-w-[150px]">
              <label class="block text-sm font-medium text-slate-300 mb-1">报告周期</label>
              <select v-model="selectedPeriod" class="w-full bg-slate-900/50 border border-slate-600/50 rounded-lg px-3 py-2 text-white focus:outline-none focus:ring-2 focus:ring-indigo-500">
                <option value="month">月度</option>
                <option value="year">年度</option>
              </select>
            </div>
            <div class="flex-1 min-w-[150px]">
              <label class="block text-sm font-medium text-slate-300 mb-1">选择月份</label>
              <input v-model="selectedMonth" type="month" class="w-full bg-slate-900/50 border border-slate-600/50 rounded-lg px-3 py-2 text-white focus:outline-none focus:ring-2 focus:ring-indigo-500">
            </div>
            <button @click="loadReportData" class="px-6 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg transition-colors">
              查询报表
            </button>
          </div>
        </div>

        <!-- Loading -->
        <div v-if="loadingReport" class="flex justify-center items-center py-12">
          <div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-indigo-600"></div>
        </div>

        <template v-else>
          <!-- Summary Cards -->
          <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
            <div class="bg-slate-800/50 border border-slate-700/50 rounded-2xl p-4">
              <div class="flex items-center space-x-2 mb-1">
                <span class="text-lg">💰</span>
                <span class="text-sm text-slate-400">总收入</span>
              </div>
              <div class="text-xl font-bold text-emerald-400">{{ formatBalance(reportSummary?.income || 0) }}</div>
            </div>
            <div class="bg-slate-800/50 border border-slate-700/50 rounded-2xl p-4">
              <div class="flex items-center space-x-2 mb-1">
                <span class="text-lg">💸</span>
                <span class="text-sm text-slate-400">总支出</span>
              </div>
              <div class="text-xl font-bold text-rose-400">{{ formatBalance(reportSummary?.expense || 0) }}</div>
            </div>
            <div class="bg-slate-800/50 border border-slate-700/50 rounded-2xl p-4">
              <div class="flex items-center space-x-2 mb-1">
                <span class="text-lg">💳</span>
                <span class="text-sm text-slate-400">结余</span>
              </div>
              <div class="text-xl font-bold text-blue-400">{{ formatBalance(reportSummary?.balance || 0) }}</div>
            </div>
            <div class="bg-slate-800/50 border border-slate-700/50 rounded-2xl p-4">
              <div class="flex items-center space-x-2 mb-1">
                <span class="text-lg">{{ getTrendIcon(reportSummary?.trend) }}</span>
                <span class="text-sm text-slate-400">趋势</span>
              </div>
              <div class="text-xl font-bold" :class="getTrendClass(reportSummary?.trend)">
                {{ reportSummary?.trend || '持平' }}
              </div>
            </div>
          </div>

          <!-- Trend Chart -->
          <div v-if="trendChartData" class="bg-slate-800/50 border border-slate-700/50 rounded-2xl p-6 mb-6">
            <h3 class="text-lg font-semibold text-white mb-4">收支趋势</h3>
            <div class="relative">
              <div class="flex items-stretch">
                <!-- Y轴标签 -->
                <div class="flex flex-col justify-between text-xs text-slate-500 pr-3 py-2 min-w-[60px]">
                  <span class="text-right">{{ formatBalance(chartMaxValue) }}</span>
                  <span class="text-right">{{ formatBalance(chartMaxValue * 0.5) }}</span>
                  <span class="text-right">{{ formatBalance(0) }}</span>
                </div>
                <!-- 图表区域 - 可滚动 -->
                <div class="flex-1 overflow-x-auto">
                  <div class="h-48 flex items-end justify-start gap-1 min-w-max px-2">
                    <div v-for="(label, index) in trendChartData.labels" :key="index" class="flex flex-col items-center w-8 flex-shrink-0">
                      <div class="w-full flex items-end justify-center gap-0.5 h-36">
                        <div 
                          class="w-3 bg-emerald-500 rounded-t transition-all duration-300"
                          :style="{ height: `${(trendChartData.income[index] / chartMaxValue) * 100}%`, minHeight: '2px' }"
                        ></div>
                        <div 
                          class="w-3 bg-rose-500 rounded-t transition-all duration-300"
                          :style="{ height: `${(trendChartData.expense[index] / chartMaxValue) * 100}%`, minHeight: '2px' }"
                        ></div>
                      </div>
                      <span class="text-xs text-slate-500 mt-1 whitespace-nowrap">{{ label }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div class="flex items-center justify-center space-x-6 mt-4">
              <div class="flex items-center space-x-2">
                <div class="w-4 h-4 bg-emerald-500 rounded"></div>
                <span class="text-sm text-slate-300">收入</span>
              </div>
              <div class="flex items-center space-x-2">
                <div class="w-4 h-4 bg-rose-500 rounded"></div>
                <span class="text-sm text-slate-300">支出</span>
              </div>
            </div>
          </div>

          <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
            <!-- Category Report -->
            <div class="bg-slate-800/50 border border-slate-700/50 rounded-2xl p-6">
              <h3 class="text-lg font-semibold text-white mb-4">分类支出</h3>
              
              <div v-if="categoryReport.length === 0" class="text-center py-8 text-slate-400">
                暂无分类支出数据
              </div>
              
              <div v-else class="space-y-3">
                <div v-for="(item, index) in categoryReport" :key="index" class="flex items-center justify-between p-3 bg-slate-900/50 rounded-xl hover:bg-slate-900/70 transition-colors">
                  <div class="flex items-center space-x-3">
                    <div class="w-10 h-10 rounded-full flex items-center justify-center text-lg" :style="{ backgroundColor: getCategoryColor(index) + '20' }">
                      {{ getCategoryInfo(item.category).icon }}
                    </div>
                    <div>
                      <span class="text-white font-medium">{{ getCategoryInfo(item.category).label }}</span>
                      <span class="text-xs text-slate-500 ml-2">{{ getCategoryInfo(item.category).parent }}</span>
                    </div>
                  </div>
                  <div class="text-right">
                    <span class="font-medium text-white">{{ formatBalance(item.amount || 0) }}</span>
                    <span class="text-slate-500 text-sm ml-2">({{ formatPercent(item.percent || 0) }})</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Budget Execution -->
            <div class="bg-slate-800/50 border border-slate-700/50 rounded-2xl p-6">
              <h3 class="text-lg font-semibold text-white mb-4">预算执行</h3>
              
              <div v-if="budgetExecution.length === 0" class="text-center py-8 text-slate-400">
                <template v-if="isAllAccounts">
                  请选择单个账户查看预算执行情况
                </template>
                <template v-else>
                  暂无预算数据
                </template>
              </div>
              
              <div v-else class="space-y-4">
                <div v-for="item in budgetExecution" :key="item.category" class="bg-slate-900/50 rounded-xl p-4">
                  <div class="flex items-center justify-between mb-2">
                    <div class="flex items-center space-x-2">
                      <span class="font-medium text-white">{{ getCategoryInfo(item.category).icon }}</span>
                      <span class="font-medium text-white">{{ getCategoryInfo(item.category).label }}</span>
                    </div>
                    <span class="text-sm text-slate-400">
                      {{ formatBalance(item.usedAmount || 0) }} / {{ formatBalance(item.budgetAmount || 0) }}
                    </span>
                  </div>
                  <div class="w-full bg-slate-700 rounded-full h-3 mb-2 overflow-hidden">
                    <div 
                      :class="[
                        'h-full rounded-full transition-all duration-500',
                        ((item.usedAmount || 0) / (item.budgetAmount || 1)) >= 1 ? 'bg-rose-500' :
                        ((item.usedAmount || 0) / (item.budgetAmount || 1)) >= 0.8 ? 'bg-amber-500' : 'bg-emerald-500'
                      ]"
                      :style="{ width: `${Math.min((item.usedAmount || 0) / (item.budgetAmount || 1) * 100, 100)}%` }"
                    ></div>
                  </div>
                  <div class="flex justify-between text-xs text-slate-500">
                    <span>已用 {{ formatPercent((item.usedAmount || 0) / (item.budgetAmount || 1)) }}</span>
                    <span>剩余 {{ formatBalance((item.budgetAmount || 0) - (item.usedAmount || 0)) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>
      </main>
    </main>
  </div>
</template>
