#!/bin/bash
BASE_URL="http://localhost:8080/api"
EMAIL="realadmin@example.com"
PASSWORD="adminPassword"

# 1. Login
echo "Logging in..."
LOGIN_RES=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")
TOKEN=$(echo "$LOGIN_RES" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "Login failed"
  exit 1
fi

# 2. Create Product
echo "Creating Product..."
CREATE_RES=$(curl -s -X POST "$BASE_URL/products" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
  "name": "Original Name",
  "description": "Original Desc",
  "price": 100.00,
  "stock": 10,
  "category": "Test",
  "imageUrls": ["http://img.com"]
}')
PID=$(echo "$CREATE_RES" | grep -o '"id":"[^"]*' | cut -d'"' -f4)
echo "Created Product ID: $PID"

# 3. Update Product (Partial - just price)
echo "Updating Product (Partial - just price)..."
UPDATE_RES=$(curl -s -X PUT "$BASE_URL/products/$PID" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
  "price": 200.00
}')
echo "Update Response: $UPDATE_RES"

# 4. Fetch Product to verify
echo "Fetching Product..."
GET_RES=$(curl -s -X GET "$BASE_URL/products/$PID")
echo "Final Product State: $GET_RES"
