# Health Check Page Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Display the backend health response at the root Vue route through a proxied Axios request.

**Architecture:** A single Axios instance provides the API prefix and timeout. Vue Router resolves `/` to a mounted view that loads `/health`, exposes the response text, and shows a stable fallback for request errors. Vite keeps the `/api` request prefix while forwarding to the local backend.

**Tech Stack:** Vue 3, Vite, Axios, Vue Router, Element Plus.

---

### Task 1: Install runtime packages

**Files:**
- Modify: `package.json`
- Modify: `package-lock.json`

- [ ] **Step 1: Install exactly the requested runtime dependencies**

Run: `/Users/wayne/.codex/skills/node-project-node-version/scripts/node-project-env.sh run -- npm install element-plus axios vue-router`

Expected: `package.json` lists all three packages under `dependencies`; no Vitest, jsdom, or `@vue/test-utils` packages are added.

### Task 2: Configure the API transport and Vite proxy

**Files:**
- Create: `src/api/http.js`
- Modify: `vite.config.js`

- [ ] **Step 1: Add the shared Axios client**

```js
import axios from 'axios'

const http = axios.create({
  baseURL: '/api',
  timeout: 10_000,
})

export default http
```

- [ ] **Step 2: Add the development proxy without path rewriting**

```js
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
  },
},
```

### Task 3: Create the routed health page

**Files:**
- Create: `src/views/HealthView.vue`
- Create: `src/router/index.js`
- Modify: `src/main.js`
- Modify: `src/App.vue`

- [ ] **Step 1: Create the root route**

```js
import { createRouter, createWebHistory } from 'vue-router'
import HealthView from '../views/HealthView.vue'

export default createRouter({
  history: createWebHistory(),
  routes: [{ path: '/', name: 'health', component: HealthView }],
})
```

- [ ] **Step 2: Implement mount-time health loading**

```js
import { onMounted, ref } from 'vue'
import http from '../api/http'

const status = ref('加载中…')

onMounted(async () => {
  try {
    const { data } = await http.get('/health')
    status.value = typeof data === 'string' ? data : JSON.stringify(data)
  } catch {
    status.value = '后端不可用'
  }
})
```

- [ ] **Step 3: Register Element Plus and routing, and reduce the root component to the route outlet**

```js
import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'

createApp(App).use(ElementPlus).use(router).mount('#app')
```

```vue
<template>
  <RouterView />
</template>
```

### Task 4: Verify the production bundle

**Files:**
- Verify: generated build output only (not committed)

- [ ] **Step 1: Run the production build under the project Node environment**

Run: `/Users/wayne/.codex/skills/node-project-node-version/scripts/node-project-env.sh run -- npm run build`

Expected: Vite completes with `✓ built` and no unresolved import errors.

- [ ] **Step 2: Check the final dependency list**

Run: `npm ls axios element-plus vue-router vitest jsdom @vue/test-utils --depth=0`

Expected: Axios, Element Plus, and Vue Router are present; the three excluded test packages are absent.
