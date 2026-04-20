<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/auth'
import { createOrUpdateBudget, getCurrentBudgets } from '@/api/budget'

const router = useRouter()
const selectedAccount = JSON.parse(localStorage.getItem('selectedAccount'))
const budgets = ref([])
const loadingBudgets = ref(false)
const showBudgetModal = ref(false)
const creatingBudget = ref(false)
const budgetError = ref('')

// 预算表单数据（严格遵循接口文档字段）
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

const goBack = () => {
  router.push('/services')
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

// 加载预算数据
const loadBudgets = async () => {
  loadingBudgets.value = true
  
  try {
    console.log('开始加载当前月预算数据（调用 /api/budgets/current）...')
    const response = await getCurrentBudgets()
    console.log('API响应原始数据:', JSON.stringify(response))
    
    // 处理响应结构：response.data 是 Result 对象，response.data.data 才是数组
    let budgetArray = []
    if (response?.data?.data && Array.isArray(response.data.data)) {
      budgetArray = response.data.data
    } else if (response?.data && Array.isArray(response.data)) {
      budgetArray = response.data
    }
    
    console.log('提取到的预算数组:', budgetArray)
    
    if (budgetArray.length > 0) {
      const budgetData = budgetArray.map(budget => ({
        id: budget.id,
        category: budget.category,
        amount: budget.budget,
        usedAmount: budget.spent || 0,
        remainingAmount: budget.remain || (budget.budget - (budget.spent || 0)),
        usageRate: budget.budget > 0 ? ((budget.spent || 0) / budget.budget) * 100 : 0
      }))
      
      console.log('处理后的预算数据:', budgetData)
      budgets.value = budgetData
    } else {
      console.log('暂无预算数据（请先创建预算）')
      budgets.value = []
    }
  } catch (err) {
    console.error('加载预算失败:', err)
    budgets.value = []
  } finally {
    loadingBudgets.value = false
  }
}

const openBudgetModal = () => {
  // 重置表单
  budgetForm.value = {
    month: currentMonth.value,
    category: '',
    amount: 0
  }
  budgetError.value = ''
  showBudgetModal.value = true
}

const handleCreateBudget = async () => {
  budgetError.value = ''
  
  // 验证表单
  if (!budgetForm.value.month) {
    budgetError.value = '请选择月份'
    return
  }
  
  if (!budgetForm.value.category) {
    budgetError.value = '请选择分类'
    return
  }
  
  if (budgetForm.value.amount <= 0) {
    budgetError.value = '预算金额必须大于0'
    return
  }
  
  creatingBudget.value = true
  
  try {
    // 严格按接口文档构造请求数据
    const budgetData = {
      month: budgetForm.value.month,
      category: budgetForm.value.category,
      amount: budgetForm.value.amount
    }
    
    console.log('发送的预算数据:', budgetData)
    const response = await createOrUpdateBudget(budgetData)
    console.log('创建/更新预算成功:', response)
    
    // 关闭模态框并刷新列表
    showBudgetModal.value = false
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
}

const refreshBudgets = () => {
  loadBudgets()
}

// 获取分类文本
const getCategoryText = (category) => {
  if (!category) return '总预算'
  
  const categoryMap = {
    FOOD_GROCERY: '餐饮- groceries',
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
    TRANSPORT_TOLL: '交通-过路费',
    TRANSPORT_TRAIN: '交通-火车',
    TRANSPORT_DIDI: '交通-滴滴',
    TRANSPORT_PARKING: '交通-停车',
    TRANSPORT_BIKE: '交通-自行车',
    TRANSPORT_FLIGHT: '交通-飞机',
    SHOP_HOME: '购物-家居',
    SHOP_BAG: '购物-包袋',
    SHOP_DIGITAL: '购物-数码',
    SHOP_SHOES: '购物-鞋子',
    SHOP_GIFT: '购物-礼物',
    SHOP_CLOTHES: '购物-衣服',
    SHOP_DAILY: '购物-日用品',
    SHOP_COSMETIC: '购物-化妆品',
    SHOP_BOOK: '购物-书籍',
    ENTERTAIN_GAME: '娱乐-游戏',
    ENTERTAIN_MOVIE: '娱乐-电影',
    ENTERTAIN_STREAM: '娱乐-流媒体',
    ENTERTAIN_KTV: '娱乐-KTV',
    ENTERTAIN_GYM: '娱乐-健身',
    ENTERTAIN_SPORT: '娱乐-运动',
    ENTERTAIN_TRAVEL: '娱乐-旅行',
    MEDICAL_DENTAL: '医疗-牙科',
    MEDICAL_CHECKUP: '医疗-体检',
    MEDICAL_MEDICINE: '医疗-药品',
    MEDICAL_INSURANCE: '医疗-保险',
    MEDICAL_HOSPITAL: '医疗-住院',
    EDUCATION_TUITION: '教育-学费',
    EDUCATION_EXAM: '教育-考试',
    EDUCATION_MATERIAL: '教育-教材',
    EDUCATION_COURSE: '教育-课程',
    LIVING_RENT: '生活-房租',
    LIVING_INTERNET: '生活-网络',
    LIVING_GAS: '生活-燃气',
    LIVING_PROPERTY: '生活-物业',
    LIVING_MAINTENANCE: '生活-维修',
    LIVING_WATER: '生活-水费',
    LIVING_ELECTRIC: '生活-电费',
    LIVING_MORTGAGE: '生活-房贷',
    SOCIAL_DONATION: '社交-捐赠',
    SOCIAL_RED_PACKET: '社交-红包',
    SOCIAL_TIP: '社交-小费',
    OTHER_LOSS: '其他-损失',
    OTHER_PENALTY: '其他-罚款',
    OTHER_MISC: '其他-杂项'
  }
  return categoryMap[category] || category
}

const formatPercentage = (value) => {
  return `${value.toFixed(1)}%`
}

onMounted(() => {
  // 检查是否已登录
  if (!authApi.isAuthenticated()) {
    router.replace('/login')
    return
  }
  
  // 检查是否已选择账户
  if (!selectedAccount) {
    router.replace('/accounts')
    return
  }
  
  // 加载当前月预算数据
  loadBudgets()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Header -->
    <header class="bg-white border-b border-gray-200 sticky top-0 z-30">
      <div class="flex items-center justify-between px-6 py-4">
        <div class="flex items-center space-x-3">
          <button
            @click="goBack"
            class="text-gray-600 hover:text-primary-600 transition-colors"
          >
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
            </svg>
          </button>
          <div>
            <span class="text-xl font-bold text-gray-900 block">Piggy</span>
            <span class="text-xs text-blue-600 font-medium block -mt-1">Budget 服务</span>
          </div>
        </div>
        
        <div class="flex items-center space-x-4">
          <!-- Selected Account Info -->
          <div v-if="selectedAccount" class="flex items-center space-x-3">
            <div class="bg-primary-50 text-primary-600 px-3 py-1.5 rounded-lg text-sm font-medium">
              {{ selectedAccount.accountName }}
            </div>
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
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <div class="flex items-center justify-between pb-4 border-b border-gray-200">
          <h2 class="text-xl font-semibold text-gray-900">Budget 服务</h2>
          <div class="flex items-center space-x-4">
            <span class="bg-yellow-100 text-yellow-800 text-xs font-medium px-3 py-1 rounded-full">预算管理</span>
            <button
              @click="refreshBudgets"
              :disabled="loadingBudgets"
              class="flex items-center space-x-1 border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none hover:bg-gray-50"
            >
              <svg v-if="!loadingBudgets" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
              </svg>
              <div v-else class="animate-spin rounded-full h-4 w-4 border-t-2 border-b-2 border-gray-500"></div>
              <span>刷新</span>
            </button>
          </div>
        </div>
        <div class="space-y-4">
          <p class="text-gray-600">管理您的预算计划，跟踪支出情况。</p>
          
          <!-- 预算列表 -->
          <div v-if="!loadingBudgets" class="space-y-4 max-h-[600px] overflow-y-auto pr-2">
            <div v-if="budgets.length === 0" class="bg-gray-50 rounded-lg p-6 text-center">
              <p class="text-gray-600 mb-4">暂无预算计划</p>
              <button @click="openBudgetModal" class="btn-primary px-4 py-2">创建预算</button>
            </div>
            
            <div v-else class="space-y-4">
              <div v-for="(budget, index) in budgets" :key="index" class="bg-white border border-gray-200 rounded-lg p-4">
                <div class="flex items-center justify-between mb-3">
                  <h4 class="font-medium text-gray-900">{{ getCategoryText(budget.category) }}</h4>
                </div>
                
                <div class="space-y-2">
                  <div class="flex justify-between">
                    <span class="text-sm text-gray-600">预算金额</span>
                    <span class="text-sm font-medium text-gray-900">{{ formatBalance(budget.amount || 0) }}</span>
                  </div>
                  <div class="flex justify-between">
                    <span class="text-sm text-gray-600">已使用</span>
                    <span class="text-sm font-medium text-gray-900">{{ formatBalance(budget.usedAmount || 0) }}</span>
                  </div>
                  <div class="flex justify-between">
                    <span class="text-sm text-gray-600">剩余</span>
                    <span class="text-sm font-medium text-green-600">{{ formatBalance(budget.remainingAmount || 0) }}</span>
                  </div>
                  <div class="flex justify-between">
                    <span class="text-sm text-gray-600">使用比例</span>
                    <span :class="[
                      'text-sm font-medium',
                      (budget.usageRate || 0) >= 100 ? 'text-red-600' : (budget.usageRate || 0) >= 80 ? 'text-yellow-600' : 'text-green-600'
                    ]">
                      {{ formatPercentage(budget.usageRate || 0) }}
                    </span>
                  </div>
                </div>
                
                <!-- 进度条 -->
                <div class="mt-4">
                  <div class="w-full bg-gray-200 rounded-full h-2.5">
                    <div 
                      :class="[
                        'h-2.5 rounded-full transition-all duration-500',
                        (budget.usageRate || 0) >= 100 ? 'bg-red-500' : (budget.usageRate || 0) >= 80 ? 'bg-yellow-500' : 'bg-green-500'
                      ]"
                      :style="{ width: `${Math.min(budget.usageRate || 0, 100)}%` }"
                    ></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 加载状态 -->
          <div v-else class="bg-gray-50 rounded-lg p-6 flex justify-center items-center">
            <div class="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-yellow-500"></div>
          </div>
          
          <!-- 操作按钮 -->
          <div class="flex justify-end">
            <button @click="openBudgetModal" class="btn-primary px-4 py-2">设置预算</button>
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

    <!-- 设置预算模态框 -->
    <div v-if="showBudgetModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-xl shadow-xl w-full max-w-md p-6">
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-xl font-bold text-gray-900">设置预算</h3>
          <button
            @click="cancelCreateBudget"
            class="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
            </button>
        </div>

        <!-- Error Message -->
        <div v-if="budgetError" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-4 flex items-center">
          <svg class="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/>
          </svg>
          {{ budgetError }}
        </div>

        <!-- Budget Form -->
        <form @submit.prevent="handleCreateBudget" class="space-y-4">
          <!-- Month -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              月份
            </label>
            <input
              v-model="budgetForm.month"
              type="month"
              class="border border-gray-300 rounded-md px-3 py-2 w-full focus:outline-none focus:ring-2 focus:ring-primary-500"
              :disabled="creatingBudget"
            />
          </div>

          <!-- Category -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              预算分类
            </label>
            <select
              v-model="budgetForm.category"
              class="border border-gray-300 rounded-md px-3 py-2 w-full focus:outline-none focus:ring-2 focus:ring-primary-500"
              :disabled="creatingBudget"
            >
              <option value="">请选择分类</option>
              <optgroup label="餐饮">
                <option value="FOOD_GROCERY">餐饮- groceries</option>
                <option value="FOOD_SNACK">餐饮-零食</option>
                <option value="FOOD_FRUIT">餐饮-水果</option>
                <option value="FOOD_DRINK">餐饮-饮料</option>
                <option value="FOOD_LUNCH">餐饮-午餐</option>
                <option value="FOOD_DINNER">餐饮-晚餐</option>
                <option value="FOOD_BREAKFAST">餐饮-早餐</option>
                <option value="FOOD_DELIVERY">餐饮-外卖</option>
                <option value="FOOD_TREAT">餐饮-请客</option>
              </optgroup>
              <optgroup label="交通">
                <option value="TRANSPORT_TAXI">交通-出租车</option>
                <option value="TRANSPORT_SUBWAY">交通-地铁</option>
                <option value="TRANSPORT_BUS">交通-公交</option>
                <option value="TRANSPORT_FUEL">交通-燃油</option>
                <option value="TRANSPORT_TOLL">交通-过路费</option>
                <option value="TRANSPORT_TRAIN">交通-火车</option>
                <option value="TRANSPORT_DIDI">交通-滴滴</option>
                <option value="TRANSPORT_PARKING">交通-停车</option>
                <option value="TRANSPORT_BIKE">交通-自行车</option>
                <option value="TRANSPORT_FLIGHT">交通-飞机</option>
              </optgroup>
              <optgroup label="购物">
                <option value="SHOP_HOME">购物-家居</option>
                <option value="SHOP_BAG">购物-包袋</option>
                <option value="SHOP_DIGITAL">购物-数码</option>
                <option value="SHOP_SHOES">购物-鞋子</option>
                <option value="SHOP_GIFT">购物-礼物</option>
                <option value="SHOP_CLOTHES">购物-衣服</option>
                <option value="SHOP_DAILY">购物-日用品</option>
                <option value="SHOP_COSMETIC">购物-化妆品</option>
                <option value="SHOP_BOOK">购物-书籍</option>
              </optgroup>
              <optgroup label="娱乐">
                <option value="ENTERTAIN_GAME">娱乐-游戏</option>
                <option value="ENTERTAIN_MOVIE">娱乐-电影</option>
                <option value="ENTERTAIN_STREAM">娱乐-流媒体</option>
                <option value="ENTERTAIN_KTV">娱乐-KTV</option>
                <option value="ENTERTAIN_GYM">娱乐-健身</option>
                <option value="ENTERTAIN_SPORT">娱乐-运动</option>
                <option value="ENTERTAIN_TRAVEL">娱乐-旅行</option>
              </optgroup>
              <optgroup label="医疗">
                <option value="MEDICAL_DENTAL">医疗-牙科</option>
                <option value="MEDICAL_CHECKUP">医疗-体检</option>
                <option value="MEDICAL_MEDICINE">医疗-药品</option>
                <option value="MEDICAL_INSURANCE">医疗-保险</option>
                <option value="MEDICAL_HOSPITAL">医疗-住院</option>
              </optgroup>
              <optgroup label="教育">
                <option value="EDUCATION_TUITION">教育-学费</option>
                <option value="EDUCATION_EXAM">教育-考试</option>
                <option value="EDUCATION_MATERIAL">教育-教材</option>
                <option value="EDUCATION_COURSE">教育-课程</option>
              </optgroup>
              <optgroup label="生活">
                <option value="LIVING_RENT">生活-房租</option>
                <option value="LIVING_INTERNET">生活-网络</option>
                <option value="LIVING_GAS">生活-燃气</option>
                <option value="LIVING_PROPERTY">生活-物业</option>
                <option value="LIVING_MAINTENANCE">生活-维修</option>
                <option value="LIVING_WATER">生活-水费</option>
                <option value="LIVING_ELECTRIC">生活-电费</option>
                <option value="LIVING_MORTGAGE">生活-房贷</option>
              </optgroup>
              <optgroup label="社交">
                <option value="SOCIAL_DONATION">社交-捐赠</option>
                <option value="SOCIAL_RED_PACKET">社交-红包</option>
                <option value="SOCIAL_TIP">社交-小费</option>
              </optgroup>
              <optgroup label="其他">
                <option value="OTHER_LOSS">其他-损失</option>
                <option value="OTHER_PENALTY">其他-罚款</option>
                <option value="OTHER_MISC">其他-杂项</option>
              </optgroup>
            </select>
          </div>

          <!-- Budget Amount -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              预算金额
            </label>
            <input
              v-model.number="budgetForm.amount"
              type="number"
              min="0"
              step="0.01"
              placeholder="请输入预算金额"
              class="border border-gray-300 rounded-md px-3 py-2 w-full focus:outline-none focus:ring-2 focus:ring-primary-500"
              :disabled="creatingBudget"
            />
          </div>

          <!-- Action Buttons -->
          <div class="flex space-x-4 pt-4">
            <button
              type="button"
              @click="cancelCreateBudget"
              class="flex-1 border border-gray-300 rounded-md px-4 py-2 bg-gray-50 text-gray-700 hover:bg-gray-100 transition-colors"
              :disabled="creatingBudget"
            >
              取消
            </button>
            <button
              type="submit"
              class="flex-1 bg-primary-600 text-white rounded-md px-4 py-2 hover:bg-primary-700 transition-colors"
              :disabled="creatingBudget"
            >
              <svg v-if="creatingBudget" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white inline" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              <span v-if="creatingBudget">创建中...</span>
              <span v-else>创建预算</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
