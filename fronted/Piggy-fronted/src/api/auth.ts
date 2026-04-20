// src/api/auth.ts - 认证相关接口
// POST /api/auth/register - 用户注册
// POST /api/auth/login - 用户登录

import apiClient from '@/utils/axios'
import type { ApiResponse } from '@/types/api'

// ============ 请求类型 ============

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  nickname: string
}

// ============ 响应类型 ============

export interface UserInfo {
  userId: number
  username: string
  nickname: string
  token: string
  refreshToken: string
}

// ============ API ============

/**
 * 用户注册
 */
export const register = async (data: RegisterRequest): Promise<ApiResponse<UserInfo>> => {
  const res = await apiClient.post('/auth/register', data)
  return res.data
}

/**
 * 用户登录
 */
export const login = async (data: LoginRequest): Promise<ApiResponse<UserInfo>> => {
  const res = await apiClient.post('/auth/login', data)
  return res.data
}
