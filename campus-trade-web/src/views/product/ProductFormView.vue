<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import type { FormInstance, FormRules, UploadRawFile } from "element-plus";
import { useRoute, useRouter } from "vue-router";
import { getCategories, type Category } from "../../api/category";
import { createProduct, getProduct, updateProduct } from "../../api/product";
import { getErrorMessage } from "../../api/http";
import {
  fileToDataUrl,
  validateImageFile,
  validatePrice,
} from "../../utils/product";
const route = useRoute();
const router = useRouter();
const formRef = ref<FormInstance>();
const categories = ref<Category[]>([]);
const submitting = ref(false);
const loading = ref(false);
const editing = computed(() => route.name === "product-edit");
const imagePreview = ref<string | null>(null);
const form = reactive({
  categoryId: undefined as number | undefined,
  title: "",
  description: "",
  price: "",
  imageBase64: null as string | null,
});
const rules: FormRules = {
  categoryId: [{ required: true, message: "请选择分类", trigger: "change" }],
  title: [
    { required: true, message: "请输入标题", trigger: "blur" },
    { min: 2, max: 60, message: "标题长度为 2 到 60 个字符", trigger: "blur" },
  ],
  description: [
    { required: true, message: "请输入商品描述", trigger: "blur" },
    {
      min: 10,
      max: 2000,
      message: "描述长度为 10 到 2000 个字符",
      trigger: "blur",
    },
  ],
  price: [
    { required: true, message: "请输入价格", trigger: "blur" },
    {
      validator: (_r, value, callback) => {
        const message = validatePrice(String(value));
        callback(message ? new Error(message) : undefined);
      },
      trigger: "blur",
    },
  ],
};
async function beforeUpload(file: UploadRawFile) {
  const message = validateImageFile(file);
  if (message) {
    ElMessage.error(message);
    return false;
  }
  try {
    form.imageBase64 = await fileToDataUrl(file);
    imagePreview.value = form.imageBase64;
  } catch {
    ElMessage.error("图片读取失败，请重试");
  }
  return false;
}
function removeImage() {
  form.imageBase64 = null;
  imagePreview.value = null;
}
async function load() {
  loading.value = true;
  try {
    categories.value = (await getCategories()).data.data;
    if (editing.value) {
      const data = (await getProduct(Number(route.params.id))).data.data;
      Object.assign(form, {
        categoryId: data.categoryId,
        title: data.title,
        description: data.description,
        price: String(data.price),
        imageBase64: data.imageBase64,
      });
      imagePreview.value = data.imageBase64;
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, "商品信息加载失败"));
    if (editing.value) router.replace("/my-products");
  } finally {
    loading.value = false;
  }
}
async function submit() {
  if (
    !formRef.value ||
    !(await formRef.value.validate().catch(() => false)) ||
    !form.categoryId
  )
    return;
  submitting.value = true;
  const payload = {
    categoryId: form.categoryId,
    title: form.title.trim(),
    description: form.description.trim(),
    price: Number(form.price),
    imageBase64: form.imageBase64,
  };
  try {
    const result = editing.value
      ? await updateProduct(Number(route.params.id), payload)
      : await createProduct(payload);
    ElMessage.success(editing.value ? "商品已更新" : "发布成功");
    router.replace(`/products/${result.data.data.id}`);
  } catch (error) {
    ElMessage.error(
      getErrorMessage(error, editing.value ? "商品更新失败" : "商品发布失败"),
    );
  } finally {
    submitting.value = false;
  }
}
onMounted(load);
</script>
<template>
  <section class="form-page">
    <header>
      <p class="eyebrow">{{ editing ? "EDIT LISTING" : "NEW LISTING" }}</p>
      <h1>{{ editing ? "编辑你的商品" : "发布一件闲置" }}</h1>
      <p>写清楚一点，下一位需要它的同学就更容易找到它。</p>
    </header>
    <el-skeleton v-if="loading" animated :rows="8" /><el-form
      v-else
      ref="formRef"
      class="product-form"
      :model="form"
      :rules="rules"
      label-position="top"
      @submit.prevent="submit"
      ><el-form-item label="商品分类" prop="categoryId"
        ><el-select v-model="form.categoryId" placeholder="选择分类"
          ><el-option
            v-for="item in categories"
            :key="item.id"
            :label="item.name"
            :value="item.id" /></el-select></el-form-item
      ><el-form-item label="商品标题" prop="title"
        ><el-input
          v-model.trim="form.title"
          maxlength="60"
          show-word-limit
          placeholder="例如：九成新高等数学教材" /></el-form-item
      ><el-form-item label="商品描述" prop="description"
        ><el-input
          v-model.trim="form.description"
          type="textarea"
          :rows="6"
          maxlength="2000"
          show-word-limit
          placeholder="说明新旧程度、规格、使用情况等" /></el-form-item
      ><el-form-item label="价格（元）" prop="price"
        ><el-input
          v-model="form.price"
          inputmode="decimal"
          placeholder="例如：25.50" /></el-form-item
      ><el-form-item label="商品图片（可选）"
        ><div class="upload-area">
          <img
            v-if="imagePreview"
            :src="imagePreview"
            alt="商品预览"
          /><el-upload
            v-else
            accept="image/jpeg,image/png,image/webp"
            :show-file-list="false"
            :before-upload="beforeUpload"
            ><el-button>选择 JPEG / PNG / WebP 图片</el-button></el-upload
          ><el-button
            v-if="imagePreview"
            text
            type="danger"
            @click="removeImage"
            >移除图片</el-button
          ><small>最多一张，大小不超过 2 MB</small>
        </div></el-form-item
      >
      <div class="form-actions">
        <el-button @click="router.back()">取消</el-button
        ><el-button type="primary" native-type="submit" :loading="submitting">{{
          editing ? "保存修改" : "发布商品"
        }}</el-button>
      </div></el-form
    >
  </section>
</template>
