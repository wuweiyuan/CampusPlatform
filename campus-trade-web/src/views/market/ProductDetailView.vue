<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { useRoute, useRouter } from "vue-router";
import { addFavorite, removeFavorite } from "../../api/favorite";
import { getErrorMessage } from "../../api/http";
import { createOrder } from "../../api/order";
import { getProduct, type ProductDetail } from "../../api/product";
import { useAuthStore } from "../../stores/auth";
const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const product = ref<ProductDetail>();
const loading = ref(true);
const errorMessage = ref("");
const favoriteLoading = ref(false);
const buyLoading = ref(false);
const isOwner = computed(() => product.value?.sellerId === authStore.user?.id);
const canBuy = computed(
  () =>
    authStore.isAuthenticated &&
    product.value?.status === "ON_SALE" &&
    !isOwner.value,
);
const canToggleFavorite = computed(
  () =>
    authStore.isAuthenticated &&
    product.value?.status === "ON_SALE" &&
    Boolean(product.value),
);
async function load() {
  loading.value = true;
  errorMessage.value = "";
  try {
    product.value = (await getProduct(Number(route.params.id))).data.data;
  } catch (error) {
    errorMessage.value = getErrorMessage(error, "商品不存在或已下架");
  } finally {
    loading.value = false;
  }
}
async function toggleFavorite() {
  if (!product.value) return;

  const wasFavorited = product.value.favorited;
  favoriteLoading.value = true;
  try {
    if (wasFavorited) {
      await removeFavorite(product.value.id);
    } else {
      await addFavorite(product.value.id);
    }
    product.value.favorited = !wasFavorited;
    ElMessage.success(wasFavorited ? "已取消收藏" : "收藏成功");
  } catch (error) {
    ElMessage.error(
      getErrorMessage(error, wasFavorited ? "取消收藏失败" : "收藏失败"),
    );
  } finally {
    favoriteLoading.value = false;
  }
}
async function buy() {
  if (!product.value) return;

  try {
    await ElMessageBox.confirm(
      `确定购买“${product.value.title}”吗？最终价格以服务端订单快照为准。`,
      "确认购买",
      {
        type: "warning",
        confirmButtonText: "确认下单",
        cancelButtonText: "再想想",
      },
    );
    buyLoading.value = true;
    await createOrder(product.value.id);
    ElMessage.success("下单成功");
    await router.push({ name: "orders", query: { tab: "buying" } });
  } catch (error) {
    if (error !== "cancel" && error !== "close") {
      ElMessage.error(getErrorMessage(error, "下单失败"));
    }
  } finally {
    buyLoading.value = false;
  }
}
onMounted(load);
</script>
<template>
  <section class="detail-page">
    <el-skeleton v-if="loading" animated :rows="8" /><el-result
      v-else-if="errorMessage"
      icon="warning"
      title="无法查看该商品"
      :sub-title="errorMessage"
      ><template #extra
        ><el-button type="primary" @click="router.push('/')"
          >返回商品广场</el-button
        ></template
      ></el-result
    ><template v-else-if="product"
      ><el-button text @click="router.back()">← 返回广场</el-button>
      <article class="detail-card">
        <img
          v-if="product.imageBase64"
          :src="product.imageBase64"
          :alt="product.title"
        />
        <div v-else class="detail-placeholder">
          {{ product.categoryName.slice(0, 2) }}
        </div>
        <div>
          <span class="category-label">{{ product.categoryName }}</span>
          <h1>{{ product.title }}</h1>
          <strong class="detail-price"
            >¥ {{ Number(product.price).toFixed(2) }}</strong
          >
          <p class="detail-description">{{ product.description }}</p>
          <dl>
            <dt>发布人</dt>
            <dd>{{ product.sellerName }}</dd>
            <dt>浏览量</dt>
            <dd>{{ product.viewCount }}</dd>
            <dt>发布时间</dt>
            <dd>{{ new Date(product.createdAt).toLocaleString("zh-CN") }}</dd>
          </dl>
          <el-button
            v-if="isOwner && product.status === 'ON_SALE'"
            type="primary"
            @click="router.push(`/products/${product.id}/edit`)"
            >编辑商品</el-button
          >
          <el-button
            v-if="canBuy"
            type="primary"
            :loading="buyLoading"
            :disabled="buyLoading"
            @click="buy"
            >立即购买</el-button
          >
          <el-button
            v-if="canToggleFavorite"
            :type="product.favorited ? 'info' : 'primary'"
            :plain="product.favorited"
            :loading="favoriteLoading"
            :disabled="favoriteLoading"
            @click="toggleFavorite"
          >
            {{ product.favorited ? "取消收藏" : "收藏" }}
          </el-button>
        </div>
      </article></template
    >
  </section>
</template>
