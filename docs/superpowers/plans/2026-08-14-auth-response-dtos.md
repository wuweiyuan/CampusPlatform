# Authentication Response DTOs Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add safe, reusable response DTOs for successful login and current-user API responses.

**Architecture:** `UserInfoResponse` is the single public representation of a user and deliberately excludes persistence-only fields. `LoginResponse` wraps the JWT metadata and one `UserInfoResponse`; both use Lombok `@Data`, matching the user's chosen DTO style.

**Tech Stack:** Java 17, Spring Boot, Lombok, Jackson, JUnit 5.

---

## File structure

- Create `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/auth/dto/UserInfoResponse.java`: public fields safe for API responses.
- Create `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/auth/dto/LoginResponse.java`: JWT metadata plus the public user object.
- Create `campus-trade-server/src/test/java/com/campus/trade/campustradeserver/auth/dto/AuthResponseDtoTests.java`: JSON-shape coverage for the two DTOs.

### Task 1: Add `UserInfoResponse`

**Files:**

- Create: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/auth/dto/UserInfoResponse.java`
- Create: `campus-trade-server/src/test/java/com/campus/trade/campustradeserver/auth/dto/AuthResponseDtoTests.java`

- [ ] **Step 1: Write the failing serialization test**

```java
package com.campus.trade.campustradeserver.auth.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthResponseDtoTests {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void userInfoResponseOnlySerializesPublicFields() throws Exception {
        UserInfoResponse user = new UserInfoResponse();
        user.setId(1L);
        user.setUsername("zhangsan");
        user.setEmail("student@example.com");
        user.setRole("USER");

        String json = objectMapper.writeValueAsString(user);

        assertThat(json).contains("\\\"id\\\":1");
        assertThat(json).contains("\\\"username\\\":\\\"zhangsan\\\"");
        assertThat(json).contains("\\\"email\\\":\\\"student@example.com\\\"");
        assertThat(json).contains("\\\"role\\\":\\\"USER\\\"");
        assertThat(json).doesNotContain("password");
    }
}
```

- [ ] **Step 2: Run the test and confirm it fails because `UserInfoResponse` does not exist**

Run: `./mvnw -Dtest=AuthResponseDtoTests test`

Expected: compilation failure that reports `cannot find symbol: class UserInfoResponse`.

- [ ] **Step 3: Create the public user DTO**

```java
package com.campus.trade.campustradeserver.auth.dto;

import lombok.Data;

@Data
public class UserInfoResponse {

    private Long id;
    private String username;
    private String email;
    private String role;
}
```

- [ ] **Step 4: Run the DTO test and confirm it passes**

Run: `./mvnw -Dtest=AuthResponseDtoTests test`

Expected: `BUILD SUCCESS` and one passing test.

### Task 2: Add `LoginResponse`

**Files:**

- Create: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/auth/dto/LoginResponse.java`
- Modify: `campus-trade-server/src/test/java/com/campus/trade/campustradeserver/auth/dto/AuthResponseDtoTests`

- [ ] **Step 1: Add a failing login-response serialization test**

Append this method inside `AuthResponseDtoTests`:

```java
    @Test
    void loginResponseSerializesJwtMetadataAndPublicUser() throws Exception {
        UserInfoResponse user = new UserInfoResponse();
        user.setId(1L);
        user.setUsername("zhangsan");
        user.setEmail("student@example.com");
        user.setRole("USER");

        LoginResponse response = new LoginResponse();
        response.setToken("jwt-token");
        response.setTokenType("Bearer");
        response.setExpiresIn(7200L);
        response.setUser(user);

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\\\"token\\\":\\\"jwt-token\\\"");
        assertThat(json).contains("\\\"tokenType\\\":\\\"Bearer\\\"");
        assertThat(json).contains("\\\"expiresIn\\\":7200");
        assertThat(json).contains("\\\"user\\\":{");
        assertThat(json).doesNotContain("password");
    }
```

- [ ] **Step 2: Run the test and confirm it fails because `LoginResponse` does not exist**

Run: `./mvnw -Dtest=AuthResponseDtoTests test`

Expected: compilation failure that reports `cannot find symbol: class LoginResponse`.

- [ ] **Step 3: Create the login response DTO**

```java
package com.campus.trade.campustradeserver.auth.dto;

import lombok.Data;

@Data
public class LoginResponse {

    private String token;
    private String tokenType;
    private long expiresIn;
    private UserInfoResponse user;
}
```

- [ ] **Step 4: Run the DTO test and confirm it passes**

Run: `./mvnw -Dtest=AuthResponseDtoTests test`

Expected: `BUILD SUCCESS` and two passing tests.

### Task 3: Verify the response boundary

**Files:**

- Verify: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/auth/dto/UserInfoResponse.java`
- Verify: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/auth/dto/LoginResponse.java`
- Verify: `campus-trade-server/src/test/java/com/campus/trade/campustradeserver/auth/dto/AuthResponseDtoTests.java`

- [ ] **Step 1: Inspect the DTO fields**

Confirm `UserInfoResponse` has only `id`, `username`, `email`, and `role`; confirm `LoginResponse` has only `token`, `tokenType`, `expiresIn`, and `user`.

- [ ] **Step 2: Run the focused test suite**

Run: `./mvnw -Dtest=AuthResponseDtoTests test`

Expected: `BUILD SUCCESS`; the serialized JSON has no `password` property.

- [ ] **Step 3: Do not commit yet**

The Phase 1 learning plan requires committing only after the complete authentication test set passes. Keep these DTO changes as part of the Phase 1 working set.
