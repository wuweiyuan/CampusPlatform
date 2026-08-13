# Campus Trade Phase 5: Single-Item Order State Machine Plan

**Goal:** Implement transactional single-product ordering with cancellation, mock payment, and buyer completion.

**Depends on:** Phase 3 passed; Phase 4 is optional.

**Deliverable:** One user can buy another user’s on-sale item without duplicate purchase; all state transitions are enforced by the backend.

## 1. State machine contract

- [ ] Copy the following exact state table into `docs/api/phase-5.md` before coding.

| Operation | Actor | Required order state | Required product state | Result order | Result product |
|---|---|---|---|---|---|
| create order | buyer | none | `ON_SALE` | `PENDING_PAYMENT` | `LOCKED` |
| cancel | buyer | `PENDING_PAYMENT` | `LOCKED` | `CANCELLED` | `ON_SALE` |
| mock pay | buyer | `PENDING_PAYMENT` | `LOCKED` | `PAID` | `SOLD` |
| confirm complete | buyer | `PAID` | `SOLD` | `COMPLETED` | `SOLD` |

- [ ] State and document that user cannot purchase own product; seller cannot pay/cancel/complete as buyer; all timestamp fields are server-generated.

## 2. Schema

- [ ] Add `orders` migration: bigint `id`, unique varchar `order_no`, `buyer_id`, `seller_id`, `product_id`, decimal `amount`, `status`, `created_at`, `paid_at`, `completed_at`, `updated_at`.
- [ ] Index buyer list `(buyer_id, created_at)`, seller list `(seller_id, created_at)`, product ID, status, and order number.
- [ ] Use a server-generated order number; document the format and guarantee uniqueness with a database constraint.

## 3. Transactional backend implementation

- [ ] Document endpoints: create order, get detail, list my purchases, list my sales, cancel, pay, complete. Request body for create only contains product ID; state/amount/seller are never trusted from client.
- [ ] Write a concurrency test first: two different buyers concurrently call create for one `ON_SALE` product; exactly one request succeeds and final product is `LOCKED`.
- [ ] Implement create within `@Transactional`: fetch product with a locking strategy or atomic conditional update `WHERE id=? AND status='ON_SALE'`; reject if no row is updated; reject self-purchase; insert snapshot order using product’s current seller and price.
- [ ] Implement cancel within `@Transactional`: require caller is buyer; atomically change only `PENDING_PAYMENT` to `CANCELLED`; change its product from `LOCKED` to `ON_SALE`; reject retry/other states.
- [ ] Implement mock pay within `@Transactional`: require caller buyer; atomically change only `PENDING_PAYMENT` to `PAID`, set `paid_at`; change exact product `LOCKED` to `SOLD`; reject retries.
- [ ] Implement complete within `@Transactional`: require caller buyer; atomically change only `PAID` to `COMPLETED`, set `completed_at`; product remains `SOLD`.
- [ ] Read all current order/product states inside the transaction; do not use status values cached in an HTTP request or browser state.
- [ ] Return a documented conflict/business error for invalid state; never silently treat a repeated payment as success.

## 4. Backend test matrix

- [ ] Normal create → cancel restores product to `ON_SALE`.
- [ ] Normal create → pay marks product `SOLD` → complete succeeds.
- [ ] Self-purchase fails.
- [ ] Create for `OFF_SHELF`, `LOCKED`, or `SOLD` fails.
- [ ] Other user cannot cancel/pay/complete.
- [ ] Cancel/pay/complete in the wrong order fails.
- [ ] Concurrent create allows one winner only.
- [ ] Buyer and seller order lists return correct scoped records and pagination.

## 5. Vue order experience

- [ ] Add buy action only on an on-sale product not owned by current user; show a confirmation dialog with product title and server price.
- [ ] On confirmation, create order then navigate to order detail or my orders; do not rely solely on disabled UI for self-purchase prevention.
- [ ] Build my orders with buyer/seller tabs, pagination, status tags, product links, and state-driven buttons: cancel/pay only pending; complete only paid.
- [ ] Require a confirmation dialog for cancel and mock pay; show success then refresh from server.
- [ ] Handle backend conflict with a message and reload current order/product state.
- [ ] Component-test which buttons appear for each returned status.

## 6. Phase 5 exit checklist

- [ ] State table, API documentation, and migration reflect each other.
- [ ] Product and order updates occur in one transaction.
- [ ] Duplicate purchase is prevented by database-level/atomic behavior, not UI timing.
- [ ] Every invalid actor/status combination is rejected.
- [ ] Full backend suite, frontend tests, build, and manual buyer/seller walkthrough pass; changes are committed.
