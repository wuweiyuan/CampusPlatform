<script setup lang="ts">
import { onMounted, ref } from 'vue'
import http from '../api/http'

const status = ref('加载中…')
const isAvailable = ref<boolean | null>(null)

interface HealthResponse {
  code: number
  message: string
  data: {
    status: string
  }
}

onMounted(async () => {
  try {
    const { data } = await http.get<HealthResponse>('/health')
    status.value = data.data.status
    isAvailable.value = true
  } catch {
    isAvailable.value = false
  }
})
</script>

<template>
  <main class="health-page">
    <el-card class="health-card" shadow="never">
      <h1>后端状态</h1>
      <p v-if="isAvailable">后端状态：{{ status }}</p>
      <p v-else-if="isAvailable === false">后端不可用</p>
      <p v-else>加载中…</p>
    </el-card>
  </main>
</template>

<style scoped>
.health-page {
  display: grid;
  min-height: 100vh;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
}

.health-card {
  width: min(100%, 420px);
  text-align: center;
}

h1 {
  margin: 0 0 12px;
  font-size: 24px;
}

p {
  margin: 0;
}
</style>
