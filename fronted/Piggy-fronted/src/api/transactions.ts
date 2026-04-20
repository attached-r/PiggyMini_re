// src/api/transactions.ts - 交易记录接口
// POST /api/transactions - 新增交易记录
// GET /api/transactions - 分页查询交易记录
// GET /api/transactions/:id - 查询单条交易记录
// DELETE /api/transactions/:id - 删除交易记录

import apiClient from '@/utils/axios'
import type { ApiResponse, PageResponse } from '@/types/api'

// ============ 枚举类型 ============

export enum TransactionType {
  EXPENSE = 'EXPENSE',   // 支出
  INCOME = 'INCOME',      // 收入
  TRANSFER = 'TRANSFER'   // 转账
}

// ============ 请求类型 ============

export interface CreateTransactionRequest {
  accountId: number
  type: string           // "EXPENSE" | "INCOME" | "TRANSFER"
  amount: number
  category: string       // "FOOD_LUNCH"
  description?: string
  tradeTime?: string     // "2026-04-09T12:00:00"
  remark?: string
  tags?: string
  targetAccountId?: number | null
}

// ============ 响应类型 ============

export interface TransactionInfo {
  id: number
  userId: number
  accountId: number
  transactionType: string
  amount: number
  category: string
  description?: string
  transactionTime: string
  remark?: string
  tags?: string
  targetAccountId?: number | null
  createTime: string
  updateTime: string
}

// ============ API ============

/**
 * 新增交易记录
 */
export const createTransaction = async (data: CreateTransactionRequest): Promise<ApiResponse<TransactionInfo>> => {
  const res = await apiClient.post('/transactions', data)
  return res.data
}

/**
 * 分页查询交易记录
 */
export const getTransactionList = async (params?: {
  accountId?: number
  type?: string
  page?: number
  size?: number
}): Promise<ApiResponse<PageResponse<TransactionInfo>>> => {
  const res = await apiClient.get('/transactions', { params })
  return res.data
}

/**
 * 查询单条交易记录
 */
export const getTransactionById = async (id: number): Promise<ApiResponse<TransactionInfo>> => {
  const res = await apiClient.get(`/transactions/${id}`)
  return res.data
}

/**
 * 删除交易记录
 */
export const deleteTransaction = async (id: number): Promise<ApiResponse<string>> => {
  const res = await apiClient.delete(`/transactions/${id}`)
  return res.data
}
