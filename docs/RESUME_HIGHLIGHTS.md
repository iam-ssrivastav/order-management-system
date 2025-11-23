# Resume Highlights & Interview Guide

Use this project to demonstrate your expertise in **Distributed Systems** and **Modern Java Backend Development**.

## Key Concepts to Mention

### 1. Event-Driven Architecture (EDA)
**What to say:** "I implemented an event-driven architecture using **Apache Kafka** to decouple the Order and Inventory services. When an order is placed, an event is published asynchronously, allowing the Inventory service to update stock without blocking the user response. This ensures high availability and scalability."

### 2. Microservices Patterns
**What to say:** "I used the **API Gateway pattern** with Spring Cloud Gateway to centralize authentication and routing. I also implemented the **Circuit Breaker pattern** using Resilience4j to handle failures gracefully when dependent services are down, preventing cascading failures."

### 3. Database & Caching
**What to say:** "I used **PostgreSQL** for persistent storage with **Flyway** for version-controlled database migrations. To optimize read performance, I integrated **Redis** as a distributed cache for frequently accessed order data."

### 4. Observability
**What to say:** "In a distributed system, debugging is hard. I set up a full observability stack using **Prometheus** for metrics, **Grafana** for visualization, and **Zipkin** for distributed tracing to track requests across microservices."

## Technical Deep Dive Questions

**Q: How do you handle distributed transactions?**
A: "I used the **Saga Pattern** (Choreography approach) via Kafka events. Instead of a two-phase commit, services react to events to maintain eventual consistency."

**Q: How do you secure the microservices?**
A: "I implemented **JWT (JSON Web Token)** authentication at the API Gateway level. The gateway validates the token before forwarding the request to internal services."

**Q: How do you handle database schema changes?**
A: "I used **Flyway** to manage database migrations, ensuring that schema changes are versioned and applied consistently across all environments."
