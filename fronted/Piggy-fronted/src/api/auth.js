import request from '@/utils/request'

/**
 * 认证 API
 * 严格根据后端 AuthController 接口
 */

export const authApi = {
  /**
   * 用户登录
   * POST /api/auth/login
   * 请求体: { username, password }
   * 响应: { code: 200, data: { userId, username, nickname, token, refreshToken }, message }
   */
  async login(credentials) {
    const response = await request.post('/api/auth/login', credentials)
    
    // 响应格式: { code, data: { userId, username, nickname, token, refreshToken }, message }
    if (response.code === 200 && response.data) {
      const { userId, username, nickname, token, refreshToken } = response.data
      
      // 保存到 localStorage
      localStorage.setItem('piggy_token', token)
      localStorage.setItem('piggy_refresh_token', refreshToken)
      localStorage.setItem('piggy_user_id', userId)
      localStorage.setItem('piggy_username', username)
      if (nickname) {
        localStorage.setItem('piggy_nickname', nickname)
      }
      
      return {
        success: true,
        token,
        userId,
        username,
        nickname,
        isAdmin: false
      }
    }
    
    throw new Error(response.message || '登录失败')
  },

  /**
   * 用户注册
   * POST /api/auth/register
   * 请求体: { username, password, nickname? }
   */
  async register(userData) {
    const response = await request.post('/api/auth/register', userData)
    
    if (response.code === 200 && response.data) {
      const { userId, username, token, refreshToken } = response.data
      
      localStorage.setItem('piggy_token', token)
      localStorage.setItem('piggy_refresh_token', refreshToken)
      localStorage.setItem('piggy_user_id', userId)
      localStorage.setItem('piggy_username', username)
      
      return {
        success: true,
        userId,
        username,
        token
      }
    }
    
    throw new Error(response.message || '注册失败')
  },

  /**
   * 刷新 Token
   * POST /api/auth/refresh
   * 请求体: { refreshToken }
   */
  async refreshToken() {
    const refreshToken = localStorage.getItem('piggy_refresh_token')
    if (!refreshToken) {
      throw new Error('没有刷新令牌')
    }
    
    const response = await request.post('/api/auth/refresh', { refreshToken })
    
    if (response.code === 200 && response.data) {
      const { token, refreshToken: newRefreshToken, userId } = response.data
      
      localStorage.setItem('piggy_token', token)
      localStorage.setItem('piggy_refresh_token', newRefreshToken)
      localStorage.setItem('piggy_user_id', userId)
      
      return { token, userId }
    }
    
    throw new Error(response.message || '刷新令牌失败')
  },

  /**
   * 登出
   */
  logout() {
    localStorage.removeItem('piggy_token')
    localStorage.removeItem('piggy_refresh_token')
    localStorage.removeItem('piggy_user_id')
    localStorage.removeItem('piggy_username')
    localStorage.removeItem('piggy_nickname')
    localStorage.removeItem('userRole')
    localStorage.removeItem('selectedAccountId')
  },

  /**
   * 检查是否已登录
   */
  isAuthenticated() {
    return !!localStorage.getItem('piggy_token')
  },

  /**
   * 获取当前用户ID
   */
  getUserId() {
    return localStorage.getItem('piggy_user_id')
  },

  /**
   * 获取当前用户名
   */
  getUsername() {
    return localStorage.getItem('piggy_username') || ''
  },

  /**
   * 获取当前昵称
   */
  getNickname() {
    return localStorage.getItem('piggy_nickname') || ''
  },

  /**
   * 获取 Token
   */
  getToken() {
    return localStorage.getItem('piggy_token')
  },

  /**
   * 检查是否是管理员（默认返回 false，需要后端支持）
   */
  isAdmin() {
    return localStorage.getItem('userRole') === 'admin'
  }
}
