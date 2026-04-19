import request from '@/utils/request';

/**
 * Get report summary (income, expense, balance)
 * @param {string} period 'month' | 'year'
 * @param {string} date 'yyyy-MM'
 */
export const getSummary = (period = 'month', date) => {
  return request.get('/api/reports/summary', { params: { period, date } });
};

/**
 * Get category-based report
 */
export const getCategoryReport = (period = 'month', date) => {
  return request.get('/api/reports/category', { params: { period, date } });
};

/**
 * Get budget execution rate report
 */
export const getBudgetExecution = (month) => {
  return request.get('/api/reports/budget-execution', { params: { month } });
};

/**
 * Get income/expense trend report
 */
export const getTrend = (startDate, endDate) => {
  return request.get('/api/reports/trend', { params: { startDate, endDate } });
};
