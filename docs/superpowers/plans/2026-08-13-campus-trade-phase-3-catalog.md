# Campus Trade Phase 3: Categories and Products Plan

**Goal:** Build category management APIs and the student-facing product catalog, including Base64 image validation, pagination, filtering, and search.

**Depends on:** Phase 1 passed; Phase 2 is recommended for the web portion.

**Deliverable:** Students can browse, search, view, publish, edit, and take down their own single-item listings.

## 1. Schema and constants

- [ ] Write `docs/api/phase-3.md` before coding, including request fields, response fields, ownership, and every status transition.
- [ ] Add `category` migration: `id`, `name` (unique), `sort`, `status`, timestamps. Define category status `ENABLED` and `DISABLED`.
- [ ] Add `product` migration: `id`, `seller_id`, `category_id`, `title`, `description`, decimal `price`, `image_base64` LONGTEXT, `status`, `view_count`, timestamps.
- [ ] Add indexes for product list queries: `(status, created_at)`, `category_id`, and `seller_id`. Define product status constants `ON_SALE`, `LOCKED`, `SOLD`, `OFF_SHELF`.
- [ ] Seed at least five enabled categories through a migration, and document how to change them later as admin.

## 2. Backend category rules

- [ ] Implement public `GET /api/categories` returning enabled categories in `sort` order.
- [ ] Implement administrator-only category create, update, sort/status update, and list endpoints; reserve management UI for Phase 6.
- [ ] Validate trimmed category name is nonempty, 1–30 characters, and unique.
- [ ] Block publishing into a missing or disabled category.
- [ ] Test public list excludes disabled categories and student calls to admin endpoints return 403.

## 3. Backend product rules

- [ ] Define product APIs: public paginated list and detail; authenticated create/update/off-shelf; authenticated “my products” list.
- [ ] List query parameters: `page`, `pageSize`, optional `categoryId`, optional keyword; cap page size at 50; search title and description using parameterized SQL, never string-concatenated SQL.
- [ ] Validate title 2–60 characters, description 10–2000 characters, price positive with at most two decimals, and category exists/enabled.
- [ ] Accept only one Base64 data URL per product in Phase 1. Decode safely and reject non-JPEG/PNG/WebP or decoded source >2 MB. Never trust just the MIME prefix supplied by the browser.
- [ ] Create product with authenticated user as `sellerId` and status `ON_SALE`; never accept seller ID or product status from create request.
- [ ] Allow edit only when caller is seller and product is `ON_SALE`; only permitted fields may change.
- [ ] Allow seller off-shelf only from `ON_SALE`. Administrator override is postponed to Phase 6. Do not permit off-shelf of `LOCKED` unless you later define order cancellation semantics.
- [ ] Increment `view_count` for a successful public detail view; tolerating approximate counts is acceptable for this learning project.

## 4. Product tests

- [ ] Test repository pagination/search/filter combinations against a disposable database.
- [ ] Test seller can create and edit own on-sale product.
- [ ] Test another user cannot edit/off-shelf it (403).
- [ ] Test invalid category, price, image type, image size, and status all return documented errors.
- [ ] Test detail increments view count and an off-shelf product is excluded from the public list.

## 5. Vue pages

- [ ] Create API modules for categories and products, with request/response types or JSDoc documented near the calls.
- [ ] Build product market page: category filter, keyword search, reset, paginated card grid, price, category, seller name, image fallback, loading, empty, and error states.
- [ ] Build product detail page: full product information and seller identity; reserve favorite/order controls as disabled or absent until later phases.
- [ ] Build product form used by create/edit: fields match server DTO, category fetched from API, image chooser validates type/size before converting to Base64, preview, submit loading/error state.
- [ ] Build my-products page: paginated listings with edit and off-shelf actions only where status permits.
- [ ] Add all Phase 3 links to ordinary-user menu; protect publish/edit/my-products with auth route metadata.

## 6. Phase 3 exit checklist

- [ ] Product market, detail, publish, edit, my-products and off-shelf flows work against real APIs.
- [ ] No client request can set `sellerId`, `status`, or bypass backend ownership checks.
- [ ] Public browsing supports category, keyword, and pagination.
- [ ] Image validation happens on both frontend and backend.
- [ ] Tests/build pass; API documentation and a Git commit exist.
