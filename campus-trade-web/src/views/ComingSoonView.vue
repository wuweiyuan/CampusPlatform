<script setup lang="ts">
defineProps<{ title: string; description: string }>()
</script>

<template>
  <el-card class="page-card" shadow="never">
    <h1>{{ title }}</h1>
    <p>{{ description }}</p>
  </el-card>
</template>

<style scoped>
.page-card { max-width: 760px; }
h1 { margin-top: 0; font-size: 24px; }
p { color: #6b7280; }
</style>
