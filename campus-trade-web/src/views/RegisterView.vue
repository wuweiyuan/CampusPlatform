<script setup lang="ts">
import { onBeforeUnmount, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import type { FormInstance, FormRules } from "element-plus";
import { register, sendEmailCode } from "../api/auth";
import { getErrorMessage } from "../api/http";

const formRef = ref<FormInstance>();
const router = useRouter();
const submitting = ref(false);
const sendingCode = ref(false);
const secondsLeft = ref(0);
let timer: number | undefined;
const form = reactive({ username: "", email: "", password: "", emailCode: "" });
const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const rules: FormRules<typeof form> = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 3, max: 32, message: "用户名长度为 3 到 32 位", trigger: "blur" },
  ],
  email: [
    { required: true, message: "请输入邮箱", trigger: "blur" },
    { type: "email", message: "邮箱格式不正确", trigger: "blur" },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 8, max: 64, message: "密码长度为 8 到 64 位", trigger: "blur" },
  ],
  emailCode: [
    { required: true, message: "请输入验证码", trigger: "blur" },
    { pattern: /^\d{6}$/, message: "验证码必须是 6 位数字", trigger: "blur" },
  ],
};

async function sendCode() {
  if (!emailPattern.test(form.email)) {
    ElMessage.warning("请先输入格式正确的邮箱");
    return;
  }
  sendingCode.value = true;
  try {
    await sendEmailCode(form.email);
    ElMessage.success("验证码已发送，请查看后端日志");
    secondsLeft.value = 60;
    timer = window.setInterval(() => {
      secondsLeft.value -= 1;
      if (secondsLeft.value <= 0 && timer) {
        window.clearInterval(timer);
        timer = undefined;
      }
    }, 1000);
  } catch (error) {
    ElMessage.error(getErrorMessage(error));
  } finally {
    sendingCode.value = false;
  }
}

async function submit() {
  if (!formRef.value || !(await formRef.value.validate().catch(() => false)))
    return;
  submitting.value = true;
  try {
    await register(form);
    ElMessage.success("注册成功，请登录");
    await router.replace("/login");
  } catch (error) {
    ElMessage.error(getErrorMessage(error));
  } finally {
    submitting.value = false;
  }
}

onBeforeUnmount(() => {
  if (timer) window.clearInterval(timer);
});
</script>

<template>
  <main class="auth-page">
    <el-card class="auth-card" shadow="never">
      <h1>注册账号</h1>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent="submit"
      >
        <el-form-item label="用户名" prop="username"
          ><el-input v-model.trim="form.username" autocomplete="username"
        /></el-form-item>
        <el-form-item label="邮箱" prop="email"
          ><el-input v-model.trim="form.email" autocomplete="email"
        /></el-form-item>
        <el-form-item label="密码" prop="password"
          ><el-input
            v-model="form.password"
            type="password"
            show-password
            autocomplete="new-password"
        /></el-form-item>
        <el-form-item label="验证码" prop="emailCode"
          ><div class="code-row">
            <el-input v-model.trim="form.emailCode" maxlength="6" /><el-button
              native-type="button"
              :disabled="secondsLeft > 0"
              :loading="sendingCode"
              @click="sendCode"
              >{{
                secondsLeft > 0 ? `${secondsLeft} 秒后重发` : "发送验证码"
              }}</el-button
            >
          </div></el-form-item
        >
        <el-button
          type="primary"
          native-type="submit"
          :loading="submitting"
          class="submit-button"
          >注册</el-button
        >
      </el-form>
      <p class="switch-link">
        已有账号？<RouterLink to="/login">去登录</RouterLink>
      </p>
    </el-card>
  </main>
</template>

<style scoped>
.auth-page {
  display: grid;
  min-height: 100vh;
  place-items: center;
  padding: 24px;
  background: #f5f2e8;
}
.auth-card {
  width: min(100%, 460px);
  border: 1px solid #d7d0c1;
  background: #fffdf7;
}
.auth-card:before {
  content: "校 易 · CAMPUS MARKET";
  display: block;
  margin-bottom: 22px;
  color: #f2643d;
  font: 800 10px/1 system-ui;
  letter-spacing: 0.16em;
}
h1 {
  margin: 0 0 24px;
  font-size: 28px;
  color: #164b3a;
}
.code-row {
  display: flex;
  width: 100%;
  gap: 12px;
}
.code-row .el-input {
  flex: 1;
}
.submit-button {
  width: 100%;
}
.switch-link {
  margin: 20px 0 0;
  text-align: center;
  color: #657066;
}
</style>
