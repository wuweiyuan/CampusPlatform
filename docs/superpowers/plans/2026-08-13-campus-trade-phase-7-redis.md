# Campus Trade Phase 7: Redis Cache and Token Operations Plan

**Goal:** Make Redis use observable and correct for email codes, token logout, enabled categories, and hot products.

**Depends on:** Phases 1, 3, 5, and 6 passed.

**Deliverable:** Repeated reads use cache, all relevant writes invalidate cache, and Redis unavailability does not silently corrupt business state.

## 1. Establish measurable cache behavior

- [ ] Add a `docs/cache.md` table listing key, value shape, TTL, writer, readers, and invalidation events.
- [ ] Define `category:list` as enabled categories in display order, TTL 30 minutes.
- [ ] Define `product:hot` as top 10 `ON_SALE` products by `view_count DESC, created_at DESC`, TTL 10 minutes.
- [ ] Retain `auth:email:code:{email}`, cooldown keys, and `auth:token:blacklist:{jti}` from Phase 1 with their existing TTL semantics.
- [ ] Use JSON serialization with explicit DTO/value classes where possible; do not cache mutable JPA/MyBatis entity objects with hidden fields.

## 2. Categories cache

- [ ] Write test: first public categories request loads database and writes `category:list`; second request returns cached values.
- [ ] Implement cache-aside read for enabled category list.
- [ ] On any category create, update, reorder, enable, or disable, delete `category:list` after successful database transaction.
- [ ] Test the next public request after invalidation sees new database data rather than stale data.

## 3. Hot-products cache

- [ ] Add a dedicated query returning exactly ten or fewer `ON_SALE` product summary records using the documented order.
- [ ] Write test for miss → database load → Redis set → hit.
- [ ] Invalidate `product:hot` after product create/update/off-shelf, order create/cancel/pay, and view-count update. It is acceptable for a product detail view to delete the key rather than rewrite it.
- [ ] Expose a public `/api/products/hot` endpoint or clearly include hot data in an existing documented endpoint; do not hide cache behavior behind an unused service.
- [ ] Add hot-products section to market page and handle fewer than ten records.

## 4. Resilience and observability

- [ ] Add structured logs or metrics markers for cache hit, miss, put, and eviction; never log email codes, passwords, full JWTs, or Base64 images.
- [ ] Verify app behavior when Redis is unavailable. Recommended learning-project policy: auth code/login logout functions return a clear service-unavailable error; category/hot reads fall back to MySQL with a warning log; order/product writes still use MySQL transaction and attempt cache deletion best-effort after commit.
- [ ] Test invalidation runs only after the database transaction succeeds; a failed write must not remove cache unnecessarily unless that is explicitly documented.

## 5. Phase 7 exit checklist

- [ ] `redis-cli` or application logs demonstrate category/hot cache miss then hit.
- [ ] Every documented data mutation invalidates the matching cache key.
- [ ] No Redis key contains a plaintext password, secret, or full long-lived JWT.
- [ ] Cache fallback/error policy is documented and manually tested.
- [ ] Tests/build pass and changes are committed.
