<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { logout } from '../api/auth'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const displayName = computed(() => authStore.user?.username ?? '用户')

async function handleLogout() {
  try {
    await logout()
  } catch {
    // Token 已失效时，仍应完成本地退出。
  } finally {
    authStore.clearSession()
    ElMessage.success('已退出登录')
    await router.replace({ name: 'login' })
  }
}
</script>

<template>
  <el-container class="app-layout">
    <el-aside width="220px" class="sidebar">
      <RouterLink to="/" class="brand">校园二手交易</RouterLink>
      <el-menu :default-active="route.path" :router="true" class="nav-menu">
        <el-menu-item index="/">商品广场</el-menu-item>
        <el-menu-item index="/products/new">发布商品</el-menu-item>
        <el-menu-item index="/my-products">我的发布</el-menu-item>
        <el-menu-item index="/favorites">我的收藏</el-menu-item>
        <el-menu-item index="/orders">我的订单</el-menu-item>
        <el-menu-item index="/profile">个人中心</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="topbar">
        <span>欢迎，{{ displayName }}</span>
        <div class="topbar-actions">
          <el-button text @click="router.push('/profile')">个人中心</el-button>
          <el-button type="primary" plain @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="page-content"><RouterView /></el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.app-layout { min-height: 100vh; }
.sidebar { background: #1f2937; padding: 24px 12px; }
.brand { display: block; margin: 0 12px 24px; color: #fff; font-size: 18px; font-weight: 700; text-decoration: none; }
.nav-menu { border-right: 0; }
.topbar { display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #e5e7eb; background: #fff; }
.topbar-actions { display: flex; gap: 8px; }
.page-content { padding: 32px; background: #f6f7fb; }
@media (max-width: 700px) { .sidebar { width: 160px !important; } .page-content { padding: 20px; } }
</style>
