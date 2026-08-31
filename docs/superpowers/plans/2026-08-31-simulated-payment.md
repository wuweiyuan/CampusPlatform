# Simulated Payment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a buyer-only simulated-payment endpoint that atomically changes an order to `PAID`, records its payment time, and marks its product `SOLD`.

**Architecture:** Keep state-transition SQL in `OrderMapper` and `ProductMapper`; keep authorization, state checks, and the transaction in `OrderService`; keep HTTP parameter validation and JWT user extraction in `OrderController`. The order update uses its current status in the `WHERE` clause, so concurrent or repeated payments cannot both succeed.

**Tech Stack:** Spring Boot, Spring Security, MyBatis-Plus annotations, MySQL, Postman/manual SQL verification.

---

## File map

- Modify: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/order/mapper/OrderMapper.java` — conditionally update an order and record `paid_at`.
- Modify: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/order/service/OrderService.java` — implement the transactional payment state transition.
- Modify: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/order/controller/OrderController.java` — expose `POST /api/orders/{orderId}/pay`.
- Verify manually: Postman requests and MySQL queries. The user explicitly chose not to add JUnit tests for this learning step.

### Task 1: Add the paid-order conditional update

**Files:**
- Modify: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/order/mapper/OrderMapper.java`

- [ ] **Step 1: Add the Mapper method before the interface's final `}`**

```java
    @Update("""
        UPDATE orders
        SET status = #{targetStatus},
            paid_at = NOW()
        WHERE id = #{orderId}
          AND status = #{expectedStatus}
        """)
    int updateStatusAndPaidAtIfCurrentStatus(
            @Param("orderId") Long orderId,
            @Param("expectedStatus") String expectedStatus,
            @Param("targetStatus") String targetStatus
    );
```

- [ ] **Step 2: Check the intended SQL meaning**

The method must update exactly one row only for an order whose ID matches and whose database status is `PENDING_PAYMENT`. `NOW()` makes MySQL record the server-side payment time.

### Task 2: Add the transactional payment service

**Files:**
- Modify: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/order/service/OrderService.java`

- [ ] **Step 1: Add `payOrder` after `cancelOrder`**

```java
    @Transactional
    public void payOrder(Long buyerId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(4001, "订单不存在");
        }

        if (!order.getBuyerId().equals(buyerId)) {
            throw new AccessDeniedException("无权支付该订单");
        }

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BusinessException(4002, "当前订单状态不允许付款");
        }

        int updatedOrderRows = orderMapper.updateStatusAndPaidAtIfCurrentStatus(
                orderId,
                OrderStatus.PENDING_PAYMENT.getCode(),
                OrderStatus.PAID.getCode()
        );
        if (updatedOrderRows != 1) {
            throw new BusinessException(4002, "当前订单状态不允许付款");
        }

        int updatedProductRows = productMapper.updateStatusIfCurrentStatus(
                order.getProductId(),
                ProductStatus.LOCKED.name(),
                ProductStatus.SOLD.name()
        );
        if (updatedProductRows != 1) {
            throw new BusinessException(4002, "订单关联商品状态异常，付款失败");
        }
    }
```

- [ ] **Step 2: Check rollback behavior**

Confirm the method has `@Transactional`. If the product update affects zero rows, the thrown `BusinessException` is a runtime exception; Spring rolls back the preceding order status and `paid_at` update.

### Task 3: Add the HTTP endpoint

**Files:**
- Modify: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/order/controller/OrderController.java`

- [ ] **Step 1: Add the endpoint after `cancelOrder`**

```java
    @PostMapping("/{orderId}/pay")
    public ApiResponse<Void> payOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        validateOrderId(orderId);
        orderService.payOrder(currentUser.id(), orderId);
        return new ApiResponse<>(0, "付款成功", null);
    }
```

- [ ] **Step 2: Reuse existing validation**

Do not add a second validation method: `validateOrderId(orderId)` already returns business error code `400` for zero or negative IDs.

### Task 4: Manually verify payment behavior

**Files:**
- Verify: Postman and MySQL

- [ ] **Step 1: Create a fresh pending-payment order**

Use buyer A to call `POST /api/orders` for an `ON_SALE` product. Before payment, confirm:

```sql
SELECT id, status, paid_at FROM orders WHERE id = <orderId>;
SELECT id, status FROM product WHERE id = <productId>;
```

Expected: order is `PENDING_PAYMENT` with `paid_at` NULL; product is `LOCKED`.

- [ ] **Step 2: Verify successful payment**

```http
POST /api/orders/<orderId>/pay
Authorization: Bearer <buyer-A-token>
```

Expected JSON: `code` is `0` and `message` is `付款成功`. Query the same rows again: order is `PAID` with non-NULL `paid_at`; product is `SOLD`.

- [ ] **Step 3: Verify state conflicts**

Repeat the payment request, then make a payment request for the earlier cancelled order. Each request must return business code `4002`, and neither order/product pair may change.

- [ ] **Step 4: Verify authorization**

Use the seller token and buyer B token to request payment for buyer A's pending-payment order. Each request must return HTTP 403, and the order stays `PENDING_PAYMENT` while its product stays `LOCKED`.

## Verification note

The workspace terminal currently uses JDK 8, while this project requires Java 17 language features. Run `./mvnw test` only after the terminal's Java version reports 17 or newer; otherwise its failure is unrelated to the payment change.
