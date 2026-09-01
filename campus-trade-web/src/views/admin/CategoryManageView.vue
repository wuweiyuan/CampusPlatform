<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import {
  ElMessage,
  ElMessageBox,
  type FormInstance,
  type FormRules,
} from "element-plus";
import { getErrorMessage } from "../../api/http";
import {
  createAdminCategory,
  getAdminCategories,
  updateAdminCategory,
  updateAdminCategoryStatus,
  type AdminCategory,
  type CategoryStatus,
} from "../../api/admin-category";

type DialogMode = "create" | "edit";

const records = ref<AdminCategory[]>([]);
const loading = ref(false);
const errorMessage = ref("");
const operatingId = ref<number>();
const dialogVisible = ref(false);
const dialogMode = ref<DialogMode>("create");
const editingId = ref<number>();
const saving = ref(false);
const formRef = ref<FormInstance>();
const form = reactive({
  name: "",
  sort: undefined as number | undefined,
});

const rules: FormRules = {
  name: [
    { required: true, message: "请输入分类名称", trigger: "blur" },
    { max: 30, message: "分类名称不能超过 30 个字符", trigger: "blur" },
    {
      validator: (_, value, callback) => {
        if (!String(value ?? "").trim()) {
          callback(new Error("分类名称不能为空"));
          return;
        }
        callback();
      },
      trigger: "blur",
    },
  ],
  sort: [
    {
      required: true,
      type: "number",
      message: "请输入排序值",
      trigger: "change",
    },
  ],
};

function statusLabel(status: CategoryStatus) {
  return status === "ENABLED" ? "启用" : "已停用";
}

function statusType(status: CategoryStatus) {
  return status === "ENABLED" ? "success" : "info";
}

function formatDate(value: string) {
  return new Date(value).toLocaleString("zh-CN");
}

async function load() {
  loading.value = true;
  errorMessage.value = "";
  try {
    records.value = (await getAdminCategories()).data.data;
  } catch (error) {
    errorMessage.value = getErrorMessage(error, "分类加载失败");
  } finally {
    loading.value = false;
  }
}

function resetForm() {
  form.name = "";
  form.sort = undefined;
  formRef.value?.clearValidate();
}

function openCreateDialog() {
  dialogMode.value = "create";
  editingId.value = undefined;
  resetForm();
  dialogVisible.value = true;
}

function openEditDialog(category: AdminCategory) {
  dialogMode.value = "edit";
  editingId.value = category.id;
  form.name = category.name;
  form.sort = category.sort;
  formRef.value?.clearValidate();
  dialogVisible.value = true;
}

async function submit() {
  const valid = await formRef.value?.validate();
  if (!valid || form.sort === undefined) return;

  const payload = { name: form.name.trim(), sort: form.sort };
  saving.value = true;
  try {
    if (dialogMode.value === "create") {
      await createAdminCategory(payload);
      ElMessage.success("分类已新增");
    } else if (editingId.value !== undefined) {
      await updateAdminCategory(editingId.value, payload);
      ElMessage.success("分类已更新");
    }
    dialogVisible.value = false;
    await load();
  } catch (error) {
    ElMessage.error(getErrorMessage(error, "保存分类失败"));
  } finally {
    saving.value = false;
  }
}

async function toggleStatus(category: AdminCategory) {
  const targetStatus: CategoryStatus =
    category.status === "ENABLED" ? "DISABLED" : "ENABLED";
  const isDisabling = targetStatus === "DISABLED";

  try {
    await ElMessageBox.confirm(
      isDisabling
        ? `停用“${category.name}”后，用户不能再用它发布或编辑商品，确定继续吗？`
        : `确定启用“${category.name}”吗？`,
      isDisabling ? "确认停用分类" : "确认启用分类",
      {
        type: "warning",
        confirmButtonText: "确认",
        cancelButtonText: "取消",
      },
    );
    operatingId.value = category.id;
    await updateAdminCategoryStatus(category.id, targetStatus);
    ElMessage.success(isDisabling ? "分类已停用" : "分类已启用");
    await load();
  } catch (error) {
    if (error !== "cancel" && error !== "close") {
      ElMessage.error(getErrorMessage(error, "更新分类状态失败"));
      await load();
    }
  } finally {
    operatingId.value = undefined;
  }
}

onMounted(load);
</script>

<template>
  <section class="category-manage-page">
    <header>
      <div>
        <p class="eyebrow">ADMIN CONSOLE</p>
        <h1>分类管理</h1>
        <p>维护校园市集的商品分类与展示顺序。</p>
      </div>
      <el-button type="primary" @click="openCreateDialog">新增分类</el-button>
    </header>

    <el-skeleton v-if="loading" animated :rows="8" />
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
    <el-empty v-else-if="!records.length" description="暂无分类">
      <el-button type="primary" @click="openCreateDialog">新增第一个分类</el-button>
    </el-empty>
    <div v-else class="table-shell">
      <el-table :data="records" stripe>
        <el-table-column prop="name" label="分类名称" min-width="180" />
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" min-width="180">
          <template #default="{ row }">
            {{ formatDate(row.updatedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openEditDialog(row)">
              编辑
            </el-button>
            <el-button
              text
              :type="row.status === 'ENABLED' ? 'danger' : 'success'"
              :loading="operatingId === row.id"
              :disabled="operatingId === row.id"
              @click="toggleStatus(row)"
            >
              {{ row.status === "ENABLED" ? "停用" : "启用" }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增分类' : '编辑分类'"
      width="min(460px, calc(100vw - 32px))"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="108px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" maxlength="30" show-word-limit />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :precision="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="saving" @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">
          保存
        </el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.category-manage-page {
  max-width: 1080px;
  margin: auto;
}
.category-manage-page > header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 24px;
  margin-bottom: 28px;
}
.category-manage-page h1 {
  margin: 0;
  font-size: clamp(28px, 4vw, 46px);
  line-height: 1.15;
}
.category-manage-page > header p:not(.eyebrow) {
  color: #657066;
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
@media (max-width: 760px) {
  .category-manage-page > header {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
