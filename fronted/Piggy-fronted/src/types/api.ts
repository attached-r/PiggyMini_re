// src/types/api.ts - 通用 API 类型定义

/**
 * 统一响应结构
 * 后端返回格式: { code, data, message, success, timestamp }
 */
export interface ApiResponse<T = any> {
  code: number
  data: T
  message: string
  success: boolean
  timestamp?: number
}

/**
 * 分页响应
 */
export interface PageResponse<T> {
  current: number
  pages: number
  records: T[]
  size: number
  total: number
}
