# Health Check Page Design

## Scope

Add the frontend foundations needed to show the backend health endpoint at the root route. Install only `element-plus`, `axios`, and `vue-router`; do not add Vitest, jsdom, or `@vue/test-utils`.

## Architecture

`src/api/http.js` exports a shared Axios instance with a `/api` base URL and a 10-second timeout. The Vite development server proxies requests starting with `/api` to `http://localhost:8080` without rewriting the path.

Vue Router maps `/` to `HealthView.vue`. When the view mounts, it calls `http.get('/health')` and renders the returned response data as `后端状态：<response>`. If the request fails, it renders `后端不可用`.

`main.js` installs Element Plus and the router, while `App.vue` contains only the router outlet.

## Error Handling and Verification

The page has a loading state until the health request resolves. Successful response values are rendered as strings; object or array responses are serialized for display. Failures have a stable Chinese fallback message. Since the requested scope explicitly excludes test tooling, verification is a production build plus a development-server request against the proxy when the backend is available.
