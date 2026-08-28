import { createRouter, createWebHistory } from "vue-router";
import pinia from "../stores/pinia";
import { useAuthStore } from "../stores/auth";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/login",
      name: "login",
      component: () => import("../views/LoginView.vue"),
    },
    {
      path: "/register",
      name: "register",
      component: () => import("../views/RegisterView.vue"),
    },
    {
      path: "/",
      component: () => import("../layouts/AppLayout.vue"),
      children: [
        {
          path: "",
          name: "home",
          component: () => import("../views/market/ProductMarketView.vue"),
        },
        {
          path: "products/new",
          name: "product-create",
          component: () => import("../views/product/ProductFormView.vue"),
          meta: { requiresAuth: true },
        },
        {
          path: "products/:id(\\d+)",
          name: "product-detail",
          component: () => import("../views/market/ProductDetailView.vue"),
        },
        {
          path: "products/:id(\\d+)/edit",
          name: "product-edit",
          component: () => import("../views/product/ProductFormView.vue"),
          meta: { requiresAuth: true },
        },
        {
          path: "my-products",
          name: "my-products",
          component: () => import("../views/product/MyProductsView.vue"),
          meta: { requiresAuth: true },
        },
        {
          path: "favorites",
          name: "favorites",
          component: () => import("../views/ComingSoonView.vue"),
          props: { title: "我的收藏", description: "收藏功能将在阶段四完成。" },
          meta: { requiresAuth: true },
        },
        {
          path: "orders",
          name: "orders",
          component: () => import("../views/ComingSoonView.vue"),
          props: { title: "我的订单", description: "订单功能将在阶段五完成。" },
          meta: { requiresAuth: true },
        },
        {
          path: "profile",
          name: "profile",
          component: () => import("../views/ProfileView.vue"),
          meta: { requiresAuth: true },
        },
        {
          path: "admin",
          name: "admin",
          component: () => import("../views/ComingSoonView.vue"),
          props: { title: "管理后台", description: "管理功能将在阶段六完成。" },
          meta: { requiresAuth: true, roles: ["ADMIN"] },
        },
      ],
    },
    {
      path: "/health",
      name: "health",
      component: () => import("../views/HealthView.vue"),
    },
    {
      path: "/403",
      name: "forbidden",
      component: () => import("../views/ForbiddenView.vue"),
    },
    {
      path: "/:pathMatch(.*)*",
      name: "not-found",
      component: () => import("../views/NotFoundView.vue"),
    },
  ],
});

router.beforeEach((to) => {
  const authStore = useAuthStore(pinia);

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return { name: "login", query: { redirect: to.fullPath } };
  }
  if (
    to.meta.roles &&
    (!authStore.user || !to.meta.roles.includes(authStore.user.role))
  ) {
    return { name: "forbidden" };
  }
  return true;
});

export default router;
