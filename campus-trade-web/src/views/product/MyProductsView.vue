<script setup lang="ts">
import { onMounted, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { useRouter } from "vue-router";
import { getErrorMessage } from "../../api/http";
import {
  getMyProducts,
  offShelfProduct,
  type ProductCard,
  type ProductStatus,
} from "../../api/product";
const router = useRouter();
const records = ref<ProductCard[]>([]);
const keyword = ref("");
const status = ref<ProductStatus>();
const page = ref(1);
const pageSize = ref(10);
const total = ref(0);
const loading = ref(false);
const errorMessage = ref("");
const operatingId = ref<number>();
const labels: Record<ProductStatus, string> = {
  ON_SALE: "在售",
  LOCKED: "已锁定",
  SOLD: "已售出",
  OFF_SHELF: "已下架",
};
async function load(nextPage = page.value) {
  loading.value = true;
  errorMessage.value = "";
  try {
    const data = (
      await getMyProducts({
        page: nextPage,
        pageSize: pageSize.value,
        ...(keyword.value.trim() ? { keyword: keyword.value.trim() } : {}),
        ...(status.value ? { status: status.value } : {}),
      })
    ).data.data;
    records.value = data.records;
    total.value = data.total;
    page.value = data.page;
  } catch (error) {
    errorMessage.value = getErrorMessage(error, "我的发布加载失败");
  } finally {
    loading.value = false;
  }
}
function changePageSize(size: number) {
  pageSize.value = size;
  load(1);
}
async function offShelf(item: ProductCard) {
  try {
    await ElMessageBox.confirm(
      `确定下架“${item.title}”吗？下架后不能重新上架。`,
      "确认下架",
      {
        type: "warning",
        confirmButtonText: "确认下架",
        cancelButtonText: "取消",
      },
    );
    operatingId.value = item.id;
    await offShelfProduct(item.id);
    ElMessage.success("商品已下架");
    load();
  } catch (error) {
    if (error !== "cancel" && error !== "close")
      ElMessage.error(getErrorMessage(error, "下架失败"));
  } finally {
    operatingId.value = undefined;
  }
}
onMounted(() => load(1));
</script>
<template>
  <section class="my-products-page">
    <header>
      <p class="eyebrow">MY LISTINGS</p>
      <h1>我的发布</h1>
      <p>管理你发布过的每一件闲置。</p>
    </header>
    <div class="my-filter">
      <el-input
        v-model="keyword"
        clearable
        placeholder="搜索我的商品"
        @keyup.enter="load(1)"
      /><el-select
        v-model="status"
        clearable
        placeholder="全部状态"
        @change="load(1)"
        ><el-option
          v-for="(label, value) in labels"
          :key="value"
          :label="label"
          :value="value" /></el-select
      ><el-button @click="load(1)">搜索</el-button>
    </div>
    <el-skeleton v-if="loading" animated :rows="8" /><el-result
      v-else-if="errorMessage"
      icon="error"
      title="加载失败"
      :sub-title="errorMessage"
      ><template #extra
        ><el-button @click="load()">重试</el-button></template
      ></el-result
    ><el-empty v-else-if="!records.length" description="还没有发布任何商品"
      ><el-button type="primary" @click="router.push('/products/new')"
        >去发布商品</el-button
      ></el-empty
    >
    <div v-else class="my-list">
      <article v-for="item in records" :key="item.id">
        <img
          v-if="item.imageBase64"
          :src="item.imageBase64"
          :alt="item.title"
        />
        <div v-else class="image-placeholder">
          {{ item.categoryName.slice(0, 2) }}
        </div>
        <div class="my-info">
          <h2>{{ item.title }}</h2>
          <p>
            {{ item.categoryName }} · ¥ {{ Number(item.price).toFixed(2) }} ·
            {{ item.viewCount }} 浏览
          </p>
        </div>
        <el-tag :type="item.status === 'ON_SALE' ? 'success' : 'info'">{{
          labels[item.status]
        }}</el-tag>
        <div v-if="item.status === 'ON_SALE'" class="my-actions">
          <el-button @click="router.push(`/products/${item.id}/edit`)"
            >编辑</el-button
          ><el-button
            type="danger"
            plain
            :loading="operatingId === item.id"
            @click="offShelf(item)"
            >下架</el-button
          >
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
      @current-change="load"
    />
  </section>
</template>
