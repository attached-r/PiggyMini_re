<!-- src/components/ProtectedRoute.vue -->
<template>
  <slot v-if="authStore.isAuthenticated"></slot>
  <router-view v-else />
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

onMounted(() => {
  if (!authStore.isAuthenticated) {
    router.replace('/login')
  }
})
</script>
