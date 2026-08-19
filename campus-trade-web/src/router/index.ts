import { createRouter, createWebHistory } from 'vue-router'
import HealthView from '../views/HealthView.vue'
import AppLayout from '../layouts/AppLayout.vue'
import ComingSoonView from '../views/ComingSoonView.vue'
import ForbiddenView from '../views/ForbiddenView.vue'
import LoginView from '../views/LoginView.vue'
import NotFoundView from '../views/NotFoundView.vue'
import ProfileView from '../views/ProfileView.vue'
import RegisterView from '../views/RegisterView.vue'
import pinia from '../stores/pinia'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'login', component: LoginView },
    { path: '/register', name: 'register', component: RegisterView },
    {
      path: '/',
      component: AppLayout,
      meta: { requiresAuth: true },
      children: [
        { path: '', name: 'home', component: ComingSoonView, props: { title: '商品广场', description: '商品浏览功能将在阶段三完成。' } },
        { path: 'products/new', name: 'product-create', component: ComingSoonView, props: { title: '发布商品', description: '商品发布功能将在阶段三完成。' } },
        { path: 'my-products', name: 'my-products', component: ComingSoonView, props: { title: '我的发布', description: '我的商品功能将在阶段三完成。' } },
        { path: 'favorites', name: 'favorites', component: ComingSoonView, props: { title: '我的收藏', description: '收藏功能将在阶段四完成。' } },
        { path: 'orders', name: 'orders', component: ComingSoonView, props: { title: '我的订单', description: '订单功能将在阶段五完成。' } },
        { path: 'profile', name: 'profile', component: ProfileView },
        { path: 'admin', name: 'admin', component: ComingSoonView, props: { title: '管理后台', description: '管理功能将在阶段六完成。' }, meta: { roles: ['ADMIN'] } },
      ],
    },
    { path: '/health', name: 'health', component: HealthView },
    { path: '/403', name: 'forbidden', component: ForbiddenView },
    { path: '/:pathMatch(.*)*', name: 'not-found', component: NotFoundView },
  ],
})

router.beforeEach((to) => {
  const authStore = useAuthStore(pinia)

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.roles && (!authStore.user || !to.meta.roles.includes(authStore.user.role))) {
    return { name: 'forbidden' }
  }
  return true
})

export default router
