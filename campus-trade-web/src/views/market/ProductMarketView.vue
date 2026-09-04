<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import { useRouter } from "vue-router";
import { getCategories, type Category } from "../../api/category";
import { addFavorite, removeFavorite } from "../../api/favorite";
import { getErrorMessage } from "../../api/http";
import {
  getHotProducts,
  getProducts,
  type HotProduct,
  type PageResponse,
  type ProductCard,
} from "../../api/product";
import { useAuthStore } from "../../stores/auth";

const router = useRouter();
const authStore = useAuthStore();
const categories = ref<Category[]>([]);
const records = ref<ProductCard[]>([]);
const hotProducts = ref<HotProduct[]>([]);
const hotLoading = ref(false);
const hotErrorMessage = ref("");
const keyword = ref("");
const categoryId = ref<number>();
const page = ref(1);
const pageSize = ref(10);
const total = ref(0);
const loading = ref(false);
const errorMessage = ref("");
const operatingId = ref<number>();
const hasFilters = computed(() => keyword.value.trim() || categoryId.value);

function placeholderClass(name: string) {
  return `placeholder-${name.charCodeAt(0) % 4}`;
}
function formatPrice(price: number) {
  return `¥ ${Number(price).toFixed(2)}`;
}
function formatDate(value: string) {
  return new Date(value).toLocaleDateString("zh-CN");
}
async function loadProducts(nextPage = page.value) {
  loading.value = true;
  errorMessage.value = "";
  try {
    const response = await getProducts({
      page: nextPage,
      pageSize: pageSize.value,
      ...(categoryId.value ? { categoryId: categoryId.value } : {}),
      ...(keyword.value.trim() ? { keyword: keyword.value.trim() } : {}),
    });
    const data: PageResponse<ProductCard> = response.data.data;
    records.value = data.records;
    total.value = data.total;
    page.value = data.page;
  } catch (error) {
    errorMessage.value = getErrorMessage(error, "商品列表加载失败");
  } finally {
    loading.value = false;
  }
}
async function loadHotProducts() {
  hotLoading.value = true;
  hotErrorMessage.value = "";
  try {
    hotProducts.value = (await getHotProducts()).data.data;
  } catch (error) {
    hotErrorMessage.value = getErrorMessage(error, "热门商品加载失败");
  } finally {
    hotLoading.value = false;
  }
}
async function loadCategories() {
  try {
    categories.value = (await getCategories()).data.data;
  } catch {
    /* 筛选项失败不阻断广场 */
  }
}
function search() {
  loadProducts(1);
}
function selectCategory(id?: number) {
  categoryId.value = id;
  loadProducts(1);
}
function reset() {
  keyword.value = "";
  categoryId.value = undefined;
  loadProducts(1);
}
function changePageSize(size: number) {
  pageSize.value = size;
  loadProducts(1);
}
async function toggleFavorite(item: ProductCard) {
  const wasFavorited = item.favorited;
  operatingId.value = item.id;
  try {
    if (wasFavorited) {
      await removeFavorite(item.id);
    } else {
      await addFavorite(item.id);
    }
    item.favorited = !wasFavorited;
    ElMessage.success(wasFavorited ? "已取消收藏" : "收藏成功");
  } catch (error) {
    ElMessage.error(
      getErrorMessage(error, wasFavorited ? "取消收藏失败" : "收藏失败"),
    );
  } finally {
    operatingId.value = undefined;
  }
}
onMounted(() => {
  loadCategories();
  loadHotProducts();
  loadProducts(1);
});
</script>

<template>
  <section class="market-page">
    <header class="market-heading">
      <div>
        <p class="eyebrow">CAMPUS MARKET</p>
        <h1>今天，发现一件刚好的旧物</h1>
        <p>从同学手中，让好物继续被需要。</p>
      </div>
      <el-button type="primary" @click="router.push('/products/new')"
        >发布闲置</el-button
      >
    </header>
    <div class="market-filter">
      <el-input
        v-model="keyword"
        placeholder="搜索教材、耳机、宿舍好物…"
        clearable
        @keyup.enter="search"
        ><template #append
          ><el-button @click="search">搜索</el-button></template
        ></el-input
      >
      <div class="category-chips">
        <button :class="{ active: !categoryId }" @click="selectCategory()">
          全部</button
        ><button
          v-for="item in categories"
          :key="item.id"
          :class="{ active: categoryId === item.id }"
          @click="selectCategory(item.id)"
        >
          {{ item.name }}
        </button>
      </div>
    </div>
    <section class="hot-products-section">
      <header class="hot-products-heading">
        <div>
          <p class="hot-eyebrow">TRENDING NOW</p>
          <h2>本周热门</h2>
        </div>
        <span>按浏览热度排序</span>
      </header>
      <div v-if="hotLoading" class="hot-products-grid">
        <el-skeleton v-for="i in 4" :key="i" animated>
          <template #template>
            <el-skeleton-item variant="image" style="height: 96px" />
            <el-skeleton-item style="width: 70%; margin-top: 10px" />
          </template>
        </el-skeleton>
      </div>
      <div v-else-if="hotErrorMessage" class="hot-products-message">
        <span>{{ hotErrorMessage }}</span>
        <el-button text type="primary" @click="loadHotProducts">重试</el-button>
      </div>
      <p v-else-if="!hotProducts.length" class="hot-products-message">
        暂无热门商品
      </p>
      <div v-else class="hot-products-grid">
        <article
          v-for="item in hotProducts"
          :key="item.id"
          class="hot-product-card"
          @click="router.push(`/products/${item.id}`)"
        >
          <div
            class="hot-placeholder"
            :class="placeholderClass(item.categoryName)"
          >
            {{ item.categoryName.slice(0, 2) }}
          </div>
          <div class="hot-product-body">
            <span class="category-label">{{ item.categoryName }}</span>
            <h3>{{ item.title }}</h3>
            <strong>{{ formatPrice(item.price) }}</strong>
            <p>{{ item.viewCount }} 浏览 · {{ item.sellerName }}</p>
          </div>
        </article>
      </div>
    </section>
    <div v-if="loading" class="product-grid">
      <el-skeleton v-for="i in 6" :key="i" animated
        ><template #template
          ><el-skeleton-item
            variant="image"
            style="height: 180px" /><el-skeleton-item
            style="width: 70%; margin-top: 12px" /></template
      ></el-skeleton>
    </div>
    <el-result
      v-else-if="errorMessage"
      icon="error"
      title="暂时无法加载商品"
      :sub-title="errorMessage"
      ><template #extra
        ><el-button type="primary" @click="loadProducts()"
          >重新加载</el-button
        ></template
      ></el-result
    >
    <el-empty v-else-if="!records.length" description="没有找到符合条件的商品"
      ><el-button v-if="hasFilters" @click="reset"
        >清除筛选</el-button
      ></el-empty
    >
    <template v-else
      ><div class="product-grid">
        <article
          v-for="item in records"
          :key="item.id"
          class="product-card"
          @click="router.push(`/products/${item.id}`)"
        >
          <img
            v-if="item.imageBase64"
            :src="item.imageBase64"
            :alt="item.title"
          />
          <div
            v-else
            class="image-placeholder"
            :class="placeholderClass(item.categoryName)"
          >
            {{ item.categoryName.slice(0, 2) }}
          </div>
          <div class="product-card-body">
            <span class="category-label">{{ item.categoryName }}</span>
            <h2>{{ item.title }}</h2>
            <strong>{{ formatPrice(item.price) }}</strong>
            <p>
              {{ item.sellerName }} · {{ formatDate(item.createdAt) }} ·
              {{ item.viewCount }} 浏览
            </p>
            <el-button
              v-if="authStore.isAuthenticated && item.status === 'ON_SALE'"
              class="favorite-button"
              size="small"
              :type="item.favorited ? 'info' : 'primary'"
              :plain="item.favorited"
              :loading="operatingId === item.id"
              :disabled="operatingId === item.id"
              @click.stop="toggleFavorite(item)"
            >
              {{ item.favorited ? "取消收藏" : "收藏" }}
            </el-button>
          </div>
        </article>
      </div>
      <el-pagination
        v-if="total > 0"
        class="pagination"
        background
        layout="total, sizes, prev, pager, next"
        :page-sizes="[10, 20, 30, 50]"
        :current-page="page"
        :page-size="pageSize"
        :total="total"
        @size-change="changePageSize"
        @current-change="loadProducts"
    /></template>
  </section>
</template>

<style scoped>
.hot-products-section {
  margin: 30px 0 34px;
}
.hot-products-heading {
  display: flex;
  justify-content: space-between;
  align-items: end;
  gap: 16px;
  margin-bottom: 12px;
}
.hot-eyebrow {
  margin: 0;
  color: #b14f31;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.14em;
}
.hot-products-heading h2 {
  margin: 4px 0 0;
  font-size: 24px;
}
.hot-products-heading > span {
  color: #657066;
  font-size: 13px;
}
.hot-products-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}
.hot-product-card {
  overflow: hidden;
  border: 1px solid #d7d0c1;
  background: #fffdf7;
  cursor: pointer;
  transition: transform 160ms ease, box-shadow 160ms ease;
}
.hot-product-card:hover {
  box-shadow: 4px 5px 0 #d7d0c1;
  transform: translate(-2px, -2px);
}
.hot-placeholder {
  display: grid;
  height: 96px;
  place-items: center;
  color: #365445;
  font-size: 18px;
  font-weight: 800;
}
.hot-product-body {
  padding: 11px;
}
.hot-product-body h3 {
  overflow: hidden;
  margin: 7px 0;
  font-size: 15px;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.hot-product-body strong {
  color: #b14f31;
}
.hot-product-body p {
  margin: 7px 0 0;
  color: #657066;
  font-size: 12px;
}
.hot-products-message {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 62px;
  margin: 0;
  padding: 0 14px;
  border: 1px dashed #d7d0c1;
  color: #657066;
  font-size: 13px;
}
@media (max-width: 920px) {
  .hot-products-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
@media (max-width: 560px) {
  .hot-products-heading {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
