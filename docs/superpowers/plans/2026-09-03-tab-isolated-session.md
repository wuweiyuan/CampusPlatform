# 标签页隔离登录会话 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 不同普通浏览器标签页可以独立登录不同账号，且同一标签页刷新后保留会话。

**Architecture:** 保持 `AuthStore` 作为唯一认证状态入口，将其存储读写从跨标签页共享的 `localStorage` 切换为按标签页隔离的 `sessionStorage`。Token 键名、用户对象、Axios 请求拦截器和路由守卫均不变。

**Tech Stack:** Vue 3、Pinia、浏览器 Web Storage API。

**Verification exception:** 用户明确要求不主动运行构建、格式化或自动测试；以浏览器手动验收替代。

---

### Task 1: 隔离认证会话存储

**Files:**
- Modify: `campus-trade-web/src/stores/auth.ts`

- [x] 将 `readStoredUser` 的 `localStorage.getItem`、解析失败时的 `localStorage.removeItem` 分别替换为 `sessionStorage.getItem`、`sessionStorage.removeItem`。
- [x] 将 state 中的 `localStorage.getItem(TOKEN_KEY)` 替换为 `sessionStorage.getItem(TOKEN_KEY)`。
- [x] 在 `setSession` 中将 Token 和用户 JSON 写入 `sessionStorage`；在 `clearSession` 中从 `sessionStorage` 删除两项。键名不变。
- [x] 未修改 `AuthUser`、Pinia state 字段、请求拦截器、401 响应处理或路由守卫。

### Task 2: 浏览器验收

**Files:**
- Modify: `docs/学习清单/阶段-6-今日交接记录-2026-09-01.md`

- [x] 标签页 A 登录管理员并刷新，确认会话仍存在且可访问 `/admin/users`。
- [x] 新标签页 B 打开站点，确认未登录；单独登录普通学生，确认没有管理员菜单且直接访问 `/admin/users` 跳转 `/403`。
- [x] 在 B 退出登录或触发 401，确认 A 的管理员会话仍存在；在 A 退出登录，确认 B 不受影响。
- [x] 未运行构建、格式化或自动测试，结果以浏览器手动验收记录。
