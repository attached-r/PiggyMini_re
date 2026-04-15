import request from '@/utils/request';

/**
 * Create or update a budget
 * @param {Object} data { month, category, amount }
 */
export const createOrUpdateBudget = (data) => {
  return request.post('/api/budgets', data);
};

/**
 * Get current month budgets
 */
export const getCurrentBudgets = () => {
  return request.get('/api/budgets/current');
};

/**
 * Get over-budget warnings
 */
export const getBudgetWarnings = () => {
  return request.get('/api/budgets/warn');
};
