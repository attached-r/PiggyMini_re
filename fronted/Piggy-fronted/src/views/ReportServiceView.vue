<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/auth'
import { getAccounts } from '@/api/account'
import { getSummary, getCategoryReport, getBudgetExecution, getTrend } from '@/api/report'
import { ElMessage } from 'element-plus'

const router = useRouter()

// Account related
const allAccounts = ref([])
const selectedAccountId = ref(null)
const selectedAccount = ref(null)
const loadingAccounts = ref(false)

const selectedPeriod = ref('month')
const currentDate = new Date()

// Helper function to get last day of month (for date conversion)
const getLastDayOfMonth = (year, month) => {
  return new Date(year, month, 0).getDate()
}

// Convert yyyy-MM (month picker) to yyyy-MM-dd (backend format) - using last day of month
const monthToDate = (monthStr) => {
  if (!monthStr) return ''
  const [year, month] = monthStr.split('-')
  const lastDay = getLastDayOfMonth(parseInt(year), parseInt(month))
  return `${year}-${month}-${String(lastDay).padStart(2, '0')}`
}

// selectedDate stores the date in yyyy-MM-dd format for backend API
const selectedDate = ref(monthToDate(`${currentDate.getFullYear()}-${String(currentDate.getMonth() + 1).padStart(2, '0')}`))

// selectedMonth for month picker binding (yyyy-MM format)
const selectedMonth = ref(`${currentDate.getFullYear()}-${String(currentDate.getMonth() + 1).padStart(2, '0')}`)

// Watch month picker changes and update selectedDate
watch(selectedMonth, (newMonth) => {
  selectedDate.value = monthToDate(newMonth)
})

// Trend date range - using yyyy-MM-dd format as required by backend
const trendStartDate = ref(`${currentDate.getFullYear()}-01-01`)
const trendEndDate = ref(monthToDate(`${currentDate.getFullYear()}-${String(currentDate.getMonth() + 1).padStart(2, '0')}`))

// Report data - strictly following backend DTOs
const reportSummary = ref(null)  // SummaryReportResponse: { income, expense, balance, trend }
const categoryReport = ref([])     // CategoryReportResponse[]: { category, amount, percent }
const budgetExecution = ref([])    // BudgetExecutionResponse[]: { category, budgetAmount, usedAmount, remainingAmount, executionRate, isOverBudget }
const trendReport = ref(null)      // TrendReportResponse: { dates, income, expense }
const loadingReport = ref(false)

// Category mapping - strictly following backend ExpenseCategory enum
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
  'TRANSPORT_PARKING': { label: '停车费', icon: '🅿️', parent: '交通' },
  'TRANSPORT_TOLL': { label: '过路费', icon: '🛣️', parent: '交通' },
  'TRANSPORT_BIKE': { label: '共享单车', icon: '🚲', parent: '交通' },
  'TRANSPORT_TRAIN': { label: '火车/高铁', icon: '🚄', parent: '交通' },
  'TRANSPORT_FLIGHT': { label: '机票', icon: '✈️', parent: '交通' },
  'SHOP_CLOTHES': { label: '服饰', icon: '👔', parent: '购物' },
  'SHOP_SHOES': { label: '鞋靴', icon: '👟', parent: '购物' },
  'SHOP_BAG': { label: '箱包', icon: '👜', parent: '购物' },
  'SHOP_COSMETIC': { label: '美妆', icon: '💄', parent: '购物' },
  'SHOP_DIGITAL': { label: '数码', icon: '💻', parent: '购物' },
  'SHOP_HOME': { label: '家居', icon: '🏠', parent: '购物' },
  'SHOP_DAILY': { label: '日用品', icon: '🧴', parent: '购物' },
  'SHOP_BOOK': { label: '书籍', icon: '📚', parent: '购物' },
  'SHOP_GIFT': { label: '礼物', icon: '🎁', parent: '购物' },
  'LIVING_RENT': { label: '房租', icon: '🏢', parent: '居住' },
  'LIVING_MORTGAGE': { label: '房贷', icon: '🏦', parent: '居住' },
  'LIVING_PROPERTY': { label: '物业费', icon: '🔑', parent: '居住' },
  'LIVING_WATER': { label: '水费', icon: '💧', parent: '居住' },
  'LIVING_ELECTRIC': { label: '电费', icon: '⚡', parent: '居住' },
  'LIVING_GAS': { label: '燃气费', icon: '🔥', parent: '居住' },
  'LIVING_INTERNET': { label: '网费', icon: '🌐', parent: '居住' },
  'LIVING_MAINTENANCE': { label: '维修', icon: '🔧', parent: '居住' },
  'ENTERTAIN_MOVIE': { label: '电影', icon: '🎬', parent: '娱乐' },
  'ENTERTAIN_GAME': { label: '游戏', icon: '🎮', parent: '娱乐' },
  'ENTERTAIN_KTV': { label: 'KTV', icon: '🎤', parent: '娱乐' },
  'ENTERTAIN_TRAVEL': { label: '旅游', icon: '🏖️', parent: '娱乐' },
  'ENTERTAIN_SPORT': { label: '运动', icon: '⚽', parent: '娱乐' },
  'ENTERTAIN_GYM': { label: '健身', icon: '🏋️', parent: '娱乐' },
  'ENTERTAIN_STREAM': { label: '会员订阅', icon: '📺', parent: '娱乐' },
  'MEDICAL_HOSPITAL': { label: '看病', icon: '🏥', parent: '医疗' },
  'MEDICAL_MEDICINE': { label: '买药', icon: '💊', parent: '医疗' },
  'MEDICAL_DENTAL': { label: '牙科', icon: '🦷', parent: '医疗' },
  'MEDICAL_CHECKUP': { label: '体检', icon: '🩺', parent: '医疗' },
  'MEDICAL_INSURANCE': { label: '保险', icon: '🛡️', parent: '医疗' },
  'EDUCATION_TUITION': { label: '学费', icon: '🎓', parent: '教育' },
  'EDUCATION_COURSE': { label: '培训', icon: '📖', parent: '教育' },
  'EDUCATION_EXAM': { label: '考试', icon: '📝', parent: '教育' },
  'EDUCATION_MATERIAL': { label: '教材', icon: '📚', parent: '教育' },
  'SOCIAL_RED_PACKET': { label: '红包', icon: '🧧', parent: '社交' },
  'SOCIAL_DONATION': { label: '捐赠', icon: '❤️', parent: '社交' },
  'SOCIAL_TIP': { label: '小费', icon: '💰', parent: '社交' },
  'OTHER_MISC': { label: '杂项', icon: '📦', parent: '其他' },
  'OTHER_LOSS': { label: '损失', icon: '💸', parent: '其他' },
  'OTHER_PENALTY': { label: '罚款', icon: '⚠️', parent: '其他' }
}

// Get category display info
const getCategoryInfo = (category) => {
  return categoryLabels[category] || { label: category, icon: '📦', parent: '其他' }
}

// Get category color
const getCategoryColor = (index) => {
  const colors = ['#EF4444', '#F59E0B', '#10B981', '#3B82F6', '#8B5CF6', '#EC4899', '#6366F1', '#14B8A6', '#F97316', '#84CC16']
  return colors[index % colors.length]
}

// Navigation
const goBack = () => router.push('/dashboard')
const handleLogout = () => { authApi.logout(); router.push('/login') }

// Format helpers
const formatBalance = (balance) => {
  return new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY', minimumFractionDigits: 2 }).format(balance || 0)
}

const formatPercent = (value) => `${((value || 0) * 100).toFixed(1)}%`

// Get trend badge class
const getTrendClass = (trend) => {
  if (!trend) return 'text-gray-500'
  if (trend.includes('up') || trend.includes('增加') || trend.includes('上升')) return 'text-green-600'
  if (trend.includes('down') || trend.includes('减少') || trend.includes('下降')) return 'text-red-600'
  return 'text-gray-500'
}

const getTrendIcon = (trend) => {
  if (!trend) return '➡️'
  if (trend.includes('up') || trend.includes('增加') || trend.includes('上升')) return '📈'
  if (trend.includes('down') || trend.includes('减少') || trend.includes('下降')) return '📉'
  return '➡️'
}

// Load accounts
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
  } finally {
    loadingAccounts.value = false
  }
}

// Load all report data
const loadReportData = async () => {
  loadingReport.value = true
  try {
    // Load summary - SummaryReportResponse: { income, expense, balance, trend }
    // Backend expects date in yyyy-MM format for summary
    const summaryResponse = await getSummary(selectedPeriod.value, selectedMonth.value)
    const summaryData = summaryResponse?.data?.data ?? summaryResponse?.data ?? summaryResponse
    if (summaryData && typeof summaryData === 'object') {
      reportSummary.value = summaryData
    }

    // Load category report - CategoryReportResponse[]: { category, amount, percent }
    // Backend expects date in yyyy-MM format for category report
    const categoryResponse = await getCategoryReport(selectedPeriod.value, selectedMonth.value)
    const categoryData = categoryResponse?.data?.data ?? categoryResponse?.data ?? []
    if (Array.isArray(categoryData)) {
      categoryReport.value = categoryData
    }

    // Load budget execution - BudgetExecutionResponse[]: { category, budgetAmount, usedAmount, remainingAmount, executionRate, isOverBudget }
    // Backend expects month in yyyy-MM format for budget execution
    const budgetResponse = await getBudgetExecution(selectedMonth.value)
    const budgetData = budgetResponse?.data?.data ?? budgetResponse?.data ?? []
    if (Array.isArray(budgetData)) {
      budgetExecution.value = budgetData
    }

    // Load trend report - TrendReportResponse: { dates, income, expense }
    // Backend expects date in yyyy-MM-dd format for trend
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
    labels: trendReport.value.dates.map(d => d.substring(5)), // MM-DD format
    income: trendReport.value.income || [],
    expense: trendReport.value.expense || []
  }
})

// Max value for chart scaling
const chartMaxValue = computed(() => {
  if (!trendChartData.value) return 1000
  const maxIncome = Math.max(...trendChartData.value.income, 0)
  const maxExpense = Math.max(...trendChartData.value.expense, 0)
  return Math.max(maxIncome, maxExpense) * 1.2
})

// Watchers
watch(selectedAccountId, (newId) => {
  if (newId) {
    localStorage.setItem('selectedAccountId', newId)
    selectedAccount.value = allAccounts.value.find(acc => acc.id === Number(newId))
    loadReportData()
  }
})

watch([selectedPeriod, selectedMonth], () => {
  loadReportData()
})

onMounted(async () => {
  if (!authApi.isAuthenticated()) {
    router.replace('/login')
    return
  }
  await loadAccounts()
  if (selectedAccountId.value) await loadReportData()
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
            <span class="text-xs text-blue-600 font-medium block -mt-1">报表中心</span>
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

    <!-- Main Content -->
    <main class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Query Conditions -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-4 mb-6">
        <div class="flex flex-wrap items-end gap-4">
          <div class="flex-1 min-w-[150px]">
            <label class="block text-sm font-medium text-gray-700 mb-1">报告周期</label>
            <select v-model="selectedPeriod" class="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary-500">
              <option value="month">月度</option>
              <option value="year">年度</option>
            </select>
          </div>
          <div class="flex-1 min-w-[150px]">
            <label class="block text-sm font-medium text-gray-700 mb-1">选择月份</label>
            <input v-model="selectedMonth" type="month" class="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary-500">
          </div>
          <div class="self-end">
            <button @click="loadReportData" class="btn-primary px-6 py-2 bg-blue-600 hover:bg-blue-700">查询报表</button>
          </div>
        </div>
      </div>

      <!-- Loading -->
      <div v-if="loadingReport" class="flex justify-center items-center py-12">
        <div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary-600"></div>
      </div>

      <template v-else>
        <!-- Summary Cards - SummaryReportResponse: { income, expense, balance, trend } -->
        <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
          <!-- Total Income -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-4">
            <div class="flex items-center space-x-2 mb-1">
              <span class="text-lg">💰</span>
              <span class="text-sm text-gray-500">总收入</span>
            </div>
            <div class="text-xl font-bold text-green-600">{{ formatBalance(reportSummary?.income || 0) }}</div>
          </div>
          <!-- Total Expense -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-4">
            <div class="flex items-center space-x-2 mb-1">
              <span class="text-lg">💸</span>
              <span class="text-sm text-gray-500">总支出</span>
            </div>
            <div class="text-xl font-bold text-red-600">{{ formatBalance(reportSummary?.expense || 0) }}</div>
          </div>
          <!-- Balance -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-4">
            <div class="flex items-center space-x-2 mb-1">
              <span class="text-lg">💳</span>
              <span class="text-sm text-gray-500">结余</span>
            </div>
            <div class="text-xl font-bold text-blue-600">{{ formatBalance(reportSummary?.balance || 0) }}</div>
          </div>
          <!-- Trend -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-4">
            <div class="flex items-center space-x-2 mb-1">
              <span class="text-lg">{{ getTrendIcon(reportSummary?.trend) }}</span>
              <span class="text-sm text-gray-500">趋势</span>
            </div>
            <div class="text-xl font-bold" :class="getTrendClass(reportSummary?.trend)">
              {{ reportSummary?.trend || '持平' }}
            </div>
          </div>
        </div>

        <!-- Trend Chart - TrendReportResponse: { dates, income, expense } -->
        <div v-if="trendChartData" class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 mb-6">
          <h3 class="text-lg font-semibold text-gray-900 mb-4">收支趋势</h3>
          <div class="relative h-48">
            <!-- Y-axis labels -->
            <div class="absolute left-0 top-0 bottom-0 flex flex-col justify-between text-xs text-gray-400 pr-2">
              <span>{{ formatBalance(chartMaxValue) }}</span>
              <span>{{ formatBalance(chartMaxValue * 0.5) }}</span>
              <span>{{ formatBalance(0) }}</span>
            </div>
            <!-- Chart area -->
            <div class="ml-12 h-full flex items-end justify-between gap-1">
              <div v-for="(label, index) in trendChartData.labels" :key="index" class="flex-1 flex flex-col items-center">
                <!-- Bars -->
                <div class="w-full flex items-end justify-center gap-1 h-36">
                  <!-- Income bar -->
                  <div 
                    class="w-4 bg-green-500 rounded-t transition-all duration-300"
                    :style="{ height: `${(trendChartData.income[index] / chartMaxValue) * 100}%`, minHeight: '2px' }"
                    :title="`收入: ${formatBalance(trendChartData.income[index])}`"
                  ></div>
                  <!-- Expense bar -->
                  <div 
                    class="w-4 bg-red-500 rounded-t transition-all duration-300"
                    :style="{ height: `${(trendChartData.expense[index] / chartMaxValue) * 100}%`, minHeight: '2px' }"
                    :title="`支出: ${formatBalance(trendChartData.expense[index])}`"
                  ></div>
                </div>
                <!-- X-axis label -->
                <span class="text-xs text-gray-400 mt-1">{{ label }}</span>
              </div>
            </div>
          </div>
          <!-- Legend -->
          <div class="flex items-center justify-center space-x-6 mt-4">
            <div class="flex items-center space-x-2">
              <div class="w-4 h-4 bg-green-500 rounded"></div>
              <span class="text-sm text-gray-600">收入</span>
            </div>
            <div class="flex items-center space-x-2">
              <div class="w-4 h-4 bg-red-500 rounded"></div>
              <span class="text-sm text-gray-600">支出</span>
            </div>
          </div>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
          <!-- Category Report - CategoryReportResponse[]: { category, amount, percent } -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <h3 class="text-lg font-semibold text-gray-900 mb-4">分类支出</h3>
            
            <div v-if="categoryReport.length === 0" class="text-center py-8 text-gray-500">
              暂无分类支出数据
            </div>
            
            <div v-else class="space-y-3">
              <div v-for="(item, index) in categoryReport" :key="index" class="flex items-center justify-between p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors">
                <div class="flex items-center space-x-3">
                  <div class="w-8 h-8 rounded-full flex items-center justify-center text-lg" :style="{ backgroundColor: getCategoryColor(index) + '20' }">
                    {{ getCategoryInfo(item.category).icon }}
                  </div>
                  <div>
                    <span class="text-gray-700 font-medium">{{ getCategoryInfo(item.category).label }}</span>
                    <span class="text-xs text-gray-400 ml-2">{{ getCategoryInfo(item.category).parent }}</span>
                  </div>
                </div>
                <div class="text-right">
                  <span class="font-medium text-gray-900">{{ formatBalance(item.amount || 0) }}</span>
                  <span class="text-gray-400 text-sm ml-2">({{ formatPercent(item.percent || 0) }})</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Budget Execution - BudgetExecutionResponse[]: { category, budgetAmount, usedAmount, remainingAmount, executionRate, isOverBudget } -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <h3 class="text-lg font-semibold text-gray-900 mb-4">预算执行</h3>
            
            <div v-if="budgetExecution.length === 0" class="text-center py-8 text-gray-500">
              暂无预算数据
            </div>
            
            <div v-else class="space-y-4">
              <div v-for="item in budgetExecution" :key="item.category" class="border border-gray-200 rounded-lg p-4">
                <div class="flex items-center justify-between mb-2">
                  <div class="flex items-center space-x-2">
                    <span class="font-medium text-gray-900">{{ getCategoryInfo(item.category).icon }}</span>
                    <span class="font-medium text-gray-900">{{ getCategoryInfo(item.category).label }}</span>
                  </div>
                  <span class="text-sm text-gray-500">
                    {{ formatBalance(item.usedAmount || 0) }} / {{ formatBalance(item.budgetAmount || 0) }}
                  </span>
                </div>
                <!-- Progress bar -->
                <div class="w-full bg-gray-200 rounded-full h-3 mb-2 overflow-hidden">
                  <div 
                    class="h-3 rounded-full transition-all duration-300"
                    :class="item.isOverBudget ? 'bg-red-500' : 'bg-green-500'"
                    :style="{ width: `${Math.min((item.executionRate || 0) * 100, 100)}%` }"
                  ></div>
                </div>
                <div class="flex items-center justify-between text-sm">
                  <span class="text-gray-500">执行率: {{ formatPercent(item.executionRate || 0) }}</span>
                  <span :class="item.isOverBudget ? 'text-red-600 font-medium' : 'text-green-600'">
                    {{ item.isOverBudget ? '⚠️ 已超支' : `剩余 ${formatBalance(item.remainingAmount || 0)}` }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </main>

    <!-- Footer -->
    <footer class="bg-white border-t border-gray-200 py-6 mt-8">
      <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
        <p class="text-gray-500 text-sm">&copy; 2024 Piggy. All rights reserved.</p>
      </div>
    </footer>
  </div>
</template>
