<script setup>
import { computed } from 'vue';

const props = defineProps({
  label: {
    type: String,
    default: '总预算'
  },
  spent: {
    type: Number,
    required: true
  },
  total: {
    type: Number,
    required: true
  }
});

const percentage = computed(() => {
  if (props.total <= 0) return 0;
  return Math.min(Math.round((props.spent / props.total) * 100), 100);
});

const statusColor = computed(() => {
  if (percentage.value >= 80) return 'bg-rose-500';
  if (percentage.value >= 50) return 'bg-amber-500';
  return 'bg-indigo-500';
});

const statusText = computed(() => {
  if (percentage.value >= 100) return 'text-rose-400';
  if (percentage.value >= 80) return 'text-amber-400';
  return 'text-indigo-400';
});
</script>

<template>
  <div class="space-y-3">
    <div class="flex justify-between items-end">
      <div>
        <span class="text-xs font-semibold text-slate-400 uppercase tracking-widest">{{ label }}</span>
        <div class="flex items-baseline gap-1 mt-1">
          <span class="text-lg font-bold text-white">{{ spent.toLocaleString() }}</span>
          <span class="text-xs text-slate-500">/ {{ total.toLocaleString() }}</span>
        </div>
      </div>
      <span class="text-sm font-bold" :class="statusText">{{ percentage }}%</span>
    </div>

    <div class="h-2.5 w-full bg-slate-800 rounded-full overflow-hidden">
      <div 
        class="h-full rounded-full transition-all duration-1000 ease-out"
        :class="statusColor"
        :style="{ width: `${percentage}%` }"
      >
        <div class="w-full h-full opacity-30 bg-[linear-gradient(90deg,transparent_0%,rgba(255,255,255,0.4)_50%,transparent_100%)] animate-[shimmer_2s_infinite]"></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@keyframes shimmer {
  from { transform: translateX(-100%); }
  to { transform: translateX(100%); }
}
</style>
