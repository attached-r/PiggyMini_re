import request from '@/utils/request'

/**
 * 报表 API
 * 严格根据后端 ReportController 接口
 */

/**
 * 获取报表摘要
 * GET /api/reports/summary?period=month&date=2026-04&accountId=1
 * 请求头: X-User-Id
 * 参数:
 *   - period: month | year
 *   - date: yyyy-MM 格式
 *   - accountId: 账户ID（可选，不传则查询所有账户汇总）
 * 响应: { income, expense, balance, trend }
 */
export const getSummary = (period = 'month', date, accountId) => {
  const params = { period, date }
  if (accountId) params.accountId = accountId
  return request.get('/api/reports/summary', { params })
}

/**
 * 获取分类报表
 * GET /api/reports/category?period=month&date=2026-04&accountId=1
 * 请求头: X-User-Id
 * 参数:
 *   - period: month | year
 *   - date: yyyy-MM 格式
 *   - accountId: 账户ID（可选，不传则查询所有账户汇总）
 * 响应: [{ category, amount, percent }, ...]
 */
export const getCategoryReport = (period = 'month', date, accountId) => {
  const params = { period, date }
  if (accountId) params.accountId = accountId
  return request.get('/api/reports/category', { params })
}

/**
 * 获取预算执行率报表
 * GET /api/reports/budget-execution?month=2026-04
 * 请求头: X-User-Id
 * 参数:
 *   - month: yyyy-MM 格式
 * 响应: [{ category, budgetAmount, usedAmount, remainingAmount, executionRate, isOverBudget }, ...]
 */
export const getBudgetExecution = (month) => {
  return request.get('/api/reports/budget-execution', { params: { month } })
}

/**
 * 获取收支趋势报表
 * GET /api/reports/trend?startDate=2026-04-01&endDate=2026-04-30
 * 请求头: X-User-Id
 * 参数:
 *   - startDate: yyyy-MM-dd 格式
 *   - endDate: yyyy-MM-dd 格式
 * 响应: { dates: [], income: [], expense: [] }
 */
export const getTrend = (startDate, endDate) => {
  return request.get('/api/reports/trend', { params: { startDate, endDate } })
}
