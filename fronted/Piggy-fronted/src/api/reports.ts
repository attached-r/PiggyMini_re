// src/api/reports.ts - 报表接口
// GET /api/reports/summary - 获取总报表
// GET /api/reports/category - 获取分类报表
// GET /api/reports/budget-execution - 获取预算执行率报表
// GET /api/reports/trend - 获取收支趋势报表

import apiClient from '@/utils/axios'
import type { ApiResponse } from '@/types/api'

// ============ 响应类型 ============

/** 总报表数据 */
export interface SummaryReport {
  income: number
  expense: number
  balance: number
  trend: string  // "positive" | "negative" | "neutral"
}

/** 分类报表项 */
export interface CategoryReportItem {
  category: string
  amount: number
  percentage: number
  count?: number
}

/** 预算执行率报表项 */
export interface BudgetExecutionItem {
  category: string
  budgetAmount: number
  usedAmount: number
  remainingAmount: number
  executionRate: number
  isOverBudget: boolean
}

/** 趋势数据点 */
export interface TrendDataPoint {
  date: string
  income: number
  expense: number
  balance: number
}

/** 收支趋势报表 */
export interface TrendReport {
  data: TrendDataPoint[]
  totalIncome: number
  totalExpense: number
  totalBalance: number
}

// ============ API ============

/**
 * 获取总报表
 * @param params period: "day"|"week"|"month"|"year", date: "2026-04-09"
 */
export const getSummaryReport = async (params?: { period?: string; date?: string }): Promise<ApiResponse<SummaryReport>> => {
  const res = await apiClient.get('/reports/summary', { params })
  return res.data
}

/**
 * 获取分类报表
 */
export const getCategoryReport = async (params?: { period?: string; date?: string }): Promise<ApiResponse<CategoryReportItem[]>> => {
  const res = await apiClient.get('/reports/category', { params })
  return res.data
}

/**
 * 获取预算执行率报表
 * @param month 月份，格式 "2026-04"
 */
export const getBudgetExecutionReport = async (month?: string): Promise<ApiResponse<BudgetExecutionItem[]>> => {
  const res = await apiClient.get('/reports/budget-execution', { params: { month } })
  return res.data
}

/**
 * 获取收支趋势报表
 */
export const getTrendReport = async (params?: { startDate?: string; endDate?: string }): Promise<ApiResponse<TrendReport>> => {
  const res = await apiClient.get('/reports/trend', { params })
  return res.data
}
