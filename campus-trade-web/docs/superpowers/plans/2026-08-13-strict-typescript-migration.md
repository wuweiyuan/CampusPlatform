# Strict TypeScript Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Convert the Vue/Vite health-check application to strict TypeScript with type checking included in its build.

**Architecture:** TypeScript configuration supplies strict compiler semantics and Vite client types. The application entry point, API client, and router become `.ts` modules, while the health view uses a typed SFC script and preserves its existing request states.

**Tech Stack:** Vue 3, Vite, TypeScript, vue-tsc, Axios, Vue Router, Element Plus.

---

### Task 1: Add strict TypeScript tooling

**Files:**
- Modify: `package.json`
- Modify: `package-lock.json`
- Create: `tsconfig.json`
- Create: `src/env.d.ts`

- [ ] **Step 1: Install the compiler and Vue type checker as development dependencies**

Run: `/Users/wayne/.codex/skills/node-project-node-version/scripts/node-project-env.sh run -- npm install -D typescript vue-tsc`

Expected: `typescript` and `vue-tsc` are under `devDependencies`; no test packages are added.

- [ ] **Step 2: Add strict compiler configuration**

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "module": "ESNext",
    "moduleResolution": "Bundler",
    "strict": true,
    "noEmit": true,
    "isolatedModules": true,
    "verbatimModuleSyntax": true,
    "skipLibCheck": true,
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "types": ["vite/client"]
  },
  "include": ["src/**/*.ts", "src/**/*.d.ts", "src/**/*.tsx", "src/**/*.vue", "vite.config.ts"]
}
```

- [ ] **Step 3: Declare Vite client types**

```ts
/// <reference types="vite/client" />
```

- [ ] **Step 4: Include type checking in production builds**

```json
"build": "vue-tsc --noEmit && vite build"
```

### Task 2: Migrate application modules and health view

**Files:**
- Rename: `src/main.js` to `src/main.ts`
- Rename: `src/api/http.js` to `src/api/http.ts`
- Rename: `src/router/index.js` to `src/router/index.ts`
- Modify: `src/views/HealthView.vue`

- [ ] **Step 1: Rename JavaScript modules to TypeScript**

Run: `mv src/main.js src/main.ts && mv src/api/http.js src/api/http.ts && mv src/router/index.js src/router/index.ts`

Expected: no runtime source `.js` modules remain under `src` except static asset imports.

- [ ] **Step 2: Type the health endpoint response and SFC script**

```ts
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import http from '../api/http'

type HealthResponse = string | number | boolean | null | Record<string, unknown> | unknown[]

const status = ref('加载中…')
const isAvailable = ref<boolean | null>(null)

onMounted(async () => {
  try {
    const { data } = await http.get<HealthResponse>('/health')
    status.value = typeof data === 'string' ? data : JSON.stringify(data)
    isAvailable.value = true
  } catch {
    isAvailable.value = false
  }
})
</script>
```

### Task 3: Strict type and bundle verification

**Files:**
- Verify: source files and generated build output only (not committed)

- [ ] **Step 1: Run the direct strict type check**

Run: `/Users/wayne/.codex/skills/node-project-node-version/scripts/node-project-env.sh run -- npx vue-tsc --noEmit`

Expected: exit code 0 and no diagnostics.

- [ ] **Step 2: Run the production build**

Run: `/Users/wayne/.codex/skills/node-project-node-version/scripts/node-project-env.sh run -- npm run build`

Expected: type check and Vite build both exit with code 0.

- [ ] **Step 3: Confirm dependency boundaries**

Run: `npm ls typescript vue-tsc vitest jsdom @vue/test-utils --depth=0`

Expected: TypeScript and vue-tsc are present; Vitest, jsdom, and `@vue/test-utils` are absent.
