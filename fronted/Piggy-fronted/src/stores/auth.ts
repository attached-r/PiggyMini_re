// src/stores/auth.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import apiClient from '@/utils/axios'
import router from '@/router'

export const useAuthStore = defineStore('auth', () => {
  // State
  const token = ref(localStorage.getItem('piggy_token') || '')
  const refreshToken = ref(localStorage.getItem('piggy_refresh_token') || '')
  const userInfo = ref<any>(null)

  // Getters
  const isAuthenticated = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'admin')
  const getUserId = computed(() => userInfo.value?.userId)

  // Actions - 登录（已优化）
  const login = async (credentials: { username: string; password: string }) => {
    try {
      console.log('开始登录:', credentials)

      const response = await apiClient.post('/auth/login', credentials)
      console.log('登录响应原始数据:', response.data)

      let data = response.data

      // 处理两种可能的响应结构
      if (data && data.data) {
        data = data.data
      }

      const tokenValue = data?.token

      if (!tokenValue) {
        throw new Error(data?.message || '登录失败，未获取到 token')
      }

      // 保存 token 和用户信息
      token.value = tokenValue
      refreshToken.value = data.refreshToken || ''
      userInfo.value = {
        userId: data.userId || data.id,
        username: data.username,
        nickname: data.nickname || data.username,
        role: data.role || 'user',
      }

      localStorage.setItem('piggy_token', tokenValue)
      if (refreshToken.value) {
        localStorage.setItem('piggy_refresh_token', refreshToken.value)
      }
      // 保存 userId 供其他 API 使用
      localStorage.setItem('piggy_user_id', userInfo.value.userId)
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))

      console.log('✅ 登录成功！token 已保存:', tokenValue.substring(0, 30) + '...')

      // 返回成功标志
      return true
    } catch (error: any) {
      console.error('登录失败:', error)

      const message = error.response?.data?.message
        || error.response?.data?.msg
        || error.message
        || '登录失败，请检查账号密码'

      console.error('错误信息:', message)
      return false
    }
  }

  const register = async (userData: { username: string; password: string; nickname: string }) => {
    try {
      const response = await apiClient.post('/auth/register', userData)
      console.log('注册响应:', response.data)

      // 检查注册是否成功
      if (response.data?.code === 200 || response.data?.success === true) {
        console.log('注册成功')
        router.push('/login')
        return { success: true }
      }

      throw new Error(response.data?.message || '注册失败')
    } catch (error: any) {
      const message = error.response?.data?.message || error.response?.data?.msg || '注册失败'
      console.error('注册失败:', message)
      return { success: false, message }
    }
  }

  const logout = () => {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null

    localStorage.removeItem('piggy_token')
    localStorage.removeItem('piggy_refresh_token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('selectedAccount')

    console.log('已退出登录')
    router.push('/login')
  }

  // 初始化时从 localStorage 恢复用户信息
  const initAuth = () => {
    const savedUser = localStorage.getItem('userInfo')
    if (savedUser) {
      try {
        userInfo.value = JSON.parse(savedUser)
      } catch (e) {
        console.error('解析用户信息失败:', e)
      }
    }
  }

  return {
    token,
    refreshToken,
    userInfo,
    isAuthenticated,
    isAdmin,
    getUserId,
    login,
    register,
    logout,
    initAuth,
  }
})
