<script setup lang="ts">
import { computed } from "vue";
import { ElMessage } from "element-plus";
import { useRouter } from "vue-router";
import { logout } from "../api/auth";
import { useAuthStore } from "../stores/auth";
const router = useRouter();
const authStore = useAuthStore();
const authenticated = computed(() => authStore.isAuthenticated);
async function handleLogout() {
  try {
    await logout();
  } catch {
    /* 本地会话仍应清除 */
  } finally {
    authStore.clearSession();
    ElMessage.success("已退出登录");
    router.replace("/");
  }
}
</script>
<template>
  <el-container class="app-layout"
    ><el-aside class="sidebar" width="228px"
      ><RouterLink to="/" class="brand"
        ><span>校 易</span><small>CAMPUS MARKET</small></RouterLink
      >
      <nav>
        <RouterLink to="/">商品广场</RouterLink
        ><template v-if="authenticated"
          ><RouterLink to="/products/new">发布商品</RouterLink
          ><RouterLink to="/my-products">我的发布</RouterLink
          ><RouterLink to="/favorites">我的收藏</RouterLink
          ><RouterLink to="/profile">个人中心</RouterLink></template
        >
      </nav>
      <p class="sidebar-note">让闲置在校园里<br />继续被需要。</p></el-aside
    ><el-container
      ><el-header class="topbar"
        ><span class="topbar-location">校园二手交易平台</span>
        <div v-if="authenticated">
          <span>你好，{{ authStore.user?.username }}</span
          ><el-button text @click="handleLogout">退出</el-button>
        </div>
        <div v-else>
          <el-button text @click="router.push('/login')">登录</el-button
          ><el-button type="primary" @click="router.push('/register')"
            >注册</el-button
          >
        </div></el-header
      ><el-main class="page-content"><RouterView /></el-main></el-container
  ></el-container>
</template>
