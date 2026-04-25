<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/auth'
import { getAccounts } from '@/api/account'
import { classifyTransaction, queryAI, analyzeConsumption } from '@/api/ai'
import { createTransaction } from '@/api/transaction'  // lxy: 引入创建交易 API
import Sidebar from '@/components/Sidebar.vue'
import CustomModal from '@/components/CustomModal.vue'
import ToastContainer from '@/components/ToastContainer.vue'

const router = useRouter()

// lxy: Toast 引用
const toastRef = ref(null)

// lxy: 弹窗状态
const modalState = ref({
  show: false,
  type: 'info',
  title: '',
  message: '',
  showCancel: false,
  onConfirm: null,
  onCancel: null
})

// lxy: 显示 Toast 提示
const showToast = (type, message, duration = 3000) => {
  if (toastRef.value) {
    toastRef.value[type](message, duration)
  }
}

// lxy: 显示弹窗
const showModal = (options) => {
  modalState.value = {
    show: true,
    type: options.type || 'info',
    title: options.title || '',
    message: options.message || '',
    showCancel: options.showCancel || false,
    onConfirm: options.onConfirm || null,
    onCancel: options.onCancel || null
  }
}

// lxy: 关闭弹窗
const closeModal = () => {
  modalState.value.show = false
}

// lxy: 弹窗确认回调
const handleModalConfirm = () => {
  if (modalState.value.onConfirm) {
    modalState.value.onConfirm()
  }
  closeModal()
}

// lxy: 弹窗取消回调
const handleModalCancel = () => {
  if (modalState.value.onCancel) {
    modalState.value.onCancel()
  }
  closeModal()
}

// lxy: 侧边栏状态
const sidebarCollapsed = ref(false)

// lxy: 账户相关
const allAccounts = ref([])
const selectedAccountId = ref(null)
const selectedAccount = ref(null)

// lxy: AI 相关
const aiInput = ref('')
const aiResponse = ref(null)  // lxy: 存储 ClassifyResponse
const queryResult = ref('')
const isLoading = ref(false)
const activeTab = ref('classify')  // classify | query | analyze

// lxy: 保存状态
const saveLoading = ref(false)  // lxy: 保存中状态
const saveSuccess = ref(false)  // lxy: 保存成功状态

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

// lxy: 中文分类名到英文枚举值的映射
const categoryMap = {
  '餐饮': 'FOOD_LUNCH',
  '早餐': 'FOOD_BREAKFAST',
  '午餐': 'FOOD_LUNCH',
  '晚餐': 'FOOD_DINNER',
  '零食': 'FOOD_SNACK',
  '水果': 'FOOD_FRUIT',
  '饮品': 'FOOD_DRINK',
  '买菜': 'FOOD_GROCERY',
  '聚餐': 'FOOD_TREAT',
  '外卖': 'FOOD_DELIVERY',
  '交通': 'TRANSPORT_SUBWAY',
  '地铁': 'TRANSPORT_SUBWAY',
  '公交': 'TRANSPORT_BUS',
  '打车': 'TRANSPORT_TAXI',
  '网约车': 'TRANSPORT_DIDI',
  '加油': 'TRANSPORT_FUEL',
  '购物': 'SHOP_DAILY',
  '服饰': 'SHOP_CLOTHES',
  '数码': 'SHOP_DIGITAL',
  '日用品': 'SHOP_DAILY',
  '居住': 'LIVING_RENT',
  '房租': 'LIVING_RENT',
  '电费': 'LIVING_ELECTRIC',
  '水费': 'LIVING_WATER',
  '娱乐': 'ENTERTAIN_MOVIE',
  '电影': 'ENTERTAIN_MOVIE',
  '游戏': 'ENTERTAIN_GAME',
  '旅游': 'ENTERTAIN_TRAVEL',
  '医疗': 'MEDICAL_MEDICINE',
  '买药': 'MEDICAL_MEDICINE',
  '教育': 'EDUCATION_TUITION',
  '学费': 'EDUCATION_TUITION',
  '工资': 'SALARY',
  '收入': 'SALARY',
  '投资': 'INVESTMENT',
  '其他': 'OTHER_MISC',
  '杂项': 'OTHER_MISC'
}

// lxy: 将中文分类名转换为后端枚举值
const getCategoryEnum = (category) => {
  if (!category) return null
  // 如果已经是枚举值，直接返回
  if (category.startsWith('FOOD_') || category.startsWith('TRANSPORT_') ||
      category.startsWith('SHOP_') || category.startsWith('LIVING_') ||
      category.startsWith('ENTERTAIN_') || category.startsWith('MEDICAL_') ||
      category.startsWith('EDUCATION_') || category.startsWith('SOCIAL_') ||
      category.startsWith('OTHER_') || category === 'SALARY' || category === 'INVESTMENT') {
    return category
  }
  // 尝试转换为中文映射
  return categoryMap[category] || 'OTHER_MISC'
}

// lxy: 格式化分类名称（用于显示）
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
    'OTHER_MISC': { label: '其他', icon: '📦', color: '#94A3B8' }
  }
  return textMap[category] || { label: category, icon: '🏷️', color: '#94A3B8' }
}

// 获取交易类型文本
const getTransactionTypeText = (type) => {
  return type === 'EXPENSE' ? '支出' : type === 'INCOME' ? '收入' : '转账'
}

// lxy: 智能分类 - 对应后端 POST /internal/ai/classify
const handleClassify = async () => {
  if (!aiInput.value.trim()) {
    return
  }

  isLoading.value = true
  aiResponse.value = null
  saveSuccess.value = false  // lxy: 重置保存状态
  try {
    console.log('智能分类输入:', aiInput.value)

    // 调用后端 POST /api/ai/classify
    const response = await classifyTransaction(aiInput.value)

    console.log('分类响应:', response)

    // lxy: 后端返回: { code: 200, data: ClassifyResponse, message: "..." }
    // lxy: ClassifyResponse: { amount, category, description, merchant, transactionType, confidence }
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

// lxy: 保存交易记录到指定账户
const handleSaveTransaction = async () => {
  // lxy: 检查是否有分类结果和选择的账户
  if (!aiResponse.value) {
    console.error('没有分类结果，无法保存')
    return
  }
  if (!selectedAccountId.value) {
    showModal({
      type: 'warning',
      title: '提示',
      message: '请先选择一个账户'
    })
    return
  }

  // lxy: 获取当前选中的账户信息
  const currentAccount = allAccounts.value.find(a => a.id === Number(selectedAccountId.value))
  if (!currentAccount) {
    showModal({
      type: 'error',
      title: '错误',
      message: '账户信息获取失败，请刷新页面重试'
    })
    return
  }

  // lxy: 检查余额是否足够（支出类型且金额大于余额时提示）
  const isExpense = (aiResponse.value.transactionType || 'EXPENSE') === 'EXPENSE'
  const amount = Math.abs(aiResponse.value.amount)
  const currentBalance = Number(currentAccount.balance) || 0
  if (isExpense && currentBalance < amount) {
    showModal({
      type: 'warning',
      title: '余额不足',
      message: `账户「${currentAccount.accountName}」余额不足！\n\n当前余额: ¥${currentBalance.toFixed(2)}\n需要支付: ¥${amount.toFixed(2)}\n\n请选择其他账户或先充值。`
    })
    return
  }

  saveLoading.value = true
  try {
    // lxy: 构建交易数据
    const transactionData = {
      accountId: selectedAccountId.value,  // lxy: 用户选择的账户
      type: aiResponse.value.transactionType || 'EXPENSE',  // lxy: 交易类型
      amount: amount,  // lxy: 金额（取绝对值）
      category: getCategoryEnum(aiResponse.value.category),  // lxy: 将中文分类转为枚举值
      description: aiResponse.value.description || '',  // lxy: 描述
      remark: aiResponse.value.merchant || '',  // lxy: 商户信息放入备注
      tradeTime: new Date().toISOString()  // lxy: 交易时间
    }

    console.log('保存交易数据:', transactionData)

    // lxy: 调用创建交易接口
    const response = await createTransaction(transactionData)

    if (response && response.code === 200) {
      console.log('交易保存成功:', response)
      saveSuccess.value = true  // lxy: 标记保存成功

      // lxy: 更新本地账户余额显示（减少余额）
      if (isExpense) {
        currentAccount.balance -= amount
      } else {
        currentAccount.balance += amount
      }

      // lxy: 显示成功提示
      showToast('success', '交易记录保存成功！')
      
      // lxy: 提示用户并可选跳转
      setTimeout(() => {
        showModal({
          type: 'info',
          title: '保存成功',
          message: '交易记录已保存！是否前往查看交易列表？',
          showCancel: true,
          cancelText: '继续使用',
          confirmText: '查看列表',
          onConfirm: () => {
            router.push('/transactions')
          },
          onCancel: () => {}
        })
      }, 500)
    } else {
      console.error('保存失败:', response)
      showModal({
        type: 'error',
        title: '保存失败',
        message: response?.message || '未知错误，请稍后重试'
      })
    }
  } catch (error) {
    console.error('保存交易失败:', error)
    // lxy: 改进错误提示，显示后端返回的具体错误信息
    const errorMsg = error?.response?.data?.message || error?.message || '网络错误'
    if (errorMsg.includes('余额不足')) {
      showModal({
        type: 'warning',
        title: '保存失败',
        message: '账户余额不足，请选择其他账户或先充值。'
      })
    } else {
      showModal({
        type: 'error',
        title: '保存失败',
        message: errorMsg
      })
    }
  } finally {
    saveLoading.value = false
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
    <!-- Toast 提示组件 -->
    <ToastContainer ref="toastRef" />
    
    <!-- 自定义弹窗组件 -->
    <CustomModal 
      :show="modalState.show"
      :type="modalState.type"
      :title="modalState.title"
      :message="modalState.message"
      :show-cancel="modalState.showCancel"
      :cancel-text="modalState.cancelText || '取消'"
      :confirm-text="modalState.confirmText || '确定'"
      @close="closeModal"
      @confirm="handleModalConfirm"
      @cancel="handleModalCancel"
    />

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

            <!-- lxy: 分类结果 -->
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

              <!-- lxy: 保存操作区域 -->
              <div class="mt-6 pt-4 border-t border-slate-700/50">
                <div class="flex flex-col md:flex-row items-start md:items-center gap-4">
                  <!-- lxy: 账户选择下拉框 -->
                  <div class="flex items-center gap-2">
                    <label class="text-slate-400 text-sm">保存到账户:</label>
                    <select
                      v-model="selectedAccountId"
                      @change="onAccountChange"
                      class="bg-slate-700/50 border border-slate-600/50 text-white rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    >
                      <option v-for="acc in allAccounts" :key="acc.id" :value="acc.id">
                        {{ acc.accountName }}
                      </option>
                    </select>
                  </div>

                  <!-- lxy: 保存按钮 -->
                  <button
                    v-if="!saveSuccess"
                    @click="handleSaveTransaction"
                    :disabled="saveLoading || !selectedAccountId"
                    class="flex items-center space-x-2 bg-emerald-500 hover:bg-emerald-600 disabled:bg-slate-600 disabled:cursor-not-allowed text-white px-6 py-2.5 rounded-xl transition-colors"
                  >
                    <span v-if="saveLoading" class="animate-spin rounded-full h-5 w-5 border-t-2 border-b-2 border-white"></span>
                    <span v-else>💾</span>
                    <span class="font-medium">{{ saveLoading ? '保存中...' : '保存到账户' }}</span>
                  </button>

                  <!-- lxy: 保存成功提示 -->
                  <div v-if="saveSuccess" class="flex items-center space-x-2 text-emerald-400">
                    <span>✅</span>
                    <span class="font-medium">已保存到 {{ selectedAccount?.accountName }}</span>
                  </div>
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
