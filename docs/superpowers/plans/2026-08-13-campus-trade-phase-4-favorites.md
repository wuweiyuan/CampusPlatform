# Campus Trade Phase 4: Favorites Plan

**Goal:** Add a reliable user-to-product favorite relationship and a usable favorites page.

**Depends on:** Phase 3 passed.

**Deliverable:** A logged-in user can favorite/unfavorite a product and browse their own favorites without duplicates.

## 1. Schema and API contract

- [ ] Add `favorite` migration with `id`, `user_id`, `product_id`, `created_at` and unique index `(user_id, product_id)`.
- [ ] Document: `POST /api/products/{productId}/favorite`, `DELETE /api/products/{productId}/favorite`, `GET /api/favorites?page=&pageSize=`.
- [ ] Decide and document whether users may favorite off-shelf products. Recommended: existing favorites remain visible, but only `ON_SALE` products can be newly favorited.

## 2. Backend

- [ ] Obtain user ID only from authenticated principal.
- [ ] On add, verify product exists and is `ON_SALE`; create favorite.
- [ ] Treat repeated add as idempotent success or explicit documented conflict; recommended: idempotent success to simplify the UI while retaining the database unique constraint.
- [ ] On delete, delete only where both `user_id` and `product_id` match; never allow a user to remove another user’s favorite.
- [ ] Paginate favorites ordered by favorite creation time descending, returning enough product summary fields to render cards without N+1 queries.
- [ ] Add a `favorited` boolean to authenticated product detail/list responses if convenient, but never derive it from client input.

## 3. Tests

- [ ] Test add, duplicate add, delete, delete missing, unauthenticated access, and cross-user isolation.
- [ ] Test favorites list paginates and exposes product’s current status.
- [ ] Run the full backend suite before frontend work.

## 4. Vue

- [ ] Add a heart/favorite control on eligible product cards and product detail; prevent double click during request.
- [ ] Update local favorite state only after server success; show backend error if an on-sale condition changed.
- [ ] Build “我的收藏” with pagination, navigation to product detail, and a clear off-shelf/locked/sold status label.
- [ ] Add empty, loading, and error states; test add/remove UI using mocked APIs.

## 5. Phase 4 exit checklist

- [ ] Duplicate database records are impossible.
- [ ] Favorites are scoped to current user.
- [ ] Product status changes are represented honestly in the favorite list.
- [ ] Tests/build pass, endpoints are documented, and changes are committed.
