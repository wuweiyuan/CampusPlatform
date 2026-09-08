<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { ElMessage } from "element-plus";
import { useRoute, useRouter } from "vue-router";
import { logout } from "../api/auth";
import { getErrorMessage } from "../api/http";
import AppNavigation from "./AppNavigation.vue";
import { useAuthStore } from "../stores/auth";
const router = useRouter();
const route = useRoute();
const menuOpen = ref(false);
const loggingOut = ref(false);
watch(() => route.fullPath, () => { menuOpen.value = false; });
const authStore = useAuthStore();
const authenticated = computed(() => authStore.isAuthenticated);
async function handleLogout() {
  if (loggingOut.value) return;
  loggingOut.value = true;
  try {
    const response = await logout();
    if (response.data.code !== 0) {
      throw new Error(response.data.message);
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, "退出未完成，请稍后重试"));
    return;
  } finally {
    loggingOut.value = false;
  }
  authStore.clearSession();
  menuOpen.value = false;
  ElMessage.success("已退出登录");
  await router.replace("/");
}
</script>
<template>
  <el-container class="app-layout"
    ><el-aside class="sidebar" width="228px"
      ><RouterLink to="/" class="brand"
        ><span>校 易</span><small>CAMPUS MARKET</small></RouterLink
      >
      <AppNavigation />
      <p class="sidebar-note">让闲置在校园里<br />继续被需要。</p></el-aside
    ><el-container
      ><el-header class="topbar"
        ><el-button
          class="mobile-menu-button"
          aria-label="打开导航菜单"
          :aria-expanded="menuOpen"
          aria-controls="mobile-navigation"
          @click="menuOpen = true"
        >菜单</el-button>
        <span class="topbar-location">校园二手交易平台</span>
        <div v-if="authenticated">
          <span>你好，{{ authStore.user?.username }}</span
          ><el-button text :loading="loggingOut" :disabled="loggingOut" @click="handleLogout">退出</el-button>
        </div>
        <div v-else>
          <el-button text @click="router.push('/login')">登录</el-button
          ><el-button type="primary" @click="router.push('/register')"
            >注册</el-button
          >
        </div></el-header
      ><el-main class="page-content"><RouterView /></el-main></el-container
  ></el-container>
  <el-drawer
    v-model="menuOpen"
    class="mobile-navigation-drawer"
    title="校易 · 导航"
    direction="ltr"
    size="min(300px, 88vw)"
  >
    <div id="mobile-navigation" @click="menuOpen = false">
      <AppNavigation />
    </div>
  </el-drawer>
</template>
