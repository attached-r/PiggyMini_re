// src/api/ai.ts - AI 智能分类接口
// POST /internal/ai/classify - 智能分类（同步）

import apiClient from '@/utils/axios'
import type { ApiResponse } from '@/types/api'

// ============ 请求类型 ============

export interface AIClassifyRequest {
  input: string           // 输入描述，如 "午餐外卖 35 元"
  userId?: string | null
}

// ============ 响应类型 ============

export interface AIClassifyResult {
  amount: number          // 金额
  category: string        // 分类名称，如 "午餐"
  confidence: number      // 置信度 0-1
  description: string     // 描述
  merchant: string         // 商户
  transactionType: string // "EXPENSE" | "INCOME"
}

// ============ API ============

/**
 * 智能记账 - 单条流水分类（同步）
 * 根据用户输入的描述，AI 自动识别金额、分类、交易类型等
 */
export const classifyTransaction = async (data: AIClassifyRequest): Promise<ApiResponse<AIClassifyResult>> => {
  const res = await apiClient.post('/internal/ai/classify', data)
  return res.data
}

/**
 * 批量智能分类（预留）
 */
export const batchClassifyTransactions = async (inputs: string[]): Promise<ApiResponse<AIClassifyResult[]>> => {
  const res = await apiClient.post('/internal/ai/classify/batch', { inputs })
  return res.data
}

/**
 * 智能建议 - 根据历史数据推荐分类（预留）
 */
export const suggestCategory = async (description: string): Promise<ApiResponse<string>> => {
  const res = await apiClient.get('/internal/ai/suggest', { params: { description } })
  return res.data
}
