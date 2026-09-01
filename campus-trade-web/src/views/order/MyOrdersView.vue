<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { useRoute, useRouter } from "vue-router";
import { getErrorMessage } from "../../api/http";
import {
  cancelOrder,
  completeOrder,
  getBuyingOrders,
  getSellingOrders,
  payOrder,
  type OrderItem,
  type OrderStatus,
} from "../../api/order";

type OrderTab = "buying" | "selling";
type OrderAction = "cancel" | "pay" | "complete";

const route = useRoute();
const router = useRouter();
const records = ref<OrderItem[]>([]);
const activeTab = ref<OrderTab>(readTab(route.query.tab));
const page = ref(1);
const pageSize = ref(12);
const total = ref(0);
const loading = ref(false);
const errorMessage = ref("");
const operatingId = ref<number>();

const statusLabels: Record<OrderStatus, string> = {
  PENDING_PAYMENT: "待付款",
  CANCELLED: "已取消",
  PAID: "已付款",
  COMPLETED: "已完成",
};

const actions: Record<
  OrderAction,
  {
    title: string;
    message: string;
    successMessage: string;
    failureMessage: string;
    request: (orderId: number) => Promise<unknown>;
  }
> = {
  cancel: {
    title: "确认取消订单",
    message: "取消后商品会恢复为在售状态，确定继续吗？",
    successMessage: "订单已取消",
    failureMessage: "取消订单失败",
    request: cancelOrder,
  },
  pay: {
    title: "确认模拟付款",
    message: "这会将商品标记为已售出，确定继续吗？",
    successMessage: "付款成功",
    failureMessage: "付款失败",
    request: payOrder,
  },
  complete: {
    title: "确认完成交易",
    message: "确认完成后订单不能再修改，确定继续吗？",
    successMessage: "订单已确认完成",
    failureMessage: "确认完成失败",
    request: completeOrder,
  },
};

function readTab(tab: unknown): OrderTab {
  return tab === "selling" ? "selling" : "buying";
}

function statusType(status: OrderStatus) {
  if (status === "PENDING_PAYMENT") return "warning";
  if (status === "PAID") return "primary";
  if (status === "COMPLETED") return "success";
  return "info";
}

function formatPrice(amount: number) {
  return `¥ ${Number(amount).toFixed(2)}`;
}

function formatDate(value: string | null) {
  return value ? new Date(value).toLocaleString("zh-CN") : "—";
}

async function load(nextPage = page.value) {
  loading.value = true;
  errorMessage.value = "";
  try {
    const request =
      activeTab.value === "buying" ? getBuyingOrders : getSellingOrders;
    const data = (
      await request({ page: nextPage, pageSize: pageSize.value })
    ).data.data;
    records.value = data.records;
    page.value = data.page;
    total.value = data.total;
  } catch (error) {
    errorMessage.value = getErrorMessage(error, "订单加载失败");
  } finally {
    loading.value = false;
  }
}

async function changeTab(tab: string | number) {
  const nextTab = readTab(tab);
  if (route.query.tab !== nextTab) {
    await router.replace({ query: { ...route.query, tab: nextTab } });
  }
  await load(1);
}

function changePageSize(size: number) {
  pageSize.value = size;
  void load(1);
}

async function runOrderAction(item: OrderItem, action: OrderAction) {
  const config = actions[action];
  try {
    await ElMessageBox.confirm(config.message, config.title, {
      type: "warning",
      confirmButtonText: "确认",
      cancelButtonText: "取消",
    });
    operatingId.value = item.id;
    await config.request(item.id);
    ElMessage.success(config.successMessage);

    if (records.value.length === 1 && page.value > 1) {
      await load(page.value - 1);
    } else {
      await load();
    }
  } catch (error) {
    if (error !== "cancel" && error !== "close") {
      ElMessage.error(getErrorMessage(error, config.failureMessage));
      await load();
    }
  } finally {
    operatingId.value = undefined;
  }
}

watch(
  () => route.query.tab,
  (tab) => {
    const nextTab = readTab(tab);
    if (activeTab.value !== nextTab) {
      activeTab.value = nextTab;
      void load(1);
    }
  },
);

onMounted(() => load(1));
</script>

<template>
  <section class="orders-page">
    <header>
      <p class="eyebrow">MY ORDERS</p>
      <h1>我的订单</h1>
      <p>每一次交换，都有清晰的记录。</p>
    </header>

    <el-tabs v-model="activeTab" class="order-tabs" @tab-change="changeTab">
      <el-tab-pane label="我买到的" name="buying" />
      <el-tab-pane label="我卖出的" name="selling" />
    </el-tabs>

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
    <el-empty
      v-else-if="!records.length"
      :description="
        activeTab === 'buying' ? '还没有买入任何商品' : '还没有卖出任何商品'
      "
    />
    <template v-else>
      <div class="order-list">
        <article v-for="item in records" :key="item.id">
          <img
            v-if="item.productImageBase64"
            :src="item.productImageBase64"
            :alt="item.productTitle"
          />
          <div v-else class="image-placeholder">
            {{ item.productTitle.slice(0, 2) }}
          </div>

          <div class="order-info">
            <div class="order-title-row">
              <h2>{{ item.productTitle }}</h2>
              <el-tag :type="statusType(item.status)">
                {{ statusLabels[item.status] }}
              </el-tag>
            </div>
            <strong>{{ formatPrice(item.amount) }}</strong>
            <p>
              订单号 {{ item.orderNo }} ·
              {{ activeTab === "buying" ? `卖家：${item.sellerName}` : `买家：${item.buyerName}` }}
            </p>
            <p>下单时间：{{ formatDate(item.createdAt) }}</p>
          </div>

          <div class="order-actions">
            <template
              v-if="activeTab === 'buying' && item.status === 'PENDING_PAYMENT'"
            >
              <el-button
                :loading="operatingId === item.id"
                :disabled="operatingId === item.id"
                @click="runOrderAction(item, 'cancel')"
              >
                取消订单
              </el-button>
              <el-button
                type="primary"
                :loading="operatingId === item.id"
                :disabled="operatingId === item.id"
                @click="runOrderAction(item, 'pay')"
              >
                模拟付款
              </el-button>
            </template>
            <el-button
              v-else-if="activeTab === 'buying' && item.status === 'PAID'"
              type="primary"
              :loading="operatingId === item.id"
              :disabled="operatingId === item.id"
              @click="runOrderAction(item, 'complete')"
            >
              确认完成
            </el-button>
          </div>
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

<style scoped>
.orders-page {
  max-width: 920px;
  margin: auto;
}
.orders-page > header {
  margin-bottom: 22px;
}
.orders-page h1 {
  font-size: clamp(28px, 4vw, 46px);
  line-height: 1.15;
  margin: 0;
}
.orders-page > header p:not(.eyebrow) {
  color: #657066;
}
.order-tabs {
  margin-bottom: 18px;
}
.order-list {
  display: grid;
  gap: 12px;
}
.order-list article {
  background: #fffdf7;
  border: 1px solid #d7d0c1;
  padding: 12px;
  display: grid;
  grid-template-columns: 108px 1fr auto;
  gap: 16px;
  align-items: center;
}
.order-list img,
.order-list .image-placeholder {
  width: 108px;
  height: 82px;
  object-fit: cover;
}
.order-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}
.order-info h2 {
  margin: 0;
  font-size: 18px;
}
.order-info strong {
  color: #e65730;
  display: block;
  margin-top: 7px;
}
.order-info p {
  margin: 7px 0 0;
  color: #68736b;
  font-family: system-ui;
  font-size: 13px;
}
.order-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}
@media (max-width: 760px) {
  .order-list article {
    grid-template-columns: 78px 1fr;
    gap: 10px;
  }
  .order-list img,
  .order-list .image-placeholder {
    width: 78px;
    height: 62px;
  }
  .order-actions {
    grid-column: 2;
    justify-content: flex-start;
  }
}
</style>
