<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/auth'

const router = useRouter()
const loading = ref(false)
const error = ref('')

const form = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const validateForm = () => {
  if (!form.username.trim()) {
    error.value = '请输入账号'
    return false
  }
  if (form.username.length < 3) {
    error.value = '账号长度至少为 3 个字符'
    return false
  }
  if (!form.password) {
    error.value = '请输入密码'
    return false
  }
  if (form.password.length < 6) {
    error.value = '密码长度至少为 6 个字符'
    return false
  }
  if (form.password !== form.confirmPassword) {
    error.value = '两次输入的密码不一致'
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
    await authApi.register({
      username: form.username,
      password: form.password
    })

    // 注册成功后自动登录
    const response = await authApi.login({
      username: form.username,
      password: form.password
    })

    if (response.token) {
      router.push('/home')
    }
  } catch (err) {
    error.value = err.message || '注册失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
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
        <h1 class="text-3xl font-bold text-gray-900">创建账号</h1>
        <p class="text-gray-600 mt-2">填写以下信息完成注册</p>
      </div>

      <!-- Register Card -->
      <div class="card">
        <form @submit.prevent="handleSubmit" class="space-y-5">
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
                placeholder="请输入账号（至少 3 个字符）"
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
                placeholder="请输入密码（至少 6 个字符）"
                class="input-field pl-10"
                :disabled="loading"
              />
            </div>
          </div>

          <!-- Confirm Password Input -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              确认密码
            </label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                </svg>
              </div>
              <input
                v-model="form.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                class="input-field pl-10"
                :disabled="loading"
              />
            </div>
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
            <span v-if="loading">注册中...</span>
            <span v-else>注册</span>
          </button>
        </form>

        <!-- Login Link -->
        <div class="mt-6 text-center">
          <p class="text-gray-600">
            已有账号？
            <button
              @click="goToLogin"
              class="text-primary-600 hover:text-primary-700 font-medium focus:outline-none"
            >
              立即登录
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
