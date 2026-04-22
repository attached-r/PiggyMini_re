<!-- src/views/Dashboard.vue -->
<template>
  <div class="dashboard">
    <!-- 欢迎信息 -->
    <div class="welcome-section">
      <h1>欢迎回来，{{ authStore.userInfo?.nickname || authStore.userInfo?.username || '用户' }}！</h1>
      <p class="date">{{ currentDate }}</p>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载中...</span>
    </div>

    <!-- 统计卡片 -->
    <div v-else class="stats-grid">
      <div class="stat-card income">
        <div class="stat-icon">
          <el-icon><Top /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-label">总收入</span>
          <span class="stat-value">¥ {{ formatNumber(summaryData.income) }}</span>
        </div>
      </div>

      <div class="stat-card expense">
        <div class="stat-icon">
          <el-icon><Bottom /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-label">总支出</span>
          <span class="stat-value">¥ {{ formatNumber(summaryData.expense) }}</span>
        </div>
      </div>

      <div class="stat-card balance">
        <div class="stat-icon">
          <el-icon><Coin /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-label">账户余额</span>
          <span class="stat-value" :class="summaryData.balance >= 0 ? 'positive' : 'negative'">
            ¥ {{ formatNumber(summaryData.balance) }}
          </span>
        </div>
      </div>
    </div>

    <!-- 错误提示 -->
    <el-alert
      v-if="error"
      :title="error"
      type="error"
      show-icon
      :closable="false"
      class="error-alert"
    />

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <h2>快捷操作</h2>
      <div class="action-cards">
        <el-card class="action-card" @click="router.push('/transactions')">
          <el-icon :size="32"><Plus /></el-icon>
          <span>记一笔</span>
        </el-card>
        <el-card class="action-card" @click="router.push('/budgets')">
          <el-icon :size="32"><Coin /></el-icon>
          <span>预算管理</span>
        </el-card>
        <el-card class="action-card" @click="router.push('/reports')">
          <el-icon :size="32"><PieChart /></el-icon>
          <span>查看报表</span>
        </el-card>
        <el-card class="action-card" @click="router.push('/ai')">
          <el-icon :size="32"><MagicStick /></el-icon>
          <span>AI 记账</span>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getSummaryReport } from '@/api/reports'
import {
  Loading,
  Top,
  Bottom,
  Coin,
  Plus,
  PieChart,
  MagicStick
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(true)
const error = ref('')
const summaryData = ref({
  income: 0,
  expense: 0,
  balance: 0,
  trend: 'neutral'
})

const currentDate = computed(() => {
  const now = new Date()
  return now.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})

const formatNumber = (num) => {
  return Number(num || 0).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

const fetchSummaryData = async () => {
  loading.value = true
  error.value = ''

  try {
    const res = await getSummaryReport({ period: 'month' })

    if (res.code === 200 && res.success) {
      summaryData.value = res.data
    } else {
      error.value = res.message || '获取数据失败'
    }
  } catch (err) {
    console.error('获取报表失败:', err)
    error.value = err.response?.data?.message || '网络错误，请稍后重试'
    ElMessage.error('获取报表数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  authStore.initAuth()
  fetchSummaryData()
})
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-section {
  margin-bottom: 24px;
}

.welcome-section h1 {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.date {
  color: #909399;
  font-size: 14px;
}

.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 60px;
  color: #909399;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.stat-card.income .stat-icon {
  background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
  color: #fff;
}

.stat-card.expense .stat-icon {
  background: linear-gradient(135deg, #f56c6c 0%, #fab6b6 100%);
  color: #fff;
}

.stat-card.balance .stat-icon {
  background: linear-gradient(135deg, #409eff 0%, #79bbff 100%);
  color: #fff;
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.stat-value.positive {
  color: #67c23a;
}

.stat-value.negative {
  color: #f56c6c;
}

.error-alert {
  margin-bottom: 24px;
}

.quick-actions h2 {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
}

.action-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
}

.action-card {
  cursor: pointer;
  text-align: center;
  padding: 20px;
  transition: all 0.3s;
}

.action-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  border-color: #409eff;
}

.action-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: #606266;
}

.action-card span {
  font-size: 14px;
}

/* 响应式 */
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .stat-card {
    padding: 16px;
  }

  .stat-value {
    font-size: 20px;
  }
}
</style>
