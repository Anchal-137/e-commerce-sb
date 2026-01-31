#!/bin/bash

# Configuration
BASE_URL="http://localhost:8080/api"
EMAIL="testuser_$(date +%s)@example.com"
PASSWORD="password123"

echo "==============================================="
echo "Testing E-Commerce API Endpoints"
echo "==============================================="

# 1. Register a new user
echo ""
echo "[1] Registering new user: $EMAIL"
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"testuser_$(date +%s)\",\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")
echo "Response: $REGISTER_RESPONSE"

# Extract Token (Basic extraction assuming JSON format)
TOKEN=$(echo "$REGISTER_RESPONSE" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "Error: Failed to register or extract token. Trying login..."
  # Try login if register failed (e.g. user exists) - simplified for script
  LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")
  TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
  
  if [ -z "$TOKEN" ]; then
     echo "CRITICAL FAILIURE: Could not get token."
     exit 1
  fi
fi

echo "Token obtained: ${TOKEN:0:15}..."

# 2. Get User Details
echo ""
echo "[2] GET /api/users/me"
curl -s -X GET "$BASE_URL/users/me" \
  -H "Authorization: Bearer $TOKEN" | grep -o '"email":"[^"]*'

# 3. Get Products and extract ID
echo ""
echo ""
echo "[3] GET /api/products"
PRODUCTS_RESPONSE=$(curl -s -X GET "$BASE_URL/products" \
  -H "Authorization: Bearer $TOKEN")
echo "$PRODUCTS_RESPONSE" | head -c 200
echo "..."

PRODUCT_ID=$(echo "$PRODUCTS_RESPONSE" | grep -o '"id":"[^"]*"' | head -n 1 | cut -d'"' -f4)
echo "Found Product ID: $PRODUCT_ID"

# 4. Add to Cart
echo ""
echo "[4] POST /api/cart/items (Add to Cart)"
curl -s -X POST "$BASE_URL/cart/items" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"productId\": \"$PRODUCT_ID\", \"quantity\": 1}"

# 5. Get Cart
echo ""
echo "[5] GET /api/cart"
curl -s -X GET "$BASE_URL/cart" \
  -H "Authorization: Bearer $TOKEN"

# 6. Create Order (Checkout simulation)
echo ""
echo ""
echo "[6] POST /api/orders (Simulated Checkout)"
ORDER_RESPONSE=$(curl -s -X POST "$BASE_URL/orders" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "shippingAddress": {
        "street": "123 Test St",
        "city": "Test City",
        "state": "TS",
        "zipCode": "12345",
        "country": "Testland",
        "phoneNumber": "+1234567890"
    },
    "paymentMethod": "Credit Card"
  }')
echo "Response: $ORDER_RESPONSE"

echo ""
echo "==============================================="
echo "Test Completed Check outputs above."
echo "==============================================="
