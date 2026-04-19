<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const message = ref('Transaction Service View - Test')

const goBack = () => {
  router.push('/dashboard')
}

onMounted(() => {
  console.log('TransactionServiceView mounted')
})
</script>

<template>
  <div class="min-h-screen bg-gray-50 p-8">
    <h1 class="text-2xl font-bold mb-4">{{ message }}</h1>
    <button @click="goBack" class="btn-primary">Go Back</button>
  </div>
</template>
