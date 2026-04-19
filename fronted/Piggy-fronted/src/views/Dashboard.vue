<script setup>
import { ref, onMounted, computed } from 'vue'

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const currentDate = computed(() => {
  const now = new Date()
  return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日`
})

const quickActions = ref([
  { id: 1, label: '记支出', icon: '💸', color: 'bg-red-500', path: '/transactions?type=expense' },
  { id: 2, label: '记收入', icon: '💰', color: 'bg-green-500', path: '/transactions?type=income' },
  { id: 3, label: '做预算', icon: '📊', color: 'bg-blue-500', path: '/budgets' },
  { id: 4, label: '看报表', icon: '📈', color: 'bg-purple-500', path: '/reports' },
])

const navigateTo = (path) => {
  window.location.href = path
}
</script>

<template>
  <div class="p-6 max-w-6xl mx-auto">
    <!-- Header -->
    <div class="mb-8">
      <h1 class="text-2xl font-bold text-gray-900">{{ greeting }} 👋</h1>
      <p class="text-gray-500 mt-1">{{ currentDate }}</p>
    </div>

    <!-- Quick Actions -->
    <div class="mb-8">
      <h2 class="text-lg font-semibold text-gray-800 mb-4">快捷操作</h2>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <button
          v-for="action in quickActions"
          :key="action.id"
          @click="navigateTo(action.path)"
          class="flex flex-col items-center p-4 bg-white rounded-xl shadow-sm border border-gray-100 hover:shadow-md hover:border-gray-200 transition-all"
        >
          <span class="text-3xl mb-2">{{ action.icon }}</span>
          <span class="text-sm font-medium text-gray-700">{{ action.label }}</span>
        </button>
      </div>
    </div>

    <!-- Welcome Card -->
    <div class="bg-gradient-to-r from-blue-500 to-blue-600 rounded-2xl p-6 text-white">
      <h2 class="text-xl font-bold mb-2">欢迎使用 Piggy 记账</h2>
      <p class="text-blue-100">记录每一笔收支，管理你的财务生活</p>
      <div class="mt-4 flex gap-4">
        <div class="bg-white/20 rounded-lg px-4 py-2">
          <p class="text-xs text-blue-100">累计记账</p>
          <p class="text-lg font-bold">便捷高效</p>
        </div>
        <div class="bg-white/20 rounded-lg px-4 py-2">
          <p class="text-xs text-blue-100">智能分析</p>
          <p class="text-lg font-bold">一目了然</p>
        </div>
      </div>
    </div>
  </div>
</template>
