<script setup lang="ts">
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import { useRouter } from "vue-router";
import {
  getFavorites,
  removeFavorite,
  type FavoriteProduct,
} from "../../api/favorite";
import { getErrorMessage } from "../../api/http";
import type { ProductStatus } from "../../api/product";

const router = useRouter();
const records = ref<FavoriteProduct[]>([]);
const page = ref(1);
const pageSize = ref(12);
const total = ref(0);
const loading = ref(false);
const errorMessage = ref("");
const operatingId = ref<number>();

const statusLabels: Record<ProductStatus, string> = {
  ON_SALE: "在售",
  LOCKED: "已锁定",
  SOLD: "已售出",
  OFF_SHELF: "已下架",
};

function statusType(status: ProductStatus) {
  if (status === "ON_SALE") return "success";
  if (status === "SOLD") return "info";
  return "warning";
}

function formatPrice(price: number) {
  return "¥ " + Number(price).toFixed(2);
}

function formatDate(value: string) {
  return new Date(value).toLocaleDateString("zh-CN");
}

function openProduct(productId: number) {
  router.push({ name: "product-detail", params: { id: productId } });
}

async function load(nextPage = page.value) {
  loading.value = true;
  errorMessage.value = "";
  try {
    const data = (
      await getFavorites({
        page: nextPage,
        pageSize: pageSize.value,
      })
    ).data.data;
    records.value = data.records;
    page.value = data.page;
    total.value = data.total;
  } catch (error) {
    errorMessage.value = getErrorMessage(error, "我的收藏加载失败");
  } finally {
    loading.value = false;
  }
}

async function cancelFavorite(item: FavoriteProduct) {
  operatingId.value = item.id;
  try {
    await removeFavorite(item.productId);
    ElMessage.success("已取消收藏");

    if (records.value.length === 1 && page.value > 1) {
      await load(page.value - 1);
    } else {
      await load();
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, "取消收藏失败"));
  } finally {
    operatingId.value = undefined;
  }
}

function changePageSize(size: number) {
  pageSize.value = size;
  load(1);
}

onMounted(() => load(1));
</script>

<template>
  <section class="favorites-page">
    <header>
      <p class="eyebrow">MY FAVORITES</p>
      <h1>我的收藏</h1>
      <p>留住想再看一看的好物。</p>
    </header>

    <el-skeleton v-if="loading" animated :rows="8" />
    <el-result
      v-else-if="errorMessage"
      icon="error"
      title="加载失败"
      :sub-title="errorMessage"
    >
      <template #extra>
        <el-button type="primary" @click="load()">重新加载</el-button>
      </template>
    </el-result>
    <el-empty v-else-if="!records.length" description="还没有收藏任何商品">
      <el-button type="primary" @click="router.push('/')">去逛逛</el-button>
    </el-empty>
    <template v-else>
      <div class="favorite-list">
        <article v-for="item in records" :key="item.id">
          <img
            v-if="item.imageBase64"
            :src="item.imageBase64"
            :alt="item.title"
            @click="openProduct(item.productId)"
          />
          <button
            v-else
            class="image-placeholder favorite-placeholder"
            type="button"
            @click="openProduct(item.productId)"
          >
            {{ item.categoryName.slice(0, 2) }}
          </button>
          <div class="favorite-info">
            <div class="favorite-title-row">
              <h2 @click="openProduct(item.productId)">{{ item.title }}</h2>
              <el-tag :type="statusType(item.status)">{{
                statusLabels[item.status]
              }}</el-tag>
            </div>
            <strong>{{ formatPrice(item.price) }}</strong>
            <p>
              {{ item.categoryName }} · {{ item.sellerName }} · 收藏于
              {{ formatDate(item.favoriteCreatedAt) }}
            </p>
          </div>
          <el-button
            plain
            :loading="operatingId === item.id"
            :disabled="operatingId === item.id"
            @click="cancelFavorite(item)"
          >
            取消收藏
          </el-button>
        </article>
      </div>
      <el-pagination
        v-if="total > 0"
        class="pagination"
        background
        layout="total, sizes, prev, pager, next"
        :page-sizes="[12, 20, 30, 50]"
        :current-page="page"
        :page-size="pageSize"
        :total="total"
        @size-change="changePageSize"
        @current-change="load"
      />
    </template>
  </section>
</template>
