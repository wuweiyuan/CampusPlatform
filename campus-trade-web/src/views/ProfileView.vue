<script setup lang="ts">
import { useAuthStore } from "../stores/auth";

const authStore = useAuthStore();
</script>

<template>
  <el-card class="profile-card" shadow="never">
    <template #header><strong>个人中心</strong></template>
    <el-descriptions v-if="authStore.user" :column="1" border>
      <el-descriptions-item label="用户 ID">{{
        authStore.user.id
      }}</el-descriptions-item>
      <el-descriptions-item label="用户名">{{
        authStore.user.username
      }}</el-descriptions-item>
      <el-descriptions-item label="邮箱">{{
        authStore.user.email
      }}</el-descriptions-item>
      <el-descriptions-item label="角色">{{
        authStore.user.role
      }}</el-descriptions-item>
    </el-descriptions>
  </el-card>
</template>

<style scoped>
.profile-card {
  max-width: 680px;
  border: 1px solid #d7d0c1;
  background: #fffdf7;
}
.profile-card :deep(.el-card__header) {
  background: #164b3a;
  color: #fff;
}
</style>
