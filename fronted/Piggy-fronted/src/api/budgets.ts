// src/api/budgets.ts - 预算管理接口
// POST /api/budgets - 创建/更新月度预算
// GET /api/budgets/current - 查询当月所有预算
// GET /api/budgets/warn - 超支预警列表

import apiClient from '@/utils/axios'
import type { ApiResponse } from '@/types/api'

// ============ 请求类型 ============

export interface CreateBudgetRequest {
  month: string    // "2026-04"
  category: string  // "FOOD_LUNCH"
  amount: number
}

// ============ 响应类型 ============

/** 当月预算返回项（/api/budgets/current） */
export interface CurrentBudgetItem {
  category: string
  budget: number
  spent: number
  remain: number
}

/** 详细预算项 */
export interface BudgetDetail {
  id: number
  userId: number
  budgetAmount: number
  usedAmount: number
  cycleType: number
  category: string
  startTime: string
  endTime: string
  status: number
  warningThreshold: number
}

/** 超支预警项 */
export interface BudgetWarningItem {
  category: string
  budgetAmount: number
  usedAmount: number
  remainingAmount: number
  executionRate: number
  isOverBudget: boolean
}

// ============ API ============

/**
 * 创建/更新月度预算
 */
export const createOrUpdateBudget = async (data: CreateBudgetRequest): Promise<ApiResponse<BudgetDetail>> => {
  const res = await apiClient.post('/budgets', data)
  return res.data
}

/**
 * 查询当月所有预算
 */
export const getCurrentBudgets = async (): Promise<ApiResponse<CurrentBudgetItem[]>> => {
  const res = await apiClient.get('/budgets/current')
  return res.data
}

/**
 * 超支预警列表
 */
export const getBudgetWarnings = async (): Promise<ApiResponse<BudgetWarningItem[]>> => {
  const res = await apiClient.get('/budgets/warn')
  return res.data
}
