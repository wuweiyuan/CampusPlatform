# Campus Trade Phase 0: Initialization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create independently runnable Spring Boot and Vue 3 applications that can communicate through a versioned health endpoint.

**Architecture:** Use two sibling applications: `campus-trade-server` exposes REST endpoints under `/api`, and `campus-trade-web` calls the backend only through the Vite development proxy. Phase 0 contains no user, product, JWT, Redis business, or Docker code; it establishes only conventions that later stages depend on.

**Tech Stack:** Java 17, Maven, Spring Boot 3.x, Spring Web, Validation, Lombok, Vue 3, Vite, Element Plus, Axios, Vue Router, Vitest, MySQL 8, Redis 7.

---

## File structure decided in this phase

```text
CampusPlatform/
├── .gitignore
├── README.md
├── campus-trade-server/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/campus/trade/
│       │   ├── CampusTradeApplication.java
│       │   └── common/
│       │       ├── api/ApiResponse.java
│       │       ├── exception/BusinessException.java
│       │       ├── exception/GlobalExceptionHandler.java
│       │       └── web/HealthController.java
│       ├── main/resources/application.yml
│       └── test/java/com/campus/trade/common/web/HealthControllerTest.java
├── campus-trade-web/
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── api/http.js
│       ├── router/index.js
│       ├── views/HealthView.vue
│       ├── App.vue
│       └── main.js
└── docs/
    └── api/phase-0.md
```

`ApiResponse<T>` is the only Phase 0 response envelope. It contains `code` (integer), `message` (string), and `data` (generic payload). `HealthController` owns only `GET /api/health`; later modules must not put business methods in this controller.

### Task 1: Initialize repository and document local prerequisites

**Files:**
- Create: `.gitignore`
- Create: `README.md`
- Create: `docs/api/phase-0.md`

- [ ] **Step 1: Verify tool versions before creating projects**

Run:

```bash
java -version
mvn -version
node -v
npm -v
git --version
docker --version
```

Expected: Java major version is 17 or newer; Maven, Node, npm, Git are available. Docker may be absent at this stage, but record that fact in `README.md` rather than installing it now.

- [ ] **Step 2: Create a Git repository and ignore generated/secrets files**

Run:

```bash
git init
```

Create `.gitignore` with these entries:

```gitignore
# Java
campus-trade-server/target/
*.class

# Node/Vite
campus-trade-web/node_modules/
campus-trade-web/dist/

# IDE and operating system
.idea/
.vscode/
*.iml
.DS_Store

# Local secrets and runtime files
.env
.env.*
!.env.example
*.log
.superpowers/
```

- [ ] **Step 3: Write the minimum README**

Create `README.md` containing: project purpose, the Phase 0 prerequisite versions found in Step 1, the two application directories, and empty placeholders replaced with the actual commands that start each app after Tasks 2 and 4. State that SMTP credentials, database passwords, and JWT secrets must stay in local environment files.

- [ ] **Step 4: Define the Phase 0 API contract before implementing it**

Create `docs/api/phase-0.md`:

```markdown
# Phase 0 API

## GET /api/health

No authentication is required.

Successful response:

```json
{
  "code": 0,
  "message": "ok",
  "data": { "status": "UP" }
}
```
```

- [ ] **Step 5: Commit the project skeleton documentation**

Run:

```bash
git add .gitignore README.md docs/api/phase-0.md
git commit -m "docs: initialize campus trade project"
```

Expected: Git reports one new commit.

### Task 2: Create a tested Spring Boot health API

**Files:**
- Create: `campus-trade-server/pom.xml`
- Create: `campus-trade-server/src/main/java/com/campus/trade/CampusTradeApplication.java`
- Create: `campus-trade-server/src/main/java/com/campus/trade/common/api/ApiResponse.java`
- Create: `campus-trade-server/src/main/java/com/campus/trade/common/web/HealthController.java`
- Create: `campus-trade-server/src/test/java/com/campus/trade/common/web/HealthControllerTest.java`

- [ ] **Step 1: Generate the Maven project**

Use Spring Initializr with group `com.campus`, artifact `campus-trade-server`, package `com.campus.trade`, Java 17, Maven, and Spring Boot 3.x. Select `Spring Web`, `Validation`, `Lombok`, and `Spring Boot Test`. Put the generated project in `campus-trade-server/`.

- [ ] **Step 2: Write the failing MVC test first**

In `HealthControllerTest`, use `@WebMvcTest(HealthController.class)` and `MockMvc` to assert this exact contract:

```java
mockMvc.perform(get("/api/health"))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.code").value(0))
    .andExpect(jsonPath("$.message").value("ok"))
    .andExpect(jsonPath("$.data.status").value("UP"));
```

- [ ] **Step 3: Run the test and confirm the expected failure**

Run:

```bash
./mvnw test -Dtest=HealthControllerTest
```

Expected: failure because `HealthController` does not yet exist or does not map `/api/health`.

- [ ] **Step 4: Implement the smallest response model and controller**

Implement `ApiResponse<T>` with fields `Integer code`, `String message`, `T data`, plus a static `success(T data)` factory that always returns code `0` and message `"ok"`. Implement `HealthController` as `@RestController`, with `@RequestMapping("/api")`, and a `GET /health` method returning `ApiResponse.success(Map.of("status", "UP"))`. Do not add a database, Redis, authentication, or extra endpoint.

- [ ] **Step 5: Run the backend test again**

Run:

```bash
./mvnw test -Dtest=HealthControllerTest
```

Expected: `BUILD SUCCESS` and one passing test.

- [ ] **Step 6: Commit the health API**

Run:

```bash
git add campus-trade-server
git commit -m "feat(server): add health endpoint"
```

### Task 3: Add error and configuration conventions without business dependencies

**Files:**
- Create: `campus-trade-server/src/main/java/com/campus/trade/common/exception/BusinessException.java`
- Create: `campus-trade-server/src/main/java/com/campus/trade/common/exception/GlobalExceptionHandler.java`
- Create: `campus-trade-server/src/main/resources/application.yml`
- Modify: `campus-trade-server/src/test/java/com/campus/trade/common/web/HealthControllerTest.java`

- [ ] **Step 1: Add a failing test for a malformed request only if you expose a test endpoint**

Do not create a fake business endpoint purely for error handling. Instead, keep this task documentation-led: add a short `Error response convention` section to `docs/api/phase-0.md` declaring that later validation and business errors use the same `code/message/data` envelope, with `data` equal to `null`.

- [ ] **Step 2: Implement shared exception types**

Create `BusinessException` extending `RuntimeException` with an integer `code`. Create `GlobalExceptionHandler` annotated with `@RestControllerAdvice`. Its handler for `BusinessException` returns `ResponseEntity.badRequest()` with `ApiResponse` containing the exception code, exception message, and `null` data. Its handler for validation errors returns HTTP 400, code `400`, message `"请求参数不合法"`, data `null`.

- [ ] **Step 3: Create a safe local configuration file**

Set `server.port: 8080`, `spring.application.name: campus-trade-server`, and `spring.jackson.default-property-inclusion: non_null` in `application.yml`. Do not add a datasource, Redis, SMTP password, or JWT secret in Phase 0. Those start in Phase 1 and must use environment variables or an ignored local profile.

- [ ] **Step 4: Re-run all backend tests**

Run:

```bash
./mvnw test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 5: Commit shared conventions**

Run:

```bash
git add campus-trade-server docs/api/phase-0.md
git commit -m "feat(server): add response and error conventions"
```

### Task 4: Create a Vue health page and development proxy

**Files:**
- Create: `campus-trade-web/` from Vite Vue template
- Create: `campus-trade-web/src/api/http.js`
- Create: `campus-trade-web/src/router/index.js`
- Create: `campus-trade-web/src/views/HealthView.vue`
- Modify: `campus-trade-web/src/main.js`
- Modify: `campus-trade-web/src/App.vue`
- Modify: `campus-trade-web/vite.config.js`

- [ ] **Step 1: Identify the Node version declared by the frontend project before running npm commands**

After generating the project, check `.nvmrc`, `.node-version`, `package.json` `engines`, and `volta` fields. If none exists, record the Node version used in `README.md`; do not assume Codex's Node version is the required project version.

- [ ] **Step 2: Scaffold Vue 3 and install only Phase 0 dependencies**

Run from the repository root:

```bash
npm create vite@latest campus-trade-web -- --template vue
cd campus-trade-web
npm install
npm install element-plus axios vue-router
npm install -D vitest jsdom @vue/test-utils
```

- [ ] **Step 3: Write the failing component test**

Create `src/views/HealthView.spec.js`. Mock the HTTP client so its `get('/health')` resolves to `{ data: { code: 0, message: 'ok', data: { status: 'UP' } } }`. Mount `HealthView`, wait for promises to settle, and assert the rendered text contains `后端状态：UP`.

- [ ] **Step 4: Run the frontend test and confirm it fails**

Add a `test` script invoking `vitest run` to `package.json`, then run:

```bash
npm test
```

Expected: failure because `HealthView` and its HTTP client do not exist.

- [ ] **Step 5: Implement the minimal web integration**

Create `src/api/http.js` as an Axios instance with `baseURL: '/api'` and a 10-second timeout. Configure Vite's development server proxy so requests matching `/api` target `http://localhost:8080` and remove the `/api` prefix only if your backend controller does not already include it; because this plan's backend does include `/api`, preserve the prefix. Create one route `/` pointing to `HealthView`. The view calls `http.get('/health')` on mount and renders `后端状态：UP` from `response.data.data.status`; it renders `后端不可用` for a failed request. Register Element Plus in `main.js`, and render `<RouterView />` from `App.vue`.

- [ ] **Step 6: Make the component test pass and build the app**

Run:

```bash
npm test
npm run build
```

Expected: Vitest passes and Vite creates `dist/` without errors.

- [ ] **Step 7: Commit the web skeleton**

Run:

```bash
git add campus-trade-web README.md
git commit -m "feat(web): add health page and api client"
```

### Task 5: Perform the end-to-end Phase 0 acceptance check

**Files:**
- Modify: `README.md`
- Modify: `docs/api/phase-0.md`

- [ ] **Step 1: Start the backend in one terminal**

Run from `campus-trade-server/`:

```bash
./mvnw spring-boot:run
```

Expected: a startup log reporting port `8080`.

- [ ] **Step 2: Verify the backend contract directly**

Run from a second terminal:

```bash
curl -i http://localhost:8080/api/health
```

Expected: HTTP 200 and the JSON body defined in `docs/api/phase-0.md`.

- [ ] **Step 3: Start the frontend in a third terminal**

Run from `campus-trade-web/`:

```bash
npm run dev
```

Expected: Vite prints a local URL, normally `http://localhost:5173`.

- [ ] **Step 4: Verify the proxy in the browser**

Open Vite's URL. Expected text: `后端状态：UP`. In browser developer tools, the health request URL begins with the frontend origin and `/api/health`; it must not be a hard-coded `localhost:8080` URL in application code.

- [ ] **Step 5: Record the actual startup commands and result**

Replace temporary text in `README.md` with the exact backend and frontend commands that worked. Add an `Acceptance` section to `docs/api/phase-0.md` recording the date, the curl result, and whether the browser page showed `后端状态：UP`.

- [ ] **Step 6: Commit the acceptance record**

Run:

```bash
git add README.md docs/api/phase-0.md
git commit -m "docs: record phase zero acceptance"
```

## Phase 0 exit checklist

- [ ] `GET /api/health` returns HTTP 200 and `{ "code": 0, "message": "ok", "data": { "status": "UP" } }`.
- [ ] `./mvnw test` passes in `campus-trade-server/`.
- [ ] `npm test` and `npm run build` pass in `campus-trade-web/`.
- [ ] The Vue page gets its health result through `/api/health` and the Vite proxy.
- [ ] Generated files and secret-bearing `.env` files are ignored by Git.
- [ ] No authentication, database schema, Redis cache, email, business table, Docker, or Nginx configuration has been added yet.
