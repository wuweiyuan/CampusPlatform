# Campus Trade Phase 1: Authentication Implementation Plan

**Goal:** Build email-code registration, username/password login, JWT authentication, and logout blacklisting.

**Depends on:** Phase 0 passed; MySQL and Redis are running locally.

**Deliverable:** A user can obtain an email code, register, log in, call `/api/auth/me` with a JWT, and log out so the same JWT is rejected.

## 1. Before coding

- [ ] Create local MySQL database `campus_trade` with UTF-8 encoding.
- [ ] Start Redis and record its host/port in an ignored local profile or environment variables.
- [ ] Add dependencies: MyBatis Plus, MySQL connector, Spring Data Redis, Spring Security Crypto, JWT library, Spring Mail, and a database migration solution such as Flyway.
- [ ] Keep SMTP host, username, authorization code, database password, JWT secret, and JWT lifetime outside Git. Create `.env.example` with names only: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `REDIS_HOST`, `REDIS_PORT`, `JWT_SECRET`, `JWT_EXPIRE_SECONDS`, `MAIL_MODE`.
- [ ] Decide and record the Phase 1 API prefix `/api/auth` in `docs/api/phase-1.md`.

## 2. Database migration

- [ ] Create migration `V1__create_sys_user.sql`.
- [ ] Create table `sys_user` with: bigint `id`, varchar(32) `username`, varchar(128) `email`, varchar(100) `password`, varchar(16) `role`, tinyint `status`, tinyint `email_verified`, datetime `created_at`, datetime `updated_at`.
- [ ] Add unique indexes for `username` and `email`; default `role` is `USER`, default `status` is enabled, and default `email_verified` is false.
- [ ] Add an initial disabled or documented administrator creation method; do not expose public registration with role `ADMIN`.
- [ ] Run migrations against an empty database and inspect the actual table definition.

## 3. Backend API contract

Document these endpoints and exact request/response fields in `docs/api/phase-1.md` before implementation.

| Method and path | Request | Success | Auth |
|---|---|---|---|
| `POST /api/auth/email-code` | `email` | no data | no |
| `POST /api/auth/register` | `username,email,password,code` | user id/username/email | no |
| `POST /api/auth/login` | `username,password` | `token,expiresIn,user` | no |
| `GET /api/auth/me` | none | id/username/email/role | JWT |
| `POST /api/auth/logout` | none | no data | JWT |

- [ ] Define validation: username 3–32 characters; password 8–64 characters; email uses a valid email format; code is exactly six digits.
- [ ] Use error codes consistently: `400` parameter error, `1001` code invalid/expired, `1002` username/email exists, `1003` account disabled, `1004` credentials invalid, `401` not logged in, `403` forbidden.
- [ ] Add a send-code cooldown key `auth:email:cooldown:{email}` with 60-second TTL, and verification key `auth:email:code:{email}` with 5-minute TTL.

## 4. Backend implementation checklist

- [ ] Create `auth` package with DTOs, `SysUser` entity, `SysUserMapper`, `AuthService`, `AuthController`, `JwtService`, `JwtAuthenticationFilter`, and `EmailCodeService`; keep each class focused on one responsibility.
- [ ] Write Mapper tests or an integration test proving unique username/email behavior and successful user insertion.
- [ ] Implement a six-digit code generator using a cryptographically suitable random source; never log passwords or JWT secrets.
- [ ] Implement mail abstraction: `MAIL_MODE=log` writes only the code and recipient to application log in development; `MAIL_MODE=smtp` uses configured SMTP sender in deployment.
- [ ] Write tests for: successful send code; cooldown rejection; correct code registration; expired/wrong code rejection; duplicate username/email; BCrypt password is not plain text.
- [ ] Implement registration as a transaction: verify code, create BCrypt password hash, insert user, then delete the verification key only after successful insert.
- [ ] Implement login to reject disabled accounts and wrong password with the same outward-facing credentials error.
- [ ] JWT payload must include `sub` as user ID, `username`, `role`, `jti`, `iat`, and `exp`. Sign with `JWT_SECRET`; validate expiration and signature on every protected request.
- [ ] Implement `/me` by reading the authenticated principal, not an ID supplied by the client.
- [ ] Implement logout by placing the `jti` in `auth:token:blacklist:{jti}` with remaining token TTL; requests with blacklisted `jti` return 401.
- [ ] Extend global exception mapping for validation, duplicate-key errors, authentication failure, and authorization failure using the common response envelope.

## 5. Backend verification

- [ ] Run the full backend test suite.
- [ ] In Apifox/Postman, save: send code; register; login; `/me` with token; `/me` without token; logout; `/me` with old token; duplicate registration; wrong code; invalid password.
- [ ] Confirm the normal path returns 200, unauthenticated returns 401, and no user password hash appears in any response.
- [ ] Commit only when the above test set passes.

## 6. Phase 1 exit checklist

- [ ] `sys_user` is migration-managed and has unique username/email constraints.
- [ ] Development mail mode prints a usable code; SMTP settings are optional and externalized.
- [ ] User registration requires a non-expired correct code.
- [ ] Passwords are BCrypt hashes.
- [ ] JWT authenticates `/me`, and logout invalidates the exact current token.
- [ ] API documentation, test evidence, and a Git commit exist.
