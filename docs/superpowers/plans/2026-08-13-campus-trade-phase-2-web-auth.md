# Campus Trade Phase 2: Web Authentication and Access Control Plan

**Goal:** Let users register and log in through Vue, preserve a session, and expose role-appropriate routes safely.

**Depends on:** Phase 1 passed.

**Deliverable:** Login and registration UI; role-aware menu; route guard; authenticated Axios requests; 401 recovery.

## 1. Route and state design

- [ ] Write `docs/web/phase-2.md` with routes: `/login`, `/register`, `/`, `/profile`, `/403`, `/404`; reserve `/admin/*` for Phase 6.
- [ ] Use one store solution consistently (Pinia is recommended). Its state contains only `token` and sanitized current user `{id,username,email,role}`; never store the password or email code.
- [ ] Persist token and sanitized user in localStorage, restore them on startup, and clear them together on logout or backend 401.
- [ ] Route metadata: `requiresAuth` for profile and future protected pages; `roles: ['ADMIN']` for administrator routes.

## 2. API layer

- [ ] Create frontend auth API methods matching Phase 1: send code, register, login, me, logout.
- [ ] Add Axios request interceptor that sends `Authorization: Bearer <token>` only when token exists.
- [ ] Add Axios response interceptor that clears session and redirects to `/login?redirect=<current path>` on 401; do not redirect for login/register endpoint failures.
- [ ] Add generic UI messages for API error `message`; do not display stack traces or raw Axios objects.

## 3. Pages and components

- [ ] Build `RegisterView`: username/email/password/code fields, client validation matching backend rules, send-code button, 60-second countdown, disabled repeated send button, submit loading state, success redirect to login.
- [ ] Build `LoginView`: username/password fields, submit loading state, preserve redirect target after successful login, then call `/me` or consume the validated returned user.
- [ ] Build `AppLayout`: header, logout action, and ordinary-user navigation: 商品广场, 发布商品, 我的发布, 我的收藏, 我的订单, 个人中心. Links not implemented yet can show a neutral “功能将在后续阶段完成” page, not a broken route.
- [ ] Build `ProfileView`: display only current username, email, and role.
- [ ] Build dedicated 403 and 404 views.

## 4. Tests and manual checks

- [ ] Unit-test the auth store: login stores sanitized session; logout clears both localStorage and memory state.
- [ ] Unit-test a route guard: unauthenticated access to `/profile` redirects to login with a redirect parameter; an ordinary user cannot enter an ADMIN route.
- [ ] Component-test registration countdown state and login failure message using mocked API methods.
- [ ] Run frontend tests and production build under the Node version declared by the project.
- [ ] Manually register a student, log in, reload the browser, open `/profile`, log out, and confirm navigating back to `/profile` redirects to login.

## 5. Phase 2 exit checklist

- [ ] Frontend requests use relative `/api` URLs only.
- [ ] Both client validation and backend validation are present.
- [ ] Menu differences follow role, while the backend remains responsible for actual authorization.
- [ ] 401 clears stale credentials reliably.
- [ ] Tests/build pass and changes are committed.
