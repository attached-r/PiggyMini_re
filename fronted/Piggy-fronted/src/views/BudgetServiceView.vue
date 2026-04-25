<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/auth'
import { createOrUpdateBudget, getCurrentBudgets } from '@/api/budget'
import { ExpenseCategory, ExpenseCategoryGroups } from '@/api/transaction'
import { ElMessage } from 'element-plus'
import Sidebar from '@/components/Sidebar.vue'

const router = useRouter()
const selectedAccount = JSON.parse(localStorage.getItem('selectedAccount'))
const budgets = ref([])
const loadingBudgets = ref(false)
const showBudgetModal = ref(false)
const creatingBudget = ref(false)
const budgetError = ref('')
const sidebarCollapsed = ref(false)

// 编辑预算相关
const editingBudget = ref(null) // 当前编辑的预算
const isEditMode = ref(false) // 是否是编辑模式

// 分类筛选
const selectedCategoryFilter = ref('all')
const categories = ref([])
const parentCategories = ref([])

// 加载分类数据（使用前端定义的分类，避免后端接口问题）
const loadCategories = () => {
  // 构建分类列表
  const categoryList = []
  for (const [categoryKey, categoryInfo] of Object.entries(ExpenseCategory)) {
    categoryList.push({
      value: categoryKey,
      label: categoryInfo.label,
      icon: categoryInfo.icon,
      parent: categoryInfo.parent
    })
  }
  categories.value = categoryList
  
  // 获取父分类列表（按定义顺序）
  parentCategories.value = Object.keys(ExpenseCategoryGroups)
}

// 预算表单数据
const budgetForm = ref({
  month: '',
  category: '',
  amount: 0
})

// 计算当前月份
const currentMonth = computed(() => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  return `${year}-${month}`
})

const formatBalance = (balance) => {
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    minimumFractionDigits: 2
  }).format(balance)
}

// 加载预算数据
const loadBudgets = async () => {
  loadingBudgets.value = true
  
  try {
    const response = await getCurrentBudgets()
    
    if (response && response.data && Array.isArray(response.data)) {
      const budgetData = response.data.map(budget => ({
        id: budget.id,
        category: budget.category,
        amount: budget.budget,
        usedAmount: budget.spent || 0,
        remainingAmount: budget.remain || (budget.budget - (budget.spent || 0)),
        usageRate: budget.budget > 0 ? ((budget.spent || 0) / budget.budget) * 100 : 0
      }))
      
      budgets.value = budgetData
    } else {
      budgets.value = []
    }
  } catch (err) {
    console.error('加载预算失败:', err)
    ElMessage.error('加载预算失败')
    budgets.value = []
  } finally {
    loadingBudgets.value = false
  }
}

// 根据分类获取父分类
const getCategoryParent = (category) => {
  const cat = categories.value.find(c => c.value === category)
  return cat ? cat.parent : '其他'
}

// 处理分类筛选切换
const handleCategoryFilterChange = (filter) => {
  selectedCategoryFilter.value = filter
}

// 过滤后的预算
const filteredBudgets = computed(() => {
  if (selectedCategoryFilter.value === 'all') {
    return budgets.value
  }
  return budgets.value.filter(budget => {
    const parent = getCategoryParent(budget.category)
    return parent === selectedCategoryFilter.value
  })
})

const openBudgetModal = () => {
  budgetForm.value = {
    month: currentMonth.value,
    category: '',
    amount: 0
  }
  editingBudget.value = null
  isEditMode.value = false
  budgetError.value = ''
  showBudgetModal.value = true
}

// 打开编辑预算模态框
const openEditBudgetModal = (budget) => {
  editingBudget.value = budget
  isEditMode.value = true
  budgetForm.value = {
    month: currentMonth.value,
    category: budget.category,
    amount: budget.amount
  }
  budgetError.value = ''
  showBudgetModal.value = true
}

const handleCreateBudget = async () => {
  budgetError.value = ''
  
  if (!budgetForm.value.month) {
    budgetError.value = '请选择月份'
    return
  }
  if (!budgetForm.value.category) {
    budgetError.value = '请选择分类'
    return
  }
  if (!budgetForm.value.amount || budgetForm.value.amount <= 0) {
    budgetError.value = '请输入有效的预算金额'
    return
  }
  
  creatingBudget.value = true
  
  try {
    const data = {
      month: budgetForm.value.month,
      category: budgetForm.value.category,
      amount: budgetForm.value.amount
    }
    
    await createOrUpdateBudget(data)
    ElMessage.success(isEditMode.value ? '更新预算成功' : '设置预算成功')
    showBudgetModal.value = false
    editingBudget.value = null
    isEditMode.value = false
    await loadBudgets()
  } catch (err) {
    console.error('创建预算失败:', err)
    budgetError.value = err.message || '创建预算失败，请稍后重试'
  } finally {
    creatingBudget.value = false
  }
}

const cancelCreateBudget = () => {
  showBudgetModal.value = false
  budgetError.value = ''
  editingBudget.value = null
  isEditMode.value = false
}

const refreshBudgets = () => {
  loadBudgets()
}

// 获取分类文本（使用后端返回的分类数据）
const getCategoryText = (category) => {
  if (!category) return '总预算'
  
  const cat = categories.value.find(c => c.value === category)
  if (cat) {
    return `${cat.parent}-${cat.label}`
  }
  
  // 备用映射表
  const categoryMap = {
    FOOD_GROCERY: '餐饮-买菜',
    FOOD_SNACK: '餐饮-零食',
    FOOD_FRUIT: '餐饮-水果',
    FOOD_DRINK: '餐饮-饮料',
    FOOD_LUNCH: '餐饮-午餐',
    FOOD_DINNER: '餐饮-晚餐',
    FOOD_BREAKFAST: '餐饮-早餐',
    FOOD_DELIVERY: '餐饮-外卖',
    FOOD_TREAT: '餐饮-请客',
    TRANSPORT_TAXI: '交通-出租车',
    TRANSPORT_SUBWAY: '交通-地铁',
    TRANSPORT_BUS: '交通-公交',
    TRANSPORT_FUEL: '交通-燃油',
    SHOP_CLOTHES: '购物-衣服',
    SHOP_DIGITAL: '购物-数码',
    SHOP_DAILY: '购物-日用品',
    ENTERTAIN_GAME: '娱乐-游戏',
    ENTERTAIN_MOVIE: '娱乐-电影',
    ENTERTAIN_TRAVEL: '娱乐-旅行',
    MEDICAL_MEDICINE: '医疗-药品',
    EDUCATION_TUITION: '教育-学费',
    LIVING_RENT: '生活-房租',
    LIVING_ELECTRIC: '生活-电费',
    LIVING_WATER: '生活-水费',
    OTHER_MISC: '其他-杂项'
  }
  return categoryMap[category] || category
}

const formatPercentage = (value) => {
  return `${value.toFixed(1)}%`
}

onMounted(() => {
  if (!authApi.isAuthenticated()) {
    router.replace('/login')
    return
  }
  loadCategories()
  loadBudgets()
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
              <h1 class="text-2xl font-bold text-white">预算服务</h1>
              <p class="text-slate-400 text-sm">管理您的预算计划</p>
            </div>
            <button @click="refreshBudgets" :disabled="loadingBudgets" class="px-4 py-2 bg-slate-700/50 hover:bg-slate-700 text-slate-300 rounded-lg transition-colors flex items-center space-x-2">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
              </svg>
              <span>刷新</span>
            </button>
          </div>
        </div>
      </header>

      <!-- Main Content -->
      <main class="max-w-7xl mx-auto px-6 py-8">
        <!-- Loading -->
        <div v-if="loadingBudgets" class="flex justify-center items-center py-12">
          <div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-indigo-600"></div>
        </div>

        <template v-else>
          <!-- 分类筛选器 -->
          <div class="bg-slate-800/50 border border-slate-700/50 rounded-2xl p-4 mb-6">
            <div class="flex items-center justify-between">
              <span class="text-slate-300 font-medium">按分类筛选</span>
              <div class="flex flex-wrap gap-2">
                <button
                  @click="handleCategoryFilterChange('all')"
                  :class="[
                    'px-4 py-2 rounded-lg text-sm font-medium transition-colors',
                    selectedCategoryFilter === 'all' 
                      ? 'bg-indigo-600 text-white' 
                      : 'bg-slate-700 text-slate-300 hover:bg-slate-600'
                  ]"
                >
                  全部
                </button>
                <button
                  v-for="parent in parentCategories"
                  :key="parent"
                  @click="handleCategoryFilterChange(parent)"
                  :class="[
                    'px-4 py-2 rounded-lg text-sm font-medium transition-colors',
                    selectedCategoryFilter === parent 
                      ? 'bg-indigo-600 text-white' 
                      : 'bg-slate-700 text-slate-300 hover:bg-slate-600'
                  ]"
                >
                  {{ parent }}
                </button>
              </div>
            </div>
            <div v-if="filteredBudgets.length > 0" class="mt-3 text-sm text-slate-400">
              共 {{ filteredBudgets.length }} 个预算
            </div>
          </div>

          <!-- 预算卡片列表 -->
          <div v-if="filteredBudgets.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-6">
            <div v-for="(budget, index) in filteredBudgets" :key="index" class="bg-slate-800/50 border border-slate-700/50 rounded-2xl p-6">
              <div class="flex items-center justify-between mb-4">
                <h3 class="font-medium text-white">{{ getCategoryText(budget.category) }}</h3>
                <div class="flex items-center space-x-2">
                  <button 
                    @click="openEditBudgetModal(budget)"
                    class="text-slate-400 hover:text-indigo-400 transition-colors p-1"
                    title="编辑预算"
                  >
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path>
                    </svg>
                  </button>
                  <span :class="[
                    'text-sm font-medium px-2 py-1 rounded-full',
                    (budget.usageRate || 0) >= 100 ? 'bg-rose-500/20 text-rose-400' : 
                    (budget.usageRate || 0) >= 80 ? 'bg-amber-500/20 text-amber-400' : 
                    'bg-emerald-500/20 text-emerald-400'
                  ]">
                    {{ formatPercentage(budget.usageRate || 0) }}
                  </span>
                </div>
              </div>
              
              <div class="space-y-3">
                <div class="flex justify-between">
                  <span class="text-slate-400 text-sm">预算金额</span>
                  <span class="text-white font-medium">{{ formatBalance(budget.amount || 0) }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-slate-400 text-sm">已使用</span>
                  <span class="text-rose-400 font-medium">{{ formatBalance(budget.usedAmount || 0) }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-slate-400 text-sm">剩余</span>
                  <span :class="(budget.remainingAmount || 0) >= 0 ? 'text-emerald-400' : 'text-rose-400'">
                    {{ formatBalance(budget.remainingAmount || 0) }}
                  </span>
                </div>
              </div>
              
              <!-- 进度条 -->
              <div class="mt-4">
                <div class="w-full bg-slate-900/50 rounded-full h-2.5">
                  <div 
                    :class="[
                      'h-2.5 rounded-full transition-all duration-500',
                      (budget.usageRate || 0) >= 100 ? 'bg-rose-500' : 
                      (budget.usageRate || 0) >= 80 ? 'bg-amber-500' : 
                      'bg-emerald-500'
                    ]"
                    :style="{ width: `${Math.min(budget.usageRate || 0, 100)}%` }"
                  ></div>
                </div>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-else class="bg-slate-800/50 border border-slate-700/50 rounded-2xl p-12 text-center">
            <div class="text-6xl mb-4">📊</div>
            <h3 v-if="selectedCategoryFilter === 'all'" class="text-xl font-semibold text-white mb-2">暂无预算计划</h3>
            <h3 v-else class="text-xl font-semibold text-white mb-2">该分类暂无预算</h3>
            <p v-if="selectedCategoryFilter === 'all'" class="text-slate-400 mb-6">创建您的第一个预算来管理支出</p>
            <p v-else class="text-slate-400 mb-6">选择其他分类查看，或创建新的预算</p>
            <button @click="openBudgetModal" class="px-6 py-3 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg transition-colors">
              创建预算
            </button>
          </div>

          <!-- 设置预算按钮 -->
          <div v-if="filteredBudgets.length > 0" class="flex justify-end">
            <button @click="openBudgetModal" class="px-6 py-3 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg transition-colors flex items-center space-x-2">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
              </svg>
              <span>设置预算</span>
            </button>
          </div>
        </template>
      </main>
    </main>

    <!-- 设置预算模态框 -->
    <div v-if="showBudgetModal" class="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50">
      <div class="bg-slate-800 border border-slate-700 rounded-2xl w-full max-w-md p-6">
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-xl font-bold text-white">{{ isEditMode ? '编辑预算' : '设置预算' }}</h3>
          <button @click="cancelCreateBudget" class="text-slate-400 hover:text-white transition-colors">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
        </div>

        <!-- Error Message -->
        <div v-if="budgetError" class="bg-rose-500/10 border border-rose-500/20 text-rose-400 px-4 py-3 rounded-lg mb-4">
          {{ budgetError }}
        </div>

        <!-- Budget Form -->
        <form @submit.prevent="handleCreateBudget" class="space-y-4">
          <!-- 分类信息（编辑模式下只读） -->
          <div v-if="isEditMode" class="bg-slate-900/50 border border-slate-600/50 rounded-lg p-3">
            <label class="block text-sm font-medium text-slate-400 mb-1">分类</label>
            <div class="text-white font-medium">{{ getCategoryText(editingBudget?.category) }}</div>
          </div>

          <!-- Month -->
          <div>
            <label class="block text-sm font-medium text-slate-300 mb-2">月份</label>
            <input
              v-model="budgetForm.month"
              type="month"
              class="w-full bg-slate-900/50 border border-slate-600/50 rounded-lg px-3 py-2.5 text-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
              :disabled="creatingBudget"
            />
          </div>

          <!-- Category -->
          <div v-if="!isEditMode">
            <label class="block text-sm font-medium text-slate-300 mb-2">预算分类</label>
            <select
              v-model="budgetForm.category"
              class="w-full bg-slate-900/50 border border-slate-600/50 rounded-lg px-3 py-2.5 text-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
              :disabled="creatingBudget"
            >
              <option value="">请选择分类</option>
              <optgroup label="餐饮">
                <option value="FOOD_GROCERY">餐饮-买菜</option>
                <option value="FOOD_SNACK">餐饮-零食</option>
                <option value="FOOD_FRUIT">餐饮-水果</option>
                <option value="FOOD_LUNCH">餐饮-午餐</option>
                <option value="FOOD_DINNER">餐饮-晚餐</option>
                <option value="FOOD_BREAKFAST">餐饮-早餐</option>
                <option value="FOOD_DELIVERY">餐饮-外卖</option>
              </optgroup>
              <optgroup label="交通">
                <option value="TRANSPORT_SUBWAY">交通-地铁</option>
                <option value="TRANSPORT_BUS">交通-公交</option>
                <option value="TRANSPORT_TAXI">交通-打车</option>
                <option value="TRANSPORT_FUEL">交通-加油</option>
              </optgroup>
              <optgroup label="购物">
                <option value="SHOP_CLOTHES">购物-服饰</option>
                <option value="SHOP_DIGITAL">购物-数码</option>
                <option value="SHOP_DAILY">购物-日用品</option>
              </optgroup>
              <optgroup label="娱乐">
                <option value="ENTERTAIN_MOVIE">娱乐-电影</option>
                <option value="ENTERTAIN_GAME">娱乐-游戏</option>
                <option value="ENTERTAIN_TRAVEL">娱乐-旅行</option>
              </optgroup>
              <optgroup label="生活">
                <option value="LIVING_RENT">生活-房租</option>
                <option value="LIVING_ELECTRIC">生活-电费</option>
                <option value="LIVING_WATER">生活-水费</option>
              </optgroup>
              <optgroup label="其他">
                <option value="OTHER_MISC">其他-杂项</option>
              </optgroup>
            </select>
          </div>

          <!-- Amount -->
          <div>
            <label class="block text-sm font-medium text-slate-300 mb-2">预算金额</label>
            <div class="relative">
              <span class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">¥</span>
              <input
                v-model.number="budgetForm.amount"
                type="number"
                step="0.01"
                min="0"
                placeholder="0.00"
                class="w-full bg-slate-900/50 border border-slate-600/50 rounded-lg pl-8 pr-3 py-2.5 text-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
                :disabled="creatingBudget"
              />
            </div>
          </div>

          <!-- Buttons -->
          <div class="flex justify-end space-x-3 pt-4">
            <button
              type="button"
              @click="cancelCreateBudget"
              class="px-4 py-2 bg-slate-700 hover:bg-slate-600 text-slate-300 rounded-lg transition-colors"
            >
              取消
            </button>
            <button
              type="submit"
              :disabled="creatingBudget"
              class="px-6 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg transition-colors disabled:opacity-50"
            >
              {{ creatingBudget ? '保存中...' : '保存' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
