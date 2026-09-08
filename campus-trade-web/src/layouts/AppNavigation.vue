<script setup lang="ts">
import { useAuthStore } from "../stores/auth";
const authStore = useAuthStore();
</script>

<template>
<nav class="app-navigation" aria-label="主导航">
        <RouterLink to="/">商品广场</RouterLink
        ><template v-if="authStore.isAuthenticated"
          ><RouterLink to="/products/new">发布商品</RouterLink
          ><RouterLink to="/my-products">我的发布</RouterLink
          ><RouterLink to="/favorites">我的收藏</RouterLink
          ><RouterLink to="/orders">我的订单</RouterLink
          ><template v-if="authStore.user?.role === 'ADMIN'"
            ><span class="nav-section">管理后台</span>
            <RouterLink class="admin-link" to="/admin/categories">分类管理</RouterLink>
            <RouterLink class="admin-link" to="/admin/users">用户管理</RouterLink>
            <RouterLink class="admin-link" to="/admin/products">商品管理</RouterLink>
            <RouterLink class="admin-link" to="/admin/orders">订单管理</RouterLink></template
          ><RouterLink to="/profile">个人中心</RouterLink></template
        >
      </nav>
</template>
