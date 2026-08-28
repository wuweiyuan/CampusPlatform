# 阶段 3 商品前端 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 提供可匿名浏览、登录后可发布和管理商品的完整 Vue 前端，并统一已有页面视觉。

**Architecture:** API 层集中定义后端契约和 HTTP 调用；页面维护请求状态和路由交互；纯校验/转换逻辑放在 `utils/product.ts` 并使用 Vitest 测试。统一布局允许公开访问，路由元数据只保护写操作与个人页面。

**Tech Stack:** Vue 3、TypeScript、Vue Router、Pinia、Axios、Element Plus、Vite、Vitest。

---

### Task 1: 建立可测试的商品表单工具

**Files:**
- Modify: `campus-trade-web/package.json`
- Modify: `campus-trade-web/package-lock.json`
- Modify: `campus-trade-web/vite.config.ts`
- Create: `campus-trade-web/src/utils/product.ts`
- Create: `campus-trade-web/src/utils/product.spec.ts`

- [ ] **Step 1: 写失败测试，定义金额和图片客户端校验行为。**

```ts
import { describe, expect, it } from 'vitest'
import { validateImageFile, validatePrice } from './product'

describe('validatePrice', () => {
  it('accepts a positive price with at most two decimal places', () => {
    expect(validatePrice('25.50')).toBe('')
  })
  it('rejects zero and prices with more than two decimals', () => {
    expect(validatePrice('0')).toBe('价格必须大于 0')
    expect(validatePrice('1.234')).toBe('价格最多保留两位小数')
  })
})

describe('validateImageFile', () => {
  it('rejects an unsupported image type and a file over 2 MB', () => {
    expect(validateImageFile(new File(['x'], 'a.gif', { type: 'image/gif' }))).toBe('仅支持 JPEG、PNG 或 WebP 图片')
    expect(validateImageFile(new File([new Uint8Array(2 * 1024 * 1024 + 1)], 'a.png', { type: 'image/png' }))).toBe('图片大小不能超过 2 MB')
  })
})
```

- [ ] **Step 2: 在项目 Node 20.19.5 下运行测试，确认因模块不存在而失败。**

Run: `node-project-env.sh run -- npm run test -- src/utils/product.spec.ts`

Expected: FAIL，提示找不到 `./product` 或缺少 `test` 脚本。

- [ ] **Step 3: 添加 Vitest 和最小实现。**

```ts
export const IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp']
export const MAX_IMAGE_SIZE = 2 * 1024 * 1024
export function validatePrice(value: string) {
  const amount = Number(value)
  if (!Number.isFinite(amount) || amount <= 0) return '价格必须大于 0'
  return /^\d+(\.\d{1,2})?$/.test(value) ? '' : '价格最多保留两位小数'
}
export function validateImageFile(file: File) {
  if (!IMAGE_TYPES.includes(file.type)) return '仅支持 JPEG、PNG 或 WebP 图片'
  return file.size > MAX_IMAGE_SIZE ? '图片大小不能超过 2 MB' : ''
}
export function fileToDataUrl(file: File) {
  return new Promise<string>((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(String(reader.result))
    reader.onerror = () => reject(new Error('图片读取失败'))
    reader.readAsDataURL(file)
  })
}
```

`package.json` 增加 `"test": "vitest run"`，并增加 `vitest`、`jsdom`、`@vue/test-utils` 开发依赖；Vite 配置加入 `test: { environment: 'jsdom' }`。组件测试使用 `@vue/test-utils` 的 `mount`，为 Element Plus 和 Router 提供测试插件。

- [ ] **Step 4: 重跑单测和完整测试。**

Run: `node-project-env.sh run -- npm run test`

Expected: PASS，所有校验测试通过。

### Task 2: 建立分类和商品 API 契约

**Files:**
- Create: `campus-trade-web/src/api/category.ts`
- Create: `campus-trade-web/src/api/product.ts`

- [ ] **Step 1: 先在 API 使用方写失败类型检查用例。**

创建临时的 `src/api/product.contract.spec.ts`，导入 `getProducts`、`getProduct`、`createProduct`、`updateProduct`、`offShelfProduct`、`getMyProducts`，断言请求参数使用 `ProductListQuery` / `ProductPayload`；运行 Vitest 确认模块尚不存在。

- [ ] **Step 2: 实现共享契约和请求函数。**

```ts
export interface ApiResponse<T> { code: number; message: string; data: T }
export interface Category { id: number; name: string; sort: number }
export interface PageResponse<T> { page: number; pageSize: number; total: number; records: T[] }
export interface ProductCard { id: number; title: string; price: number; imageBase64: string | null; status: ProductStatus; categoryId: number; categoryName: string; sellerId: number; sellerName: string; viewCount: number; createdAt: string }
export type ProductStatus = 'ON_SALE' | 'LOCKED' | 'SOLD' | 'OFF_SHELF'
export interface ProductPayload { categoryId: number; title: string; description: string; price: number; imageBase64: string | null }
```

`category.ts` 调用 `GET /categories`；`product.ts` 分别调用 `GET /products`、`GET /products/:id`、`POST /products`、`PUT /products/:id`、`POST /products/:id/off-shelf`、`GET /products/mine`，并以 `params` 传递所有列表筛选参数。

- [ ] **Step 3: 执行类型检查，确认所有 API 消费者能解析类型。**

Run: `node-project-env.sh run -- npm run type-check`

Expected: PASS。

### Task 3: 改造全局壳和路由权限

**Files:**
- Modify: `campus-trade-web/src/router/index.ts`
- Modify: `campus-trade-web/src/layouts/AppLayout.vue`
- Modify: `campus-trade-web/src/style.css`

- [ ] **Step 1: 写路由配置的失败测试。**

测试在未登录时 `router.resolve('/')` 和 `router.resolve('/products/1')` 不要求 `requiresAuth`，而 `/products/new`、`/products/1/edit`、`/my-products` 要求认证；先运行并确认当前首页仍受保护导致失败。

- [ ] **Step 2: 用最小路由变更通过测试。**

将 `AppLayout` 父路由改为公开；新增详情与编辑子路由；仅为创建、编辑、我的发布和个人中心设置 `meta: { requiresAuth: true }`。未登录菜单提供登录/注册，登录菜单提供发布/我的发布/个人中心/退出。

- [ ] **Step 3: 应用视觉令牌和响应式结构。**

在 `style.css` 设置暖米白 `#f5f2e8`、墨绿 `#164b3a`、珊瑚橙 `#f2643d`、边框与阴影变量；移除 Vite 初始演示布局约束。布局使用响应式侧栏与顶部工具栏，不让窄屏横向溢出。

- [ ] **Step 4: 运行路由测试与类型检查。**

Run: `node-project-env.sh run -- npm run test && /Users/wayne/.codex/skills/node-project-node-version/scripts/node-project-env.sh run -- npm run type-check`

Expected: PASS。

### Task 4: 实现商品广场

**Files:**
- Create: `campus-trade-web/src/views/market/ProductMarketView.vue`

- [ ] **Step 1: 写失败组件测试。**

测试 mocked `getCategories` 和 `getProducts` 后，页面默认请求 `{ page: 1, pageSize: 12 }`，点击分类或搜索后请求携带条件且重置到第一页；mock 空记录时展示空状态。

- [ ] **Step 2: 实现数据状态与请求。**

页面维护 `keyword`、`selectedCategoryId`、`pagination`、`loading`、`errorMessage`；在挂载时并行读取分类和第一页商品。搜索、选择分类、重置、翻页均调用统一 `loadProducts`。

- [ ] **Step 3: 实现目录式界面。**

使用搜索框、分类胶囊、发布按钮、响应式 `el-card` 商品网格和 `el-pagination`。卡片显示分类、价格、卖家、浏览量；`imageBase64` 为空时按分类名称首字生成色块占位。加载显示骨架屏，空状态提供重置按钮，错误状态提供重试按钮。

- [ ] **Step 4: 运行组件测试和类型检查。**

Run: `node-project-env.sh run -- npm run test && /Users/wayne/.codex/skills/node-project-node-version/scripts/node-project-env.sh run -- npm run type-check`

Expected: PASS。

### Task 5: 实现公开商品详情

**Files:**
- Create: `campus-trade-web/src/views/market/ProductDetailView.vue`

- [ ] **Step 1: 写失败组件测试。**

测试页面从路由参数读取 ID 并调用 `getProduct(1)`，成功时展示标题、描述、价格、卖家和浏览量，接口拒绝时展示可返回广场的错误状态。

- [ ] **Step 2: 实现详情与状态处理。**

使用 `onMounted` / 监听路由 ID 请求详情，分别渲染加载骨架、错误结果、图片或占位、分类/状态信息、发布人、发布时间与浏览量。提供返回广场按钮，卖家为当前登录用户时额外显示编辑入口。

- [ ] **Step 3: 验证详情页。**

Run: `node-project-env.sh run -- npm run test && /Users/wayne/.codex/skills/node-project-node-version/scripts/node-project-env.sh run -- npm run type-check`

Expected: PASS。

### Task 6: 实现发布和编辑表单

**Files:**
- Create: `campus-trade-web/src/views/product/ProductFormView.vue`

- [ ] **Step 1: 写失败测试。**

测试表单选择 GIF 或大于 2 MB 文件显示相应错误而不读取文件；编辑模式加载商品详情并预填字段；创建模式调用 `createProduct`，编辑模式调用 `updateProduct`。

- [ ] **Step 2: 实现共享表单。**

根据路由名称区分创建和编辑。挂载时读取启用分类，编辑模式额外读取商品详情。以 Element Plus 表单配合 `validatePrice`，在文件选择时调用 `validateImageFile` / `fileToDataUrl` 并渲染预览和移除按钮。提交负载只含 `categoryId`、`title`、`description`、`price`、`imageBase64`。

- [ ] **Step 3: 实现成功与失败导航。**

创建成功显示消息后进入新详情；编辑成功进入更新后详情。403、状态限制、图片或分类错误保留服务端消息；编辑不可操作时返回我的发布。

- [ ] **Step 4: 运行测试与类型检查。**

Run: `node-project-env.sh run -- npm run test && /Users/wayne/.codex/skills/node-project-node-version/scripts/node-project-env.sh run -- npm run type-check`

Expected: PASS。

### Task 7: 实现我的发布

**Files:**
- Create: `campus-trade-web/src/views/product/MyProductsView.vue`

- [ ] **Step 1: 写失败组件测试。**

测试默认调用 `getMyProducts({ page: 1, pageSize: 12 })`；选择状态和提交关键词会回到第一页；只有 `ON_SALE` 卡片显示编辑、下架；确认下架调用 `offShelfProduct` 后刷新。

- [ ] **Step 2: 实现列表与操作。**

提供关键词输入、状态 `el-select`、商品列表、分页和全部请求状态。状态名称映射为中文标签。下架使用 `ElMessageBox.confirm`，确认后禁用操作按钮、调用 API 并刷新当前页。

- [ ] **Step 3: 运行测试与类型检查。**

Run: `node-project-env.sh run -- npm run test && /Users/wayne/.codex/skills/node-project-node-version/scripts/node-project-env.sh run -- npm run type-check`

Expected: PASS。

### Task 8: 统一既有页面的视觉

**Files:**
- Modify: `campus-trade-web/src/views/LoginView.vue`
- Modify: `campus-trade-web/src/views/RegisterView.vue`
- Modify: `campus-trade-web/src/views/ProfileView.vue`
- Modify: `campus-trade-web/src/views/ForbiddenView.vue`
- Modify: `campus-trade-web/src/views/NotFoundView.vue`

- [ ] **Step 1: 写视觉结构回归测试。**

测试认证页依然保留原有表单字段、提交按钮、注册验证码按钮和路由链接；状态页依然提供返回广场操作。

- [ ] **Step 2: 只改模板类名和 scoped CSS。**

为认证页添加校园市集文字标识、纸张卡片、绿色主按钮和橙色小强调；个人中心改为信息卡片；403/404 改为同一套有明确返回入口的状态页。保留既有脚本、表单规则、消息和所有认证 API 调用。

- [ ] **Step 3: 运行完整测试与类型检查。**

Run: `node-project-env.sh run -- npm run test && npm run type-check`

Expected: PASS。

### Task 9: 整体验证和文档交接

**Files:**
- Modify: `docs/学习清单/阶段-3-今日交接记录-2026-08-20.md`

- [ ] **Step 1: 在项目 Node 20.19.5 下运行完整自动验证。**

Run: `node-project-env.sh run -- npm run test && /Users/wayne/.codex/skills/node-project-node-version/scripts/node-project-env.sh run -- npm run build`

Expected: 两条命令 exit code 0。

- [ ] **Step 2: 执行浏览器手动验收。**

验证未登录广场和详情、未登录重定向发布/我的发布、登录后发布含图片商品、编辑、下架、另一账号公开浏览、我的发布状态筛选、非法图片提示和窄屏布局。

- [ ] **Step 3: 更新交接记录。**

勾选已完成的前端页面、浏览器验收和构建项；保留无法验证的后端补测（超 2 MB 原始图片、有效 WebP、停用分类、文本边界）为待办。记录实际验证命令和结果，不记录密码、Token、验证码或密钥。

- [ ] **Step 4: 交给用户检查变更。**

查看 `git diff --check`、`git status --short` 和自动验证输出。用户自行决定 `git add` 和 `git commit`；实施者不得修改 Git index 或提交历史。
