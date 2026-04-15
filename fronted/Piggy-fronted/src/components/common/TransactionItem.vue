<script setup>
import { computed } from 'vue';
import { 
  Utensils, Bus, ShoppingBag, Home, 
  Gamepad2, Hospital, GraduationCap, Users, 
  Package, ArrowUpRight, ArrowDownLeft, Repeat 
} from 'lucide-vue-next';

const props = defineProps({
  transaction: {
    type: Object,
    required: true
  }
});

const iconMap = {
  'FOOD': Utensils,
  'TRANSPORT': Bus,
  'SHOP': ShoppingBag,
  'LIVING': Home,
  'ENTERTAIN': Gamepad2,
  'MEDICAL': Hospital,
  'EDUCATION': GraduationCap,
  'SOCIAL': Users,
  'OTHER': Package
};

const getIcon = computed(() => {
  const category = props.transaction.category || '';
  for (const key in iconMap) {
    if (category.startsWith(key)) return iconMap[key];
  }
  return Package;
});

const isExpense = computed(() => props.transaction.transactionType === 'EXPENSE');
const isIncome = computed(() => props.transaction.transactionType === 'INCOME');

const formatAmount = (val) => {
  return new Intl.NumberFormat('zh-CN', {
    minimumFractionDigits: 2
  }).format(val);
};

const formatDate = (val) => {
  if (!val) return '';
  const date = new Date(val);
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' });
};
</script>

<template>
  <div class="flex items-center justify-between p-4 hover:bg-white/5 rounded-2xl transition-colors cursor-pointer group">
    <div class="flex items-center gap-4">
      <div 
        class="w-12 h-12 rounded-xl flex items-center justify-center transition-transform group-hover:scale-110"
        :class="[
          isExpense ? 'bg-rose-500/10 text-rose-400' : 
          isIncome ? 'bg-emerald-500/10 text-emerald-400' : 
          'bg-indigo-500/10 text-indigo-400'
        ]"
      >
        <component :is="getIcon" :size="22" />
      </div>
      
      <div>
        <h4 class="text-white font-medium text-sm">{{ transaction.remark || transaction.category || '未分类交易' }}</h4>
        <p class="text-slate-400 text-xs mt-1">{{ formatDate(transaction.transactionTime) }}</p>
      </div>
    </div>

    <div class="text-right">
      <p 
        class="text-sm font-bold"
        :class="[
          isExpense ? 'text-white' : 
          isIncome ? 'text-emerald-400' : 
          'text-indigo-400'
        ]"
      >
        {{ isExpense ? '-' : (isIncome ? '+' : '') }}{{ formatAmount(transaction.amount) }}
      </p>
      <p class="text-[10px] text-slate-500 uppercase tracking-tighter mt-1">{{ transaction.transactionType }}</p>
    </div>
  </div>
</template>
