import { createRouter, createWebHistory } from 'vue-router'
import { authApi } from '@/api/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/LoginView.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/RegisterView.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/home',
      name: 'Home',
      component: () => import('@/views/HomeView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/user',
      name: 'UserHome',
      component: () => import('@/views/UserHomeView.vue'),
      meta: { requiresAuth: true, requiresAdmin: false }
    }
  ],
})

// 路由守卫
router.beforeEach((to, from) => {
  const isAuthenticated = authApi.isAuthenticated()
  const isAdmin = authApi.isAdmin()

  // 访问需要认证的页面但未登录
  if (to.meta.requiresAuth && !isAuthenticated) {
    return '/login'
  }

  // 访问管理员页面但不是管理员
  if (to.meta.requiresAdmin && !isAdmin) {
    // 如果是管理员账号但角色不对，清除缓存重新登录
    if (isAuthenticated) {
      authApi.logout()
      return '/login'
    }
    return '/login'
  }

  // 已登录用户访问登录/注册页
  if ((to.path === '/login' || to.path === '/register') && isAuthenticated) {
    // 管理员跳转到控制台，普通用户跳转到用户首页
    return isAdmin ? '/home' : '/user'
  }

  return true
})

export default router
