<script setup>
import { TrendingUp, TrendingDown, Wallet } from 'lucide-vue-next';

defineProps({
  balance: {
    type: Number,
    default: 0
  },
  income: {
    type: Number,
    default: 0
  },
  expense: {
    type: Number,
    default: 0
  },
  currency: {
    type: String,
    default: 'CNY'
  }
});

const formatCurrency = (value) => {
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
  }).format(value);
};
</script>

<template>
  <div class="glass-card p-6 flex flex-col gap-6">
    <div class="flex items-center gap-4">
      <div class="p-3 bg-indigo-500/20 rounded-xl text-indigo-400">
        <Wallet :size="24" />
      </div>
      <div>
        <h3 class="text-slate-400 text-sm font-medium">总资产</h3>
        <p class="text-3xl font-bold text-white tracking-tight">
          {{ formatCurrency(balance) }}
        </p>
      </div>
    </div>

    <div class="grid grid-cols-2 gap-4">
      <div class="p-4 bg-emerald-500/10 rounded-2xl border border-emerald-500/10">
        <div class="flex items-center gap-2 text-emerald-400 mb-1">
          <TrendingUp :size="16" />
          <span class="text-xs font-semibold uppercase tracking-wider">本月收入</span>
        </div>
        <p class="text-lg font-bold text-white">{{ formatCurrency(income) }}</p>
      </div>

      <div class="p-4 bg-rose-500/10 rounded-2xl border border-rose-500/10">
        <div class="flex items-center gap-2 text-rose-400 mb-1">
          <TrendingDown :size="16" />
          <span class="text-xs font-semibold uppercase tracking-wider">本月支出</span>
        </div>
        <p class="text-lg font-bold text-white">{{ formatCurrency(expense) }}</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.glass-card {
  display: flex;
  flex-direction: column;
}
</style>
