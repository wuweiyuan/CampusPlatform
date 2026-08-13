# Campus Trade Phase 8: Quality, Tests, and Documentation Plan

**Goal:** Turn the working learning project into a reproducible, testable, understandable application before container deployment.

**Depends on:** Phases 1–7 passed.

**Deliverable:** Automated coverage of important behavior, consistent errors/logging, robust page states, and documentation another learner can follow.

## 1. Backend test suite

- [ ] Separate fast unit tests from MySQL/Redis integration tests; use Testcontainers or a documented dedicated local test database, never a developer’s production-like database.
- [ ] Ensure test cases cover email registration, JWT logout, product ownership, category status, favorite uniqueness, all order transition rows, admin authorization, and Redis invalidation.
- [ ] Add tests for invalid page numbers/page sizes, nonexistent IDs, validation errors, malformed Base64, disabled users, and duplicate keys.
- [ ] Verify transaction rollback: force a failure after a product lock/order insert boundary and assert no partial order/product state remains.
- [ ] Run one complete backend command from a clean checkout and record it in README.

## 2. API and error quality

- [ ] Publish all endpoint request/response examples in `docs/api/`, grouped by phase or module.
- [ ] Create a single error-code table with code, HTTP status, user-visible message, and triggering condition.
- [ ] Confirm validation errors identify the affected field without returning internal stack traces.
- [ ] Add request IDs or consistent log context; log authenticated user ID and operation type where helpful, but never password, email code, JWT, SMTP authorization code, or raw Base64 image data.

## 3. Frontend quality

- [ ] Audit every data-fetching page for four states: initial loading, loaded data, empty data, and request failure with retry/navigation option.
- [ ] Add component/router/store tests for auth interception, market search pagination, favorite mutation, and order action visibility.
- [ ] Run `npm test` and production build with the version declared by the frontend project.
- [ ] Check narrow screen layout for login/register, product cards, product form, order list, and administrator tables; document any intentionally unsupported viewports.

## 4. Documentation and reproducibility

- [ ] Expand README: project overview, architecture diagram, requirements, local environment variables, migration command, backend/frontend start commands, test/build commands, demo accounts, and main user journey.
- [ ] Include `.env.example` files with variable names and safe placeholders only.
- [ ] Add a manual acceptance script: user registration → login → publish product → second user favorite/order/pay/complete → administrator moderation; include expected states after each action.
- [ ] Audit Git status and history: generated directories, local `.env`, secrets, database dump, and IDE files must not be committed.

## 5. Phase 8 exit checklist

- [ ] Test commands pass from a clean environment.
- [ ] API contract, error codes, schema migration, and Vue behavior agree.
- [ ] Full buyer/seller/admin manual flow is reproducible from README.
- [ ] No secrets or generated artifacts tracked by Git.
- [ ] Changes are committed before deployment begins.
