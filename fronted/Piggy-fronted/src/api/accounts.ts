// src/api/accounts.ts - 账户管理接口
// POST /api/accounts - 新增账户
// GET /api/accounts - 分页查询账户
// GET /api/accounts/:id - 查询账户详情
// PUT /api/accounts/:id - 更新账户
// DELETE /api/accounts/:id - 删除账户

import apiClient from '@/utils/axios'
import type { ApiResponse, PageResponse } from '@/types/api'

// ============ 枚举类型 ============

export enum AccountType {
  CASH = 'CASH',           // 现金
  BANK = 'BANK',           // 银行账户
  CREDIT = 'CREDIT',        // 信用卡
  ONLINE = 'ONLINE',       // 在线账户
  INVESTMENT = 'INVESTMENT' // 投资账户
}

// ============ 请求类型 ============

export interface CreateAccountRequest {
  accountName: string
  accountType: string
  balance?: number
  currency?: string
  remark?: string
}

export interface UpdateAccountRequest {
  accountName?: string
  balance?: number | string
  currency?: string
  remark?: string
}

// ============ 响应类型 ============

export interface AccountInfo {
  id: number
  userId: number
  accountName: string
  accountType: string
  balance: number
  currency: string
  icon?: string | null
  remark?: string | null
  isDefault: number
  sortOrder: number
  createTime: string
  updateTime: string
}

// ============ API ============

/**
 * 新增账户
 */
export const createAccount = async (data: CreateAccountRequest): Promise<ApiResponse<AccountInfo>> => {
  const res = await apiClient.post('/accounts', data)
  return res.data
}

/**
 * 分页查询账户列表
 */
export const getAccountList = async (params?: { page?: number; size?: number }): Promise<ApiResponse<PageResponse<AccountInfo>>> => {
  const res = await apiClient.get('/accounts', { params })
  return res.data
}

/**
 * 查询账户详情
 */
export const getAccountById = async (id: number): Promise<ApiResponse<AccountInfo>> => {
  const res = await apiClient.get(`/accounts/${id}`)
  return res.data
}

/**
 * 更新账户
 */
export const updateAccount = async (id: number, data: UpdateAccountRequest): Promise<ApiResponse<AccountInfo>> => {
  const res = await apiClient.put(`/accounts/${id}`, data)
  return res.data
}

/**
 * 删除账户
 */
export const deleteAccount = async (id: number): Promise<ApiResponse<string>> => {
  const res = await apiClient.delete(`/accounts/${id}`)
  return res.data
}
