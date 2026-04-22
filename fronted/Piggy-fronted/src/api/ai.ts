// src/api/ai.ts - AI 智能服务接口
// 严格对应后端 AiController

import apiClient from '@/utils/axios'
import type { ApiResponse } from '@/types/api'

// ============ 请求类型 ============

// 智能分类请求
export interface ClassifyRequest {
  input: string           // 输入描述，如 "午餐外卖 35 元"
}

// 自然语言查询请求
export interface QueryRequest {
  query: string            // 查询内容
}

// 消费分析请求
export interface AnalyzeRequest {
  date?: string           // 日期，如 "2024-01"
  period?: string         // 周期，如 "month", "year"
}

// 批量分类请求
export interface BatchClassifyRequest {
  inputs: string[]        // 输入描述列表
}

// ============ 响应类型 ============

// 智能分类响应 - 对应后端 ClassifyResponse
export interface ClassifyResponse {
  amount: number          // 金额
  category: string        // 分类，如 "FOOD_LUNCH"
  description: string     // 描述
  merchant: string        // 商户
  transactionType: string // "EXPENSE" | "INCOME"
  confidence: number      // 置信度 0-1
}

// ============ API ============

/**
 * 智能记账 - 单条流水分类（同步）
 * POST /api/ai/classify
 * 后端：AiController.classify()
 */
export const classifyTransaction = async (input: string): Promise<ApiResponse<ClassifyResponse>> => {
  const res = await apiClient.post('/ai/classify', { input })
  return res.data
}

/**
 * 自然语言查询
 * POST /api/ai/query
 * 后端：AiController.query()
 */
export const queryAI = async (query: string): Promise<ApiResponse<string>> => {
  const res = await apiClient.post('/ai/query', { query })
  return res.data
}

/**
 * 消费分析报告
 * POST /api/ai/analyze
 * 后端：AiController.analyze()
 */
export const analyzeConsumption = async (data: AnalyzeRequest): Promise<ApiResponse<string>> => {
  const res = await apiClient.post('/ai/analyze', data)
  return res.data
}

/**
 * 批量流水解析（异步MQ）
 * POST /api/ai/batch-classify
 * 后端：AiController.batchClassify()
 */
export const batchClassify = async (inputs: string[]): Promise<ApiResponse<{ taskId: string }>> => {
  const res = await apiClient.post('/ai/batch-classify', { inputs })
  return res.data
}

/**
 * 查询批量任务进度
 * GET /api/ai/batch-task/{taskId}/progress
 * 后端：AiController.getProgress()
 */
export const getBatchProgress = async (taskId: string): Promise<ApiResponse<{ progress: number; total: number }>> => {
  const res = await apiClient.get(`/ai/batch-task/${taskId}/progress`)
  return res.data
}

/**
 * 清除对话历史
 * DELETE /api/ai/chat/history
 * 后端：AiController.clearHistory()
 */
export const clearChatHistory = async (): Promise<ApiResponse<string>> => {
  const res = await apiClient.delete('/ai/chat/history')
  return res.data
}
