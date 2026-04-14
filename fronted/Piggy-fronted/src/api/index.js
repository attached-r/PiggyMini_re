const BASE_URL = 'http://localhost:9000/api'

class ApiService {
  constructor() {
    this.token = localStorage.getItem('token') || null
  }

  getToken() {
    return localStorage.getItem('token')
  }

  setToken(token) {
    this.token = token
    localStorage.setItem('token', token)
  }

  removeToken() {
    this.token = null
    localStorage.removeItem('token')
  }

  async request(endpoint, options = {}) {
    const url = `${BASE_URL}${endpoint}`
    const headers = {
      'Content-Type': 'application/json',
      ...options.headers,
    }

    // 如果 token 存在，添加到请求头
    if (this.token) {
      headers['Authorization'] = `Bearer ${this.token}`
    }

    const config = {
      ...options,
      headers,
    }

    try {
      const response = await fetch(url, config)
      const data = await response.json()

      if (!response.ok) {
        throw new Error(data.message || 'Request failed')
      }

      return data
    } catch (error) {
      console.error('API Error:', error)
      throw error
    }
  }

  async post(endpoint, data) {
    return this.request(endpoint, {
      method: 'POST',
      body: JSON.stringify(data),
    })
  }

  async get(endpoint) {
    return this.request(endpoint, {
      method: 'GET',
    })
  }
}

export const apiService = new ApiService()
