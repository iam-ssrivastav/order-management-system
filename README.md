# Enterprise Order Management System

## About

A production-ready microservices-based order management system built with Spring Boot, demonstrating modern backend architecture patterns and best practices.

> **Part of a portfolio showcasing event‑driven architectures** – also check out [CivicPulse](https://github.com/iam-ssrivastav/civic-pulse), an AI‑powered smart‑city system using the same Kafka + Spring Boot stack with local LLM integration.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-7.5.0-black)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED)

## 🏗️ Architecture Overview

This project implements a **microservices architecture** with event-driven communication using Apache Kafka, demonstrating enterprise-level design patterns and best practices.

### System Architecture

![System Architecture](architecture-diagram.png)

*Complete system architecture showing all microservices, Kafka event flow, PostgreSQL with Outbox Pattern, Redis caching, and observability stack (Prometheus, Zipkin).*

### Component Flow

```mermaid
graph TB
    Client[Client Browser]
    Frontend[Frontend<br/>Nginx :8081]
    Gateway[API Gateway<br/>Spring Cloud :8080]
    
    Order[Order Service<br/>:8082]
    Inventory[Inventory Service<br/>:8083]
    Notification[Notification Service<br/>:8084]
    Payment[Payment Service<br/>:8085]
    
    %% Outbox Components
    OrderOutbox[(Order Outbox<br/>order_outbox_events)]
    PaymentOutbox[(Payment Outbox<br/>payment_outbox_events)]
    InventoryOutbox[(Inventory Outbox<br/>inventory_outbox_events)]
    NotificationOutbox[(Notification Outbox<br/>notification_outbox_events)]
    
    OrderScheduler[Order Scheduler<br/>@Scheduled]
    PaymentScheduler[Payment Scheduler<br/>@Scheduled]
    InventoryScheduler[Inventory Scheduler<br/>@Scheduled]
    NotificationScheduler[Notification Scheduler<br/>@Scheduled]
    
    Kafka[Apache Kafka<br/>Event Bus :19092]
    OrdersTopic[orders topic]
    NotificationsTopic[notifications topic]
    CancellationTopic[order-cancellations topic]
    
    DB[(PostgreSQL<br/>:5435)]
    Cache[(Redis<br/>:6381)]
    Trace[Zipkin<br/>:9412]
    Metrics[Prometheus<br/>:9990]
    
    Client --> Frontend
    Frontend --> Gateway
    Gateway --> Order
    Gateway --> Inventory
    Gateway --> Notification
    Gateway --> Payment
    
    %% Outbox Pattern Flow
    Order --> DB
    Order --> Cache
    Order --> OrderOutbox
    OrderOutbox --> OrderScheduler
    OrderScheduler --> Kafka
    
    Payment --> DB
    Payment --> PaymentOutbox
    PaymentOutbox --> PaymentScheduler
    PaymentScheduler --> Kafka
    
    Inventory --> DB
    Inventory --> InventoryOutbox
    InventoryOutbox --> InventoryScheduler
    InventoryScheduler --> Kafka
    
    Notification --> NotificationOutbox
    NotificationOutbox --> NotificationScheduler
    NotificationScheduler --> Kafka
    
    %% Kafka Topics
    Kafka --> OrdersTopic
    Kafka --> NotificationsTopic
    Kafka --> CancellationTopic
    
    %% Event Flow
    OrdersTopic -.->|ORDER_CREATED| Payment
    OrdersTopic -.->|ORDER_CREATED| Inventory
    OrdersTopic -.->|ORDER_CREATED| Notification
    CancellationTopic -.->|ORDER_CANCELLED| Payment
    CancellationTopic -.->|ORDER_CANCELLED| Inventory
    
    %% Observability Connections
    Order -.-> Trace
    Inventory -.-> Trace
    Notification -.-> Trace
    Payment -.-> Trace
    Gateway -.-> Trace
    
    Order -.-> Metrics
    Inventory -.-> Metrics
    Notification -.-> Metrics
    Payment -.-> Metrics
    Gateway -.-> Metrics
    
    %% Styling
    style Order fill:#4A90E2
    style Inventory fill:#4A90E2
    style Notification fill:#4A90E2
    style Payment fill:#4A90E2
    style Gateway fill:#7B68EE
    style Kafka fill:#FF6B6B
    style OrdersTopic fill:#FFB6C1
    style NotificationsTopic fill:#FFB6C1
    style CancellationTopic fill:#FFB6C1
    style DB fill:#50C878
    style Cache fill:#FFB347
    style Trace fill:#9370DB
    style Metrics fill:#20B2AA
    style OrderOutbox fill:#98D8C8
    style PaymentOutbox fill:#98D8C8
    style InventoryOutbox fill:#98D8C8
    style NotificationOutbox fill:#98D8C8
    style OrderScheduler fill:#F7DC6F
    style PaymentScheduler fill:#F7DC6F
    style InventoryScheduler fill:#F7DC6F
    style NotificationScheduler fill:#F7DC6F
```


## ✨ Key Features

### Microservices Architecture
- **API Gateway**: Centralized entry point with routing and authentication
- **Order Service**: Order lifecycle management with circuit breaker pattern and cancellation support
- **Payment Service**: Mock payment processing with 80/20 success/failure simulation and refund capabilities
- **Inventory Service**: Stock management with event-driven updates and restoration
- **Notification Service**: Asynchronous notification processing

### Event-Driven Communication
### Distributed Patterns
- **Event-Driven Architecture**: Kafka-based asynchronous communication
- **Saga Pattern**: Choreography-based distributed transactions
  - Forward transactions: Order → Payment → Inventory
  - Compensating transactions: Cancel → Refund + Restore Stock
- **CQRS**: Separate read/write paths with caching
- **API Gateway Pattern**: Single entry point with routing
### Design Patterns & Best Practices
- ✅ **Circuit Breaker** (Resilience4j) - Fault tolerance
- ✅ **Repository Pattern** - Data access abstraction
- ✅ **DTO Pattern** - Data transfer objects
- ✅ **Builder Pattern** - Object construction

### Security
- 🔐 **JWT Authentication** - Token-based security
- 🔐 **CORS Configuration** - Cross-origin resource sharing
- 🔐 **Password Encryption** - Secure credential storage

### Data Management
- 💾 **PostgreSQL** - Relational database for persistence
- 💾 **Redis** - Caching layer for performance
- 💾 **Flyway** - Database migration management

### Monitoring & Observability
- 📊 **Prometheus** - Metrics collection
- 📊 **Grafana** - Metrics visualization
- 📊 **Zipkin** - Distributed tracing
- 📊 **Spring Actuator** - Health checks and metrics

### Frontend
- 🎨 **Modern UI** - Dark theme with glassmorphic design
- 🎨 **Responsive** - Mobile-friendly interface
- 🎨 **Real-time Updates** - Dynamic order management

## 🚀 Quick Start

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- Git

### 1. Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/order-management-system.git
cd order-management-system
```

### 2. Start Infrastructure (Docker)

```bash
docker-compose up -d
```

This starts:
- Kafka & Zookeeper
- PostgreSQL
- Redis
- Prometheus, Grafana, Zipkin
- Frontend (Nginx)

### 3. Build All Services

```bash
mvn clean package -DskipTests
```

### 4. Start Microservices

Open 4 terminal windows and run:

```bash
# Terminal 1 - API Gateway
cd api-gateway
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=8080"

# Terminal 2 - Order Service
cd order-service
mvn spring-boot:run

# Terminal 3 - Inventory Service
cd inventory-service
mvn spring-boot:run

# Terminal 4 - Notification Service
cd notification-service
mvn spring-boot:run
```

### 5. Verify System

Run the end-to-end test script:

```bash
./verify-all-apis.sh
```
```

Expected output:
```
✓ API Gateway (port 8080): UP
✓ Order Service (port 8082): UP
✓ Inventory Service (port 8083): UP
✓ Notification Service (port 8084): UP
```

## 🌐 Access Points

| Service | URL | Credentials |
|---------|-----|-------------|
| **Frontend** | http://localhost:8081 | Any username |
| **API Gateway** | http://localhost:8080 | - |
| **Prometheus** | http://localhost:9990 | - |
| **Grafana** | http://localhost:3000 | admin/admin |
| **Zipkin** | http://localhost:9412 | - |

## 📖 API Documentation

### 📚 Centralized Swagger UI
Access all API documentation in one place:
**[http://localhost:8080/webjars/swagger-ui/index.html](http://localhost:8080/webjars/swagger-ui/index.html)**

Use the dropdown in the top-right to switch between services.

### Authentication

### Authentication

**Login**
```bash
curl -X POST "http://localhost:8080/auth/login?username=testuser"
```

Response:
```json
"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTcwMDAwMDAwMCwiZXhwIjoxNzAwMDAzNjAwfQ..."
```

### Orders

**Create Order**
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "productId": "LAPTOP-001",
    "quantity": 2,
    "price": 1200.00,
    "customerId": "testuser"
  }'
```

**Get Order by ID**
```bash
curl -X GET http://localhost:8080/api/orders/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Get Customer Orders**
```bash
curl -X GET http://localhost:8080/api/orders/customer/testuser \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Cancel Order**
```bash
curl -X POST http://localhost:8080/api/orders/{orderId}/cancel?reason=Customer%20requested \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

Example response:
```json
{
  "id": 12,
  "productId": "laptop-cancel",
  "quantity": 3,
  "price": 500.00,
  "customerId": "canceltest",
  "status": "CANCELLED",
  "createdAt": "2025-11-24T09:41:27.886944"
}
```

**Note**: Cancelling an order triggers compensating transactions:
- Payment is refunded (status: SUCCESS → REFUNDED)
- Inventory stock is restored
- Cancellation notification is sent

### Inventory

**Check Stock**
```bash
curl -X GET http://localhost:8080/api/inventory/LAPTOP-001
```

**Update Stock**
```bash
curl -X PUT http://localhost:8080/api/inventory/LAPTOP-001 \
  -H "Content-Type: application/json" \
  -d '{"quantity": 100}'
```

**Add Stock**
```bash
curl -X POST http://localhost:8080/api/inventory \
  -H "Content-Type: application/json" \
  -d '{"productId": "LAPTOP-001", "quantity": 50}'
```

**Deduct Stock**
```bash
curl -X POST http://localhost:8080/api/inventory/deduct \
  -H "Content-Type: application/json" \
  -d '{"productId": "LAPTOP-001", "quantity": 10}'
```

### Notifications

**Send Manual Notification**
```bash
curl -X POST http://localhost:8080/api/notifications/send \
  -H "Content-Type: application/json" \
  -d '{"orderId": "123", "customerId": "user-1"}'
```

**View Notification History**
```bash
curl -X GET http://localhost:8080/api/notifications
```

### Payments

**Check Payment Status**
```bash
curl -X GET http://localhost:8080/api/payments/order/{orderId}
```

Example response:
```json
{
  "id": 14,
  "orderId": 10,
  "customerId": "paymenttest3",
  "amount": 200.00,
  "status": "SUCCESS",
  "paymentMethod": "CREDIT_CARD",
  "transactionId": "31e609a3-6b3f-4f97-99e9-2304b40db2a7",
  "createdAt": "2025-11-24T09:25:35.088256",
  "updatedAt": "2025-11-24T09:25:35.0938"
}
```

**Note**: Payment is automatically triggered when an order is placed. The payment service simulates:
- **80% Success Rate**: Payment succeeds and inventory is deducted
- **20% Failure Rate**: Payment fails and inventory is NOT deducted (Saga pattern)

## 🏛️ Project Structure

```
order-management-system/
├── api-gateway/              # API Gateway service
│   └── src/main/java/com/enterprise/gateway/
│       ├── filter/          # Authentication filters
│       ├── controller/      # Auth endpoints
│       └── util/           # JWT utilities
├── order-service/           # Order management service
│   └── src/main/java/com/enterprise/order/
│       ├── controller/     # REST controllers
│       ├── service/        # Business logic
│       ├── repository/     # Data access
│       ├── entity/         # JPA entities
│       ├── dto/           # Data transfer objects
│       └── kafka/         # Kafka producers
├── inventory-service/       # Inventory management service
│   └── src/main/java/com/enterprise/inventory/
│       ├── service/       # Business logic
│       ├── kafka/         # Kafka consumers
│       └── repository/    # Data access
├── notification-service/    # Notification service
│   └── src/main/java/com/enterprise/notification/
│       ├── service/       # Notification logic
│       └── kafka/         # Kafka consumers
├── frontend/               # Web UI
│   ├── index.html
│   ├── style.css
│   ├── app.js
│   └── nginx.conf
├── monitoring/             # Monitoring configs
│   ├── prometheus/
│   └── grafana/
├── docker-compose.yml      # Infrastructure setup
└── test-e2e.sh            # End-to-end test script
```

## 🔧 Configuration

### Port Configuration

| Service | Port | Description |
|---------|------|-------------|
| Frontend | 8081 | Web UI |
| API Gateway | 8080 | Entry point |
| Order Service | 8082 | Order management |
| Inventory Service | 8083 | Stock management |
| Payment Service | 8085 | Payment processing |
| Notification Service | 8084 | Notifications |
| Kafka | 19092 | Message broker |
| PostgreSQL | 5435 | Database |
| Redis | 6381 | Cache |
| Prometheus | 9990 | Metrics |
| Grafana | 3000 | Dashboards |
| Zipkin | 9412 | Tracing |
| Zookeeper | 12181 | Kafka coordination |

### Environment Variables

Each service can be configured via `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5435/order_db
    username: admin
    password: password
  kafka:
    bootstrap-servers: localhost:19092
  data:
    redis:
      host: localhost
      port: 6381
```

## 🧪 Testing

### Run Unit Tests
```bash
mvn test
```

### Run Integration Tests
```bash
mvn verify
```

### End-to-End Testing
```bash
./test-e2e.sh
```

## 📊 Observability & Monitoring

The system is integrated with the **ELK Stack (Elasticsearch, Logstash, Kibana)** for centralized logging and **Zipkin** for distributed tracing.

### Centralized Logging (ELK)
All microservices ship logs asynchronously to Logstash, which are then indexed in Elasticsearch and visualized in Kibana.

![ELK Dashboard](docs/elk-dashboard.png)

**Access Points:**
*   **Kibana:** [http://localhost:5601](http://localhost:5601)
*   **Zipkin:** [http://localhost:9411](http://localhost:9411)
*   **Grafana:** [http://localhost:3000](http://localhost:3000)

## 📈 Metrics & Dashboards

### Prometheus Metrics
Access metrics at: http://localhost:9990

Available metrics:
- HTTP request rates
- Response times
- Error rates
- JVM metrics (heap, threads, GC)
- Custom business metrics

### Grafana Dashboards
Access dashboards at: http://localhost:3000 (admin/admin)

Pre-configured dashboards:
- Service health overview
- Request throughput
- Error trends
- JVM monitoring

### Distributed Tracing
Access Zipkin at: http://localhost:9412

Trace requests across all microservices to identify bottlenecks.

## 🔄 Event Flow

### Order Placement Flow (with Payment)

1. **User** submits order via Frontend
2. **API Gateway** validates JWT and routes to Order Service
3. **Order Service**:
   - Saves order to PostgreSQL
   - Caches in Redis
   - Publishes `OrderCreatedEvent` to Kafka
4. **Kafka** distributes event to subscribers
5. **Payment Service**:
   - Consumes `OrderCreatedEvent`
   - Processes payment (80% success, 20% failure)
   - Publishes `PaymentSuccessEvent` or `PaymentFailedEvent`
6. **Inventory Service**:
   - Consumes `PaymentSuccessEvent`
   - Deducts stock levels
7. **Notification Service**:
   - Consumes payment events
   - Sends notification to customer

### Saga Pattern (Payment Failure)
When payment fails, the system demonstrates eventual consistency:
- Payment Service publishes `PaymentFailedEvent`
- Inventory Service does NOT deduct stock
- Order status remains `CREATED` (can be updated to `FAILED` in future enhancement)
- Customer receives failure notification

### Order Cancellation Flow (Compensating Transactions)
When an order is cancelled, the system performs rollback via compensating transactions:

1. **User** cancels order via API
2. **Order Service**:
   - Validates order can be cancelled (not SHIPPED)
   - Updates status to `CANCELLED`
   - Publishes `OrderCancelledEvent` to Kafka
3. **Kafka** distributes event to subscribers
4. **Payment Service** (Compensating Transaction):
   - Finds payment by orderId
   - Validates payment status is `SUCCESS`
   - Updates status to `REFUNDED`
   - Publishes refund notification
5. **Inventory Service** (Compensating Transaction):
   - Restores stock quantity
   - Logs restoration for audit
6. **Notification Service**:
   - Sends cancellation confirmation email

## 🛡️ Resilience Patterns

### Circuit Breaker
```yaml
resilience4j:
  circuitbreaker:
    instances:
      inventory:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 5s
```

### Retry Pattern
Automatic retries for transient failures with exponential backoff.

### Fallback Methods
Graceful degradation when services are unavailable.

## 🚧 Troubleshooting

### Services Won't Start

**Check port conflicts:**
```bash
lsof -i :8080 -i :8082 -i :8083 -i :8084
```

**Verify Docker containers:**
```bash
docker ps
```

### Kafka Connection Issues

**Check Kafka status:**
```bash
docker logs kafka
```

**Verify Kafka topics:**
```bash
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092
```

### Database Connection Issues

**Check PostgreSQL:**
```bash
docker exec postgres psql -U admin -d order_db -c "SELECT 1;"
```

## 📝 Development

### Adding a New Service

1. Create new Maven module
2. Add Spring Boot dependencies
3. Configure in `docker-compose.yml`
4. Update API Gateway routes
5. Add Kafka listeners if needed

### Database Migrations

Using Flyway for version control:

```sql
-- V1__create_orders_table.sql
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    product_id VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    ...
);
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Apache Kafka for reliable messaging
- Resilience4j for fault tolerance patterns
- The open-source community

## 🔗 Related Projects

### 🏙️ [CivicPulse - Smart City Incident Triage System](https://github.com/iam-ssrivastav/civic-pulse)

A complementary project showcasing **AI-powered event-driven architecture** for smart cities.

**Key Highlights:**
- 🤖 **Local AI Integration** - Uses Ollama (Llama3) for automatic incident categorization
- ⚡ **Real-time Processing** - Apache Kafka for event streaming (similar to this project)
- 🔒 **Privacy-First** - 100% local processing, zero cloud costs
- 💰 **Cost Savings** - Saves cities $60,000+/year vs cloud AI solutions
- 📊 **Live Dashboard** - Real-time incident tracking and analysis

**Tech Stack:** Spring Boot, Spring AI, Apache Kafka, PostgreSQL, Ollama

**Use Case:** Citizens report city issues (potholes, broken lights, etc.), and AI automatically categorizes and prioritizes them in seconds.

> 💡 **Perfect companion project** to learn how to integrate AI with event-driven microservices!

---

**Other Projects:**
- 📋 [TaskFlow](https://github.com/iam-ssrivastav/taskflow-app) - Modern task management with WebSockets
- 🎯 [System Design Demo](https://github.com/iam-ssrivastav/system-design-demo) - Design patterns showcase

## 📧 Contact

For questions or support, please open an issue on GitHub.

---

**Built with ❤️ using Spring Boot, Kafka, and modern microservices patterns**

## 👨‍💻 Author

**Shivam Srivastav**

*   **GitHub:** [iam-ssrivastav](https://github.com/iam-ssrivastav)
*   **Role:** Lead Architect & Developer

---
*Built with ❤️ by Shivam Srivastav*
