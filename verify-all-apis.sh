#!/bin/bash

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

echo "========================================="
echo "      API VERIFICATION SCRIPT"
echo "========================================="

# 1. Authentication
echo ""
echo "🔐 1. Authentication"
TOKEN=$(curl -s -X POST "http://localhost:8080/auth/login?username=apitest")
if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
    echo -e "${GREEN}✓ Token generated${NC}"
else
    echo -e "${RED}✗ Failed to generate token${NC}"
    exit 1
fi

# 2. Inventory Service (New APIs)
echo ""
echo "📦 2. Inventory Service"

# Add Stock
echo "   -> Adding stock for 'test-prod-1'..."
ADD_RES=$(curl -X POST http://localhost:8083/api/inventory \
  -H "Content-Type: application/json" \
  -d '{"productId": "test-prod-1", "quantity": 100}')
echo "      Response: $ADD_RES"

# Deduct Stock (Manual)
echo "   -> Deducting stock for 'test-prod-1'..."
curl -X POST http://localhost:8083/api/inventory/deduct \
  -H "Content-Type: application/json" \
  -d '{"productId": "test-prod-1", "quantity": 10}'
echo ""

# Check Stock
echo "   -> Checking stock for 'test-prod-1'..."
GET_RES=$(curl http://localhost:8083/api/inventory/test-prod-1)
echo "      Response: $GET_RES"

if [[ "$GET_RES" == *"90"* ]]; then
     echo -e "${GREEN}✓ Inventory logic correct (100 - 10 = 90)${NC}"
else
     echo -e "${RED}✗ Inventory logic failed${NC}"
fi

# 3. Notification Service (New APIs)
echo ""
echo "🔔 3. Notification Service"

# Send Manual Notification
echo "   -> Sending manual notification..."
SEND_RES=$(curl -X POST http://localhost:8084/api/notifications/send \
  -H "Content-Type: application/json" \
  -d '{"orderId": "manual-order-1", "customerId": "user-1"}')
echo "      Response: $SEND_RES"

sleep 2

# View History
echo "   -> Checking notification history..."
HIST_RES=$(curl http://localhost:8084/api/notifications)
echo "      Response: $HIST_RES"

if [[ "$HIST_RES" == *"manual-order-1"* ]]; then
     echo -e "${GREEN}✓ Notification recorded in history${NC}"
else
     echo -e "${RED}✗ Notification not found in history${NC}"
fi

# 4. Order Service (End-to-End Flow)
echo ""
echo "🛒 4. Order Service (End-to-End)"
echo "   -> Placing an order..."
ORDER_RES=$(curl -s -X POST http://localhost:8082/api/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": "test-prod-1", "quantity": 5, "customerId": "user-1", "price": 100.00}')
echo "      Response: $ORDER_RES"

echo "   -> Waiting for async processing (5s)..."
sleep 5

echo "   -> Checking notification history for order..."
HIST_RES_2=$(curl -s http://localhost:8084/api/notifications)

if [[ "$HIST_RES_2" == *"Your order #"* ]]; then
     echo -e "${GREEN}✓ Async notification received via Kafka${NC}"
else
     echo -e "${RED}✗ Async notification missing (Kafka issue?)${NC}"
fi

echo ""
echo "========================================="
echo "      VERIFICATION COMPLETE"
echo "========================================="
