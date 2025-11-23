# System Architecture

## Overview

The system follows a **Microservices Architecture** where each domain is isolated in its own service. Communication is primarily **asynchronous** via Kafka, with some synchronous REST calls for queries.

## Components

### 1. API Gateway (Spring Cloud Gateway)
- **Role**: Entry point for all external traffic.
- **Features**: 
  - JWT Token Validation
  - Request Routing
  - Rate Limiting (Redis)
  - CORS Configuration

### 2. Order Service (Producer)
- **Role**: Handles order lifecycle.
- **Database**: PostgreSQL (`orders` table).
- **Cache**: Redis (`orders` key).
- **Kafka**: Publishes `OrderCreatedEvent` to `order-events` topic.
- **Resilience**: Circuit Breaker for calls to Inventory Service (if any sync calls exist).

### 3. Inventory Service (Consumer)
- **Role**: Manages product stock.
- **Database**: PostgreSQL (`inventory` table).
- **Kafka**: Consumes `OrderCreatedEvent` from `order-events` topic.
- **Concurrency**: Optimistic Locking (`@Version`) to prevent race conditions during stock updates.

### 4. Notification Service (Consumer)
- **Role**: Sends alerts to users.
- **Kafka**: Consumes `OrderCreatedEvent`.
- **Async**: Uses `@Async` for non-blocking email simulation.

## Data Flow

1. **User** sends `POST /api/orders` to **API Gateway**.
2. **Gateway** validates JWT and routes to **Order Service**.
3. **Order Service** saves order to DB (status: CREATED).
4. **Order Service** publishes event to **Kafka**.
5. **Order Service** returns `201 Created` to User immediately.
6. **Inventory Service** consumes event -> Deducts stock.
7. **Notification Service** consumes event -> Sends email.

## Infrastructure

- **Docker Compose**: Orchestrates all containers.
- **Zookeeper**: Manages Kafka brokers.
- **Zipkin**: Collects trace spans.
- **Prometheus**: Scrapes metrics from `/actuator/prometheus`.
