# API Documentation

## Table of Contents
1. [Authentication API](#authentication-api)
2. [Order Service API](#order-service-api)
3. [Inventory Service API](#inventory-service-api)
4. [Notification Service API](#notification-service-api)
5. [Error Responses](#error-responses)
6. [Rate Limiting](#rate-limiting)

---

## Base URL

```
http://localhost:8080
```

All API requests go through the API Gateway.

---

## Authentication API

### Generate JWT Token

**Endpoint:** `POST /auth/login`

**Description:** Generate a JWT token for authentication

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| username | string | Yes | User's username |

**Request Example:**
```bash
curl -X POST "http://localhost:8080/auth/login?username=testuser"
```

**Response:**
```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTcwMDAwMDAwMCwiZXhwIjoxNzAwMDAzNjAwfQ.signature
```

**Response Type:** `text/plain`

**Token Expiration:** 30 minutes

**Usage:**
Include the token in the Authorization header for subsequent requests:
```
Authorization: Bearer <token>
```

---

## Order Service API

### 1. Create Order

**Endpoint:** `POST /api/orders`

**Description:** Create a new order

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <jwt_token>
```

**Request Body:**
```json
{
  "productId": "LAPTOP-001",
  "quantity": 2,
  "price": 1200.00,
  "customerId": "testuser"
}
```

**Request Schema:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| productId | string | Yes | Unique product identifier |
| quantity | integer | Yes | Number of items (min: 1) |
| price | decimal | Yes | Price per unit |
| customerId | string | Yes | Customer identifier |

**Response:** `201 Created`
```json
{
  "id": 1,
  "productId": "LAPTOP-001",
  "quantity": 2,
  "price": 1200.00,
  "customerId": "testuser",
  "status": "CREATED",
  "createdAt": "2024-01-01T10:00:00"
}
```

**Response Schema:**
| Field | Type | Description |
|-------|------|-------------|
| id | long | Order ID |
| productId | string | Product identifier |
| quantity | integer | Quantity ordered |
| price | decimal | Price per unit |
| customerId | string | Customer identifier |
| status | string | Order status (CREATED, PROCESSING, COMPLETED, FAILED) |
| createdAt | datetime | Order creation timestamp |

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGc..." \
  -d '{
    "productId": "LAPTOP-001",
    "quantity": 2,
    "price": 1200.00,
    "customerId": "testuser"
  }'
```

**Error Responses:**
- `401 Unauthorized` - Invalid or missing JWT token
- `400 Bad Request` - Invalid request body
- `500 Internal Server Error` - Server error

---

### 2. Get Order by ID

**Endpoint:** `GET /api/orders/{orderId}`

**Description:** Retrieve a specific order by ID

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| orderId | long | Order ID |

**Response:** `200 OK`
```json
{
  "id": 1,
  "productId": "LAPTOP-001",
  "quantity": 2,
  "price": 1200.00,
  "customerId": "testuser",
  "status": "CREATED",
  "createdAt": "2024-01-01T10:00:00"
}
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/orders/1 \
  -H "Authorization: Bearer eyJhbGc..."
```

**Error Responses:**
- `401 Unauthorized` - Invalid or missing JWT token
- `404 Not Found` - Order not found
- `500 Internal Server Error` - Server error

---

### 3. Get Orders by Customer

**Endpoint:** `GET /api/orders/customer/{customerId}`

**Description:** Retrieve all orders for a specific customer

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| customerId | string | Customer identifier |

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "productId": "LAPTOP-001",
    "quantity": 2,
    "price": 1200.00,
    "customerId": "testuser",
    "status": "CREATED",
    "createdAt": "2024-01-01T10:00:00"
  },
  {
    "id": 2,
    "productId": "MOUSE-001",
    "quantity": 1,
    "price": 50.00,
    "customerId": "testuser",
    "status": "COMPLETED",
    "createdAt": "2024-01-02T11:00:00"
  }
]
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/orders/customer/testuser \
  -H "Authorization: Bearer eyJhbGc..."
```

**Error Responses:**
- `401 Unauthorized` - Invalid or missing JWT token
- `200 OK` - Returns empty array if no orders found

---

## Inventory Service API

### 1. Get Product Inventory

**Endpoint:** `GET /api/inventory/{productId}`

**Description:** Get current stock level for a product

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| productId | string | Product identifier |

**Response:** `200 OK`
```json
{
  "id": 1,
  "productId": "LAPTOP-001",
  "productName": "Dell XPS 15",
  "quantity": 50,
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T10:00:00"
}
```

**Response Schema:**
| Field | Type | Description |
|-------|------|-------------|
| id | long | Inventory record ID |
| productId | string | Product identifier |
| productName | string | Product name |
| quantity | integer | Available stock |
| createdAt | datetime | Record creation time |
| updatedAt | datetime | Last update time |

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/inventory/LAPTOP-001
```

**Error Responses:**
- `404 Not Found` - Product not found
- `500 Internal Server Error` - Server error

---

### 2. Update Product Stock

**Endpoint:** `PUT /api/inventory/{productId}`

**Description:** Update stock level for a product

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <jwt_token>
```

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| productId | string | Product identifier |

**Request Body:**
```json
{
  "quantity": 100
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "productId": "LAPTOP-001",
  "productName": "Dell XPS 15",
  "quantity": 100,
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T12:00:00"
}
```

**cURL Example:**
```bash
curl -X PUT http://localhost:8080/api/inventory/LAPTOP-001 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGc..." \
  -d '{"quantity": 100}'
```

**Error Responses:**
- `401 Unauthorized` - Invalid or missing JWT token
- `404 Not Found` - Product not found
- `400 Bad Request` - Invalid quantity
- `500 Internal Server Error` - Server error

---

### 3. Get All Products

**Endpoint:** `GET /api/inventory`

**Description:** Get all products in inventory

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "productId": "LAPTOP-001",
    "productName": "Dell XPS 15",
    "quantity": 50,
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  },
  {
    "id": 2,
    "productId": "MOUSE-001",
    "productName": "Logitech MX Master",
    "quantity": 200,
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  }
]
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/inventory
```

---

## Notification Service API

### Get Notification History

**Endpoint:** `GET /api/notifications/customer/{customerId}`

**Description:** Get notification history for a customer

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| customerId | string | Customer identifier |

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "customerId": "testuser",
    "orderId": 1,
    "message": "Order placed successfully for LAPTOP-001",
    "status": "SENT",
    "sentAt": "2024-01-01T10:00:00"
  }
]
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/notifications/customer/testuser \
  -H "Authorization: Bearer eyJhbGc..."
```

---

## Health & Monitoring Endpoints

### Service Health Check

**Endpoint:** `GET /actuator/health`

**Description:** Check service health status

**Response:** `200 OK`
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "diskSpace": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

**Available for all services:**
- API Gateway: `http://localhost:8080/actuator/health`
- Order Service: `http://localhost:8082/actuator/health`
- Inventory Service: `http://localhost:8083/actuator/health`
- Notification Service: `http://localhost:8084/actuator/health`

---

### Prometheus Metrics

**Endpoint:** `GET /actuator/prometheus`

**Description:** Get Prometheus-formatted metrics

**Response:** `200 OK` (text/plain)
```
# HELP jvm_memory_used_bytes The amount of used memory
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap",id="PS Eden Space",} 1.234567E8
...
```

---

## Error Responses

### Standard Error Format

All error responses follow this format:

```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid request body",
  "path": "/api/orders"
}
```

### HTTP Status Codes

| Code | Description | When Used |
|------|-------------|-----------|
| 200 | OK | Successful GET request |
| 201 | Created | Successful POST request |
| 400 | Bad Request | Invalid request data |
| 401 | Unauthorized | Missing or invalid JWT token |
| 404 | Not Found | Resource not found |
| 500 | Internal Server Error | Server-side error |
| 503 | Service Unavailable | Service temporarily down |

---

## Rate Limiting

**Current Status:** Not implemented

**Future Implementation:**
- 100 requests per minute per user
- 1000 requests per minute per IP
- Response header: `X-RateLimit-Remaining`

---

## Postman Collection

Import this collection to test all endpoints:

```json
{
  "info": {
    "name": "Order Management System",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Authentication",
      "item": [
        {
          "name": "Login",
          "request": {
            "method": "POST",
            "header": [],
            "url": {
              "raw": "http://localhost:8080/auth/login?username=testuser",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["auth", "login"],
              "query": [{"key": "username", "value": "testuser"}]
            }
          }
        }
      ]
    },
    {
      "name": "Orders",
      "item": [
        {
          "name": "Create Order",
          "request": {
            "method": "POST",
            "header": [
              {"key": "Content-Type", "value": "application/json"},
              {"key": "Authorization", "value": "Bearer {{jwt_token}}"}
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"productId\": \"LAPTOP-001\",\n  \"quantity\": 2,\n  \"price\": 1200.00,\n  \"customerId\": \"testuser\"\n}"
            },
            "url": {
              "raw": "http://localhost:8080/api/orders",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "orders"]
            }
          }
        }
      ]
    }
  ]
}
```

---

## WebSocket API (Future Enhancement)

Real-time order updates via WebSocket:

```javascript
const socket = new WebSocket('ws://localhost:8080/ws/orders');

socket.onmessage = (event) => {
  const order = JSON.parse(event.data);
  console.log('Order update:', order);
};
```

---

## API Versioning (Future Enhancement)

```
/api/v1/orders  - Version 1
/api/v2/orders  - Version 2 (with breaking changes)
```

---

## Support

For API questions or issues:
- GitHub Issues: https://github.com/iam-ssrivastav/order-management-system/issues
- Email: support@example.com

---

**Last Updated:** 2024-01-01
**API Version:** 1.0.0
