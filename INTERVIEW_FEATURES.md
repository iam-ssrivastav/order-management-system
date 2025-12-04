# 🎓 Top 5 Backend Features to Ace System Design Interviews

These features are specifically chosen because they solve **hard distributed systems problems**. Implementing any of these demonstrates "Senior Engineer" thinking to an interviewer.

---

## 1. 🔒 Distributed Locking (The "Overselling" Problem)
**The Interview Question**: *"How do you prevent two users from buying the last iPhone at the exact same millisecond?"*

*   **Feature**: Implement a Distributed Lock using **Redis (Redisson)** around the inventory deduction logic.
*   **Why it impresses**: Shows you understand **Race Conditions** in a clustered environment. A simple Java `synchronized` block doesn't work when you have multiple instances of the Inventory Service.
*   **Tech**: Redis, Redisson, Spring AOP (custom annotation `@DistributedLock`).

## 2. 🆔 Idempotency (The "Double Payment" Problem)
**The Interview Question**: *"What happens if the client sends a payment request, the server charges the card, but the network fails before the response reaches the client? The client retries..."*

*   **Feature**: Implement an Idempotency Key mechanism.
*   **Logic**:
    1.  Client sends `Idempotency-Key: uuid-123` header.
    2.  Server checks Redis: "Have I seen uuid-123?"
    3.  If yes, return the *cached response* (don't charge again).
    4.  If no, process and save response.
*   **Why it impresses**: Shows you understand **Fault Tolerance** and **Data Consistency**.

## 3. 📦 The Transactional Outbox Pattern (The "Dual Write" Problem)
**The Interview Question**: *"You save the order to the DB, but Kafka goes down before you publish the event. Now your DB has the order, but the Payment Service never knows. How do you fix this?"*

*   **Feature**: Implement the Outbox Pattern (or use Debezium).
*   **Logic**:
    1.  Save Order AND save an `OutboxEvent` to the *same database transaction*.
    2.  A separate background process (or Debezium connector) reads the `Outbox` table and pushes to Kafka.
*   **Why it impresses**: This is the **Gold Standard** for data consistency in microservices. It solves the "Dual Write" problem.

## 4. 🚦 Rate Limiting (The "Noisy Neighbor" Problem)
**The Interview Question**: *"How do you protect your system from a script sending 10,000 requests per second?"*

*   **Feature**: Implement Token Bucket Rate Limiting at the API Gateway.
*   **Tech**: **Bucket4j** with Redis.
*   **Logic**: Allow 10 requests/second per API Key. Return `429 Too Many Requests` if exceeded.
*   **Why it impresses**: Shows you care about **System Stability** and **Security** (DDoS protection).

## 5. ⚡ gRPC for Internal Communication (The "Latency" Problem)
**The Interview Question**: *"Your microservices are chatting a lot and JSON serialization is slow. How do you optimize?"*

*   **Feature**: Replace internal REST calls (e.g., Order -> Inventory) with **gRPC (Protobuf)**.
*   **Why it impresses**: Shows you know about **Performance Optimization** and different communication protocols. gRPC is much faster and lighter than REST/JSON.

## 6. 🛡️ Role-Based Access Control - RBAC (The "Authorization" Problem)
**The Interview Question**: *"You have JWT authentication, but how do you prevent a regular user from deleting all orders? What's the difference between Authentication and Authorization?"*

*   **Feature**: Implement Role-Based Access Control (RBAC).
*   **Logic**:
    1.  JWT token includes `roles: ["USER"]` or `roles: ["ADMIN"]`.
    2.  API Gateway extracts roles from token and adds to request header.
    3.  Each service checks: `@PreAuthorize("hasRole('ADMIN')")` on sensitive endpoints.
    4.  Example: Only `ADMIN` can call `DELETE /api/orders/{id}`.
*   **Why it impresses**: Shows you understand the critical difference between **Authentication** (who you are) and **Authorization** (what you can do). This is a common interview trap question.
*   **Tech**: Spring Security, JWT Claims, Method-level security annotations.

---

### 💡 My Advice
If you only pick one, pick **#2 (Idempotency)** or **#3 (Outbox Pattern)**. These are the most common "gotcha" questions in backend system design interviews.

For security-focused interviews, be ready to explain **#6 (RBAC)** - it's the difference between a junior and senior understanding of security.
