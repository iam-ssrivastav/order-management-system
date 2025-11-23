#!/bin/bash
set -e

echo "=== 1. CLEANING UP ENVIRONMENT ==="
# Kill ports
lsof -ti:9092 | xargs kill -9 2>/dev/null || true
lsof -ti:8080 | xargs kill -9 2>/dev/null || true
lsof -ti:8082 | xargs kill -9 2>/dev/null || true
lsof -ti:8083 | xargs kill -9 2>/dev/null || true
lsof -ti:8084 | xargs kill -9 2>/dev/null || true

# Clean Docker
docker-compose down --remove-orphans 2>/dev/null || true

echo "=== 2. STARTING DOCKER STACK ==="
docker-compose up -d

echo "=== 3. WAITING FOR SERVICES (45s) ==="
sleep 45

echo "=== 4. CHECKING STATUS ==="
docker ps

echo "=== 5. RUNNING PROOF TEST ==="
# 1. Auth
TOKEN=$(curl -s -X POST "http://localhost:8080/auth/login?username=finalfix")
echo "Token: ${TOKEN:0:20}..."

# 2. Order
echo "Placing Order..."
ORDER=$(curl -s -X POST -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" -d '{"productId":"LAPTOP-001","quantity":1,"price":1200,"customerId":"finalfix"}' http://localhost:8080/api/orders)
echo "Order Response: $ORDER"

# 3. DB Check
echo "Checking Database..."
docker exec postgres psql -U admin -d order_db -c "SELECT id, status, customer_id FROM orders WHERE customer_id='finalfix';"

echo "=== DONE ==="
