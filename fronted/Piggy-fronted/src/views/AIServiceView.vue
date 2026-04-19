<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/auth'
import { aiClassify } from '@/api/transaction'

const router = useRouter()
const selectedAccount = JSON.parse(localStorage.getItem('selectedAccount'))
const aiInput = ref('')
const aiResponse = ref('')
const isLoading = ref(false)

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

const getAIAdvice = async () => {
  if (!aiInput.value.trim()) {
    return
  }
  
  isLoading.value = true
  try {
    // 调用后端API获取AI建议
    console.log('获取AI建议:', aiInput.value)
    
    const response = await aiClassify({
      input: aiInput.value,
      userId: authApi.getUserId()
    })
    
    console.log('API响应:', response)
    
    // 从 response.data 中获取实际数据
    if (response && response.data) {
      const data = response.data
      aiResponse.value = `根据您的输入，我为您提供以下分析：

1. 交易类型：${data.transactionType === 'EXPENSE' ? '支出' : data.transactionType === 'INCOME' ? '收入' : '转账'}
2. 分类：${getCategoryText(data.category)}
3. 金额：${formatBalance(Math.abs(data.amount || 0))}
4. 描述：${data.description || '无'}
5. 商户：${data.merchant || '未知商户'}
6. 置信度：${((data.confidence || 0) * 100).toFixed(1)}%

希望这些分析对您有所帮助！`
    } else {
      console.error('API响应结构不正确:', response)
      aiResponse.value = '抱歉，无法获取AI建议，请稍后重试。'
    }
  } catch (error) {
    console.error('获取AI建议失败:', error)
    console.error('错误详情:', error.message)
    aiResponse.value = '抱歉，获取AI建议失败，请稍后重试。'
  } finally {
    isLoading.value = false
  }
}

// 获取分类文本
const getCategoryText = (category) => {
  const textMap = {
    FOOD: '餐饮',
    TRANSPORT: '交通',
    SHOPPING: '购物',
    ENTERTAINMENT: '娱乐',
    SALARY: '工资',
    INVESTMENT: '投资',
    MEDICAL: '医疗',
    EDUCATION: '教育',
    HOUSING: '住房',
    OTHER: '其他'
  }
  return textMap[category] || category || '未分类'
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
            <span class="text-xs text-blue-600 font-medium block -mt-1">AI 服务</span>
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
          <h2 class="text-xl font-semibold text-gray-900">AI 服务</h2>
          <span class="bg-indigo-100 text-indigo-800 text-xs font-medium px-3 py-1 rounded-full">智能分析</span>
        </div>
        <div class="space-y-6">
          <p class="text-gray-600">获取智能财务建议和分析，优化您的财务管理。</p>
          
          <!-- AI 交互界面 -->
          <div class="bg-gray-50 rounded-lg p-6">
            <h4 class="font-medium text-gray-900 mb-4">智能财务建议</h4>
            <div class="space-y-4">
              <div class="bg-white p-4 rounded-lg border border-gray-200">
                <h5 class="font-medium text-gray-900 mb-3">AI 助手</h5>
                <p class="text-sm text-gray-600 mb-4">您好！我是您的智能财务助手，有什么可以帮助您的吗？</p>
                
                <div v-if="aiResponse" class="bg-gray-50 p-3 rounded-lg mb-4">
                  <p class="text-sm text-gray-700 whitespace-pre-line">{{ aiResponse }}</p>
                </div>
                
                <div class="flex space-x-2">
                  <input 
                    v-model="aiInput" 
                    type="text" 
                    placeholder="请输入您的财务问题..." 
                    class="flex-1 border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary-500"
                  />
                  <button 
                    @click="getAIAdvice" 
                    :disabled="isLoading" 
                    class="btn-primary px-4 py-2"
                  >
                    <svg v-if="!isLoading" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"></path>
                    </svg>
                    <div v-else class="animate-spin rounded-full h-5 w-5 border-t-2 border-b-2 border-white"></div>
                  </button>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 推荐功能 -->
          <div class="bg-gray-50 rounded-lg p-6">
            <h4 class="font-medium text-gray-900 mb-4">推荐功能</h4>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div class="bg-white p-4 rounded-lg border border-gray-200">
                <h5 class="font-medium text-gray-900 mb-2">智能分类</h5>
                <p class="text-sm text-gray-600 mb-3">自动识别交易类别，帮助您更好地管理支出。</p>
                <button class="btn-primary text-sm px-4 py-2">使用功能</button>
              </div>
              <div class="bg-white p-4 rounded-lg border border-gray-200">
                <h5 class="font-medium text-gray-900 mb-2">消费分析</h5>
                <p class="text-sm text-gray-600 mb-3">分析您的消费习惯，提供个性化的省钱建议。</p>
                <button class="btn-primary text-sm px-4 py-2">使用功能</button>
              </div>
            </div>
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
  </div>
</template>
