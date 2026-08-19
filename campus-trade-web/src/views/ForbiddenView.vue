<script setup lang="ts">
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<template>
  <main class="status-page">
    <el-result icon="warning" title="403 无权访问" sub-title="你没有访问此页面的权限。">
      <template #extra><el-button type="primary" @click="router.push('/')">返回首页</el-button></template>
    </el-result>
  </main>
</template>

<style scoped>.status-page { display: grid; min-height: 100vh; place-items: center; }</style>
