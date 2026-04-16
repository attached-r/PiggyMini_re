import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import request from '@/utils/request';

export const useFinanceStore = defineStore('finance', () => {
  // State
  const accounts = ref([]);
  const recentTransactions = ref([]);
  const budgets = ref([]);
  const loading = ref(false);

  // Getters
  const currentBalance = computed(() => {
    return accounts.value.reduce((total, acc) => total + (acc.balance || 0), 0);
  });

  // Actions

  /**
   * Fetch all accounts for current user
   */
  const fetchAccounts = async (page = 1, size = 50) => {
    loading.value = true;
    try {
      const res = await request.get(`/api/accounts?page=${page}&size=${size}`);
      if (res.code === 200) {
        accounts.value = res.data.records || [];
        return res.data;
      }
      throw new Error(res.message);
    } catch (error) {
      console.error('Failed to fetch accounts:', error);
      throw error;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Fetch recent transactions
   */
  const fetchRecentTransactions = async (page = 1, size = 10) => {
    loading.value = true;
    try {
      const res = await request.get(`/api/transactions?page=${page}&size=${size}`);
      if (res.code === 200) {
        recentTransactions.value = res.data.records || [];
        return res.data;
      }
      throw new Error(res.message);
    } catch (error) {
      console.error('Failed to fetch transactions:', error);
      throw error;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Fetch current month budgets
   */
  const fetchCurrentBudgets = async () => {
    loading.value = true;
    try {
      const res = await request.get('/api/budgets/current');
      if (res.code === 200) {
        budgets.value = res.data || [];
        return res.data;
      }
      throw new Error(res.message);
    } catch (error) {
      console.error('Failed to fetch budgets:', error);
      throw error;
    } finally {
      loading.value = false;
    }
  };

  return {
    accounts,
    recentTransactions,
    budgets,
    currentBalance,
    loading,
    fetchAccounts,
    fetchRecentTransactions,
    fetchCurrentBudgets
  };
});
