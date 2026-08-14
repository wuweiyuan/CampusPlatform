# Authentication response DTO design

## Purpose

Define the public user data returned by authentication endpoints without exposing database-only fields.

## Classes

### `UserInfoResponse`

Located in `auth.dto`. Reusable public user representation for both login and the current-user endpoint.

Fields:

- `id` (`Long`)
- `username` (`String`)
- `email` (`String`)
- `role` (`String`)

It must never contain `password`, `status`, or other internal account fields.

### `LoginResponse`

Located in `auth.dto`. Represents the data returned after a successful login.

Fields:

- `token` (`String`)
- `tokenType` (`String`), initially `Bearer`
- `expiresIn` (`long`), in seconds
- `user` (`UserInfoResponse`)

## Data flow

`AuthService` will authenticate the username and password, create a JWT, then assemble a `LoginResponse`. The controller returns it inside the existing `ApiResponse` envelope.

## Boundary

`SysUser` remains the database entity. It is never returned directly to the client. `UserInfoResponse` is the explicit mapping boundary between persistence and the API.

## Validation and testing

These response DTOs do not validate client input. Tests for the login endpoint will verify the exact public fields and confirm that no password hash appears in the response.
