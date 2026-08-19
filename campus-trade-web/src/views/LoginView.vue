<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getCurrentUser, login } from '../api/auth'
import { getErrorMessage } from '../api/http'
import { useAuthStore } from '../stores/auth'

const formRef = ref<FormInstance>()
const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const submitting = ref(false)
const form = reactive({ username: '', password: '' })

const rules: FormRules<typeof form> = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 3, max: 32, message: '用户名长度为 3 到 32 位', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 8, max: 64, message: '密码长度为 8 到 64 位', trigger: 'blur' }],
}

async function submit() {
  if (!formRef.value || !(await formRef.value.validate().catch(() => false))) return
  submitting.value = true
  try {
    const loginResponse = await login(form)
    const userResponse = await getCurrentUser(loginResponse.data.data.token)
    authStore.setSession(loginResponse.data.data.token, userResponse.data.data)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect
    await router.replace(typeof redirect === 'string' && redirect.startsWith('/') ? redirect : '/')
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '用户名或密码错误'))
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <main class="auth-page">
    <el-card class="auth-card" shadow="never">
      <h1>登录校园二手交易平台</h1>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="submit">
        <el-form-item label="用户名" prop="username"><el-input v-model.trim="form.username" autocomplete="username" /></el-form-item>
        <el-form-item label="密码" prop="password"><el-input v-model="form.password" type="password" show-password autocomplete="current-password" /></el-form-item>
        <el-button type="primary" native-type="submit" :loading="submitting" class="submit-button">登录</el-button>
      </el-form>
      <p class="switch-link">还没有账号？<RouterLink to="/register">去注册</RouterLink></p>
    </el-card>
  </main>
</template>

<style scoped>
.auth-page { display: grid; min-height: 100vh; place-items: center; padding: 24px; box-sizing: border-box; background: #f6f7fb; }
.auth-card { width: min(100%, 420px); } h1 { margin: 0 0 24px; font-size: 24px; } .submit-button { width: 100%; } .switch-link { margin: 20px 0 0; text-align: center; color: #6b7280; }
</style>
