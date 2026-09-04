<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { getErrorMessage } from "../../api/http";
import {
  getAdminOrders,
  type AdminOrderQuery,
} from "../../api/admin-order";
import type { OrderItem, OrderStatus } from "../../api/order";

const records = ref<OrderItem[]>([]);
const loading = ref(false);
const errorMessage = ref("");
const page = ref(1);
const pageSize = ref(12);
const total = ref(0);
const filters = reactive<{
  orderNo: string;
  status?: OrderStatus;
  buyerId?: number;
  sellerId?: number;
}>({
  orderNo: "",
});

const statusOptions: Array<{ label: string; value: OrderStatus }> = [
  { label: "待付款", value: "PENDING_PAYMENT" },
  { label: "已取消", value: "CANCELLED" },
  { label: "已付款", value: "PAID" },
  { label: "已完成", value: "COMPLETED" },
];

const hasFilters = computed(
  () =>
    Boolean(filters.orderNo.trim()) ||
    filters.status !== undefined ||
    filters.buyerId !== undefined ||
    filters.sellerId !== undefined,
);

function positiveInteger(value: number | undefined) {
  return value !== undefined && Number.isInteger(value) && value > 0
    ? value
    : undefined;
}

function buildQuery(): AdminOrderQuery {
  const orderNo = filters.orderNo.trim();
  const buyerId = positiveInteger(filters.buyerId);
  const sellerId = positiveInteger(filters.sellerId);

  return {
    page: page.value,
    pageSize: pageSize.value,
    ...(orderNo ? { orderNo } : {}),
    ...(filters.status ? { status: filters.status } : {}),
    ...(buyerId ? { buyerId } : {}),
    ...(sellerId ? { sellerId } : {}),
  };
}

async function load() {
  loading.value = true;
  errorMessage.value = "";

  try {
    const response = await getAdminOrders(buildQuery());
    records.value = response.data.data.records;
    total.value = response.data.data.total;
    page.value = response.data.data.page;
  } catch (error) {
    errorMessage.value = getErrorMessage(error, "订单加载失败");
  } finally {
    loading.value = false;
  }
}

async function search() {
  page.value = 1;
  await load();
}

async function resetFilters() {
  filters.orderNo = "";
  filters.status = undefined;
  filters.buyerId = undefined;
  filters.sellerId = undefined;
  page.value = 1;
  await load();
}

async function handlePageChange(nextPage: number) {
  page.value = nextPage;
  await load();
}

function formatDate(value: string | null) {
  return value ? new Date(value).toLocaleString("zh-CN") : "—";
}

function formatPrice(amount: number) {
  return `¥ ${Number(amount).toFixed(2)}`;
}

function statusLabel(status: OrderStatus) {
  return statusOptions.find((option) => option.value === status)?.label ?? status;
}

function statusType(status: OrderStatus) {
  if (status === "PENDING_PAYMENT") return "warning";
  if (status === "PAID") return "primary";
  if (status === "COMPLETED") return "success";
  return "info";
}

onMounted(load);
</script>

<template>
  <section class="order-manage-page">
    <header>
      <div>
        <p class="eyebrow">ADMIN CONSOLE</p>
        <h1>订单管理</h1>
        <p>查看交易进度，不干预买卖双方的订单流程。</p>
      </div>
      <p class="record-count">共 {{ total }} 笔订单 · 只读</p>
    </header>

    <el-form class="filter-panel" @submit.prevent="search">
      <el-input
        v-model="filters.orderNo"
        clearable
        placeholder="精确订单号"
        @keyup.enter="search"
      />
      <el-select v-model="filters.status" clearable placeholder="全部状态">
        <el-option
          v-for="option in statusOptions"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>
      <el-input-number
        v-model="filters.buyerId"
        :min="1"
        :precision="0"
        controls-position="right"
        placeholder="买家 ID"
      />
      <el-input-number
        v-model="filters.sellerId"
        :min="1"
        :precision="0"
        controls-position="right"
        placeholder="卖家 ID"
      />
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
      :description="hasFilters ? '没有符合条件的订单' : '暂无订单'"
    >
      <el-button v-if="hasFilters" type="primary" @click="resetFilters">
        清除筛选
      </el-button>
    </el-empty>
    <div v-else class="table-shell">
      <el-table :data="records" stripe>
        <el-table-column label="订单 / 商品" min-width="250">
          <template #default="{ row }">
            <div class="order-product">
              <img
                v-if="row.productImageBase64"
                :src="row.productImageBase64"
                :alt="row.productTitle"
              />
              <div v-else class="image-placeholder">
                {{ row.productTitle.slice(0, 2) }}
              </div>
              <div>
                <strong>{{ row.orderNo }}</strong>
                <small>{{ row.productTitle }}</small>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="买家" min-width="130">
          <template #default="{ row }">
            <div>{{ row.buyerName }}</div>
            <small class="muted">ID {{ row.buyerId }}</small>
          </template>
        </el-table-column>
        <el-table-column label="卖家" min-width="130">
          <template #default="{ row }">
            <div>{{ row.sellerName }}</div>
            <small class="muted">ID {{ row.sellerId }}</small>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="110">
          <template #default="{ row }">
            <strong class="amount">{{ formatPrice(row.amount) }}</strong>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="下单时间" min-width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="记录" width="84" fixed="right">
          <template #default>
            <span class="readonly">只读</span>
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
.order-manage-page {
  max-width: 1260px;
  margin: auto;
}
.order-manage-page > header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 24px;
  margin-bottom: 24px;
}
.order-manage-page h1 {
  margin: 0;
  font-size: clamp(28px, 4vw, 46px);
  line-height: 1.15;
}
.order-manage-page > header p:not(.eyebrow),
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
  grid-template-columns: minmax(200px, 1.4fr) minmax(130px, 0.85fr) minmax(120px, 0.75fr) minmax(120px, 0.75fr) auto;
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
.order-product {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}
.order-product img,
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
.order-product strong,
.order-product small {
  display: block;
}
.order-product small {
  margin-top: 4px;
  color: #657066;
}
.amount {
  color: #b14f31;
}
.readonly {
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
  .order-manage-page > header {
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
