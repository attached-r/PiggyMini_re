<script setup>
import { ref, onMounted, computed } from 'vue';
import { 
  LayoutDashboard, CreditCard, ArrowLeftRight, 
  Target, BarChart3, Bell, User, LogOut, Search, Plus, Menu, X
} from 'lucide-vue-next';
import { useFinanceStore } from '@/stores/finance';
import { useUserStore } from '@/stores/user';
import BalanceCard from '@/components/common/BalanceCard.vue';
import TransactionItem from '@/components/common/TransactionItem.vue';
import BudgetProgressBar from '@/components/common/BudgetProgressBar.vue';

// Chart.js imports
import { Pie } from 'vue-chartjs';
import { 
  Chart as ChartJS, Title, Tooltip, Legend, ArcElement, CategoryScale 
} from 'chart.js';

ChartJS.register(Title, Tooltip, Legend, ArcElement, CategoryScale);

const financeStore = useFinanceStore();
const userStore = useUserStore();

const isSidebarOpen = ref(true);
const toggleSidebar = () => isSidebarOpen.value = !isSidebarOpen.value;

onMounted(async () => {
  await Promise.all([
    financeStore.fetchAccounts(),
    financeStore.fetchRecentTransactions(),
    financeStore.fetchCurrentBudgets()
  ]);
});

// Mock data for breadcrumbs/income-expense (since API might need separate report calls)
// In real use, these would come from report.js API
const monthlyIncome = ref(8500.00);
const monthlyExpense = ref(3240.50);

// Pie Chart Data
const chartData = computed(() => ({
  labels: ['餐饮', '交通', '购物', '居住', '娱乐'],
  datasets: [{
    backgroundColor: ['#6366f1', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6'],
    data: [1200, 500, 800, 450, 290],
    borderWidth: 0
  }]
}));

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'bottom',
      labels: { color: '#94a6b8', usePointStyle: true, pointStyle: 'circle', padding: 20 }
    }
  }
};

const navItems = [
  { name: 'Dashboard', icon: LayoutDashboard, active: true },
  { name: 'Accounts', icon: CreditCard },
  { name: 'Transactions', icon: ArrowLeftRight },
  { name: 'Budgets', icon: Target },
  { name: 'Reports', icon: BarChart3 },
];
</script>

<template>
  <div class="flex min-h-screen bg-slate-950 text-slate-100">
    <!-- Sidebar -->
    <aside 
      class="fixed inset-y-0 left-0 z-50 w-64 bg-slate-900/50 backdrop-blur-xl border-r border-white/5 transition-transform duration-300 transform"
      :class="[isSidebarOpen ? 'translate-x-0' : '-translate-x-full', 'lg:translate-x-0 lg:static lg:inset-0']"
    >
      <div class="flex flex-col h-full p-6">
        <div class="flex items-center gap-3 mb-10 px-2">
          <div class="w-10 h-10 bg-indigo-500 rounded-xl flex items-center justify-center shadow-lg shadow-indigo-500/20">
            <span class="font-black text-xl italic">P</span>
          </div>
          <h1 class="text-xl font-bold tracking-tight">Piggy <span class="text-indigo-400">Mini</span></h1>
        </div>

        <nav class="flex-1 space-y-2">
          <a 
            v-for="item in navItems" 
            :key="item.name"
            href="#" 
            class="flex items-center gap-3 px-4 py-3 rounded-xl transition-all duration-200 group"
            :class="[item.active ? 'bg-indigo-500 text-white shadow-lg shadow-indigo-500/20' : 'text-slate-400 hover:text-white hover:bg-white/5']"
          >
            <component :is="item.icon" :size="20" />
            <span class="font-medium">{{ item.name }}</span>
          </a>
        </nav>

        <div class="pt-6 border-t border-white/5 space-y-2">
          <button class="w-full flex items-center gap-3 px-4 py-3 text-slate-400 hover:text-rose-400 hover:bg-rose-500/5 rounded-xl transition-all group" @click="userStore.logout()">
            <LogOut :size="20" />
            <span class="font-medium">Logout</span>
          </button>
        </div>
      </div>
    </aside>

    <!-- Main Content -->
    <main class="flex-1 flex flex-col min-w-0 overflow-hidden">
      <!-- Top Header -->
      <header class="h-20 flex items-center justify-between px-6 lg:px-10 border-b border-white/5 bg-slate-950/50 backdrop-blur-md sticky top-0 z-40">
        <div class="flex items-center gap-4">
          <button class="lg:hidden p-2 text-slate-400 hover:text-white" @click="toggleSidebar">
            <Menu :size="24" />
          </button>
          <div>
            <h2 class="text-lg font-bold">Dashboard</h2>
            <p class="text-xs text-slate-500">Welcome back, {{ userStore.nickname || userStore.username }}!</p>
          </div>
        </div>

        <div class="flex items-center gap-4">
          <div class="hidden md:flex items-center bg-white/5 border border-white/5 rounded-full px-4 py-1.5 focus-within:border-indigo-500/50 transition-all">
            <Search :size="16" class="text-slate-500" />
            <input type="text" placeholder="Search..." class="bg-transparent border-none focus:ring-0 text-sm ml-2 w-40 text-slate-200" />
          </div>
          <button class="p-2.5 bg-white/5 hover:bg-indigo-500/10 hover:text-indigo-400 rounded-full transition-all relative">
            <Bell :size="20" />
            <span class="absolute top-2 right-2 w-2 h-2 bg-indigo-500 rounded-full border-2 border-slate-950"></span>
          </button>
          <div class="flex items-center gap-3 pl-4 border-l border-white/5">
            <div class="w-10 h-10 rounded-full bg-gradient-to-tr from-indigo-500 to-purple-500 p-0.5">
              <div class="w-full h-full rounded-full bg-slate-900 flex items-center justify-center overflow-hidden">
                <User :size="20" class="text-indigo-400" />
              </div>
            </div>
          </div>
        </div>
      </header>

      <!-- Dashboard Grid -->
      <div class="flex-1 overflow-y-auto p-6 lg:p-10 space-y-8 max-w-[1600px] mx-auto w-full">
        <!-- Top row: Overview & Chart -->
        <div class="grid grid-cols-1 lg:grid-cols-12 gap-8">
          <div class="lg:col-span-8 space-y-8">
            <BalanceCard 
              :balance="financeStore.currentBalance" 
              :income="monthlyIncome" 
              :expense="monthlyExpense" 
            />
            
            <div class="glass-card p-6 h-[400px]">
              <div class="flex items-center justify-between mb-6">
                <h3 class="text-lg font-bold">Categories</h3>
                <button class="text-xs text-indigo-400 hover:underline">View All</button>
              </div>
              <div class="h-full pb-10">
                <Pie :data="chartData" :options="chartOptions" />
              </div>
            </div>
          </div>

          <!-- Right: Budget & Quick Actions -->
          <div class="lg:col-span-4 space-y-8">
            <div class="glass-card p-6 space-y-6">
              <div class="flex items-center justify-between">
                <h3 class="text-lg font-bold">Monthly Budgets</h3>
                <button class="p-2 bg-indigo-500/10 text-indigo-400 rounded-lg hover:bg-indigo-500 hover:text-white transition-all">
                  <Plus :size="18" />
                </button>
              </div>
              <div class="space-y-6">
                <BudgetProgressBar label="Total Budget" :spent="monthlyExpense" :total="5000" />
                <div v-for="b in financeStore.budgets.slice(0, 2)" :key="b.category">
                  <BudgetProgressBar :label="b.category || 'Category'" :spent="b.spent" :total="b.budget" />
                </div>
              </div>
            </div>

            <div class="glass-card p-6 bg-gradient-to-br from-indigo-500/20 to-purple-500/20 shadow-indigo-500/5">
               <h3 class="text-md font-bold mb-2">Smart AI Assistant</h3>
               <p class="text-xs text-slate-400 mb-4 lh-relaxed">Need help categorizing a transaction? Try typing a description like "Coffee with friend at Starbucks".</p>
               <button class="btn-primary w-full py-2 text-sm">Analyze Text</button>
            </div>
          </div>
        </div>

        <!-- Bottom row: Recent Transactions -->
        <div class="glass-card p-6">
          <div class="flex items-center justify-between mb-6">
            <h3 class="text-lg font-bold">Recent Transactions</h3>
            <div class="flex gap-2">
              <button class="px-3 py-1 bg-white/5 rounded-lg text-xs text-slate-400 hover:text-white transition-all">Today</button>
              <button class="px-3 py-1 bg-white/5 rounded-lg text-xs text-slate-400 hover:text-white transition-all">This Week</button>
            </div>
          </div>
          <div class="divide-y divide-white/5">
            <TransactionItem 
              v-for="t in financeStore.recentTransactions" 
              :key="t.id" 
              :transaction="t" 
            />
            <div v-if="financeStore.recentTransactions.length === 0" class="py-10 text-center text-slate-500 text-sm italic">
              No transactions found.
            </div>
          </div>
          <div class="mt-6 pt-6 border-t border-white/5 text-center">
            <button class="text-indigo-400 text-sm font-semibold hover:underline">View All Activity</button>
          </div>
        </div>
      </div>

      <!-- Mobile Bottom Tab Bar -->
      <nav class="lg:hidden h-16 bg-slate-900/80 backdrop-blur-xl border-t border-white/5 flex items-center justify-around px-2 pb-safe">
        <a 
          v-for="item in navItems" 
          :key="item.name"
          href="#" 
          class="flex flex-col items-center gap-1 transition-all"
          :class="[item.active ? 'text-indigo-400' : 'text-slate-500']"
        >
          <component :is="item.icon" :size="20" />
          <span class="text-[10px] font-medium">{{ item.name }}</span>
        </a>
      </nav>
    </main>
  </div>
</template>

<style scoped>
/* Scoped overrides if needed, but mostly using main.css */
::-webkit-scrollbar {
  width: 4px;
}
</style>
