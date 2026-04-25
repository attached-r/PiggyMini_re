// lxy: AI服务API接口定义
// 严格对应后端 AiController.java

import axios from 'axios'

// AI 专用请求实例，使用相对路径经过 Vite 代理
const aiRequest = axios.create({
  baseURL: '',  // 使用相对路径，经过 Vite 代理
  timeout: 60000,
})

// 请求拦截器 - 添加用户认证
aiRequest.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('piggy_token')
    let userId = localStorage.getItem('piggy_user_id')
    
    // 如果 piggy_user_id 不存在，尝试从 userInfo JSON 中获取
    if (!userId) {
      try {
        const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
        userId = userInfo.userId || userInfo.id
      } catch (e) {
        console.error('解析 userInfo 失败:', e)
      }
    }
    
    console.log('[AI Request] token:', token ? token.substring(0, 20) + '...' : 'null')
    console.log('[AI Request] userId:', userId)
    
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    if (userId) {
      config.headers['X-User-Id'] = String(userId)
    }
    
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
aiRequest.interceptors.response.use(
  (response) => response.data,
  (error) => {
    console.error('AI服务请求失败:', error.message)
    return Promise.reject(error)
  }
)

/**
 * 智能记账 - 单条流水分类（同步）
 * POST /internal/ai/classify
 * 后端：AiController.classify()
 * 请求：{ input: "午餐外卖 35 元" }
 * 响应：{ code: 200, data: { amount, category, description, merchant, transactionType, confidence }, message: "..." }
 */
export const classifyTransaction = (input) => {
  return aiRequest.post('/internal/ai/classify', { input })
}

/**
 * 自然语言查询
 * POST /internal/ai/query
 * 后端：AiController.query()
 * 请求：{ query: "我的支出情况如何？" }
 * 响应：{ code: 200, data: "回答字符串", message: "..." }
 */
export const queryAI = (query) => {
  return aiRequest.post('/internal/ai/query', { query })
}

/**
 * 消费分析报告
 * POST /internal/ai/analyze
 * 后端：AiController.analyze()
 * 请求：{ date?: "2024-01", period?: "month" }
 * 响应：{ code: 200, data: "分析报告字符串", message: "..." }
 */
export const analyzeConsumption = (data) => {
  return aiRequest.post('/internal/ai/analyze', data)
}

/**
 * 批量流水解析（异步MQ）
 * POST /internal/ai/batch-classify
 * 后端：AiController.batchClassify()
 * 请求：{ inputs: ["描述1", "描述2"] }
 * 响应：{ code: 200, data: { taskId: "xxx" }, message: "任务已提交" }
 */
export const batchClassify = (inputs) => {
  return aiRequest.post('/internal/ai/batch-classify', { inputs })
}

/**
 * 查询批量任务进度
 * GET /internal/ai/batch-task/{taskId}/progress
 * 后端：AiController.getProgress()
 * 响应：{ code: 200, data: { progress: 50, total: 100 }, message: "..." }
 */
export const getBatchProgress = (taskId) => {
  return aiRequest.get(`/internal/ai/batch-task/${taskId}/progress`)
}

/**
 * 清除对话历史
 * DELETE /internal/ai/chat/history
 * 后端：AiController.clearHistory()
 * 响应：{ code: 200, data: "对话历史已清除", message: "..." }
 */
export const clearChatHistory = () => {
  return aiRequest.delete('/internal/ai/chat/history')
}

// 向后兼容的别名
export const aiClassify = classifyTransaction
export const aiQuery = queryAI
export const aiAnalyze = analyzeConsumption

/**
 * 流式对话（ SSE 服务器推送事件）
 * GET /internal/ai/chat/stream?message=xxx
 * 后端：AiController.streamChat()
 * 返回：EventSource 流式数据
 */
export const streamChat = (message) => {
  // 构建 SSE URL
  const token = localStorage.getItem('piggy_token')
  let userId = localStorage.getItem('piggy_user_id') || ''
  
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    userId = userInfo.userId || userInfo.id || userId
  } catch (e) {}

  const params = new URLSearchParams({
    message: message,
    'X-User-Id': String(userId)
  })

  // 返回 EventSource 实例
  // 注意：SSE 不支持 POST，且需要特殊处理
  // 如果后端支持 fetch 流式响应，可以用 fetch 替代
  return {
    message,
    userId,
    url: `/internal/ai/chat/stream?message=${encodeURIComponent(message)}`
  }
}

/**
 * 使用 Fetch 实现流式对话（替代 EventSource）
 * 返回 ReadableStream 可用于前端打字机效果
 */
export const streamChatFetch = async (message, onChunk, onComplete, onError) => {
  const token = localStorage.getItem('piggy_token')
  let userId = localStorage.getItem('piggy_user_id') || ''

  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    userId = userInfo.userId || userInfo.id || userId
  } catch (e) {}

  try {
    const response = await fetch(`/internal/ai/chat/stream?message=${encodeURIComponent(message)}`, {
      method: 'GET',
      headers: {
        'Authorization': token ? `Bearer ${token}` : '',
        'Accept': 'text/event-stream'
      }
    })

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })

      // 解析 SSE 数据
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        if (line.startsWith('data: ')) {
          const data = line.slice(6).trim()
          if (data && data !== '[DONE]') {
            onChunk?.(data)
          }
        }
      }
    }

    onComplete?.()
    return true
  } catch (error) {
    onError?.(error)
    return false
  }
}
