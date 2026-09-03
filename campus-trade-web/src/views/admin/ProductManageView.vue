<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { getErrorMessage } from "../../api/http";
import {
  getAdminProducts,
  offShelfAdminProduct,
  type AdminProduct,
  type AdminProductQuery,
} from "../../api/admin-product";
import type { ProductStatus } from "../../api/product";

const records = ref<AdminProduct[]>([]);
const loading = ref(false);
const errorMessage = ref("");
const operatingId = ref<number>();
const page = ref(1);
const pageSize = ref(12);
const total = ref(0);
const filters = reactive<{
  keyword: string;
  sellerId?: number;
  categoryId?: number;
  status?: ProductStatus;
}>({
  keyword: "",
});

const statusOptions: Array<{ label: string; value: ProductStatus }> = [
  { label: "在售", value: "ON_SALE" },
  { label: "已锁定", value: "LOCKED" },
  { label: "已售出", value: "SOLD" },
  { label: "已下架", value: "OFF_SHELF" },
];

const hasFilters = computed(
  () =>
    Boolean(filters.keyword.trim()) ||
    filters.sellerId !== undefined ||
    filters.categoryId !== undefined ||
    filters.status !== undefined,
);

function positiveInteger(value: number | undefined) {
  return value !== undefined && Number.isInteger(value) && value > 0
    ? value
    : undefined;
}

function buildQuery(): AdminProductQuery {
  const keyword = filters.keyword.trim();
  const sellerId = positiveInteger(filters.sellerId);
  const categoryId = positiveInteger(filters.categoryId);

  return {
    page: page.value,
    pageSize: pageSize.value,
    ...(keyword ? { keyword } : {}),
    ...(sellerId ? { sellerId } : {}),
    ...(categoryId ? { categoryId } : {}),
    ...(filters.status ? { status: filters.status } : {}),
  };
}

async function load() {
  loading.value = true;
  errorMessage.value = "";

  try {
    const response = await getAdminProducts(buildQuery());
    records.value = response.data.data.records;
    total.value = response.data.data.total;
    page.value = response.data.data.page;
  } catch (error) {
    errorMessage.value = getErrorMessage(error, "商品加载失败");
  } finally {
    loading.value = false;
  }
}

async function search() {
  page.value = 1;
  await load();
}

async function resetFilters() {
  filters.keyword = "";
  filters.sellerId = undefined;
  filters.categoryId = undefined;
  filters.status = undefined;
  page.value = 1;
  await load();
}

async function handlePageChange(nextPage: number) {
  page.value = nextPage;
  await load();
}

function formatDate(value: string) {
  return new Date(value).toLocaleString("zh-CN");
}

function statusLabel(status: ProductStatus) {
  return statusOptions.find((option) => option.value === status)?.label ?? status;
}

function statusType(status: ProductStatus) {
  if (status === "ON_SALE") return "success";
  if (status === "LOCKED") return "warning";
  return "info";
}

async function offShelf(product: AdminProduct) {
  try {
    await ElMessageBox.confirm(
      `下架“${product.title}”后不能重新上架，且不会修改已经存在的订单。确定继续吗？`,
      "确认下架商品",
      {
        type: "warning",
        confirmButtonText: "确认下架",
        cancelButtonText: "取消",
      },
    );

    operatingId.value = product.id;
    await offShelfAdminProduct(product.id);
    ElMessage.success("商品已下架");
    await load();
  } catch (error) {
    if (error !== "cancel" && error !== "close") {
      ElMessage.error(getErrorMessage(error, "下架商品失败"));
      await load();
    }
  } finally {
    operatingId.value = undefined;
  }
}

onMounted(load);
</script>

<template>
  <section class="product-manage-page">
    <header>
      <div>
        <p class="eyebrow">ADMIN CONSOLE</p>
        <h1>商品管理</h1>
        <p>跨卖家查看商品状态，保护进行中的交易。</p>
      </div>
      <p class="record-count">共 {{ total }} 件商品</p>
    </header>

    <el-form class="filter-panel" @submit.prevent="search">
      <el-input
        v-model="filters.keyword"
        clearable
        placeholder="标题或描述关键词"
        @keyup.enter="search"
      />
      <el-input-number
        v-model="filters.sellerId"
        :min="1"
        :precision="0"
        controls-position="right"
        placeholder="卖家 ID"
      />
      <el-input-number
        v-model="filters.categoryId"
        :min="1"
        :precision="0"
        controls-position="right"
        placeholder="分类 ID"
      />
      <el-select v-model="filters.status" clearable placeholder="全部状态">
        <el-option
          v-for="option in statusOptions"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>
      <div class="filter-actions">
        <el-button v-if="hasFilters" text @click="resetFilters">重置</el-button>
        <el-button type="primary" native-type="submit">查询</el-button>
      </div>
    </el-form>

    <el-skeleton v-if="loading" animated :rows="9" />
    <el-result
      v-else-if="errorMessage"
      icon="error"
      title="加载失败"
      :sub-title="errorMessage"
    >
      <template #extra>
        <el-button type="primary" @click="load">重新加载</el-button>
      </template>
    </el-result>
    <el-empty
      v-else-if="!records.length"
      :description="hasFilters ? '没有符合条件的商品' : '暂无商品'"
    >
      <el-button v-if="hasFilters" type="primary" @click="resetFilters">
        清除筛选
      </el-button>
    </el-empty>
    <div v-else class="table-shell">
      <el-table :data="records" stripe>
        <el-table-column label="商品" min-width="260">
          <template #default="{ row }">
            <div class="product-cell">
              <img v-if="row.imageBase64" :src="row.imageBase64" :alt="row.title" />
              <div v-else class="image-placeholder">
                {{ row.categoryName.slice(0, 2) }}
              </div>
              <div>
                <strong>{{ row.title }}</strong>
                <small>¥ {{ Number(row.price).toFixed(2) }} · {{ row.viewCount }} 浏览</small>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="卖家" min-width="130">
          <template #default="{ row }">
            <div>{{ row.sellerName }}</div>
            <small class="muted">ID {{ row.sellerId }}</small>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" min-width="120" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" min-width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'ON_SALE'"
              text
              type="danger"
              :loading="operatingId === row.id"
              :disabled="operatingId === row.id"
              @click="offShelf(row)"
            >
              下架
            </el-button>
            <span v-else class="unavailable">不可下架</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <span>第 {{ page }} 页</span>
        <el-pagination
          background
          layout="prev, pager, next"
          :current-page="page"
          :page-size="pageSize"
          :total="total"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </section>
</template>

<style scoped>
.product-manage-page {
  max-width: 1240px;
  margin: auto;
}
.product-manage-page > header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 24px;
  margin-bottom: 24px;
}
.product-manage-page h1 {
  margin: 0;
  font-size: clamp(28px, 4vw, 46px);
  line-height: 1.15;
}
.product-manage-page > header p:not(.eyebrow),
.record-count,
.muted {
  color: #657066;
}
.record-count {
  margin: 0;
  font-size: 14px;
  white-space: nowrap;
}
.filter-panel {
  display: grid;
  grid-template-columns: minmax(180px, 1.35fr) minmax(120px, 0.75fr) minmax(120px, 0.75fr) minmax(130px, 0.85fr) auto;
  gap: 10px;
  align-items: center;
  margin-bottom: 18px;
  padding: 14px;
  border: 1px solid #d7d0c1;
  background: #fffdf7;
}
.filter-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
}
.table-shell {
  overflow: hidden;
  border: 1px solid #d7d0c1;
  background: #fffdf7;
}
.table-shell :deep(.el-table) {
  --el-table-header-bg-color: #eef2e8;
  --el-table-row-hover-bg-color: #f7f4ec;
  --el-table-border-color: #d7d0c1;
  --el-table-text-color: #26362d;
  --el-table-header-text-color: #365445;
}
.product-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}
.product-cell img,
.image-placeholder {
  flex: 0 0 auto;
  width: 42px;
  height: 42px;
  border: 1px solid #d7d0c1;
  object-fit: cover;
}
.image-placeholder {
  display: grid;
  place-items: center;
  background: #eef2e8;
  color: #365445;
  font-size: 12px;
  font-weight: 700;
}
.product-cell strong,
.product-cell small {
  display: block;
}
.product-cell small {
  margin-top: 4px;
  color: #657066;
}
.unavailable {
  color: #8a9187;
  font-size: 13px;
}
.pagination-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 14px 16px;
  border-top: 1px solid #d7d0c1;
  color: #657066;
  font-size: 13px;
}
@media (max-width: 1040px) {
  .filter-panel {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .filter-actions {
    grid-column: span 2;
  }
}
@media (max-width: 760px) {
  .product-manage-page > header {
    align-items: flex-start;
    flex-direction: column;
  }
  .filter-panel {
    grid-template-columns: 1fr;
  }
  .filter-actions {
    grid-column: auto;
  }
  .table-shell {
    overflow-x: auto;
  }
  .pagination-bar {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
