<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { getErrorMessage } from "../../api/http";
import {
  getAdminUsers,
  updateAdminUserStatus,
  type AdminUser,
  type AdminUserQuery,
  type UserRole,
  type UserStatus,
} from "../../api/admin-user";
import { useAuthStore } from "../../stores/auth";

const authStore = useAuthStore();
const records = ref<AdminUser[]>([]);
const loading = ref(false);
const errorMessage = ref("");
const operatingId = ref<number>();
const page = ref(1);
const pageSize = ref(12);
const total = ref(0);
const filters = reactive<{
  username: string;
  email: string;
  role?: UserRole;
  status?: UserStatus;
}>({
  username: "",
  email: "",
});

const hasFilters = computed(
  () =>
    Boolean(filters.username.trim()) ||
    Boolean(filters.email.trim()) ||
    filters.role !== undefined ||
    filters.status !== undefined,
);

function buildQuery(): AdminUserQuery {
  const username = filters.username.trim();
  const email = filters.email.trim();

  return {
    page: page.value,
    pageSize: pageSize.value,
    ...(username ? { username } : {}),
    ...(email ? { email } : {}),
    ...(filters.role !== undefined ? { role: filters.role } : {}),
    ...(filters.status !== undefined ? { status: filters.status } : {}),
  };
}

async function load() {
  loading.value = true;
  errorMessage.value = "";

  try {
    const response = await getAdminUsers(buildQuery());
    records.value = response.data.data.records;
    total.value = response.data.data.total;
  } catch (error) {
    errorMessage.value = getErrorMessage(error, "用户加载失败");
  } finally {
    loading.value = false;
  }
}

async function search() {
  page.value = 1;
  await load();
}

async function resetFilters() {
  filters.username = "";
  filters.email = "";
  filters.role = undefined;
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

function roleLabel(role: UserRole) {
  return role === "ADMIN" ? "管理员" : "普通用户";
}

function statusLabel(status: UserStatus) {
  return status === 1 ? "启用" : "已禁用";
}

function statusType(status: UserStatus) {
  return status === 1 ? "success" : "info";
}

function isCurrentAdmin(user: AdminUser) {
  return authStore.user?.id === user.id;
}

async function toggleStatus(user: AdminUser) {
  const targetStatus: UserStatus = user.status === 1 ? 0 : 1;
  const isDisabling = targetStatus === 0;

  try {
    await ElMessageBox.confirm(
      isDisabling
        ? `禁用“${user.username}”后，该用户将立即无法登录，已签发的 Token 也会立即失效。确定继续吗？`
        : `确定重新启用“${user.username}”吗？用户需要重新登录后才能继续使用平台。`,
      isDisabling ? "确认禁用用户" : "确认启用用户",
      {
        type: "warning",
        confirmButtonText: "确认",
        cancelButtonText: "取消",
      },
    );

    operatingId.value = user.id;
    await updateAdminUserStatus(user.id, targetStatus);
    ElMessage.success(isDisabling ? "用户已禁用" : "用户已启用");
    await load();
  } catch (error) {
    if (error !== "cancel" && error !== "close") {
      ElMessage.error(getErrorMessage(error, "更新用户状态失败"));
      await load();
    }
  } finally {
    operatingId.value = undefined;
  }
}

onMounted(load);
</script>

<template>
  <section class="user-manage-page">
    <header>
      <div>
        <p class="eyebrow">ADMIN CONSOLE</p>
        <h1>用户管理</h1>
        <p>查看账户状态，并安全地启用或禁用用户。</p>
      </div>
      <p class="record-count">共 {{ total }} 位用户</p>
    </header>

    <el-form class="filter-panel" @submit.prevent="search">
      <el-input
        v-model="filters.username"
        clearable
        placeholder="用户名关键词"
        @keyup.enter="search"
      />
      <el-input
        v-model="filters.email"
        clearable
        placeholder="邮箱关键词"
        @keyup.enter="search"
      />
      <el-select v-model="filters.role" clearable placeholder="全部角色">
        <el-option label="普通用户" value="USER" />
        <el-option label="管理员" value="ADMIN" />
      </el-select>
      <el-select v-model="filters.status" clearable placeholder="全部状态">
        <el-option label="启用" :value="1" />
        <el-option label="已禁用" :value="0" />
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
      :description="hasFilters ? '没有符合条件的用户' : '暂无用户'"
    >
      <el-button v-if="hasFilters" type="primary" @click="resetFilters">
        清除筛选
      </el-button>
    </el-empty>
    <div v-else class="table-shell">
      <el-table :data="records" stripe>
        <el-table-column prop="username" label="用户名" min-width="130" />
        <el-table-column prop="email" label="邮箱" min-width="210" />
        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'warning' : 'primary'">
              {{ roleLabel(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" min-width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              text
              :type="row.status === 1 ? 'danger' : 'success'"
              :loading="operatingId === row.id"
              :disabled="operatingId === row.id || (isCurrentAdmin(row) && row.status === 1)"
              :title="isCurrentAdmin(row) && row.status === 1 ? '不能禁用自己' : undefined"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? "禁用" : "启用" }}
            </el-button>
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
.user-manage-page {
  max-width: 1180px;
  margin: auto;
}
.user-manage-page > header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 24px;
  margin-bottom: 24px;
}
.user-manage-page h1 {
  margin: 0;
  font-size: clamp(28px, 4vw, 46px);
  line-height: 1.15;
}
.user-manage-page > header p:not(.eyebrow) {
  color: #657066;
}
.record-count {
  margin: 0;
  color: #657066;
  font-size: 14px;
  white-space: nowrap;
}
.filter-panel {
  display: grid;
  grid-template-columns: minmax(150px, 1.25fr) minmax(190px, 1.45fr) minmax(120px, 0.8fr) minmax(120px, 0.8fr) auto;
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
@media (max-width: 960px) {
  .filter-panel {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .filter-actions {
    grid-column: span 2;
  }
}
@media (max-width: 760px) {
  .user-manage-page > header {
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
