<script setup lang="ts">
import { useRouter } from "vue-router";
const router = useRouter();
</script>

<template>
  <main class="status-page">
    <el-result icon="error" title="404 页面不存在" sub-title="请检查访问地址。">
      <template #extra
        ><el-button type="primary" @click="router.push('/')"
          >返回首页</el-button
        ></template
      >
    </el-result>
  </main>
</template>

<style scoped>
.status-page {
  display: grid;
  min-height: 100vh;
  place-items: center;
  background: #f5f2e8;
}
.status-page :deep(.el-result) {
  padding: 48px;
  background: #fffdf7;
  border: 1px solid #d7d0c1;
}
</style>
