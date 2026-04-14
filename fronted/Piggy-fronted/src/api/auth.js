import { apiService } from './index'

export const authApi = {
  // 登录
  async login(credentials) {
    // 检查是否是管理员账号
    const isAdmin = credentials.username === '8888' && credentials.password === '8888'
    
    // 如果是管理员，直接生成一个模拟的 token
    if (isAdmin) {
      const mockToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6Ijg4ODgiLCJyb2xlIjoiYWRtaW4iLCJpYXQiOjE2OTk5OTk5OTksImV4cCI6MTcwMDAwMzU5OX0.mock_signature'
      apiService.setToken(mockToken)
      localStorage.setItem('userRole', 'admin')
      localStorage.setItem('username', '8888')
      
      return {
        token: mockToken,
        isAdmin: true,
        message: '管理员登录成功'
      }
    }
    
    // 普通用户需要调用后端 API
    const response = await apiService.post('/auth/login', credentials)
    console.log('登录响应:', response)
    
    // 尝试不同的 token 字段名
    const token = response.token || response.data?.token || response.access_token || response.result?.token
    
    if (token) {
      apiService.setToken(token)
      localStorage.setItem('userRole', 'user')
      localStorage.setItem('username', credentials.username)
    }
    
    return {
      ...response,
      token,
      isAdmin: false
    }
  },

  // 注册
  async register(userData) {
    const response = await apiService.post('/auth/register', userData)
    return response
  },

  // 刷新 token
  async refreshToken() {
    const response = await apiService.post('/auth/refresh')
    if (response.token) {
      apiService.setToken(response.token)
    }
    return response
  },

  // 登出
  logout() {
    apiService.removeToken()
    localStorage.removeItem('userRole')
    localStorage.removeItem('username')
  },

  // 检查是否已登录
  isAuthenticated() {
    return !!apiService.getToken()
  },

  // 检查是否是管理员
  isAdmin() {
    return localStorage.getItem('userRole') === 'admin'
  },

  // 获取当前用户角色
  getUserRole() {
    return localStorage.getItem('userRole') || 'user'
  },

  // 获取当前用户名
  getUsername() {
    return localStorage.getItem('username') || ''
  }
}
