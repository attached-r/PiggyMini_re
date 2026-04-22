import request from '@/utils/request'

/**
 * 预算 API
 * 严格根据后端 BudgetController 接口
 */

/**
 * 预算周期类型
 * cycleType: 1-周，2-月，3-年
 */
export const CycleType = {
  WEEK: 1,
  MONTH: 2,
  YEAR: 3
}

/**
 * 创建/更新月度预算
 * POST /api/budgets
 * 请求头: X-User-Id
 * 请求体: { month, category?, amount }
 * 注意: category 为空表示总预算
 */
export const createOrUpdateBudget = (data) => {
  const payload = {
    month: data.month,
    category: data.category || null,
    amount: data.amount
  }
  return request.post('/api/budgets', payload)
}

/**
 * 查询当月所有预算
 * GET /api/budgets/current
 * 请求头: X-User-Id
 * 响应: [{ category, budget, spent, remain }, ...]
 */
export const getCurrentBudgets = () => {
  return request.get('/api/budgets/current')
}

/**
 * 超支预警列表
 * GET /api/budgets/warn
 * 请求头: X-User-Id
 * 响应: [{ category, budgetAmount, usedAmount, remainingAmount, executionRate, isOverBudget }, ...]
 */
export const getBudgetWarnings = () => {
  return request.get('/api/budgets/warn')
}
