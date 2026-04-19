<!-- src/layouts/MainLayout.vue -->
<template>
  <el-container class="main-layout">
    <!-- 顶部 Header -->
    <el-header class="header">
      <div class="header-left">
        <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
          <Expand v-if="isCollapse" />
          <Fold v-else />
        </el-icon>
        <div class="logo">
          <span class="logo-text">🐷 Piggy</span>
        </div>
      </div>
      <div class="header-right">
        <span class="current-account">
          {{ currentAccount || '未选择账户' }}
        </span>
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-icon><User /></el-icon>
            <span>{{ authStore.userInfo?.nickname || authStore.userInfo?.username || '用户' }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>个人中心
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <el-icon><SwitchButton /></el-icon>退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-container>
      <!-- 左侧侧边栏 -->
      <el-aside :width="isCollapse ? '64px' : '200px'" class="aside">
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :router="true"
          class="sidebar-menu"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409eff"
        >
          <el-menu-item index="/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <template #title>仪表盘</template>
          </el-menu-item>
          <el-menu-item index="/accounts">
            <el-icon><Wallet /></el-icon>
            <template #title>账户管理</template>
          </el-menu-item>
          <el-menu-item index="/transactions">
            <el-icon><List /></el-icon>
            <template #title>交易流水</template>
          </el-menu-item>
          <el-menu-item index="/budgets">
            <el-icon><Coin /></el-icon>
            <template #title>预算管理</template>
          </el-menu-item>
          <el-menu-item index="/reports">
            <el-icon><PieChart /></el-icon>
            <template #title>报表中心</template>
          </el-menu-item>
          <el-menu-item index="/ai">
            <el-icon><MagicStick /></el-icon>
            <template #title>AI 智能记账</template>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  Fold,
  Expand,
  User,
  ArrowDown,
  SwitchButton,
  DataAnalysis,
  Wallet,
  List,
  Coin,
  PieChart,
  MagicStick
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const isCollapse = ref(false)

const currentAccount = computed(() => {
  return localStorage.getItem('selectedAccount') || ''
})

const activeMenu = computed(() => {
  return route.path
})

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'logout':
      authStore.logout()
      break
  }
}

// 初始化
onMounted(() => {
  authStore.initAuth()
})
</script>

<style scoped>
.main-layout {
  height: 100vh;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #304156;
  color: #fff;
  padding: 0 20px;
  height: 60px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #bfcbd9;
  transition: color 0.3s;
}

.collapse-btn:hover {
  color: #409eff;
}

.logo {
  font-size: 20px;
  font-weight: bold;
}

.logo-text {
  color: #409eff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.current-account {
  color: #bfcbd9;
  font-size: 14px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  color: #bfcbd9;
  padding: 0 10px;
  border-radius: 4px;
  transition: all 0.3s;
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.aside {
  background: #304156;
  transition: width 0.3s;
  overflow-x: hidden;
}

.sidebar-menu {
  border-right: none;
  height: calc(100vh - 60px);
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 200px;
}

.main-content {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

/* 响应式 - 移动端 */
@media (max-width: 768px) {
  .header {
    padding: 0 10px;
  }

  .current-account {
    display: none;
  }

  .main-content {
    padding: 10px;
  }
}
</style>
