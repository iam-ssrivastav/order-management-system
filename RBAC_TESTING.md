# RBAC Testing Guide

## Quick Start

### 1. Start the Services

```bash
# Terminal 1 - API Gateway
cd api-gateway
mvn spring-boot:run

# Terminal 2 - Order Service  
cd order-service
mvn spring-boot:run
```

### 2. Test USER Role (Cannot Cancel Orders)

```bash
# Login as regular user
TOKEN_USER=$(curl -s -X POST "http://localhost:8080/auth/login?username=testuser" | tr -d '"')

# Create an order (should succeed)
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN_USER" \
  -d '{
    "productId": "LAPTOP-001",
    "quantity": 1,
    "price": 1200.00,
    "customerId": "testuser"
  }'

# Try to cancel order (should fail with 403 Forbidden)
curl -v -X POST "http://localhost:8080/api/orders/1/cancel?reason=Testing" \
  -H "Authorization: Bearer $TOKEN_USER"

# Expected: HTTP 403 Forbidden
```

### 3. Test ADMIN Role (Can Cancel Orders)

```bash
# Login as admin
TOKEN_ADMIN=$(curl -s -X POST "http://localhost:8080/auth/login?username=admin" | tr -d '"')

# Cancel order (should succeed)
curl -X POST "http://localhost:8080/api/orders/1/cancel?reason=Admin%20testing" \
  -H "Authorization: Bearer $TOKEN_ADMIN"

# Expected: HTTP 200 OK with cancelled order response
```

## Verify JWT Tokens

```bash
# Decode USER token (paste your token)
echo $TOKEN_USER | cut -d'.' -f2 | base64 -d | jq

# Should show: "roles": ["USER"]

# Decode ADMIN token
echo $TOKEN_ADMIN | cut -d'.' -f2 | base64 -d | jq

# Should show: "roles": ["ADMIN", "USER"]
```

## Expected Results

| Action | USER Role | ADMIN Role |
|--------|-----------|------------|
| Create Order | ✅ 200 OK | ✅ 200 OK |
| View Order | ✅ 200 OK | ✅ 200 OK |
| Cancel Order | ❌ 403 Forbidden | ✅ 200 OK |
