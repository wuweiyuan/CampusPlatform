# 商品分页条数选择 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 让商品广场和我的发布可选每页 10、20、30、50 条，默认 10 条。

**Architecture:** 两个页面各自维护响应式 `pageSize`，将其传入现有 API 查询。`el-pagination` 负责展示选择器并在条数变化时触发页码重置和请求。

**Tech Stack:** Vue 3、TypeScript、Element Plus、Vitest。

---

### Task 1: 用测试固定默认和切换行为

**Files:**
- Modify: `campus-trade-web/src/views/market/ProductMarketView.vue`
- Modify: `campus-trade-web/src/views/product/MyProductsView.vue`
- Create: `campus-trade-web/src/views/product/page-size.spec.ts`

- [ ] **Step 1: 编写失败测试。**

使用 mocked `getProducts` / `getMyProducts` 挂载两个页面，断言首次查询传入 `{ page: 1, pageSize: 10 }`；触发 `size-change` 为 20 后断言下一次查询传入 `{ page: 1, pageSize: 20 }`。

- [ ] **Step 2: 运行测试，确认当前固定 12 条导致失败。**

Run: `/Users/wayne/.codex/skills/node-project-node-version/scripts/node-project-env.sh run -- npm run test -- src/views/product/page-size.spec.ts`

Expected: FAIL，首次请求使用 12，且不存在 `size-change` 处理。

- [ ] **Step 3: 实现响应式页大小与分页选择器。**

在两个页面将固定 `12` 改为 `ref(10)`；分页组件使用：

```vue
<el-pagination
  layout="total, sizes, prev, pager, next"
  :page-sizes="[10, 20, 30, 50]"
  :page-size="pageSize"
  @size-change="changePageSize"
/>
```

`changePageSize(size)` 将 `pageSize.value = size`，随后调用 `load(1)` 或 `loadProducts(1)`。

- [ ] **Step 4: 运行失败测试至通过。**

Run: `/Users/wayne/.codex/skills/node-project-node-version/scripts/node-project-env.sh run -- npm run test -- src/views/product/page-size.spec.ts`

Expected: PASS。

### Task 2: 验证完整前端与交接

**Files:**
- Modify: `docs/学习清单/阶段-3-今日交接记录-2026-08-20.md`

- [ ] **Step 1: 运行完整测试和构建。**

Run: `/Users/wayne/.codex/skills/node-project-node-version/scripts/node-project-env.sh run -- npm run test && /Users/wayne/.codex/skills/node-project-node-version/scripts/node-project-env.sh run -- npm run build`

Expected: 退出码为 0。

- [ ] **Step 2: 更新交接记录。**

记录两个商品分页页默认 10 条、可选 10/20/30/50，以及自动测试/构建实际结果；不记录未执行的浏览器手动验收为完成。

- [ ] **Step 3: 交给用户检查变更。**

运行 `git diff --check` 和 `git status --short`。用户自行暂存和提交，实施者不修改 Git index 或提交历史。
