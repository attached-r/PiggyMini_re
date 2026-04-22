<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/auth'
import { getAccounts } from '@/api/account'
import { classifyTransaction, queryAI, analyzeConsumption } from '@/api/ai'
import Sidebar from '@/components/Sidebar.vue'

const router = useRouter()

// 侧边栏状态
const sidebarCollapsed = ref(false)

// 账户相关
const allAccounts = ref([])
const selectedAccountId = ref(null)
const selectedAccount = ref(null)

// AI 相关
const aiInput = ref('')
const aiResponse = ref(null)  // 存储 ClassifyResponse
const queryResult = ref('')
const isLoading = ref(false)
const activeTab = ref('classify')  // classify | query | analyze

// 加载账户列表
const loadAccounts = async () => {
  try {
    const response = await getAccounts()
    let accountList = []
    if (response.data?.records) {
      accountList = response.data.records
    } else if (Array.isArray(response.data)) {
      accountList = response.data
    }
    allAccounts.value = accountList
    
    // 恢复上次选择或默认第一个
    const savedId = localStorage.getItem('selectedAccountId')
    if (savedId && accountList.find(a => a.id === Number(savedId))) {
      selectedAccountId.value = Number(savedId)
      selectedAccount.value = accountList.find(a => a.id === Number(savedId))
    } else if (accountList.length > 0) {
      selectedAccountId.value = accountList[0].id
      selectedAccount.value = accountList[0]
    }
  } catch (error) {
    console.error('加载账户列表失败:', error)
  }
}

// 账户变更处理
const onAccountChange = () => {
  if (selectedAccountId.value) {
    localStorage.setItem('selectedAccountId', selectedAccountId.value)
    selectedAccount.value = allAccounts.value.find(a => a.id === Number(selectedAccountId.value))
  }
}

// 格式化金额
const formatBalance = (balance) => {
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    minimumFractionDigits: 2
  }).format(balance || 0)
}

// 格式化分类名称
const getCategoryText = (category) => {
  const textMap = {
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
    'SALARY': { label: '工资', icon: '💰', color: '#10B981' },
    'INVESTMENT': { label: '投资', icon: '📈', color: '#10B981' },
    'OTHER': { label: '其他', icon: '📦', color: '#94A3B8' }
  }
  return textMap[category] || { label: category, icon: '🏷️', color: '#94A3B8' }
}

// 获取交易类型文本
const getTransactionTypeText = (type) => {
  return type === 'EXPENSE' ? '支出' : type === 'INCOME' ? '收入' : '转账'
}

// 智能分类 - 对应后端 POST /internal/ai/classify
const handleClassify = async () => {
  if (!aiInput.value.trim()) {
    return
  }
  
  isLoading.value = true
  aiResponse.value = null
  try {
    console.log('智能分类输入:', aiInput.value)
    
    // 调用后端 POST /api/ai/classify
    const response = await classifyTransaction(aiInput.value)
    
    console.log('分类响应:', response)
    
    // 后端返回: { code: 200, data: ClassifyResponse, message: "..." }
    // ClassifyResponse: { amount, category, description, merchant, transactionType, confidence }
    if (response && response.code === 200 && response.data) {
      aiResponse.value = response.data
    } else {
      console.error('API响应格式错误:', response)
      aiResponse.value = null
    }
  } catch (error) {
    console.error('智能分类失败:', error)
    aiResponse.value = null
  } finally {
    isLoading.value = false
  }
}

// 自然语言查询 - 对应后端 POST /internal/ai/query
const handleQuery = async () => {
  if (!aiInput.value.trim()) {
    return
  }
  
  isLoading.value = true
  queryResult.value = ''
  try {
    console.log('自然语言查询:', aiInput.value)
    
    // 调用后端 POST /api/ai/query
    const response = await queryAI(aiInput.value)
    
    console.log('查询响应:', response)
    
    // 后端返回: { code: 200, data: "回答字符串", message: "..." }
    if (response && response.code === 200 && response.data) {
      queryResult.value = response.data
    } else {
      console.error('API响应格式错误:', response)
    }
  } catch (error) {
    console.error('查询失败:', error)
  } finally {
    isLoading.value = false
  }
}

// 消费分析 - 对应后端 POST /internal/ai/analyze
const handleAnalyze = async () => {
  isLoading.value = true
  queryResult.value = ''
  try {
    console.log('消费分析请求')
    
    // 调用后端 POST /api/ai/analyze
    const response = await analyzeConsumption({
      period: 'month'
    })
    
    console.log('分析响应:', response)
    
    // 后端返回: { code: 200, data: "分析报告字符串", message: "..." }
    if (response && response.code === 200 && response.data) {
      queryResult.value = response.data
    } else {
      console.error('API响应格式错误:', response)
    }
  } catch (error) {
    console.error('分析失败:', error)
  } finally {
    isLoading.value = false
  }
}

// 获取用户名
const username = computed(() => {
  return authApi.getNickname() || authApi.getUsername() || '用户'
})

// 问候语
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

onMounted(async () => {
  // 检查是否已登录
  if (!authApi.isAuthenticated()) {
    router.replace('/login')
    return
  }
  
  // 加载账户列表
  await loadAccounts()
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
              <p class="text-slate-400 text-sm">AI 智能财务分析服务</p>
            </div>
            
            <div class="flex items-center space-x-4">
              <!-- 账户选择 -->
              <select 
                v-model="selectedAccountId"
                @change="onAccountChange"
                class="bg-slate-800/50 border border-slate-600/50 text-slate-200 rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              >
                <option v-for="acc in allAccounts" :key="acc.id" :value="acc.id">
                  {{ acc.accountName }} - {{ formatBalance(acc.balance) }}
                </option>
              </select>
            </div>
          </div>
        </div>
      </header>

      <!-- 页面内容 -->
      <div class="max-w-7xl mx-auto px-6 py-8">
        <!-- 账户概览卡片 -->
        <div v-if="selectedAccount" class="bg-gradient-to-br from-indigo-600 to-purple-600 rounded-2xl p-6 mb-8 shadow-xl shadow-indigo-500/20">
          <div class="flex items-center justify-between">
            <div class="flex items-center space-x-4">
              <div class="w-12 h-12 bg-white/20 rounded-xl flex items-center justify-center text-2xl">
                {{ selectedAccount.icon || '💳' }}
              </div>
              <div>
                <p class="text-indigo-200 text-sm">当前账户</p>
                <p class="text-white font-semibold text-lg">{{ selectedAccount.accountName }}</p>
              </div>
            </div>
            <div class="text-right">
              <p class="text-indigo-200 text-sm">账户余额</p>
              <p class="text-white font-bold text-2xl">{{ formatBalance(selectedAccount.balance) }}</p>
            </div>
          </div>
        </div>

        <!-- 功能标签页 -->
        <div class="bg-slate-800/50 border border-slate-700/50 rounded-2xl p-6 mb-8">
          <div class="flex items-center space-x-2 mb-6">
            <button 
              @click="activeTab = 'classify'"
              class="px-4 py-2 rounded-lg text-sm font-medium transition-colors"
              :class="activeTab === 'classify' ? 'bg-pink-500 text-white' : 'bg-slate-700/50 text-slate-300 hover:bg-slate-700'"
            >
              🤖 智能分类
            </button>
            <button 
              @click="activeTab = 'query'"
              class="px-4 py-2 rounded-lg text-sm font-medium transition-colors"
              :class="activeTab === 'query' ? 'bg-indigo-500 text-white' : 'bg-slate-700/50 text-slate-300 hover:bg-slate-700'"
            >
              💬 智能问答
            </button>
            <button 
              @click="activeTab = 'analyze'"
              class="px-4 py-2 rounded-lg text-sm font-medium transition-colors"
              :class="activeTab === 'analyze' ? 'bg-violet-500 text-white' : 'bg-slate-700/50 text-slate-300 hover:bg-slate-700'"
            >
              📊 消费分析
            </button>
          </div>

          <!-- 智能分类 -->
          <div v-show="activeTab === 'classify'">
            <div class="bg-slate-900/50 rounded-xl p-4 mb-4">
              <textarea 
                v-model="aiInput" 
                placeholder="描述您的交易，例如：中午在肯德基吃了午餐花了35元..."
                class="w-full bg-transparent text-white placeholder-slate-500 resize-none focus:outline-none"
                rows="3"
                @keydown.enter.ctrl="handleClassify"
              ></textarea>
            </div>

            <div class="flex items-center justify-between mb-4">
              <p class="text-slate-500 text-xs">按 Ctrl + Enter 快速发送</p>
              <button 
                @click="handleClassify" 
                :disabled="isLoading || !aiInput.trim()"
                class="flex items-center space-x-2 bg-pink-500 hover:bg-pink-600 disabled:bg-slate-600 disabled:cursor-not-allowed text-white px-6 py-2.5 rounded-xl transition-colors"
              >
                <span v-if="isLoading" class="animate-spin rounded-full h-5 w-5 border-t-2 border-b-2 border-white"></span>
                <span v-else>🚀</span>
                <span class="font-medium">{{ isLoading ? '分析中...' : '智能分类' }}</span>
              </button>
            </div>

            <!-- 分类结果 -->
            <div v-if="aiResponse" class="bg-slate-900/50 rounded-xl p-5">
              <h4 class="text-white font-semibold mb-4">📋 分类结果</h4>
              <div class="grid grid-cols-2 md:grid-cols-3 gap-4">
                <div class="bg-slate-800/50 rounded-lg p-3">
                  <p class="text-slate-400 text-xs mb-1">交易类型</p>
                  <p class="text-white font-medium">{{ getTransactionTypeText(aiResponse.transactionType) }}</p>
                </div>
                <div class="bg-slate-800/50 rounded-lg p-3">
                  <p class="text-slate-400 text-xs mb-1">识别金额</p>
                  <p class="text-emerald-400 font-bold">{{ formatBalance(aiResponse.amount) }}</p>
                </div>
                <div class="bg-slate-800/50 rounded-lg p-3">
                  <p class="text-slate-400 text-xs mb-1">分类</p>
                  <p class="text-white font-medium flex items-center">
                    {{ getCategoryText(aiResponse.category).icon }}
                    {{ getCategoryText(aiResponse.category).label }}
                  </p>
                </div>
                <div class="bg-slate-800/50 rounded-lg p-3">
                  <p class="text-slate-400 text-xs mb-1">商户</p>
                  <p class="text-white font-medium">{{ aiResponse.merchant || '未知' }}</p>
                </div>
                <div class="bg-slate-800/50 rounded-lg p-3">
                  <p class="text-slate-400 text-xs mb-1">描述</p>
                  <p class="text-white font-medium">{{ aiResponse.description || '无' }}</p>
                </div>
                <div class="bg-slate-800/50 rounded-lg p-3">
                  <p class="text-slate-400 text-xs mb-1">置信度</p>
                  <p class="text-indigo-400 font-medium">{{ ((aiResponse.confidence || 0) * 100).toFixed(0) }}%</p>
                </div>
              </div>
            </div>
          </div>

          <!-- 智能问答 -->
          <div v-show="activeTab === 'query'">
            <div class="bg-slate-900/50 rounded-xl p-4 mb-4">
              <textarea 
                v-model="aiInput" 
                placeholder="输入您的问题，例如：这个月我的支出情况如何？"
                class="w-full bg-transparent text-white placeholder-slate-500 resize-none focus:outline-none"
                rows="3"
                @keydown.enter.ctrl="handleQuery"
              ></textarea>
            </div>

            <div class="flex items-center justify-between mb-4">
              <p class="text-slate-500 text-xs">按 Ctrl + Enter 快速发送</p>
              <button 
                @click="handleQuery" 
                :disabled="isLoading || !aiInput.trim()"
                class="flex items-center space-x-2 bg-indigo-500 hover:bg-indigo-600 disabled:bg-slate-600 disabled:cursor-not-allowed text-white px-6 py-2.5 rounded-xl transition-colors"
              >
                <span v-if="isLoading" class="animate-spin rounded-full h-5 w-5 border-t-2 border-b-2 border-white"></span>
                <span v-else>💬</span>
                <span class="font-medium">{{ isLoading ? '思考中...' : '提问' }}</span>
              </button>
            </div>

            <div v-if="queryResult" class="bg-slate-900/50 rounded-xl p-5">
              <h4 class="text-white font-semibold mb-3">💡 AI 回答</h4>
              <p class="text-slate-300 leading-relaxed whitespace-pre-line">{{ queryResult }}</p>
            </div>
          </div>

          <!-- 消费分析 -->
          <div v-show="activeTab === 'analyze'">
            <div class="bg-slate-900/50 rounded-xl p-4 mb-4">
              <p class="text-slate-400 text-sm mb-3">点击下方按钮获取本月消费分析报告</p>
              <button 
                @click="handleAnalyze" 
                :disabled="isLoading"
                class="flex items-center space-x-2 bg-violet-500 hover:bg-violet-600 disabled:bg-slate-600 disabled:cursor-not-allowed text-white px-6 py-2.5 rounded-xl transition-colors"
              >
                <span v-if="isLoading" class="animate-spin rounded-full h-5 w-5 border-t-2 border-b-2 border-white"></span>
                <span v-else>📊</span>
                <span class="font-medium">{{ isLoading ? '分析中...' : '生成分析报告' }}</span>
              </button>
            </div>

            <div v-if="queryResult" class="bg-slate-900/50 rounded-xl p-5">
              <h4 class="text-white font-semibold mb-3">📈 消费分析报告</h4>
              <p class="text-slate-300 leading-relaxed whitespace-pre-line">{{ queryResult }}</p>
            </div>
          </div>
        </div>

        <!-- 快捷功能 -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
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
            @click="router.push('/reports')"
            class="bg-slate-800/50 border border-slate-700/50 rounded-xl p-6 hover:bg-slate-700/50 hover:border-slate-600/50 transition-all group"
          >
            <div class="w-12 h-12 bg-violet-500/20 rounded-xl flex items-center justify-center mb-4 group-hover:bg-violet-500/30 transition-colors">
              <span class="text-2xl">📈</span>
            </div>
            <h3 class="text-white font-semibold mb-1">查看报表</h3>
            <p class="text-slate-400 text-sm">分析收支情况</p>
          </button>
        </div>
      </div>
    </main>
  </div>
</template>
