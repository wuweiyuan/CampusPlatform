# Campus Trade Phase 6: Administrator Console Plan

**Goal:** Add administrator-only server operations and corresponding Vue management pages without creating a second frontend app.

**Depends on:** Phases 2, 3, and 5 passed.

**Deliverable:** An ADMIN user sees management menus and can manage categories, users, products, and orders; USER receives 403 from every admin API.

## 1. Access-control baseline

- [ ] Add tests that a valid `USER` JWT receives HTTP 403 from every `/api/admin/**` endpoint.
- [ ] Centralize ADMIN requirement in security configuration/annotations; do not manually copy role checks into every controller without a shared policy.
- [ ] Seed or document one local administrator with BCrypt password; public registration always creates `USER`.
- [ ] Add `/admin` route metadata `requiresAuth: true` and `roles: ['ADMIN']`; route guard sends non-admin users to `/403`.

## 2. Category management

- [ ] Implement paginated/list management endpoint including disabled categories, create, update name/sort, and enable/disable action.
- [ ] Ensure disabled categories are not accepted by product publishing even when admin changes status while a product form is open.
- [ ] Build category management table and modal/form with validation, sort, enabled status, loading/error/empty state.

## 3. User management

- [ ] Implement administrator paginated user query with username/email/role/status filters and safe fields only; never return password hash or JWT details.
- [ ] Implement enable/disable endpoint. Refuse to disable the currently authenticated admin and document whether disabled users’ existing JWTs remain valid; recommended first version: token check reloads user status and rejects disabled users.
- [ ] Build users table with filters, status tag, and enable/disable confirmation.

## 4. Product and order management

- [ ] Implement administrator product list with seller/category/status/keyword filters and pagination.
- [ ] Implement administrator product off-shelf only from `ON_SALE`; locked product must not be forcibly off-shelved in Phase 6 because it has an active buyer flow.
- [ ] Implement administrator order list with order number/status/buyer/seller filters and pagination. Keep it read-only in Phase 6: administrator does not pay, cancel, or complete user orders.
- [ ] Build product management and order management tables; include product/order detail navigation and status labels.

## 5. Verification

- [ ] Test every admin endpoint with USER and ADMIN tokens.
- [ ] Test a disabled user cannot log in; if implementing status lookup per request, test existing token also becomes invalid.
- [ ] Test administrator cannot affect locked product or force a user’s order state transition.
- [ ] Browser-test role change by logging out/in as a student and administrator; verify menu and direct URL behavior.

## 6. Phase 6 exit checklist

- [ ] One Vue project has role-specific menu and routes.
- [ ] Server-side admin authorization protects every admin action.
- [ ] All admin tables paginate and have loading/empty/error states.
- [ ] Password hashes and secret data never enter admin responses.
- [ ] Tests/build pass, documentation is current, and changes are committed.
