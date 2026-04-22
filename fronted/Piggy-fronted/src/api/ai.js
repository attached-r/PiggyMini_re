// lxy: AI服务API接口定义
// 严格对应后端 AiController.java

import request from '@/utils/request'

/**
 * 智能记账 - 单条流水分类（同步）
 * POST /internal/ai/classify
 * 后端：AiController.classify()
 * 请求：{ input: "午餐外卖 35 元" }
 * 响应：{ code: 200, data: { amount, category, description, merchant, transactionType, confidence }, message: "..." }
 */
export const classifyTransaction = (input) => {
  return request({
    url: '/internal/ai/classify',
    method: 'post',
    data: { input }
  })
}

/**
 * 自然语言查询
 * POST /internal/ai/query
 * 后端：AiController.query()
 * 请求：{ query: "我的支出情况如何？" }
 * 响应：{ code: 200, data: "回答字符串", message: "..." }
 */
export const queryAI = (query) => {
  return request({
    url: '/internal/ai/query',
    method: 'post',
    data: { query },
    timeout: 60000  // AI 回答可能需要较长时间
  })
}

/**
 * 消费分析报告
 * POST /internal/ai/analyze
 * 后端：AiController.analyze()
 * 请求：{ date?: "2024-01", period?: "month" }
 * 响应：{ code: 200, data: "分析报告字符串", message: "..." }
 */
export const analyzeConsumption = (data) => {
  return request({
    url: '/internal/ai/analyze',
    method: 'post',
    data: data,
    timeout: 60000  // AI 分析可能需要较长时间，设置为 60 秒
  })
}

/**
 * 批量流水解析（异步MQ）
 * POST /internal/ai/batch-classify
 * 后端：AiController.batchClassify()
 * 请求：{ inputs: ["描述1", "描述2"] }
 * 响应：{ code: 200, data: { taskId: "xxx" }, message: "任务已提交" }
 */
export const batchClassify = (inputs) => {
  return request({
    url: '/internal/ai/batch-classify',
    method: 'post',
    data: { inputs }
  })
}

/**
 * 查询批量任务进度
 * GET /internal/ai/batch-task/{taskId}/progress
 * 后端：AiController.getProgress()
 * 响应：{ code: 200, data: { progress: 50, total: 100 }, message: "..." }
 */
export const getBatchProgress = (taskId) => {
  return request({
    url: `/internal/ai/batch-task/${taskId}/progress`,
    method: 'get'
  })
}

/**
 * 清除对话历史
 * DELETE /internal/ai/chat/history
 * 后端：AiController.clearHistory()
 * 响应：{ code: 200, data: "对话历史已清除", message: "..." }
 */
export const clearChatHistory = () => {
  return request({
    url: '/internal/ai/chat/history',
    method: 'delete'
  })
}

// 向后兼容的别名
export const aiClassify = classifyTransaction
export const aiQuery = queryAI
export const aiAnalyze = analyzeConsumption
