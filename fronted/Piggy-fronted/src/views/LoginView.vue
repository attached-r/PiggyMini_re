<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/auth'

const router = useRouter()
const loading = ref(false)
const error = ref('')

const form = reactive({
  username: '',
  password: ''
})

// 检测是否是管理员账号
const isAdminAccount = () => {
  return form.username === '8888'
}

const validateForm = () => {
  if (!form.username.trim()) {
    error.value = '请输入账号'
    return false
  }
  if (!form.password) {
    error.value = '请输入密码'
    return false
  }
  return true
}

const handleSubmit = async () => {
  error.value = ''
  
  if (!validateForm()) {
    return
  }

  loading.value = true

  try {
    const response = await authApi.login({
      username: form.username,
      password: form.password
    })

    console.log('登录成功，响应数据:', response)
    
    // 检查 token 是否存在
    if (response.token) {
      // 根据用户角色跳转到不同页面
      if (response.isAdmin) {
        // 管理员跳转到控制台
        router.replace('/home')
      } else {
        // 普通用户跳转到账户选择页面
        router.replace('/accounts')
      }
    } else {
      console.error('响应中没有 token 字段，完整响应:', response)
      error.value = '登录失败：服务器未返回 token。请检查后端接口。'
    }
  } catch (err) {
    console.error('Login error:', err)
    error.value = err.message || '登录失败，请检查账号和密码'
  } finally {
    loading.value = false
  }
}

const goToRegister = () => {
  router.push('/register')
}
</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-primary-50 via-white to-primary-100 flex items-center justify-center p-4">
    <div class="w-full max-w-md">
      <!-- Logo/Header -->
      <div class="text-center mb-8">
        <div class="inline-flex items-center justify-center w-16 h-16 bg-primary-600 rounded-2xl mb-4 shadow-lg">
          <svg class="w-10 h-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
          </svg>
        </div>
        <h1 class="text-3xl font-bold text-gray-900">Piggy</h1>
        <p class="text-gray-600 mt-2">欢迎回来，请登录您的账号</p>
      </div>

      <!-- Login Card -->
      <div class="card">
        <form @submit.prevent="handleSubmit" class="space-y-6">
          <!-- Error Message -->
          <div v-if="error" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg flex items-center">
            <svg class="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/>
            </svg>
            {{ error }}
          </div>

          <!-- Username Input -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              账号
            </label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                </svg>
              </div>
              <input
                v-model="form.username"
                type="text"
                placeholder="请输入账号"
                class="input-field pl-10"
                :disabled="loading"
              />
            </div>
          </div>

          <!-- Password Input -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              密码
            </label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
                </svg>
              </div>
              <input
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
                class="input-field pl-10"
                :disabled="loading"
              />
            </div>
          </div>

          <!-- Admin Hint -->
          <div v-if="isAdminAccount()" class="bg-blue-50 border border-blue-200 text-blue-700 px-4 py-3 rounded-lg flex items-center text-sm">
            <svg class="w-5 h-5 mr-2 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"/>
            </svg>
            <span>管理员登录：将进入系统控制台</span>
          </div>

          <!-- Submit Button -->
          <button
            type="submit"
            :disabled="loading"
            class="btn-primary w-full flex items-center justify-center py-3"
          >
            <svg v-if="loading" class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            <span v-if="loading">登录中...</span>
            <span v-else>登录</span>
          </button>
        </form>

        <!-- Register Link -->
        <div class="mt-6 text-center">
          <p class="text-gray-600">
            还没有账号？
            <button
              @click="goToRegister"
              class="text-primary-600 hover:text-primary-700 font-medium focus:outline-none"
            >
              立即注册
            </button>
          </p>
        </div>
      </div>

      <!-- Footer -->
      <div class="text-center mt-8 text-gray-500 text-sm">
        <p>&copy; 2024 Piggy. All rights reserved.</p>
      </div>
    </div>
  </div>
</template>
