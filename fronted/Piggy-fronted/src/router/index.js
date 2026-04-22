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
    },
    {
      path: '/accounts',
      name: 'AccountSelect',
      component: () => import('@/views/AccountSelectView.vue'),
      meta: { requiresAuth: true, requiresAdmin: false }
    },
    {
      path: '/services',
      redirect: '/dashboard'
    },
    {
      path: '/dashboard',
      name: 'Dashboard',
      component: () => import('@/views/finance/DashboardView.vue'),
      meta: { requiresAuth: true, requiresAdmin: false }
    },
    {
      path: '/transactions',
      name: 'Transactions',
      component: () => import('@/views/TransactionServiceView.vue'),
      meta: { requiresAuth: true, requiresAdmin: false }
    },
    {
      path: '/budgets',
      name: 'Budgets',
      component: () => import('@/views/BudgetServiceView.vue'),
      meta: { requiresAuth: true, requiresAdmin: false }
    },
    {
      path: '/reports',
      name: 'Reports',
      component: () => import('@/views/ReportServiceView.vue'),
      meta: { requiresAuth: true, requiresAdmin: false }
    },
    {
      path: '/ai',
      name: 'AiService',
      component: () => import('@/views/AIServiceView.vue'),
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
    // 如果已登录但不是管理员，跳转到用户首页
    if (isAuthenticated) {
      return '/dashboard'
    }
    return '/login'
  }

  // 已登录用户访问登录/注册页
  if ((to.path === '/login' || to.path === '/register') && isAuthenticated) {
    // 管理员跳转到管理后台，普通用户跳转到仪表盘
    return isAdmin ? '/home' : '/dashboard'
  }

  return true
})

export default router
