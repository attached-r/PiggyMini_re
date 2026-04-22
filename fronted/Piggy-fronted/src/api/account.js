import request from '@/utils/request'

/**
 * 账户 API
 * 严格根据后端 AccountController 接口
 */

/**
 * 账户类型枚举
 * 对应后端 AccountType
 */
export const AccountType = {
  CASH: 'CASH',           // 现金
  DEBIT_CARD: 'DEBIT_CARD', // 储蓄卡
  CREDIT_CARD: 'CREDIT_CARD', // 信用卡
  ALIPAY: 'ALIPAY',       // 支付宝
  WECHAT: 'WECHAT',       // 微信
  BANK_TRANSFER: 'BANK_TRANSFER', // 银行转账
  INVESTMENT: 'INVESTMENT', // 投资账户
  VIRTUAL: 'VIRTUAL',     // 虚拟账户
  OTHER: 'OTHER'          // 其他
}

/**
 * 账户类型映射（用于显示）
 */
export const AccountTypeLabels = {
  CASH: { label: '现金', icon: '💵' },
  DEBIT_CARD: { label: '储蓄卡', icon: '💳' },
  CREDIT_CARD: { label: '信用卡', icon: '💎' },
  ALIPAY: { label: '支付宝', icon: '🔵' },
  WECHAT: { label: '微信', icon: '💚' },
  BANK_TRANSFER: { label: '银行转账', icon: '🏦' },
  INVESTMENT: { label: '投资账户', icon: '📈' },
  VIRTUAL: { label: '虚拟账户', icon: '🎮' },
  OTHER: { label: '其他', icon: '📦' }
}

/**
 * 创建账户
 * POST /api/accounts
 * 请求头: X-User-Id
 * 请求体: { accountName, accountType, balance?, currency?, icon?, remark?, sortOrder?, isDefault? }
 */
export const createAccount = (data) => {
  return request.post('/api/accounts', data)
}

/**
 * 查询账户列表（分页）
 * GET /api/accounts?page=1&size=10
 * 请求头: X-User-Id
 */
export const getAccounts = (page = 1, size = 100) => {
  return request.get('/api/accounts', { params: { page, size } })
}

/**
 * 查询账户详情
 * GET /api/accounts/{id}
 * 请求头: X-User-Id
 */
export const getAccountById = (id) => {
  return request.get(`/api/accounts/${id}`)
}

/**
 * 更新账户
 * PUT /api/accounts/{id}
 * 请求头: X-User-Id
 * 请求体: { accountName?, accountType?, balance?, currency?, icon?, remark?, sortOrder?, isDefault? }
 */
export const updateAccount = (id, data) => {
  return request.put(`/api/accounts/${id}`, data)
}

/**
 * 删除账户
 * DELETE /api/accounts/{id}
 * 请求头: X-User-Id
 */
export const deleteAccount = (id) => {
  return request.delete(`/api/accounts/${id}`)
}
