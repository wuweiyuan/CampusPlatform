# Campus Trade Phase 9: Docker Compose and Nginx Deployment Plan

**Goal:** Deploy the Vue application, Spring Boot API, MySQL, and Redis as a reproducible Docker Compose stack behind Nginx.

**Depends on:** Phase 8 passed; a Linux host with Docker Engine and Docker Compose plugin is available.

**Deliverable:** `docker compose up -d` starts all services, Nginx serves the frontend and proxies `/api`, and persistent database/cache volumes survive container recreation.

## 1. Deployment design

- [ ] Write `deploy/README.md` listing host requirements, ports, volumes, variables, startup, logs, migrations, shutdown, backup, and update steps.
- [ ] Use four services: `mysql`, `redis`, `server`, `nginx`.
- [ ] Only Nginx exposes a host port (recommended `80:80`); MySQL and Redis communicate inside the Compose network and do not publish external ports by default.
- [ ] Create named volumes `mysql_data` and `redis_data`.
- [ ] Store deploy-time values in ignored `deploy/.env`; commit `deploy/.env.example` only.

## 2. Server image

- [ ] Create multi-stage `campus-trade-server/Dockerfile`: Maven/Java builder stage runs the backend package command; lean JRE runtime stage copies only the executable JAR.
- [ ] Run server as a non-root user if base image permits.
- [ ] Use environment variables for datasource URL/user/password, Redis host/password, `JWT_SECRET`, SMTP configuration, `MAIL_MODE=smtp`, and active Spring profile.
- [ ] Add a health endpoint compatible with Compose health checking; the existing `/api/health` is sufficient if it does not require database/Redis for Phase 0, but add a separate readiness endpoint if you need dependency checks.

## 3. Web and Nginx image

- [ ] Create multi-stage frontend build (Node build stage and Nginx runtime stage) or use Nginx build context that copies known `dist/` output. Build using the Node version declared by the Vue project.
- [ ] Configure Nginx to serve Vue history fallback (`try_files ... /index.html`) and cache immutable static assets.
- [ ] Configure `location /api/` to proxy to `http://server:8080/api/`; preserve proxy headers `Host`, `X-Real-IP`, and `X-Forwarded-For`.
- [ ] Make frontend production requests stay relative `/api`; no production CORS configuration should be needed through same-origin Nginx.

## 4. Compose configuration

- [ ] Use MySQL 8 with database/user/password values from environment, initialization/migration strategy documented, persistent volume, and a health check.
- [ ] Use Redis 7 with persistent volume only if desired; configure an explicit password in any non-local deployment and pass it to server.
- [ ] Make server depend on healthy MySQL/Redis where Compose version supports it; application still must retry/check dependencies safely.
- [ ] Add restart policy appropriate for a personal server, such as `unless-stopped`.
- [ ] Do not put real passwords, SMTP authorization codes, or JWT secrets into Compose YAML or Git.

## 5. Linux deployment acceptance

- [ ] Copy only the repository and ignored `.env` configuration to the Linux host; never copy development `node_modules`, `target`, database dumps, or personal editor files.
- [ ] Run `docker compose config` and resolve interpolation/configuration errors before starting services.
- [ ] Run `docker compose up -d --build`, then inspect `docker compose ps` and service logs.
- [ ] Verify `curl -i http://<host>/api/health` returns the documented healthy response.
- [ ] Open the site via Nginx and complete login plus one protected API call; browser network requests must target `<host>/api/...`, not `localhost:8080`.
- [ ] Restart containers with `docker compose down` then `up -d`; verify MySQL data persists through `mysql_data`.
- [ ] Verify Nginx returns Vue 404 route via `index.html`, but unknown API endpoints return backend JSON 404/error rather than frontend HTML.
- [ ] Record the image tags, deployment date, and acceptance results in `deploy/README.md`.

## 6. Phase 9 exit checklist

- [ ] One Compose command starts healthy MySQL, Redis, server, and Nginx services.
- [ ] Only Nginx is externally exposed by default.
- [ ] Frontend and API work at one same-origin URL.
- [ ] Volumes retain intended data through recreation.
- [ ] Secrets are externalized; deployment instructions and verification record are committed.
